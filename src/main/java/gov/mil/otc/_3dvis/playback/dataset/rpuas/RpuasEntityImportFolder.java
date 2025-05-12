package gov.mil.otc._3dvis.playback.dataset.rpuas;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.tspi.TspiImportObject;
import gov.mil.otc._3dvis.playback.dataset.manual.TirImportFolder;
import gov.mil.otc._3dvis.playback.dataset.media.MediaImportFolder;
import gov.mil.otc._3dvis.project.rpuas.*;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RpuasEntityImportFolder extends ImportFolder {

    /* Directory Layout
        {entity folder}
        |  'bus' OR 'flight-logs'
        |  |  {files}
        |  'video' //media folder set
        |  'audio' //media folder set
        |  'tir' //tir folder set
     */

    public static RpuasEntityImportFolder scanAndCreate(File folder, MissionConfigurations missionConfigurations) {
        DeviceInformation deviceInformation = missionConfigurations.getDeviceInformation(folder.getName());
        if (deviceInformation != null) {
            RpuasEntityImportFolder rpuasEntityDataSet = new RpuasEntityImportFolder(folder, missionConfigurations, deviceInformation);
            if (rpuasEntityDataSet.validateAndScan()) {
                return rpuasEntityDataSet;
            }
        }
        return null;
    }

    private final MissionConfigurations missionConfigurations;
    private final MediaTimestampFormat mediaTimestampFormat;
    private BusImportFolder busImportFolder = null;
    private MediaImportFolder videoImportFolder = null;
    private MediaImportFolder audioImportFolder = null;
    private TirImportFolder tirImportFolder = null;
    private final TableView<MissionRow> tableView = new TableView<>();

    public RpuasEntityImportFolder(File folder, MissionConfigurations missionConfigurations, DeviceInformation deviceInformation) {
        super(folder);

        this.missionConfigurations = missionConfigurations;

        if (deviceInformation.getVendor().equalsIgnoreCase("skydio")) {
            mediaTimestampFormat = MediaTimestampFormat.METADATA;
        } else {
            mediaTimestampFormat = MediaTimestampFormat.STANDARD;
        }
    }

    @Override
    protected boolean scanFiles(File[] files) {
        setNew(determineIsNew(getObject()));

        if (missionConfigurations.getMissionConfigurationList().isEmpty()) {
            setMissing(true);
        }

        for (File file : files) {
            if (BusImportFolder.isImportFolder(file)) {
                busImportFolder = BusImportFolder.scanAndCreate(file);
            } else if (MediaImportFolder.isVideoFolder(file)) {
                videoImportFolder = MediaImportFolder.scanAndCreate(file, mediaTimestampFormat);
            } else if (MediaImportFolder.isAudioFolder(file)) {
                audioImportFolder = MediaImportFolder.scanAndCreate(file, MediaTimestampFormat.STANDARD);
            } else if (TirImportFolder.isTirFolder(file)) {
                tirImportFolder = TirImportFolder.scanAndCreate(file);
            }
        }

        if (busImportFolder == null) {
            setMissing(true);
        } else if ((busImportFolder.isNew() || busImportFolder.isModified()) ||
                (videoImportFolder != null && (videoImportFolder.isNew() || videoImportFolder.isModified())) ||
                (audioImportFolder != null && (audioImportFolder.isNew() || audioImportFolder.isModified()))) {
            if (!isNew()) {
                setModified(true);
            }
        }

        initializeTable();

        return true;
    }

    @Override
    public VBox getDisplayPane() {
        return new VBox(UiConstants.SPACING, tableView);
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        if (busImportFolder != null) {
            treeItems.add(busImportFolder.getTreeItem());
        }
        if (videoImportFolder != null) {
            treeItems.add(videoImportFolder.getTreeItem());
        }
        if (audioImportFolder != null) {
            treeItems.add(audioImportFolder.getTreeItem());
        }
        if (tirImportFolder != null) {
            treeItems.add(tirImportFolder.getTreeItem());
        }
        return treeItems;
    }

    @Override
    public void doImport() {
        RpuasEntity entity = getOrCreateEntity(getName());
        long startTime = Long.MAX_VALUE;

        if (busImportFolder != null) {
            busImportFolder.importFolder(entity, importStatusLine);
            startTime = busImportFolder.getStartTime();
        }

        if (videoImportFolder != null) {
            videoImportFolder.importFolder(entity, importStatusLine);
        }

        if (audioImportFolder != null) {
            audioImportFolder.importFolder(entity, importStatusLine);
        }

        if (tirImportFolder != null) {
            tirImportFolder.importFolder(entity, importStatusLine);
        }

        addMissionConfiguration(entity, startTime);
    }

    private RpuasEntity getOrCreateEntity(String entityName) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof RpuasEntity rpuasEntity &&
                    entity.getLastEntityDetail() != null &&
                    entityName.equals(entity.getLastEntityDetail().getName())) {
                return rpuasEntity;
            }
        }

        EntityId entityId = DataManager.getNextGenericEntityId();
        DatabaseLogger.addEntityId(entityId);
        RpuasEntity entity = new RpuasEntity(entityId);
        EntityManager.addEntity(entity, false);
        return entity;
    }

    private void addMissionConfiguration(RpuasEntity entity, long startTime) {
        //DataSource dataSource = DataManager.createDataSource(getObject().getAbsolutePath(), startTime, -1);

        for (MissionConfiguration missionConfiguration : missionConfigurations.getMissionConfigurationList()) {
            for (DeviceConfiguration deviceConfiguration : missionConfiguration.getDeviceConfigurationList()) {
                if (deviceConfiguration.getDeviceId().equalsIgnoreCase(getName())) {
                    EntityType entityType = new EntityType(1, 2, 225, 50, 0, 0, 0);
                    String militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
                    militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, deviceConfiguration.getAffiliation());

                    EntityDetail entityDetail = new EntityDetail.Builder()
                            .setTimestamp(missionConfiguration.getStartTime())
                            .setName(getName())
                            .setSource("RPUAS")
                            .setEntityType(entityType)
                            .setMilitarySymbol(militarySymbol)
                            .setAffiliation(deviceConfiguration.getAffiliation())
                            .build();
                    entity.addEntityDetail(entityDetail);
                    EntityScope entityScope = new EntityScope(missionConfiguration.getStartTime(), missionConfiguration.getStopTime());
                    entity.addEntityScope(entityScope);

                    entity.addMissionConfiguration(missionConfiguration);
                }
            }
        }
    }

    private void initializeTable() {
        TableColumn<MissionRow, String> missionTableColumn = new TableColumn<>("Mission");
        missionTableColumn.setCellValueFactory(new PropertyValueFactory<>("missionName"));

        TableColumn<MissionRow, Affiliation> affiliationTableColumn = new TableColumn<>("Affiliation");
        affiliationTableColumn.setCellValueFactory(new PropertyValueFactory<>("affiliation"));

        TableColumn<MissionRow, Integer> operatorIdTableColumn = new TableColumn<>("Operator ID");
        operatorIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("operatorId"));

        TableColumn<MissionRow, Boolean> hasDataTableColumn = new TableColumn<>("Has Data");
        hasDataTableColumn.setCellValueFactory(new PropertyValueFactory<>("hasData"));

        TableColumn<MissionRow, Boolean> hasVideoTableColumn = new TableColumn<>("Has Video");
        hasVideoTableColumn.setCellValueFactory(new PropertyValueFactory<>("hasVideo"));

