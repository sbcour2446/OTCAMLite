package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.worldwindex.terrain.HighResolutionTerrainEx;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TspiFile extends CsvFile {

    private static final String DATE_FORMAT = "yyyy DDD HH:mm:ss.SSSSSS";
    private final int year;
    private final List<TspiData> tspiDataList = new ArrayList<>();
    private final List<FlightData> flightDataList = new ArrayList<>();
    private final HighResolutionTerrain terrain;
    private String timeHeader = "Time";
    private String bus1LatitudeHeader = "1553-1:BLEND_LAT_M122T03";
    private String bus1LongitudeHeader = "1553-1:BLEND_LONG_M122T17";
    private String bus1MslAltitudeHeader = "1553-1:MSL_ALT_M122T29";
    private String bus1PressureAltitudeHeader = "1553-1:PRESS_ALT_M122R30";
    private String bus1RadarAltitudeHeader = "1553-1:RADALT_ALT_M120T01";
    private String bus1HeadingHeader = "1553-1:BLENDED_TRUE_HEADING_M122T03";
    private String bus1WowHeader = "1553-1:WOW_M117T01";
    private String bus2WowHeader = "1553-2:WOW_M218T01";
    private String bus1AirspeedHeader = "1553-1:TRUE_ASPD_M122R30";
    private String bus2LatitudeHeader = "1553-2:BLEND_LAT_M223T03";
    private String bus2LongitudeHeader = "1553-2:BLEND_LONGITUDE_M223T17";
    private String bus2MslAltitudeHeader = "1553-2:MSL_ALT_M223T29";
    private String bus2PressureAltitudeHeader = "1553-2:PRESS_ALT_M223R30";
    private String bus2HeadingHeader = "1553-2:BLENDED_TRUE_HEADING_M223T03";
    private String bus2AirspeedHeader = "1553-2:TRUE_ASPD_M223R30";

    public TspiFile(File file, int year) {
        super(file, 3);

        this.year = year;
        terrain = new HighResolutionTerrainEx(WWController.getGlobe(), null);
        terrain.setUseCachedElevationsOnly(false);
        terrain.setCacheCapacity((long) 500e6);
        terrain.setTimeout(1000L);

        timeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.timeHeader", timeHeader);
        bus1LatitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.latitudeHeader", bus1LatitudeHeader);
        bus1LongitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.longitudeHeader", bus1LongitudeHeader);
        bus1MslAltitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.mslAltitudeHeader", bus1MslAltitudeHeader);
        bus1PressureAltitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.pressureAltitudeHeader", bus1PressureAltitudeHeader);
        bus1RadarAltitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.radarAltitudeHeader", bus1RadarAltitudeHeader);
        bus1HeadingHeader = SettingsManager.getSettings().getString("blackhawk.tspi.headingHeader", bus1HeadingHeader);
        bus1WowHeader = SettingsManager.getSettings().getString("blackhawk.tspi.wowHeader", bus1WowHeader);
        bus1AirspeedHeader = SettingsManager.getSettings().getString("blackhawk.tspi.airspeedHeader", bus1AirspeedHeader);
        timeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.timeHeader", timeHeader);
        bus2LatitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.latitudeHeader", bus2LatitudeHeader);
        bus2LongitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.longitudeHeader", bus2LongitudeHeader);
        bus2MslAltitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.mslAltitudeHeader", bus2MslAltitudeHeader);
        bus2PressureAltitudeHeader = SettingsManager.getSettings().getString("blackhawk.tspi.pressureAltitudeHeader", bus2PressureAltitudeHeader);
        bus2HeadingHeader = SettingsManager.getSettings().getString("blackhawk.tspi.headingHeader", bus2HeadingHeader);
        bus2WowHeader = SettingsManager.getSettings().getString("blackhawk.tspi.wowHeader", bus2WowHeader);
        bus2AirspeedHeader = SettingsManager.getSettings().getString("blackhawk.tspi.airspeedHeader", bus2AirspeedHeader);

        addColumn(timeHeader);
        addColumn(bus1LatitudeHeader, true);
        addColumn(bus1LongitudeHeader, true);
        addColumn(bus1MslAltitudeHeader, true);
        addColumn(bus1PressureAltitudeHeader, true);
        addColumn(bus1RadarAltitudeHeader, true);
        addColumn(bus1HeadingHeader, true);
        addColumn(bus1WowHeader, true);
        addColumn(bus1AirspeedHeader, true);
        addColumn(bus2LatitudeHeader, true);
        addColumn(bus2LongitudeHeader, true);
        addColumn(bus2MslAltitudeHeader, true);
        addColumn(bus2PressureAltitudeHeader, true);
        addColumn(bus2HeadingHeader, true);
        addColumn(bus2WowHeader, true);
        addColumn(bus2AirspeedHeader, true);
    }

    public List<TspiData> getTspiDataList() {
        return tspiDataList;
    }

    public List<FlightData> getFlightDataList() {
        return flightDataList;
    }

    public long getFirstTimestamp() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            if (!processHeader(bufferedReader)) {
                return 0;
            }

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (verifyFields(fields)) {
                    String timestampString = year + " " + fields[getColumnIndex(timeHeader)];
                    LocalDateTime localDateTime = LocalDateTime.parse(timestampString,
                            DateTimeFormatter.ofPattern(DATE_FORMAT));
                    Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
                    if (instant.toEpochMilli() > 0) {
                        return instant.toEpochMilli();
                    }
                }
            }
        } catch (Exception e) {
            String message = String.format("Error processing BlackHawk TSPI file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }

        return 0;
    }

    public long getLastTimestamp() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            if (!processHeader(bufferedReader)) {
                return 0;
            }

            String[] lastFields = null;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (verifyFields(fields)) {
                    lastFields = fields;
                }
            }

            if (lastFields != null) {
                String timestampString = year + " " + lastFields[getColumnIndex(timeHeader)];
                LocalDateTime localDateTime = LocalDateTime.parse(timestampString,
                        DateTimeFormatter.ofPattern(DATE_FORMAT));
                Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
                return instant.toEpochMilli();
            }
        } catch (Exception e) {
            String message = String.format("Error processing BlackHawk TSPI file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }

        return 0;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestampString = year + " " + fields[getColumnIndex(timeHeader)];
            LocalDateTime localDateTime = LocalDateTime.parse(timestampString,
                    DateTimeFormatter.ofPattern(DATE_FORMAT));
            Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
            long timestamp = instant.toEpochMilli();
            Double latitude = parseDouble(fields[getColumnIndex(bus1LatitudeHeader)]);
            if (latitude == null) {
                latitude = parseDouble(fields[getColumnIndex(bus2LatitudeHeader)]);
                if (latitude == null) {
                    return;
                }
            }

            Double longitude = parseDouble(fields[getColumnIndex(bus1LongitudeHeader)]);
            if (longitude == null) {
                longitude = parseDouble(fields[getColumnIndex(bus2LongitudeHeader)]);
                if (longitude == null) {
                    return;
                }
            }

            if (latitude == 0 || longitude == 0) {
                return;
            }

            Double altitude = parseDouble(fields[getColumnIndex(bus1MslAltitudeHeader)]);
            if (altitude == null) {
                altitude = parseDouble(fields[getColumnIndex(bus2MslAltitudeHeader)]);
                if (altitude == null) {
                    return;
                }
            }
            altitude *= 0.3048;

//            Double terrainElevation = terrain.getElevation(LatLon.fromDegrees(latitude, longitude));
//            if (terrainElevation == null) {
//                terrainElevation = terrain.getElevation(LatLon.fromDegrees(latitude + .00001, longitude + .00001));
//                if (terrainElevation == null) {
//                    terrainElevation = 0.0;
//                }
//            }

            Double pressureAltitude = parseDouble(fields[getColumnIndex(bus1PressureAltitudeHeader)]);
            if (pressureAltitude == null) {
                pressureAltitude = parseDouble(fields[getColumnIndex(bus2PressureAltitudeHeader)]);
                if (pressureAltitude == null) {
                    pressureAltitude = 0.0;
                }
            }
            pressureAltitude *= 0.3048;

            Double radarAltitude = parseDouble(fields[getColumnIndex(bus1RadarAltitudeHeader)]);
            if (radarAltitude == null) {
                radarAltitude = 0.0;
            }
            radarAltitude *= 0.3048;

            Double heading = parseDouble(fields[getColumnIndex(bus1HeadingHeader)]);
            if (heading == null) {
                heading = parseDouble(fields[getColumnIndex(bus2HeadingHeader)]);
                if (heading == null) {
                    heading = 0.0;
                }
            }

            Double airspeed = parseDouble(fields[getColumnIndex(bus1AirspeedHeader)]);
            if (airspeed == null) {
                airspeed = parseDouble(fields[getColumnIndex(bus2AirspeedHeader)]);
                if (airspeed == null) {
                    airspeed = 0.0;
                }
            }

            String wheelStatus = fields[getColumnIndex(bus1WowHeader)];
            if (wheelStatus.isBlank()) {
                wheelStatus = fields[getColumnIndex(bus2WowHeader)];
            }

//            double useAltitude = altitude;// > 1500 ? altitude : radarAltitude;
//            if (altitude < 1500 && radarAltitude > 0) {
//                useAltitude = radarAltitude;
//            }
//
//            if (terrainElevation != null) {
//                useAltitude = terrainElevation;
//            }

//            double useAltitude = altitude;
//            if (wheelStatus.equalsIgnoreCase("WEIGHT ON") && terrainElevation > 1) {
//                useAltitude = terrainElevation;
//            } else if (terrainElevation > 1 && altitude < 1500) {
//                useAltitude = terrainElevation + radarAltitude;
//            }

//            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, useAltitude)));
//System.out.println(latitude + " : " + longitude + " : " + terrainElevation + " : " + altitude + " : " + useAltitude );
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude)));
//                    Math.max(altitude, terrainElevation))));
            flightDataList.add(new FlightData(timestamp, pressureAltitude, radarAltitude, heading, airspeed, wheelStatus));
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.INFO, message, e);
        }
    }

    private Double parseDouble(String value) {
        if (!value.isBlank()) {
            try {
                return Double.parseDouble(value);
            } catch (Exception e) {
                if (Logger.getGlobal().isLoggable(Level.INFO)) {
                    String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
                    Logger.getGlobal().log(Level.INFO, message, e);
                }
            }
        }
        return null;
    }
}
