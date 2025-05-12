package gov.mil.otc._3dvis.data.database;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.database.table.*;
import gov.mil.otc._3dvis.data.database.table.blackhawk.BlackHawkDataProxy;
import gov.mil.otc._3dvis.data.database.table.blackhawk.BlackHawkFlightDataTable;
import gov.mil.otc._3dvis.data.database.table.nbcrv.NbcrvDataProxy;
import gov.mil.otc._3dvis.data.database.table.staticentity.StaticProxy;
import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.data.oadms.WdlReading;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityScope;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.datamodel.aircraft.TspiExtendedData;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.mil.otc._3dvis.event.MunitionDetonationEvent;
import gov.mil.otc._3dvis.event.MunitionFireEvent;
import gov.mil.otc._3dvis.event.otcam.OtcamEvent;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.playback.Playback;
import gov.mil.otc._3dvis.project.blackhawk.CcmEvent;
import gov.mil.otc._3dvis.project.blackhawk.FlightData;
import gov.mil.otc._3dvis.project.blackhawk.SurveyData;
import gov.mil.otc._3dvis.project.dlm.message.GenericDlmMessage;
import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvState;
import gov.mil.otc._3dvis.project.nbcrv.RadNucState;
import gov.mil.otc._3dvis.settings.Defaults;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to manage the database.
 */
public class Database {

    private static final String DATABASE_DRIVER = "org.sqlite.JDBC";
    private static final String DATABASE_VERSION = "1.3";
    private final String url;
    private final DataSourceTable dataSourceTable = new DataSourceTable();
    private final IterationTable iterationTable = new IterationTable();
    private final PlaybackTable playbackTable = new PlaybackTable();
    private final MissionTable missionTable = new MissionTable();
    private final MissionDataSourceTable missionDataSourceTable = new MissionDataSourceTable();
    private final AppIdTable appIdTable = new AppIdTable();
    private final EntityTable entityTable = new EntityTable();
    private final EntityIdTable entityIdTable = new EntityIdTable();
    private final EntityScopeTable entityScopeTable = new EntityScopeTable();
    private final EntityDetailTable entityDetailTable = new EntityDetailTable();
    private final TspiDataTable tspiTable = new TspiDataTable();
    private final TspiExtendedDataTable tspiExtendedTable = new TspiExtendedDataTable();
    private final UasPayloadDataTable uasPayloadTable = new UasPayloadDataTable();
    private final BlackHawkFlightDataTable blackHawkFlightDataTable = new BlackHawkFlightDataTable();
    private final OtcamEventTable otcamEventTable = new OtcamEventTable();
    private final MunitionFireTable munitionFireTable = new MunitionFireTable();
    private final MunitionDetonationTable munitionDetonationTable = new MunitionDetonationTable();
    private final MediaTable mediaTable = new MediaTable();
    private final TimedFileTable timedFileTable = new TimedFileTable();
    private final DlmMessageTable dlmMessageTable = new DlmMessageTable();
    private final C2MessageTable c2MessageTable = new C2MessageTable();
    private final BlackHawkDataProxy blackHawkDataProxy = new BlackHawkDataProxy();
    private final NbcrvDataProxy nbcrvDataProxy = new NbcrvDataProxy();
    private final StaticProxy staticProxy = new StaticProxy();
    private final Object databaseWriteLock = new Object();
    private final List<AbstractDataTable<?>> databaseObjectTableList = new ArrayList<>();
    private Connection databaseConnection = null;

    private Database(String databasePath) {
        url = "jdbc:sqlite:" + databasePath;

        databaseObjectTableList.add(entityTable);
        databaseObjectTableList.add(entityIdTable);
        databaseObjectTableList.add(entityScopeTable);
        databaseObjectTableList.add(entityDetailTable);
        databaseObjectTableList.add(tspiTable);
        databaseObjectTableList.add(tspiExtendedTable);
        databaseObjectTableList.add(uasPayloadTable);
        databaseObjectTableList.add(blackHawkFlightDataTable);
        databaseObjectTableList.add(otcamEventTable);
        databaseObjectTableList.add(munitionFireTable);
        databaseObjectTableList.add(munitionDetonationTable);
        databaseObjectTableList.add(mediaTable);
        databaseObjectTableList.add(timedFileTable);
        databaseObjectTableList.add(dlmMessageTable);
        databaseObjectTableList.add(c2MessageTable);
    }

