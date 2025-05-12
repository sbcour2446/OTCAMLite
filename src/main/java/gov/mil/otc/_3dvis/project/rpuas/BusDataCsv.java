package gov.mil.otc._3dvis.project.rpuas;

import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BusDataCsv extends TspiCsvFile {

    private static final String DATE_FORMAT = "MMM d HH:mm:ss yyyy";
    private static final String TIMESTAMP_COLUMN = "timestamp";
    private static final String ALTITUDE_COLUMN = "altitudeAMSL";
    private static final String LATITUDE_COLUMN = "gps.lat";
    private static final String LONGITUDE_COLUMN = "gps.lon";

    private final List<ValuePairTimedData> valueMapList = new ArrayList<>();

    public BusDataCsv(File file) {
        super(file);
        addColumn(TIMESTAMP_COLUMN);
        addColumn(ALTITUDE_COLUMN);
        addColumn(LATITUDE_COLUMN);
        addColumn(LONGITUDE_COLUMN);
    }

    public List<ValuePairTimedData> getValueMapList() {
        return valueMapList;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            //Fri Jun 7 21:10:31 2024 GMT
            //Fri Sep 15 04:23:01 2023
            String timestampString = fields[getColumnIndex(TIMESTAMP_COLUMN)];
            String[] timestampStringFields = timestampString.split(" ");
            if (timestampStringFields.length < 5) {
                return;
            }
            timestampString = timestampStringFields[1] + " " + timestampStringFields[2] +
                    " " + timestampStringFields[3] + " " + timestampStringFields[4];
//            if (timestampString.length() < DATE_FORMAT.length() + 4) {
//                return;
//            }
//            timestampString = timestampString.substring(4, DATE_FORMAT.length() + 4);
            long timestamp = Utility.parseTime(timestampString, DATE_FORMAT);

            String latitudeString = fields[getColumnIndex(LATITUDE_COLUMN)];
            if (latitudeString.contains("--")) {
                return;
            }
            double latitude = Double.parseDouble(latitudeString);
            double longitude = Double.parseDouble(fields[getColumnIndex(LONGITUDE_COLUMN)]);
            double altitudeFeet = Double.parseDouble(fields[getColumnIndex(ALTITUDE_COLUMN)]);
            double altitude = altitudeFeet * 0.3048;
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude)));

            Map<String, String> otherFields = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : getOtherFieldsIndexMap().entrySet()) {
                otherFields.put(entry.getKey(), fields[entry.getValue()]);
            }
            valueMapList.add(new ValuePairTimedData(timestamp, otherFields));
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.INFO, message, e);
        }
    }
}
