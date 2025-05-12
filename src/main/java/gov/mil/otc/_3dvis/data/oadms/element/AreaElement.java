package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class AreaElement {

    public static final String ELEMENT_NAME = "area";

    public static AreaElement parse(Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(GeographicArcElement.ELEMENT_NAME)) {
                GeographicArcElement geographicArcElement = GeographicArcElement.parse(child);
                if (geographicArcElement != null) {
                    return new AreaElement(geographicArcElement);
                }
            } else if (child.getNodeName().equalsIgnoreCase(GeographicPolygonElement.ELEMENT_NAME)) {
                GeographicPolygonElement geographicPolygonElement = GeographicPolygonElement.parse(child);
                if (geographicPolygonElement != null) {
                    return new AreaElement(geographicPolygonElement);
                }
            }
        }
        return null;
    }

    private final GeographicArcElement geographicArcElement;
    private final GeographicPolygonElement geographicPolygonElement;

    private AreaElement(GeographicArcElement geographicArcElement) {
        this.geographicArcElement = geographicArcElement;
        this.geographicPolygonElement = null;
    }

    private AreaElement(GeographicPolygonElement geographicPolygonElement) {
        this.geographicArcElement = null;
        this.geographicPolygonElement = geographicPolygonElement;
    }

    public GeographicArcElement getGeographicArcElement() {
        return geographicArcElement;
    }

    public GeographicPolygonElement getGeographicPolygonElement() {
        return geographicPolygonElement;
    }
}

