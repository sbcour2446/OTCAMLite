package gov.mil.otc._3dvis.data.oadms;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SensorXml {

    private final File file;

    public SensorXml(File file) {
        this.file = file;
    }

    public Sensor read() {
        Sensor sensor = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();

            Element element = doc.getDocumentElement();
            String type = element.getAttribute(Sensor.TYPE);
            String trialStartTime = element.getAttribute(Sensor.TRIAL_START_TIME);
            String sid = element.getAttribute(Sensor.SID);
            String sensorName = element.getAttribute(Sensor.SENSOR_NAME);
            String serialNumber = element.getAttribute(Sensor.SERIAL_NUMBER);
            String bladeSerialNumber = element.getAttribute(Sensor.BLADE_SERIAL_NUMBER);
            sensor = new Sensor(type, trialStartTime, sid, sensorName, serialNumber, bladeSerialNumber);

            NodeList nodeList = doc.getElementsByTagName(Reading.NODE_NAME);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    NamedNodeMap namedNodeMap = node.getAttributes();
                    long timestamp = getTimestamp(namedNodeMap);
                    if (timestamp == 0) {
                        continue;
                    }

                    int index = getIndex(namedNodeMap);

                    Reading reading = new Reading(timestamp, index);
                    sensor.addReading(reading);

                    NodeList readingNodeList = node.getChildNodes();
                    for (int j = 0; j < readingNodeList.getLength(); j++) {
                        Node readingNode = readingNodeList.item(j);

                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String nodeName = readingNode.getNodeName();
                            Node childNode = readingNode.getFirstChild();
                            String nodeValue = null;
                            if (childNode != null) {
                                nodeValue = childNode.getNodeValue();
                            }
                            reading.addValue(nodeName, nodeValue);
                            System.out.println(nodeName + ":" + nodeValue);
                        }
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        return sensor;
    }

    private static long getTimestamp(NamedNodeMap namedNodeMap) {
        Node node = namedNodeMap.getNamedItem(Reading.READING_TIME);
        String readingTimeString = node.getNodeValue();
        if (readingTimeString == null) {
            return 0;
        }

        try {
            LocalDateTime localDateTime = LocalDateTime.parse(readingTimeString,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
            return instant.toEpochMilli();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "oadms.Sensor:getTimestamp", e);
            return 0;
        }
    }

    private static int getIndex(NamedNodeMap namedNodeMap) {
        Node node = namedNodeMap.getNamedItem(Reading.INDEX);
        String indexString = node.getNodeValue();
        if (indexString == null) {
            return 0;
        }

        try {
            return Integer.parseInt(indexString);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "oadms.Sensor:getIndex", e);
            return 0;
        }
    }
}
