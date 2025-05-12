package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.data.oadms.element.*;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.DeviceState;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RocketSledXmlFile extends OadmsXmlFile {

    private final List<TspiData> sledTspiList = new ArrayList<>();
    private final List<TspiData> ugvTspiList = new ArrayList<>();
    private final List<NbcrvDetection> nbcrvDetectionList = new ArrayList<>();
    private final Map<String, Device> sledDeviceList = new HashMap<>();
    private final Map<String, Device> ugvDeviceList = new HashMap<>();

    public RocketSledXmlFile(File file) {
        super(file);
    }

    public List<TspiData> getSledTspiList() {
        return sledTspiList;
    }

    public List<TspiData> getUgvTspiList() {
        return ugvTspiList;
    }

    public List<NbcrvDetection> getNbcrvEventList() {
        return nbcrvDetectionList;
    }

    public List<Device> getSledDeviceList() {
        return new ArrayList<>(sledDeviceList.values());
    }

    public List<Device> getUgvDeviceList() {
        return new ArrayList<>(ugvDeviceList.values());
    }

    @Override
    protected void parseReadingElement(Node node) {
        ReadingElement readingElement = ReadingElement.parse(node);
        if (readingElement == null || readingElement.getReadingTime() == 0) {
            return;
        }

        if (readingElement.getReadingTime() > stopTime) {
            stopTime = readingElement.getReadingTime();
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(SledVehicleElement.SLED_VEHICLE_ELEMENT)) {
                parseSledVehicleElement(child, readingElement.getReadingTime());
            } else if (child.getNodeName().equalsIgnoreCase(UgvVehicleElement.UGV_VEHICLE_ELEMENT)) {
                parseUgvVehicleElement(child, readingElement.getReadingTime());
            } else if (child.getNodeName().equalsIgnoreCase(SledBoomerangElement.ELEMENT_NAME)) {
                parseSledBoomerangElement(child, readingElement.getReadingTime());
            } else if (child.getNodeName().equalsIgnoreCase(MissionElement.ELEMENT_NAME)) {
                parseEventThreadEventElement(child, readingElement.getReadingTime());
            } else if (DeviceElement.isElement(child.getNodeName())) {
                parseDeviceElement(child, readingElement.getReadingTime());
            }
        }
    }

    private void parseSledVehicleElement(Node node, long readingTime) {
        PropertiesElement propertiesElement = SledVehicleElement.parse(node, readingTime);
        if (propertiesElement != null) {
            sledTspiList.add(new TspiData(propertiesElement.getTimestamp(),
                    propertiesElement.getPosition()));
        }
    }

    private void parseUgvVehicleElement(Node node, long readingTime) {
        UgvVehicleElement ugvVehicleElement = UgvVehicleElement.parse(node, readingTime);
        if (ugvVehicleElement != null) {
            ugvTspiList.add(new TspiData(ugvVehicleElement.getPropertiesElement().getTimestamp(),
                    ugvVehicleElement.getPropertiesElement().getPosition()));
        }
    }

    private void parseSledBoomerangElement(Node node, long readingTime) {
        PropertiesElement propertiesElement = SledBoomerangElement.parse(node, readingTime);
        if (propertiesElement != null) {
            Device device = getOrCreateSledDevice(SledBoomerangElement.COMMON_NAME);
            device.addDeviceState(new DeviceState(propertiesElement));
        }
    }

    private void parseEventThreadEventElement(Node node, long readingTime) {
        EventThreadEventElement eventThreadEventElement = MissionElement.parse(node, readingTime);
        if (eventThreadEventElement != null && eventThreadEventElement.getAction() == EventThreadEventElement.EventAction.ADD_ENTRY) {
            NbcrvDetection nbcrvDetection = NbcrvDetection.create(eventThreadEventElement);
            if (nbcrvDetection != null) {
                nbcrvDetectionList.add(nbcrvDetection);
            }
        }
    }

    private void parseDeviceElement(Node node, long readingTime) {
        DeviceElement deviceElement = DeviceElement.parse(node, readingTime);
        if (deviceElement != null) {
            String deviceName = DeviceElement.getDeviceCommonName(node.getNodeName());
            if (node.getNodeName().startsWith("ugv")) {
                Device device = getOrCreateUgvDevice(deviceName);
                device.addDeviceState(new DeviceState(deviceElement));
            } else {
                Device device = getOrCreateSledDevice(deviceName);
                device.addDeviceState(new DeviceState(deviceElement));
            }
        }
    }

    private Device getOrCreateSledDevice(String name) {
        Device device = sledDeviceList.get(name);
        if (device == null) {
            device = new Device(name);
            sledDeviceList.put(name, device);
        }
        return device;
    }

    private Device getOrCreateUgvDevice(String name) {
        Device device = ugvDeviceList.get(name);
        if (device == null) {
            device = new Device(name);
            ugvDeviceList.put(name, device);
        }
        return device;
    }
}
