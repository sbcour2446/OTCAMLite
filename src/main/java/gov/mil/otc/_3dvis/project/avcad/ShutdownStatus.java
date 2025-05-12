package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class ShutdownStatus extends TimedData {

    private final long endTime;

    public ShutdownStatus(long timestamp, long endTime) {
        super(timestamp);
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean isShutdown(long time) {
        return time >= getTimestamp() && time < endTime;
    }
}
