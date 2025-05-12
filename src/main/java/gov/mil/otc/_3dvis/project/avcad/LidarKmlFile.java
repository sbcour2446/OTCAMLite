package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LidarKmlFile extends File {

    private String name = "";
    private Position lidarPosition = null;
    private final Map<String, String> styleMap = new LinkedHashMap<>();
    private final List<String> styleIndexList = new ArrayList<>();
    private final List<ScanData> scanDataList = new ArrayList<>();
    private TimeSpan timeSpan = new TimeSpan(Long.MAX_VALUE, Long.MIN_VALUE);

    public LidarKmlFile(File file) {
        super(file.getAbsolutePath());
    }

    public String getSystemName() {
        return name;
    }

    public TimeSpan getTimeSpan() {
        return timeSpan;
    }

    public Position getLidarPosition() {
        return lidarPosition;
    }

    public List<ScanData> getScanDataList() {
        return scanDataList;
    }

    public boolean process() {
        XMLEventReader reader = null;
        try {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, true);
            reader = xmlInputFactory.createXMLEventReader(new FileInputStream(this));

            processLidarInfo(reader);

            while (reader.hasNext()) {
                XMLEvent nextEvent = reader.nextEvent();
                if (isStartElement(nextEvent, "Style")) {
                    readStyle(reader, nextEvent);
                } else if (isStartElement(nextEvent, "Folder")) {
                    ScanData scan = getScanData(reader);
                    if (scan != null) {
                        scanDataList.add(scan);
                    }
                }
            }

            reader.close();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "LidarKmlFile:process", e);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "LidarKmlFile:process", e);
                }
            }
        }
        return true;
    }

    /*
        <Point>
         <name>WDL</name>
         <coordinates>
          -113.049160,0040.069170,0
         </coordinates>
        </Point>
         */
    private void processLidarInfo(XMLEventReader reader) throws Exception {
        findNext(reader, "Point");
        findNext(reader, "name");
        name = getNextAsString(reader);
        findNext(reader, "coordinates");
        List<Position> positions = getCoordinates(reader, 0);
        if (!positions.isEmpty()) {
            lidarPosition = positions.getFirst();
        }
    }

    private void readStyle(XMLEventReader reader, XMLEvent event) throws Exception {
        String id = event.asStartElement().getAttributeByName(new QName("id")).getValue();
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (isEndElement(nextEvent, "Style")) {
                return;
            } else if (isStartElement(nextEvent, "color")) {
                //aabbggrr
                String color = getNextAsString(reader);
                styleMap.put(id, color);
                styleIndexList.add(id);
            }
        }
    }

    private ScanData getScanData(XMLEventReader reader) throws Exception {
        String name = "";
        TimeSpan timeSpan = new TimeSpan(0, 0);//getTimeSpan(reader);
        List<Placemark> placemarks = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (isEndElement(nextEvent, "Folder")) {
                return new ScanData(name, timeSpan, placemarks);
            } else if (isStartElement(nextEvent, "name")) {
                name = getNextAsString(reader);
            } else if (isStartElement(nextEvent, "TimeSpan")) {
                timeSpan = getTimeSpan(reader);
                udpateTimeSpan(timeSpan);
            } else if (isStartElement(nextEvent, "Placemark")) {
                Placemark placemark = getPlacemark(reader);
                if (placemark != null) {
                    placemarks.add(placemark);
                }
            }
        }

        return null;
    }

    private void udpateTimeSpan(TimeSpan timeSpan) {
        if (this.timeSpan.startTime > timeSpan.startTime) {
            this.timeSpan = new TimeSpan(timeSpan.startTime, this.timeSpan.stopTime);
        }
        if (this.timeSpan.stopTime < timeSpan.stopTime) {
            this.timeSpan = new TimeSpan(this.timeSpan.startTime, timeSpan.stopTime);
        }
    }

    private Placemark getPlacemark(XMLEventReader reader) throws Exception {
        String color = "";
        double heightOffset = 0.0;
        List<Position> outerBoundary = new ArrayList<>();
        List<Position> innerBoundary = new ArrayList<>();

        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (isEndElement(nextEvent, "Placemark")) {
                return new Placemark(color, heightOffset, outerBoundary, innerBoundary);
            } else if (isStartElement(nextEvent, "styleUrl")) {
                String style = getNextAsString(reader);
                if (style.length() > 1) {
                    style = style.substring(1);
                }
                color = styleMap.get(style);
                heightOffset = (styleIndexList.indexOf(style) / (double) styleIndexList.size());
                ;
            } else if (isStartElement(nextEvent, "outerBoundaryIs")) {
                while (reader.hasNext()) {
                    nextEvent = reader.nextEvent();
                    if (isEndElement(nextEvent, "outerBoundaryIs")) {
                        break;
                    } else if (isStartElement(nextEvent, "coordinates")) {
                        outerBoundary = getCoordinates(reader, heightOffset);
                    }
                }
            } else if (isStartElement(nextEvent, "innerBoundaryIs")) {
                while (reader.hasNext()) {
                    nextEvent = reader.nextEvent();
                    if (isEndElement(nextEvent, "innerBoundaryIs")) {
                        break;
                    } else if (isStartElement(nextEvent, "coordinates")) {
                        innerBoundary = getCoordinates(reader, heightOffset);
                    }
                }
            }
        }

        return null;
    }

    /*
        <TimeSpan>
         <begin>2022-08-31T03:44:35Z</begin>
         <end>2022-08-31T03:44:56Z</end>
        </TimeSpan>
     */
    private TimeSpan getTimeSpan(XMLEventReader reader) throws Exception {
        long startTime = 0;
        long stopTime = 0;
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (isEndElement(nextEvent, "TimeSpan")) {
                return new TimeSpan(startTime, stopTime);
            } else if (isStartElement(nextEvent, "begin")) {
                startTime = getNextAsTimestamp(reader);
            } else if (isStartElement(nextEvent, "end")) {
                stopTime = getNextAsTimestamp(reader);
            }
        }
        return new TimeSpan(startTime, stopTime);
    }

    /*
        <begin>2022-08-31T03:44:35Z</begin>
     */
    private long getNextAsTimestamp(XMLEventReader reader) throws Exception {
        if (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isCharacters()) {
                String timeString = nextEvent.asCharacters().getData();
                timeString = timeString.replace('T', ' ');
                timeString = timeString.substring(0, timeString.length() - 1);
                return Utility.parseTime(timeString, "yyyy-MM-dd HH:mm:ss");
            }
        }
        return 0;
    }

    private void findNext(XMLEventReader reader, String name) throws Exception {
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (isStartElement(nextEvent, name)) {
                return;
            }
        }
    }

    private boolean isStartElement(XMLEvent xmlEvent, String name) throws Exception {
        return xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase(name);
    }

    private boolean isEndElement(XMLEvent xmlEvent, String name) throws Exception {
        return xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equalsIgnoreCase(name);
    }

    private String getNextAsString(XMLEventReader reader) throws Exception {
        if (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (nextEvent.isCharacters()) {
                return nextEvent.asCharacters().getData();
            }
        }
        return "";
    }

    /*
     <coordinates>
      -113.049160,0040.069170,0
     </coordinates>
     */
    private List<Position> getCoordinates(XMLEventReader reader, double heightOffset) throws Exception {
        List<Position> positions = new ArrayList<>();
        while (reader.hasNext()) {
            XMLEvent nextEvent = reader.nextEvent();
            if (isEndElement(nextEvent, "coordinates")) {
                return positions;
            }
            if (nextEvent.isCharacters()) {
                String value = nextEvent.asCharacters().getData().trim();
                positions.addAll(parseCoordinates(value, heightOffset));
            }
        }
        return new ArrayList<>();
    }

    private List<Position> parseCoordinates(String value, double heightOffset) {
        List<Position> positions = new ArrayList<>();
        String[] coordinateLines = value.split("\n");
        for (String coordinateLine : coordinateLines) {
            coordinateLine = coordinateLine.trim();
            if (coordinateLine.isBlank()) {
                continue;
            }
            String[] coordinates = coordinateLine.trim().split(",");
            if (coordinates.length >= 2) {
                try {
                    double longitude = Double.parseDouble(coordinates[0]);
                    double latitude = Double.parseDouble(coordinates[1]);
                    double altitude = heightOffset;
                    if (coordinates.length > 2) {
                        altitude += Double.parseDouble(coordinates[2]);
                    }
                    positions.add(Position.fromDegrees(latitude, longitude, altitude));
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "LidarKmlFile::parseCoordinates:",
                            coordinates[0] + "," + coordinates[1] + "," + coordinates[2]);
                }
            }
        }
        return positions;
    }

    public final static class TimeSpan {
        private final long startTime;
        private final long stopTime;

        public TimeSpan(long startTime, long stopTime) {
            this.startTime = startTime;
            this.stopTime = stopTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getStopTime() {
            return stopTime;
        }
    }

    public static final class ScanData {
        private final String name;
        private final TimeSpan timeSpan;
        private final List<Placemark> placemarkList;

        public ScanData(String name, TimeSpan timeSpan, List<Placemark> placemarkList) {
            this.name = name;
            this.timeSpan = timeSpan;
            this.placemarkList = placemarkList;
        }

        public String getName() {
            return name;
        }

        public TimeSpan getTimeSpan() {
            return timeSpan;
        }

        public List<Placemark> getPlacemarkList() {
            return placemarkList;
        }
    }

    public static class Placemark {
        private final String style;
        private final double heightOffset;
        private final List<Position> outerBoundaryList;
        private final List<Position> innerBoundaryList;

        public Placemark(String style, double heightOffset, List<Position> outerBoundary, List<Position> innerBoundary) {
            this.style = style;
            this.heightOffset = heightOffset;
            this.outerBoundaryList = outerBoundary;
            this.innerBoundaryList = innerBoundary;
        }

        public String getStyle() {
            return style;
        }

        public double getHeightOffset() {
            return heightOffset;
        }

        public List<Position> getOuterBoundaryList() {
            return outerBoundaryList;
        }

        public List<Position> getInnerBoundaryList() {
            return innerBoundaryList;
        }
    }
}
