package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.event.Event;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polygon;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LidarScan extends Event {

    private final List<LidarPlacemark> placemarkList = new ArrayList<>();
    private final int scanId;

    public LidarScan(int scanId, long eventTime, long endTime) {
        super(eventTime, endTime);
        this.scanId = scanId;
    }

    public void setOpacity(final double opacity) {
        SwingUtilities.invokeLater(() -> {
            for (Polygon polygon : placemarkList) {
                polygon.getAttributes().setOutlineOpacity(opacity);
                polygon.getAttributes().setInteriorOpacity(opacity);
            }
        });
    }

    public void setOverrideHeight(boolean overrideHeight, double height) {
        for (LidarPlacemark lidarPlacemark : placemarkList) {
            lidarPlacemark.setOverrideHeight(overrideHeight, height);
        }
    }

    @Override
    protected void setType() {
        type = "LidarScan";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void update(long time, RenderableLayer layer) {
        boolean isActive = isActive(time);
        if (isActive) {
            if (!isVisible) {
                for (Polygon polygon : placemarkList) {
                    layer.addRenderable(polygon);
                }
                isVisible = true;
            }
        } else if (isVisible) {
            for (Polygon polygon : placemarkList) {
                layer.removeRenderable(polygon);
            }
            isVisible = false;
        }
    }

    @Override
    public void dispose(RenderableLayer layer) {
        if (isVisible) {
            for (Polygon polygon : placemarkList) {
                layer.removeRenderable(polygon);
            }
            isVisible = false;
        }
    }

    public int getScanId() {
        return scanId;
    }

    public void addPlacemark(LidarPlacemark lidarPlacemark) {
        placemarkList.add(lidarPlacemark);

    }

    public List<LidarPlacemark> getLidarPlacemarkList() {
        return placemarkList;
    }
}
