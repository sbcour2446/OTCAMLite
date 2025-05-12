package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IdentityElement {

    protected static final String ELEMENT_NAME = "identity";
    private static final String SYMBOL_ELEMENT = "symbol";

    protected static IdentityElement parse(Node node) {
        NodeList nodeList = node.getChildNodes();
        String symbol = "";
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equals(SYMBOL_ELEMENT)) {
                symbol = ElementUtility.parseString(child);
            }
        }
        return new IdentityElement(symbol);
    }

    private final String symbol;

    public IdentityElement(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
