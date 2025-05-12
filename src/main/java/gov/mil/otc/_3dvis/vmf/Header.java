package gov.mil.otc._3dvis.vmf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Header Class
 */
public class Header {

    protected static final String KEY_FORMAT_EXT = "%7d-%7d-%d%d-%s";
    protected static final String KEY_FORMAT = "%7d-%d%d-%s";
    protected static final String ACK_FORMAT_KEY = "%d-%d-%s-%d";

    private static final int MILSTD_47001_B = 1;
    private static final int MILSTD_47001_C = 2;
    private static final int MILSTD_47001_D = 3;
    private boolean parseValid = false;
    private MessageType messageType;
    //    VmfDataBuffer data;
    private int year;
    private int month; // used in older messages where only day of the month is sent
    private int stdVersion = 6;
    private int referenceUrn;
    private int senderUrn;
    private int version;
    private int umf;
    private int fad = 0;
    private int messageNumber = 0;
    private int messageSubtype = 0;
    private int dtgExt;
    private int dtgRefExt;
    private int dtgAckExt;
    private int messageSize = 0;
    private int operationIndicator;
    private int retransmitIndicator;
    private int precedenceCode;
    private int securityClass;

    private boolean machineAckRequest;
    private boolean operatorAckRequest;
    private boolean replyRequest;
    private boolean ack;
    private final List<Integer> destUrns = new ArrayList<>();
    private final List<Integer> infoUrns = new ArrayList<>();
    private final List<String> destNames = new ArrayList<>();
    private final List<String> infoNames = new ArrayList<>();
    private final Calendar responseTime;
    private final Calendar originatorTime;
    private final Calendar referenceTime;
    private final Calendar perishabilityTime;
    private String senderName;
    private String filename;
    private String controlMarking;

    public int getRetransmitIndicator() {
        return retransmitIndicator;
    }

