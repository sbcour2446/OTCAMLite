package gov.mil.otc._3dvis.data;

public class DataSource {

    private final int id;
    private final String name;
    private final long startTime;
    private final long stopTime;
    private boolean use;

    public DataSource(int id, String name, long startTime, long stopTime, boolean use) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.use = use;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public boolean isUse() {
        return use;
    }

    public void setUse(boolean use) {
        this.use = use;
    }
}
