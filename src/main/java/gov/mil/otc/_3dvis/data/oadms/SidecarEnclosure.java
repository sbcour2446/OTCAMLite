package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.staticentity.StaticEntity;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.nasa.worldwind.geom.Position;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SidecarEnclosure extends StaticEntity {

    public static SidecarEnclosure createEntity(Sensor sensor) {
        try {
            int id = Integer.parseInt(sensor.getSerialNumber());
            EntityId entityId = new EntityId(Defaults.SITE_APP_ID_3DVIS, Defaults.APP_ID_NBCRV, id);
            SidecarEnclosure sidecarEnclosure = new SidecarEnclosure(entityId, sensor);
            LocalDateTime localDateTime = LocalDateTime.parse(sensor.getTrialStartTime(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
            long timestamp = instant.toEpochMilli();
            EntityDetail entityDetail = new EntityDetail.Builder()
                    .setName(sensor.getSensorName())
                    .setSource("xml")
                    .setTimestamp(timestamp)
                    .build();
            sidecarEnclosure.addEntityDetail(entityDetail);
            return sidecarEnclosure;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "SidecarEnclosure:createEntity", e);
            return null;
        }
    }

    private final Sensor sensor;

    protected SidecarEnclosure(EntityId entityId, Sensor sensor) {
        super(entityId, "/images/dot_light_blue.png");
        this.sensor = sensor;
    }

    public void processSensorReadings() {
        for (Reading reading : sensor.getReadings().getAll()) {
            Position position= getPosition(reading);
            if (position != null) {
                addTspi(new TspiData(reading.getTimestamp(), position));
            }
        }
    }

    public Position getPosition(Reading reading) {
        try {
            double latitude = Double.parseDouble(reading.getValue("latitude"));
            double longitude = Double.parseDouble(reading.getValue("longitude"));
            double altitude = Double.parseDouble(reading.getValue("altitude"));
            return Position.fromDegrees(latitude, longitude, altitude);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "SidecarEnclosure:getPosition", e);
        }
        return null;
    }
}
