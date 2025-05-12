package gov.mil.otc._3dvis.data.otcam;

import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.event.MunitionDetonationEvent;
import gov.mil.otc._3dvis.event.MunitionFireEvent;
import gov.mil.otc._3dvis.event.otcam.*;
import gov.mil.otc._3dvis.project.dlm.DlmPlaybackEntity;
import gov.mil.otc._3dvis.project.dlm.IDlmEntity;
import gov.mil.otc._3dvis.project.dlm.message.DlmMessage;
import gov.mil.otc._3dvis.project.dlm.message.DlmMessageFactory;
import gov.mil.otc._3dvis.project.dlm.message.GenericDlmMessage;
import gov.mil.otc._3dvis.tena.TenaUtility;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OtcamUtility {

    private static final String DATABASE_DRIVER = "org.sqlite.JDBC";
    private static final String TIMESTAMP_COLUMN = "timestamp";
    private static final String EVENT_SITE_COLUMN = "event_site";
    private static final String EVENT_APP_COLUMN = "event_app";
    private static final String EVENT_ID_COLUMN = "event_id";
    private static final String SHOOTER_SITE_COLUMN = "shooter_site";
    private static final String SHOOTER_APP_COLUMN = "shooter_app";
    private static final String SHOOTER_ID_COLUMN = "shooter_id";
    private static final String MUNITION_TYPE_COLUMN = "munition_type";
    private final String filename;
    private final String url;
    private Connection databaseConnection;
    private boolean cancel = false;

    public OtcamUtility(String filename) {
        this.filename = filename;
        url = "jdbc:sqlite:" + filename;
    }

    public void startLoadAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (open()) {
                    doLoad();
                    close();
                } else {
                    Logger.getGlobal().log(Level.SEVERE, "could not open database: " + filename);
                }
                OtcamLoader.loadComplete(filename);
            }
        }, "OtcamUtitity::loadAsync:" + filename).start();
    }

    public void cancelLoad() {
        cancel = true;
    }

    private void doLoad() {
        List<EntityId> entityIds = getEntityIds();

        if (cancel) {
            return;
        }

        List<Event> events = getEvents();

        if (cancel) {
            return;
        }

        List<Long> startStopTime = getStartStopTime();

        if (cancel) {
            return;
        }

        if (startStopTime.size() != 2) {
            return;
        }

        for (EntityId entityId : entityIds) {
            if (cancel) {
                return;
            }

            IEntity entity = EntityManager.getEntity(entityId);
            if (entity == null) {
                entity = new PlaybackEntity(entityId);
                EntityManager.addEntity(entity);
            }
            getEntityScopes(entity, -1);

            if (cancel) {
                return;
            }

            getEntityDetails(entity, -1);

            if (cancel) {
                return;
            }

            if (createOrderedTspi()) {
                getTspiFast(entity, -1);
            } else {
                getTspi(entity, -1);
            }

            if (cancel) {
                return;
            }

            if (isDlm(entityId)) {
                if (!(entity instanceof IDlmEntity) &&
                        entity instanceof AbstractEntity) {
                    entity = new DlmPlaybackEntity((AbstractEntity) entity);
                    EntityManager.addEntity(entity);
                }
                if (entity instanceof IDlmEntity) {
                    getDlmMessages((IDlmEntity) entity, -1);
                }
            }
        }

        for (Event event : events) {
            if (cancel) {
                return;
            }

            EventManager.addEvent(event);
            if (event instanceof OtcamEvent otcamEvent) {
                IEntity target = EntityManager.getEntity(otcamEvent.getTargetId());
                if (target != null) {
                    target.addEvent(otcamEvent);
                }
            } else if (event instanceof MunitionFireEvent munitionFireEvent) {
                IEntity shooter = EntityManager.getEntity(munitionFireEvent.getShooterId());
                if (shooter != null) {
                    shooter.addEvent(munitionFireEvent);
                }
            }
        }
    }

    public boolean open() {
        try {
            Class.forName(DATABASE_DRIVER);
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return false;
        }
        return getConnection() != null;
    }

    public void close() {
        try {
            if (databaseConnection != null && !databaseConnection.isClosed()) {
                databaseConnection.close();
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Could not close OTCAM database %s", filename), e);
        }
    }

    public Connection getConnection() {
        try {
            if (databaseConnection == null || databaseConnection.isClosed()) {
                databaseConnection = DriverManager.getConnection(url);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Could not create database connection for %s", filename), e);
            databaseConnection = null;
        }
        return databaseConnection;
    }

    public List<EntityId> getEntityIds() {
        List<EntityId> entityIds = new ArrayList<>();

        Connection connection = getConnection();
        if (connection == null) {
            return entityIds;
        }

        String sql = "SELECT DISTINCT site, app, id FROM entity_discovery ORDER BY site, app, id";
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next() && !cancel) {
                int site = resultSet.getInt("site");
                int app = resultSet.getInt("app");
                int id = resultSet.getInt("id");
                entityIds.add(new EntityId(site, app, id));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return entityIds;
    }

    public List<EntityId> getEntityIdsWithName(String name) {
        List<EntityId> entityIds = new ArrayList<>();
        Connection connection = getConnection();
        if (connection == null) {
            return entityIds;
        }

        String sql = String.format("SELECT DISTINCT site,app,id FROM entity_discovery WHERE marking='%s' ORDER BY site,app,id", name);
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next() && !cancel) {
                int site = resultSet.getInt("site");
                int app = resultSet.getInt("app");
                int id = resultSet.getInt("id");
                entityIds.add(new EntityId(site, app, id));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "getEntityIdsForName", e);
        }

        return entityIds;
    }

    public void getEntityScopes(IEntity entity, int sourceId) {
        Connection connection = getConnection();
        if (connection == null) {
            return;
        }

        String sql = String.format("SELECT %s, TRUE AS inScope" +
                        " FROM entity_discovery WHERE site=%d AND app=%d AND id=%d" +
                        " UNION SELECT %s, FALSE AS inScope" +
                        " FROM entity_destruction WHERE site=%d AND app=%d AND id=%d" +
                        " UNION SELECT MAX(%s), FALSE AS inScope" +
                        " FROM tspi WHERE site=%d AND app=%d AND id=%d" +
                        " ORDER BY %s",
                TIMESTAMP_COLUMN,
                entity.getEntityId().getSite(), entity.getEntityId().getApplication(), entity.getEntityId().getId(),
                TIMESTAMP_COLUMN,
                entity.getEntityId().getSite(), entity.getEntityId().getApplication(), entity.getEntityId().getId(),
                TIMESTAMP_COLUMN,
                entity.getEntityId().getSite(), entity.getEntityId().getApplication(), entity.getEntityId().getId(),
                TIMESTAMP_COLUMN);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            long lastInScope = -1;
            while (resultSet.next() && !cancel) {
                long timestamp = resultSet.getLong(TIMESTAMP_COLUMN) / TenaUtility.MILLI_NANO;
                boolean inScope = resultSet.getBoolean("inScope");
                if (lastInScope < 0 && inScope) {
                    lastInScope = timestamp;
                } else if (lastInScope > 0 && !inScope) {
                    EntityScope entityScope = new EntityScope(lastInScope, timestamp);
                    entity.addEntityScope(entityScope);
                    if (sourceId >= 0) {
                        DatabaseLogger.addEntityScope(entityScope, entity.getEntityId(), sourceId);
                    }
                    lastInScope = -1;
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public void getEntityDetails(IEntity entity, int sourceId) {
        Connection connection = getConnection();
        if (connection == null) {
            return;
        }

        String sql = String.format("SELECT * FROM entity_state WHERE site=%d AND app=%d AND id=%d",
                entity.getEntityId().getSite(), entity.getEntityId().getApplication(), entity.getEntityId().getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next() && !cancel) {
                long timestamp = resultSet.getLong(TIMESTAMP_COLUMN) / TenaUtility.MILLI_NANO;
                String source = resultSet.getString("identifier") + " - "
                        + resultSet.getString("source_identifier");
                EntityType entityType = EntityType.fromString(resultSet.getString("entity_type"));
                Affiliation affiliation = Affiliation.getEnum(resultSet.getInt("affiliation"));
                String marking = resultSet.getString("marking");
                String militarySymbol = resultSet.getString("military_symbol");
                int urn = resultSet.getInt("urn");
                int milesPid = resultSet.getInt("miles_pid");
                boolean isKillCatastrophic = resultSet.getBoolean("is_kill_catastrophic");
                boolean isKillFirepower = resultSet.getBoolean("is_kill_firepower");
                boolean isKillMobility = resultSet.getBoolean("is_kill_mobility");
                boolean isKillCommunication = resultSet.getBoolean("is_kill_communication");
                boolean isSuppressed = resultSet.getBoolean("is_suppressed");
                boolean isJammed = resultSet.getBoolean("is_jammed");
                boolean isHit = resultSet.getBoolean("is_hit");
                boolean isMissed = resultSet.getBoolean("is_missed");
                String rtcaOther = resultSet.getString("rtca_other");
                int damagePercent = resultSet.getInt("damage_percent");

                RtcaState rtcaState = new RtcaState(isKillCatastrophic, isKillFirepower, isKillMobility,
                        isKillCommunication, isSuppressed, isJammed, isHit, isMissed, rtcaOther, damagePercent);

                if (entityType.getKind() != EntityType.Kind.MUNITION.ordinal()) {
                    militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, affiliation);
                }

                EntityDetail entityDetail = new EntityDetail.Builder()
                        .setTimestamp(timestamp)
                        .setEntityType(entityType)
                        .setAffiliation(affiliation)
                        .setName(marking)
                        .setMilitarySymbol(militarySymbol)
                        .setUrn(urn)
                        .setRtcaState(rtcaState)
                        .setSource(source)
                        .setMilesPid(milesPid)
                        .build();
                entity.addEntityDetail(entityDetail);
                if (sourceId >= 0) {
                    DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), sourceId);
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public void getTspiFast(IEntity entity, int sourceId) {
        Connection connection = getConnection();
        if (connection == null) {
            return;
        }

        List<TspiData> tspiDataList = new ArrayList<>();
        String sql = String.format("SELECT * FROM ordered_tspi WHERE site=%d AND app=%d AND id=%d ORDER BY timestamp",
                entity.getEntityId().getSite(), entity.getEntityId().getApplication(), entity.getEntityId().getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            double lastLatitude = 0;
            double lastLongitude = 0;
            double lastAltitude = 0;
            double lastForwardVelocity = 0;
            double lastVerticalVelocity = 0;
            double lastHeading = 0;
            double lastPitch = 0;
            double lastRoll = 0;
            boolean lastIsRecovered = false;
            while (resultSet.next() && !cancel) {
                long timestamp = resultSet.getLong(TIMESTAMP_COLUMN) / TenaUtility.MILLI_NANO;
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                double altitude = resultSet.getFloat("altitude");
                double forwardVelocity = resultSet.getFloat("forward_velocity");
                double verticalVelocity = resultSet.getFloat("vertical_velocity");
                double heading = resultSet.getFloat("heading");
                double pitch = resultSet.getFloat("pitch");
                double roll = resultSet.getFloat("roll");
                boolean isRecovered = resultSet.getBoolean("is_recovered");

                if (lastLatitude != latitude
                        || lastLongitude != longitude
                        || lastAltitude != altitude
                        || lastForwardVelocity != forwardVelocity
                        || lastVerticalVelocity != verticalVelocity
                        || lastHeading != heading
                        || lastPitch != pitch
                        || lastRoll != roll
                        || lastIsRecovered != isRecovered) {

                    TspiData tspiData = new TspiData(timestamp,
                            new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), altitude),
                            forwardVelocity, verticalVelocity, heading, pitch, roll, isRecovered);
                    tspiDataList.add(tspiData);

                    if (sourceId >= 0) {
                        DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), sourceId);
                    }

                    lastLatitude = latitude;
                    lastLongitude = longitude;
                    lastAltitude = altitude;
                    lastForwardVelocity = forwardVelocity;
                    lastVerticalVelocity = verticalVelocity;
                    lastHeading = heading;
                    lastPitch = pitch;
                    lastRoll = roll;
                    lastIsRecovered = isRecovered;
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        if (!tspiDataList.isEmpty()) {
            entity.addTspiList(tspiDataList);
        }
    }

    private boolean createOrderedTspi() {
        Connection connection = getConnection();
        if (connection == null) {
            return false;
        }

        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='ordered_tspi'";
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        sql = "CREATE TABLE ordered_tspi" +
                " (site INTEGER,app INTEGER,id INTEGER,timestamp INTEGER,latitude REAL,longitude REAL,altitude REAL," +
                "  forward_velocity REAL,vertical_velocity REAL,heading REAL,pitch REAL,roll REAL,is_recovered INTEGER," +
                " PRIMARY KEY(site,app,id,timestamp))";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return false;
        }

        sql = "INSERT INTO ordered_tspi" +
                " (site,app,id,timestamp,latitude,longitude,altitude,forward_velocity,vertical_velocity," +
                "  heading,pitch,roll,is_recovered)" +
                " SELECT site,app,id,timestamp,latitude,longitude,altitude,forward_velocity,vertical_velocity," +
                "  heading,pitch,roll,is_recovered" +
                " FROM tspi" +
                " ORDER BY site,app,id,timestamp";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return false;
        }

        return true;
    }

    public void getTspi(IEntity entity, int sourceId) {
        Connection connection = getConnection();
        if (connection == null) {
            return;
        }

        List<TspiData> tspiDataList = new ArrayList<>();
        String sql = String.format("SELECT * FROM tspi WHERE site=%d AND app=%d AND id=%d ORDER BY timestamp",
                entity.getEntityId().getSite(), entity.getEntityId().getApplication(), entity.getEntityId().getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            double lastLatitude = 0;
            double lastLongitude = 0;
            double lastAltitude = 0;
            double lastForwardVelocity = 0;
            double lastVerticalVelocity = 0;
            double lastHeading = 0;
            double lastPitch = 0;
            double lastRoll = 0;
            boolean lastIsRecovered = false;
            while (resultSet.next() && !cancel) {
                long timestamp = resultSet.getLong(TIMESTAMP_COLUMN) / TenaUtility.MILLI_NANO;
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                double altitude = resultSet.getFloat("altitude");
                double forwardVelocity = resultSet.getFloat("forward_velocity");
                double verticalVelocity = resultSet.getFloat("vertical_velocity");
                double heading = resultSet.getFloat("heading");
                double pitch = resultSet.getFloat("pitch");
                double roll = resultSet.getFloat("roll");
                boolean isRecovered = resultSet.getBoolean("is_recovered");

                if (lastLatitude != latitude
                        || lastLongitude != longitude
                        || lastAltitude != altitude
                        || lastForwardVelocity != forwardVelocity
                        || lastVerticalVelocity != verticalVelocity
                        || lastHeading != heading
                        || lastPitch != pitch
                        || lastRoll != roll
                        || lastIsRecovered != isRecovered) {

                    TspiData tspiData = new TspiData(timestamp,
                            new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), altitude),
                            forwardVelocity, verticalVelocity, heading, pitch, roll, isRecovered);
                    tspiDataList.add(tspiData);

                    if (sourceId >= 0) {
                        DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), sourceId);
                    }

                    lastLatitude = latitude;
                    lastLongitude = longitude;
                    lastAltitude = altitude;
                    lastForwardVelocity = forwardVelocity;
                    lastVerticalVelocity = verticalVelocity;
                    lastHeading = heading;
                    lastPitch = pitch;
                    lastRoll = roll;
                    lastIsRecovered = isRecovered;
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        if (!tspiDataList.isEmpty()) {
            entity.addTspiList(tspiDataList);
        }
    }

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();
        events.addAll(getMuntionFireEvents());
        events.addAll(getMuntionDetonationEvents());
        events.addAll(getOtcamEvents());
        return events;
    }

    public List<Event> getMuntionFireEvents() {
        List<Event> events = new ArrayList<>();
        Connection connection = getConnection();
        if (connection == null) {
            return events;
        }

        try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM munition_fire")) {
            while (resultSet.next() && !cancel) {
                long timestamp = resultSet.getLong(TIMESTAMP_COLUMN) / TenaUtility.MILLI_NANO;

                int eventSite = resultSet.getInt(EVENT_SITE_COLUMN);
                int eventApp = resultSet.getInt(EVENT_APP_COLUMN);
                int eventId = resultSet.getInt(EVENT_ID_COLUMN);
                EntityId eventEntityId = new EntityId(eventSite, eventApp, eventId);

                int shooterSite = resultSet.getInt(SHOOTER_SITE_COLUMN);
                int shooterApp = resultSet.getInt(SHOOTER_APP_COLUMN);
                int shooterId = resultSet.getInt(SHOOTER_ID_COLUMN);
                EntityId shooterEntityId = new EntityId(shooterSite, shooterApp, shooterId);

                int targetSite = resultSet.getInt("target_site");
                int targetApp = resultSet.getInt("target_app");
                int targetId = resultSet.getInt("target_id");
                EntityId targetEntityId = null;
                if (targetSite > 0 && targetApp > 0 && targetId > 0) {
                    targetEntityId = new EntityId(targetSite, targetApp, targetId);
                }
                EntityType munitionType = EntityType.fromString(resultSet.getString(MUNITION_TYPE_COLUMN));
                int quantity = resultSet.getInt("quantity");

                MunitionFireEvent munitionFireEvent = new MunitionFireEvent(timestamp, eventEntityId,
                        shooterEntityId, targetEntityId, munitionType, quantity);
                events.add(munitionFireEvent);
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return events;
    }

    public List<Event> getMuntionDetonationEvents() {
        List<Event> events = new ArrayList<>();
        Connection connection = getConnection();
        if (connection == null) {
            return events;
        }

        try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM munition_detonation")) {
            while (resultSet.next() && !cancel) {
                long timestamp = resultSet.getLong(TIMESTAMP_COLUMN) / TenaUtility.MILLI_NANO;

                int eventSite = resultSet.getInt(EVENT_SITE_COLUMN);
                int eventApp = resultSet.getInt(EVENT_APP_COLUMN);
                int eventId = resultSet.getInt(EVENT_ID_COLUMN);
                EntityId eventEntityId = new EntityId(eventSite, eventApp, eventId);

                EntityType munitionType = EntityType.fromString(resultSet.getString(MUNITION_TYPE_COLUMN));

                double impactLatitude = resultSet.getDouble("impact_latitude");
                double impactLongitude = resultSet.getDouble("impact_longitude");
                double impactAltitude = resultSet.getDouble("impact_altitude");
                Position impactPosition = new Position(Angle.fromDegrees(impactLatitude),
                        Angle.fromDegrees(impactLongitude), impactAltitude);

                int radius = resultSet.getInt("radius");

                MunitionDetonationEvent munitionDetonationEvent = new MunitionDetonationEvent(timestamp, eventEntityId,
                        impactPosition, munitionType, radius);
                events.add(munitionDetonationEvent);
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return events;
    }

    public List<Event> getOtcamEvents() {
        List<Event> events = new ArrayList<>();
        Connection connection = getConnection();
        if (connection == null) {
            return events;
        }

        try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM otcam_event")) {
            while (resultSet.next() && !cancel) {
                int eventSite = resultSet.getInt(EVENT_SITE_COLUMN);
                int eventApp = resultSet.getInt(EVENT_APP_COLUMN);
                int eventId = resultSet.getInt(EVENT_ID_COLUMN);
                EntityId eventEntityId = new EntityId(eventSite, eventApp, eventId);

                long timestamp = resultSet.getLong(TIMESTAMP_COLUMN) / TenaUtility.MILLI_NANO;
                EventType eventType = EventType.getEnum(resultSet.getInt("event_type"));
                EventOrigin eventOrigin = EventOrigin.getEnum(resultSet.getInt("event_origin"));
                EventFrom eventFrom = EventFrom.getEnum(resultSet.getInt("event_from"));

                AdminAction adminAction = null;
                int adminActionValue = resultSet.getInt("admin_action");
                if (!resultSet.wasNull()) {
                    adminAction = AdminAction.getEnum(adminActionValue);
                }

                int targetSite = resultSet.getInt("target_site");
                int targetApp = resultSet.getInt("target_app");
                int targetId = resultSet.getInt("target_id");
                EntityId targetEntityId = new EntityId(targetSite, targetApp, targetId);

                int shooterSite = resultSet.getInt(SHOOTER_SITE_COLUMN);
                int shooterApp = resultSet.getInt(SHOOTER_APP_COLUMN);
                int shooterId = resultSet.getInt(SHOOTER_ID_COLUMN);
                EntityId shooterEntityId = new EntityId(shooterSite, shooterApp, shooterId);

                EntityType munitionType = EntityType.fromString(resultSet.getString(MUNITION_TYPE_COLUMN));
                String munitionName = resultSet.getString("munition_name");

                boolean isKillCatastrophic = resultSet.getBoolean("is_kill_catastrophic");
                boolean isKillFirepower = resultSet.getBoolean("is_kill_firepower");
                boolean isKillMobility = resultSet.getBoolean("is_kill_mobility");
                boolean isKillCommunication = resultSet.getBoolean("is_kill_communication");
                boolean isSuppressed = resultSet.getBoolean("is_suppressed");
                boolean isHit = resultSet.getBoolean("is_hit");
                boolean isMissed = resultSet.getBoolean("is_missed");
                int damagePercent = resultSet.getInt("damage_percent");
                RtcaState rtcaState = new RtcaState(isKillCatastrophic, isKillFirepower, isKillMobility,
                        isKillCommunication, isSuppressed, false, isHit, isMissed, "", damagePercent);
                if (eventType == EventType.ENGAGE && !isKillCatastrophic && !isKillFirepower && !isKillMobility
                        && !isSuppressed && !isHit && !isMissed) {
                    continue;
                }

                OtcamEvent otcamEvent = new OtcamEvent(timestamp, eventEntityId, eventType, eventOrigin, eventFrom, adminAction,
                        targetEntityId, shooterEntityId, munitionType, munitionName, rtcaState);
                events.add(otcamEvent);
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return events;
    }

    public long getStartTime() {
        long startTime = 0;
        Connection connection = getConnection();
        if (connection == null) {
            return startTime;
        }

        String sql = "SELECT MIN(timestamp) FROM entity_discovery";
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                startTime = resultSet.getLong(1) / TenaUtility.MILLI_NANO;
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return startTime;
    }

    public long getStopTime() {
        long stopTime = -1;
        Connection connection = getConnection();
        if (connection == null) {
            return stopTime;
        }

        String sql = "SELECT MAX(timestamp) FROM entity_destruction";
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                stopTime = resultSet.getLong(1) / TenaUtility.MILLI_NANO;
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return stopTime;
    }

    public List<Long> getStartStopTime() {
        List<Long> startStopTime = new ArrayList<>();
        Connection connection = getConnection();
        if (connection == null) {
            return startStopTime;
        }

        String sql = "SELECT MIN(discovery.timestamp), MAX(destruction.timestamp)" +
                " FROM entity_discovery discovery, entity_destruction destruction";
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                startStopTime.add(resultSet.getLong(1) / TenaUtility.MILLI_NANO);
                startStopTime.add(resultSet.getLong(2) / TenaUtility.MILLI_NANO);
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return startStopTime;
    }

    public boolean isDlm(EntityId entityId) {
        Connection connection = getConnection();
        if (connection == null) {
            return false;
        }

        String sql = String.format("SELECT 1 FROM dlm_message WHERE site=%d AND app=%d AND id=%d",
                entityId.getSite(), entityId.getApplication(), entityId.getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return false;
    }

    public void getDlmMessages(IDlmEntity entity, int sourceId) {
        Connection connection = getConnection();
        if (connection == null) {
            return;
        }

        String sql = String.format("SELECT * FROM dlm_message WHERE site=%d AND app=%d AND id=%d ORDER BY %s",
                entity.getEntityId().getSite(), entity.getEntityId().getApplication(), entity.getEntityId().getId(),
                TIMESTAMP_COLUMN);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next() && !cancel) {
                long timestamp = resultSet.getLong(TIMESTAMP_COLUMN) / TenaUtility.MILLI_NANO;
                int messageType = resultSet.getInt("message_type");
                int time = resultSet.getInt("time");
                boolean overrideTimestamp = resultSet.getBoolean("timestamp_override");
                byte[] data = resultSet.getBytes("data");
                DlmMessage dlmMessage = DlmMessageFactory.createMessage(messageType, data);
                if (entity.processMessage(dlmMessage, timestamp)) {
                    DatabaseLogger.addDlmMessage(new GenericDlmMessage(messageType, time, data, timestamp, overrideTimestamp),
                            entity.getEntityId(), sourceId);
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    public void importFile(int sourceId) {
        List<EntityId> entityIds = getEntityIds();
        for (EntityId entityId : entityIds) {
            IEntity entity = EntityManager.getEntity(entityId);
            if (entity == null) {
                entity = new PlaybackEntity(entityId);
                EntityManager.addEntity(entity, true);
            }
            getEntityScopes(entity, sourceId);
            getEntityDetails(entity, sourceId);
            getTspi(entity, sourceId);
        }
        importEvents(sourceId);
    }

    public void importEvents(int sourceId) {
        for (Event event : getEvents()) {
            EventManager.addEvent(event);
            if (sourceId >= 0) {
                DatabaseLogger.addEvent(event, sourceId);
            }
            if (event instanceof OtcamEvent otcamEvent) {
                IEntity target = EntityManager.getEntity(otcamEvent.getTargetId());
                if (target != null) {
                    target.addEvent(otcamEvent);
                }
            } else if (event instanceof MunitionFireEvent munitionFireEvent) {
                IEntity shooter = EntityManager.getEntity(munitionFireEvent.getShooterId());
                if (shooter != null) {
                    shooter.addEvent(munitionFireEvent);
                }
            }
        }
    }
}
