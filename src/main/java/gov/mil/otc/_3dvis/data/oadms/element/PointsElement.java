package gov.mil.otc._3dvis.data.oadms.element;

import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class PointsElement {

    public static final String ELEMENT_NAME = "points";

    public static List<Position> parse(Node node) {
        List<Position> positions = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(PointElement.ELEMENT_NAME)) {
                Position position = PointElement.parse(child);
                if (position != null) {
                    positions.add(position);
                }
            }
        }
        return positions;
    }
}
