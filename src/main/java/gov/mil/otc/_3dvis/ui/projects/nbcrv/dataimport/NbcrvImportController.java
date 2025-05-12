package gov.mil.otc._3dvis.ui.projects.nbcrv.dataimport;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.admas.Admas3dmCsv;
import gov.mil.otc._3dvis.data.admas.AdmasGpsCsv;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.data.oadms.RocketSledXmlFile;
import gov.mil.otc._3dvis.data.oadms.SidecarEnclosureXmlFile;
import gov.mil.otc._3dvis.data.oadms.WdlXmlFile;
import gov.mil.otc._3dvis.data.otcam.OtcamUtility;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.project.nbcrv.*;
import gov.mil.otc._3dvis.project.nbcrv.flir.DetectionsFile;
import gov.mil.otc._3dvis.project.nbcrv.flir.NbcrvFile;
import gov.mil.otc._3dvis.project.nbcrv.flir.UasGpsFile;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.tir.TirReader;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.DateTimePicker2;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import gov.mil.otc._3dvis.ui.widgets.cellfactory.TimestampTableColumnCellFactory;
import gov.mil.otc._3dvis.vmf.*;
import gov.mil.otc._3dvis.vmf.pcap.ProtocolParser;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvImportController extends TransparentWindow {

    public static synchronized void show() {
        new NbcrvImportController().createAndShow();
    }

    private final CheckBox scanForMissionTimeCheckBox = new CheckBox("Scan for Mission Time");
    private final TextField missionFolderTextField = new TextField();
    private final TextField missionTextField = new TextField();
    private final TextField importTypeTextField = new TextField();
    private final CheckBox userDefinedStartStop = new CheckBox("User Defined");
    private final DateTimePicker2 startTimeDateTimePicker = new DateTimePicker2(System.currentTimeMillis());
    private final DateTimePicker2 stopTimeDateTimePicker = new DateTimePicker2(System.currentTimeMillis());
    private final RadioButton useAdmasTspiRadioButton = new RadioButton("ADMAS");
    private final RadioButton useOadmsTspiRadioButton = new RadioButton("OADMS");
    private final CheckBox includeUgvCheckBox = new CheckBox("Include UGV");
    private final CheckBox flirOnlyCheckBox = new CheckBox("FLIR Only");
    private final Spinner<Integer> oadmsTimeZoneOffsetSpinner = new Spinner<>(0, 23, 7);
    private final Spinner<Integer> oadmsSecondsOffsetSpinner = new Spinner<>(-60000, 60000, 2500);
    private final Spinner<Integer> medianFilterWindowSpinner = new Spinner<>(0, 1000,
            SettingsManager.getSettings().getNbcrvSettings().getLidarMedianFilterWindowSize());
    private final Spinner<Integer> clippedStartSpinner = new Spinner<>(0, 1000,
            SettingsManager.getSettings().getNbcrvSettings().getLidarClippedStartSize());
    private final Spinner<Integer> maxConcentrationSpinner = new Spinner<>(0, 3000,
            SettingsManager.getSettings().getNbcrvSettings().getLidarMedianFilterWindowSize());
    private final String[] tabNames = {"Data Files", "Images", "Video", "Manual", "Other"};
    private final TabPane tabPane = new TabPane();
    private final Button importButton = new Button("Import");
    private ProgressDialog progressDialog;
    private NbcrvMission nbcrvMission = null;
    private int numberOfFilesToProcess = 0;
    private double processedFiles = 0.0;
    private UrnMap urnMap = null;
    private final Map<Integer, IEntity> urnEntityMap = new HashMap<>();

    @Override
    protected Pane createContentPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        scanForMissionTimeCheckBox.setSelected(true);

        gridPane.add(scanForMissionTimeCheckBox, 1, rowIndex);

        rowIndex++;

        missionFolderTextField.setEditable(false);
        missionFolderTextField.setMaxWidth(Double.MAX_VALUE);
        Button missionFolderButton = new Button("...");
        missionFolderButton.setOnAction(event -> selectMissionFolder());

        gridPane.add(new TextWithStyleClass("Mission Folder:"), 0, rowIndex);
        gridPane.add(missionFolderTextField, 1, rowIndex);
        gridPane.add(missionFolderButton, 2, rowIndex);

        rowIndex++;

        missionTextField.setEditable(false);
        missionTextField.setMaxWidth(Double.MAX_VALUE);

        gridPane.add(new TextWithStyleClass("Mission Name:"), 0, rowIndex);
        gridPane.add(missionTextField, 1, rowIndex);

        rowIndex++;

        importTypeTextField.setEditable(false);
        importTypeTextField.setMaxWidth(Double.MAX_VALUE);

        gridPane.add(new TextWithStyleClass("Import Type:"), 0, rowIndex);
        gridPane.add(importTypeTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Mission Time:"), 0, rowIndex);
        gridPane.add(userDefinedStartStop, 1, rowIndex);

        rowIndex++;

        VBox userDefinedStartStopVBox = new VBox(UiConstants.SPACING);
        userDefinedStartStopVBox.setPadding(new Insets(0, 0, 0, UiConstants.SPACING));
        userDefinedStartStopVBox.getChildren().addAll(
                new HBox(UiConstants.SPACING, new TextWithStyleClass("Start Time:"), startTimeDateTimePicker),
                new HBox(UiConstants.SPACING, new TextWithStyleClass("Stop Time:"), stopTimeDateTimePicker)
        );
        userDefinedStartStopVBox.disableProperty().bind(userDefinedStartStop.selectedProperty().not());
        gridPane.add(userDefinedStartStopVBox, 1, rowIndex);

        rowIndex++;

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(useAdmasTspiRadioButton, useOadmsTspiRadioButton);
        useAdmasTspiRadioButton.setSelected(true);

        gridPane.add(new TextWithStyleClass("TSPI Source:"), 0, rowIndex);
        gridPane.add(new HBox(UiConstants.SPACING, useAdmasTspiRadioButton, useOadmsTspiRadioButton), 1, rowIndex);

        rowIndex++;

        flirOnlyCheckBox.setOnAction(event -> updateImportTypeAndButton());

        HBox optionsHBox = new HBox(UiConstants.SPACING, includeUgvCheckBox, flirOnlyCheckBox);
        optionsHBox.setAlignment(Pos.CENTER_LEFT);

        gridPane.add(new TextWithStyleClass("Options:"), 0, rowIndex);
        gridPane.add(optionsHBox, 1, rowIndex);

        rowIndex++;

        HBox oadmsVideoTimeZoneOffsetHBox = new HBox(UiConstants.SPACING,
                new TextWithStyleClass("OADMS Video Offset: "),
                new TextWithStyleClass("Time Zone:"),
                oadmsTimeZoneOffsetSpinner,
                new TextWithStyleClass("Milliseconds:"),
                oadmsSecondsOffsetSpinner);
        oadmsVideoTimeZoneOffsetHBox.setPadding(new Insets(0, UiConstants.SPACING * 2.0, 0, 0));
        oadmsVideoTimeZoneOffsetHBox.setAlignment(Pos.CENTER_LEFT);

        gridPane.add(oadmsVideoTimeZoneOffsetHBox, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Median Filter Window Size:"), 0, rowIndex);
        gridPane.add(medianFilterWindowSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Clipped Start Size:"), 0, rowIndex);
        gridPane.add(clippedStartSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Max Concentration:"), 0, rowIndex);
        gridPane.add(maxConcentrationSpinner, 1, rowIndex);

        for (String tabName : tabNames) {
            tabPane.getTabs().add(new Tab(tabName, new ListView<>()));
        }
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        importButton.setOnAction(event -> startImport());
        importButton.setDisable(true);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        AnchorPane buttonAnchorPane = new AnchorPane(importButton, closeButton);
        AnchorPane.setLeftAnchor(importButton, 0.0);
        AnchorPane.setRightAnchor(closeButton, 0.0);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Import NBCRV Files"),
                new Separator(),
                gridPane,
                tabPane,
                new Separator(),
                buttonAnchorPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setFillWidth(true);
        mainVBox.setPrefWidth(800);

        return mainVBox;
    }

    private void selectMissionFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select mission folder");
        directoryChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("nbcrv"));
        File file = directoryChooser.showDialog(getStage());
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory("nbcrv", file.getParent());
            missionFolderTextField.setText(file.getAbsolutePath());
            loadMission(file);
        }
    }

    private void loadMission(final File missionFolder) {
        new Thread(() -> {
            nbcrvMission = null;

            for (Mission mission : DataManager.getMissions()) {
                if (mission.getName().equalsIgnoreCase(missionFolder.getName())) {
                    nbcrvMission = new NbcrvMission(mission);
                }
            }

            if (nbcrvMission == null) {
                nbcrvMission = new NbcrvMission(missionFolder.getName());
            }

            if (!nbcrvMission.load(missionFolder)) {
                nbcrvMission = null;
            }
//            loadFiles(missionFolder);
            if (scanForMissionTimeCheckBox.isSelected()) {
                scanFiles();
            }

            Platform.runLater(this::populateMission);
        }, "NbcrvImportController::loadMission").start();

        progressDialog = new ProgressDialog(getStage());
        progressDialog.createAndShow();
        progressDialog.addStatus("loading mission data...");
    }

    private void loadFiles(File missionFolder) {
        File[] directoryFiles = missionFolder.listFiles();
        if (directoryFiles != null) {
            for (File directory : directoryFiles) {
                if (directory.isDirectory()) {
                    List<File> files = getFiles(directory);
                    for (File file : files) {
                        nbcrvMission.addFile(new NbcrvImportFile(file, directory.getName(), nbcrvMission.isNew()));
                    }
                }
            }
        }
    }

    private void scanFiles() {
        for (NbcrvImportEntity nbcrvImportEntity : nbcrvMission.getNbcrvImportEntityList()) {
            for (NbcrvImportFile nbcrvImportFile : nbcrvImportEntity.getNbcrvImportFileList()) {
                if (nbcrvImportFile.getFileType() == NbcrvImportFile.FileType.OADMS_ROCKETSLED) {
                    RocketSledXmlFile rocketSledXmlFile = new RocketSledXmlFile(nbcrvImportFile.getFile());
                    if (rocketSledXmlFile.scan()) {
                        updateMissionStartTime(rocketSledXmlFile.getStartTime());
                        updateMissionStopTime(rocketSledXmlFile.getStopTime());
                    }
                }
            }
        }
    }

    private void updateMissionStartTime(long timestamp) {
        if (nbcrvMission.getStartTime() > timestamp) {
            nbcrvMission.setStartTime(timestamp);
        }
    }

    private void updateMissionStopTime(long timestamp) {
        if (nbcrvMission.getStopTime() < timestamp) {
            nbcrvMission.setStopTime(timestamp);
        }
    }

    private void populateMission() {
        missionTextField.setText(nbcrvMission.getName());

        updateImportTypeAndButton();

        startTimeDateTimePicker.setTimestamp(nbcrvMission.getStartTime());
        stopTimeDateTimePicker.setTimestamp(nbcrvMission.getStopTime());

        populateFileList();

        progressDialog.close();
    }

    private void updateImportTypeAndButton() {
        if (nbcrvMission != null) {
            String importType = nbcrvMission.isNew() ? "new" : "update";

            if (!nbcrvMission.hasRocketSledFile() && nbcrvMission.isNew() && !flirOnlyCheckBox.isSelected()) {
                importType += " RocketSled file missing";
                importTypeTextField.setStyle("-fx-text-fill: red;");
                importButton.setDisable(true);
            } else {
                importTypeTextField.setStyle(null);
                importButton.setDisable(false);
            }

            importTypeTextField.setText(importType);
        }
    }

    private void populateFileList() {
        Map<String, TableView<NbcrvImportFile>> tableViewMap = new HashMap<>();
        for (NbcrvImportEntity nbcrvImportEntity : nbcrvMission.getNbcrvImportEntityList()) {
            for (NbcrvImportFile nbcrvImportFile : nbcrvImportEntity.getNbcrvImportFileList()) {
                String tabName = switch (nbcrvImportFile.getFileType()) {
                    case ATC_GPS, ATC_3DM, FLIR_DETECTIONS, FLIR_FAULTS, FLIR_NBCRV, FLIR_RADIO_SUMMARY, FLIR_UGV,
                            OADMS_ROCKETSLED, OADMS_SIDECAR_ENCLOSURE, OADMS_WDL -> tabNames[0];
                    case FLIR_SCREENSHOT, FLIR_SNAPSHOT -> tabNames[1];
                    case ATC_VIDEO, OADMS_VIDEO -> tabNames[2];
                    case MANUAL_DATA -> tabNames[3];
                    default -> tabNames[4];
                };
                TableView<NbcrvImportFile> tableView = tableViewMap.get(tabName);
                if (tableView == null) {
                    tableView = createFileTableView();
                    tableViewMap.put(tabName, tableView);
                }
                tableView.getItems().add(nbcrvImportFile);
            }
        }
        for (NbcrvImportFile nbcrvImportFile : nbcrvMission.getFileList()) {
            String tabName = switch (nbcrvImportFile.getFileType()) {
                case BFT_LDIF, BFT_PCAP, BFT_UTO, OTCAM, UAS_GPS, URN_MAP -> tabNames[0];
                default -> tabNames[4];
            };
            TableView<NbcrvImportFile> tableView = tableViewMap.get(tabName);
            if (tableView == null) {
                tableView = createFileTableView();
                tableViewMap.put(tabName, tableView);
            }
            tableView.getItems().add(nbcrvImportFile);
        }

        for (int i = 0; i < tabNames.length; i++) {
            Tab tab = tabPane.getTabs().get(i);
            TableView<NbcrvImportFile> tableView = tableViewMap.get(tabNames[i]);
            if (tableView == null) {
                tableView = new TableView<>();
            }
            tab.setContent(tableView);
        }
    }

    private TableView<NbcrvImportFile> createFileTableView() {
        TableColumn<NbcrvImportFile, String> fileNameTableColumn = new TableColumn<>("File Name");
        fileNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<NbcrvImportFile, NbcrvImportFile.FileType> fileTypeTableColumn = new TableColumn<>("File Type");
        fileTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("fileType"));

        TableColumn<NbcrvImportFile, Boolean> isNewTableColumn = new TableColumn<>("Is New");
        isNewTableColumn.setCellValueFactory(new PropertyValueFactory<>("isNew"));

        TableColumn<NbcrvImportFile, Long> startTimeTableColumn = new TableColumn<>("Start Time");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeTableColumn.setCellFactory(new TimestampTableColumnCellFactory<>());

        TableView<NbcrvImportFile> tableView = new TableView<>();
        tableView.getColumns().add(fileNameTableColumn);
        tableView.getColumns().add(fileTypeTableColumn);
        tableView.getColumns().add(isNewTableColumn);
        tableView.getColumns().add(startTimeTableColumn);

        fileNameTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(14 / 44.0));
        fileTypeTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(13 / 44.0));
        isNewTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(5 / 44.0));
        startTimeTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(10 / 44.0));

        return tableView;
    }

    private List<File> getFiles(File directory) {
        List<File> files = new ArrayList<>();
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles == null) {
            return files;
        }

        for (File file : directoryFiles) {
            if (file.isDirectory()) {
                files.addAll(getFiles(file));
            } else {
                files.add(file);
            }
        }
        return files;
    }

    private void startImport() {
        nbcrvMission.setIncludeUgv(includeUgvCheckBox.isSelected());
        nbcrvMission.setFlirOnly(flirOnlyCheckBox.isSelected());
        if (userDefinedStartStop.isSelected()) {
            nbcrvMission.setStartTime(startTimeDateTimePicker.getTimestamp());
            nbcrvMission.setStopTime(stopTimeDateTimePicker.getTimestamp());
        }

        SettingsManager.getSettings().getNbcrvSettings().setOadmsTimeZoneOffset(oadmsTimeZoneOffsetSpinner.getValue());
        SettingsManager.getSettings().getNbcrvSettings().setOadmsVideoOffset(oadmsSecondsOffsetSpinner.getValue());
        SettingsManager.getSettings().getNbcrvSettings().setLidarMedianFilterWindowSize(medianFilterWindowSpinner.getValue());
        SettingsManager.getSettings().getNbcrvSettings().setLidarClippedStartSize(clippedStartSpinner.getValue());
        SettingsManager.getSettings().getNbcrvSettings().setLidarMaxConcentration(maxConcentrationSpinner.getValue());

        Thread thread = new Thread(() -> {
            long importTime = System.currentTimeMillis();
            updateStatus("creating mission...");

            numberOfFilesToProcess = nbcrvMission.getTotalFilesToProcess();
            for (NbcrvImportEntity nbcrvImportEntity : nbcrvMission.getNbcrvImportEntityList()) {
                numberOfFilesToProcess += nbcrvImportEntity.getTotalFilesToProcess();
            }
            processedFiles = 0.0;

            Mission mission = nbcrvMission.getMission();
            if (mission == null) {
                mission = new Mission(nbcrvMission.getName(), nbcrvMission.getStartTime(), nbcrvMission.getStopTime());
                DataManager.addMission(mission);
            }

            processUrnMap(mission);

            updateStatus("processing NBCRV entities...");
            for (NbcrvImportEntity nbcrvImportEntity : nbcrvMission.getNbcrvImportEntityList()) {
                String entityName = nbcrvImportEntity.getName() + " (" + nbcrvMission.getName() + ")";
                updateStatus(String.format(" processing entity %s ...", entityName));
                NbcrvEntity nbcrvEntity = getOrCreateEntity(entityName);
                processAtcData(mission, nbcrvEntity, nbcrvImportEntity);
                processOadmsData(mission, nbcrvEntity, nbcrvImportEntity);
                processFlirData(mission, nbcrvEntity, nbcrvImportEntity);
                processManualData(mission, nbcrvEntity, nbcrvImportEntity);
                saveEntityDetails(nbcrvEntity, entityName, nbcrvImportEntity.getName());
            }
//            String entityName = "NBCRV (" + nbcrvMission.getName() + ")";
//            NbcrvEntity nbcrvEntity = getOrCreateEntity(entityName);

//            updateStatus("processing files...");
//            processAtcData(mission, nbcrvEntity);
//            processOadmsData(mission, nbcrvEntity);
//            processFlirData(mission, nbcrvEntity);
            processUasData(mission);
            processOtcamData(mission);
            processBftData(mission);
//            processManualData(mission, nbcrvEntity);

//            updateStatus("saving data...");
//            if (nbcrvMission.isNew()) {
//                saveEntityDetails(nbcrvEntity, entityName);
//                if (nbcrvMission.isIncludeUgv()) {
//                    saveUgvEntityDetails();
//                }
//            }

            updateStatus("import time: " + ((System.currentTimeMillis() - importTime) / 1000.0));
            Platform.runLater(() -> progressDialog.setComplete(true));
        }, "NbcrvImportController::startImport");
        thread.start();

        progressDialog = new ProgressDialog(getStage(), this::cancelImport);
        progressDialog.createAndShow();
        progressDialog.setProgress(0);
    }

    private void processUrnMap(Mission mission) {
        updateStatus("processing urn map...");

        List<NbcrvImportFile> bftUrnMapFiles = getFilesByType(NbcrvImportFile.FileType.URN_MAP);
        if (!bftUrnMapFiles.isEmpty()) {
            urnMap = new UrnMap(bftUrnMapFiles.get(0).getFile());
            urnMap.processFile();
        }

        updateStatus("processing urn map complete");
    }

    private void processAtcData(Mission mission, NbcrvEntity nbcrvEntity, NbcrvImportEntity nbcrvImportEntity) {
        updateStatus("processing atc data...");

        List<NbcrvImportFile> atcGpsFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.ATC_GPS);
        List<NbcrvImportFile> atc3dmFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.ATC_3DM);
        if (!atcGpsFiles.isEmpty()) {
            processAtcGpsFiles(atcGpsFiles, atc3dmFiles, mission, nbcrvEntity);
        }

