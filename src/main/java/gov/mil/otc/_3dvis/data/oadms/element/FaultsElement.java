package gov.mil.otc._3dvis.data.oadms.element;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class FaultsElement {

    public static final String ELEMENT_NAME = "faults";

    public static final FaultsElement FAULTS_ELEMENT_NONE = new FaultsElement(new ArrayList<>());

    public static FaultsElement parse(Node node) {
        List<FaultElement> faultElements = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(FaultElement.ELEMENT_NAME)) {
                faultElements.add(FaultElement.parse(child));
            }
        }
        return new FaultsElement(faultElements);
    }

    public final List<FaultElement> faultElementList;

    private FaultsElement(List<FaultElement> faultElements) {
        faultElementList = faultElements;
    }

    public List<FaultElement> getFaultElementList() {
        return faultElementList;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (FaultElement faultElement : faultElementList) {
            stringBuilder.append(prefix);
            stringBuilder.append(faultElement.getName());
            prefix = "; ";
        }
        return stringBuilder.toString();
    }
}
