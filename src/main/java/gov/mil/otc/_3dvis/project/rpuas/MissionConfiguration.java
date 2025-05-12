package gov.mil.otc._3dvis.project.rpuas;

import com.google.gson.annotations.SerializedName;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.utility.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MissionConfiguration {

    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm";

    @SerializedName("mission name")
    private final String missionName;

    @SerializedName("start time")
    private final String startTimeString;

    private transient long startTime;

    @SerializedName("stop time")
    private final String stopTimeString;

    private transient long stopTime;

    @SerializedName("device configurations")
    private final List<DeviceConfiguration> deviceConfigurationList = new ArrayList<>();

    public MissionConfiguration(String missionName, long startTime, long stopTime) {
        this.missionName = missionName;
        this.startTime = startTime;
        this.startTimeString = Utility.formatTime(startTime, TIME_FORMAT);
        this.stopTime = stopTime;
        this.stopTimeString = Utility.formatTime(stopTime, TIME_FORMAT);
    }

    protected void initialize() {
        startTime = Utility.parseTime(startTimeString, TIME_FORMAT);
        stopTime = Utility.parseTime(stopTimeString, TIME_FORMAT);
    }

    public String getMissionName() {
        return missionName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void addDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        deviceConfigurationList.add(deviceConfiguration);
    }

    public void addDeviceConfiguration(String deviceId, Affiliation affiliation, int operatorId) {
        deviceConfigurationList.add(new DeviceConfiguration(missionName, deviceId, affiliation, operatorId));
    }

    public DeviceConfiguration getDeviceConfiguration(String deviceId) {
        for (DeviceConfiguration deviceConfiguration : deviceConfigurationList) {
            if (deviceConfiguration.getDeviceId().equalsIgnoreCase(deviceId)) {
                return deviceConfiguration;
            }
        }
        return null;
    }

    public List<DeviceConfiguration> getDeviceConfigurationList() {
        return deviceConfigurationList;
    }

    public boolean inMission(long timestamp) {
        return timestamp >= startTime && timestamp < stopTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MissionConfiguration missionConfiguration = (MissionConfiguration) o;
        return missionName.equals(missionConfiguration.missionName) &&
                startTimeString.equals(missionConfiguration.startTimeString) &&
                stopTimeString.equals(missionConfiguration.stopTimeString) &&
                deviceConfigurationList.equals(missionConfiguration.deviceConfigurationList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(missionName, startTimeString, stopTimeString, deviceConfigurationList);
    }
}
