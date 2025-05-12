package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OperatingStateElement {

    protected static final String ELEMENT_NAME = "operatingState";
    private static final String CODE_ELEMENT = "code";
    private static final String SUBCODE_ELEMENT = "subcode";

    protected static OperatingStateElement parse(Node node) {
        NodeList nodeList = node.getChildNodes();
        String code = "";
        String subcode = "";
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(CODE_ELEMENT)) {
                code = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(SUBCODE_ELEMENT)) {
                subcode = ElementUtility.parseString(child);
            }
        }
        return new OperatingStateElement(code, subcode);
    }

    private final String code;
    private final String subcode;

    public OperatingStateElement(String code, String subcode) {
        this.code = code;
        this.subcode = subcode;
    }

    public String getCode() {
        return code;
    }

    public String getSubcode() {
        return subcode;
    }
}
