package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UgvVehicleElement {

    public static final String UGV_VEHICLE_ELEMENT = "ugvVehicle";

    public static UgvVehicleElement parse(Node node, long readingTime) {
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
            return new UgvVehicleElement(propertiesElement);
        } else {
            return null;
        }
    }

    private final PropertiesElement propertiesElement;

    public UgvVehicleElement(PropertiesElement propertiesElement) {
        this.propertiesElement = propertiesElement;
    }

    public PropertiesElement getPropertiesElement() {
        return propertiesElement;
    }
}