    /**
     * Creates the database management object.  Creates a new database file if it does not exist.
     *
     * @param databasePath The database path.
     * @return The database object.
     */
    public static Database createAndVerify(String databasePath) {
        Database database = new Database(databasePath);

        File file = new File(databasePath);
        if (!file.exists()) {
            database.createDatabase(databasePath);
        }

        if (!database.verifyVersion()) {
            database.createDatabase(databasePath);
        }

        return database;
    }

    /**
     * Closes the database connection if its open.
     */
    public void closeDatabase() {
        Logger.getGlobal().log(Level.INFO, "closing database");
        try {
            if (databaseConnection != null && !databaseConnection.isClosed()) {
                databaseConnection.close();
                Logger.getGlobal().log(Level.INFO, "database closed");
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Could not close the database.", e);
        }
    }

    /**
     * Insert data to the database.
     */
    public void insertData() {
        Connection connection = getConnection();
        if (connection == null) {
            return;
        }

        synchronized (databaseWriteLock) {
            for (AbstractDataTable<?> table : databaseObjectTableList) {
                table.insertToDatabase(connection);
            }
        }
    }

    public int getOrCreateAppId(String name) {
        int id = appIdTable.getId(getConnection(), name);
        if (id == 0) {
            synchronized (databaseWriteLock) {
                id = appIdTable.createId(getConnection(), name);
            }
        }
        return id;
    }

    public EntityId getNextAvailableEntityId(String appName) {
        int appId = getOrCreateAppId(appName);
        return getNextAvailableEntityId(appId);
    }

    public EntityId getNextAvailableEntityId(int appId) {
        synchronized (databaseWriteLock) {
            return entityIdTable.getAndAddNextAvailableId(getConnection(), Defaults.SITE_APP_ID_3DVIS, appId);
        }
    }

    public void removeEntity(IEntity entity) {
        synchronized (databaseWriteLock) {
            entityIdTable.removeEntityId(getConnection(), entity.getEntityId());
            entityDetailTable.remove(getConnection(), entity.getEntityId());
            entityScopeTable.remove(getConnection(), entity.getEntityId());
            tspiTable.remove(getConnection(), entity.getEntityId());
        }
    }

    public DataSource createDataSource(String name, long startTime, long stopTime) {
        synchronized (databaseWriteLock) {
            return dataSourceTable.createDataSource(getConnection(), name, startTime, stopTime, true);
        }
    }

    public List<DataSource> getDataSources() {
        return dataSourceTable.getDataSources(getConnection());
    }

    public void removeDataSources(List<Integer> sourceIds) {
        synchronized (databaseWriteLock) {
            for (AbstractDataTable<?> table : databaseObjectTableList) {
                if (table instanceof DatabaseObjectTable<?>) {
                    ((DatabaseObjectTable<?>) table).remove(getConnection(), sourceIds);
                }
            }

            blackHawkDataProxy.remove(getConnection(), sourceIds);

            dataSourceTable.removeDataSources(getConnection(), sourceIds);
        }
    }

    public void updateDataSource(DataSource dataSource) {
        synchronized (databaseWriteLock) {
            dataSourceTable.updateDataSource(getConnection(), dataSource);
        }
    }

    public void add(IEntity entity) {
        entityTable.add(entity);
    }

    public void add(EntityId entityId) {
        entityIdTable.add(entityId);
    }

    public void add(EntityScope entityScope, EntityId entityId, int sourceId) {
        entityScopeTable.add(new DatabaseObject<>(entityScope, entityId, sourceId));
    }

    public void add(EntityDetail entityDetail, EntityId entityId, int sourceId) {
        entityDetailTable.add(new DatabaseObject<>(entityDetail, entityId, sourceId));
    }

    public void add(TspiData tspiData, EntityId entityId, int sourceId) {
        tspiTable.add(new DatabaseObject<>(tspiData, entityId, sourceId));
    }

    public void add(TspiExtendedData tspiExtendedData, EntityId entityId, int sourceId) {
        tspiExtendedTable.add(new DatabaseObject<>(tspiExtendedData, entityId, sourceId));
    }

    public void add(UasPayloadData uasPayloadData, EntityId entityId, int sourceId) {
        uasPayloadTable.add(new DatabaseObject<>(uasPayloadData, entityId, sourceId));
    }

    public void add(FlightData flightData, EntityId entityId, int sourceId) {
        blackHawkFlightDataTable.add(new DatabaseObject<>(flightData, entityId, sourceId));
    }

    public void add(OtcamEvent otcamEvent, int sourceId) {
        otcamEventTable.add(new DatabaseObject<>(otcamEvent, sourceId));
    }

    public void add(MunitionFireEvent munitionFireEvent, int sourceId) {
        munitionFireTable.add(new DatabaseObject<>(munitionFireEvent, sourceId));
    }

    public void add(MunitionDetonationEvent munitionDetonationEvent, int sourceId) {
        munitionDetonationTable.add(new DatabaseObject<>(munitionDetonationEvent, sourceId));
    }

    public void add(EntityId entityId, CcmEvent ccmEvent, int sourceId) {
        synchronized (databaseWriteLock) {
            blackHawkDataProxy.addCcmEvent(getConnection(), entityId, ccmEvent, sourceId);
        }
    }

    public DataSource add(MediaFile mediaFile, EntityId entityId) {
        DataSource dataSource = createDataSource(mediaFile.getAbsolutePath(), mediaFile.getStartTime(), mediaFile.getStopTime());
        DataManager.addDataSource(dataSource);
        mediaTable.add(new DatabaseObject<>(mediaFile, entityId, dataSource.getId()));
        return dataSource;
    }

    public DataSource add(TimedFile timedFile, EntityId entityId) {
        DataSource dataSource = createDataSource(timedFile.getFile().getAbsolutePath(), timedFile.getTimestamp(), 0);
        DataManager.addDataSource(dataSource);
        synchronized (databaseWriteLock) {
            timedFileTable.add(new DatabaseObject<>(timedFile, entityId, dataSource.getId()));
        }
        return dataSource;
    }

    public void add(GenericDlmMessage genericDlmMessage, EntityId entityId, int sourceId) {
        dlmMessageTable.add(new DatabaseObject<>(genericDlmMessage, entityId, sourceId));
    }

    public void add(C2MessageEvent c2MessageEvent, EntityId entityId, int sourceId) {
        c2MessageTable.add(new DatabaseObject<>(c2MessageEvent, entityId, sourceId));
    }

    public void add(Device device, EntityId entityId, int sourceId) {
        synchronized (databaseWriteLock) {
            nbcrvDataProxy.addDevice(getConnection(), device, entityId, sourceId);
        }
    }

    public void add(NbcrvDetection nbcrvDetection, EntityId entityId, int sourceId) {
        synchronized (databaseWriteLock) {
            nbcrvDataProxy.addEvent(getConnection(), nbcrvDetection, entityId, sourceId);
        }
    }

    public void add(List<NbcrvDetection> nbcrvDetections, EntityId entityId, int sourceId) {
        synchronized (databaseWriteLock) {
            nbcrvDataProxy.addEvents(getConnection(), nbcrvDetections, entityId, sourceId);
        }
    }

    public void addNbcrvStates(List<NbcrvState> nbcrvStates, EntityId entityId, int sourceId) {
        synchronized (databaseWriteLock) {
            nbcrvDataProxy.addNbcrvStates(getConnection(), nbcrvStates, entityId, sourceId);
        }
    }

    public void addRadNucStates(List<RadNucState> radNucStates, EntityId entityId, int sourceId) {
        synchronized (databaseWriteLock) {
            nbcrvDataProxy.addRadNucStates(getConnection(), radNucStates, entityId, sourceId);
        }
    }

    public void addWdlReadings(List<WdlReading> wdlReadings, EntityId entityId, int sourceId) {
        synchronized (databaseWriteLock) {
            nbcrvDataProxy.addWdlReadings(getConnection(), wdlReadings, entityId, sourceId);
        }
    }

    public List<EntityId> getEntityIds() {
        return entityIdTable.getEntityIds(getConnection());
    }

    public Class<?> getClassType(EntityId entityId) {
        return entityTable.getClassType(getConnection(), entityId);
    }

    public List<EntityScope> getEntityScopes(EntityId entityId, List<Integer> sourceIds) {
        return entityScopeTable.getEntityScopes(getConnection(), entityId, sourceIds);
    }

    public List<EntityDetail> getEntityDetails(EntityId entityId, List<Integer> sourceIds) {
        return entityDetailTable.getEntityDetails(getConnection(), entityId, sourceIds);
    }

    public List<TspiData> getTspi(EntityId entityId, List<Integer> sourceIds) {
        return tspiTable.getTspi(getConnection(), entityId, sourceIds);
    }

    public List<TspiExtendedData> getTspiExtendedData(EntityId entityId, List<Integer> sourceIds) {
        return tspiExtendedTable.getTspiExtendedData(getConnection(), entityId, sourceIds);
    }

    public List<UasPayloadData> getUasPayloadData(EntityId entityId, List<Integer> sourceIds) {
        return uasPayloadTable.getUasPayloadData(getConnection(), entityId, sourceIds);
    }

    public List<FlightData> getFlightData(EntityId entityId, List<Integer> sourceIds) {
        return blackHawkFlightDataTable.getFlightData(getConnection(), entityId, sourceIds);
    }

    public List<CcmEvent> getCcmEvents() {
        return blackHawkDataProxy.getCcmEvents(getConnection());
    }

    public List<OtcamEvent> getOtcamEvents(List<Integer> sourceIds) {
        return otcamEventTable.getEvents(getConnection(), sourceIds);
    }

    public List<MunitionFireEvent> getMunitionFireEvents(List<Integer> sourceIds) {
        return munitionFireTable.getEvents(getConnection(), sourceIds);
    }

    public List<MunitionDetonationEvent> getMunitionDetonationEvents(List<Integer> sourceIds) {
        return munitionDetonationTable.getEvents(getConnection(), sourceIds);
    }

    public List<MediaFile> getMedia(EntityId entityId) {
        return mediaTable.getMedia(getConnection(), entityId);
    }

    public List<TimedFile> getTimedFiles() {
        return timedFileTable.getFiles(getConnection());
    }

    public List<TimedFile> getTimedFiles(EntityId entityId) {
        return timedFileTable.getFiles(getConnection(), entityId);
    }

    public List<GenericDlmMessage> getGenericDlmMessage(EntityId entityId, List<Integer> sourceIds) {
        return dlmMessageTable.getMessages(getConnection(), entityId, sourceIds);
    }

    public List<C2MessageEvent> getC2MessageEvents(IEntity entity, List<Integer> sourceIds) {
        return c2MessageTable.getC2MessageEvent(getConnection(), entity, sourceIds);
    }

    public List<NbcrvState> getNbcrvStates(EntityId entityId, List<Integer> sourceIds) {
        return nbcrvDataProxy.getNbcrvStates(getConnection(), entityId, sourceIds);
    }

    public List<RadNucState> getRadNucStates(EntityId entityId, List<Integer> sourceIds) {
        return nbcrvDataProxy.getRadNucStates(getConnection(), entityId, sourceIds);
    }

    public List<Device> getNbcrvDevices(EntityId entityId, List<Integer> sourceIds) {
        return nbcrvDataProxy.getDevices(getConnection(), entityId, sourceIds);
    }

    public List<NbcrvDetection> getNbcrvEvents(EntityId entityId, List<Integer> sourceIds) {
        return nbcrvDataProxy.getEvents(getConnection(), entityId, sourceIds);
    }

    public List<WdlReading> getWdlReadings(EntityId entityId, List<Integer> sourceIds) {
        return nbcrvDataProxy.getWdlReadings(getConnection(), entityId, sourceIds);
    }

    public void addIteration(Iteration iteration) {
        synchronized (databaseWriteLock) {
            iterationTable.addIteration(getConnection(), iteration);
        }
    }

    public void updateIteration(Iteration oldIteration, Iteration newIteration) {
        synchronized (databaseWriteLock) {
            iterationTable.updateIteration(getConnection(), oldIteration, newIteration);
        }
    }

    public void removeIteration(String name) {
        synchronized (databaseWriteLock) {
            iterationTable.removeIteration(getConnection(), name);
        }
    }

    public List<Iteration> getIterations() {
        return iterationTable.getIterations(getConnection());
    }

    public boolean addPlayback(Playback playback) {
        synchronized (databaseWriteLock) {
            return playbackTable.addPlayback(getConnection(), playback);
        }
    }

    public List<Playback> getPlaybackList() {
        return playbackTable.getPlaybackList(getConnection());
    }

    public void removePlayback(Playback playback) {
        playbackTable.removePlayback(getConnection(), playback);
    }

    public void addMission(Mission mission) {
        synchronized (databaseWriteLock) {
            missionTable.addMission(getConnection(), mission);
        }
    }

    public List<Mission> getMissions() {
        return missionTable.getMissions(getConnection());
    }

    public void removeMission(Mission mission) {
        synchronized (databaseWriteLock) {
            missionTable.removeMission(getConnection(), mission);
            missionDataSourceTable.removeMission(getConnection(), mission);
        }
    }

    public void addMissionDataSource(Mission mission, DataSource dataSource) {
        synchronized (databaseWriteLock) {
            missionDataSourceTable.addDataSource(getConnection(), mission, dataSource);
        }
    }

    public void removeMissionDataSource(Mission mission, DataSource dataSource) {
        synchronized (databaseWriteLock) {
            missionDataSourceTable.removeDataSource(getConnection(), mission, dataSource);
        }
    }

    public List<Integer> getMissionDataSourceIds(Mission mission) {
        return missionDataSourceTable.getDataSourceIds(getConnection(), mission);
    }

    public void addCrewMissionData(String mission, int flightNumber, int tailNumber, String pin,
                                   String seat, String role, long startTime, long endTime, int sourceId) {
        synchronized (databaseWriteLock) {
            blackHawkDataProxy.addCrewMissionData(getConnection(), mission, flightNumber, tailNumber, pin, seat, role,
                    startTime, endTime, sourceId);
        }
    }

    public void addSurveyData(String mission, int tailNumber, String pin, String seat,
                              String role, String question, String answer, int sourceId) {
        synchronized (databaseWriteLock) {
            blackHawkDataProxy.addSurveyData(getConnection(), mission, tailNumber, pin, seat, role, question,
                    answer, sourceId);
        }
    }

    public List<String> getRoles(Mission mission, int tailNumber) {
        return blackHawkDataProxy.getRoles(getConnection(), mission, tailNumber);
    }

    public List<SurveyData> getSurveyData(Mission mission, int tailNumber, String role) {
        return blackHawkDataProxy.getSurveyData(getConnection(), mission, tailNumber, role);
    }

    public void addEntityImage(EntityId entityId, BufferedImage bufferedImage) {
        synchronized (databaseWriteLock) {
            staticProxy.addEntityImage(getConnection(), entityId, bufferedImage);
        }
    }

    public BufferedImage getEntityImage(EntityId entityId) {
        return staticProxy.getEntityImage(getConnection(), entityId);
    }

    public void removeEntityImage(EntityId entityId) {
        synchronized (databaseWriteLock) {
            staticProxy.removeEntityImage(getConnection(), entityId);
        }
    }

    private boolean createDatabase(String databasePath) {
        return initializeDatabaseConnection(databasePath) &&
                checkForOldVersion(databasePath) &&
                initializeTables();
    }

    /**
     * Create database file if not exists.  Initialize database connection.
     *
     * @param databasePath The database path.
     * @return True if initialized successfully.
     */
    private boolean initializeDatabaseConnection(String databasePath) {
        try {
            File file = new File(databasePath);
            if (!file.exists() && !file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                return false;
            }
        } catch (Exception e) {
            String message = String.format("Could not open or create database %s.", databasePath);
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }

        try {
            Class.forName(DATABASE_DRIVER);
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.WARNING, "Could not load database driver.", e);
            return false;
        }

        return getConnection() != null;
    }