//        if (!atc3dmFiles.isEmpty()) {
//            processAtc3dmFiles(atc3dmFiles, mission, nbcrvEntity);
//        }

        List<NbcrvImportFile> atcVideoFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.ATC_VIDEO);
        if (!atcVideoFiles.isEmpty()) {
            processAtcVideoFiles(atcVideoFiles, mission, nbcrvEntity);
        }

        updateStatus("processing atc data complete");
    }

    private void processAtcGpsFiles(List<NbcrvImportFile> atcGpsFiles, List<NbcrvImportFile> atc3dmFiles,
                                    Mission mission, NbcrvEntity nbcrvEntity) {
        for (NbcrvImportFile nbcrvImportFile : atcGpsFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            AdmasGpsCsv admasGpsCsv = new AdmasGpsCsv(nbcrvImportFile.getFile());
            admasGpsCsv.setFilter1Hz(true);
            if (!admasGpsCsv.processFile()) {
                logProcessingError("processAtcGpsFiles", nbcrvImportFile.getFileName());
                continue;
            }

            List<TspiData> tspiDataList = admasGpsCsv.getTspiDataList();
            File matching3dmFile = getMatching3dmFile(nbcrvImportFile.getFileName(), atc3dmFiles);
            if (matching3dmFile != null) {
                Admas3dmCsv admas3dmCsv = new Admas3dmCsv(matching3dmFile, tspiDataList);
                admas3dmCsv.setFilter1Hz(true);
                if (!admas3dmCsv.processFile()) {
                    logProcessingError("processAtcGpsFiles", nbcrvImportFile.getFileName());
                } else {
                    tspiDataList = admas3dmCsv.getTspiDataList();
                    DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                            mission.getTimestamp(), mission.getStopTime());
                    DataManager.addMissionDataSource(mission, dataSource);
                }
            }

            if (!userDefinedStartStop.isSelected() && !admasGpsCsv.getTspiDataList().isEmpty()) {
                updateMissionStartTime(admasGpsCsv.getTspiDataList().get(0).getTimestamp());
                updateMissionStopTime(admasGpsCsv.getTspiDataList().get(admasGpsCsv.getTspiDataList().size() - 1).getTimestamp());
            }

            DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                    mission.getTimestamp(), mission.getStopTime());
            DataManager.addMissionDataSource(mission, dataSource);

            for (TspiData tspiData : tspiDataList) {
                nbcrvEntity.addTspi(tspiData);
                DatabaseLogger.addTspiData(tspiData, nbcrvEntity.getEntityId(), dataSource.getId());
            }
        }
    }

    private File getMatching3dmFile(String fileName, List<NbcrvImportFile> atc3dmFiles) {
        String[] fileNameParts1 = fileName.split("_");
        if (fileNameParts1.length < 2) {
            return null;
        }
        for (NbcrvImportFile nbcrvImportFile : atc3dmFiles) {
            String[] fileNameParts2 = fileName.split("_");
            if (fileNameParts2.length < 2) {
                continue;
            }
            if (fileNameParts1[1].equalsIgnoreCase(fileNameParts2[1])) {
                return nbcrvImportFile.getFile();
            }
        }
        return null;
    }

    private void processAtc3dmFiles(List<NbcrvImportFile> atcedmFiles, Mission mission, NbcrvEntity nbcrvEntity) {
//        for (NbcrvImportFile nbcrvImportFile : atcedmFiles) {
//            updateStatus("processing " + nbcrvImportFile.getFileName() + "...", increaseAndGetProcessStatus());
//
//            Admas3dmCsv admas3dmCsv = new Admas3dmCsv(nbcrvImportFile.getFile());
//            if (!admas3dmCsv.processFile()) {
//                logProcessingError("processAtc3dmFilesFiles", nbcrvImportFile.getFileName());
//                continue;
//            }
//
//            if (!userDefinedStartStop.isSelected() && !admas3dmCsv.getTspiDataList().isEmpty()) {
//                updateMissionStartTime(admas3dmCsv.getTspiDataList().get(0).getTimestamp());
//                updateMissionStopTime(admas3dmCsv.getTspiDataList().get(admas3dmCsv.getTspiDataList().size() - 1).getTimestamp());
//            }
//
//            DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFileName(),
//                    mission.getTimestamp(), mission.getStopTime());
//            DataManager.addMissionDataSource(mission, dataSource);
//
//            for (TspiData tspiData : admas3dmCsv.getTspiDataList()) {
//                nbcrvEntity.addTspi(tspiData);
//                DatabaseLogger.addTspiData(tspiData, nbcrvEntity.getEntityId(), dataSource.getId());
//            }
//        }
    }

    private void processAtcVideoFiles(List<NbcrvImportFile> videoFiles, Mission mission, NbcrvEntity nbcrvEntity) {
        for (NbcrvImportFile nbcrvImportFile : videoFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            long startTime = nbcrvImportFile.getStartTime();
            long stopTime = startTime + (long) getDuration(nbcrvImportFile.getFile());
            String mediaSet = nbcrvImportFile.getFile().getParentFile().getName();
            MediaFile mediaFile = new MediaFile(nbcrvImportFile.getFile().getAbsolutePath(), startTime, stopTime,
                    "ATC", mediaSet, false);
            nbcrvEntity.getMediaCollection().addMediaFile(mediaFile);
            DataSource mediaDataSource = DatabaseLogger.addMedia(mediaFile, nbcrvEntity.getEntityId());
            DataManager.addMissionDataSource(mission, mediaDataSource);
        }
    }

    private void processOadmsData(Mission mission, NbcrvEntity nbcrvEntity, NbcrvImportEntity nbcrvImportEntity) {
        updateStatus("processing oadms data...");

        List<NbcrvImportFile> rocketSledFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.OADMS_ROCKETSLED);
        if (!rocketSledFiles.isEmpty()) {
            processRocketSledFiles(rocketSledFiles, mission, nbcrvEntity);
        }

        List<NbcrvImportFile> sidecarEnclosureFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.OADMS_SIDECAR_ENCLOSURE);
        if (!sidecarEnclosureFiles.isEmpty()) {
            processSidecarEnclosureFiles(sidecarEnclosureFiles, mission);
        }

        List<NbcrvImportFile> wdlFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.OADMS_WDL);
        if (!wdlFiles.isEmpty()) {
            processWdlFiles(wdlFiles, mission);
        }

        List<NbcrvImportFile> videoFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.OADMS_VIDEO);
        if (!videoFiles.isEmpty()) {
            processOadmsVideoFiles(videoFiles, mission, nbcrvEntity);
        }

        updateStatus("processing oadms data complete");
    }

    private void processRocketSledFiles(List<NbcrvImportFile> rocketSledFiles, Mission mission, NbcrvEntity nbcrvEntity) {
        NbcrvEntity ugvEntity = null;
        if (nbcrvMission.isIncludeUgv()) {
            String ugvEntityName = "NBCRV UGV (" + nbcrvMission.getName() + ")";
            ugvEntity = getOrCreateEntity(ugvEntityName);
        }

        for (NbcrvImportFile nbcrvImportFile : rocketSledFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            RocketSledXmlFile rocketSledXmlFile = new RocketSledXmlFile(nbcrvImportFile.getFile());
            if (!rocketSledXmlFile.process()) {
                logProcessingError("processRocketSledFile", nbcrvImportFile.getFileName());
                continue;
            }

            DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                    rocketSledXmlFile.getStartTime(), rocketSledXmlFile.getStopTime());
            DataManager.addMissionDataSource(mission, dataSource);

            if (useOadmsTspiRadioButton.isSelected()) {
                for (TspiData tspiData : rocketSledXmlFile.getSledTspiList()) {
                    nbcrvEntity.addTspi(tspiData);
                    DatabaseLogger.addTspiData(tspiData, nbcrvEntity.getEntityId(), dataSource.getId());
                }

                if (ugvEntity != null) {
                    for (TspiData tspiData : rocketSledXmlFile.getUgvTspiList()) {
                        ugvEntity.addTspi(tspiData);
                        DatabaseLogger.addTspiData(tspiData, ugvEntity.getEntityId(), dataSource.getId());
                    }
                }
            }

            for (NbcrvDetection nbcrvDetection : rocketSledXmlFile.getNbcrvEventList()) {
                nbcrvEntity.addNbcrvDetection(nbcrvDetection);
            }
            DatabaseLogger.addNbcrvEvents(rocketSledXmlFile.getNbcrvEventList(), nbcrvEntity.getEntityId(), dataSource.getId());

            for (Device newDevice : rocketSledXmlFile.getSledDeviceList()) {
                Device device = nbcrvEntity.getOrCreateDevice(newDevice.getName());
                for (DeviceState deviceState : newDevice.getDeviceStates()) {
                    device.addDeviceState(deviceState);
                }
                DatabaseLogger.addNbcrvDevice(device, nbcrvEntity.getEntityId(), dataSource.getId());
            }

            if (ugvEntity != null) {
                for (Device newDevice : rocketSledXmlFile.getUgvDeviceList()) {
                    Device device = ugvEntity.getOrCreateDevice(newDevice.getName());
                    for (DeviceState deviceState : newDevice.getDeviceStates()) {
                        device.addDeviceState(deviceState);
                    }
                    DatabaseLogger.addNbcrvDevice(device, ugvEntity.getEntityId(), dataSource.getId());
                }
            }
        }
    }

    private IEntity getSidecarEntityFromFileName(String filename) {
        IEntity entity = null;
        String[] fileNameParts = filename.toUpperCase().split("-");
        if (fileNameParts.length > 5) {
            String name = fileNameParts[fileNameParts.length - 5];
            String entityName = name + " (" + nbcrvMission.getName() + ")";
            entity = nbcrvMission.getEntity(entityName);
            if (entity != null) {
                return entity;
            }
            if (name.equals("WDL") || name.equals("LIDAR")) {
                entity = getOrCreateWdlEntity(entityName);
            } else if (!name.contains("NBCRV") && !name.contains("SLED")) {
                entity = getOrCreateSidecarEntity(entityName);
            }
            if (entity != null) {
                nbcrvMission.addEntity(entityName, entity);
                saveSidecarEntityDetails(entity, entityName);
            }
        }
        return entity;
    }

    private void processSidecarEnclosureFiles(List<NbcrvImportFile> sidecarEnclosureFiles, Mission mission) {
        for (NbcrvImportFile nbcrvImportFile : sidecarEnclosureFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            IEntity entity = getSidecarEntityFromFileName(nbcrvImportFile.getFileName());
            SidecarEnclosureXmlFile sidecarEnclosureXmlFile = new SidecarEnclosureXmlFile(nbcrvImportFile.getFile());
            if (entity == null || !sidecarEnclosureXmlFile.process()) {
                logProcessingError("processSidecarEnclosureFiles", nbcrvImportFile.getFileName());
                continue;
            }

            DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                    sidecarEnclosureXmlFile.getStartTime(), sidecarEnclosureXmlFile.getStopTime());
            DataManager.addMissionDataSource(mission, dataSource);

            for (TspiData tspiData : sidecarEnclosureXmlFile.getTspiData()) {
                entity.addTspi(tspiData);
                DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), dataSource.getId());
            }
        }
    }

    private void processWdlFiles(List<NbcrvImportFile> wdlFiles, Mission mission) {
        String entityName = "WDL (" + nbcrvMission.getName() + ")";
        IEntity entity = nbcrvMission.getEntity(entityName);
        if (!(entity instanceof WdlEntity)) {
            entityName = "LIDAR (" + nbcrvMission.getName() + ")";
            entity = nbcrvMission.getEntity(entityName);
            if (!(entity instanceof WdlEntity)) {
                return;
            }
        }

        WdlEntity wdlEntity = (WdlEntity) entity;

        for (NbcrvImportFile nbcrvImportFile : wdlFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            WdlXmlFile wdlXmlFile = new WdlXmlFile(nbcrvImportFile.getFile());
            if (!wdlXmlFile.processFast()) {
                logProcessingError("processWdlFiles", nbcrvImportFile.getFileName());
                continue;
            }

            DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                    wdlXmlFile.getStartTime(), wdlXmlFile.getStopTime());
            DataManager.addMissionDataSource(mission, dataSource);

            wdlEntity.addWdlReadings(wdlXmlFile.getWdlReadingList());
            DatabaseLogger.addWdlReadings(wdlXmlFile.getWdlReadingList(), wdlEntity.getEntityId(), dataSource.getId());
        }
    }

    private void processOadmsVideoFiles(List<NbcrvImportFile> videoFiles, Mission mission, IEntity entity) {
        for (NbcrvImportFile nbcrvImportFile : videoFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            //NBCRV-SUUMWO-TS-SUT1-T4-Daniel G. Ondercin CTR-2023-05-15_23-38-03
            //2022-10-04_00-06-39
            String[] values = nbcrvImportFile.getFileName().split("[-_]");
            if (values.length < 7) {
                logProcessingError("processOadmsVideoFiles", nbcrvImportFile.getFileName());
                continue;
            }
            String mediaSet = values[values.length - 7];

            long startTime = nbcrvImportFile.getStartTime();
            long stopTime = startTime + (long) getDuration(nbcrvImportFile.getFile());
            MediaFile mediaFile = new MediaFile(nbcrvImportFile.getFile().getAbsolutePath(), startTime, stopTime,
                    "OADMS", mediaSet, false);
            entity.getMediaCollection().addMediaFile(mediaFile);
            DataSource mediaDataSource = DatabaseLogger.addMedia(mediaFile, entity.getEntityId());
            DataManager.addMissionDataSource(mission, mediaDataSource);
        }
    }

    private void processFlirData(Mission mission, NbcrvEntity nbcrvEntity, NbcrvImportEntity nbcrvImportEntity) {
        updateStatus("processing flir data...");

        List<NbcrvImportFile> flirNbcrvFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.FLIR_NBCRV);
        if (!flirNbcrvFiles.isEmpty()) {
            processFlirNbcrvFiles(flirNbcrvFiles, mission, nbcrvEntity);
        }

        List<NbcrvImportFile> flirDetectionsFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.FLIR_DETECTIONS);
        if (!flirDetectionsFiles.isEmpty()) {
            processFlirDetectionsFiles(flirDetectionsFiles, mission, nbcrvEntity);
        }

        List<NbcrvImportFile> videoFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.FLIR_SCREENSHOT);
        videoFiles.addAll(getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.FLIR_SNAPSHOT));
        if (!videoFiles.isEmpty()) {
            processTimedFiles(videoFiles, nbcrvEntity);
        }

        updateStatus("processing flir data complete");
    }

    private void processFlirNbcrvFiles(List<NbcrvImportFile> flirNbcrvFiles, Mission mission, NbcrvEntity nbcrvEntity) {
        for (NbcrvImportFile nbcrvImportFile : flirNbcrvFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            NbcrvFile nbcrvFile = new NbcrvFile(nbcrvImportFile.getFile());
            if (!nbcrvFile.processFile()) {
                logProcessingError("processFlirNbcrvFiles", nbcrvImportFile.getFileName());
                continue;
            }

            DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                    mission.getTimestamp(), mission.getStopTime());
            DataManager.addMissionDataSource(mission, dataSource);

            nbcrvEntity.addNbcrvStates(nbcrvFile.getNbcrvStates());
            DatabaseLogger.addNbcrvStates(nbcrvFile.getNbcrvStates(), nbcrvEntity.getEntityId(), dataSource.getId());

            if (nbcrvMission.isFlirOnly()) {
                for (TspiData tspiData : nbcrvFile.getTspiDataList()) {
                    nbcrvEntity.addTspi(tspiData);
                    DatabaseLogger.addTspiData(tspiData, nbcrvEntity.getEntityId(), dataSource.getId());
                }
            }

            for (Device newDevice : nbcrvFile.getDeviceList()) {
                if (newDevice.getName().toLowerCase().startsWith("merlin") ||
                        newDevice.getName().toLowerCase().startsWith("viper") ||
                        newDevice.getName().toLowerCase().startsWith("jcad")) {
                    Device device = nbcrvEntity.getDevice(newDevice.getName());
                    if (device == null) {
                        device = newDevice;
                        nbcrvEntity.addDevice(device);
                    } else {
                        for (DeviceState deviceState : newDevice.getDeviceStates()) {
                            device.addDeviceState(deviceState);
                        }
                    }
                    DatabaseLogger.addNbcrvDevice(device, nbcrvEntity.getEntityId(), dataSource.getId());
                }
            }
        }
    }

    private void processFlirDetectionsFiles(List<NbcrvImportFile> flirDetectionsFiles, Mission mission, NbcrvEntity nbcrvEntity) {
        for (NbcrvImportFile nbcrvImportFile : flirDetectionsFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            DetectionsFile detectionsFile = new DetectionsFile(nbcrvImportFile.getFile());
            if (!detectionsFile.processFile()) {
                logProcessingError("processFlirDetectionsFiles", nbcrvImportFile.getFileName());
                continue;
            }

            List<NbcrvDetection> nbcrvDetections = new ArrayList<>();
            for (NbcrvDetection nbcrvDetection : detectionsFile.getDetectionList()) {
                if (nbcrvDetection.getDeviceName().toLowerCase().startsWith("merlin") ||
                        nbcrvDetection.getDeviceName().toLowerCase().startsWith("viper") ||
                        nbcrvDetection.getDeviceName().toLowerCase().startsWith("jcad")) {
                    nbcrvDetections.add(nbcrvDetection);
                    nbcrvEntity.addNbcrvDetection(nbcrvDetection);
                }
            }

            List<RadNucState> radNucStates = new ArrayList<>();
            for (RadNucState radNucState : detectionsFile.getRadNucStateList()) {
                radNucStates.add(radNucState);
                nbcrvEntity.addRadNucState(radNucState);
            }

            if (!nbcrvDetections.isEmpty() || !radNucStates.isEmpty()) {
                DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                        mission.getTimestamp(), mission.getStopTime());
                DataManager.addMissionDataSource(mission, dataSource);
                if (!nbcrvDetections.isEmpty()) {
                    DatabaseLogger.addNbcrvEvents(nbcrvDetections, nbcrvEntity.getEntityId(), dataSource.getId());
                }
                if (!radNucStates.isEmpty()) {
                    DatabaseLogger.addRadNucStates(radNucStates, nbcrvEntity.getEntityId(), dataSource.getId());
                }
            }
        }
    }

    private void processTimedFiles(List<NbcrvImportFile> videoFiles, NbcrvEntity nbcrvEntity) {
        for (NbcrvImportFile nbcrvImportFile : videoFiles) {
            updateStatusProcessFile(nbcrvImportFile);

            TimedFile timedFile = new TimedFile(nbcrvImportFile.getStartTime(), nbcrvImportFile.getFile(),
                    TimedFile.FileType.IMAGE, "FLIR Screenshot");
            nbcrvEntity.addTimedImage(timedFile);
            DatabaseLogger.addTimedFile(timedFile, nbcrvEntity.getEntityId());
        }
    }

    private void processManualData(Mission mission, NbcrvEntity nbcrvEntity, NbcrvImportEntity nbcrvImportEntity) {
        updateStatus("processing manual data...");

        List<NbcrvImportFile> manualDataFiles = getFilesByType(nbcrvImportEntity.getNbcrvImportFileList(), NbcrvImportFile.FileType.MANUAL_DATA);
        if (!manualDataFiles.isEmpty()) {
            for (NbcrvImportFile nbcrvImportFile : manualDataFiles) {
                TimedFile.FileType fileType = TimedFile.FileType.UNKNOWN;
                long timestamp = 0;
                String fileGroup = "";
                if (nbcrvImportFile.getFileName().toLowerCase().endsWith(".pdf")) {
                    fileType = TimedFile.FileType.PDF;
                    timestamp = TirReader.getTime(nbcrvImportFile.getFile());
                    fileGroup = "TIR";
                } else if (nbcrvImportFile.getFileName().toLowerCase().endsWith(".csv")) {
                    fileType = TimedFile.FileType.CSV;
                    timestamp = mission.getTimestamp();
                    fileGroup = "Manual Data";
                }
                TimedFile timedFile = new TimedFile(timestamp, nbcrvImportFile.getFile(), fileType, fileGroup);
                nbcrvEntity.addTimedFile(timedFile);
                DatabaseLogger.addTimedFile(timedFile, nbcrvEntity.getEntityId());
            }
        }

        updateStatus("processing manual data complete");
    }

    private void processUasData(Mission mission) {
        updateStatus("processing uas data...");

        List<NbcrvImportFile> uasGpsFiles = getFilesByType(NbcrvImportFile.FileType.UAS_GPS);
        if (!uasGpsFiles.isEmpty()) {
            for (NbcrvImportFile nbcrvImportFile : uasGpsFiles) {
                updateStatusProcessFile(nbcrvImportFile);

                UasGpsFile uasGpsFile = new UasGpsFile(nbcrvImportFile.getFile());
                if (uasGpsFile.processFile()) {
                    String entityName = uasGpsFile.getName() + " (" + nbcrvMission.getName() + ")";
                    IEntity entity = getUasEntity(entityName);
                    if (entity == null) {
                        EntityId entityId = DataManager.getNextAvailableEntityId(Defaults.APP_ID_NBCRV);
                        entity = new PlaybackEntity(entityId);
                        DataSource dataSource = DataManager.createDataSource(nbcrvMission.getName(),
                                nbcrvMission.getStartTime(), nbcrvMission.getStopTime());

                        EntityManager.addEntity(entity, true);

                        EntityType entityType = new EntityType(1, 2, 225, 50, 0, 0, 0);
                        String militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
                        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, Affiliation.FRIENDLY);

                        EntityDetail entityDetail = new EntityDetail.Builder()
                                .setTimestamp(nbcrvMission.getStartTime())
                                .setName(entityName)
                                .setSource("FLIR")
                                .setEntityType(entityType)
                                .setMilitarySymbol(militarySymbol)
                                .setAffiliation(Affiliation.FRIENDLY)
                                .build();
                        entity.addEntityDetail(entityDetail);
                        DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), dataSource.getId());

                        EntityScope entityScope = new EntityScope(nbcrvMission.getStartTime(), nbcrvMission.getStopTime());
                        entity.addEntityScope(entityScope);
                        DatabaseLogger.addEntityScope(entityScope, entity.getEntityId(), dataSource.getId());
                    }

                    DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                            mission.getTimestamp(), mission.getStopTime());
                    DataManager.addMissionDataSource(mission, dataSource);

                    for (TspiData tspiData : uasGpsFile.getTspiDataList()) {
                        entity.addTspi(tspiData);
                        DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), dataSource.getId());
                    }
                }
            }
        }

        updateStatus("processing flir data complete");
    }

    private void processOtcamData(Mission mission) {
        updateStatus("processing otcam data...");

        List<NbcrvImportFile> otcamFiles = getFilesByType(NbcrvImportFile.FileType.OTCAM);
        if (!otcamFiles.isEmpty()) {
            for (NbcrvImportFile nbcrvImportFile : otcamFiles) {
                updateStatusProcessFile(nbcrvImportFile);

                OtcamUtility otcamUtility = new OtcamUtility(nbcrvImportFile.getFile().getAbsolutePath());
                if (otcamUtility.getEntityIds().isEmpty()) {
                    continue;
                }
                DataSource dataSource = DataManager.createDataSource(nbcrvImportFile.getFile().getAbsolutePath(),
                        otcamUtility.getStartTime(), otcamUtility.getStopTime());
                DataManager.addMissionDataSource(mission, dataSource);
                otcamUtility.importFile(dataSource.getId());
            }
        }

        updateStatus("processing otcam data complete");
    }

    private void processBftData(Mission mission) {
        updateStatus("processing bft data...");

        List<NbcrvImportFile> bftPcapFiles = getFilesByType(NbcrvImportFile.FileType.BFT_PCAP);
        if (!bftPcapFiles.isEmpty()) {
            for (NbcrvImportFile nbcrvImportFile : bftPcapFiles) {
                updateStatusProcessFile(nbcrvImportFile);

                processBftPcapFile(mission, nbcrvImportFile.getFile());
            }
        }

        updateStatus("processing bft data complete");
    }

    private final Map<Integer, AdHocEntity> bftEntityMap = new HashMap<>();

    private void processBftPcapFile(Mission mission, File file) {
        ProtocolParser protocolParser = new ProtocolParser();
        List<VmfMessage> messages = protocolParser.processPcap(file);
        if (messages.isEmpty()) {
            return;
        }

        DataSource dataSource = DataManager.createDataSource(file.getAbsolutePath(),
                mission.getTimestamp(), mission.getStopTime());
        DataManager.addMissionDataSource(mission, dataSource);

        List<String> notHandledMessageTypes = new ArrayList<>();
        for (VmfMessage message : messages) {
            if (message instanceof K0101) {
                processK0101((K0101) message, dataSource);
            } else if (message instanceof K0501) {
                processK0501((K0501) message);
            } else if (message instanceof Sdsa) {
                processSdsa((Sdsa) message);
            } else if (!notHandledMessageTypes.contains(message.getHeader().getMessageType().getName())) {
                notHandledMessageTypes.add(message.getHeader().getMessageType().getName());
                Logger.getGlobal().log(Level.INFO, message.getHeader().getMessageType().getName());
            }
        }
    }

    private void processK0101(K0101 k0101, DataSource dataSource) {
        IEntity entity = getSenderEntity(k0101);
        if (entity != null) {
            C2MessageEvent c2MessageEvent = new C2MessageEvent(entity, k0101);
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

    private void processK0501(K0501 k0501) {
        for (K0501.PositionReport report : k0501.getReports()) {
            if (report.getUrn() <= 0) {
                continue;
            }

            AdHocEntity adHocEntity = bftEntityMap.get(report.getUrn());
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                adHocEntity.setName(String.valueOf(report.getUrn()));
                adHocEntity.setSource("BFT");
                adHocEntity.setAffiliation(Affiliation.FRIENDLY);
                adHocEntity.setUrn(report.getUrn());
                bftEntityMap.put(report.getUrn(), adHocEntity);
            }

            if (report.getPosition().getLatitude().getDegrees() != 0 && report.getPosition().getLongitude().getDegrees() != 0) {
                adHocEntity.getTspiDataList().add(new TspiData(report.getTime().getTimeInMillis(), report.getPosition()));
            }
        }
    }

    private void processSdsa(Sdsa sdsa) {
        for (Sdsa.Record sdsaRecord : sdsa.getRecords()) {
            if (sdsaRecord.getUrn() <= 0) {
                continue;
            }

            AdHocEntity adHocEntity = bftEntityMap.get(sdsaRecord.getUrn());
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                bftEntityMap.put(sdsaRecord.getUrn(), adHocEntity);
            }

            EntityType entityType = VmfDictionary.getEntityType(
                    sdsaRecord.getDimension(),
                    sdsaRecord.getNationality(),
                    sdsaRecord.getType(),
                    sdsaRecord.getSubType());
            adHocEntity.setEntityType(entityType);

            adHocEntity.setAffiliation(Affiliation.FRIENDLY);
            adHocEntity.setUrn(sdsaRecord.getUrn());
            adHocEntity.setName(String.valueOf(sdsaRecord.getUrn()));

            // set name
            if (!sdsaRecord.getFullName().isEmpty()) {
                adHocEntity.setName(sdsaRecord.getFullName());
            } else if (!sdsaRecord.getShortName().isEmpty()) {
                adHocEntity.setName(sdsaRecord.getShortName());
            } else if (!sdsaRecord.getAlias().isEmpty()) {
                adHocEntity.setName(sdsaRecord.getAlias());
            }

            // set icon
            adHocEntity.setMilitarySymbol(sdsaRecord.getSymbol());
        }
    }

    private List<NbcrvImportFile> getFilesByType(NbcrvImportFile.FileType fileType) {
        List<NbcrvImportFile> files = new ArrayList<>();
        for (NbcrvImportFile nbcrvImportFile : nbcrvMission.getFileList()) {
            if (nbcrvImportFile.getFileType() == fileType) {
                files.add(nbcrvImportFile);
            }
        }
        return files;
    }

    private List<NbcrvImportFile> getFilesByType(List<NbcrvImportFile> nbcrvImportFileList, NbcrvImportFile.FileType fileType) {
        List<NbcrvImportFile> files = new ArrayList<>();
        for (NbcrvImportFile nbcrvImportFile : nbcrvImportFileList) {
            if (nbcrvImportFile.getFileType() == fileType) {
                files.add(nbcrvImportFile);
            }
        }
        return files;
    }

    private double increaseAndGetProcessStatus() {
        return processedFiles++ / numberOfFilesToProcess;
    }

    private void logProcessingError(String method, String filename) {
        String message = "error processing " + filename + "!!!";
        updateStatus(message);
        message = "NbcrvImportController::" + method + ":" + message;
        Logger.getGlobal().log(Level.WARNING, message);
    }

    private double getDuration(File videoFile) {
        try {
            FFprobe fFprobe = new FFprobe("external\\ffmpeg\\bin\\ffprobe");
            FFmpegProbeResult probeResult = fFprobe.probe(videoFile.getAbsolutePath());
            return probeResult.format.duration * 1000;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "NbcrvImportController::getDuration", e);
        }
        return 0;
    }

    private void saveEntityDetails(NbcrvEntity nbcrvEntity, String entityName, String entityNameWithoutMission) {
        DataSource dataSource = DataManager.createDataSource(nbcrvMission.getName(),
                nbcrvMission.getStartTime(), nbcrvMission.getStopTime());

        EntityManager.addEntity(nbcrvEntity, true);

        int urn = 0;
        if (urnMap != null) {
            urn = urnMap.getUrn(entityNameWithoutMission);
        }

        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(nbcrvMission.getStartTime())
                .setName(entityName)
                .setSource(nbcrvMission.getName())
                .setEntityType(new EntityType(1, 1, 225, 2, 5, 32, 0))
                .setMilitarySymbol("SFGPEVAL------G")
                .setAffiliation(Affiliation.FRIENDLY)
                .setUrn(urn)
                .build();
        nbcrvEntity.addEntityDetail(entityDetail);
        DatabaseLogger.addEntityDetail(entityDetail, nbcrvEntity.getEntityId(), dataSource.getId());

        EntityScope entityScope = new EntityScope(nbcrvMission.getStartTime(), nbcrvMission.getStopTime());
        nbcrvEntity.addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, nbcrvEntity.getEntityId(), dataSource.getId());
    }

    private void saveSidecarEntityDetails(IEntity entity, String entityName) {
        DataSource dataSource = DataManager.createDataSource(nbcrvMission.getName(),
                nbcrvMission.getStartTime(), nbcrvMission.getStopTime());

        EntityManager.addEntity(entity, true);

        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(nbcrvMission.getStartTime())
                .setName(entityName)
                .setSource("OADMS")
                .setAffiliation(Affiliation.NONPARTICIPANT)
                .build();
        entity.addEntityDetail(entityDetail);
        DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), dataSource.getId());

        EntityScope entityScope = new EntityScope(nbcrvMission.getStartTime(), nbcrvMission.getStopTime());
        entity.addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, entity.getEntityId(), dataSource.getId());
    }

    private void saveUgvEntityDetails() {
        DataSource dataSource = DataManager.createDataSource(nbcrvMission.getName(),
                nbcrvMission.getStartTime(), nbcrvMission.getStopTime());

        String ugvEntityName = "NBCRV UGV (" + nbcrvMission.getName() + ")";
        NbcrvEntity ugvEntity = getOrCreateEntity(ugvEntityName);

        EntityManager.addEntity(ugvEntity, true);

        EntityDetail ugvEntityDetail = new EntityDetail.Builder()
                .setTimestamp(nbcrvMission.getStartTime())
                .setName(ugvEntityName)
                .setSource("FLIR")
                .setEntityType(new EntityType(1, 1, 225, 50, 2, 0, 0))
                .setMilitarySymbol("SFGPEXN-------G")
                .setAffiliation(Affiliation.FRIENDLY)
                .build();
        ugvEntity.addEntityDetail(ugvEntityDetail);
        DatabaseLogger.addEntityDetail(ugvEntityDetail, ugvEntity.getEntityId(), dataSource.getId());

        EntityScope ugvEntityScope = new EntityScope(nbcrvMission.getStartTime(), nbcrvMission.getStopTime());
        ugvEntity.addEntityScope(ugvEntityScope);
        DatabaseLogger.addEntityScope(ugvEntityScope, ugvEntity.getEntityId(), dataSource.getId());
    }

    private NbcrvEntity getOrCreateEntity(String entityName) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof NbcrvEntity && entityName.equals(entity.getLastEntityDetail().getName())) {
                return (NbcrvEntity) entity;
            }
        }

        EntityId entityId = DataManager.getNextAvailableEntityId(Defaults.APP_ID_NBCRV);
        return new NbcrvEntity(entityId);
    }

    private SidecarEntity getOrCreateSidecarEntity(String entityName) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof SidecarEntity && entityName.equals(entity.getLastEntityDetail().getName())) {
                return (SidecarEntity) entity;
            }
        }

        EntityId entityId = DataManager.getNextAvailableEntityId(Defaults.APP_ID_NBCRV);
        return new SidecarEntity(entityId);
    }

    private WdlEntity getOrCreateWdlEntity(String entityName) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof WdlEntity && entityName.equals(entity.getLastEntityDetail().getName())) {
                return (WdlEntity) entity;
            }
        }

        EntityId entityId = DataManager.getNextAvailableEntityId(Defaults.APP_ID_NBCRV);
        return new WdlEntity(entityId);
    }

    private IEntity getUasEntity(String entityName) {
        IEntity entity = nbcrvMission.getEntity(entityName);
        if (entity != null) {
            return entity;
        }

        for (IEntity existingEntity : EntityManager.getEntities()) {
            if (existingEntity.getLastEntityDetail() != null && entityName.equals(existingEntity.getLastEntityDetail().getName())) {
                nbcrvMission.addEntity(entityName, entity);
                return existingEntity;
            }
        }

        return null;
    }

    private void updateStatusProcessFile(NbcrvImportFile nbcrvImportFile) {
        updateStatus(" processing " + nbcrvImportFile.getFileName() + "...", increaseAndGetProcessStatus());
    }

    private void updateStatus(final String status) {
        String message = "NbcrvImportController::updateStatus:" + status;
        Logger.getGlobal().log(Level.INFO, message);
        Platform.runLater(() -> progressDialog.addStatus(status));
    }

    private void updateStatus(final String status, final double progress) {
        Platform.runLater(() -> {
            progressDialog.addStatus(status);
            progressDialog.setProgress(progress);
        });
    }

    private void cancelImport() {
        progressDialog.close();
    }
}
