package gov.mil.otc._3dvis.data.stanag;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.datamodel.aircraft.TspiExtendedData;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

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
 * CSV File that contains STANAG message #101: Inertial States.
 */
public class A101File extends CsvFile {

    private final List<TspiData> tspiDataList = new ArrayList<>();
    private final List<TspiExtendedData> tspiExtendedDataList = new ArrayList<>();

    public A101File(File file) {
        super(file);

        addColumn("IRIG");
        addColumn("RecNum");
        addColumn("Date1");
        addColumn("Source");
        addColumn("TimeStamp1");
        addColumn("VehicleID");
        addColumn("CUCSID");
        addColumn("Latitude");
        addColumn("Longitude");
        addColumn("Altitude");
        addColumn("AltitudeType");
        addColumn("U_Speed");
        addColumn("V_Speed");
        addColumn("W_Speed");
        addColumn("U_Accel");
        addColumn("V_Accel");
        addColumn("W_Accel");
        addColumn("Phi");
        addColumn("Theta");
        addColumn("Psi");
        addColumn("Phi_Dot");
        addColumn("Theta_Dot");
        addColumn("Psi_Dot");
    }

    public List<TspiData> getTspiDataList() {
        return tspiDataList;
    }

    public List<TspiExtendedData> getTspiExtendedDataList() {
        return tspiExtendedDataList;
    }

    @Override
    protected void processLine(String[] fields) {
        //239:18:58:32.000
        //240:18:07:31.000000
        try {
            String timestring = fields[getColumnIndex("IRIG")];
            String timestampString = fields[getColumnIndex("Date1")] + " " +
                    timestring.substring(timestring.indexOf(":") + 1);
            String timeFormat = "M/dd/yyyy HH:mm:ss.SSS";
            if (timestampString.length() > 23) {
                timeFormat = "M/dd/yyyy HH:mm:ss.SSSSSS";
            }
            long timestamp = Utility.parseTime(timestampString, timeFormat);
//            LocalDateTime localDateTime = LocalDateTime.parse(timestampString,
//                    DateTimeFormatter.ofPattern("M/dd/yyyy HH:mm:ss.SSS"));
//            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
//            long timestamp = instant.toEpochMilli();

            double latitude = Angle.fromRadians(Double.parseDouble(fields[getColumnIndex("Latitude")])).getDegrees();
            double longitude = Angle.fromRadians(Double.parseDouble(fields[getColumnIndex("Longitude")])).getDegrees();
            double altitude = Double.parseDouble(fields[getColumnIndex("Altitude")]);
            double northVelocity = Double.parseDouble(fields[getColumnIndex("U_Accel")]);
            double eastVelocity = Double.parseDouble(fields[getColumnIndex("V_Accel")]);
            double forwardVelocity = Math.sqrt(Math.pow(northVelocity, 2) + Math.pow(eastVelocity, 2));
            double verticalVelocity = Double.parseDouble(fields[getColumnIndex("W_Accel")]);
            double heading = Angle.fromRadians(Double.parseDouble(fields[getColumnIndex("Psi")])).getDegrees();
            double pitch = Angle.fromRadians(Double.parseDouble(fields[getColumnIndex("Theta")])).getDegrees();
            double roll = Angle.fromRadians(Double.parseDouble(fields[getColumnIndex("Phi")])).getDegrees();
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude),
                    forwardVelocity, verticalVelocity, heading, pitch, roll));

            TspiExtendedData tspiExtendedData = new TspiExtendedData.Builder()
                    .setTimestamp(timestamp)
                    .setAltitudeType(Double.parseDouble(fields[getColumnIndex("AltitudeType")]))
                    .setUSpeed(Double.parseDouble(fields[getColumnIndex("U_Speed")]))
                    .setVSpeed(Double.parseDouble(fields[getColumnIndex("V_Speed")]))
                    .setWSpeed(Double.parseDouble(fields[getColumnIndex("W_Speed")]))
                    .setUAcceleration(Double.parseDouble(fields[getColumnIndex("U_Accel")]))
                    .setVAcceleration(Double.parseDouble(fields[getColumnIndex("V_Accel")]))
                    .setWAcceleration(Double.parseDouble(fields[getColumnIndex("W_Accel")]))
                    .setPhi(Double.parseDouble(fields[getColumnIndex("Phi")]))
                    .setTheta(Double.parseDouble(fields[getColumnIndex("Theta")]))
                    .setPsi(Double.parseDouble(fields[getColumnIndex("Psi")]))
                    .setPhiDot(Double.parseDouble(fields[getColumnIndex("Phi_Dot")]))
                    .setThetaDot(Double.parseDouble(fields[getColumnIndex("Theta_Dot")]))
                    .setPsiDot(Double.parseDouble(fields[getColumnIndex("Psi_Dot")]))
                    .build();
            tspiExtendedDataList.add(tspiExtendedData);
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
