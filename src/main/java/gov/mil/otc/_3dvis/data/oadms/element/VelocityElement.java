package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VelocityElement {

    protected static final String ELEMENT_NAME = "velocity";
    private static final String HEADING_ELEMENT = "heading";
    private static final String PITCH_ELEMENT = "pitch";
    private static final String SPEED_ELEMENT = "speed";

    protected static VelocityElement parse(Node node) {
        NodeList nodeList = node.getChildNodes();
        Double heading = null;
        Double pitch = null;
        Double speed = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(HEADING_ELEMENT)) {
                heading = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(PITCH_ELEMENT)) {
                pitch = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(SPEED_ELEMENT)) {
                speed = ElementUtility.parseDouble(child);
            }
        }
        return new VelocityElement(heading, pitch, speed);
    }

    private final double heading;
    private final double pitch;
    private final double speed;

    public VelocityElement(Double heading, Double pitch, Double speed) {
        if (heading == null) {
            heading = 0.0;
        }
        if (pitch == null) {
            pitch = 0.0;
        }
        if (speed == null) {
            speed = 0.0;
        }
        this.heading = heading;
        this.pitch = pitch;
        this.speed = speed;
    }

    public double getHeading() {
        return heading;
    }

    public double getPitch() {
        return pitch;
    }

    public double getSpeed() {
        return speed;
    }
}
