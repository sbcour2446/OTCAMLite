package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.worldwindex.render.Circle;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;

import javax.swing.*;
import java.awt.*;

public class Range {

    private final TimedDataSet<RangeData> rangeDataTimedDataSet = new TimedDataSet<>();
    private final Circle rangeCircle = new Circle(Position.ZERO, 0);
    private final int id;
    private Position center;
    private boolean showing = false;

    protected Range(int id, Position center) {
        this.id = id;
        this.center = center;

        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setOutlineMaterial(new Material(Color.YELLOW));
        rangeCircle.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        rangeCircle.setAttributes(shapeAttributes);
        rangeCircle.setOffset(1);
    }

    protected int getId() {
        return id;
    }

    protected void addRangeData(long timestamp, float range) {
        rangeDataTimedDataSet.add(new RangeData(timestamp, range));
    }

    protected void update(long time, Position center, boolean isFiltered) {
        boolean hasChange = rangeDataTimedDataSet.updateTime(time);
        RangeData rangeData = rangeDataTimedDataSet.getCurrent();

        if (center != null) {
            this.center = center;
            hasChange = true;
        }

        boolean show = isFiltered && rangeData != null &&
                time >= rangeData.getTimestamp() &&
                time < rangeData.getTimestamp() + SettingsManager.getSettings().getDlmSetting().getRangeDisplayTime();

        if (show) {
            if (!showing) {
                showing = true;
                EntityLayer.add(rangeCircle);
            }
            if (hasChange) {
                updateVisual(rangeData);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(rangeCircle);
        }
    }

    protected void dispose() {
        if (showing) {
            showing = false;
            EntityLayer.remove(rangeCircle);
        }
    }

    private void updateVisual(final RangeData rangeData) {
        SwingUtilities.invokeLater(() -> {
            rangeCircle.setCenter(center);
            rangeCircle.setRadius(rangeData.getRange());
            rangeCircle.setValue(AVKey.ROLLOVER_TEXT, String.format("%.2f", rangeData.getRange()));
        });
    }

    private static class RangeData extends TimedData {

        private final double range;

        private RangeData(long timestamp, double range) {
            super(timestamp);
            this.range = range;
        }

        public double getRange() {
            return range;
        }
    }
}
