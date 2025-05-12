/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package pcap;

import java.io.File;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pcap.PcapReader.Packet;

/**
 *
 * @author hansen
 */
public class MdpAssembler {

    long streamId;
    int accumulatedBytes, expectedBytes;                            // total message length
    static HashMap<Long, MdpAssembler> streams = new HashMap<>();   // contains all current Assemblers 
    HashMap<Integer, MdpBlock> blockMap = new HashMap<>();          // a stream is an 
    static boolean isSupported = false;                             // Only true if the DLL loads

    static {
        try {
            String filename = System.getProperty("user.dir") + "/MdpDecoder.dll";
            if (new File(filename).exists()) {
                System.load(filename);
                isSupported = true;
            }
        } catch (java.lang.UnsatisfiedLinkError | Exception ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
    }

    int getLength() {
        return expectedBytes;
    }

    static void remove(MdpAssembler mdpAssembler) {
        streams.remove(mdpAssembler.streamId);

    }

    /*
    MdpPacket holds data for eventual reassembly into MdpBlocks
     */
    static public class MdpPacket {

        byte type, version;
        int nodeId;
        long streamId;
        int sequence, object_id, object_size, offset, segment_size, payloadSize, grtt;
        byte ndata, nparity, flags, parityId;
        byte[] data;
        boolean isRunt;

        public MdpPacket(Packet packet, int dataLen) {
            // six bytes of common header
            type = packet.data.get();
            version = packet.data.get();
            nodeId = packet.data.getInt();
            streamId = ((long) nodeId) << 32;
            if (isData() || isParity()) {
//                int payloadLen = 0;
                // decode the first 18 bytes
//                int payloadSize;
                sequence = packet.data.getShort();
                object_id = packet.data.getInt();
                streamId |= object_id;
                object_size = packet.data.getInt();
                ndata = packet.data.get();
                nparity = packet.data.get();
                flags = packet.data.get();
                isRunt = (flags & 0x04) == 0x04;
                grtt = ((int) packet.data.get()) & 0xFF;
                offset = packet.data.getInt();
                // subtract UDP header + MDP common header + parity header (8 + 6 + 18)
                payloadSize = segment_size = dataLen - 32;
                // now check if this is a runt or the segment size is wrong 
                if (isParity()) {
                    parityId = packet.data.get();
                    payloadSize -= 1;
                    segment_size = payloadSize;
                } else if (isRunt) {
                    // subtract UDP header + MDP common header + parity header (8 + 6 + 19)
                    segment_size = ((int) packet.data.getShort()) & 0xFFFF;
                    payloadSize -= 2;
                }
                data = new byte[segment_size];  // use segment size instead of payload size to zero fill runts
                packet.data.get(data, 0, payloadSize);

            } else {
                // for now we don't use other packet types
            }

        }

        public final boolean isData() {
            return type == 3;
        }

        public final boolean isParity() {
            return type == 4;
        }

        private int getBlockOffset() {
            if (isParity()) {
                return offset;
            } else {
                int blockSize = ndata * segment_size; // the maximum block size
                return (offset / blockSize) * blockSize; // get the block offset
            }
        }

    }

    /* 
    MdpBlock represents a block within the MDP stream. Once the required number of 
    packets (segments) is stored, then block decoding can take place
     */
    public class MdpBlock {

        int offset, bytesAvailable, bytesExpected, count, countExpected;
        int nData, nParity, size, segmentSize;
        ArrayList<MdpPacket> packets = new ArrayList<>();

        public MdpBlock(MdpPacket pkt) {
//            this.offset = offset;
            this.nData = pkt.ndata;
            this.nParity = pkt.nparity;
            this.segmentSize = pkt.segment_size;
            size = pkt.ndata * pkt.segment_size; // the maximum block size
            this.offset = pkt.isParity() ? pkt.offset : (pkt.offset / size) * size; // get the block offset
            bytesAvailable = count = 0;
            bytesExpected = min(size, pkt.object_size - offset);
            countExpected = bytesExpected / pkt.segment_size + (pkt.isRunt ? 1 : 0);
            for (int i = 0; i < nData + nParity; i++) {
                packets.add(null);
            }
            add(pkt);
        }

        public byte[] getData() {

            // check for data loss (erasures)
            boolean erasures = false;
            for (int i = 0; i < countExpected; i++) {
                erasures |= (packets.get(i) == null);
            }

            if (!erasures) {
                // if their is no loss, then just copy the data
                byte[] data = new byte[size];
                for (int i = 0; i < countExpected; i++) {
                    System.arraycopy(packets.get(i).data, 0, data, packets.get(i).segment_size * i, packets.get(i).payloadSize);
                }
                return data;
            } else {
                if (isSupported) {
                    byte[] vectors[] = new byte[nData + nParity][];
                    for (int i = 0; i < vectors.length; i++) {
                        vectors[i] = packets.get(i) == null ? null : packets.get(i).data;
                    }

                    System.out.println("calling mdpDecode");
                    try {
                        // some packets lost, need to call the error correction routime
                        return mdpDecode(vectors, nParity, this.segmentSize, null); // TODO
                    } catch (java.lang.UnsatisfiedLinkError ex) {
                        Logger.getGlobal().log(Level.SEVERE, null, ex);
                        byte[] data = new byte[size];
                        return data;
                    }
                } else {
                    byte[] data = new byte[size];
                    return data;
                }
            }
        }

        public final void add(MdpPacket packet) {
            int index;
            if (packet.isParity()) {
                index = packet.parityId;
            } else if (packet.isData()) {
                index = (packet.offset - offset) / packet.segment_size;
            } else {
                return;
            }
            if (packets.get(index) == null) {
                packets.set(index, packet);
                count++;
                bytesAvailable = min(count, nData) * packet.segment_size;
            }

        }

    }

    static public MdpAssembler process(Packet packet, int dataLen) {

        // decode the header and create an MDP packet
        MdpPacket mdpPacket = new MdpPacket(packet, dataLen);

        // get or create the apprppriate stream
        if (mdpPacket.isData() || mdpPacket.isParity()) {

            MdpAssembler mdpAssembler = streams.get(mdpPacket.streamId);

            if (mdpAssembler == null) {
                mdpAssembler = new MdpAssembler(mdpPacket, dataLen);
                streams.put(mdpPacket.streamId, mdpAssembler);
            } else {
                mdpAssembler.add(mdpPacket, dataLen);
            }

            return mdpAssembler;
        }

        return null;
    }

    /**
     *
     * @param packet
     * @param flags
     * @param dataLen
     */
    private MdpAssembler(MdpPacket mdpPacket, int dataLen) {

        accumulatedBytes = 0;
        expectedBytes = mdpPacket.object_size;
        streamId = mdpPacket.streamId;
        add(mdpPacket, dataLen);
    }

    /**
     * mdpDecode is just the block repair function for blocks with missing
     * segments do not call if all the data segments are present
     *
     * @param blocks an array of blocks
     * @param numParity number of parity packets per block
     * @param vecSizeMax the segment size
     * @param erasureMask Not used
     * @return
     */
    public native byte[] mdpDecode(Object[] blocks, int numParity, int vecSizeMax, byte[] erasureMask);

    final void add(MdpPacket packet, int dataLen) {

        int offset = packet.getBlockOffset();

        // find the right block and add data or create a new block
        MdpBlock block = blockMap.get(offset);
        if (block == null) {
            block = new MdpBlock(packet);
            blockMap.put(offset, block);
        } else {
            // add this packet to the appropriate block
            block.add(packet);
        }

        // sum up the total bytes available
        accumulatedBytes = 0;
        for (MdpBlock b : blockMap.values()) {
            accumulatedBytes += b.bytesAvailable;
        }

    }

    boolean isComplete() {
        return accumulatedBytes >= expectedBytes;
    }

    Packet getData(long timeStamp) {

        Packet newPacket = new Packet(accumulatedBytes);
        newPacket.timestamp = timeStamp;
        int offset = 0;

        for (MdpBlock b : blockMap.values()) {
            try {
                System.arraycopy(b.getData(), 0, newPacket.bytes, b.offset, b.bytesAvailable);
            } catch (Exception ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
                return null;
            }
        }

        newPacket.udpLen = expectedBytes;

        return newPacket;
    }

}
