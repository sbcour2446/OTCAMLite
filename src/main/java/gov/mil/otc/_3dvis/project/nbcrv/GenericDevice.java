package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Path;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.sin;

public class GenericDevice {

    private final String name;
    private final TimedDataSet<GenericDeviceState> genericDeviceStateTimedDataSet = new TimedDataSet<>();
    private final List<GenericDeviceStateListener> deviceStateListenerList = new ArrayList<>();
    private GenericDeviceState currentDeviceState = null;

    public GenericDevice(String name) {
        this.name = name;
    }

    public void addListener(GenericDeviceStateListener deviceStateListener) {
        synchronized (deviceStateListenerList) {
            deviceStateListenerList.add(deviceStateListener);
        }
    }

    public void removeListener(GenericDeviceStateListener deviceStateListener) {
        synchronized (deviceStateListenerList) {
            deviceStateListenerList.remove(deviceStateListener);
        }
    }

    public String getName() {
        return name;
    }

    public void addGenericDeviceState(GenericDeviceState genericDeviceState) {
        genericDeviceStateTimedDataSet.add(genericDeviceState);
    }

    public List<GenericDeviceState> getGenericDeviceStates() {
        return genericDeviceStateTimedDataSet.getAll();
    }

    public boolean update(long time, Position position, boolean isFiltered, boolean isInScope) {
        boolean hasChange = genericDeviceStateTimedDataSet.updateTime(time);
        GenericDeviceState genericDeviceState = isInScope ? genericDeviceStateTimedDataSet.getCurrent() : null;

        if (currentDeviceState != genericDeviceState) {
            currentDeviceState = genericDeviceState;
            hasChange = true;
        }

        if (hasChange) {
            notifyListeners();
        }

        return hasChange;
    }

    private void notifyListeners() {
        synchronized (deviceStateListenerList) {
            for (GenericDeviceStateListener deviceStateListener : deviceStateListenerList) {
                deviceStateListener.changed(this, currentDeviceState);
            }
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

        GenericDevice device = (GenericDevice) o;
        return getName().equals(device.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
