package gov.mil.otc._3dvis.data.gps;

import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class P10DataLog extends TspiCsvFile {

    //HEADER : INDEX,TAG,DATE,TIME,LATITUDE N/S,LONGITUDE E/W,HEIGHT,SPEED,HEADING
    //INDEX : Sequence number
    //TAG : Type of way point. T: Normal point; C: POI; Second type of POI; G: Wake-up point
    //DATE : Date (yymmdd)
    //TIME : Time (hhmmss)
    //LATITUDE N/S : Latitude (dd.dddddd) N: north; S: south
    //LONGITUDE E/W : Longitude (ddd.dddddd). E: east; W: west
    //HEIGHT : Mean sea level altitude (meters)
    //SPEED : Speed (km h)
    //HEADING : Course over ground (degrees)

    private static final String TIMESTAMP_FORMAT = "yyMMddHHmmss";
    private static final String INDEX_COLUMN = "INDEX";
    private static final String TAG_COLUMN = "TAG";
    private static final String DATE_COLUMN = "DATE";
    private static final String TIME_COLUMN = "TIME";
    private static final String LATITUDE_COLUMN = "LATITUDE N/S";
    private static final String LONGITUDE_COLUMN = "LONGITUDE E/W";
    private static final String ALTITUDE_COLUMN = "HEIGHT";
    private static final String SPEED_COLUMN = "SPEED";
    private static final String HEADING_COLUMN = "HEADING";

    public P10DataLog(File file) {
        super(file);
        addColumn(INDEX_COLUMN);
        addColumn(TAG_COLUMN);
        addColumn(DATE_COLUMN);
        addColumn(TIME_COLUMN);
        addColumn(LATITUDE_COLUMN);
        addColumn(LONGITUDE_COLUMN);
        addColumn(ALTITUDE_COLUMN);
        addColumn(SPEED_COLUMN);
        addColumn(HEADING_COLUMN);
    }


    @Override
    protected void processLine(String[] fields) {
        try {
            String timestampString = fields[getColumnIndex(DATE_COLUMN)] + fields[getColumnIndex(TIME_COLUMN)];
            long timestamp = Utility.parseTime(timestampString, TIMESTAMP_FORMAT);

            String latitudeString = fields[getColumnIndex(LATITUDE_COLUMN)];
            if (latitudeString.length() < 2) {
                return;
            }
            int latitudeModifier = (latitudeString.charAt(latitudeString.length() - 1) == 'N') ? 1 : -1;
            double latitude = latitudeModifier * Double.parseDouble(latitudeString.substring(0, latitudeString.length() - 1));

            String longitudeString = fields[getColumnIndex(LONGITUDE_COLUMN)];
            if (longitudeString.length() < 2) {
                return;
            }
            int longitudeModifier = (longitudeString.charAt(latitudeString.length() - 1) == 'N') ? 1 : -1;
            double longitude = longitudeModifier * Double.parseDouble(longitudeString.substring(0, longitudeString.length() - 1));

            double altitude = Double.parseDouble(fields[getColumnIndex(ALTITUDE_COLUMN)]);
            double speed = Double.parseDouble(fields[getColumnIndex(SPEED_COLUMN)]);
            double heading = Double.parseDouble(fields[getColumnIndex(HEADING_COLUMN)]);

            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude), speed, heading, false));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "P10DataLog::processLine", e);
        }
    }
}