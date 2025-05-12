package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.IconImageHelper;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.settings.IconType;
import gov.nasa.worldwind.geom.Position;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SidecarEntity extends AbstractEntity {

    private final Map<String, GenericDevice> deviceList = new HashMap<>();

    public SidecarEntity(EntityId entityId) {
        super(entityId);
    }

    public List<GenericDevice> getDeviceList() {
        return new ArrayList<>(deviceList.values());
    }

    public void addDevice(GenericDevice device) {
        deviceList.put(device.getName(), device);
    }

    public void addDevices(List<GenericDevice> devices) {
        for (GenericDevice device : devices) {
            deviceList.put(device.getName(), device);
        }
    }

    public GenericDevice getDevice(String name) {
        return deviceList.get(name);
    }

    public GenericDevice getOrCreateDevice(String name) {
        GenericDevice device = deviceList.get(name);
        if (device == null) {
            device = new GenericDevice(name);
            deviceList.put(name, device);
        }
        return device;
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        boolean hasChange = super.update(time, entityFilter);
        boolean hasDeviceChange = false;
        Position position = getPosition();

        boolean isDisplayable = position != null && getEntityDetail() != null && isFiltered();

        synchronized (deviceList) {
            for (GenericDevice device : deviceList.values()) {
                hasDeviceChange = device.update(time, position, isDisplayable, isInScope());
            }
        }

        return hasChange || hasDeviceChange;
    }

    @Override
    public BufferedImage createIcon() {
        return IconImageHelper.getPinIcon(Color.GREEN);
    }

    @Override
    public IconType getIconType() {
        return IconType.PIN;
    }

    @Override
    protected SidecarStatusAnnotation createStatusAnnotation() {
        return new SidecarStatusAnnotation(this);
    }
}