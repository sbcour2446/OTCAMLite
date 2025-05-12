package gov.mil.otc._3dvis.event;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.nasa.worldwind.layers.RenderableLayer;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private static final EventManager SINGLETON = new EventManager();
    private final RenderableLayer layer = new RenderableLayer();
    private final List<Event> eventList = new ArrayList<>();
    private Timer timer;
    private boolean isRunning = false;

    private EventManager() {
    }

    public static void start() {
        SINGLETON.doStart();
    }

    public static void shutdown() {
        SINGLETON.doShutdown();
    }

    public static void addEvent(Event event) {
        SINGLETON.doAddEvent(event);
    }

    public static List<Event> getEvents() {
        return SINGLETON.doGetEvents();
    }

    public static List<RtcaEvent> getRtcaEvents() {
        return SINGLETON.doGetRtcaEvents();
    }

    public static void removeAllEvents() {
        SINGLETON.doRemoveAllEvents();
    }

    private void doStart() {
        if (!isRunning) {
            layer.setName("EventManager");
            layer.setPickEnabled(true);
            WWController.addLayer(layer);
            timer = new Timer(50, e -> update());
            timer.start();
            isRunning = true;
        }
    }

    private void doShutdown() {
        if (isRunning) {
            timer.stop();
            isRunning = false;
        }
    }

    private void doAddEvent(Event event) {
        synchronized (eventList) {
            for (int i = 0; i < eventList.size(); i++) {
                if (eventList.get(i).getTimestamp() > event.getTimestamp()) {
                    eventList.add(i, event);
                    return;
                }
            }
            eventList.add(event);
        }
    }

    private List<Event> doGetEvents() {
        synchronized (eventList) {
            return new ArrayList<>(eventList);
        }
    }

    private List<RtcaEvent> doGetRtcaEvents() {
        List<RtcaEvent> rtcaEvents = new ArrayList<>();
        synchronized (eventList) {
            for (Event event : eventList) {
                if (event instanceof RtcaEvent) {
                    rtcaEvents.add((RtcaEvent) event);
                }
            }
        }
        return rtcaEvents;
    }

    private void doRemoveAllEvents() {
        synchronized (eventList) {
            for (Event event : eventList) {
                event.dispose(layer);
            }
            eventList.clear();
        }
    }

    private void update() {
        long time = TimeManager.getTime();

        synchronized (eventList) {
            for (Event event : eventList) {
                event.update(time, layer);
            }
        }
    }
}