package gov.mil.otc._3dvis.data.oadms.element;

import gov.mil.otc._3dvis.utility.Utility;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadingElement {

    public static final String ELEMENT_NAME = "Reading";
    public static final String READING_TIME = "ReadingTime";
    public static final String INDEX = "Index";

    public static ReadingElement parse(Node node) {
        NamedNodeMap namedNodeMap = node.getAttributes();
        long readingTime = getTimestamp(namedNodeMap);
        int index = getIndex(namedNodeMap);
        if (readingTime > 0) {
            return new ReadingElement(readingTime, index);
        }
        return null;
    }

    private static long getTimestamp(NamedNodeMap namedNodeMap) {
        Node node = namedNodeMap.getNamedItem(READING_TIME);
        String readingTimeString = node.getNodeValue();
        if (readingTimeString == null) {
            return 0;
        }
        return Utility.parseTime(readingTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    private static int getIndex(NamedNodeMap namedNodeMap) {
        Node node = namedNodeMap.getNamedItem(INDEX);
        String indexString = node.getNodeValue();
        if (indexString == null) {
            return 0;
        }
        try {
            return Integer.parseInt(indexString);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "ReadingElement:getIndex", e);
            return 0;
        }
    }

    private final long readingTime;
    private final int index;

    private ReadingElement(long readingTime, int index) {
        this.readingTime = readingTime;
        this.index = index;
    }

    public long getReadingTime() {
        return readingTime;
    }

    public int getIndex() {
        return index;
    }
}
