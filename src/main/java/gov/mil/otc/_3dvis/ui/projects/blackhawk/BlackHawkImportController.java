package gov.mil.otc._3dvis.ui.projects.blackhawk;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.project.blackhawk.UrnMap;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.project.blackhawk.*;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.DateTimePicker2;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.vmf.K0101;
import gov.mil.otc._3dvis.vmf.VmfMessage;
import gov.mil.otc._3dvis.vmf.pcap.ProtocolParser;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlackHawkImportController extends TransparentWindow {

    private static final String BFT_FOLDER_NAME = "BFT";
    private static final String SURVEYS_FOLDER_NAME = "Surveys";
    private static final String THREAT_FOLDER_NAME = "Threat";
    private static final String NO_FILES_FOUND = "no files found!";
    private static final String ERROR_STYLE = "-fx-background-color: red";
    private final TextField missionFolderTextField = new TextField();
    private final DateTimePicker2 startTimeDateTimePicker = new DateTimePicker2(System.currentTimeMillis());
    private final DateTimePicker2 stopTimeDateTimePicker = new DateTimePicker2(System.currentTimeMillis());
    private final CheckBox removeAudioCheckBox = new CheckBox("remove audio");
    private final CheckBox useRelativeMediaPath = new CheckBox("use relative paths for media");
    private final TabPane tabPane = new TabPane();
    private final Label missionEmptyLabel = new Label("no data loaded");
    private final TableView<ThreatView> threatTableView = new TableView<>();
    private final Label importCompleteLabel = new Label("Import Complete");
    private final Button importFileButton = new Button("Import");
    private boolean threatTableInitialized = false;
    private File[] bftFiles = null;
    private File[] surveysFiles = null;
    private CcmEventFile ccmEventFile = null;
    private List<MediaFile> threatMediaFileList = new ArrayList<>();
    private UrnMap urnMap = null;
    private final List<BlackHawkDataSet> blackHawkDataSetList = new ArrayList<>();
    private long missionStartTime = System.currentTimeMillis();
    private long missionStopTime = 0;
    ProgressDialog importProgressDialog;
    private final Map<Integer, IEntity> urnEntityMap = new HashMap<>();

    public static synchronized void show() {
        new BlackHawkImportController().createAndShow();
    }

    private BlackHawkImportController() {
    }

    @Override
    protected Pane createContentPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        missionFolderTextField.setMaxWidth(Double.MAX_VALUE);
        Button missionFolderButton = new Button("...");
        missionFolderButton.setOnAction(event -> selectMissionFolder());

        gridPane.add(new Label("Mission Folder:"), 0, rowIndex);
        gridPane.add(missionFolderTextField, 1, rowIndex);
        gridPane.add(missionFolderButton, 2, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Start Time:"), 0, rowIndex);
        gridPane.add(startTimeDateTimePicker, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Stop Time:"), 0, rowIndex);
        gridPane.add(stopTimeDateTimePicker, 1, rowIndex);

        rowIndex++;

        gridPane.add(new HBox(UiConstants.SPACING, removeAudioCheckBox, useRelativeMediaPath), 1, rowIndex);

        tabPane.setMinSize(800, 400);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        StackPane stackPane = new StackPane(missionEmptyLabel, tabPane);
        missionEmptyLabel.setVisible(false);

        importFileButton.setOnAction(event -> startImport());
        importFileButton.setDisable(true);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        AnchorPane buttonAnchorPane = new AnchorPane(importFileButton, closeButton);
        AnchorPane.setLeftAnchor(importFileButton, 0.0);
        AnchorPane.setRightAnchor(closeButton, 0.0);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Import BlackHawk Files"),
                new Separator(),
                gridPane,
                new Separator(),
                stackPane,
                new Separator(),
                buttonAnchorPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setFillWidth(true);

        importCompleteLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 58));
        importCompleteLabel.setTextFill(Color.GREEN);
        importCompleteLabel.setVisible(false);

        return new StackPane(mainVBox, importCompleteLabel);
    }

    private void selectMissionFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select mission folder");
        directoryChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("blackhawk"));
        File file = directoryChooser.showDialog(getStage());
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory("blackhawk", file.getParent());
            startLoadMission(file);
            importCompleteLabel.setVisible(false);
        }
    }

    private void startLoadMission(final File missionFolder) {
        final ProgressDialog progressDialog = new ProgressDialog(getStage());
        progressDialog.setCancelListener(progressDialog::close);
        progressDialog.addStatus("loading");
        progressDialog.createAndShow();
        new Thread(() -> {
            scanMissionFolder(missionFolder);
            Platform.runLater(() -> {
                loadMissionData(missionFolder);
                progressDialog.close();
            });
        }, "black hawk mission loader").start();
    }

    private void scanMissionFolder(File missionFolder) {
        bftFiles = null;
        surveysFiles = null;
        ccmEventFile = null;
        threatMediaFileList.clear();
        urnMap = null;
        blackHawkDataSetList.clear();
        missionStartTime = System.currentTimeMillis();
        missionStopTime = 0;

        File[] missionFolderfiles = missionFolder.listFiles();
        if (missionFolderfiles == null) {
            return;
        }

        for (File file : missionFolderfiles) {
            if (file.isDirectory()) {
                if (file.getName().equalsIgnoreCase(BFT_FOLDER_NAME)) {
                    bftFiles = file.listFiles();
                } else if (file.getName().equalsIgnoreCase(SURVEYS_FOLDER_NAME)) {
                    scanSurveysFolder(file);
                } else if (file.getName().equalsIgnoreCase(THREAT_FOLDER_NAME)) {
                    scanThreatFolder(file);
                } else {
                    scanBlackHawkFolder(file);
                }
            } else if (file.getName().equalsIgnoreCase("urn_map.csv")) {
                urnMap = new UrnMap(file);
                urnMap.processFile();
            }
        }
    }

    private void scanSurveysFolder(File surveysFolder) {
        surveysFiles = surveysFolder.listFiles();
    }

    private void scanThreatFolder(File threatFolder) {
        File[] threatFiles = threatFolder.listFiles();
        if (threatFiles != null) {
            for (File file : threatFiles) {
                if (file.isDirectory() && file.getName().equalsIgnoreCase("acquisition data")) {
                    File[] files = file.listFiles();
                    if (files != null && files.length > 0) {
                        ccmEventFile = new CcmEventFile(files[0]);
                        ccmEventFile.processFile();
                    }
                } else if (file.isDirectory() && file.getName().equalsIgnoreCase("video")) {
                    threatMediaFileList = getVideoFiles(file);
                }
            }
        }
    }

    private void scanBlackHawkFolder(File blackHawkFolder) {
        int tailNumber;
        try {
            tailNumber = Integer.parseInt(blackHawkFolder.getName());
        } catch (NumberFormatException e) {
            Logger.getGlobal().log(Level.INFO, null, e);
            return;
        }

        BlackHawkDataSet blackHawkDataSet = new BlackHawkDataSet(tailNumber);
        blackHawkDataSetList.add(blackHawkDataSet);

        File[] blackHawkFiles = blackHawkFolder.listFiles();
        if (blackHawkFiles != null) {
            for (File file : blackHawkFiles) {
                if (file.isDirectory() && file.getName().equalsIgnoreCase("bus data")) {
                    scanBusDataFolder(file, blackHawkDataSet);
                } else if (file.isDirectory() && file.getName().equalsIgnoreCase("video")) {
                    List<MediaFile> mediaFiles = getVideoFiles(file);
                    blackHawkDataSet.setMediaFileList(mediaFiles);
                }
            }
        }
        updateTimestamps(blackHawkDataSet);
    }

    private void scanBusDataFolder(File busDataFolder, BlackHawkDataSet blackHawkDataSet) {
        File[] busDataFolderFiles = busDataFolder.listFiles();
        if (busDataFolderFiles == null) {
            return;
        }

        for (File file : busDataFolderFiles) {
            if (file.isDirectory() && file.getName().equalsIgnoreCase("tspi")) {
                scanTspiFolder(file, blackHawkDataSet);
            }
        }
    }

    private void scanTspiFolder(File tspiFolder, BlackHawkDataSet blackHawkDataSet) {
        File[] tspiFolderFiles = tspiFolder.listFiles();
        if (tspiFolderFiles == null) {
            return;
        }

        for (File file : tspiFolderFiles) {
            TspiFile tspiFile = new TspiFile(file, startTimeDateTimePicker.getYear());
            if (tspiFile.isValidFile()) {
                blackHawkDataSet.addTspiFile(tspiFile);
            }
        }
    }

    private void updateTimestamps(BlackHawkDataSet blackHawkDataSet) {
        long newStartTime = blackHawkDataSet.getStartTime();
        if (newStartTime < missionStartTime) {
            missionStartTime = newStartTime;
        }
        long newStopTime = blackHawkDataSet.getStopTime();
        if (newStopTime > missionStopTime) {
            missionStopTime = newStopTime;
        }
    }

    private List<MediaFile> getVideoFiles(File videoFolder) {
        List<MediaFile> mediaFiles = new ArrayList<>();

        File[] videoFiles = videoFolder.listFiles();
        if (videoFiles == null) {
            return mediaFiles;
        }

        for (File videoFile : videoFiles) {
            MediaFile mediaFile = processVideoFile(videoFile);
            if (mediaFile != null) {
                mediaFiles.add(mediaFile);
            }
        }
        return mediaFiles;
    }

    //"[media set]_DDD_HH_mm_ss_SSS.[ext]"
    private MediaFile processVideoFile(File videoFile) {
        String[] videoName = videoFile.getName().split("[-_.]");
        if (videoName.length >= 6) {
            try {
                int dayOfYear = Integer.parseInt(videoName[1]);
                int hour = Integer.parseInt(videoName[2]);
                int minute = Integer.parseInt(videoName[3]);
                int second = Integer.parseInt(videoName[4]);
                int millisecond = Integer.parseInt(videoName[5]);

                LocalDate localDate = LocalDate.ofYearDay(startTimeDateTimePicker.getYear(), dayOfYear);
                LocalTime localTime = LocalTime.of(hour, minute, second, millisecond * 1000000);
                LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
                Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
                long startTime = instant.toEpochMilli();
                long stopTime = startTime + (long) getDuration(videoFile);

                return new MediaFile(videoFile.getAbsolutePath(), startTime, stopTime, videoName[0],
                        useRelativeMediaPath.isSelected());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
        return null;
    }

    private double getDuration(File videoFile) {
        try {
            FFprobe fFprobe = new FFprobe("external\\ffmpeg\\bin\\ffprobe");
            FFmpegProbeResult probeResult = fFprobe.probe(videoFile.getAbsolutePath());
            return probeResult.format.duration * 1000;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return 0;
    }

    private void loadMissionData(File missionFolder) {
        tabPane.getTabs().clear();

        File[] missionFolderFiles = missionFolder.listFiles();
        if (missionFolderFiles == null) {
            missionEmptyLabel.setVisible(true);
            return;
        }

        loadBftTab();
        loadSurveysTab();
        loadThreatTab();
        loadBlackHawkTabs();

        startTimeDateTimePicker.setTimestamp(missionStartTime);
        stopTimeDateTimePicker.setTimestamp(missionStopTime);

        missionEmptyLabel.setVisible(false);

        if (checkIfMissionExists(missionFolder.getName())) {
            missionFolderTextField.setText(missionFolder.getAbsolutePath() + " (already exists)");
            missionFolderTextField.setStyle("-fx-text-fill: red;");
            importFileButton.setDisable(true);
        } else {
            missionFolderTextField.setText(missionFolder.getAbsolutePath());
            missionFolderTextField.setStyle(null);
            importFileButton.setDisable(false);
        }
    }

    private void loadBftTab() {
        if (bftFiles == null || bftFiles.length == 0) {
            Label label = new Label(NO_FILES_FOUND);
            label.setPadding(new Insets(UiConstants.SPACING));
            Tab tab = new Tab(BFT_FOLDER_NAME, label);
            tab.setStyle(ERROR_STYLE);
            tabPane.getTabs().add(tab);
        } else {
            ListView<File> listView = new ListView<>();
            listView.setPadding(new Insets(UiConstants.SPACING));
            for (File file : bftFiles) {
                listView.getItems().add(file);
            }
            tabPane.getTabs().add(new Tab(BFT_FOLDER_NAME, listView));
        }
    }

    private void loadSurveysTab() {
        if (surveysFiles == null || surveysFiles.length == 0) {
            Label label = new Label(NO_FILES_FOUND);
            label.setPadding(new Insets(UiConstants.SPACING));
            Tab tab = new Tab(SURVEYS_FOLDER_NAME, label);
            tab.setStyle(ERROR_STYLE);
            tabPane.getTabs().add(tab);
        } else {
            ListView<File> listView = new ListView<>();
            listView.setPadding(new Insets(UiConstants.SPACING));
            for (File file : surveysFiles) {
                listView.getItems().add(file);
            }
            tabPane.getTabs().add(new Tab(SURVEYS_FOLDER_NAME, listView));
        }
    }

    private void loadThreatTab() {
        if (ccmEventFile == null || !ccmEventFile.isSuccessful()) {
            Tab tab = new Tab(THREAT_FOLDER_NAME, new Label(NO_FILES_FOUND));
            tab.setStyle(ERROR_STYLE);
            tabPane.getTabs().add(tab);
        } else {
            initializeThreatTable();
            for (AdHocEntity adHocEntity : ccmEventFile.getEntityList()) {
                for (MediaFile mediaFile : threatMediaFileList) {
                    if (adHocEntity.getSource().equalsIgnoreCase(mediaFile.getMediaSet())) {
                        adHocEntity.addMediaFile(mediaFile);
                    }
                }
                threatTableView.getItems().add(new ThreatView(adHocEntity, getStage()));
            }
            tabPane.getTabs().add(new Tab(THREAT_FOLDER_NAME, threatTableView));
        }
    }

    private void loadBlackHawkTabs() {
        if (blackHawkDataSetList.isEmpty()) {
            Tab tab = new Tab("Black Hawk", new Label(NO_FILES_FOUND));
            tab.setStyle(ERROR_STYLE);
            tabPane.getTabs().add(tab);
        } else {
            for (BlackHawkDataSet blackHawkDataSet : blackHawkDataSetList) {
                loadBlackHawkTab(blackHawkDataSet);
            }
        }
    }

    private void loadBlackHawkTab(BlackHawkDataSet blackHawkDataSet) {
        boolean error = blackHawkDataSet.getTspiFileList().isEmpty() || blackHawkDataSet.getMediaFileList().isEmpty();

        TabPane blackHawkTabPane = new TabPane();
        blackHawkTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        ListView<String> tspiFileListView = new ListView<>();
        for (TspiFile tspiFile : blackHawkDataSet.getTspiFileList()) {
            tspiFileListView.getItems().add(tspiFile.getFile().getName());
        }

        Tab tspiTab = new Tab("TSPI Files", tspiFileListView);
        if (blackHawkDataSet.getTspiFileList().isEmpty()) {
            tspiTab.setStyle(ERROR_STYLE);
        }

        blackHawkTabPane.getTabs().add(tspiTab);

        ListView<String> videoFileListView = new ListView<>();
        for (MediaFile mediaFile : blackHawkDataSet.getMediaFileList()) {
            videoFileListView.getItems().add(mediaFile.getName());
        }

        Tab videoTab = new Tab("Video Files", videoFileListView);
        if (blackHawkDataSet.getMediaFileList().isEmpty()) {
            videoTab.setStyle(ERROR_STYLE);
        }

        blackHawkTabPane.getTabs().add(videoTab);

        Tab tab = new Tab(String.valueOf(blackHawkDataSet.getTailNumber()), blackHawkTabPane);
        if (error) {
            tab.setStyle(ERROR_STYLE);
        }

        tabPane.getTabs().add(tab);
    }

    private void initializeThreatTable() {
        if (threatTableInitialized) {
            return;
        }

        TableColumn<ThreatView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<ThreatView, String> systemTableColumn = new TableColumn<>("System");
        systemTableColumn.setCellValueFactory(new PropertyValueFactory<>("system"));
        TableColumn<ThreatView, Hyperlink> entityTypeTableColumn = new TableColumn<>("Entity Type");
        entityTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityTypeHyperlink"));
        TableColumn<ThreatView, String> descriptionTableColumn = new TableColumn<>("Description");
        descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        TableColumn<ThreatView, String> mediaTableColumn = new TableColumn<>("Media");
        mediaTableColumn.setCellValueFactory(new PropertyValueFactory<>("videoCount"));

        threatTableView.getColumns().add(nameTableColumn);
        threatTableView.getColumns().add(systemTableColumn);
        threatTableView.getColumns().add(entityTypeTableColumn);
        threatTableView.getColumns().add(descriptionTableColumn);
        threatTableView.getColumns().add(mediaTableColumn);

        nameTableColumn.prefWidthProperty().bind(threatTableView.widthProperty().multiply(7 / 44.0));
        systemTableColumn.prefWidthProperty().bind(threatTableView.widthProperty().multiply(7 / 44.0));
        entityTypeTableColumn.prefWidthProperty().bind(threatTableView.widthProperty().multiply(7 / 44.0));
        descriptionTableColumn.prefWidthProperty().bind(threatTableView.widthProperty().multiply(15.0 / 44.0));
        mediaTableColumn.prefWidthProperty().bind(threatTableView.widthProperty().multiply(7.0 / 44.0));

        threatTableInitialized = true;
    }

    private boolean checkIfMissionExists(String missionName) {
        List<Mission> missions = DataManager.getMissions();
        for (Mission mission : missions) {
            if (mission.getName().equalsIgnoreCase(missionName)) {
                return true;
            }
        }
        return false;
    }

    private void startImport() {
        importFileButton.setDisable(true);
        importProgressDialog = new ProgressDialog(getStage());
        importProgressDialog.setCancelListener(this::cancelImport);
        importProgressDialog.addStatus("importing");
        importProgressDialog.createAndShow();
        new Thread(() -> {
            try {
                importData();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    importCompleteLabel.setText("Import Failed!");
                    importCompleteLabel.setVisible(true);
                    importProgressDialog.close();
                });
            }
            Platform.runLater(() -> {
                importCompleteLabel.setText("Import Complete");
                importCompleteLabel.setVisible(true);
                importProgressDialog.close();
            });
        }, "black hawk mission import").start();
    }

    private void cancelImport() {
        Platform.runLater(() -> {
            if (DialogUtilities.showYesNoDialog("Cancel Import", "Are you sure you want to cance import?",
                    getStage())) {
                importProgressDialog.close();
            }
        });
    }

    private void updateStatus(final String status) {
        Platform.runLater(() -> importProgressDialog.addStatus(status));
    }

    private void importData() {
        String missionName = new File(missionFolderTextField.getText()).getName();
        updateStatus("importing mission " + missionName);

        Mission mission = new Mission(missionName, startTimeDateTimePicker.getTimestamp(),
                stopTimeDateTimePicker.getTimestamp());
        DataManager.addMission(mission);

        updateStatus("importing aircraft data");
        importBlackHawkData(mission);

        updateStatus("importing threat data");
        importThreatData(mission);

        updateStatus("importing survey data");
        importSurveysData(mission);

        updateStatus("importing bft data");
        importBftData(mission);
    }

    private void importBlackHawkData(Mission mission) {
        for (BlackHawkDataSet blackHawkDataSet : blackHawkDataSetList) {
            BlackHawkEntity blackHawkEntity = getOrCreateBlackHawkEntity(blackHawkDataSet);
            if (blackHawkEntity == null) {
                continue;
            }

            importEntityDetailAndScope(mission, blackHawkDataSet, blackHawkEntity);
            importTspi(mission, blackHawkDataSet, blackHawkEntity);
            importBlackHawkVideo(mission, blackHawkEntity, blackHawkDataSet.getMediaFileList());
        }
    }

    private void importBlackHawkVideo(Mission mission, BlackHawkEntity blackHawkEntity, List<MediaFile> mediaFiles) {
        for (MediaFile mediaFile : mediaFiles) {
            if (removeAudioCheckBox.isSelected() && !mediaFile.getName().toUpperCase().startsWith("CENTER") &&
                    !mediaFile.getName().toUpperCase().contains("NOAUDIO")) {
                MediaFile newMediaFile = removeAudio(mediaFile);
                File archiveFolder = new File(mediaFile.getParent() + File.separator + "archive");
                moveFile(mediaFile, archiveFolder);
                mediaFile = newMediaFile;
            }
            if (mediaFile != null) {
                mediaFile.setUseRelativePath(useRelativeMediaPath.isSelected());
                blackHawkEntity.getMediaCollection().addMediaFile(mediaFile);
                DataSource mediaDataSource = DatabaseLogger.addMedia(mediaFile, blackHawkEntity.getEntityId());
                DataManager.addMissionDataSource(mission, mediaDataSource);
            }
        }
    }

    private void moveFile(File file, File archiveFolder) {
        String output = archiveFolder.getAbsolutePath() + File.separator + file.getName();
        try {
            if (archiveFolder.exists() || archiveFolder.mkdir()) {
                Files.move(file.toPath(), Paths.get(output));
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private MediaFile removeAudio(MediaFile mediaFile) {
        String ext = mediaFile.getName().substring(mediaFile.getName().lastIndexOf("."));
        String output = mediaFile.getAbsolutePath().substring(0, mediaFile.getAbsolutePath().lastIndexOf(".")) +
                "_NOAUDIO" + ext;
        try {
            FFmpeg fFmpeg = new FFmpeg("external\\ffmpeg\\bin\\ffmpeg");
            fFmpeg.run(List.of("-i",
                    mediaFile.getAbsolutePath(),
                    "-c",
                    "copy",
                    "-an",
                    output));
            return new MediaFile(output, mediaFile.getStartTime(), mediaFile.getStopTime(), mediaFile.getMediaSet(),
                    useRelativeMediaPath.isSelected());
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return null;
    }

    private BlackHawkEntity getOrCreateBlackHawkEntity(BlackHawkDataSet blackHawkDataSet) {
        if (blackHawkDataSet.getTspiFileList().isEmpty()) {
            return null;
        }

        EntityId entityId = new EntityId(Defaults.SITE_APP_ID_3DVIS, Defaults.APP_ID_BLACKHAWK,
                blackHawkDataSet.getTailNumber());
        IEntity entity = EntityManager.getEntity(entityId);

        if (entity == null) {
            entity = new BlackHawkEntity(entityId);
            EntityManager.addEntity(entity, true);
        }

        if (entity instanceof BlackHawkEntity) {
            return (BlackHawkEntity) entity;
        }

        return null;
    }

    private void importEntityDetailAndScope(Mission mission, BlackHawkDataSet blackHawkDataSet, BlackHawkEntity blackHawkEntity) {
        String dataSourceName = missionFolderTextField.getText() + " : " + blackHawkDataSet.getTailNumber();
        DataSource dataSource = DataManager.createDataSource(dataSourceName, blackHawkDataSet.getStartTime(),
                blackHawkDataSet.getStopTime());
        DataManager.addMissionDataSource(mission, dataSource);

        int urn = 0;
        String role = "";
        if (urnMap != null) {
            urn = urnMap.getUrn(blackHawkDataSet.getTailNumber());
            role = urnMap.getRole(blackHawkDataSet.getTailNumber());
        }

        EntityType entityType = new EntityType(1, 2, 225, 21, 2, 26, 0);
        String militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, Affiliation.FRIENDLY);
        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(blackHawkDataSet.getStartTime())
                .setEntityType(entityType)
                .setAffiliation(Affiliation.FRIENDLY)
                .setName(String.valueOf(blackHawkDataSet.getTailNumber()))
                .setSource("Data Bus")
                .setMilitarySymbol(militarySymbol)
                .setUrn(urn)
                .build();
        blackHawkEntity.addEntityDetail(entityDetail);
        DatabaseLogger.addEntityDetail(entityDetail, blackHawkEntity.getEntityId(), dataSource.getId());

        EntityScope entityScope = new EntityScope(blackHawkDataSet.getStartTime(), blackHawkDataSet.getStopTime());
        blackHawkEntity.addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, blackHawkEntity.getEntityId(), dataSource.getId());
    }

    private void importTspi(Mission mission, BlackHawkDataSet blackHawkDataSet, BlackHawkEntity blackHawkEntity) {
        for (TspiFile tspiFile : blackHawkDataSet.getTspiFileList()) {
            tspiFile.processFile();
            if (!tspiFile.isSuccessful() || tspiFile.getTspiDataList().isEmpty()) {
                continue;
            }

            TspiData firstTspi = tspiFile.getTspiDataList().get(0);
            TspiData lastTspi = tspiFile.getTspiDataList().get(tspiFile.getTspiDataList().size() - 1);

            DataSource dataSource = DataManager.createDataSource(tspiFile.getFile().getAbsolutePath(),
                    firstTspi.getTimestamp(), lastTspi.getTimestamp());
            DataManager.addMissionDataSource(mission, dataSource);

            blackHawkEntity.addTspiList(tspiFile.getTspiDataList());
            for (TspiData tspiData : tspiFile.getTspiDataList()) {
                DatabaseLogger.addTspiData(tspiData, blackHawkEntity.getEntityId(), dataSource.getId());
            }

            blackHawkEntity.addFlightDataList(tspiFile.getFlightDataList());
            for (FlightData flightData : tspiFile.getFlightDataList()) {
                DatabaseLogger.addFlightData(flightData, blackHawkEntity.getEntityId(), dataSource.getId());
            }
        }
    }

    private void importThreatData(Mission mission) {
        if (ccmEventFile == null || !ccmEventFile.isSuccessful()) {
            return;
        }

        long startTime = startTimeDateTimePicker.getTimestamp();
        long stopTime = stopTimeDateTimePicker.getTimestamp();
        DataSource dataSource = DataManager.createDataSource(ccmEventFile.getFile().getAbsolutePath(),
                startTime, stopTime);
        DataManager.addMissionDataSource(mission, dataSource);

        for (ThreatView threatView : threatTableView.getItems()) {
            EntityId entityId = DataManager.getNextAvailableEntityId("BlackHawk Threat");
            PlaybackEntity playbackEntity = new PlaybackEntity(entityId);
            TspiData tspiData = threatView.getAdHocEntity().getTspiDataList().get(0);
            String militarySymbol = EntityTypeUtility.getTacticalSymbol(threatView.getAdHocEntity().getEntityType());
            militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol,
                    threatView.getAdHocEntity().getAffiliation());
            EntityDetail entityDetail = new EntityDetail.Builder()
                    .setTimestamp(tspiData.getTimestamp())
                    .setEntityType(threatView.getAdHocEntity().getEntityType())
                    .setName(threatView.getAdHocEntity().getName())
                    .setSource(threatView.getAdHocEntity().getSource())
                    .setAffiliation(threatView.getAdHocEntity().getAffiliation())
                    .setMilitarySymbol(militarySymbol)
                    .build();
            playbackEntity.addEntityDetail(entityDetail);
            DatabaseLogger.addEntityDetail(entityDetail, entityId, dataSource.getId());

            EntityScope entityScope = new EntityScope(startTime, stopTime);
            playbackEntity.addEntityScope(entityScope);
            DatabaseLogger.addEntityScope(entityScope, playbackEntity.getEntityId(), dataSource.getId());

            playbackEntity.addTspi(tspiData);
            DatabaseLogger.addTspiData(tspiData, entityId, dataSource.getId());

            for (Event event : threatView.getAdHocEntity().getEventList()) {
                playbackEntity.addEvent(event);
                EventManager.addEvent(event);
                DatabaseLogger.addEvent(event, entityId, dataSource.getId());
            }

            EntityManager.addEntity(playbackEntity, true);

            for (MediaFile mediaFile : threatMediaFileList) {
                if (threatView.getAdHocEntity().getSource().equalsIgnoreCase(mediaFile.getMediaSet())) {
                    mediaFile.setUseRelativePath(useRelativeMediaPath.isSelected());
                    playbackEntity.getMediaCollection().addMediaFile(mediaFile);
                    DataSource mediaDataSource = DatabaseLogger.addMedia(mediaFile, entityId);
                    DataManager.addMissionDataSource(mission, mediaDataSource);
                }
            }
        }
    }

    private void importSurveysData(Mission mission) {
        if (surveysFiles == null) {
            return;
        }

        for (File file : surveysFiles) {
            if (file.getName().equalsIgnoreCase("tblCrewMissionMatrix.csv")) {
                DataSource dataSource = DataManager.createDataSource(file.getAbsolutePath(), 0, 0);
                DataManager.addMissionDataSource(mission, dataSource);
                CrewMissionMatrixFile crewMissionMatrixFile = new CrewMissionMatrixFile(file, dataSource);
                crewMissionMatrixFile.processFile();
            } else if (file.getName().equalsIgnoreCase("tblPostMissionAnswers.csv")) {
                DataSource dataSource = DataManager.createDataSource(file.getAbsolutePath(), 0, 0);
                DataManager.addMissionDataSource(mission, dataSource);
                PostMissionAnswersFile postMissionAnswersFile = new PostMissionAnswersFile(file, dataSource);
                postMissionAnswersFile.processFile();
            }
        }
    }

    private void importBftData(Mission mission) {
        if (bftFiles == null) {
            return;
        }

        for (File file : bftFiles) {
            ProtocolParser protocolParser = new ProtocolParser();
            List<VmfMessage> messages = protocolParser.processPcap(file);
            if (!messages.isEmpty()) {
                List<VmfMessage> c2Messages = new ArrayList<>();
                for (VmfMessage message : messages) {
                    if (message instanceof K0101) {
                        c2Messages.add(message);
                    }
                }
                addC2Messages(c2Messages, file);
            }
        }
    }

    private void addC2Messages(Collection<VmfMessage> c2Messages, File file) {
        DataSource dataSource = DataManager.createDataSource(file.getAbsolutePath(), 0, 0);

        for (VmfMessage c2Message : c2Messages) {
            addBftMessage(c2Message, dataSource);
        }
    }

    private void addBftMessage(VmfMessage vmfMessage, DataSource dataSource) {
        IEntity entity = getSenderEntity(vmfMessage);
        if (entity != null) {
            C2MessageEvent c2MessageEvent = new C2MessageEvent(entity, vmfMessage);
            EventManager.addEvent(c2MessageEvent);
            DatabaseLogger.addC2Message(c2MessageEvent, entity.getEntityId(), dataSource.getId());
        }
    }

    private IEntity getSenderEntity(VmfMessage vmfMessage) {
        IEntity senderEntity = urnEntityMap.get(vmfMessage.getHeader().getSenderUrn());
        if (senderEntity == null) {
            for (IEntity entity : EntityManager.getEntities()) {
                EntityDetail entityDetail = entity.getEntityDetailBefore(vmfMessage.getHeader().getOriginatorTime());
                if (entityDetail != null && entityDetail.getUrn() == vmfMessage.getHeader().getSenderUrn()) {
                    urnEntityMap.put(vmfMessage.getHeader().getSenderUrn(), entity);
                    return entity;
                }
            }
        }
        return senderEntity;
    }
}