    private boolean verifyVersion() {
        try {
            Class.forName(DATABASE_DRIVER);
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.WARNING, "Could not load database driver.", e);
            return false;
        }

        Connection connection = getConnection();
        if (DatabaseVersionTable.verifyDatabaseVersion(connection, DATABASE_VERSION)) {
            return true;
        } else {
            closeDatabase();
            return false;
        }
    }

    /**
     * Check if current file is an old database.
     *
     * @param databasePath The database path.
     * @return True if database is ready, false if error occurred.
     */
    private boolean checkForOldVersion(String databasePath) {
        Connection connection = getConnection();
        if (DatabaseVersionTable.verifyDatabaseVersion(connection, DATABASE_VERSION)) {
            return true;
        } else {
            closeDatabase();
            try {
                Files.delete(Paths.get(databasePath));
            } catch (IOException e) {
                Logger.getGlobal().log(Level.WARNING, "Could not delete old database file.", e);
                return false;
            }
            return initializeDatabaseConnection(databasePath);
        }
    }

    /**
     * Initializes the database tables.
     *
     * @return True if initialization occurred successfully.
     */
    private boolean initializeTables() {
        Connection connection = getConnection();
        if (!DatabaseVersionTable.createIfNotExists(connection, DATABASE_VERSION)) {
            return false;
        }

        boolean successful;

        successful = dataSourceTable.createTable(connection);
        successful &= iterationTable.createTable(connection);
        successful &= playbackTable.createTable(connection);
        successful &= missionTable.createTable(connection);
        successful &= missionDataSourceTable.createTable(connection);
        successful &= appIdTable.createTable(connection);

        for (AbstractBaseTable table : databaseObjectTableList) {
            successful &= table.createTable(connection);
        }

        return successful;
    }

    /**
     * Gets the database connection.
     *
     * @return The database connection, null if error establishing connection.
     */
    private Connection getConnection() {
        try {
            if (databaseConnection == null || databaseConnection.isClosed()) {
                databaseConnection = DriverManager.getConnection(url);
                databaseConnection.setAutoCommit(false);
            }
        } catch (Exception e) {
            String message = String.format("Could not create database connection for %s. ", url);
            Logger.getGlobal().log(Level.SEVERE, message, e);
            databaseConnection = null;
        }
        return databaseConnection;
    }
}
