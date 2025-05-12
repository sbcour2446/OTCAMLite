package gov.mil.otc._3dvis.data.oadms.element;

import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class GeographicPolygonElement {

    public static final String ELEMENT_NAME = "geographicPolygon";

    public static GeographicPolygonElement parse(Node node) {
        List<Position> positions = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(PointsElement.ELEMENT_NAME)) {
                positions.addAll(PointsElement.parse(child));
            }
        }
        if (!positions.isEmpty()) {
            return new GeographicPolygonElement(positions);
        }
        return null;
    }

    private final List<Position> positionList;

    private GeographicPolygonElement(List<Position> positions) {
        this.positionList = positions;
    }

    public List<Position> getPositionList() {
        return positionList;
    }
}

