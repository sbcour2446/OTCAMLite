package gov.mil.otc._3dvis.layer.timeline;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.layer.control.ControlButton;
import gov.mil.otc._3dvis.layer.control.ImageButton;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenAnnotation;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.TimeZone;

import static gov.mil.otc._3dvis.layer.Constant.*;

public class TimelineLayer extends AnnotationLayer implements SelectListener, MouseWheelListener {

    private static final TimelineLayer SINGLETON = new TimelineLayer();

    private final ImageButton timelineControlZoomIn = new ImageButton(
            "/images/plus_small.png", new Dimension(ZOOM_BUTTON_SIZE, ZOOM_BUTTON_SIZE), true);
    private final ImageButton timelineControlZoomOut = new ImageButton(
            "/images/minus_small.png", new Dimension(ZOOM_BUTTON_SIZE, ZOOM_BUTTON_SIZE), true);
    private final ImageButton timelineControlSkipBack = new ImageButton(
            "/images/skip_back.png", new Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT), true);
    private final ImageButton timelineControlSlower = new ImageButton(
            "/images/slower.png", new Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT), true);
    private final ImageButton timelineControlPlay = new ImageButton(
            "/images/play.png", new Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT), true);
    private final ImageButton timelineControlPause = new ImageButton(
            "/images/pause.png", new Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT), true);
    private final ImageButton timelineControlFaster = new ImageButton(
            "/images/faster.png", new Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT), true);
    private final ImageButton timelineControlSkipForward = new ImageButton(
            "/images/skip_forward.png", new Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT), true);
    private final ImageButton timelineControlLive = new ImageButton(
            "/images/live.png", new Dimension(CONTROL_BUTTON_WIDTH, CONTROL_BUTTON_HEIGHT), true);
    private final Background background = new Background();
    private final StatusAnnotation statusAnnotation = new StatusAnnotation();
    private final TimelineAnnotation timelineAnnotation = new TimelineAnnotation("", new Point());
    private boolean initialized = false;
    private ScreenAnnotation currentAnnotation = null;

    private TimelineLayer() {
    }

    public static void initialize() {
        SINGLETON.doInitialize();
    }

    public static void mark(long startTime, long stopTime) {
        SINGLETON.timelineAnnotation.setMark(startTime, stopTime);
    }

    public static void clearMark() {
        SINGLETON.timelineAnnotation.clearMark();
    }

    public static void setShowLocalTimeZone(boolean value) {
        SINGLETON.statusAnnotation.setShowLocalTimeZone(value);
    }

    public static void setLocalTimeZone(TimeZone value) {
        SINGLETON.statusAnnotation.setLocalTimeZone(value);
    }

    private void doInitialize() {
        if (!initialized) {
            initialized = true;

            timelineControlZoomIn.setValue(AVKey.HOVER_TEXT, "Zoom in to timeline");
            timelineControlZoomOut.setValue(AVKey.HOVER_TEXT, "Zoom out of timeline");
            timelineControlSkipBack.setValue(AVKey.HOVER_TEXT, "Skip back 30 seconds");
            timelineControlSlower.setValue(AVKey.HOVER_TEXT, "Reduce play speed");
            timelineControlPlay.setValue(AVKey.HOVER_TEXT, "Resume");
            timelineControlPause.setValue(AVKey.HOVER_TEXT, "Pause");
            timelineControlFaster.setValue(AVKey.HOVER_TEXT, "Increase play speed");
            timelineControlSkipForward.setValue(AVKey.HOVER_TEXT, "Skip forward 30 seconds");
            timelineControlLive.setValue(AVKey.HOVER_TEXT, "Set to live mode");

            addAnnotation(background);
            addAnnotation(timelineControlZoomIn);
            addAnnotation(timelineControlZoomOut);
            addAnnotation(timelineControlSkipBack);
            addAnnotation(timelineControlSlower);
            addAnnotation(timelineControlPlay);
            addAnnotation(timelineControlPause);
            addAnnotation(timelineControlFaster);
            addAnnotation(timelineControlSkipForward);
            addAnnotation(timelineControlLive);
            addAnnotation(statusAnnotation);
            addAnnotation(timelineAnnotation);

            setPickEnabled(true);

            setName("TimelineLayer");

            WWController.getWorldWindowPanel().getInputHandler().addMouseWheelListener(this);
            WWController.getWorldWindowPanel().addSelectListener(this);
            WWController.addLayer(this);
        }
    }

    private void handleMouseOverEvent(SelectEvent event) {
        if (currentAnnotation == event.getTopObject()) {
            return;
        }

        if (currentAnnotation instanceof ControlButton) {
            ((ControlButton) currentAnnotation).highlight(false);
        }

        if (event.getTopObject() instanceof ScreenAnnotation) {
            currentAnnotation = (ScreenAnnotation) event.getTopObject();
        }

        if (currentAnnotation instanceof ControlButton) {
            ((ControlButton) currentAnnotation).highlight(true);
        }
    }

    private void handleLeftPressEvent(SelectEvent event) {
        if (event.getTopObject() == timelineControlZoomIn) {
            timelineAnnotation.zoomIn();
        } else if (event.getTopObject() == timelineControlZoomOut) {
            timelineAnnotation.zoomOut();
        } else if (event.getTopObject() == timelineControlSkipBack) {
            TimeManager.setTime(TimeManager.getTime() - SKIP_TIME);
        } else if (event.getTopObject() == timelineControlSlower) {
            TimeManager.decreaseRate();
        } else if (event.getTopObject() == timelineControlPlay) {
            TimeManager.setPause(false);
        } else if (event.getTopObject() == timelineControlPause) {
            TimeManager.setPause(true);
        } else if (event.getTopObject() == timelineControlFaster) {
            TimeManager.increaseRate();
        } else if (event.getTopObject() == timelineControlLive) {
            TimeManager.setToLive();
        } else if (event.getTopObject() == timelineControlSkipForward) {
            TimeManager.setTime(TimeManager.getTime() + SKIP_TIME);
        }
    }

    @Override
    public void doRender(DrawContext dc) {
        int width = dc.getView().getViewport().width;
        int centerX = width / 2;

        background.getAttributes().setSize(new Dimension(width, TIMELINE_TOP));
        background.setScreenPoint(new Point(centerX, 0));

        statusAnnotation.setScreenPoint(new Point(centerX, PADDING_BOTTOM));
        statusAnnotation.setTime(timelineAnnotation.isDragging() ?
                timelineAnnotation.getTimeInMillis() : TimeManager.getTime());
        statusAnnotation.initialize();

        timelineControlZoomIn.setScreenPoint(new Point(width - TIMELINE_INSET - ZOOM_BAR_WIDTH / 2 - 2,
                TIMELINE_TOP - ZOOM_BUTTON_SIZE));

        timelineControlZoomOut.setScreenPoint(new Point(width - TIMELINE_INSET - ZOOM_BAR_WIDTH / 2 - 2,
                TIMELINE_TOP - TIMELINE_HEIGHT));

        if (width < TIMELINE_INSET * 3) {
            timelineControlLive.getAttributes().setVisible(false);
            timelineControlFaster.getAttributes().setVisible(false);
            timelineControlSkipForward.getAttributes().setVisible(false);
            timelineControlPlay.getAttributes().setVisible(false);
            timelineControlPause.getAttributes().setVisible(false);
            timelineControlSkipBack.getAttributes().setVisible(false);
            timelineControlSlower.getAttributes().setVisible(false);
        } else {
            timelineControlLive.getAttributes().setVisible(true);
            timelineControlFaster.getAttributes().setVisible(true);
            timelineControlSkipForward.getAttributes().setVisible(true);
            if (TimeManager.isPaused()) {
                timelineControlPlay.getAttributes().setVisible(true);
                timelineControlPause.getAttributes().setVisible(false);
            } else {
                timelineControlPlay.getAttributes().setVisible(false);
                timelineControlPause.getAttributes().setVisible(true);
            }
            timelineControlSkipBack.getAttributes().setVisible(true);
            timelineControlSlower.getAttributes().setVisible(true);

            int x = CONTROL_BUTTON_WIDTH + PADDING;
            int y = TIMELINE_TOP - TIMELINE_HEIGHT;
            timelineControlSlower.setScreenPoint(new Point(x, y));
            x += (CONTROL_BUTTON_WIDTH) + PADDING;
            timelineControlSkipBack.setScreenPoint(new Point(x, y));
            x += (CONTROL_BUTTON_WIDTH) + PADDING;
            timelineControlPlay.setScreenPoint(new Point(x, y));
            timelineControlPause.setScreenPoint(new Point(x, y));
            x += (CONTROL_BUTTON_WIDTH) + PADDING;
            timelineControlSkipForward.setScreenPoint(new Point(x, y));
            x += (CONTROL_BUTTON_WIDTH) + PADDING;
            timelineControlFaster.setScreenPoint(new Point(x, y));
            x += (CONTROL_BUTTON_WIDTH) + PADDING;
            timelineControlLive.setScreenPoint(new Point(x, y));
        }

        timelineAnnotation.getAttributes().setSize(new Dimension(width - TIMELINE_INSET * 2 - ZOOM_BAR_WIDTH,
                TIMELINE_HEIGHT));
        timelineAnnotation.setScreenPoint(new Point(centerX - ZOOM_BAR_WIDTH / 2,
                TIMELINE_TOP - TIMELINE_HEIGHT));

        dc.addOrderedRenderable(timelineAnnotation);
        dc.addOrderedRenderable(statusAnnotation);

        super.doRender(dc);
    }

    @Override
    public void selected(SelectEvent event) {
        if (event.getMouseEvent() != null && event.getMouseEvent().isConsumed()
                || event.getTopObject() == null
                || event.getTopPickedObject().getParentLayer() != this
                || !(event.getTopObject() instanceof AVList)) {
            if (currentAnnotation instanceof ControlButton) {
                ((ControlButton) currentAnnotation).highlight(false);
            }
            currentAnnotation = null;
            return;
        }

        switch (event.getEventAction()) {
            case SelectEvent.ROLLOVER -> handleMouseOverEvent(event);
            case SelectEvent.DRAG -> {
                if (event.getTopObject() == timelineAnnotation) {
                    timelineAnnotation.doDrag(event);
                    event.consume();
                } else if (event.getTopObject() == background) {
                    event.consume();
                }
            }
            case SelectEvent.DRAG_END -> {
                if (event.getTopObject() == timelineAnnotation) {
                    timelineAnnotation.doDragEnd();
                    event.consume();
                }
            }
            case SelectEvent.LEFT_PRESS -> handleLeftPressEvent(event);
            default -> {
                // ignore
//                if (Logger.getGlobal().isLoggable(Level.FINEST)) {
//                    String message = String.format("Unexpected value: %s", event.getEventAction());
//                    Logger.getGlobal().log(Level.WARNING, message);
//                }
            }
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (currentAnnotation == timelineAnnotation) {
            if (e.getWheelRotation() > 0) {
                timelineAnnotation.zoomOut();
            } else {
                timelineAnnotation.zoomIn();
            }
            e.consume();
        } else if (currentAnnotation == background
                || currentAnnotation == statusAnnotation
                || currentAnnotation instanceof ControlButton) {
            e.consume();
        }
    }
}
