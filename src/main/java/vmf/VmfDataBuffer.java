/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import java.nio.ByteBuffer;

/**
 *
 * @author hansen
 */
public class VmfDataBuffer {

    private static final byte[] BIT_MASK = new byte[]{(byte)0x01, (byte)0x02, (byte)0x04, (byte)0x08, (byte)0x10, (byte)0x20, (byte)0x40, (byte)0x80};
    private static final int[] BIT_MASK32 = new int[32];

    public static final int NO_INT = -1;

    static {
        for (int i = 0; i < 32; i++) {
            BIT_MASK32[i] = 1 << i;
        }
    }

    int bitPosition, bytePosition, numOfBitsRemaining, numOfBytes;
    byte[] stringBuffer = new byte[64];
    ByteBuffer data;

    public VmfDataBuffer(ByteBuffer data, int payloadLength) {
        this.data = data;
        bitPosition = 0;
        bytePosition = data.position();
        numOfBytes = payloadLength;
        numOfBitsRemaining = numOfBytes * 8;
    }

    // <editor-fold defaultstate="collapsed" desc="low level bit handling">
    public boolean getFpi() {

        boolean result = (data.get(bytePosition) & BIT_MASK[bitPosition++]) != 0;

        if ((bitPosition % 8) == 0) {
            bytePosition++;
            bitPosition = 0;
        }
        numOfBitsRemaining--;
        return result;
    }

    public boolean getGri() {
        return getFpi();
    }

    public boolean getGpi() {
        return getFpi();
    }

    public int getFpiInt(int bits) {
        if (getFpi()) {
            return getInt(bits);
        }
        return NO_INT;
    }

    public int getInt(int numOfBits) {

        int i;
        int result = 0;

        /* Make sure we can read from the packet bytes. */
        if (numOfBits > numOfBitsRemaining) {
            /* request for more bits than available. */
            return -1;
        } else {
            /* do nothing */
        }

        /* get the bits */
        for (i = 0; i < numOfBits; i++) {
            if ((data.get(bytePosition) & BIT_MASK[bitPosition++]) != 0) {
                /* set the bit */
                result |= BIT_MASK32[i];
            }

            if ((bitPosition % 8) == 0) {
                bytePosition++;
                bitPosition = 0;
            }
        }

        numOfBitsRemaining -= numOfBits;
        return result;
    }

    public String getFpiString(int bits) {
        if (getFpi()) {
            return getString(bits);
        }
        return "";
    }

    public String getString(int max) {
        if (max > stringBuffer.length * 7) {
            stringBuffer = new byte[max / 7 + 1];
        }
        boolean endOfString = false;
        int bits = 0;
        String result;
        int index = 0;
        while (!endOfString && bits < max) {
            stringBuffer[index] = (byte)getInt(7);
            endOfString = stringBuffer[index++] == 0x7F;
            bits += 7;
        }
        return new String(stringBuffer, 0, index - (endOfString ? 1 : 0));
    }

    public void skip(int bits) {
        bytePosition += bits / 8;
        getInt(bits % 8);
    }

    public void skipPad() {
        // 
        if (bitPosition != 0) {
            bitPosition = 0;
            bytePosition++;
        }

    }

    public int size() {
        return data.limit();
    }
    // </editor-fold>

}
