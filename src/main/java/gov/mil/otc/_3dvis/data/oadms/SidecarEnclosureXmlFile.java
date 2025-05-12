package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.data.oadms.element.ElementUtility;
import gov.mil.otc._3dvis.data.oadms.element.ReadingElement;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SidecarEnclosureXmlFile extends OadmsXmlFile {

    private static final String OPERATIONAL_STATUS = "operationalStatus";
    private static final String STATUS_DESCRIPTION = "statusDescription";
    private static final String DOOR_OPEN = "doorOpen";
    private static final String ELEVATION = "elevation";
    private static final String ELEVATION_ACCURACY = "elevationAccuracy";
    private static final String GPS_SERIAL_NUM = "gpsSerialNum";
    private static final String INTERNAL_PERCENT_RELATIVE_HUMIDITY = "internalPercentRelativeHumidity";
    private static final String INTERNAL_TEMP = "internalTemp";
    private static final String LATITUDE = "latitude";
    private static final String LAT_LONG_ACCURACY = "latLongAccuracy";
    private static final String LONGITUDE = "longitude";
    private static final String SATELLITE_COUNT = "satelliteCount";
    private static final String UPS_BACKUP_ON = "upsBackupOn";
    private static final String UPS_CHARGE_PERCENTAGE = "upsChargePercentage";

    private final List<TspiData> tspiDataList = new ArrayList<>();

    public SidecarEnclosureXmlFile(File file) {
        super(file);
    }

    public List<TspiData> getTspiData() {
        return new ArrayList<>(tspiDataList);
    }

    @Override
    protected void parseReadingElement(Node node) {
        ReadingElement readingElement = ReadingElement.parse(node);
        if (readingElement == null || readingElement.getReadingTime() == 0) {
            return;
        }

        if (readingElement.getReadingTime() > stopTime) {
            stopTime = readingElement.getReadingTime();
        }

        String operationalStatus = "";
        Double latitude = null;
        Double longitude = null;
        Double elevation = null;
        Map<String, String> otherValues = new HashMap<>();

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(OPERATIONAL_STATUS)) {
                operationalStatus = child.getNodeValue();
            } else if (child.getNodeName().equalsIgnoreCase(LATITUDE)) {
                latitude = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(LONGITUDE)) {
                longitude = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(ELEVATION)) {
                elevation = ElementUtility.parseDouble(child);
            } else {
                otherValues.put(child.getNodeName(), child.getNodeValue());
            }
        }

        if (latitude != null && longitude != null && elevation != null) {
            tspiDataList.add(new TspiData(readingElement.getReadingTime(), Position.fromDegrees(latitude, longitude, elevation)));
        }
    }
}
