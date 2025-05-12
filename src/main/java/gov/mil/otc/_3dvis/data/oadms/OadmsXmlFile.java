package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.data.oadms.element.ReadingElement;
import gov.mil.otc._3dvis.utility.Utility;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OadmsXmlFile {

    private static final String ROOT = "DataLog";
    private static final String TYPE = "Type";
    private static final String TRIAL_START_TIME = "TrialStartTime";
    private static final String SID = "Sid";
    private static final String SENSOR_NAME = "SensorName";
    private static final String SERIAL_NUMBER = "SerialNumber";
    private static final String BLADE_SERIAL_NUMBER = "BladeSerialNumber";

    protected final File file;
    protected String type;
    protected String trialStartTime;
    protected String sid;
    protected String sensorName;
    protected String serialNumber;
    protected String bladeSerialNumber;
    protected long startTime = 0;
    protected long stopTime = 0;

    public OadmsXmlFile(File file) {
        this.file = file;
    }

    public boolean scan() {
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(file));
            boolean foundStart = false;
            XMLEvent lastReadingEvent = null;
            while (xmlEventReader.hasNext()) {
                XMLEvent nextEvent = xmlEventReader.nextEvent();
                if (!foundStart && nextEvent.isStartElement()) {
                    if (nextEvent.asStartElement().getName().getLocalPart().contains(ROOT)) {
                        String trialStartTime = nextEvent.asStartElement()
                                .getAttributeByName(QName.valueOf(TRIAL_START_TIME)).getValue();
                        startTime = Utility.parseTime(trialStartTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                        foundStart = true;
                    }
                } else if (nextEvent.isStartElement() && nextEvent.asStartElement().getName().getLocalPart().contains(ReadingElement.ELEMENT_NAME)) {
                    lastReadingEvent = nextEvent;
                } else if (!xmlEventReader.hasNext() && lastReadingEvent != null) {
                    String readingTime = lastReadingEvent.asStartElement()
                            .getAttributeByName(QName.valueOf(Reading.READING_TIME)).getValue();
                    stopTime = Utility.parseTime(readingTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                }
            }
            xmlEventReader.close();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "RocketSledXmlReader:scan", e);
            return false;
        }
        return true;
    }

    public boolean process() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            document.getDocumentElement().normalize();
            Element element = document.getDocumentElement();
            type = element.getAttribute(TYPE);
            trialStartTime = element.getAttribute(TRIAL_START_TIME);
            sid = element.getAttribute(SID);
            sensorName = element.getAttribute(SENSOR_NAME);
            serialNumber = element.getAttribute(SERIAL_NUMBER);
            bladeSerialNumber = element.getAttribute(BLADE_SERIAL_NUMBER);

            startTime = Utility.parseTime(trialStartTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

            NodeList nodeList = document.getElementsByTagName(ReadingElement.ELEMENT_NAME);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                if (child == null) {
                    continue;
                }
                parseReadingElement(child);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "RocketSledXmlReader:process", e);
            return false;
        }
        return true;
    }

    public boolean processFast() {
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new FileInputStream(file));
            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();
                if (nextEvent.isStartElement() && nextEvent.asStartElement().getName().getLocalPart().contains(ReadingElement.ELEMENT_NAME)) {
                    parseReadingElement(reader, nextEvent);
                }
            }
            reader.close();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "RocketSledXmlReader:scan", e);
            return false;
        }
        return true;
    }

    public File getFile() {
        return file;
    }

    public String getType() {
        return type;
    }

    public String getTrialStartTime() {
        return trialStartTime;
    }

    public String getSid() {
        return sid;
    }

    public String getSensorName() {
        return sensorName;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getBladeSerialNumber() {
        return bladeSerialNumber;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    protected void parseReadingElement(Node node) {
        Logger.getGlobal().log(Level.WARNING, "not implemented:" + node.getNodeName());
    }

    protected void parseReadingElement(XMLEventReader reader, XMLEvent xmlEvent) throws Exception {
        Logger.getGlobal().log(Level.WARNING, "parseReadingElement not implemented:");
    }
}
