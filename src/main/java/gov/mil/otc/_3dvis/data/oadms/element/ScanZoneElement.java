package gov.mil.otc._3dvis.data.oadms.element;

import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ScanZoneElement {

    public static final String ELEMENT_NAME = "scanZone";
    private static final String ARC_ANGLE = "arcAngle";
    private static final String CENTERLINE = "centerline";
    private static final String RADIUS = "radius";

    public static ScanZoneElement parse(Node node) {
        Double arcAngle = null;
        Double centerline = null;
        Position position = null;
        Double radius = null;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(ARC_ANGLE)) {
                arcAngle = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(CENTERLINE)) {
                centerline = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(PositionElement.ELEMENT_NAME)) {
                position = PositionElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(RADIUS)) {
                radius = ElementUtility.parseDouble(child);
            }
        }
        if (arcAngle != null && centerline != null && position != null && radius != null) {
            return new ScanZoneElement(arcAngle, centerline, position, radius);
        }
        return null;
    }

    private final double arcAngle;
    private final double centerline;
    private final Position position;
    private final double radius;

    private ScanZoneElement(double arcAngle, double centerline, Position position, double radius) {
        this.arcAngle = arcAngle;
        this.centerline = centerline;
        this.position = position;
        this.radius = radius;
    }

    public double getArcAngle() {
        return arcAngle;
    }

    public double getCenterline() {
        return centerline;
    }

    public Position getPosition() {
        return position;
    }

    public double getRadius() {
        return radius;
    }
}

