package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ZapTrapElement {

    public static ZapTrapElement parse(Node node, long readingTime) {
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
            return new ZapTrapElement(propertiesElement);
        } else {
            return null;
        }
    }

    private final PropertiesElement propertiesElement;

    public ZapTrapElement(PropertiesElement propertiesElement) {
        this.propertiesElement = propertiesElement;
    }

    public PropertiesElement getPropertiesElement() {
        return propertiesElement;
    }
}
