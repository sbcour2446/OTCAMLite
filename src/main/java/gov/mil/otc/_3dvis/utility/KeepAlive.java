package gov.mil.otc._3dvis.utility;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeepAlive {

    private static final KeepAlive SINGLETON = new KeepAlive();
    private Timer keepAliveTimer;
    private Point lastMousePoint = new Point(0, 0);
    private final AtomicInteger counter = new AtomicInteger(1);

    public static void start() {
        SINGLETON.doStart();
    }

    public static void shutdown() {
        SINGLETON.doShutdown();
    }

    private KeepAlive() {
    }

    private void doStart() {
        keepAliveTimer = new Timer("keepAliveTimer");
        keepAliveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkMovement();
            }
        }, 30000, 30000);
    }

    private void doShutdown() {
        if (keepAliveTimer != null) {
            keepAliveTimer.cancel();
        }
    }

    private void checkMovement() {
        Point currentMousePoint = MouseInfo.getPointerInfo().getLocation();
        if (lastMousePoint.x == currentMousePoint.x
                && lastMousePoint.y == currentMousePoint.y) {
            try {
                Robot robot = new Robot();
                robot.mouseMove(lastMousePoint.x, lastMousePoint.y);
            } catch (AWTException e) {
                Logger.getGlobal().log(Level.WARNING, "KeepAlive:checkMovement", e);
            }
        } else {
            lastMousePoint = currentMousePoint;
        }
    }
}
