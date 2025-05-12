package gov.mil.otc._3dvis.vmf.pcap;

import java.util.HashMap;

/**
 * Reconstruct an IPV4 Packet
 */
public class Ipv4Assembler {

    private final Packet newPacket;
    private final HashMap<Integer, Integer> dataMap = new HashMap<>();
    private final byte[] assembledBytes = new byte[0xFFFF];
    private int accumulatedBytes;
    private int totalBytes;
    private final int assemblerId;
    private static int seqNumber = 0;

    /**
     * Constructor
     *
     * @param packet  Packet to Assemble
     * @param flags   IPV4 Flags
     * @param dataLen Bytes to process
     */
    public Ipv4Assembler(Packet packet, int flags, int dataLen) {
        newPacket = packet.deepCopy();  // start with the new packet but replace data as needed
        accumulatedBytes = totalBytes = 0;
        assemblerId = seqNumber++;
        add(packet, flags, dataLen);
    }

    /**
     * Append data
     *
     * @param packet  Packet to Assemble
     * @param flags   IPV4 Flags
     * @param dataLen Bytes to process
     */
    final void add(Packet packet, int flags, int dataLen) {
        int fragOffset = (flags & 0x1FFF) * 8;
        boolean moreFrags = (flags & 0x2000) != 0;
        if (!dataMap.containsKey(fragOffset)) {
            System.arraycopy(packet.getBytes(), packet.getData().position(), assembledBytes, fragOffset, dataLen);
            dataMap.put(fragOffset, dataLen);
            if (!moreFrags) {
                totalBytes = fragOffset + dataLen;
            }
            accumulatedBytes += dataLen;
        }

    }

    public int getAssemblerId() {
        return assemblerId;
    }

    /**
     * Determine completeness
     *
     * @return true if complete otherwise, false
     */
    public boolean isComplete() {
        return accumulatedBytes == totalBytes;
    }

    /**
     * Return the assembled IPV4 Packet
     *
     * @return the assembled IPV4 Packet
     */
    public Packet getPacket() {
        // place the assembled bytes into the packet buffer (this duoble buffering is not really needed but easier to debug)
        System.arraycopy(assembledBytes, 0, newPacket.getBytes(), newPacket.getData().position(), totalBytes);
        return newPacket;
    }
}
