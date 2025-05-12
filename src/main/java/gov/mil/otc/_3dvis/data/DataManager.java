package gov.mil.otc._3dvis.data;

import gov.mil.otc._3dvis.data.database.Database;
import gov.mil.otc._3dvis.data.mission.IMissionUpdateListener;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.data.report.ReportManager;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityScope;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.datamodel.aircraft.TspiExtendedData;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.staticentity.StaticEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.event.MunitionDetonationEvent;
import gov.mil.otc._3dvis.event.MunitionFireEvent;
import gov.mil.otc._3dvis.event.otcam.OtcamEvent;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.playback.Playback;
import gov.mil.otc._3dvis.project.avcad.LidarEntity;
import gov.mil.otc._3dvis.project.avcad.SensorEntity;
import gov.mil.otc._3dvis.project.blackhawk.BlackHawkEntity;
import gov.mil.otc._3dvis.project.blackhawk.CcmEvent;
import gov.mil.otc._3dvis.project.blackhawk.SurveyData;
import gov.mil.otc._3dvis.project.dlm.DlmEntity;
import gov.mil.otc._3dvis.project.dlm.DlmPlaybackEntity;
import gov.mil.otc._3dvis.project.dlm.IDlmEntity;
import gov.mil.otc._3dvis.project.dlm.message.DlmMessage;
import gov.mil.otc._3dvis.project.dlm.message.DlmMessageFactory;
import gov.mil.otc._3dvis.project.dlm.message.GenericDlmMessage;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvEntity;
import gov.mil.otc._3dvis.project.nbcrv.SidecarEntity;
import gov.mil.otc._3dvis.project.nbcrv.WdlEntity;
import gov.mil.otc._3dvis.project.rpuas.RpuasEntity;
import gov.mil.otc._3dvis.project.shadow.ShadowEntity;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.main.MainApplication;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager {

    private static final DataManager SINGLETON = new DataManager();
    private static final AtomicInteger GENERIC_ID_COUNTER = new AtomicInteger(1);
    private final List<DataSource> dataSourceList = new ArrayList<>();
    private final List<Mission> missionList = new ArrayList<>();
    private final List<IMissionUpdateListener> missionUpdateListenerList = Collections.synchronizedList(new ArrayList<>());
    private Database database;
    private IterationManager iterationManager;
    private ReportManager reportManager;
    private boolean initialized = false;
    private DataSource realTimeDataSource = null;
    private Playback currentPlayback = null;

    private DataManager() {
    }

    public static boolean initialize() {
        return SINGLETON.doInitialize();
    }

    public static void shutdown() {
        SINGLETON.doShutdown();
    }

    public static void startStopRealTimeLogging(boolean enable) {
        if (enable) {
            DatabaseLogger.createAndStart(SINGLETON.database);
        } else {
            DatabaseLogger.shutdown();
        }
    }

    public static ReportManager getReportManager() {
        return SINGLETON.reportManager;
    }

    public static DataSource getOrCreateRealTimeDataSource() {
        if (SINGLETON.realTimeDataSource == null) {
            SINGLETON.realTimeDataSource = SINGLETON.database.createDataSource("Real Time",
                    System.currentTimeMillis(), -1);
        }
        return SINGLETON.realTimeDataSource;
    }

    public static void startRealTimeDataSource() {
        getOrCreateRealTimeDataSource();
    }

    public static void stopRealTimeDataSource() {
        DataSource dataSource = getOrCreateRealTimeDataSource();
        DataSource dataSourceStop = new DataSource(dataSource.getId(), dataSource.getName(),
                dataSource.getStartTime(), System.currentTimeMillis(), dataSource.isUse());
        SINGLETON.database.updateDataSource(dataSourceStop);
        SINGLETON.realTimeDataSource = null;
    }

    public static void addDataSource(DataSource dataSource) {
        SINGLETON.addDataSourceToList(dataSource);
    }

    public static DataSource createDataSource(String name, long startTime, long stopTime) {
        return SINGLETON.doCreateDataSource(name, startTime, stopTime);
    }

    public static List<DataSource> getDataSources() {
        return new ArrayList<>(SINGLETON.dataSourceList);
    }

    public static void removeDataSources(List<Integer> dataSourceIds) {
        SINGLETON.doRemoveDataSources(dataSourceIds);
    }

    public static void updateDataSource(DataSource dataSource) {
        SINGLETON.doUpdateDataSource(dataSource);
    }

    public static void reloadData() {
        SINGLETON.loadData();
    }

    public static IterationManager getIterationManager() {
        return SINGLETON.iterationManager;
    }

    public static void setCurrentPlayback(Playback playback) {
        SINGLETON.currentPlayback = playback;
        if (playback != null) {
            SettingsManager.getPreferences().setLastPlayback(playback.getName());
        } else {
            SettingsManager.getPreferences().setLastPlayback("");
        }
    }

    public static Playback getCurrentPlayback() {
        return SINGLETON.currentPlayback;
    }

    public static boolean addPlayback(Playback playback) {
        return SINGLETON.database.addPlayback(playback);
    }

    public static List<Playback> getPlaybackList() {
        List<Playback> playbacks = getLocalPlaybackList();
        playbacks.addAll(SINGLETON.database.getPlaybackList());
        return playbacks;
    }

    private static List<Playback> getLocalPlaybackList() {
        List<Playback> playbacks = new ArrayList<>();
        File playbackFolder = new File("playback");
        if (!playbackFolder.exists()) {
            return playbacks;
        }
        File[] files = playbackFolder.listFiles();
        if (files == null || files.length == 0) {
            return playbacks;
        }
        for (File file : files) {
            Playback playback = new Playback(file.getName(), file, file.lastModified());
            playbacks.add(playback);
        }
        return playbacks;
    }

    public static void removePlayback(Playback playback) {
        SINGLETON.database.removePlayback(playback);
    }

    public static void addMissionUpdateListener(IMissionUpdateListener missionUpdateListener) {
        SINGLETON.missionUpdateListenerList.add(missionUpdateListener);
    }

    public static void addMission(Mission mission) {
        addMission(mission, false);
    }

    public static void addMission(Mission mission, boolean saveToDatabase) {
        SINGLETON.doAddMission(mission, saveToDatabase);
    }

    public static List<Mission> getCurrentMissions() {
        List<Mission> missions = new ArrayList<>();
        for (Mission mission : SINGLETON.missionList) {
            if (mission.inMission(TimeManager.getTime())) {
                missions.add(mission);
            }
        }
        return missions;
    }

    public static List<Mission> getMissions() {
        return new ArrayList<>(SINGLETON.missionList);
    }

    public static void removeMission(Mission mission) {
        SINGLETON.doRemoveMission(mission);
    }

    public static void removeAllMissions() {
        List<Mission> missions = getMissions();
        for (Mission mission : missions) {
            SINGLETON.doRemoveMission(mission);
        }
    }

    public static void addMissionDataSource(Mission mission, DataSource dataSource) {
        SINGLETON.database.addMissionDataSource(mission, dataSource);
    }

    public static void removeMissionDataSource(Mission mission, DataSource dataSource) {
        SINGLETON.database.removeMissionDataSource(mission, dataSource);
    }

    public static List<Integer> getMissionDataSourceIds(Mission mission) {
        return SINGLETON.database.getMissionDataSourceIds(mission);
    }

    public static int getOrCreateAppId(String name) {
        return SINGLETON.database.getOrCreateAppId(name);
    }

    public static EntityId getNextGenericEntityId() {
        return new EntityId(Defaults.SITE_APP_ID_3DVIS, Defaults.APP_ID_GENERIC, GENERIC_ID_COUNTER.getAndIncrement());
    }

    public static EntityId getNextAvailableEntityId(String appName) {
        int appId = getOrCreateAppId(appName);
        return SINGLETON.database.getNextAvailableEntityId(appId);
    }

    public static EntityId getNextAvailableEntityId(int appId) {
        return SINGLETON.database.getNextAvailableEntityId(appId);
    }

    public static void removeEntity(IEntity entity) {
        SINGLETON.database.removeEntity(entity);
    }

    public static List<String> getRoles(Mission mission, int tailNumber) {
        return SINGLETON.database.getRoles(mission, tailNumber);
    }

    public static List<SurveyData> getSurveyData(Mission mission, int tailNumber, String role) {
        return SINGLETON.database.getSurveyData(mission, tailNumber, role);
    }

    public static List<TimedFile> getTimedFileList() {
        return SINGLETON.database.getTimedFiles();
    }

    public static List<TimedFile> getTimedFileList(EntityId entityId) {
        return SINGLETON.database.getTimedFiles(entityId);
    }

    public static void addEntityImage(EntityId entityId, BufferedImage bufferedImage) {
        SINGLETON.database.addEntityImage(entityId, bufferedImage);
    }

    private synchronized boolean doInitialize() {
        if (!initialized) {
            String databasePath = System.getProperty("user.home") + File.separator + ".3dvis\\database\\database.db";

            if (SettingsManager.getSettings().useRemoteDatabase()) {
                loadRemoteDatabase(databasePath);
            }

            database = Database.createAndVerify(databasePath);
            iterationManager = new IterationManager(database);
            reportManager = new ReportManager(database);

            loadDataSourceList();
            if (SettingsManager.getSettings().isLoadDatabaseOnStartup()) {
                loadData();
            } else {
                synchronized (dataSourceList) {
                    for (DataSource dataSource : dataSourceList) {
                        dataSource.setUse(false);
                        database.updateDataSource(dataSource);
                    }
                }
            }

            if (SettingsManager.getSettings().isRealTimeLogging()) {
                DatabaseLogger.createAndStart(database);
            }

            initialized = true;
        }

        return true;
    }

    private void loadRemoteDatabase(String databasePath) {
        File remoteDatabase = new File(SettingsManager.getSettings().getRemoteDatabase());
        File localDatabase = new File(databasePath);
        if (isSameFile(remoteDatabase, localDatabase)) {
            return;
        }
        if (remoteDatabase.exists()) {
            try {
                if (localDatabase.exists()) {
                    Files.delete(localDatabase.toPath());
                }
                Files.copy(remoteDatabase.toPath(), localDatabase.toPath());
            } catch (Exception e) {
                MainApplication.getInstance().showErrorMessage("Unable to load remote database, switching to local.");
            }
        }
    }

    private boolean isSameFile(File file1, File file2) {
        if (!file1.exists() || !file2.exists()) {
            return false;
        }
        try {
            BasicFileAttributes basicFileAttributes1 = Files.readAttributes(file1.toPath(), BasicFileAttributes.class);
            BasicFileAttributes basicFileAttributes2 = Files.readAttributes(file2.toPath(), BasicFileAttributes.class);
            if (basicFileAttributes1.size() == basicFileAttributes2.size() &&
                    basicFileAttributes1.lastModifiedTime().equals(basicFileAttributes2.lastModifiedTime())) {
                return true;
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.INFO, null, e);
        }
        return false;
    }

    private void doShutdown() {
        DatabaseLogger.shutdown();
        database.closeDatabase();
        initialized = false;
    }

    private void loadDataSourceList() {
        dataSourceList.clear();
        List<DataSource> dataSources = database.getDataSources();
        for (DataSource dataSource : dataSources) {
            addDataSourceToList(dataSource);
        }
    }

    private DataSource doCreateDataSource(String name, long startTime, long stopTime) {
        DataSource dataSource = database.createDataSource(name, startTime, stopTime);
        addDataSourceToList(dataSource);
        return dataSource;
    }

    private void addDataSourceToList(DataSource dataSource) {
        synchronized (dataSourceList) {
            for (int i = 0; i < dataSourceList.size(); i++) {
                if (dataSourceList.get(i).getStartTime() > dataSource.getStartTime()) {
                    dataSourceList.add(i, dataSource);
                    return;
                }
            }
            dataSourceList.add(dataSource);
        }
    }

    private void doRemoveDataSources(List<Integer> dataSourceIds) {
        database.removeDataSources(dataSourceIds);
        synchronized (dataSourceList) {
            dataSourceList.removeIf(dataSource -> dataSourceIds.contains(dataSource.getId()));
        }
    }

    private void doUpdateDataSource(DataSource dataSource) {
        database.updateDataSource(dataSource);
    }

    private void doAddMission(Mission mission, boolean saveToDatabase) {
        if (!missionList.contains(mission)) {
            missionList.add(mission);
        }
        if (saveToDatabase) {
            database.addMission(mission);
        }
        notifyMissionUpdate();
    }

    private void doRemoveMission(Mission mission) {
        missionList.remove(mission);
        List<Integer> sourceIds = getMissionDataSourceIds(mission);
        doRemoveDataSources(sourceIds);
        database.removeMission(mission);
        notifyMissionUpdate();
    }

    private void notifyMissionUpdate() {
        List<Mission> missions = getMissions();
        synchronized (missionUpdateListenerList) {
            for (IMissionUpdateListener missionUpdateListener : missionUpdateListenerList) {
                missionUpdateListener.onMissionUpdate(missions);
            }
        }
    }

    private List<Integer> getSourceIds() {
        List<Integer> sourceIds = new ArrayList<>();
        synchronized (dataSourceList) {
            for (DataSource dataSource : dataSourceList) {
                if (dataSource.isUse()) {
                    sourceIds.add(dataSource.getId());
                }
            }
        }
        return sourceIds;
    }

    private void loadData() {
        EntityManager.removeAllEntities();
        EventManager.removeAllEvents();

        List<Integer> sourceIds = getSourceIds();

        if (sourceIds.isEmpty()) {
            return;
        }

        for (EntityId entityId : database.getEntityIds()) {
            loadEntityData(entityId, sourceIds);
        }

        loadEventData(sourceIds);
    }

    private void loadEntityData(EntityId entityId, List<Integer> sourceIds) {
        IEntity entity = EntityManager.getEntity(entityId);
        if (entity == null) {
            entity = createEntity(entityId);
            EntityManager.addEntity(entity, false);
        }

        for (EntityScope entityScope : database.getEntityScopes(entityId, sourceIds)) {
            entity.addEntityScope(entityScope);
        }

        for (EntityDetail entityDetail : database.getEntityDetails(entityId, sourceIds)) {
            entity.addEntityDetail(entityDetail);
        }

        entity.addTspiList(database.getTspi(entityId, sourceIds));

        for (MediaFile mediaFile : database.getMedia(entityId)) {
            entity.getMediaCollection().addMediaFile(mediaFile);
        }

        if (entity instanceof BlackHawkEntity blackHawkEntity) {
            loadBlackHawkData(blackHawkEntity, sourceIds);
        }
        if (entity instanceof IDlmEntity iDlmEntity) {
            loadDlmMessages(iDlmEntity, sourceIds);
        }
        if (entity instanceof ShadowEntity shadowEntity) {
            loadShadowData(shadowEntity, sourceIds);
        }
        if (entity instanceof StaticEntity staticEntity) {
            loadStaticData(staticEntity);
        }
        if (entity instanceof NbcrvEntity nbcrvEntity) {
            loadNbcrvData(nbcrvEntity, sourceIds);
        }
        if (entity instanceof WdlEntity wdlEntity) {
            loadWdlData(wdlEntity, sourceIds);
        }

        loadC2MessageEvents(entity, sourceIds);
    }

    private IEntity createEntity(EntityId entityId) {
        Class<?> classType = database.getClassType(entityId);
        if (BlackHawkEntity.class.equals(classType)) {
            return new BlackHawkEntity(entityId);
        } else if (DlmPlaybackEntity.class.equals(classType) || DlmEntity.class.equals(classType)) {
            return new DlmPlaybackEntity(entityId);
        } else if (ShadowEntity.class.equals(classType)) {
            return new ShadowEntity(entityId);
        } else if (StaticEntity.class.equals(classType)) {
            return new StaticEntity(entityId);
        } else if (NbcrvEntity.class.equals(classType)) {
            return new NbcrvEntity(entityId);
        } else if (SidecarEntity.class.equals(classType)) {
            return new SidecarEntity(entityId);
        } else if (WdlEntity.class.equals(classType)) {
            return new WdlEntity(entityId);
        } else if (RpuasEntity.class.equals(classType)) {
            return new RpuasEntity(entityId);
        } else if (LidarEntity.class.equals(classType)) {
            return new LidarEntity(entityId);
        } else if (SensorEntity.class.equals(classType)) {
            return new SensorEntity(entityId, false);
        } else {
            return new PlaybackEntity(entityId);
        }
    }

    private void loadEventData(List<Integer> sourceIds) {
        for (OtcamEvent otcamEvent : database.getOtcamEvents(sourceIds)) {
            EventManager.addEvent(otcamEvent);
            IEntity target = EntityManager.getEntity(otcamEvent.getTargetId());
            if (target != null) {
                target.addEvent(otcamEvent);
            }
        }

        for (MunitionFireEvent munitionFireEvent : database.getMunitionFireEvents(sourceIds)) {
            EventManager.addEvent(munitionFireEvent);
        }

        for (MunitionDetonationEvent munitionDetonationEvent : database.getMunitionDetonationEvents(sourceIds)) {
            EventManager.addEvent(munitionDetonationEvent);
        }

        for (CcmEvent ccmEvent : database.getCcmEvents()) {
            EventManager.addEvent(ccmEvent);
            IEntity entity = EntityManager.getEntity(ccmEvent.getEntityId());
            if (entity != null) {
                entity.addEvent(ccmEvent);
            }
        }
    }

    private void loadBlackHawkData(BlackHawkEntity blackHawkEntity, List<Integer> sourceIds) {
        blackHawkEntity.addFlightDataList(database.getFlightData(blackHawkEntity.getEntityId(), sourceIds));
    }

    private void loadShadowData(ShadowEntity shadowEntity, List<Integer> sourceIds) {
        for (TspiExtendedData tspiExtendedData : database.getTspiExtendedData(shadowEntity.getEntityId(), sourceIds)) {
            shadowEntity.addTspiExtendedData(tspiExtendedData);
        }
        for (UasPayloadData uasPayloadData : database.getUasPayloadData(shadowEntity.getEntityId(), sourceIds)) {
            shadowEntity.addUasPayloadData(uasPayloadData);
        }
    }

    private void loadDlmMessages(IDlmEntity dlmEntity, List<Integer> sourceIds) {
        for (GenericDlmMessage genericDlmMessage : database.getGenericDlmMessage(dlmEntity.getEntityId(), sourceIds)) {
            DlmMessage dlmMessage = DlmMessageFactory.createMessage(genericDlmMessage);
            dlmEntity.processMessage(dlmMessage, genericDlmMessage.isUseTimestampOverride() ?
                    genericDlmMessage.getTimestampOverride() : 0);
        }
    }

    private void loadC2MessageEvents(IEntity entity, List<Integer> sourceIds) {
        for (C2MessageEvent c2MessageEvent : database.getC2MessageEvents(entity, sourceIds)) {
            EventManager.addEvent(c2MessageEvent);
        }
    }

    private void loadStaticData(StaticEntity entity) {
        BufferedImage bufferedImage = database.getEntityImage(entity.getEntityId());
        if (bufferedImage != null) {
            entity.setImage(bufferedImage);
        }
    }

    private void loadNbcrvData(NbcrvEntity nbcrvEntity, List<Integer> sourceIds) {
        nbcrvEntity.addDevices(database.getNbcrvDevices(nbcrvEntity.getEntityId(), sourceIds));
        nbcrvEntity.addNbcrvStates(database.getNbcrvStates(nbcrvEntity.getEntityId(), sourceIds));
        nbcrvEntity.addRadNucStates(database.getRadNucStates(nbcrvEntity.getEntityId(), sourceIds));
        List<NbcrvDetection> nbcrvDetections = database.getNbcrvEvents(nbcrvEntity.getEntityId(), sourceIds);
        for (NbcrvDetection nbcrvDetection : nbcrvDetections) {
//            EventManager.addEvent(event);
            nbcrvEntity.addNbcrvDetection(nbcrvDetection);
        }
        for (TimedFile timedFile : database.getTimedFiles(nbcrvEntity.getEntityId())) {
            nbcrvEntity.addTimedFile(timedFile);
        }
    }

    private void loadWdlData(WdlEntity wdlEntity, List<Integer> sourceIds) {
        wdlEntity.addWdlReadings(database.getWdlReadings(wdlEntity.getEntityId(), sourceIds));
    }
}
