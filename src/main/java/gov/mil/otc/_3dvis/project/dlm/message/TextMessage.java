package gov.mil.otc._3dvis.project.dlm.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * The Text message.
 * <p>
 * See the
 * Close Terrain Shaping Obstacle (CTSO) Munition, Wide Area Top Attack, XM204
 * Dispenser Launcher Module (DLM) Internal Message Contents Document.
 */
public class TextMessage extends DlmMessage {

    public static final int MESSAGE_CODE = 0x6107;

    private String text;

    /**
     * The Constructor.
     */
    private TextMessage() {
    }

    /**
     * Creates the message from the data byte stream.
     *
     * @param data The message data.
     * @return The decoded message.
     */
    public static TextMessage create(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        TextMessage textMessage = new TextMessage();
        textMessage.deserialize(byteBuffer);
        return textMessage;
    }

    /**
     * Deserialize the data byte stream.
     *
     * @param byteBuffer The message data.
     */
    private void deserialize(ByteBuffer byteBuffer) {
        text = Arrays.toString(byteBuffer.array());
    }

    public String getText() {
        return text;
    }
}
