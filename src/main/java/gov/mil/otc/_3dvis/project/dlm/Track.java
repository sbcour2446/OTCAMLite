package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.project.dlm.message.TrackMessage;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Track {

    private static final Color TRACK_COLOR_UNKNOWN = Color.LIGHT_GRAY;
    private static final Color TRACK_COLOR = Color.YELLOW;
    private final TimedDataSet<TrackData> trackDataTimedDataSet = new TimedDataSet<>();
    private final Path trackLine = new Path();
    private final int id;
    private Position center;
    private boolean showing = false;

    protected Track(int id, Position center) {
        this.id = id;
        this.center = center;

        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setOutlineMaterial(new Material(TRACK_COLOR_UNKNOWN));
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setOutlineWidth(3);
        shapeAttributes.setOutlineStippleFactor(4);
        trackLine.setAttributes(shapeAttributes);
        trackLine.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        trackLine.setFollowTerrain(true);
        trackLine.setOffset(1);
    }

    protected long getId() {
        return id;
    }

    protected void addTrackData(long timestamp, float bearing, float quality, TrackMessage.GenClass genClass) {
        trackDataTimedDataSet.add(new TrackData(timestamp, bearing, quality, genClass));
    }

    protected void update(long time, Position center, boolean isFiltered) {
        boolean hasChange = trackDataTimedDataSet.updateTime(time);
        TrackData trackData = trackDataTimedDataSet.getCurrent();

        if (center != null) {
            this.center = center;
            hasChange = true;
        }

        boolean show = isFiltered && trackData != null &&
                time >= trackData.getTimestamp() &&
                time < trackData.getTimestamp() + SettingsManager.getSettings().getDlmSetting().getTrackDisplayTime();
        if (show) {
            if (!showing) {
                showing = true;
                EntityLayer.add(trackLine);
            }
            if (hasChange) {
                updateTrackLine(trackData);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(trackLine);
        }
    }

    protected void dispose() {
        if (showing) {
            showing = false;
            EntityLayer.remove(trackLine);
        }
    }

    private void updateTrackLine(final TrackData trackData) {
        SwingUtilities.invokeLater(() -> {
            List<Position> endPoints = new ArrayList<>();
            endPoints.add(center);
            endPoints.add(trackData.getEndPoint(center));
            trackLine.setPositions(endPoints);
            trackLine.setValue(AVKey.ROLLOVER_TEXT,
                    String.format("Track: %d %nBearing: %.1f %nQuality: %.1f %nClassification: %s",
                            getId(), trackData.getBearing(), trackData.getQuality(),
                            trackData.getGenClass().getDescription()));
            if (trackData.getQuality() < 17) {
                trackLine.getAttributes().setOutlineStippleFactor(0);
            } else {
                trackLine.getAttributes().setOutlineStippleFactor(4);
            }
            if (trackData.getGenClass() == TrackMessage.GenClass.UNKNOWN) {
                trackLine.getAttributes().setOutlineMaterial(new Material(TRACK_COLOR_UNKNOWN));
            } else {
                trackLine.getAttributes().setOutlineMaterial(new Material(TRACK_COLOR));
            }
        });
    }

    private static class TrackData extends TimedData {

        private final float bearing;
        private final float quality;
        private final TrackMessage.GenClass genClass;

        protected TrackData(long timestamp, float bearing, float quality, TrackMessage.GenClass genClass) {
            super(timestamp);
            this.bearing = bearing;
            this.quality = quality;
            this.genClass = genClass;
        }

        public Position getEndPoint(Position center) {
            if (center != null) {
                return new Position(LatLon.greatCircleEndPosition(center, Angle.fromDegrees(bearing),
                        SettingsManager.getSettings().getDlmSetting().getTrackLineLength()), 0);
            } else {
                return null;
            }
        }

        public float getBearing() {
            return bearing;
        }

        public float getQuality() {
            return quality;
        }

        public TrackMessage.GenClass getGenClass() {
            return genClass;
        }
    }
}
