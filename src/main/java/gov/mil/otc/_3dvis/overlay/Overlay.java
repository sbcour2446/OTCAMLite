package gov.mil.otc._3dvis.overlay;

import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.symbology.*;

import java.awt.*;
import java.util.*;
import java.util.List;

public class Overlay {

    protected static final Color DEFAULT_COLOR = Color.BLACK;
    protected static final String OVERLAY_ID = "3DVis_Overlay_ID";

    public static Overlay createOverlay(String overlayId, int urn) {
        return new Overlay(overlayId, urn);
    }

    private final String id;
    private final int urn;
    private long currentTime;
    private boolean isGraphic = false;
    private TacticalGraphic shownRenderable = null;
    private final List<OverlayData> history = new ArrayList<>();

    private Overlay(String overlayId, int urn) {
        id = overlayId;
        this.urn = urn;
    }

    public String getId() {
        return id;
    }

    public int getUrn() {
        return urn;
    }

    public boolean isGraphic() {
        return isGraphic;
    }

    public void setGraphic(boolean graphic) {
        isGraphic = graphic;
    }

    public void addNewData(OverlayData data) {
        synchronized (this) {
            if (history.isEmpty()) {
                history.add(data);
            } else if (data.startTime < history.get(0).startTime) {
                data.endTime = Math.min(history.get(0).startTime - 1, data.endTime);// set end 1 milli less than the next start time
                history.add(0, data);
            } else {
                for (int i = history.size() - 1; i >= 0; i--) {
                    if (data.startTime > history.get(i).startTime) {
                        history.get(i).endTime = Math.min(history.get(i).startTime - 1, data.endTime);
                        history.add(i + 1, data);
                    }
                }
            }
        }
    }

    public long getEndTime() {
        return history.isEmpty() ? 0 : history.get(history.size() - 1).endTime;
    }

    public void setEndTime(long timeInMillis) {
        if (!history.isEmpty()) {
            history.get(history.size() - 1).endTime = timeInMillis;
        }
    }

    public void setLineSize(int newSize) {
        synchronized (this) {
            history.forEach(od -> {
                od.lineSize = newSize;
                TacticalGraphic tg = od.getRenderable();
                if (tg != null) {
                    tg.getAttributes().setOutlineWidth((double) newSize);
                }
            });
        }
    }

    public int getLineSize() {
        return history.isEmpty() ? 1 : (int) history.get(history.size() - 1).lineSize;
    }

    public void setScale(double scale) {
        synchronized (this) {
            history.forEach(od -> {
                od.scale = scale;
                TacticalGraphic tg = od.getRenderable();
                if (tg != null) {
                    tg.getAttributes().setScale(scale);
                    tg.getHighlightAttributes().setScale(scale);
                }
            });
        }
    }

    public double getScale() {
        return history.isEmpty() ? .3 : history.get(history.size() - 1).scale;
    }

    public boolean hasLines() {
        return !history.isEmpty() && history.get(history.size() - 1).positions.size() > 1;
    }

    public void setColor(Color newColor) {
        synchronized (this) {
            history.forEach(od -> {
                od.color = newColor;
                TacticalGraphic tg = od.getRenderable();
                if (tg != null) {
                    tg.getAttributes().setOutlineMaterial(new Material(newColor));
                }
            });
        }
    }

    public Color getColor() {
        return history.isEmpty() ? DEFAULT_COLOR : history.get(history.size() - 1).color;
    }

}
