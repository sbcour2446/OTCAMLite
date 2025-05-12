package gov.mil.otc._3dvis.data.oadms.element;

import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class PointElement {

    public static final String ELEMENT_NAME = "point";
    private static final String LATITUDE_ELEMENT = "latitude";
    private static final String LONGITUDE_ELEMENT = "longitude";

    public static Position parse(Node node) {
        Double latitude = null;
        Double longitude = null;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(LATITUDE_ELEMENT)) {
                latitude = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(LONGITUDE_ELEMENT)) {
                longitude = ElementUtility.parseDouble(child);
            }
        }
        if (latitude != null && longitude != null) {
            return Position.fromDegrees(latitude, longitude, 0);
        } else {
            return null;
        }
    }
}
