package gov.mil.otc._3dvis.project.nbcrv.flir;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UasGpsFile extends CsvFile {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIMESTAMP_COLUMN = "Timestamp";
    private static final String NAME_COLUMN = "Name";
    private static final String SERIAL_COLUMN = "Serial";
    private static final String LATITUDE_DEGREES_COLUMN = "LatitudeDegrees";
    private static final String LONGITUDE_DEGREES_COLUMN = "LongitudeDegrees";
    private static final String ALTITUDE_METERS_COLUMN = "AltitudeMeters";
    private static final String HEADING_DEGREES_COLUMN = "HeadingDegrees";
    private static final String BATTERY_TOTAL_PERCENT_COLUMN = "BatteryTotalPercent";
    private static final String BATTERY_1_PERCENT_COLUMN = "Battery1Percent";
    private static final String BATTERY_2_PERCENT_COLUMN = "Battery2Percent";
    private static final String BATTERY_3_PERCENT_COLUMN = "Battery3Percent";
    private static final String BATTERY_4_PERCENT_COLUMN = "Battery4Percent";
    private final List<TspiData> tspiDataList = new ArrayList<>();
    private String name = "";

    public UasGpsFile(File file) {
        super(file);

        addColumn(TIMESTAMP_COLUMN);
        addColumn(NAME_COLUMN);
        addColumn(SERIAL_COLUMN);
        addColumn(LATITUDE_DEGREES_COLUMN);
        addColumn(LONGITUDE_DEGREES_COLUMN);
        addColumn(ALTITUDE_METERS_COLUMN);
        addColumn(HEADING_DEGREES_COLUMN);
        addColumn(BATTERY_TOTAL_PERCENT_COLUMN);
        addColumn(BATTERY_1_PERCENT_COLUMN);
        addColumn(BATTERY_2_PERCENT_COLUMN);
        addColumn(BATTERY_3_PERCENT_COLUMN);
        addColumn(BATTERY_4_PERCENT_COLUMN);
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestring = fields[getColumnIndex(TIMESTAMP_COLUMN)];
            long timestamp = Utility.parseTime(timestring, DATE_FORMAT);
            double latitude = Double.parseDouble(fields[getColumnIndex(LATITUDE_DEGREES_COLUMN)]);
            double longitude = Double.parseDouble(fields[getColumnIndex(LONGITUDE_DEGREES_COLUMN)]);
            double elevation = WWController.getGlobe().getElevation(Angle.fromDegreesLatitude(latitude), Angle.fromDegreesLongitude(longitude));
            double agl = Double.parseDouble(fields[getColumnIndex(ALTITUDE_METERS_COLUMN)]);
            double heading = Double.parseDouble(fields[getColumnIndex(HEADING_DEGREES_COLUMN)]);
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, elevation + agl), null, null, heading, null, null));
            name = fields[getColumnIndex(NAME_COLUMN)];
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public List<TspiData> getTspiDataList() {
        return tspiDataList;
    }

    public String getName() {
        return name;
    }
}
