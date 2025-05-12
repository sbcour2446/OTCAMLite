package gov.mil.otc._3dvis.data.gps;

import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrackStickCsvFile extends TspiCsvFile {

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    public TrackStickCsvFile(File file) {
        super(file);

        addColumn("Date");
        addColumn("Latitude");
        addColumn("Longitude");
        addColumn("Altitude");
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestampString = fields[getColumnIndex("Date")];
            LocalDateTime localDateTime = LocalDateTime.parse(timestampString,
                    DateTimeFormatter.ofPattern(DATE_FORMAT));
            Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
            long timestamp = instant.toEpochMilli();
            double latitude = Double.parseDouble(fields[getColumnIndex("Latitude")]);
            double longitude = Double.parseDouble(fields[getColumnIndex("Longitude")]);
            double altitude = Double.parseDouble(fields[getColumnIndex("Altitude")]);
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude)));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }
}
