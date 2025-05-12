package gov.mil.otc._3dvis.project.dlm.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The Launch Discrete Time message.  This message is sent by the DLM when the ground sensor algorithm has finalized a launch solution
 * and has triggered the Ordnance to launch the next available TA-CAVM. The first parameter is a time stamp that
 * indicates the numbers of seconds in UTC since midnight (00:00:00) January 1, 2000. The second parameter is the
 * number of milliseconds within that second.
 * <p>
 * See the
 * Close Terrain Shaping Obstacle (CTSO) Munition, Wide Area Top Attack, XM204
 * Dispenser Launcher Module (DLM) Internal Message Contents Document.
 */
public class LaunchDiscreteTimeMessage extends DlmMessage {

    public static final int MESSAGE_CODE = 0x6152;

    private long seconds;
    private long milliseconds;

    /**
     * The Constructor.
     */
    private LaunchDiscreteTimeMessage() {
    }

    /**
     * Creates the message from the data byte stream.
     *
     * @param data The message data.
     * @return The decoded message, null if error.
     */
    public static LaunchDiscreteTimeMessage create(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        LaunchDiscreteTimeMessage launchDiscreteTimeMessage = new LaunchDiscreteTimeMessage();
        if (launchDiscreteTimeMessage.deserialize(byteBuffer)) {
            return launchDiscreteTimeMessage;
        }
        return null;
    }

    /**
     * Deserialize the data byte stream.
     *
     * @param byteBuffer The message data.
     * @return True is successful, otherwise false.
     */
    private boolean deserialize(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() >= 8) {
            seconds = getUnsignedInt(byteBuffer);
            milliseconds = getUnsignedInt(byteBuffer);
            return true;
        }
        return false;
    }

    public long getTimestamp() {
        return covertTime(seconds, (int) milliseconds);
    }

    public long getSeconds() {
        return seconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }
}
