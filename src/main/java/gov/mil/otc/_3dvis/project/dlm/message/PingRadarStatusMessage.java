package gov.mil.otc._3dvis.project.dlm.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The Ping Radar Status message.  This message is sent by the DLM when the ground sensor algorithm has finalized
 * a launch solution and has triggered the Ordnance to launch the next available TA-CAVM.
 * <p>
 * See the
 * Close Terrain Shaping Obstacle (CTSO) Munition, Wide Area Top Attack, XM204
 * Dispenser Launcher Module (DLM) Internal Message Contents Document.
 */
public class PingRadarStatusMessage extends DlmMessage {

    public static final int MESSAGE_CODE = 0x615B;
    private boolean radarOn;

    /**
     * The Constructor.
     */
    private PingRadarStatusMessage() {
    }

    /**
     * Creates the message from the data byte stream.
     *
     * @param data The message data.
     * @return The decoded message, null if error.
     */
    public static PingRadarStatusMessage create(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        PingRadarStatusMessage pingRadarStatusMessage = new PingRadarStatusMessage();
        if (pingRadarStatusMessage.deserialize(byteBuffer)) {
            return pingRadarStatusMessage;
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
        if (byteBuffer.remaining() >= 1) {
            radarOn = byteBuffer.get() == 0x01;
            return true;
        }
        return false;
    }

    public boolean isRadarOn() {
        return radarOn;
    }
}
