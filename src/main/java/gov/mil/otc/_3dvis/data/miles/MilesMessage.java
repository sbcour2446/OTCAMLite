package gov.mil.otc._3dvis.data.miles;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Class representation of a MILES message.
 */
public class MilesMessage {

    private short pid;           // ASCII MILES PID
    private byte vehicle;        // Vehicle = 1 PDD = 0
    private final EventReport report = new EventReport();

    /**
     * Deserializes a byte buffer into this {@link MilesMessage}.
     *
     * @param b The byte buffer to populate this object with.
     */
    public void deserialize(ByteBuffer b) {
        b.order(ByteOrder.BIG_ENDIAN);
        pid = b.getShort();
        vehicle = b.get();
        report.deserialize(b);
        b.order(ByteOrder.LITTLE_ENDIAN);
    }

    public short getPid() {
        return pid;
    }

    public byte getVehicle() {
        return vehicle;
    }

    public EventReport getReport() {
        return report;
    }
}
