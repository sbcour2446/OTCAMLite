package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.worldwindex.terrain.HighResolutionTerrainEx;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class CcmEventFile extends CsvFile {

    private static final double RANGE = 10000.0;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    private final Map<String, AdHocEntity> entityMap = new HashMap<>();
    private final HighResolutionTerrainEx terrain;
    private final Map<String, EntityType> entityTypeMap = new HashMap<>();
    private long startTime = Long.MAX_VALUE;
    private long stopTime = Long.MIN_VALUE;

    public CcmEventFile(File file) {
        super(file);
        entityTypeMap.put("SA-18", new EntityType(1, 1, 222, 28, 0, 0, 0));
        entityTypeMap.put("ZSU-23", new EntityType(1, 1, 222, 28, 12, 1, 0));
        entityTypeMap.put("RBS-70", new EntityType(1, 1, 205, 28, 1, 3, 0));

        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        terrain = new HighResolutionTerrainEx(WWController.getGlobe(), null);
        terrain.setUseCachedElevationsOnly(false);
        terrain.setCacheCapacity((long) 500e6);
        terrain.setTimeout(1000L);

        addColumn("Date");
        addColumn("Mission");
        addColumn("Threat");
        addColumn("Threat Latitude");
        addColumn("Threat Longitude");
        addColumn("Threat AGL");
        addColumn("Target Azimuth");
        addColumn("Target Elevation");
        addColumn("Type");
        addColumn("System");
        addColumn("Radar Type");
        addColumn("Time On");
        addColumn("Time Off");
    }

    public List<AdHocEntity> getEntityList() {
        return new ArrayList<>(entityMap.values());
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    @Override
    protected boolean doProcessFile() {
        int totalLines = countLines();
        int lineCount = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            if (!processHeader(bufferedReader)) {
                return false;
            }
            String line = bufferedReader.readLine(); //skip 2nd row
            Logger.getGlobal().log(Level.FINEST, "skip line: {0}", line);
            while (!cancelRequested && (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                String[] fields = line.split(",");
                if (verifyFields(fields)) {
                    processLine(fields);
                }
                lineCount++;
                status = (double) lineCount / totalLines;
            }
        } catch (Exception e) {
            if (Logger.getGlobal().isLoggable(Level.WARNING)) {
                String message = String.format("Error processing BlackHawk payload file %s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
            return false;
        }

        return !cancelRequested;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String threat = fields[getColumnIndex("Threat")];
            String system = fields[getColumnIndex("System")];
            String date = fields[getColumnIndex("Date")];
            String timeOnString = fields[getColumnIndex("Time On")];
            String timeOffString = fields[getColumnIndex("Time Off")];
            Calendar timeOn = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            timeOn.setTime(timeFormat.parse(String.format("%s %s", date, timeOnString)));
            Calendar timeOff = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            timeOff.setTime(timeFormat.parse(String.format("%s %s", date, timeOffString)));
            double latitude = Double.parseDouble(fields[getColumnIndex("Threat Latitude")]);
            double longitude = Double.parseDouble(fields[getColumnIndex("Threat Longitude")]);
            double altitude = terrain.getElevation(LatLon.fromDegrees(latitude, longitude));
            double agl = Double.parseDouble(fields[getColumnIndex("Threat AGL")]);
            double azimuth = Double.parseDouble(fields[getColumnIndex("Target Azimuth")]);
            double elevation = Double.parseDouble(fields[getColumnIndex("Target Elevation")]);
            String typeString = fields[getColumnIndex("Type")];
            String radarTypeString = fields[getColumnIndex("Radar Type")];
            Position position = Position.fromDegrees(latitude, longitude, altitude);

            if (timeOn.getTimeInMillis() < startTime) {
                startTime = timeOn.getTimeInMillis();
            }
            if (timeOff.getTimeInMillis() > stopTime) {
                stopTime = timeOff.getTimeInMillis();
            }

            AdHocEntity adHocEntity = entityMap.get(system);
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                adHocEntity.setName(system);
                adHocEntity.setSource(threat);
                adHocEntity.setAffiliation(Affiliation.HOSTILE);
                adHocEntity.setEntityType(lookupEntityType(system));
                entityMap.put(system, adHocEntity);
            }

            adHocEntity.getTspiDataList().add(new TspiData(timeOn.getTimeInMillis(), position));

            Position startPosition = new Position(position, position.getElevation() + agl * .3048);
            UTMCoord utm = UTMCoord.fromLatLon(startPosition.getLatitude(), startPosition.getLongitude());
            Position endPosition = new Position(UTMCoord.locationFromUTMCoord(
                    utm.getZone(), utm.getHemisphere(),
                    utm.getEasting() + sin((azimuth * 0.0174533)) * RANGE,
                    utm.getNorthing() + cos((azimuth * 0.0174533)) * RANGE,
                    null),
                    startPosition.getElevation() + sin(elevation * 0.0174533) * RANGE);
            CcmEvent ccmEvent = new CcmEvent(timeOn.getTimeInMillis(), timeOff.getTimeInMillis(), startPosition,
                    endPosition, azimuth, elevation, RANGE, EmitterType.fromString(typeString),
                    AcquisitionType.fromString(radarTypeString));
            adHocEntity.getEventList().add(ccmEvent);
        } catch (Exception e) {
            if (Logger.getGlobal().isLoggable(Level.INFO)) {
                String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.INFO, message, e);
            }
        }
    }

    private EntityType lookupEntityType(String name) {
        for (Map.Entry<String, EntityType> entry : entityTypeMap.entrySet()) {
            if (name.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return new EntityType(1, 1, 222, 28, 0, 0, 0);
    }
}
