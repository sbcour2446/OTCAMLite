package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.data.oadms.WdlReading;
import gov.mil.otc._3dvis.entity.base.EntityId;

import java.util.List;

public class WdlEntity extends SidecarEntity {

    public WdlEntity(EntityId entityId) {
        super(entityId);
    }

    public void addWdlReadings(List<WdlReading> wdlReadings) {
        WdlDevice device = getOrCreateWdlDevice();
        device.addWdlReadings(wdlReadings);
    }

    public WdlDevice getOrCreateWdlDevice() {
        GenericDevice device = getDevice("wdl");
        if (!(device instanceof WdlDevice)) {
            device = new WdlDevice("wdl");
            addDevice(device);
        }
        return (WdlDevice) device;
    }

    public void recalculateReadings() {
        GenericDevice device = getDevice("wdl");
        if (device instanceof WdlDevice) {
            ((WdlDevice)device).recalculateReadings();
        }
    }
}