    public Header(VmfDataBuffer data) {
        responseTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        originatorTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        referenceTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        perishabilityTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        responseTime.clear();       // make sure the milliseconds field is zero
        originatorTime.clear();    // make sure the milliseconds field is zero
        referenceTime.clear();      // make sure the milliseconds field is zero
        perishabilityTime.clear();  // make sure the milliseconds field is zero
        ack = false;
        dtgExt = dtgRefExt = dtgAckExt = year = month = -1;
        try {
            parseValid = parse47001Header(data);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "parse47001Header() Exception: ", e);
        }
    }

    public String getFilename() {
        return filename;
    }

    public long getOriginatorTime() {
        return originatorTime.getTimeInMillis();
    }

    public List<Integer> getDestUrns() {
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

    public String getMessageTime() {
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdfLong.format(originatorTime.getTime());
    }

    private void setTime(VmfDataBuffer data, Calendar time) {
        time.set(data.getInt(7) + 2000, data.getInt(4) - 1,
                data.getInt(5), data.getInt(5), data.getInt(6), data.getInt(6));
    }

    private boolean parse47001Header(VmfDataBuffer data) {

        senderUrn = 0;
        senderName = "";
        boolean gri;
        boolean fri;

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

            if (version > MILSTD_47001_B) {
                if (data.getFpi()) { // header size
                    int headerSize = data.getInt(16);
                    if (headerSize > data.size()) {
                        return false;
                    }
                }
                if (version > MILSTD_47001_C) {
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
                if (version > MILSTD_47001_B) {
                    stdVersion = data.getFpiInt(4);
                } else {
                    stdVersion = 6;
                }
                if (data.getGpi()) {
                    fad = data.getInt(4);
                    messageNumber = data.getInt(7);
                    if (data.getFpi()) {
                        messageSubtype = data.getInt(7);
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
                case MILSTD_47001_B:
                    controlMarking = data.getFpiString(14);
                    break;
                case MILSTD_47001_C:
                    controlMarking = data.getFpiString(224);
                    break;
                case MILSTD_47001_D:
                    do {
                        fri = data.getGri();
                        data.getInt(9); // repeating country codes
                    } while (fri);
                    break;
                default:
                    break;
            }

            if (data.getGpi()) { // ORIGINATOR DTG
                setTime(data, originatorTime);
                if (data.getFpi()) {
                    dtgExt = data.getInt(12);
                }
                year = originatorTime.get(Calendar.YEAR);
                month = originatorTime.get(Calendar.MONTH);
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

            if (version == MILSTD_47001_B) {
                data.getInt(4); // repeat of FAD and message number ?
                data.getInt(7);
            } else {
                if (version > MILSTD_47001_C) {
                    skipSizedGroup(data);   //  FUTURE USE  6
                    skipSizedGroup(data);   //  FUTURE USE  7
                    skipSizedGroup(data);   //  FUTURE USE  8
                    skipSizedGroup(data);   //  FUTURE USE  9
                    skipSizedGroup(data);   //  FUTURE USE 10
                }
                skipSecurityGroup(data);
                if (version > MILSTD_47001_C) {
                    skipSizedGroup(data);   //  FUTURE USE 11
                    skipSizedGroup(data);   //  FUTURE USE 12
                    skipSizedGroup(data);   //  FUTURE USE 13
                    skipSizedGroup(data);   //  FUTURE USE 14
                    skipSizedGroup(data);   //  FUTURE USE 15
                }

            }

            messageType = MessageType.create(umf, fad, messageNumber);
            if ((messageType == MessageType.FILE) && (getFilename().toLowerCase().contains(".dsa"))) {
                messageType = MessageType.SDSA;
            }

            data.skipPad();

            return true;

        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "parse47001Header() Exception: ", e);
        }
        return false;
    }

    void skipSizedGroup(VmfDataBuffer data) {
        if (data.getGpi()) {
            int size = data.getInt(12);
            data.skip(size);
        }
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public boolean isAck() {
        return ack;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getDestinations() {
        String dest = System.lineSeparator();
        if (destUrns.size() == 1) {
            dest += "Destination: " + destUrns.get(0);
        } else {
            dest += "Destinations: ";
            int lastCr = 0;
            for (int i : destUrns) {
                dest += "" + i + ',';
                if (dest.length() - lastCr > 50) {
                    lastCr = dest.length();
                    dest += System.lineSeparator();
                }
            }
            dest = dest.replaceAll(",+$", "");
        }
        if (!infoUrns.isEmpty()) {
            dest += System.lineSeparator() + "Info:";
            for (int i : infoUrns) {
                dest += " " + i + ',';
            }
        }
        return dest;
    }

    public String getName() {
        return (ack ? "ACK " : "") + messageType.getVmfId() + " " + messageType.getName();
    }

    public String getShortText() {
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String text;
        if (ack) {
            text = getAckText();
        } else {
            text = messageType.getVmfId() + " " + messageType.getName() + System.lineSeparator();
            text += String.format("From: %d  ", senderUrn);
            text += sdfLong.format(originatorTime.getTime());
            text += getDestinations();
        }
        return text;
    }

    public String getAckText() {
        SimpleDateFormat sdfShort = new SimpleDateFormat("HH:mm:ss");
        String text;
        if (ack) {
            text = "ACK " + messageType.getVmfId();
            text += String.format("  %d to %d %s",
                    senderUrn, destUrns.get(0), System.lineSeparator());
            text += String.format("Original Time: %s%s",
                    sdfShort.format(responseTime.getTime()), System.lineSeparator());
            text += String.format("ACK Time: %s%s",
                    sdfShort.format(originatorTime.getTime()), System.lineSeparator());
        } else {
            text = "";
        }
        return text;
    }

    public String getKey() {
        return String.format(KEY_FORMAT, senderUrn, getOriginatorTime() / 1000, dtgExt, getMessageType());
    }

    public String getKeyExt(int destUrn) {
        return String.format(KEY_FORMAT_EXT, senderUrn, destUrn, getOriginatorTime() / 1000, dtgExt, getMessageType());
    }

    public String getAckKey() {
        return String.format(ACK_FORMAT_KEY, destUrns.get(0), getResponseTime(), getMessageType(), dtgAckExt);
    }

    public String getAckKeyExt() {
        return String.format(KEY_FORMAT_EXT, destUrns.get(0), senderUrn, getResponseTime() / 1000, dtgAckExt, getMessageType());
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

    public boolean isParseValid() {
        return parseValid;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getStdVersion() {
        return stdVersion;
    }

    public int getReferenceUrn() {
        return referenceUrn;
    }

    public int getVersion() {
        return version;
    }

    public int getUmf() {
        return umf;
    }

    public int getFad() {
        return fad;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public int getMessageSubtype() {
        return messageSubtype;
    }

    public int getDtgExt() {
        return dtgExt;
    }

    public int getDtgRefExt() {
        return dtgRefExt;
    }

    public int getDtgAckExt() {
        return dtgAckExt;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public int getOperationIndicator() {
        return operationIndicator;
    }

    public int getPrecedenceCode() {
        return precedenceCode;
    }

    public int getSecurityClass() {
        return securityClass;
    }

    public boolean isMachineAckRequest() {
        return machineAckRequest;
    }

    public boolean isOperatorAckRequest() {
        return operatorAckRequest;
    }

    public boolean isReplyRequest() {
        return replyRequest;
    }

    public List<Integer> getInfoUrns() {
        return infoUrns;
    }

    public List<String> getDestNames() {
        return destNames;
    }

    public List<String> getInfoNames() {
        return infoNames;
    }

    public Calendar getReferenceTime() {
        return referenceTime;
    }

    public Calendar getPerishabilityTime() {
        return perishabilityTime;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getControlMarking() {
        return controlMarking;
    }
}
