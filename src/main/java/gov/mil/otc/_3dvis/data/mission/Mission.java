package gov.mil.otc._3dvis.data.mission;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

import java.util.Objects;

public class Mission extends TimedData {

    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm";

    private final String name;
    private final long stopTime;

    public Mission(String name, long startTime, long stopTime) {
        super(startTime);
        this.name = name;
        this.stopTime = stopTime;
    }

    public String getName() {
        return name;
    }

    public long getStopTime() {
        return stopTime;
    }

    public boolean inMission(long timestamp) {
        return timestamp >= getTimestamp() && timestamp < stopTime;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mission mission = (Mission) o;
        return name.equalsIgnoreCase(mission.name) &&
                getTimestamp() == mission.getTimestamp() &&
                stopTime == mission.getStopTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, getTimestamp(), stopTime);
    }
}
