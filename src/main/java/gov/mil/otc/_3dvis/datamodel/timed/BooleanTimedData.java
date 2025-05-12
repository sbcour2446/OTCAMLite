package gov.mil.otc._3dvis.datamodel.timed;

public class BooleanTimedData extends TimedData {

    private final boolean value;

    public BooleanTimedData(long timestamp, boolean value) {
        super(timestamp);
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }
}
