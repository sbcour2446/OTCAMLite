package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FaultElement {

    protected static final String ELEMENT_NAME = "faults";
    private static final String ACTIVE_ELEMENT = "active";
    private static final String COUNT_ELEMENT = "count";
    private static final String DETAILS_ELEMENT = "details";
    private static final String LEVEL_ELEMENT = "level";
    private static final String NAME_ELEMENT = "name";
    private static final String TIME_ELEMENT = "time";
    private static final String VIEWED_ELEMENT = "viewed";

    protected static FaultElement parse(Node node) {
        NodeList nodeList = node.getChildNodes();
        boolean active = false;
        int count = 0;
        String details = "";
        String level = "";
        String name = "";
        double time = 0.0;
        int viewed = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(ACTIVE_ELEMENT)) {
                active = ElementUtility.parseBoolean(child);
            } else if (child.getNodeName().equalsIgnoreCase(COUNT_ELEMENT)) {
                count = ElementUtility.parseInteger(child, count);
            } else if (child.getNodeName().equalsIgnoreCase(DETAILS_ELEMENT)) {
                details = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(LEVEL_ELEMENT)) {
                level = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(NAME_ELEMENT)) {
                name = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(TIME_ELEMENT)) {
                time = ElementUtility.parseDouble(child, time);
            } else if (child.getNodeName().equalsIgnoreCase(VIEWED_ELEMENT)) {
                viewed = ElementUtility.parseInteger(child, viewed);
            }
        }
        return new FaultElement(active, count, details, level, name, time, viewed);
    }

    private final boolean active;
    private final int count;
    private final String details;
    private final String level;
    private final String name;
    private final double time;
    private final int viewed;

    private FaultElement(boolean active, int count, String details, String level, String name, double time, int viewed) {
        this.active = active;
        this.count = count;
        this.details = details;
        this.level = level;
        this.name = name;
        this.time = time;
        this.viewed = viewed;
    }

    public boolean getActive() {
        return active;
    }

    public int getCount() {
        return count;
    }

    public String getDetails() {
        return details;
    }

    public String getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public double getTime() {
        return time;
    }

    public int getViewed() {
        return viewed;
    }
}
