/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import gov.mil.otc._3dvis.settings.SettingsManager;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 *
 * @author hansen
 */
public class VmfMessage {

    // <editor-fold defaultstate="collapsed" desc="Static Members">
    public static int defaultVmfVersion = 15;
    public static int displayTimeMs = 15000;
    public static boolean verboseDecode = false;
    public static boolean showAcks = false;
    public static boolean showRetrans = false;
    public static boolean supportNoPed = true;
    public static boolean bft2Only = false;
    public static boolean showMsgBody = true;
    public static final SimpleDateFormat SDF_LONG = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String cfgKey = "plugin.pcapreader.";
    static final double FEET_TO_METERS = 0.3048;
    static final double LAT_CONVERSION_20BIT = 90.0 / 524287.0;
    static final double LAT_CONVERSION_21BIT = 90.0 / 1048575.0;
    static final double LAT_CONVERSION_22BIT = 90.0 / 2097151.0;
    static final double LAT_CONVERSION_23BIT = 90.0 / 4194303.0;
    static final double LAT_CONVERSION_24BIT = 90.0 / 8388607.0;
    static final double LAT_CONVERSION_25BIT = 90.0 / 16777215.0;
    static final double LAT_CONVERSION_31BIT = 90.0 / 1073741823.0;
    static final double LON_CONVERSION_21BIT = 180.0 / 1048575.0;
    static final double LON_CONVERSION_22BIT = 180.0 / 2097151.0;
    static final double LON_CONVERSION_23BIT = 180.0 / 4194303.0;
    static final double LON_CONVERSION_24BIT = 180.0 / 8388607.0;
    static final double LON_CONVERSION_25BIT = 180.0 / 16777215.0;
    static final double LON_CONVERSION_26BIT = 180.0 / 33554431.0;
    static final double LON_CONVERSION_32BIT = 180.0 / 2147483647.0;
    static final String CR = System.lineSeparator();
    public final int VMF_6017 = 6;
    public final int VMF_6017A = 7;
    public final int VMF_6017B = 8;
    public final int VMF_6017C = 9;
    public final int VMF_6017D = 10;
    // </editor-fold>
    Header header;
    Calendar collectTime;
    String collector;
    boolean parsedOk = false;
    boolean debugging = false;

    public static VmfMessage create(ByteBuffer byteBuffer, int payloadLength, Calendar collectTime, String collector) {
        VmfDataBuffer data = new VmfDataBuffer(byteBuffer, payloadLength);
        Header header = new Header(data);

        return parseData(header, data, collectTime, collector);
    }

    public Header getHeader() {
        return header;
    }

    private static VmfMessage parseData(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        if (header != null && header.msgType != null) {
            try {
                switch (header.msgType) {
                    case K01_01:
                        return new K01_01(header, data, collectTime, collector);
//                    case K03_02:
//                        return new K03_02(header, data, collectTime, collector);
//                    case K03_06:
//                        return new K03_06(header, data, collectTime, collector);
                    case K04_01:
                        return new K04_01(header, data, collectTime, collector);
                    case K05_01:
                        return new K05_01(header, data, collectTime, collector);
                    case K05_17:
                        return new K05_17(header, data, collectTime, collector);
                    case K07_01:
                        return new K07_01(header, data, collectTime, collector);
                    case SDSA:
                        return new Sdsa(header, data, collectTime, collector);
                    case FILE:
                        return new BinaryFile(header, data, collectTime, collector);
                    case OTHER:
                        break;
                    default:
                        break;
                }

                    return new Kxx_xx(header, data, collectTime, collector);  // return header only

            } catch (Exception ex) {
                System.out.println(Arrays.toString(ex.getStackTrace()));
            }
        }
        return null;
    }

    void printf(String fmt, Object... args) {
        if (debugging) {
            System.out.printf(fmt, args);
        }
    }

    public long getCollectionMs() {
        return collectTime.getTimeInMillis();
    }

    public String getText() {
        String text = "";
        if (header != null && header.isValid()) {
            text += header.getShortText();
        }

        return text;
    }

    // summary is meant to be a single line quick preview of the message
    public String getSummary() {
        return header.getName();
    }

    public static void saveStatics() {
        SettingsManager.getSettings().setValue(cfgKey + "defaultVmfVersion", VmfMessage.defaultVmfVersion);
        SettingsManager.getSettings().setValue(cfgKey + "verboseDecode", VmfMessage.verboseDecode);
        SettingsManager.getSettings().setValue(cfgKey + "showAcks", VmfMessage.showAcks);
        SettingsManager.getSettings().setValue(cfgKey + "showRetrans", VmfMessage.showRetrans);
        SettingsManager.getSettings().setValue(cfgKey + "supportNoPed", VmfMessage.supportNoPed);
        SettingsManager.getSettings().setValue(cfgKey + "showMsgBody", VmfMessage.showMsgBody);
        SettingsManager.getSettings().setValue(cfgKey + "bft2Only", VmfMessage.bft2Only);
        SettingsManager.getSettings().setValue(cfgKey + "displayTimeMs", VmfMessage.displayTimeMs);
    }

    public static void loadStatics() {
        VmfMessage.defaultVmfVersion = SettingsManager.getSettings().getInteger(cfgKey + "defaultVmfVersion", VmfMessage.defaultVmfVersion);
        VmfMessage.verboseDecode = SettingsManager.getSettings().getBoolean(cfgKey + "verboseDecode", VmfMessage.verboseDecode);
        VmfMessage.showAcks = SettingsManager.getSettings().getBoolean(cfgKey + "showAcks", VmfMessage.showAcks);
        VmfMessage.showRetrans = SettingsManager.getSettings().getBoolean(cfgKey + "showRetrans", VmfMessage.showRetrans);
        VmfMessage.supportNoPed = SettingsManager.getSettings().getBoolean(cfgKey + "supportNoPed", VmfMessage.supportNoPed);
        VmfMessage.showMsgBody = SettingsManager.getSettings().getBoolean(cfgKey + "showMsgBody", VmfMessage.showMsgBody);
        VmfMessage.bft2Only = SettingsManager.getSettings().getBoolean(cfgKey + "bft2Only", VmfMessage.bft2Only);
        VmfMessage.displayTimeMs = SettingsManager.getSettings().getInteger(cfgKey + "displayTimeMs", VmfMessage.displayTimeMs);
    }
}
