package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityLayer;
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
import java.util.Objects;

import static java.lang.Math.sin;

public class Device {

    private final String name;
    private final TimedDataSet<DeviceState> deviceStateTimedDataSet = new TimedDataSet<>();
    private final List<DeviceStateListener> deviceStateListenerList = new ArrayList<>();
    private final List<NbcrvDetection> nbcrvDetectionList = new ArrayList<>();
    private final Path focusLine = new Path();
    private boolean showing = false;
    private boolean interpolate = true;
    private DeviceState currentDeviceState = null;

    public Device(String name) {
        this.name = name;
        initialize();
    }

    private void initialize() {
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setOutlineMaterial(new Material(SettingsManager.getSettings().getNbcrvSettings().getDeviceColor(name)));
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setOutlineWidth(2);
        focusLine.setAttributes(shapeAttributes);
        focusLine.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
    }

    public void addListener(DeviceStateListener deviceStateListener) {
        synchronized (deviceStateListenerList) {
            deviceStateListenerList.add(deviceStateListener);
        }
    }

    public void removeListener(DeviceStateListener deviceStateListener) {
        synchronized (deviceStateListenerList) {
            deviceStateListenerList.remove(deviceStateListener);
        }
    }

    public String getName() {
        return name;
    }

    public void addDeviceState(DeviceState deviceState) {
        deviceStateTimedDataSet.add(deviceState);
    }

    public DeviceState getCurrentDeviceState() {
        return currentDeviceState;
    }

    public List<DeviceState> getDeviceStates() {
        return deviceStateTimedDataSet.getAll();
    }

    public void addNbcrvEvent(NbcrvDetection nbcrvDetection) {
        nbcrvDetectionList.add(nbcrvDetection);
    }

    public List<NbcrvDetection> getNbcrvDetectionList() {
        return nbcrvDetectionList;
    }

    public void updateColor(Color color) {
        focusLine.getAttributes().setOutlineMaterial(new Material(color));
    }

    public boolean update(long time, Position position, boolean isFiltered, boolean isInScope, double heading) {
        boolean hasStateChange = deviceStateTimedDataSet.updateTime(time);

        DeviceState deviceState = isInScope ? deviceStateTimedDataSet.getCurrent() : null;

        if (currentDeviceState != deviceState) {
            currentDeviceState = deviceState;
            hasStateChange = true;
        }

        if (hasStateChange) {
            notifyListeners();
        }

        boolean show = isFiltered && currentDeviceState != null && time >= currentDeviceState.getTimestamp() &&
                time < currentDeviceState.getTimestamp() + 2000;

        if (show) {
            if (!showing) {
                showing = true;
                EntityLayer.add(focusLine);
            }
            if (interpolate) {
                DeviceState nextDeviceState = deviceStateTimedDataSet.getNextAfter(time);
                interpolateAndUpdateVisual(time, currentDeviceState, nextDeviceState, position);
            } else if (hasStateChange) {
                updateVisual(currentDeviceState, position);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(focusLine);
        }

        for (NbcrvDetection nbcrvDetection : nbcrvDetectionList) {
            nbcrvDetection.update(time, position, currentDeviceState);
        }

        return hasStateChange;
    }

    private void notifyListeners() {
        synchronized (deviceStateListenerList) {
            for (DeviceStateListener deviceStateListener : deviceStateListenerList) {
                deviceStateListener.changed(this, currentDeviceState);
            }
        }
    }

    private void interpolateAndUpdateVisual(long time, DeviceState deviceState, DeviceState nextDeviceState, final Position position) {
        if (deviceState == null || nextDeviceState == null || deviceState.getYaw() == null || nextDeviceState.getYaw() == null) {
            updateVisual(deviceState, position);
            return;
        }
        double percentChange = (double) (time - deviceState.getTimestamp()) / (double) (nextDeviceState.getTimestamp() - deviceState.getTimestamp());
        final double newYaw = deviceState.getYaw() + (nextDeviceState.getYaw() - deviceState.getYaw()) * percentChange;
        final double newPitch = deviceState.getPitch() + (nextDeviceState.getPitch() - deviceState.getPitch()) * percentChange;
        SwingUtilities.invokeLater(() -> {
            List<Position> endPoints = new ArrayList<>();
            endPoints.add(new Position(position, SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(name)));
            endPoints.add(getScanLineEndPoint(position, newYaw, newPitch));
            focusLine.setPositions(endPoints);
            focusLine.setValue(AVKey.ROLLOVER_TEXT, String.format("Scan: %s", getName()));
        });
    }

    private void updateVisual(final DeviceState deviceState, final Position position) {
        SwingUtilities.invokeLater(() -> {
            List<Position> endPoints = new ArrayList<>();
            if (deviceState.getYaw() != null) {
                endPoints.add(new Position(position, SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(name)));
                endPoints.add(getScanLineEndPoint(position, deviceState.getYaw(), deviceState.getPitch()));
            }
            focusLine.setPositions(endPoints);
            focusLine.setValue(AVKey.ROLLOVER_TEXT, String.format("Scan: %s", getName()));
        });
    }

    private Position getScanLineEndPoint(Position position, double yaw, double pitch) {
        if (position != null) {
            double height = SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(name);
            if (SettingsManager.getSettings().getNbcrvSettings().isUsePitch()) {
                height += sin(pitch * 0.0174533) * 6000.0;
            }
            double range = 6.0 / 6371.0; // approximate distance in radians on a sphere
            return new Position(LatLon.greatCircleEndPosition(position, Angle.fromDegrees(yaw),
                    Angle.fromRadians(range)), height);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Device device = (Device) o;
        return getName().equals(device.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
