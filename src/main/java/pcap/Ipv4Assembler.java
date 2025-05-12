/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package pcap;

import java.util.HashMap;
import pcap.PcapReader.Packet;

/**
 *
 * @author hansen
 */
public class Ipv4Assembler {

    Packet newPacket;
    HashMap<Integer, Integer> dataMap = new HashMap<>();
    byte[] assembledBytes = new byte[0xFFFF];
    int accumulatedBytes, totalBytes, assemblerId;
    static int seqNumber = 0;

    /**
     *
     * @param packet
     * @param flags
     * @param dataLen
     */
    public Ipv4Assembler(Packet packet, int flags, int dataLen) {
        this.newPacket = packet.deepCopy();  // start with the new packet but replace data as needed 
        accumulatedBytes = totalBytes = 0;
        assemblerId = seqNumber++;
        add(packet, flags, dataLen);
    }

    final void add(Packet packet, int flags, int dataLen) {
        int fragOffset = (flags & 0x1FFF) * 8;
        boolean moreFrags = (flags & 0x2000) != 0;
        if (!dataMap.containsKey(fragOffset)) {
            System.arraycopy(packet.bytes, packet.data.position(), assembledBytes, fragOffset, dataLen);
            dataMap.put(fragOffset, dataLen);
            if (!moreFrags) {
                totalBytes = fragOffset + dataLen;
            }
            accumulatedBytes += dataLen;
        }

    }

    boolean isComplete() {
        return accumulatedBytes == totalBytes;
    }

    Packet getPacket() {
        // place the assembled bytes into the packet buffer (this duoble buffering is not really needed but easier to debug)
        System.arraycopy(assembledBytes, 0, newPacket.bytes, newPacket.data.position(), totalBytes);
        return newPacket;
    }

}
