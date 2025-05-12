package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OrientationElement {

    protected static final String ELEMENT_NAME = "orientation";
    private static final String PITCH_ELEMENT = "pitch";
    private static final String ROLL_ELEMENT = "roll";
    private static final String YAW_ELEMENT = "yaw";

    protected static OrientationElement parse(Node node) {
        NodeList nodeList = node.getChildNodes();
        Double pitch = null;
        Double roll = null;
        Double yaw = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(PITCH_ELEMENT)) {
                pitch = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(ROLL_ELEMENT)) {
                roll = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(YAW_ELEMENT)) {
                yaw = ElementUtility.parseDouble(child);
            }
        }
        return new OrientationElement(pitch, roll, yaw);
    }

    private final double pitch;
    private final double roll;
    private final double yaw;

    public OrientationElement(Double pitch, Double roll, Double yaw) {
        if (pitch == null) {
            pitch = 0.0;
        }
        if (roll == null) {
            roll = 0.0;
        }
        if (yaw == null) {
            yaw = 0.0;
        }
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public double getRoll() {
        return roll;
    }

    public double getYaw() {
        return yaw;
    }
}
