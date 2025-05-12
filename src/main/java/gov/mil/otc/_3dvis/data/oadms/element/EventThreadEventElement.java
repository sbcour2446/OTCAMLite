package gov.mil.otc._3dvis.data.oadms.element;

import gov.mil.otc._3dvis.data.oadms.CodeName;
import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EventThreadEventElement {

    public static final String ELEMENT_NAME = "eventThreadEvent";
    //eventThreadEvent
    private static final String ACTION_ELEMENT = "action";
    //eventThreadEvent
    private static final String AREAS_ELEMENT = "areas";
    //eventThreadEvent/areas
    private static final String AREA_ELEMENT = "area";
    //eventThreadEvent/areas/area
    private static final String GEOGRAPHIC_ARC_ELEMENT = "geographicArc";
    //eventThreadEvent/areas/area/geographicArc
    private static final String ARC_ANGLE_ELEMENT = "arcAngle";
    //eventThreadEvent/areas/area/geographicArc
    private static final String CENTER_LINE_ELEMENT = "centerline";
    //eventThreadEvent/areas/area/geographicArc
    private static final String RADIUS_ELEMENT = "radius";
    //eventThreadEvent
    private static final String EVENT_ID_ELEMENT = "eventID";
    //eventThreadEvent
    private static final String MISSION_ID_ELEMENT = "missionID";
    //eventThreadEvent
    private static final String REASON_ELEMENT = "reason";
    //eventThreadEvent
    private static final String REGION_TYPE_ELEMENT = "regionType";
    //eventThreadEvent
    private static final String SOURCE_TYPES_ELEMENT = "sourceTypes";
    //eventThreadEvent
    private static final String SOURCE_UCI_ELEMENT = "sourceUci";
    //eventThreadEvent
    private static final String TIMESTAMP_ELEMENT = "timestamp";

    public static EventThreadEventElement parse(Node node, long readingTime) {
        String action = "";
        AreasElement areasElement = null;
        DetailsElement detailsElement = null;
        Position position = null;
        String reason = "";
        String regionType = "";
        String sourceTypes = "";
        Long timestamp = readingTime;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(ACTION_ELEMENT)) {
                action = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(AreasElement.ELEMENT_NAME)) {
                areasElement = AreasElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(DetailsElement.ELEMENT_NAME)) {
                detailsElement = DetailsElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(PositionElement.ELEMENT_NAME)) {
                position = PositionElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(REASON_ELEMENT)) {
                reason = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(REGION_TYPE_ELEMENT)) {
                regionType = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(SOURCE_TYPES_ELEMENT)) {
                sourceTypes = CodeName.decode(ElementUtility.parseString(child));
            } else if (child.getNodeName().equalsIgnoreCase(TIMESTAMP_ELEMENT)) {
//ignore and use reading time                timestamp = ElementUtility.parseLong(child);
            }
        }
        if (timestamp != null && timestamp > 0) {
            return new EventThreadEventElement(action, areasElement, detailsElement, position, reason, regionType,
                    sourceTypes, timestamp);
        } else {
            return null;
        }
    }

    private static long parseTimestamp(Node node) {
        Node child = node.getFirstChild();
        if (child != null) {
            try {
                return Long.parseLong(node.getFirstChild().getNodeValue());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "MissionElement::parseTimestamp", e);
            }
        }
        return 0;
    }

    public enum EventAction {
        UNKNOWN,
        SCAN_UPDATE,
        SCAN_ENDED,
        SCAN_BEGAN,
        NEW_THREAD,
        ADD_ENTRY,
        UPDATE_THREAD
    }

    private final String action;
    private final AreasElement areasElement;
    private final DetailsElement detailsElement;
    private final Position position;
    private final String reason;
    private final String regionType;
    private final String sourceTypes;
    private final long timestamp;

    public EventThreadEventElement(String action, AreasElement areasElement, DetailsElement detailsElement,
                                   Position position, String reason, String regionType,
                                   String sourceTypes, long timestamp) {
        this.action = action;
        this.areasElement = areasElement;
        this.detailsElement = detailsElement;
        this.position = position;
        this.reason = reason;
        this.regionType = regionType;
        this.sourceTypes = sourceTypes;
        this.timestamp = timestamp;
    }

    public EventAction getAction() {
        try {
            return EventAction.valueOf(action);
        } catch (Exception e) {
            return EventAction.UNKNOWN;
        }
    }

    public AreasElement getAreasElement() {
        return areasElement;
    }

    public DetailsElement getDetailsElement() {
        return detailsElement;
    }

    public Position getPosition() {
        return position;
    }

    public String getReason() {
        return reason;
    }

    public String getRegionType() {
        return regionType;
    }

    public String getSourceTypes() {
        return sourceTypes;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
