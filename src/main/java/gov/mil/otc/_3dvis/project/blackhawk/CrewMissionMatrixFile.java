package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

//tblCrewMissionMatrix.csv
public class CrewMissionMatrixFile extends CsvFile {

    private static final String[] DATE_FORMATS = {
            "M/dd/yyyy HH:mm:ss",
            "M/dd/yyyy H:mm:ss",
            "M/dd/yyyy HH:mm",
            "M/dd/yyyy H:mm"};
    private String missionNumHeader = "missionNum";
    private String flightNumHeader = "flightNum";
    private String tailNumHeader = "tailNum";
    private String pinHeader = "tpPin";
    private String seatHeader = "tpSeat";
    private String roleHeader = "tpRole";
    //    private String startTimeHeader = "startTime";
//    private String endTimeHeader = "endTime";
    private String dateFormat = "";
    private final DataSource dataSource;

    public CrewMissionMatrixFile(File file, DataSource dataSource) {
        super(file);

        this.dataSource = dataSource;

        addColumn(missionNumHeader);
        addColumn(flightNumHeader);
        addColumn(tailNumHeader);
        addColumn(pinHeader);
        addColumn(seatHeader);
        addColumn(roleHeader);
//        addColumn(startTimeHeader);
//        addColumn(endTimeHeader);
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String mission = fields[getColumnIndex(missionNumHeader)].trim();
            int flightNumber = Integer.parseInt(fields[getColumnIndex(flightNumHeader)].trim());
            int tailNumber = Integer.parseInt(fields[getColumnIndex(tailNumHeader)].trim());
            String pin = fields[getColumnIndex(pinHeader)].trim();
            String seat = fields[getColumnIndex(seatHeader)].trim();
            String role = fields[getColumnIndex(roleHeader)].trim();
//            long startTime = parseDateTime(fields[getColumnIndex(startTimeHeader)].trim());
//            long endTime = parseDateTime(fields[getColumnIndex(endTimeHeader)].trim());

//            if (startTime > 0 && endTime > 0) {
            DatabaseLogger.addCrewMissionData(mission, flightNumber, tailNumber, pin, seat, role,
                    0, 0, dataSource.getId());
//            }
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.INFO, message, e);
        }
    }

    private long parseDateTime(String dateTimeString) {
        if (dateFormat.isBlank()) {
            dateFormat = getDateFormat(dateTimeString);
        }
        if (dateFormat.isBlank()) {
            return -1;
        }
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString,
                    DateTimeFormatter.ofPattern(dateFormat));
            Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
            return instant.toEpochMilli();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.INFO, null, e);
        }
        return -1;
    }

    private String getDateFormat(String dateTimeString) {
        for (String format : DATE_FORMATS) {
            try {
                LocalDateTime.parse(dateTimeString,
                        DateTimeFormatter.ofPattern(format));
                return format;
            } catch (Exception e) {
                Logger.getGlobal().log(Level.INFO, null, e);
            }
        }
        return "";
    }
}
