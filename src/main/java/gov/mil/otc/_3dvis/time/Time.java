package gov.mil.otc._3dvis.time;

public class Time {

    private long theTime = System.currentTimeMillis();

    public synchronized void setTime(long time) {
        theTime = time;
    }

    public synchronized long getTime() {
        return theTime;
    }
}
