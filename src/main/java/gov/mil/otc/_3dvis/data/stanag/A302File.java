package gov.mil.otc._3dvis.data.stanag;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Angle;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CSV File that contains STANAG message #302: EO/IR/Laser Operating State.
 */
public class A302File extends CsvFile {

    private final List<UasPayloadData> uasPayloadDataList = new ArrayList<>();

    public A302File(File file) {
        super(file);

        addColumn("IRIG");
        addColumn("RecNum");
        addColumn("Date1");
        addColumn("Source");
        addColumn("TimeStamp1");
        addColumn("VehicleID");
        addColumn("CUCSID");
        addColumn("StationNum");
        addColumn("AddressedSensor");
        addColumn("SysOpModeState");
        addColumn("EoCameraStatus");
        addColumn("IrPolarityStatus");
        addColumn("ImageOutputState");
        addColumn("ActCenterElAngle");
        addColumn("ActVertFieldOfView");
        addColumn("ActCenterAzAngle");
        addColumn("ActHorFieldOfView");
        addColumn("ActSensorRotAngle");
        addColumn("ImagePosition");
        addColumn("Latitude");
        addColumn("Longitude");
        addColumn("Altitude");
        addColumn("PointingModeState");
        addColumn("PreplanMode");
        addColumn("ReportedRange");
        addColumn("FLaserPointerStatus");
        addColumn("SelLaserRangeFirstLast");
        addColumn("LaserDesignatorCode");
        addColumn("LaserDesignatorStatus");
    }

    public List<UasPayloadData> getUasPayloadDataList() {
        return uasPayloadDataList;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestring = fields[getColumnIndex("IRIG")];
            String timestampString = fields[getColumnIndex("Date1")] + " " +
                    timestring.substring(timestring.indexOf(":") + 1);
            String timeFormat = "M/dd/yyyy HH:mm:ss.SSS";
            if (timestampString.length() > 23) {
                timeFormat = "M/dd/yyyy HH:mm:ss.SSSSSS";
            }
            long timestamp = Utility.parseTime(timestampString, timeFormat);
//
//            String timestring = fields[getColumnIndex("IRIG")];
//            String timestampString = fields[getColumnIndex("Date1")] + " " +
//                    timestring.substring(timestring.indexOf(":") + 1);
//            LocalDateTime localDateTime = LocalDateTime.parse(timestampString,
//                    DateTimeFormatter.ofPattern("M/dd/yyyy HH:mm:ss.SSS"));
//            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
//            long timestamp = instant.toEpochMilli();
            double latitude = Angle.fromRadians(Double.parseDouble(fields[getColumnIndex("Latitude")])).getDegrees();
            double longitude = Angle.fromRadians(Double.parseDouble(fields[getColumnIndex("Longitude")])).getDegrees();

            UasPayloadData uasPayloadData = new UasPayloadData.Builder()
                    .setTimestamp(timestamp)
                    .setSysOpModeState(Integer.parseInt(fields[getColumnIndex("SysOpModeState")]))
                    .setEoCameraStatus(Integer.parseInt(fields[getColumnIndex("EoCameraStatus")]))
                    .setIrPolarityStatus(Integer.parseInt(fields[getColumnIndex("IrPolarityStatus")]))
                    .setImageOutputState(Integer.parseInt(fields[getColumnIndex("ImageOutputState")]))
                    .setActCenterElAngle(Double.parseDouble(fields[getColumnIndex("ActCenterElAngle")]))
                    .setActVertFieldOfView(Double.parseDouble(fields[getColumnIndex("ActVertFieldOfView")]))
                    .setActCenterAzAngle(Double.parseDouble(fields[getColumnIndex("ActCenterAzAngle")]))
                    .setActHorFieldOfView(Double.parseDouble(fields[getColumnIndex("ActHorFieldOfView")]))
                    .setActSensorRotAngle(Double.parseDouble(fields[getColumnIndex("ActSensorRotAngle")]))
                    .setImagePosition(Integer.parseInt(fields[getColumnIndex("ImagePosition")]) != 0)
                    .setLatitude(latitude)
                    .setLongitude(longitude)
                    .setAltitude(Double.parseDouble(fields[getColumnIndex("Altitude")]))
                    .setReportedRange(Double.parseDouble(fields[getColumnIndex("ReportedRange")]))
                    .setPreplanMode(Integer.parseInt(fields[getColumnIndex("PreplanMode")]))
                    .setfLaserPointerStatus(Integer.parseInt(fields[getColumnIndex("FLaserPointerStatus")]))
                    .setSelLaserRangeFirstLast(Integer.parseInt(fields[getColumnIndex("SelLaserRangeFirstLast")]))
                    .setLaserDesignatorCode(Integer.parseInt(fields[getColumnIndex("LaserDesignatorCode")]))
                    .setLaserDesignatorStatus(Integer.parseInt(fields[getColumnIndex("LaserDesignatorStatus")]))
                    .setPointingModeState(Integer.parseInt(fields[getColumnIndex("PointingModeState")]))
                    .build();

            uasPayloadDataList.add(uasPayloadData);
        } catch (Exception e) {
            if (Logger.getGlobal().isLoggable(Level.INFO)) {
                String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.INFO, message, e);
            }
        }
    }
}
