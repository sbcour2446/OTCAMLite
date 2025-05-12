/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import gov.mil.otc._3dvis.utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import static vmf.VmfMessage.CR;

/**
 * @author hansen
 */
public class Header {

    protected static final String KEY_FORMAT_EXT = "%7d-%7d-%d%d-%s";
    protected static final String KEY_FORMAT = "%7d-%d%d-%s";

    final int MILSTD47001B = 1;
    final int MILSTD47001C = 2;
    final int MILSTD47001D = 3;
    boolean parseValid = false;
    MsgType msgType;
    //    VmfDataBuffer data;
    int year, month; // used in older messages where only day of the month is sent
    int stdVersion = 6, referenceUrn, senderUrn, version;

    int umf, fad = 0, msgNumber = 0, msgSubtype = 0, dtgExt, dtgRefExt, dtgAckExt;
    int messageSize = 0;
    int operationIndicator, retransmitIndicator, precedenceCode, securityClass;

    boolean machineAckRequest, operatorAckRequest, replyRequest, ack;
    ArrayList<Integer> destUrns = new ArrayList<>();

    ArrayList<Integer> infoUrns = new ArrayList<>();
    ArrayList<String> destNames = new ArrayList<>();
    ArrayList<String> infoNames = new ArrayList<>();
    Calendar responseTime, origninatorTime, referenceTime, perishabilityTime;
    String senderName, filename, controlMarking;

    public int getRetransmitIndicator() {
        return retransmitIndicator;
    }

    public Header(VmfDataBuffer data) {
        responseTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        origninatorTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        referenceTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        perishabilityTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        responseTime.clear();       // make sure the milliseconds field is zero
        origninatorTime.clear();    // make sure the milliseconds field is zero
        referenceTime.clear();      // make sure the milliseconds field is zero
        perishabilityTime.clear();  // make sure the milliseconds field is zero
        ack = false;
        dtgExt = dtgRefExt = dtgAckExt = year = month = -1;
        try {
            parseValid = parse47001Header(data);
        } catch (Exception ex) {

        }

    }

    public String getFilename() {
        return filename;
    }

    public long getOrigninatorTime() {
        return origninatorTime.getTimeInMillis();
    }

    public ArrayList<Integer> getDestUrns() {
        return destUrns;
    }

    public long getResponseTime() {
        return responseTime.getTimeInMillis();
    }

    public boolean isValid() {
        return parseValid;
    }

    public int getSenderUrn() {
        return senderUrn;
    }

    public String getMsgTime() {
        return Utility.formatTime(origninatorTime.getTimeInMillis());
    }

    void setTime(VmfDataBuffer data, Calendar time) {
        time.set(data.getInt(7) + 2000, data.getInt(4) - 1,
                data.getInt(5), data.getInt(5), data.getInt(6), data.getInt(6));
    }

