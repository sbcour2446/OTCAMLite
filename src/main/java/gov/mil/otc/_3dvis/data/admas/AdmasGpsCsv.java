package gov.mil.otc._3dvis.data.admas;

import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdmasGpsCsv extends TspiCsvFile {
    //UTC Time,Time Offset (s),"TimeStamp for Ublox_GPS (TenthsOfMilliSeconds)","UTC (GPS) (date/time string)","Latitude (GPS) (deg)","Longitude (GPS) (deg)","WGS84 Altitude (GPS) (m)","UTM Easting (GPS) (m)","UTM Northing (GPS) (m)","UTM Zone - Longitude (GPS) (code)","UTM Zone - Latitude (GPS) (code)","Course Over Ground (GPS) (deg)","Speed Over Ground (GPS) (km/h)","Dead Reckoned (GPS) (code)","Number of Satellites (GPS) (counts)","Horizontal Dilution of Precision (GPS) (m)","Age of Correction (GPS) (sec)","Diff Status of Position (GPS) (unknown)"
    //2023:05:15:23:28:44.200

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss.SSSSSSSSS";
    private static final String TIMESTAMP_COLUMN = "UTC Time";
//
//    private static final String DATE_FORMAT = "yyyy:MM:dd:HH:mm:ss.SSS";
////    private static final String DATE_FORMAT = "yyyy:MM:dd:HH:mm:ss.SSSSSSSSS ZZZ";
//    private static final String TIMESTAMP_COLUMN = "\"UTC (GPS) (date/time string)\"";
    private static final String LATITUDE_COLUMN = "\"Latitude (GPS) (deg)\"";
    private static final String LONGITUDE_COLUMN = "\"Longitude (GPS) (deg)\"";
    private static final String ALTITUDE_COLUMN = "\"WGS84 Altitude (GPS) (m)\"";
    private boolean filter1Hz = false;
    private long currentSecond = 0;

    public AdmasGpsCsv(File file) {
        super(file);

        addColumn(TIMESTAMP_COLUMN);
        addColumn(LATITUDE_COLUMN);
        addColumn(LONGITUDE_COLUMN);
        addColumn(ALTITUDE_COLUMN);
    }

    public void setFilter1Hz(boolean filter1Hz) {
        this.filter1Hz = filter1Hz;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestampString = fields[getColumnIndex(TIMESTAMP_COLUMN)];
            timestampString = timestampString.substring(0, timestampString.indexOf(" UTC"));
            long timestamp = Utility.parseTime(timestampString, DateTimeFormatter.ofPattern(DATE_FORMAT));

            if (filter1Hz) {
                if (currentSecond == timestamp / 1000) {
                    return;
                } else {
                    currentSecond = timestamp / 1000;
                }
            }

            timestamp = currentSecond * 1000;
            double latitude = Double.parseDouble(fields[getColumnIndex(LATITUDE_COLUMN)]);
            double longitude = Double.parseDouble(fields[getColumnIndex(LONGITUDE_COLUMN)]);
            double altitude = Double.parseDouble(fields[getColumnIndex(ALTITUDE_COLUMN)]);
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude)));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }
}
