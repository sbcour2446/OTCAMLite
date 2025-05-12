package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.data.oadms.element.ElementUtility;
import gov.mil.otc._3dvis.data.oadms.element.ReadingElement;
import gov.mil.otc._3dvis.utility.Utility;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WdlXmlFile extends OadmsXmlFile {

    private static final String OPERATIONAL_STATUS = "operationalStatus";
    private static final String STATUS_DESCRIPTION = "statusDescription";
    private static final String AD_MAX_VOLTAGE = "adMaxVoltage";
    private static final String APD_VOLTAGE = "apdVoltage";
    private static final String AZIMUTH_ANGLE_IN_DEGREES = "azimuthAngleInDegrees";
    private static final String ELEVATION_ANGLE_IN_DEGREES = "elevationAngleInDegrees";
    private static final String FILTER_OPTICAL_DENSITY = "filterOpticalDensity";
    private static final String FREQUENCY_IN_HZ = "frequencyInHz";
    private static final String LASER_POWER_IN_MILLI_JOULES = "laserPowerInMilliJoules";
    private static final String RANGE_OFFSET_IN_METERS = "rangeOffsetInMeters";
    private static final String READING_EPOCH_TIME = "readingEpochTime";
    private static final String READING_MICRO_SECONDS_PAST_EPOCH = "readingMicrosecondsPastEpoch";
    private static final String RESOLUTION_IN_METERS = "resolutionInMeters";
    private static final String SCAN_MODE = "scanMode";
    private static final String TRACE_COUNTS = "traceCounts";
    private static final String TRACE_ID = "traceID";
    private final List<WdlReading> wdlReadingList = new ArrayList<>();
    private WdlReading wdlReading = null;
    private int sameAzimuthCount = 0;

    public WdlXmlFile(File file) {
        super(file);
    }

    public List<WdlReading> getWdlReadingList() {
        return new ArrayList<>(wdlReadingList);
    }

    @Override
    protected void parseReadingElement(XMLEventReader reader, XMLEvent xmlEvent) throws Exception {
        String readingTime = xmlEvent.asStartElement().getAttributeByName(QName.valueOf(ReadingElement.READING_TIME)).getValue();
        long timestamp = Utility.parseTime(readingTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

        String operationalStatus = "";
        Double azimuth = null;
        Double elevation = null;
        Double resolution = null;
        String traceCountString = "";
        Integer traceId = null;

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (isStartElement(nextEvent, OPERATIONAL_STATUS)) {
                operationalStatus = parseNextAsString(reader);
            } else if (isStartElement(nextEvent, AZIMUTH_ANGLE_IN_DEGREES)) {
                azimuth = parseNextAsDouble(reader);
            } else if (isStartElement(nextEvent, ELEVATION_ANGLE_IN_DEGREES)) {
                elevation = parseNextAsDouble(reader);
            } else if (isStartElement(nextEvent, RESOLUTION_IN_METERS)) {
                resolution = parseNextAsDouble(reader);
            } else if (isStartElement(nextEvent, TRACE_COUNTS)) {
                traceCountString = parseNextAsString(reader);
            } else if (isStartElement(nextEvent, TRACE_ID)) {
                traceId = parseNextAsInteger(reader);
            } else if (nextEvent.isEndElement() && nextEvent.asEndElement().getName().getLocalPart().equalsIgnoreCase(ReadingElement.ELEMENT_NAME)) {
                break;
            }
        }

        if (azimuth != null && elevation != null && resolution != null && traceId != null) {
            if (wdlReading != null && wdlReading.getAzimuth() == azimuth) {
                wdlReading.averageRepeatTrace(traceCountString);
            } else {
                if (wdlReading != null) {
                    wdlReadingList.add(wdlReading);
                }
                wdlReading = new WdlReading(timestamp, operationalStatus, azimuth, elevation, resolution, traceId,
                        traceCountString, null);
            }
        }
    }

    protected boolean isStartElement(XMLEvent xmlEvent, String name) {
        return xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase(name);
    }

    protected String parseNextAsString(XMLEventReader reader) throws Exception {
        if (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isCharacters()) {
                return nextEvent.asCharacters().getData();
            }
        }
        return "";
    }

    protected Double parseNextAsDouble(XMLEventReader reader) throws Exception {
        if (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isCharacters()) {
                try {
                    return Double.parseDouble(nextEvent.asCharacters().getData());
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "WdlXmlFile::parseNextEventAsDouble", e);
                }
            }
        }
        return null;
    }

    protected Integer parseNextAsInteger(XMLEventReader reader) throws Exception {
        if (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isCharacters()) {
                try {
                    return Integer.parseInt(nextEvent.asCharacters().getData());
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "WdlXmlFile::parseNextEventAsInteger", e);
                }
            }
        }
        return null;
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
        Double azimuth = null;
        Double elevation = null;
        Integer resolution = null;
        String traceCountString = "";
        List<Integer> traceCountList = null;
        Integer traceId = null;

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(OPERATIONAL_STATUS)) {
                operationalStatus = child.getNodeValue();
            } else if (child.getNodeName().equalsIgnoreCase(AZIMUTH_ANGLE_IN_DEGREES)) {
                azimuth = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(ELEVATION_ANGLE_IN_DEGREES)) {
                elevation = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(RESOLUTION_IN_METERS)) {
                resolution = ElementUtility.parseInteger(child);
            } else if (child.getNodeName().equalsIgnoreCase(TRACE_COUNTS)) {
                traceCountList = parseTraceCounts(child);
            } else if (child.getNodeName().equalsIgnoreCase(TRACE_ID)) {
                traceId = ElementUtility.parseInteger(child);
            }
        }

        if (azimuth != null && elevation != null && resolution != null && traceId != null && traceCountList != null) {
            wdlReadingList.add(new WdlReading(readingElement.getReadingTime(), operationalStatus, azimuth,
                    elevation, resolution, traceId, traceCountString, null));
        }
    }

    private List<Integer> parseTraceCounts(Node node) {
        List<Integer> traceCounts = new ArrayList<>();
        Node child = node.getFirstChild();
        if (child != null && node.getNodeValue() != null) {
            String[] values = node.getNodeValue().split(",");
            for (String value : values) {
                try {
                    traceCounts.add(Integer.parseInt(value));
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "WdlXmlFile::parseTraceCounts", e);
                }
            }
        }
        return traceCounts;
    }
}
