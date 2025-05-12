package gov.mil.otc._3dvis.time;

import java.util.Timer;
import java.util.TimerTask;

public class TimeManager {

    private static final TimeManager SINGLETON = new TimeManager();
    private static final long MILLI_NANO = 1000000;
    private static final int TOLERANCE = 1000; // 1 second
    private static final int UPDATE_RATE = 10; // 100 Hz
    private final Timer timer = new Timer("TimeManager");
    private final Time currentTime = new Time();
    private TimeRate timeRate = TimeRate.SPEED_NORMAL;
    private boolean isLive = true;
    private boolean isPaused = false;
    private long lastNanoSecond = 0;

    private TimeManager() {
    }

    public static void initialize() {
        SINGLETON.doInitialize();
    }

    private void doInitialize() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long currentNanoSecond = System.nanoTime();
                if (isLive) {
                    currentTime.setTime(System.currentTimeMillis());
                } else if (!isPaused) {
                    long newTime = currentTime.getTime() +
                            (long) ((currentNanoSecond - lastNanoSecond) * timeRate.getValue() / MILLI_NANO);
                    doSetTime(newTime);
                }
                lastNanoSecond = currentNanoSecond;
            }
        }, 0, UPDATE_RATE);
    }

    public static void shutdown() {
        SINGLETON.doShutdown();
    }

    public void doShutdown() {
        timer.cancel();
    }

    public static long getTime() {
        return SINGLETON.doGetTime();
    }

    private long doGetTime() {
        if (isPaused) {
            return currentTime.getTime();
        }
        if (isLive) {
            return System.currentTimeMillis();
        }
        return currentTime.getTime() + (long) ((System.nanoTime() - lastNanoSecond) * timeRate.getValue() / MILLI_NANO);
    }

    public static void setTime(long time) {
        SINGLETON.doSetTime(time);
    }

    private synchronized void doSetTime(long time) {
        long systemTime = System.currentTimeMillis();
        if (!isPaused && (time + TOLERANCE) > systemTime) {
            currentTime.setTime(systemTime);
            doSetToLive();
        } else {
            currentTime.setTime(time);
            isLive = false;
        }
    }

    private void doSetToLive() {
        isLive = true;
        isPaused = false;
        timeRate = TimeRate.SPEED_NORMAL;
    }

    public static boolean isLive() {
        return SINGLETON.isLive;
    }

    public static void setToLive() {
        SINGLETON.doSetToLive();

    }

    public static boolean isPaused() {
        return SINGLETON.isPaused;
    }

    public static void setPause(boolean isPaused) {
        if (isPaused) {
            SINGLETON.isLive = false;
            SINGLETON.isPaused = true;
        } else {
            SINGLETON.isPaused = false;
        }
    }

    public static void increaseRate() {
        SINGLETON.timeRate = SINGLETON.timeRate.increase();
    }

    public static void decreaseRate() {
        SINGLETON.timeRate = SINGLETON.timeRate.decrease();
    }

    public static TimeRate getRate() {
        return SINGLETON.timeRate;
    }
}

