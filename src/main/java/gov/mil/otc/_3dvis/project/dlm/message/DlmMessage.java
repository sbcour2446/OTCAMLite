package gov.mil.otc._3dvis.project.dlm.message;

import java.nio.ByteBuffer;

/**
 * The abstract message for DLM messages.
 */
public abstract class DlmMessage {

    private static final long EPOCH_OFFSET = 946684800000L;

    /**
     * The Constructor.
     */
    protected DlmMessage() {
    }

    /**
     * Read next four bytes of buffer as an unsigned int.
     *
     * @param byteBuffer The ByteBuffer.
     * @return The unsigned int value as a long.
     */
    protected static long getUnsignedInt(ByteBuffer byteBuffer) {
        return byteBuffer.getInt() & 0xFFFFFFFFL;
    }

    /**
     * Read next two bytes of buffer as an unsigned short.
     *
     * @param byteBuffer The ByteBuffer.
     * @return The unsigned short value as an int.
     */
    protected static int getUnsignedShort(ByteBuffer byteBuffer) {
        return byteBuffer.getShort() & 0xFFFF;
    }

    /**
     * Read next byte of buffer as an unsigned byte.
     *
     * @param byteBuffer The ByteBuffer.
     * @return The unsigned byte value as an int.
     */
    protected static int getUnsignedByte(ByteBuffer byteBuffer) {
        return byteBuffer.get() & 0xFF;
    }

    /**
     * Convert DLM time to unix time.
     *
     * @param seconds      The seconds since January 1, 2000.
     * @param milliseconds The milliseconds of the current second.
     * @return The unix time.
     */
    protected long covertTime(long seconds, int milliseconds) {
        return EPOCH_OFFSET + seconds * 1000 + milliseconds;
    }

    /**
     * Convert DLM time to unix time.
     *
     * @param seconds The seconds since January 1, 2000.
     * @return The unix time.
     */
    protected long covertTime(double seconds) {
        return EPOCH_OFFSET + (long) (seconds * 1000);
    }
}
