package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.utility.Utility;

public class SensorStatus extends TimedData {

    private final long timestamp;
    private final String scanType;
    private final String result;
    private final String conditionCleared;

    public SensorStatus(long timestamp, String scanType, String result, String conditionCleared) {
        super(timestamp);
        this.timestamp = timestamp;
        this.scanType = scanType;
        this.result = result.replace("\4", System.lineSeparator());
        this.conditionCleared = conditionCleared;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getScanType() {
        return scanType;
    }

    public String getResult() {
        return result;
    }

    public String getConditionCleared() {
        return conditionCleared;
    }
}
