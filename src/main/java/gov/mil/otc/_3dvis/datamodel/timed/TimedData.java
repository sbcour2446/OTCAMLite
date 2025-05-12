package gov.mil.otc._3dvis.datamodel.timed;

public abstract class TimedData {

    private final long timestamp;

    protected TimedData(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
