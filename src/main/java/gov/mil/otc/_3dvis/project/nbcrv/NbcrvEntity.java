package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;

import java.io.File;
import java.util.*;

public class NbcrvEntity extends AbstractEntity {

    private final Map<String, Device> deviceList = new HashMap<>();
    private final TimedDataSet<NbcrvState> nbcrvStateTimedDataSet = new TimedDataSet<>();
    private final TimedDataSet<RadNucState> radNucStateTimedDataSet = new TimedDataSet<>();
    private final Map<Long, File> timedImageMap = new TreeMap<>();
    private final List<TimedFile> tirFileList = new ArrayList<>();
    private final List<TimedFile> timedFileList = new ArrayList<>();
    private final RenderableLayer eventLayer = new RenderableLayer();

    public NbcrvEntity(EntityId entityId) {
        super(entityId);
        eventLayer.setName("NbcrvEntity:" + entityId.toString());
        eventLayer.setPickEnabled(true);
        WWController.addLayer(eventLayer);
    }

    public NbcrvState getCurrentState() {
        return nbcrvStateTimedDataSet.getCurrent();
    }

    public RadNucState getCurrentRadNucState() {
        return radNucStateTimedDataSet.getCurrent();
    }

    public List<Device> getDeviceList() {
        return new ArrayList<>(deviceList.values());
    }

    public void addDevice(Device device) {
        deviceList.put(device.getName(), device);
    }

    public void addDevices(List<Device> devices) {
        for (Device device : devices) {
            deviceList.put(device.getName(), device);
        }
    }

    public Device getDevice(String name) {
        return deviceList.get(name);
    }

    public Device getOrCreateDevice(String name) {
        Device device = deviceList.get(name);
        if (device == null) {
            device = new Device(name);
            deviceList.put(name, device);
        }
        return device;
    }

    public void addNbcrvStates(List<NbcrvState> nbcrvStates) {
        nbcrvStateTimedDataSet.addAll(nbcrvStates);
    }

    public void addRadNucState(RadNucState radNucState) {
        radNucStateTimedDataSet.add(radNucState);
    }

    public void addRadNucStates(List<RadNucState> radNucStates) {
        radNucStateTimedDataSet.addAll(radNucStates);
    }

    public void addTimedFile(TimedFile timedFile) {
        if (timedFile.getFileType() == TimedFile.FileType.IMAGE) {
            timedImageMap.put(timedFile.getTimestamp(), timedFile.getFile());
        } else if (timedFile.getFileType() == TimedFile.FileType.PDF) {
            tirFileList.add(timedFile);
        } else {
            timedFileList.add(timedFile);
        }
    }

    public List<TimedFile> getTirFileList() {
        return tirFileList;
    }

    public List<TimedFile> getTimedFileList() {
        return timedFileList;
    }

    public void addTimedImage(TimedFile timedImage) {
        timedImageMap.put(timedImage.getTimestamp(), timedImage.getFile());
    }

    public Map<Long, File> getTimedImageMap() {
        return timedImageMap;
    }

    public void addNbcrvDetection(NbcrvDetection nbcrvDetection) {
        Device device = getDevice(nbcrvDetection.getDeviceName());
        if (device != null) {
            device.addNbcrvEvent(nbcrvDetection);
        }
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        boolean hasChange = super.update(time, entityFilter);
        hasChange |= nbcrvStateTimedDataSet.updateTime(time);
        hasChange |= radNucStateTimedDataSet.updateTime(time);

        Position position = getPosition();
        boolean isDisplayable = position != null && getEntityDetail() != null && !getEntityDetail().isOutOfComms() && isFiltered();

        boolean hasDeviceChange = false;
        TspiData tspiData = getCurrentTspi();
        double heading = 0;
        if (tspiData != null) {
            heading = tspiData.getHeading();
        }
        for (Device device : deviceList.values()) {
            hasDeviceChange = device.update(time, position, isDisplayable, isInScope(), heading);
        }

        return hasChange || hasDeviceChange;
    }

    @Override
    protected NbsrvStatusAnnotation createStatusAnnotation() {
        return new NbsrvStatusAnnotation(this);
    }
}
