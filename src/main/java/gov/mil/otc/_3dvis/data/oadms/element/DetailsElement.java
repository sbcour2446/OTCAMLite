package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class DetailsElement {

    public static final String ELEMENT_NAME = "details";

    public static DetailsElement parse(Node node) {
        List<DetailElement> details = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(DetailElement.ELEMENT_NAME)) {
                DetailElement detailElement = DetailElement.parse(child);
                if (detailElement != null) {
                    details.add(detailElement);
                }
            }
        }
        if (!details.isEmpty()) {
            return new DetailsElement(details);
        }
        return null;
    }

    private final List<DetailElement> detailElementList;

    private DetailsElement(List<DetailElement> details) {
        this.detailElementList = details;
    }

    public List<DetailElement> getDetailElementList() {
        return detailElementList;
    }

    public static class DetailElement {
        private static final String ELEMENT_NAME = "detail";
        private static final String NAME_ELEMENT = "name";
        private static final String VALUE_ELEMENT = "value";

        private static DetailElement parse(Node node) {
            NodeList nodeList = node.getChildNodes();
            String name = "";
            String value = "";
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                if (child == null) {
                    continue;
                }
                if (child.getNodeName().equalsIgnoreCase(NAME_ELEMENT)) {
                    name = ElementUtility.parseString(child);
                } else if (child.getNodeName().equalsIgnoreCase(VALUE_ELEMENT)) {
                    value = ElementUtility.parseString(child);
                }
            }
            if (!name.isBlank()) {
                return new DetailElement(name, value);
            } else {
                return null;
            }
        }

        private final String name;
        private final String value;

        private DetailElement(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
