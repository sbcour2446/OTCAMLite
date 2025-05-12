package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;

import javax.swing.*;
import java.awt.*;

public class Target {

    private final TimedDataSet<TargetData> targetDataTimedDataSet = new TimedDataSet<>();
    private final Ellipsoid targetCircle = new Ellipsoid(Position.ZERO,
            SettingsManager.getSettings().getDlmSetting().getTargetRadius(),
            SettingsManager.getSettings().getDlmSetting().getTargetHeight(),
            SettingsManager.getSettings().getDlmSetting().getTargetRadius());
    private final long id;
    private final double bearingOffset;
    private Position center;
    private boolean showing = false;

    protected Target(long id, double bearingOffset, Position center) {
        this.id = id;
        this.bearingOffset = bearingOffset;
        this.center = center;

        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setInteriorMaterial(new Material(Color.ORANGE));
        shapeAttributes.setInteriorOpacity(.5);
        shapeAttributes.setDrawOutline(false);
        targetCircle.setAttributes(shapeAttributes);
        targetCircle.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
    }

    protected long getId() {
        return id;
    }

    protected void addTargetData(long timestamp, float trackX, float trackY, float xDot, float yDot) {
        //convert to polar so the angle can be rotated by the DLM alignment
        double distance = Math.sqrt(trackX * trackX + trackY * trackY);
        double speed = Math.sqrt(xDot * xDot + yDot * yDot);
        double azimuth = Math.atan2(trackX, trackY) + bearingOffset;
        double range = distance * 1 / 6371000.0; // approximate distance in radians on a sphere

        targetDataTimedDataSet.add(new TargetData(timestamp, speed, distance, azimuth, range));
    }

    protected void update(long time, Position center, boolean isFiltered) {
        boolean hasChange = targetDataTimedDataSet.updateTime(time);
        TargetData targetData = targetDataTimedDataSet.getCurrent();

        if (center != null) {
            this.center = center;
            hasChange = true;
        }

        boolean show = isFiltered && targetData != null &&
                time >= targetData.getTimestamp() &&
                time < targetData.getTimestamp() + SettingsManager.getSettings().getDlmSetting().getTargetDisplayTime();

        if (show) {
            if (!showing) {
                showing = true;
                EntityLayer.add(targetCircle);
            }
            if (hasChange) {
                updateVisual(targetData);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(targetCircle);
        }
    }

    protected void dispose() {
        if (showing) {
            showing = false;
            EntityLayer.remove(targetCircle);
        }
    }

    private void updateVisual(final TargetData targetData) {
        final Position centerPosition = new Position(targetData.getLocation(center), 0);
        SwingUtilities.invokeLater(() -> {
            targetCircle.setCenterPosition(centerPosition);
            String description = String.format("Fusion Target %d%nRange %.1f m%nSpeed %.1f kph",
                    getId(), targetData.getDistance(), targetData.getSpeed() * 3.6);
            targetCircle.setValue(AVKey.ROLLOVER_TEXT, description);
        });
    }

    private static class TargetData extends TimedData {

        private final double speed;
        private final double distance;
        private final double azimuth;
        private final double range;

        private TargetData(long timestamp, double speed, double distance, double azimuth, double range) {
            super(timestamp);
            this.speed = speed;
            this.distance = distance;
            this.azimuth = azimuth;
            this.range = range;
        }

        public LatLon getLocation(Position center) {
            return LatLon.greatCircleEndPosition(center, azimuth, range);
        }

        public double getSpeed() {
            return speed;
        }

        public double getDistance() {
            return distance;
        }
    }
}