    final boolean parse47001Header(VmfDataBuffer data) {

        senderUrn = 0;
        senderName = "";
        boolean gri, fri;

        try {
            version = data.getInt(4);

            if (version > 4) {
                return false;
            }

            if (data.getFpi()) {
                int compressionType = data.getInt(2);
                return false;
            }

            if (data.getGpi()) {
                if (data.getFpi()) { // urn
                    senderUrn = data.getInt(24);
                }
                if (data.getFpi()) { // unit name
                    senderName = data.getString(448);
                }
            }

            if (data.getGpi()) { // RECIPIENT ADDRESS GROUP
                do {
                    gri = data.getGri();
                    if (data.getFpi()) { // unit name
                        destUrns.add(data.getInt(24));
                    }
                    if (data.getFpi()) { // unit name
                        destNames.add(data.getString(448));
                    }
                } while (gri);
            }

            if (data.getGpi()) { // INFORMATION ADDRESS GROUP
                do {
                    gri = data.getGri();
                    if (data.getFpi()) { // unit name
                        infoUrns.add(data.getInt(24));
                    }
                    if (data.getFpi()) { // unit name
                        infoNames.add(data.getString(448));
                    }
                } while (gri);
            }

            if (version > MILSTD47001B) {
                if (data.getFpi()) { // header size
                    int headerSize = data.getInt(16);
                    if (headerSize > data.size()) {
                        return false;
                    }
                }
                if (version > MILSTD47001C) {
                    skipSizedGroup(data);   //  FUTURE USE  1
                    skipSizedGroup(data);   //  FUTURE USE  2
                    skipSizedGroup(data);   //  FUTURE USE  3
                    skipSizedGroup(data);   //  FUTURE USE  4
                    skipSizedGroup(data);   //  FUTURE USE  5
                }
            }

            do { // MESSAGE HANDLING GROUP
                gri = data.getGri();
                umf = data.getInt(4);
                if (version > MILSTD47001B) {
                    stdVersion = data.getFpiInt(4);
                } else {
                    stdVersion = 6;
                }
                if (data.getGpi()) {
                    fad = data.getInt(4);
                    msgNumber = data.getInt(7);
                    if (data.getFpi()) {
                        msgSubtype = data.getInt(7);
                    }
                }

                if (data.getFpi()) {
                    filename = data.getString(448);
                }
                if (data.getFpi()) {
                    messageSize = data.getInt(20);
                }

            } while (gri); // end // MESSAGE HANDLING GROUP

            operationIndicator = data.getInt(2);
            retransmitIndicator = data.getInt(1);
            precedenceCode = data.getInt(3);
            securityClass = data.getInt(2);

            switch (version) {
                case MILSTD47001B:
                    controlMarking = data.getFpiString(14);
                    break;
                case MILSTD47001C:
                    controlMarking = data.getFpiString(224);
                    break;
                case MILSTD47001D:
                    do {
                        fri = data.getGri();
                        data.getInt(9); // repeating country codes
                    } while (fri);
                    break;
                default:
                    break;
            }

            if (data.getGpi()) { // ORIGINATOR DTG
                setTime(data, origninatorTime);
                if (data.getFpi()) {
                    dtgExt = data.getInt(12);
                }
                year = origninatorTime.get(Calendar.YEAR);
                month = origninatorTime.get(Calendar.MONTH);
            }

            if (data.getGpi()) { // PERISHABILITY DTG
                setTime(data, perishabilityTime);
            }

            if (data.getGpi()) { // ACKNOWLEDGMENT REQUEST GROUP
                machineAckRequest = data.getFpi();
                operatorAckRequest = data.getFpi();
                replyRequest = data.getFpi();
            }

            if (data.getGpi()) { // RESPONSE DATA GROUP (present in all acks)
                ack = true;
                setTime(data, responseTime);
                if (data.getFpi()) {
                    dtgAckExt = data.getInt(12);
                }
                if (data.getFpi()) {
                    int reaponseCode = data.getInt(3);
                }
                if (data.getFpi()) {
                    int cantcoReason = data.getInt(6);
                }
                if (data.getFpi()) {
                    String replyAmp = data.getString(350);
                }
            }

            if (data.getGpi()) { // REFERENCE MESSAGE DATA GROUP
                do {
                    gri = data.getGri();
                    if (data.getFpi()) { // unit name
                        referenceUrn = data.getInt(24);
                    }
                    if (data.getFpi()) { // unit name
                        senderName = data.getString(448);
                    }
                    setTime(data, referenceTime);
                    if (data.getFpi()) {
                        dtgRefExt = data.getInt(12);
                    }
                } while (gri);
            }

            if (version == MILSTD47001B) {
                data.getInt(4); // repeat of FAD and message number ?
                data.getInt(7);
            } else {
                if (version > MILSTD47001C) {
                    skipSizedGroup(data);   //  FUTURE USE  6
                    skipSizedGroup(data);   //  FUTURE USE  7
                    skipSizedGroup(data);   //  FUTURE USE  8
                    skipSizedGroup(data);   //  FUTURE USE  9
                    skipSizedGroup(data);   //  FUTURE USE 10
                }
                skipSecurityGroup(data);
                if (version > MILSTD47001C) {
                    skipSizedGroup(data);   //  FUTURE USE 11
                    skipSizedGroup(data);   //  FUTURE USE 12
                    skipSizedGroup(data);   //  FUTURE USE 13
                    skipSizedGroup(data);   //  FUTURE USE 14
                    skipSizedGroup(data);   //  FUTURE USE 15
                }

            }

            msgType = MsgType.create(umf, fad, msgNumber);
            if (msgType == MsgType.FILE) {
                if (getFilename().toLowerCase().contains(".dsa")) {
                    msgType = MsgType.SDSA;
                }
            }

            data.skipPad();

            return true;

        } catch (Exception exception) {

        }
        return false;
    }

