package gov.mil.otc._3dvis.data.file.delimited.csv;

import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GpsLogUtmCsvFile extends TspiCsvFile {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SS";

    public GpsLogUtmCsvFile(File file) {
        super(file);

        addColumn("Date(GMT)");
        addColumn("UTM Zone (North/South)");
        addColumn("Easting (m)");
        addColumn("Northing (m)");
        addColumn("Altitude(m)");
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestampString = fields[getColumnIndex("Date(GMT)")];
            LocalDateTime localDateTime = LocalDateTime.parse(timestampString,
                    DateTimeFormatter.ofPattern(DATE_FORMAT));
            Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
            long timestamp = instant.toEpochMilli();
            String[] zoneFields = fields[getColumnIndex("UTM Zone (North/South)")].split("\\s+");
            if (zoneFields.length != 2) {
                return;
            }
            int zone = Integer.parseInt(zoneFields[0]);
            String hemisphere = zoneFields[1].equals("S") ? AVKey.SOUTH : AVKey.NORTH;
            double easting = Double.parseDouble(fields[getColumnIndex("Easting (m)")]);
            double northing = Double.parseDouble(fields[getColumnIndex("Northing (m)")]);
            UTMCoord utmCoord = UTMCoord.fromUTM(zone, hemisphere, easting, northing);
            double altitude = Double.parseDouble(fields[getColumnIndex("Altitude(m)")]);
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(
                    utmCoord.getLatitude().getDegrees(), utmCoord.getLongitude().getDegrees(), altitude)));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }
}
