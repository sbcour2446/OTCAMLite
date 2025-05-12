package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CcmLogFile extends CsvFile {

    //Date,Aircraft,Threat,Mode,"Type (ACQ, TT, MG, TI)",Start Time,Stop Time,GPS Coordinates,Comments
    //2024302,AH-64,J29,05B2,TT,17:40:29.000,17:41:45.000,"35.48693, -117.27784, El: 749.37",Lead
    private static final String TIME_FORMAT = "yyyyDDD HH:mm:ss.SSS";
    private static final String DATE_COLUMN = "date";
    private static final String TYPE_COLUMN = "\"Type (ACQ, TT, MG, TI)\"";
    private static final String START_TIME_COLUMN = "start time";
    private static final String STOP_TIME_COLUMN = "stop time";
    private static final String COMMENTS_COLUMN = "comments";
    private final List<ValuePairTimedData> valuePairList = new ArrayList<>();

    public CcmLogFile(File file) {
        super(file);

        addColumn(DATE_COLUMN);
        addColumn(TYPE_COLUMN);
        addColumn(START_TIME_COLUMN);
        addColumn(STOP_TIME_COLUMN);
        addColumn(COMMENTS_COLUMN, true);
    }

    public List<ValuePairTimedData> getValuePairList() {
        return valuePairList;
    }
//
//    @Override
//    protected boolean processHeader(BufferedReader bufferedReader) {
//        try {
//            String line = bufferedReader.readLine();
//            while (line != null && line.isBlank()) {
//                line = bufferedReader.readLine();
//            }
//            if (line != null) {
//                String[] header = getFields(line);
//                for (int i = 0; i < header.length; i++) {
//                    String headerValue = header[i].toLowerCase().trim();
//                    if (!headerValue.isBlank()) {
//                        addOtherColumn(headerValue, i);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            String message = String.format("ThreatDataFile::processHeader:Error reading header %s", file.getAbsolutePath());
//            Logger.getGlobal().log(Level.WARNING, message, e);
//        }
//
//        return true;
//    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String dateString = fields[getColumnIndex(DATE_COLUMN)];
            String startTimeString = dateString + " " + fields[getColumnIndex(START_TIME_COLUMN)];
            String stopTimeString = dateString + " " + fields[getColumnIndex(STOP_TIME_COLUMN)];

            if (startTimeString.length() != TIME_FORMAT.length() ||
                    stopTimeString.length() != TIME_FORMAT.length()) {
                return;
            }

            long startTime = Utility.parseTime(startTimeString, DateTimeFormatter.ofPattern(TIME_FORMAT));
            long stopTime = Utility.parseTime(stopTimeString, DateTimeFormatter.ofPattern(TIME_FORMAT));
            if (startTime <= 0 || stopTime <= 0) {
                return;
            }

            String type = fields[getColumnIndex(TYPE_COLUMN)];
            String comments = "";
            int commentsIndex = getColumnIndex(COMMENTS_COLUMN);
            if (commentsIndex >= 0) {
                comments = fields[commentsIndex];
            }
            Map<String, String> values = new HashMap<>();
            values.put("Type", type);
            values.put("Comments", comments);
            valuePairList.add(new ValuePairTimedData(startTime, values));

            values = new HashMap<>();
            values.put("Type", "");
            values.put("Comments", "");
            valuePairList.add(new ValuePairTimedData(stopTime, values));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "ThreatDataFile::processLine:", e);
        }
    }
}
