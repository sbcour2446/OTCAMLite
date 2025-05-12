package gov.mil.otc._3dvis.project.dlm.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The LMS Software Version message.  This message contains the response to the �Request LMS Software Version� message.
 * <p>
 * See the
 * Close Terrain Shaping Obstacle (CTSO) Munition, Wide Area Top Attack, XM204
 * Dispenser Launcher Module (DLM) Internal Message Contents Document.
 */
public class OrdVersionMessage extends DlmMessage {

    public static final int MESSAGE_CODE = 0xC102;

    private String softwareVersion;

    /**
     * The Constructor.
     */
    private OrdVersionMessage() {
    }

    /**
     * Creates the message from the data byte stream.
     *
     * @param data The message data.
     * @return The decoded message.
     */
    public static OrdVersionMessage create(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        OrdVersionMessage ordVersionMessage = new OrdVersionMessage();
        ordVersionMessage.deserialize(byteBuffer);
        return ordVersionMessage;
    }

    /**
     * Deserialize the data byte stream.
     *
     * @param byteBuffer The message data.
     */
    private void deserialize(ByteBuffer byteBuffer) {
        softwareVersion = new String(byteBuffer.array());
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }
}
