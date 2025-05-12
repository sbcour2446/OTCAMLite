package gov.mil.otc._3dvis.data.oadms.element;

import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class ScanZonesElement {

    public static final String ELEMENT_NAME = "scanZones";

    public static ScanZonesElement parse(Node node) {
        List<ScanZoneElement> scanZoneElements = new ArrayList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(ScanZoneElement.ELEMENT_NAME)) {
                ScanZoneElement scanZoneElement = ScanZoneElement.parse(child);
                if (scanZoneElement != null) {
                    scanZoneElements.add(scanZoneElement);
                }
            }
        }
        return new ScanZonesElement(scanZoneElements);
    }

    private final List<ScanZoneElement> scanZoneElements;

    private ScanZonesElement(List<ScanZoneElement> scanZoneElements) {
        this.scanZoneElements = scanZoneElements;
    }

    public List<ScanZoneElement> getScanZoneElements() {
        return scanZoneElements;
    }
}

