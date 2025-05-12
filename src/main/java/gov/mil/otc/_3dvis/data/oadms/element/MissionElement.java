package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MissionElement {

    public static final String ELEMENT_NAME = "mission";
    //mission
    private static final String OBSERVABLES_ELEMENT = "observables";

    public static EventThreadEventElement parse(Node node, long readingTime) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(OBSERVABLES_ELEMENT)) {
                return parseObservables(child, readingTime);
            }
        }
        return null;
    }

    private static EventThreadEventElement parseObservables(Node node, long readingTime) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(EventThreadEventElement.ELEMENT_NAME)) {
                return EventThreadEventElement.parse(child, readingTime);
            }
        }
        return null;
    }
}
