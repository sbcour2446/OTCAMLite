package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.Map;

public class DeviceElement {

    private static final Map<String, String> DEVICE_ELEMENT_LIST = new HashMap<>();

    static {
        DEVICE_ELEMENT_LIST.put("sledboomerang", "cSDS");
        DEVICE_ELEMENT_LIST.put("ugvboomerang", "cSDS");
        DEVICE_ELEMENT_LIST.put("sledzaptrap", "iMCAD");
        DEVICE_ELEMENT_LIST.put("ugvzaptrap", "iMCAD");
        DEVICE_ELEMENT_LIST.put("tar_1", "B330-1");
        DEVICE_ELEMENT_LIST.put("tar_2", "B330-2");
        DEVICE_ELEMENT_LIST.put("tar_3", "B330-3");
        DEVICE_ELEMENT_LIST.put("tar_4", "B330-4");
        DEVICE_ELEMENT_LIST.put("tar_5", "B330-5");
        DEVICE_ELEMENT_LIST.put("tar_6", "B330-6");
        DEVICE_ELEMENT_LIST.put("tar_7", "B330-7");
        DEVICE_ELEMENT_LIST.put("tar_8", "B330-8");
        DEVICE_ELEMENT_LIST.put("tar_9", "B330-9");
        DEVICE_ELEMENT_LIST.put("tar_10", "B330-10");
        DEVICE_ELEMENT_LIST.put("tar_11", "B330-11");
        DEVICE_ELEMENT_LIST.put("tar_12", "B330-12");
    }

    public static boolean isElement(String name) {
        return DEVICE_ELEMENT_LIST.containsKey(name.toLowerCase());
    }

    public static String getDeviceCommonName(String name) {
        return DEVICE_ELEMENT_LIST.get(name.toLowerCase());
    }

    public static DeviceElement parse(Node node, long readingTime) {
        PropertiesElement propertiesElement = null;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(PropertiesElement.ELEMENT_NAME)) {
                propertiesElement = PropertiesElement.parse(child, readingTime);
            }
        }
        if (propertiesElement != null) {
            return new DeviceElement(propertiesElement);
        } else {
            return null;
        }
    }

    private final PropertiesElement propertiesElement;

    public DeviceElement(PropertiesElement propertiesElement) {
        this.propertiesElement = propertiesElement;
    }

    public PropertiesElement getPropertiesElement() {
        return propertiesElement;
    }
}
