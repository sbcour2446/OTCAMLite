package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class AreasElement {

    public static final String ELEMENT_NAME = "areas";

    public static AreasElement parse(Node node) {
        List<AreaElement> areas = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(AreaElement.ELEMENT_NAME)) {
                AreaElement areaElement = AreaElement.parse(child);
                if (areaElement != null) {
                    areas.add(areaElement);
                }
            }
        }
        if (!areas.isEmpty()) {
            return new AreasElement(areas);
        }
        return null;
    }

    private final List<AreaElement> areaElementList;

    private AreasElement(List<AreaElement> areas) {
        this.areaElementList = areas;
    }

    public List<AreaElement> getAreaElementList() {
        return areaElementList;
    }
}
