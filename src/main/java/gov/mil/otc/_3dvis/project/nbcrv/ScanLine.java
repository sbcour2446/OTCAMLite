package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Path;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ScanLine {

    private final TimedDataSet<ScanLineInfo> scanLineInfoTimedDataSet = new TimedDataSet<ScanLineInfo>();
    private final Path scanLine = new Path();
    private final String deviceName;
    private Position position;
    private boolean showing = false;

    public ScanLine(String deviceName) {
        this.deviceName = deviceName;
    }

    public void addScanLineInfo(long timestamp, double yaw) {
        scanLineInfoTimedDataSet.add(new ScanLineInfo(timestamp, yaw));
    }

    public void update(long time, Position position, boolean isFiltered) {
        boolean hasChange = scanLineInfoTimedDataSet.updateTime(time);
        ScanLineInfo scanLineInfo = scanLineInfoTimedDataSet.getCurrent();

        if (position != null) {
            this.position = position;
            hasChange = true;
        }

        boolean show = isFiltered && scanLineInfo != null && this.position != null &&
                time >= scanLineInfo.getTimestamp() &&
                time < scanLineInfo.getTimestamp() + 10;

        if (show) {
            if (!showing) {
                showing = true;
                EntityLayer.add(scanLine);
            }
            if (hasChange) {
                updateVisual(scanLineInfo);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(scanLine);
        }

    }

    protected void dispose() {
        if (showing) {
            showing = false;
            EntityLayer.remove(scanLine);
        }
    }

    private void updateVisual(final ScanLineInfo scanLineInfo) {
        SwingUtilities.invokeLater(() -> {
            List<Position> endPoints = new ArrayList<>();
            endPoints.add(position);
            endPoints.add(scanLineInfo.getEndPoint(position));
            scanLine.setPositions(endPoints);
            scanLine.setValue(AVKey.ROLLOVER_TEXT, String.format("Scan: %s", deviceName));
        });
    }

    private class ScanLineInfo extends TimedData {

        private final double yaw;

        protected ScanLineInfo(long timestamp, double yaw) {
            super(timestamp);
            this.yaw = yaw;
        }

        public double getYaw() {
            return yaw;
        }

        public Position getEndPoint(Position position) {
            if (position != null) {
                return new Position(LatLon.greatCircleEndPosition(position, Angle.fromDegrees(yaw),
                        Angle.fromDegrees(6000.0)), 0);
            } else {
                return null;
            }
        }
    }
}
