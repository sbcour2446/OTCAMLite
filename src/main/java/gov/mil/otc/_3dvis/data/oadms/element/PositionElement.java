package gov.mil.otc._3dvis.data.oadms.element;

import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PositionElement {

    public static final String ELEMENT_NAME = "position";
    private static final String ALTITUDE_ELEMENT = "altitude";
    private static final String LATITUDE_ELEMENT = "latitude";
    private static final String LONGITUDE_ELEMENT = "longitude";

    public static Position parse(Node node) {
        NodeList nodeList = node.getChildNodes();
        Double altitude = null;
        Double latitude = null;
        Double longitude = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(ALTITUDE_ELEMENT)) {
                altitude = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(LATITUDE_ELEMENT)) {
                latitude = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(LONGITUDE_ELEMENT)) {
                longitude = ElementUtility.parseDouble(child);
            }
        }
        if (altitude == null) {
            altitude = 0.0;
        }
        if (latitude != null && longitude != null) {
            return Position.fromDegrees(latitude, longitude, altitude);
        }
        return null;
    }

    private PositionElement() {
    }
}
