package gov.mil.otc._3dvis.project.dlm.message;

import java.util.Arrays;

/**
 * A generic message to hold a DLM message contents.
 * <p>
 * See the
 * Close Terrain Shaping Obstacle (CTSO) Munition, Wide Area Top Attack, XM204
 * Dispenser Launcher Module (DLM) Internal Message Contents Document.
 */
public class GenericDlmMessage extends DlmMessage {

    private final int messageType;
    private final int time;
    private final byte[] data;
    private final long timestampOverride;
    private final boolean useTimestampOverride;

    /**
     * The Constructor.
     *
     * @param messageType The message type.
     * @param time        The message time.
     * @param data        The data byte stream.
     */
    public GenericDlmMessage(int messageType, int time, byte[] data) {
        this(messageType, time, data, 0, false);
    }

    public GenericDlmMessage(int messageType, int time, byte[] data, long timestampOverride, boolean useTimestampOverride) {
        this.messageType = messageType;
        this.time = time;
        this.data = Arrays.copyOf(data, data.length);
        this.timestampOverride = timestampOverride;
        this.useTimestampOverride = useTimestampOverride;
    }

    public int getMessageType() {
        return messageType;
    }

    public int getTime() {
        return time;
    }

    public byte[] getData() {
        return data;
    }

    public long getTimestampOverride() {
        return timestampOverride;
    }

    public boolean isUseTimestampOverride() {
        return useTimestampOverride;
    }
}
