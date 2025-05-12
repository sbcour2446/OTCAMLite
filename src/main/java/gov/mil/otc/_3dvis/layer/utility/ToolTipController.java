package gov.mil.otc._3dvis.layer.utility;

import gov.mil.otc._3dvis.WWController;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.util.WWUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ToolTipController implements SelectListener {

    private static ToolTipController singleton;
    protected Object lastRolloverObject;
    protected Object lastHoverObject;
    protected AnnotationLayer layer;
    protected ToolTipAnnotation annotation;

    private ToolTipController() {
        WWController.getWorldWindowPanel().addSelectListener(this);
    }

    public static void initialize() {
        if (singleton == null) {
            singleton = new ToolTipController();
        }
    }

    protected String getHoverText(SelectEvent event) {
        return event.getTopObject() instanceof AVList
                ? ((AVList) event.getTopObject()).getStringValue(AVKey.HOVER_TEXT) : null;
    }

    protected String getRolloverText(SelectEvent event) {
        String text = null;
        if (event.getTopObject() instanceof AVList) {
            text = ((AVList) event.getTopObject()).getStringValue(AVKey.ROLLOVER_TEXT);
            if (WWUtil.isEmpty(text)) {
                text = ((AVList) event.getTopObject()).getStringValue(AVKey.DISPLAY_NAME);
            }
        }
        return text;
    }

    public void selected(SelectEvent event) {
        try {
            if (event.isRollover()) {
                handleRollover(event);
            } else if (event.isHover()) {
                handleHover(event);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    protected void handleRollover(SelectEvent event) {
        if (lastRolloverObject != null) {
            if (lastRolloverObject == event.getTopObject() && !WWUtil.isEmpty(getRolloverText(event))) {
                return;
            }

            hideToolTip();
            lastRolloverObject = null;
            WWController.getWorldWindowPanel().redraw();
        }

        if (getRolloverText(event) != null) {
            lastRolloverObject = event.getTopObject();
            showToolTip(event, getRolloverText(event));
            WWController.getWorldWindowPanel().redraw();
        }
    }

    protected void handleHover(SelectEvent event) {
        if (lastHoverObject != null) {
            if (lastHoverObject == event.getTopObject()) {
                return;
            }

            hideToolTip();
            lastHoverObject = null;
            WWController.getWorldWindowPanel().redraw();
        }

        if (getHoverText(event) != null) {
            lastHoverObject = event.getTopObject();
            showToolTip(event, getHoverText(event));
            WWController.getWorldWindowPanel().redraw();
        }
    }

    protected void showToolTip(SelectEvent event, String text) {
        if (annotation != null) {
            annotation.setText(text);
            annotation.setScreenPoint(event.getPickPoint());
        } else {
            annotation = new ToolTipAnnotation(text);
        }

        if (layer == null) {
            layer = new AnnotationLayer();
            layer.setPickEnabled(false);
        }

        layer.removeAllAnnotations();
        layer.addAnnotation(annotation);
        addLayer(layer);
    }

    protected void hideToolTip() {
        if (layer != null) {
            layer.removeAllAnnotations();
            removeLayer(layer);
            layer.dispose();
            layer = null;
        }

        if (annotation != null) {
            annotation.dispose();
            annotation = null;
        }
    }

    protected void addLayer(Layer layer) {
        if (!WWController.containsLayer(layer)) {
            WWController.addLayer(layer);
        }
    }

    protected void removeLayer(Layer layer) {
        WWController.removeLayer(layer);
    }
}
