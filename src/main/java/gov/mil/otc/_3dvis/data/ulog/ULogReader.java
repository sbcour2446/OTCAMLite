package gov.mil.otc._3dvis.data.ulog;

import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;
import jdk.jshell.execution.Util;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ULogReader extends BinaryLogReader {

    private static final byte MESSAGE_TYPE_FORMAT = (byte) 'F';
    private static final byte MESSAGE_TYPE_DATA = (byte) 'D';
    private static final byte MESSAGE_TYPE_INFO = (byte) 'I';
    private static final byte MESSAGE_TYPE_INFO_MULTIPLE = (byte) 'M';
    private static final byte MESSAGE_TYPE_PARAMETER = (byte) 'P';
    private static final byte MESSAGE_TYPE_PARAMETER_DEFAULT = (byte) 'Q';
    private static final byte MESSAGE_TYPE_ADD_LOGGED_MSG = (byte) 'A';
    private static final byte MESSAGE_TYPE_REMOVE_LOGGED_MSG = (byte) 'R';
    private static final byte MESSAGE_TYPE_SYNC = (byte) 'S';
    private static final byte MESSAGE_TYPE_DROPOUT = (byte) 'O';
    private static final byte MESSAGE_TYPE_LOG = (byte) 'L';
    private static final byte MESSAGE_TYPE_FLAG_BITS = (byte) 'B';
    private static final int HDRLEN = 3;
    private static final int FILE_MAGIC_HEADER_LENGTH = 16;
    private static final int INCOMPAT_FLAG0_DATA_APPENDED_MASK = 1;

    private String systemName = "PX4";
    private long dataStart = 0;
    private final Map<String, MessageFormat> messageFormats = new HashMap<String, MessageFormat>();

    private static class Subscription {
        public Subscription(MessageFormat f, int multiID) {
            this.format = f;
            this.multiID = multiID;
        }

        public MessageFormat format;
        public int multiID;
    }

    private final ArrayList<Subscription> messageSubscriptions = new ArrayList<>();

    private Map<String, String> fieldsList = null;
    private long sizeUpdates = -1;
    private long sizeMicroseconds = -1;
    private long startMicroseconds = -1;
    private long utcTimeReference = -1;
    private long logStartTimestamp = -1;
    private boolean nestedParsingDone = false;
    private final Map<String, Object> version = new HashMap<String, Object>();
    private final Map<String, Object> parameters = new HashMap<String, Object>();
    public ArrayList<MessageLog> loggedMessages = new ArrayList<>();
    private String hardfaultPlainText = "";
    private Vector<Long> appendedOffsets = new Vector<>();
    private int currentAppendingOffsetIndex = 0;
    public Map<String, List<ParamUpdate>> parameterUpdates;
    private boolean replayedLog = false;

    public static class ParamUpdate {
        private final String name;
        private final Object value;
        private long timestamp = -1;

        private ParamUpdate(String nm, Object v, long ts) {
            name = nm;
            value = v;
            timestamp = ts;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private final List<Exception> errors = new ArrayList<Exception>();
    private int logVersion = 0;
    private int headerSize = 2;

    /**
     * Index for fast(er) seeking
     */
    private ArrayList<SeekTime> seekTimes = null;

    private static class SeekTime {

        public long timestamp;
        public long position;

        public SeekTime(long t, long pos) {
            timestamp = t;
            position = pos;
        }
    }

    public ULogReader(File file) throws IOException, FormatErrorException {
        super(file);
        readFileHeader();
        parameterUpdates = new HashMap<String, List<ParamUpdate>>();
        updateStatistics();
    }

    @Override
    public String getFormat() {
        return "ULog v" + logVersion;
    }

    public String getSystemName() {
        return systemName;
    }

    @Override
    public long getSizeUpdates() {
        return sizeUpdates;
    }

    @Override
    public long getStartMicroseconds() {
        return startMicroseconds;
    }

    @Override
    public long getSizeMicroseconds() {
        return sizeMicroseconds;
    }

    @Override
    public long getUTCTimeReferenceMicroseconds() {
        return utcTimeReference;
    }

    @Override
    public Map<String, Object> getVersion() {
        return version;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Read and parse the file header.
     */
    private void readFileHeader() throws IOException, FormatErrorException {
        fillBuffer(FILE_MAGIC_HEADER_LENGTH);
        //magic + version
        boolean error = false;
        if ((buffer.get() & 0xFF) != 'U') {
            error = true;
        }
        if ((buffer.get() & 0xFF) != 'L') {
            error = true;
        }
        if ((buffer.get() & 0xFF) != 'o') {
            error = true;
        }
        if ((buffer.get() & 0xFF) != 'g') {
            error = true;
        }
        if ((buffer.get() & 0xFF) != 0x01) {
            error = true;
        }
        if ((buffer.get() & 0xFF) != 0x12) {
            error = true;
        }
        if ((buffer.get() & 0xFF) != 0x35) {
            error = true;
        }
        if ((buffer.get() & 0xFF) > 0x01 && !error) {
            Logger.getGlobal().log(Level.WARNING, "ULogReader::readFileHeader:different version than expected");
        }
        if (error) {
            throw new FormatErrorException("ULog: Wrong file format");
        }

        logStartTimestamp = buffer.getLong();
    }

    /**
     * Read all necessary information from the file, including message formats,
     * seeking positions and log file information.
     *
     * @throws IOException
     * @throws FormatErrorException
     */
    private void updateStatistics() throws IOException, FormatErrorException {
        position(0);
        readFileHeader();
        long packetsNum = 0;
        long timeStart = -1;
        long timeEnd = -1;
        long lastTime = -1;
        fieldsList = new HashMap<>();
        seekTimes = new ArrayList<>();
        while (true) {
            Object msg;
            long pos = position();
            try {
                msg = readMessage();
            } catch (EOFException e) {
                break;
            }
            packetsNum++;

            if (msg instanceof MessageFlagBits) {
                MessageFlagBits msgFlags = (MessageFlagBits) msg;
                // check flags
                if ((msgFlags.incompatibleFlags[0] & INCOMPAT_FLAG0_DATA_APPENDED_MASK) != 0) {
                    for (int i = 0; i < msgFlags.appendedOffsets.length; ++i) {
                        if (msgFlags.appendedOffsets[i] > 0) {
                            appendedOffsets.add(msgFlags.appendedOffsets[i]);
                        }
                    }
                    if (!appendedOffsets.isEmpty()) {
                        Logger.getGlobal().log(Level.WARNING, "ULogReader::updateStatistics:log contains appended data");
                    }
                }
                boolean containsUnknownIncompatBits = false;
                if ((msgFlags.incompatibleFlags[0] & ~0x1) != 0) {
                    containsUnknownIncompatBits = true;
                }
                for (int i = 1; i < msgFlags.incompatibleFlags.length; ++i) {
                    if (msgFlags.incompatibleFlags[i] != 0) {
                        containsUnknownIncompatBits = true;
                    }
                }
                if (containsUnknownIncompatBits) {
                    throw new FormatErrorException("Log contains unknown incompatible bits. Refusing to parse the log.");
                }

            } else if (msg instanceof MessageFormat) {
                MessageFormat msgFormat = (MessageFormat) msg;
                messageFormats.put(msgFormat.name, msgFormat);

            } else if (msg instanceof MessageAddLogged) {
                //from now on we cannot have any new MessageFormat's, so we
                //can parse the nested types
                if (!nestedParsingDone) {
                    for (MessageFormat m : messageFormats.values()) {
                        m.parseNestedTypes(messageFormats);
                    }
                    //now do a 2. pass to remove the last padding field
                    for (MessageFormat m : messageFormats.values()) {
                        m.removeLastPaddingField();
                    }
                    nestedParsingDone = true;
                }
                MessageAddLogged msgAddLogged = (MessageAddLogged) msg;
                MessageFormat msgFormat = messageFormats.get(msgAddLogged.name);
                if (msgFormat == null) {
                    throw new FormatErrorException("Format of subscribed message not found: " + msgAddLogged.name);
                }
                Subscription subscription = new Subscription(msgFormat, msgAddLogged.multiID);
                if (msgAddLogged.msgID < messageSubscriptions.size()) {
                    messageSubscriptions.set(msgAddLogged.msgID, subscription);
                } else {
                    while (msgAddLogged.msgID > messageSubscriptions.size()) {
                        messageSubscriptions.add(null);
                    }
                    messageSubscriptions.add(subscription);
                }
                if (msgAddLogged.multiID > msgFormat.maxMultiID) {
                    msgFormat.maxMultiID = msgAddLogged.multiID;
                }

            } else if (msg instanceof MessageParameter) {
                MessageParameter msgParam = (MessageParameter) msg;
                // a replayed log can contain many parameter updates, so we ignore them here
                if (parameters.containsKey(msgParam.getKey()) && !replayedLog) {
                    // maintain a record of parameters which change during flight
                    if (parameterUpdates.containsKey(msgParam.getKey())) {
                        parameterUpdates.get(msgParam.getKey()).add(new ParamUpdate(msgParam.getKey(), msgParam.value,
                                lastTime));
                    } else {
                        List<ParamUpdate> updateList = new ArrayList<ParamUpdate>();
                        updateList.add(new ParamUpdate(msgParam.getKey(), msgParam.value, lastTime));
                        parameterUpdates.put(msgParam.getKey(), updateList);
                    }
                } else {
                    // add parameter to the parameters Map
                    parameters.put(msgParam.getKey(), msgParam.value);
                }

            } else if (msg instanceof MessageInfo) {
                MessageInfo msgInfo = (MessageInfo) msg;
                if ("sys_name".equals(msgInfo.getKey())) {
                    systemName = (String) msgInfo.value;
                } else if ("ver_hw".equals(msgInfo.getKey())) {
                    version.put("HW", msgInfo.value);
                } else if ("ver_sw".equals(msgInfo.getKey())) {
                    version.put("FW", msgInfo.value);
                } else if ("time_ref_utc".equals(msgInfo.getKey())) {
                    utcTimeReference = ((long) ((Number) msgInfo.value).intValue()) * 1000 * 1000;
                } else if ("replay".equals(msgInfo.getKey())) {
                    replayedLog = true;
                }
            } else if (msg instanceof MessageInfoMultiple) {
                MessageInfoMultiple msgInfo = (MessageInfoMultiple) msg;
                if ("hardfault_plain".equals(msgInfo.getKey())) {
                    // append all hardfaults to one String (we should be looking at msgInfo.isContinued as well)
                    hardfaultPlainText += (String) msgInfo.value;
                }

            } else if (msg instanceof MessageData) {
                if (dataStart == 0) {
                    dataStart = pos;
                }
                MessageData msgData = (MessageData) msg;
                seekTimes.add(new SeekTime(msgData.timestamp, pos));

                if (timeStart < 0) {
                    timeStart = msgData.timestamp;
                }
                if (timeEnd < msgData.timestamp) {
                    timeEnd = msgData.timestamp;
                }
                lastTime = msgData.timestamp;
            } else if (msg instanceof MessageLog) {
                MessageLog msgLog = (MessageLog) msg;
                loggedMessages.add(msgLog);
            }
        }

        // fill the fieldsList now that we know how many multi-instances are in the log
        for (int k = 0; k < messageSubscriptions.size(); ++k) {
            Subscription s = messageSubscriptions.get(k);
            if (s != null) {
                MessageFormat msgFormat = s.format;
                if (msgFormat.name.charAt(0) != '_') {
                    int maxInstance = msgFormat.maxMultiID;
                    for (int i = 0; i < msgFormat.fields.size(); i++) {
                        FieldFormat fieldDescr = msgFormat.fields.get(i);
                        if (!fieldDescr.name.contains("_padding") && fieldDescr.name != "timestamp") {
                            for (int mid = 0; mid <= maxInstance; mid++) {
                                if (fieldDescr.isArray()) {
                                    for (int j = 0; j < fieldDescr.size; j++) {
                                        fieldsList.put(msgFormat.name + "_" + mid + "." + fieldDescr.name + "[" + j + "]", fieldDescr.type);
                                    }
                                } else {
                                    fieldsList.put(msgFormat.name + "_" + mid + "." + fieldDescr.name, fieldDescr.type);
                                }
                            }
                        }
                    }
                }
            }
        }
        startMicroseconds = timeStart;
        sizeUpdates = packetsNum;
        sizeMicroseconds = timeEnd - timeStart;
        seek(0);

        if (!errors.isEmpty()) {
            Logger.getGlobal().log(Level.WARNING, "ULogReader::updateStatistics:Errors while reading file:");
            for (final Exception e : errors) {
                Logger.getGlobal().log(Level.WARNING, e.getMessage());
            }
            errors.clear();
        }

        if (!hardfaultPlainText.isEmpty()) {
            Logger.getGlobal().log(Level.WARNING, "ULogReader::updateStatistics:Log contains hardfault data:" + hardfaultPlainText);
        }
    }

    @Override
    public boolean seek(long seekTime) throws IOException, FormatErrorException {
        position(dataStart);
        currentAppendingOffsetIndex = 0;

        if (seekTime == 0) {      // Seek to start of log
            return true;
        }

        //find the position in seekTime. We could speed this up further by
        //using a binary search
        for (SeekTime sk : seekTimes) {
            if (sk.timestamp >= seekTime) {
                position(sk.position);
                while (currentAppendingOffsetIndex < appendedOffsets.size() &&
                        appendedOffsets.get(currentAppendingOffsetIndex) < sk.position) {
                    ++currentAppendingOffsetIndex;
                }
                return true;
            }
        }
        return false;
    }

    private void applyMsg(Map<String, Object> update, MessageData msg) {
        applyMsgAsName(update, msg, msg.format.name + "_" + msg.multiID);
    }

    void applyMsgAsName(Map<String, Object> update, MessageData msg, String msg_name) {
        final ArrayList<FieldFormat> fields = msg.format.fields;
        for (int i = 0; i < fields.size(); i++) {
            FieldFormat field = fields.get(i);
            if (field.isArray()) {
                for (int j = 0; j < field.size; j++) {
                    update.put(msg_name + "." + field.name + "[" + j + "]", ((Object[]) msg.get(i))[j]);
                }
            } else {
                update.put(msg_name + "." + field.name, msg.get(i));
            }
        }
    }

    @Override
    public long readUpdate(Map<String, Object> update) throws IOException, FormatErrorException {
        while (true) {
            Object msg = readMessage();
            if (msg instanceof MessageData) {
                applyMsg(update, (MessageData) msg);
                return ((MessageData) msg).timestamp;
            }
        }
    }

    @Override
    public Map<String, String> getFields() {
        return fieldsList;
    }

    /**
     * Read next message from log
     *
     * @return log message
     * @throws IOException  on IO error
     * @throws EOFException on end of stream
     */
    public Object readMessage() throws IOException, FormatErrorException {
        while (true) {
            fillBuffer(HDRLEN);
            long pos = position();
            int s1 = buffer.get() & 0xFF;
            int s2 = buffer.get() & 0xFF;
            int msgSize = s1 + (256 * s2);
            int msgType = buffer.get() & 0xFF;

            // check if we cross an appending boundary: if so, we need to reset the position and skip this message
            if (currentAppendingOffsetIndex < appendedOffsets.size()) {
                if (pos + HDRLEN + msgSize > appendedOffsets.get(currentAppendingOffsetIndex)) {
                    position(appendedOffsets.get(currentAppendingOffsetIndex));
                    ++currentAppendingOffsetIndex;
                    continue;
                }
            }

            try {
                fillBuffer(msgSize);
            } catch (EOFException e) {
                errors.add(new FormatErrorException(pos, "Unexpected end of file"));
                throw e;
            }
            Object msg;
            switch (msgType) {
                case MESSAGE_TYPE_DATA:
                    s1 = buffer.get() & 0xFF;
                    s2 = buffer.get() & 0xFF;
                    int msgID = s1 + (256 * s2);
                    Subscription subscription = null;
                    if (msgID < messageSubscriptions.size()) {
                        subscription = messageSubscriptions.get(msgID);
                    }
                    if (subscription == null) {
                        position(pos);
                        errors.add(new FormatErrorException(pos, "Unknown DATA subscription ID: " + msgID));
                        buffer.position(buffer.position() + msgSize - 1);
                        continue;
                    }
                    msg = new MessageData(subscription.format, buffer, subscription.multiID);
                    break;
                case MESSAGE_TYPE_FLAG_BITS:
                    msg = new MessageFlagBits(buffer, msgSize);
                    break;
                case MESSAGE_TYPE_INFO:
                    msg = new MessageInfo(buffer);
                    break;
                case MESSAGE_TYPE_INFO_MULTIPLE:
                    msg = new MessageInfoMultiple(buffer);
                    break;
                case MESSAGE_TYPE_PARAMETER:
                    msg = new MessageParameter(buffer);
                    break;
                case MESSAGE_TYPE_FORMAT:
                    msg = new MessageFormat(buffer, msgSize);
                    break;
                case MESSAGE_TYPE_ADD_LOGGED_MSG:
                    msg = new MessageAddLogged(buffer, msgSize);
                    break;
                case MESSAGE_TYPE_DROPOUT:
                    msg = new MessageDropout(buffer);
                    break;
                case MESSAGE_TYPE_LOG:
                    msg = new MessageLog(buffer, msgSize);
                    break;
                case MESSAGE_TYPE_REMOVE_LOGGED_MSG:
                case MESSAGE_TYPE_SYNC:
                case MESSAGE_TYPE_PARAMETER_DEFAULT:
                    buffer.position(buffer.position() + msgSize); //skip this message
                    continue;
                default:
                    if (msgSize == 0 && msgType == 0) {
                        // This is an error (corrupt file): likely the file is filled with zeros from this point on.
                        // Not much we can do except to ensure that we make progress and don't spam the error console.
                    } else {
                        buffer.position(buffer.position() + msgSize);
                        errors.add(new FormatErrorException(pos, "Unknown message type: " + msgType));
                    }
                    continue;
            }
            int sizeParsed = (int) (position() - pos - HDRLEN);
            if (sizeParsed != msgSize) {
                errors.add(new FormatErrorException(pos,
                        "Message size mismatch, parsed: " + sizeParsed + ", msg size: " + msgSize));
                buffer.position(buffer.position() + msgSize - sizeParsed);
            }
            return msg;
        }
    }

    public void getData(List<TspiData> tspiData, List<ValuePairTimedData> otherGpsData) {
        List<Map<String, Object>> gpsUpdates = new ArrayList<>();
//        List<Map<String, Object>> batteryUpdates = new ArrayList<>();
//        List<String> messageNames = new ArrayList<>();
//        List<String> gpsNames = new ArrayList<>();
        while (true) {
            Object message = null;
            try {
                message = readMessage();
            } catch (EOFException e) {
                break;
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ULogReader::getGpsData", e);
            }
//            if (message instanceof MessageData messageData && !messageNames.contains(messageData.format.name)) {
//                messageNames.add(messageData.format.name);
//                System.out.println("---------------------");
//                System.out.println(messageData.format.name);
//                System.out.println("");
//                Map<String, Object> update = new HashMap<>();
//                applyMsg(update, (MessageData) message);
//                List<String> keys = new ArrayList<>();
//                for (String key : update.keySet()) {
//                    if (!keys.contains(key)) {
//                        keys.add(key);
//                        System.out.println(key);
//                    }
//                }
//                System.out.println("---------------------");
//            }
            if (message instanceof MessageData messageData) {
                if (messageData.format.name.toLowerCase().contains("vehicle_gps_position")) {
                    Map<String, Object> update = new HashMap<>();
                    applyMsg(update, (MessageData) message);
                    gpsUpdates.add(update);
//                } else if (messageData.format.name.toLowerCase().contains("battery_status")) {
//                    Map<String, Object> update = new HashMap<>();
//                    applyMsg(update, (MessageData) message);
//                    batteryUpdates.add(update);
                }
            }
        }
        for (Map<String, Object> update : gpsUpdates) {
            TspiData tspi = processVehicleGpsPosition(update);
            if (tspi != null) {
                tspiData.add(tspi);
            }
            ValuePairTimedData valuePairTimedData = processOtherGps(update);
            if (valuePairTimedData != null) {
                otherGpsData.add(valuePairTimedData);
            }
        }
//        for (Map<String, Object> update : batteryUpdates) {
//            ValuePairTimedData valuePairTimedData = processBattery(update);
//            if (valuePairTimedData != null) {
//                batteryData.add(valuePairTimedData);
//            }
//        }
    }

//    public List<TspiData> getTspiData() {
//
//        List<Map<String, Object>> updates = new ArrayList<>();
//        long time = System.nanoTime();
//        while (true) {
//            try {
//                Map<String, Object> update = new HashMap<>();
//                readUpdate(update);
//                updates.add(update);
//            } catch (EOFException eof) {
//                break;
//            } catch (Exception e) {
//                Logger.getGlobal().log(Level.WARNING, "ULogReader::getGpsData", e);
//            }
//        }
//        System.out.println(System.nanoTime() - time);
//        time = System.nanoTime();
//        for (Map<String, Object> update : updates) {
//            TspiData tspi = processVehicleGpsPosition(update);
//            if (tspi != null) {
//                tspiData.add(tspi);
//            }
//        }
//        System.out.println(System.nanoTime() - time);
//        return tspiData;
//    }

    private TspiData processVehicleGpsPosition(Map<String, Object> update) {
        Long timestamp = null;
        Double latitude = null;
        Double longitude = null;
        Double altitude = null;

        Object object = update.get("vehicle_gps_position_0.time_utc_usec");
        if (object instanceof Long) {
            timestamp = ((Long) object) / 1000;
        }

        object = update.get("vehicle_gps_position_0.lat");
        if (object instanceof Integer) {
            latitude = ((Integer) object) / 10000000.0;
        }

        object = update.get("vehicle_gps_position_0.lon");
        if (object instanceof Integer) {
            longitude = ((Integer) object) / 10000000.0;
        }

        object = update.get("vehicle_gps_position_0.alt");
        if (object instanceof Integer) {
            altitude = ((Integer) object) / 1000.0;
        }

        if (timestamp != null && latitude != null && longitude != null && altitude != null) {
            return new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude));
        }

        return null;
    }

    private ValuePairTimedData processOtherGps(Map<String, Object> update) {
        Map<String, String> otherFields = new LinkedHashMap<>();

        Long timestamp = null;
        Object object = update.get("vehicle_gps_position_0.time_utc_usec");
        if (object instanceof Long) {
            timestamp = ((Long) object) / 1000;
        }
        object = update.get("vehicle_gps_position_0.vel_m_s");
        if (object != null) {
            otherFields.put("vel_m_s", object.toString());
        }
        object = update.get("vehicle_gps_position_0.vel_n_m_s");
        if (object != null) {
            otherFields.put("vel_n_m_s", object.toString());
        }
        object = update.get("vehicle_gps_position_0.vel_e_m_s");
        if (object != null) {
            otherFields.put("vel_e_m_s", object.toString());
        }
        object = update.get("vehicle_gps_position_0.vel_d_m_s");
        if (object != null) {
            otherFields.put("vel_d_m_s", object.toString());
        }

        if (timestamp != null) {
            return new ValuePairTimedData(timestamp, otherFields);
        } else {
            return null;
        }
    }

//    private ValuePairTimedData processBattery(Map<String, Object> update) {
//        Map<String, String> otherFields = new LinkedHashMap<>();
//
//        Long timestamp = null;
//        Object object = update.get("battery_status_0.timestamp");
//        if (object instanceof Long) {
//            timestamp = ((Long) object) / 1000;
//        }
//        object = update.get("battery_status_0.time_remaining_s");
//        if (object != null) {
//            otherFields.put("time_remaining_s", object.toString());
//        }
//        object = update.get("vehicle_gps_position_0.remaining_capacity_wh");
//        if (object != null) {
//            otherFields.put("remaining_capacity_wh", object.toString());
//        }
//        object = update.get("vehicle_gps_position_0.warning");
//        if (object != null) {
//            otherFields.put("warning", object.toString());
//        }
//
//        if (timestamp != null) {
//            return new ValuePairTimedData(timestamp, otherFields);
//        } else {
//            return null;
//        }
//    }

    @Override
    public List<Exception> getErrors() {
        return errors;
    }

    @Override
    public void clearErrors() {
        errors.clear();
    }
}
