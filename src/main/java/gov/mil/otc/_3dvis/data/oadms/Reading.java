package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

import java.util.HashMap;
import java.util.Map;

public class Reading extends TimedData {

    public static final String NODE_NAME = "Reading";
    public static final String READING_TIME = "ReadingTime";
    public static final String INDEX = "Index";
    private final int index;
    private final Map<String, String> values = new HashMap<>();

    public Reading(long readingTime, int index) {
        super(readingTime);
        this.index = index;
    }

    public void addValue(String name, String value) {
        values.put(name, value);
    }

    public String getValue(String name) {
        return values.get(name);
    }

    protected Map<String, String> getValues() {
        return values;
    }
}