//        willCreateTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(5.0 / 37));
//        fileTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(15.0 / 37));
//        statusTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(16.0 / 37));

        tableView.getColumns().add(missionTableColumn);
        tableView.getColumns().add(affiliationTableColumn);
        tableView.getColumns().add(operatorIdTableColumn);
        tableView.getColumns().add(hasDataTableColumn);
        tableView.getColumns().add(hasVideoTableColumn);

//        nameTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(20 / 37.0));
//        startTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));
//        stopTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));

        tableView.setPlaceholder(new Label("no configuration available"));
//        tableView.setRowFactory(param -> new RpuasMissionConfigurationFileObject.MissionTableRow());
        tableView.setSelectionModel(null);

        URL url = ThemeHelper.class.getResource("/css/import_data_object_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            tableView.getStylesheets().add(css);
        }

        fillTable();
    }

    private void fillTable() {
        for (MissionConfiguration missionConfiguration : missionConfigurations.getMissionConfigurationList()) {
            for (DeviceConfiguration deviceConfiguration : missionConfiguration.getDeviceConfigurationList()) {
                if (deviceConfiguration.getDeviceId().equalsIgnoreCase(getName())) {
                    boolean hasData = false;
                    for (TspiImportObject tspiImportObject : busImportFolder.getTspiImportObjectList()) {
                        if ((missionConfiguration.getStartTime() < tspiImportObject.getFirstTimestamp()
                                && missionConfiguration.getStopTime() > tspiImportObject.getFirstTimestamp())
                                || (missionConfiguration.getStartTime() < tspiImportObject.getLastTimestamp()
                                && missionConfiguration.getStopTime() > tspiImportObject.getLastTimestamp())) {
                            hasData = true;
                            break;
                        }
                    }
                    boolean hasVideo = false;
                    MissionRow missionRow = new MissionRow(missionConfiguration.getMissionName(),
                            deviceConfiguration.getAffiliation(), deviceConfiguration.getOperatorId(),
                            hasData, hasVideo);
                    tableView.getItems().add(missionRow);
                }
            }
        }
    }

    public static class MissionRow {
        String missionName;
        Affiliation affiliation;
        int operatorId;
        boolean hasData;
        boolean hasVideo;

        public MissionRow(String missionName, Affiliation affiliation, int operatorId, boolean hasData, boolean hasVideo) {
            this.missionName = missionName;
            this.affiliation = affiliation;
            this.operatorId = operatorId;
            this.hasData = hasData;
            this.hasVideo = hasVideo;
        }

        public String getMissionName() {
            return missionName;
        }

        public Affiliation getAffiliation() {
            return affiliation;
        }

        public int getOperatorId() {
            return operatorId;
        }

        public boolean isHasData() {
            return hasData;
        }

        public boolean isHasVideo() {
            return hasVideo;
        }
    }
}
