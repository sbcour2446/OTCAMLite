package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.data.gps.EntityConfiguration;
import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import javafx.scene.control.Alert;

import java.util.Objects;

public class AlarmAlert extends TimedData {

    private final boolean isAlert;
    private long clearTime;
    private final String description;

    public AlarmAlert(long timestamp, boolean isAlert, long clearTime, String description) {
        super(timestamp);
        this.isAlert = isAlert;
        this.clearTime = clearTime;
        this.description = description;
    }

    public long getClearTime() {
        return clearTime;
    }

    public void setClearTime(long clearTime) {
        this.clearTime = clearTime;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public boolean isCleared(long time) {
        return time < getTimestamp() || time >= clearTime;
    }

    @Override
    public String toString() {
        return description;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AlarmAlert alarmAlert = (AlarmAlert) o;
        return getTimestamp() == alarmAlert.getTimestamp() &&
                clearTime == alarmAlert.clearTime &&
                description.equals(alarmAlert.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTimestamp(), clearTime, description);
    }
}
