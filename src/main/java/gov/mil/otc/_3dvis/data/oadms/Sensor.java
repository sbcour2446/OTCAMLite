package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;

public class Sensor {

    public static final String ROOT = "DataLog";
    public static final String TYPE = "Type";
    public static final String TRIAL_START_TIME = "TrialStartTime";
    public static final String SID = "Sid";
    public static final String SENSOR_NAME = "SensorName";
    public static final String SERIAL_NUMBER = "SerialNumber";
    public static final String BLADE_SERIAL_NUMBER = "BladeSerialNumber";

    private final String type;
    private final String trialStartTime;
    private final String sid;
    private final String sensorName;
    private final String serialNumber;
    private final String bladeSerialNumber;
    private final TimedDataSet<Reading> readings = new TimedDataSet<>(true);

    public Sensor(String type, String trialStartTime, String sid, String sensorName, String serialNumber, String bladeSerialNumber) {
        this.type = type;
        this.trialStartTime = trialStartTime;
        this.sid = sid;
        this.sensorName = sensorName;
        this.serialNumber = serialNumber;
        this.bladeSerialNumber = bladeSerialNumber;
    }

    public String getType() {
        return type;
    }

    public String getTrialStartTime() {
        return trialStartTime;
    }

    public String getSid() {
        return sid;
    }

    public String getSensorName() {
        return sensorName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getBladeSerialNumber() {
        return bladeSerialNumber;
    }

    public void addReading(Reading reading) {
        readings.add(reading);
    }

    public TimedDataSet<Reading> getReadings() {
        return readings;
    }
}
