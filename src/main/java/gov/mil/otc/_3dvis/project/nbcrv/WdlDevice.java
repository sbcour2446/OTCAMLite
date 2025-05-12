package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.data.oadms.WdlReading;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static java.lang.Math.sin;

public class WdlDevice extends GenericDevice {

    private final TimedDataSet<WdlReading> wdlReadingTimedDataSet = new TimedDataSet<>();
    private WdlReading currentWdlReading = null;
    private final Path focusLine = new Path();
    private boolean showing = false;
    private final List<CloudLine> cloudLineList = Collections.synchronizedList(new ArrayList<>());
    private int currentCloudIndex = -1;
    private double lastAzimuth = 0;

    public WdlDevice(String name) {
        super(name);
        initialize();
    }

    private void initialize() {
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setOutlineMaterial(new Material(SettingsManager.getSettings().getNbcrvSettings().getDeviceColor(getName())));
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setOutlineWidth(2);
        focusLine.setAttributes(shapeAttributes);
        focusLine.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
    }

    public void addWdlReadings(List<WdlReading> wdlReadings) {
        for (WdlReading wdlReading : wdlReadings) {
            wdlReading.recalculate();
        }
        wdlReadingTimedDataSet.addAll(wdlReadings);
    }

    public void recalculateReadings() {
        TimeManager.setPause(true);
        removeAllCloudLineRenderables();
        List<WdlReading> wdlReadings =  wdlReadingTimedDataSet.getAll();
        int counter = 0;
        for (WdlReading wdlReading : wdlReadings) {
            if (++counter % 1000 == 0) {
                System.out.println(String.format("%d of %d", counter, wdlReadings.size()));
            }
            wdlReading.recalculate();
        }
    }

    @Override
    public boolean update(long time, Position position, boolean isFiltered, boolean isInScope) {
        boolean hasChange = super.update(time, position, isFiltered, isInScope);
        hasChange |= wdlReadingTimedDataSet.updateTime(time);
        WdlReading wdlReading = isInScope ? wdlReadingTimedDataSet.getCurrent() : null;

        boolean cloudChange = currentWdlReading != wdlReading;
        if (cloudChange) {
            currentWdlReading = wdlReading;
            hasChange = true;
        }

        boolean show = isFiltered && currentWdlReading != null && time >= currentWdlReading.getTimestamp() &&
                time < currentWdlReading.getTimestamp() + 2000;

        if (show) {
            if (!showing) {
                showing = true;
                EntityLayer.add(focusLine);
            }
            if (hasChange) {
                updateVisual(currentWdlReading, position);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(focusLine);
        }

        if (cloudChange) {
            CloudLine cloudLine = null;
            if (currentWdlReading != null) {
                cloudLine = new CloudLine(currentWdlReading, position);
            }
            updateCloud(cloudLine);
        }

        return hasChange;
    }

    private void addCloudLineRenderables(CloudLine cloudLine) {
        for (Renderable renderable : cloudLine.getCloudRenderables()) {
            EntityLayer.add(renderable);
        }
    }

    private void removeCloudLineRenderables(CloudLine cloudLine) {
        for (Renderable renderable : cloudLine.getCloudRenderables()) {
            EntityLayer.remove(renderable);
        }
    }

    private void removeAllCloudLineRenderables() {
        synchronized (cloudLineList) {
            for (CloudLine cloudLine : cloudLineList) {
                removeCloudLineRenderables(cloudLine);
            }
            cloudLineList.clear();
        }
    }

    private void updateCloud(CloudLine cloudLine) {
        if (cloudLine == null) {
            removeAllCloudLineRenderables();
            return;
        }

        if (cloudLineList.isEmpty()) {
            cloudLineList.add(cloudLine);
            addCloudLineRenderables(cloudLine);
            currentCloudIndex = 0;
            lastAzimuth = cloudLine.getAzimuth();
            return;
        }

        int nextCloudIndex;
        boolean increasing;
        if (lastAzimuth > currentWdlReading.getAzimuth()) {
            increasing = false;
            nextCloudIndex = currentCloudIndex;
        } else {
            increasing = true;
            nextCloudIndex = currentCloudIndex + 1;
        }

        if (increasing) {
            while (nextCloudIndex < cloudLineList.size()) {
                if (cloudLineList.get(nextCloudIndex).getAzimuth() <= cloudLine.getAzimuth()) {
                    removeCloudLineRenderables(cloudLineList.get(nextCloudIndex));
                    cloudLineList.remove(nextCloudIndex);
                } else {
                    break;
                }
            }
            cloudLineList.add(nextCloudIndex, cloudLine);
            addCloudLineRenderables(cloudLine);
            currentCloudIndex = nextCloudIndex;
            lastAzimuth = cloudLine.getAzimuth();
        } else {
            while (nextCloudIndex > 0) {
                if (cloudLineList.get(nextCloudIndex-1).getAzimuth() > cloudLine.getAzimuth()) {
                    nextCloudIndex--;
                    removeCloudLineRenderables(cloudLineList.get(nextCloudIndex));
                    cloudLineList.remove(nextCloudIndex);
                } else {
                    break;
                }
            }
            cloudLineList.add(nextCloudIndex, cloudLine);
            addCloudLineRenderables(cloudLine);
            currentCloudIndex = nextCloudIndex;
            lastAzimuth = cloudLine.getAzimuth();
        }
    }

    private void updateVisual(final WdlReading wdlReading, final Position position) {
        SwingUtilities.invokeLater(() -> {
            List<Position> endPoints = new ArrayList<>();
            endPoints.add(new Position(position, 3));
            endPoints.add(getScanLineEndPoint(position, wdlReading.getAzimuth(), wdlReading.getElevation()));
            focusLine.setPositions(endPoints);
            focusLine.setValue(AVKey.ROLLOVER_TEXT, String.format("Scan: %s", getName()));
        });
    }

    private Position getScanLineEndPoint(Position position, double yaw, double pitch) {
        if (position != null) {
            double height = 3;
            if (SettingsManager.getSettings().getNbcrvSettings().isUsePitch()) {
                height = sin(pitch * 0.0174533) * 6000.0;
            }
            double range = 6.0 / 6371.0; // approximate distance in radians on a sphere
            return new Position(LatLon.greatCircleEndPosition(position, Angle.fromDegrees(yaw),
                    Angle.fromRadians(range)), height);
        } else {
            return null;
        }
    }
}
