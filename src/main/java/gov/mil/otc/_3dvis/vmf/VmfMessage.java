package gov.mil.otc._3dvis.vmf;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VMF Message Class
 */
public class VmfMessage {

    public static final double FEET_TO_METERS = 0.3048;
    public static final double LAT_CONVERSION_21BIT = 90.0 / 1048575.0;
    public static final double LAT_CONVERSION_23BIT = 90.0 / 4194303.0;
    public static final double LAT_CONVERSION_25BIT = 90.0 / 16777215.0;
    public static final double LON_CONVERSION_22BIT = 180.0 / 2097151.0;
    public static final double LON_CONVERSION_24BIT = 180.0 / 8388607.0;
    public static final double LON_CONVERSION_26BIT = 180.0 / 33554431.0;
    public static final int VMF_6017 = 6;
    public static final int VMF_6017A = 7;
    public static final int VMF_6017B = 8;
    public static final int VMF_6017C = 9;
    public static final int VMF_6017D = 10;
    public static final boolean SUPPORT_NO_PED = true;

    protected Header header;
    protected Calendar collectTime;
    protected String collector;
    protected boolean parsedOk = false;

    /**
     * Create a VmfMessage Object from parameters
     *
     * @param byteBuffer    UDP VMF Payload
     * @param payloadLength Length of Payload
     * @param collectTime   Time of Collection
     * @param collector     Data collector name
     * @return a parsed and populated VmfMessage Object
     */
    public static VmfMessage create(ByteBuffer byteBuffer, int payloadLength, Calendar collectTime, String collector) {
        VmfDataBuffer data = new VmfDataBuffer(byteBuffer, payloadLength);
        Header header = new Header(data);

        return parseData(header, data, collectTime, collector);
    }

    /**
     * Parse the VMF Message and populate the VmfMessage Object
     *
     * @param header      Header Object
     * @param data        UDP VMF Payload
     * @param collectTime Time of Collection
     * @param collector   Data collector name
     * @return a parsed and populated VmfMessage Object
     */
    private static VmfMessage parseData(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        VmfMessage vmfMessage = null;
        if (header != null && header.getMessageType() != null) {
            try {
                vmfMessage = switch (header.getMessageType()) {
                    case K01_01 -> new K0101(header, data, collectTime, collector);
                    case K03_02 -> new K0302(header, data, collectTime, collector);
                    case K03_06 -> new K0306(header, data, collectTime, collector);
                    case K04_01 -> new K0401(header, data, collectTime, collector);
                    case K05_01 -> new K0501(header, data, collectTime, collector);
                    case K05_17 -> new K0517(header, data, collectTime, collector);
                    case K07_01 -> new K0701(header, data, collectTime, collector);
                    case SDSA -> new Sdsa(header, data, collectTime, collector);
                    case FILE -> new BinaryFile(header, data, collectTime, collector);
                    default -> new Unknown(header, data, collectTime, collector);  // return header only
                };
                vmfMessage.parsedOk = vmfMessage.parse(data);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "parseData() Failed: ", e);
            }
        }
        return vmfMessage;
    }

    /**
     * Constructor
     *
     * @param header VMF Message Header
     * @param data VMF Message data
     * @param collectTime Time of Collection
     * @param collector Data Collection Device
     */
    protected VmfMessage(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
    }

    /**
     * Message Parser
     *
     * @param data VmfDataBuffer data
     * @return true if successful parse otherwise, false
     */
    protected boolean parse(VmfDataBuffer data) {
        return true;
    }

    public boolean isParsedOk() {
        return parsedOk;
    }

    /**
     * Return the Header
     *
     * @return Header Object
     */
    public Header getHeader() {
        return header;
    }

    /**
     * Utility print function
     *
     * @param fmt  Print Format Specifier String
     * @param args Print Format Specifier Arguments
     */
    void printf(String fmt, Object... args) {
        String message = String.format(fmt, args);
        Logger.getGlobal().log(Level.FINE, message);
    }

    /**
     * Return Collection time in ms
     *
     * @return Collection time in ms
     */
    public long getCollectionMs() {
        return collectTime.getTimeInMillis();
    }

    /**
     * Return the Message Header Text
     *
     * @return Message Header Text
     */
    public String getText() {
        String text = "";
        if (header != null && header.isValid()) {
            text += header.getShortText();
        }

        return text;
    }

    /**
     * Return a single line quick preview of the message
     *
     * @return a single line quick preview of the message
     */
    public String getSummary() {
        return header.getName();
    }
}
