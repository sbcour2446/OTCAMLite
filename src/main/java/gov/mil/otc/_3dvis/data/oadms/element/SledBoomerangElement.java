package gov.mil.otc._3dvis.data.oadms.element;

import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SledBoomerangElement extends PropertiesElement {

    public static final String ELEMENT_NAME = "sledBoomerang";
    public static final String COMMON_NAME = "cSDS";

    protected SledBoomerangElement() {
        super();
    }

    public static PropertiesElement parse(Node node, long readingTime) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(PropertiesElement.ELEMENT_NAME)) {
                return PropertiesElement.parse(child, readingTime);
            }
        }
        return null;
    }
}