    void skipSizedGroup(VmfDataBuffer data) {
        if (data.getGpi()) {
            int size = data.getInt(12);
            data.skip(size);
        }
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public boolean isAck() {
        return ack;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getDestinations() {
        String dest = CR;
        if (destUrns.size() == 1) {
            dest += "Destination: " + destUrns.get(0);
        } else {
            dest += "Destinations: ";
            int lastCr = 0;
            for (int i : destUrns) {
                dest += "" + i + ',';
                if (dest.length() - lastCr > 50) {
                    lastCr = dest.length();
                    dest += CR;
                }
            }
            dest = dest.replaceAll(",+$", "");
        }
        if (!infoUrns.isEmpty()) {
            dest += CR + "Info:";
            for (int i : infoUrns) {
                dest += " " + i + ',';
            }
        }
        return dest;
    }

    public String getName() {
        return (ack ? "ACK " : "") + msgType.getVmfId() + " " + msgType.getName();
    }

    public String getShortText() {
        String text;
        if (ack) {
            text = getAckText();
        } else {
            text = msgType.getVmfId() + " " + msgType.getName() + CR;
            text += String.format("From: %d  ", senderUrn);
            text += getMsgTime();
            text += getDestinations();
        }
        return text;
    }

    public String getAckText() {
        String text;
        if (ack) {
            text = "ACK " + msgType.getVmfId();
            text += String.format("  %d to %d %s",
                    senderUrn, destUrns.get(0), CR);
            text += String.format("Original Time: %s%s",
                    Utility.formatTime(responseTime.getTimeInMillis()), CR);
            text += String.format("ACK Time: %s%s",
                    getMsgTime(), CR);
        } else {
            text = "";
        }
        return text;
    }

    public String getKey() {
        return String.format(KEY_FORMAT, senderUrn, getOrigninatorTime() / 1000, dtgExt, getMsgType());
    }

    public String getKeyExt(int destUrn) {
        return String.format(KEY_FORMAT_EXT, senderUrn, destUrn, getOrigninatorTime() / 1000, dtgExt, getMsgType());
    }

    public String getAckKey() {
        return String.format("%d-%d-%s-%d", destUrns.get(0), getResponseTime(), getMsgType(), dtgAckExt);
    }

    public String getAckKeyExt() {
        return String.format("%7d-%7d-%d%d-%s", destUrns.get(0), senderUrn, getResponseTime() / 1000, dtgAckExt, getMsgType());
    }

    private void skipSecurityGroup(VmfDataBuffer data) {
        int len;
        boolean fri;
        // the G20 security group
        if (data.getGpi()) {
            data.skip(4);                   // SECURITY PARAMETERS INFORMATION
            if (data.getGpi()) {
                len = data.getInt(3) + 1;   // KEYING MATERIAL ID LENGTH
                data.skip(len * 8);         // KEYING MATERIAL ID
            }
            if (data.getGpi()) {
                len = data.getInt(4) + 1;   // CRYPTOGRAPHIC INITIALIZATION LENGTH
                data.skip(len * 64);        // CRYPTOGRAPHIC INITIALIZATION
            }
            if (data.getGpi()) {
                len = data.getInt(8) + 1;   // KEY TOKEN LENGTH
                do {
                    fri = data.getGri();
                    data.skip(len * 64);    // KEY TOKEN
                } while (fri);
            }
            if (data.getGpi()) {
                len = data.getInt(7) + 1;   // AUTHENTICATION DATA (A) LENGTH
                data.skip(len * 64);        // AUTHENTICATION DATA (A)
            }
            if (data.getGpi()) {
                len = data.getInt(7) + 1;   // AUTHENTICATION DATA (B) LENGTH
                data.skip(len * 64);        // AUTHENTICATION DATA (B)
            }
            data.skip(1);                   // SIGNED ACKNOWLEDGE REQUEST INDICATOR
            if (data.getGpi()) {
                len = data.getInt(8);       // MESSAGE SECURITY PADDING LENGTH
                if (data.getFpi()) {
                    data.skip(len * 8);     // MESSAGE SECURITY PADDING
                }
            }

        }
    }

    public boolean isRequestingAck() {
        return machineAckRequest;
    }
}
