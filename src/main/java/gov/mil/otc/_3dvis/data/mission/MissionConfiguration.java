package gov.mil.otc._3dvis.data.mission;

import com.google.gson.annotations.SerializedName;
import gov.mil.otc._3dvis.utility.Utility;

import java.util.Objects;

public class MissionConfiguration {

    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm";

    @SerializedName("mission name")
    private final String missionName;

    @SerializedName("start time")
    private final String startTimeString;

    private transient long startTime = -1;

    @SerializedName("stop time")
    private final String stopTimeString;

    private transient long stopTime = -1;

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
        if (startTime == -1 && startTimeString != null) {
            startTime = Utility.parseTime(startTimeString, TIME_FORMAT);
        }
        return startTime;
    }

    public long getStopTime() {
        if (stopTime == -1 && stopTimeString != null) {
            stopTime = Utility.parseTime(stopTimeString, TIME_FORMAT);
        }
        return stopTime;
    }

    public Mission toMission() {
        return new Mission(missionName, startTime, stopTime);
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
                stopTimeString.equals(missionConfiguration.stopTimeString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(missionName, startTimeString, stopTimeString);
    }
}
