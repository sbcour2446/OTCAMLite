package gov.mil.otc._3dvis.data.miles;

import java.nio.ByteBuffer;

/**
 * Class representation of a Miles report.
 */
public class EventReport {

    private static final int SIZE = 21;
    private static final byte SYNC = (byte) 0xBB;
    private byte messageId;
    private byte reportSize;
    private short number;
    private EventCode eventCode;
    private byte subCode;
    public byte zone;
    private int position;
    private short playerID;
    private int time;
    private byte hutt;
    private short checkSum;

    public void deserialize(ByteBuffer b) {
        b.get(); // SYNC
        messageId = b.get();
        reportSize = b.get();
        number = b.getShort();
        eventCode = EventCode.deserialize(b);
        subCode = b.get();
        zone = b.get();
        position = b.getInt();
        playerID = b.getShort();
        time = b.getInt();
        hutt = b.get();
        checkSum = b.getShort();
    }

    public EventCode getEventCode() {
        return eventCode;
    }

    public int getSubCode() {
        return subCode;
    }

    public int getAmmo() {
        return playerID / 3300 + 1;
    }

    public int getPid() {
        return playerID % 3300;
    }
}
