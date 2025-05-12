package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SensorLogFile extends CsvFile {

    //TimeStamp,Boot Cycle,Event Number,Scan Type,Software Version,System Runtime (Hrs),PC Serial Number,PC Cycles,PC Runtime (Hrs),Location,Coordinates (LatLon),Result,Condition Cleared
    private static final String TIMESTAMP_COLUMN = "TimeStamp";
    private static final String SCAN_TYPE_COLUMN = "Scan Type";
    private static final String RESULT_COLUMN = "Result";
    private static final String CONDITION_CLEARED_COLUMN = "Condition Cleared";

    private static final List<String> alarmList = new ArrayList<>();
    private static final List<String> alertList = new ArrayList<>();

    private final List<ConnectionStatus> connectionStatusList = new ArrayList<>();
    private final List<SensorStatus> sensorStatusList = new ArrayList<>();
    private final List<AlarmAlert> alarmAlertList = new ArrayList<>();
    private final List<ShutdownStatus> shutdownStatusList = new ArrayList<>();
    private long startTime = Long.MAX_VALUE;
    private long stopTime = 0;
    private long lastMessageTime = 0;
    private long lastShutdownTime = 0;
    private long timeZoneOffset = 0;

    public SensorLogFile(File file, AvcadConfiguration avcadConfiguration) {
        super(file, 9);

        timeZoneOffset = avcadConfiguration.getTimeZonOffset() * 3600000L; //convert to milliseconds
        alarmList.addAll(avcadConfiguration.getAlarmList());
        alertList.addAll(avcadConfiguration.getAlertList());

        addColumn(TIMESTAMP_COLUMN);
        addColumn(SCAN_TYPE_COLUMN);
        addColumn(RESULT_COLUMN);
        addColumn(CONDITION_CLEARED_COLUMN, true);
    }

    public List<ConnectionStatus> getConnectionStatusList() {
        return connectionStatusList;
    }

    public List<SensorStatus> getSensorStatusList() {
        return sensorStatusList;
    }

    public List<AlarmAlert> getAlarmAlertList() {
        return alarmAlertList;
    }

    public List<ShutdownStatus> getShutdownStatusList() {
        return shutdownStatusList;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void validateAlarmAlerts() {
        for (AlarmAlert alarmAlert : alarmAlertList) {
            if (alarmAlert.getClearTime() <= 0) {
                alarmAlert.setClearTime(stopTime);
            }
        }
    }

    @Override
    protected void processLine(String[] fields) {
        //timestamp ddHHmmssZMMMyyyy  28070359EFeb2024
        String timestampString = fields[getColumnIndex(TIMESTAMP_COLUMN)];
        long timestamp = convertTimeString(timestampString);
        if (timestamp == 0) {
            return;
        }
        if (startTime > timestamp) {
            startTime = timestamp;
        }
        if (stopTime < timestamp) {
            stopTime = timestamp;
        }

        String scanType = fields[getColumnIndex(SCAN_TYPE_COLUMN)];
        String result = fields[getColumnIndex(RESULT_COLUMN)];
        int conditionClearedIndex = getColumnIndex(CONDITION_CLEARED_COLUMN);
        String conditionCleared = "";
        long conditionClearedTime = 0;
        if (conditionClearedIndex > 0 && fields.length > conditionClearedIndex) {
            conditionCleared = fields[conditionClearedIndex];
            conditionClearedTime = convertTimeString(conditionCleared);
            if (conditionClearedTime > 0) {
                conditionCleared = Utility.formatTime(conditionClearedTime);
            }
        }

        sensorStatusList.add(new SensorStatus(timestamp, scanType, result, conditionCleared));

        ConnectionStatus connectionStatus = determineConnection(timestamp, result);
        int defaultDelay = 1000;
        if (connectionStatus != null) {
            connectionStatusList.add(connectionStatus);
        } else if (determineAlarm(result)) {
            if (conditionClearedTime == 0) {
                if (lastShutdownTime > 0) {
                    conditionClearedTime = lastShutdownTime;
                } else {
                    conditionClearedTime = stopTime + defaultDelay;
                }
            }
            alarmAlertList.add(new AlarmAlert(timestamp, false, conditionClearedTime, result));
        } else if (determineAlert(result)) {
            if (conditionClearedTime == 0) {
                if (lastShutdownTime > 0) {
                    conditionClearedTime = lastShutdownTime;
                } else {
                    conditionClearedTime = stopTime + defaultDelay;
                }
            }
            alarmAlertList.add(new AlarmAlert(timestamp, true, conditionClearedTime, result));
        } else if (result.equalsIgnoreCase("shutdown")) {
            long endTime = lastMessageTime;
            if (endTime <= timestamp) {
                endTime = timestamp + defaultDelay;
            }
            shutdownStatusList.add(new ShutdownStatus(timestamp, endTime));
            lastShutdownTime = timestamp;
        }

        lastMessageTime = timestamp;
    }

    private long convertTimeString(String value) {
        if (value.length() != 16) {
            return 0;
        }
        value = value.substring(0, 8) + value.substring(9, 16);
        long timestamp = Utility.parseTime(value, "ddHHmmssMMMyyyy");
        if (timestamp > 0) {
            timestamp += timeZoneOffset;
        }
        return timestamp;
    }

    private ConnectionStatus determineConnection(long timestamp, String value) {
        if (value.toLowerCase().startsWith("connection success")) {
            return new ConnectionStatus(timestamp, true);
        } else if (value.toLowerCase().startsWith("connection failure")) {
            return new ConnectionStatus(timestamp, false);
        }
        return null;
    }

    private boolean determineAlarm(String value) {
        return alarmList.contains(value.toLowerCase());
    }

    private boolean determineAlert(String value) {
        return alertList.contains(value.toLowerCase());
    }
}
