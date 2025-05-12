package gov.mil.otc._3dvis.vmf.pcap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The Packet Class for decoding IP Packets
 */
public class Packet {
    private long id;
    private int interfaceId;
    private int capLen;
    private int origLen;

    private int udpLen = 0;
    private long timestamp;
    private final byte[] bytes;
    private ByteBuffer data;
    private final ArrayList<Integer> udpDest = new ArrayList<>();

    /**
     * Constructor
     */
    public Packet() {
        udpLen = 0;
        bytes = new byte[65536];
        data = ByteBuffer.wrap(bytes);
    }

    /**
     * Constructor
     *
     * @param data Raw Data
     */
    public Packet(byte[] data) {
        bytes = data;
        this.data = ByteBuffer.wrap(bytes);
    }

    /**
     * Constructor
     *
     * @param len Size of Packet to create
     */
    public Packet(int len) {
        udpLen = len;
        bytes = new byte[len];
        data = ByteBuffer.wrap(bytes);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    public int getCapLen() {
        return capLen;
    }

    public void setCapLen(int capLen) {
        this.capLen = capLen;
    }

    public int getOrigLen() {
        return origLen;
    }

    public void setOrigLen(int origLen) {
        this.origLen = origLen;
    }

    public int getUdpLen() {
        return udpLen;
    }

    public void setUdpLen(int udpLen) {
        this.udpLen = udpLen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public List<Integer> getUdpDest() {
        return udpDest;
    }

    /**
     * Copy a Packet
     *
     * @return Packet Object
     */
    Packet deepCopy() {
        Packet copy = new Packet(bytes.length);
        copy.id = id;
        copy.udpLen = udpLen;
        copy.timestamp = timestamp;
        copy.interfaceId = interfaceId;
        copy.capLen = capLen;
        copy.origLen = origLen;
        System.arraycopy(bytes, 0, copy.bytes, 0, copy.bytes.length);
        copy.data = ByteBuffer.wrap(copy.bytes);
        copy.data.position(data.position());
        return copy;
    }
}