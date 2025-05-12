package gov.mil.otc._3dvis.ui.projects.javelin;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityScope;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.javelin.CutSheetFile;
import gov.mil.otc._3dvis.project.javelin.IvtsTspiFile;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavelinImportController extends TransparentWindow {

    public static synchronized void show() {
        new JavelinImportController().createAndShow();
    }

    private final TextField missionFolderTextField = new TextField();
    private final TextField missionTextField = new TextField();
    private final TableView<IvtsFileView> tableView = new TableView<>();
    private final Button importButton = new Button("Import");
    private ProgressDialog progressDialog;
    private CutSheetFile cutSheetFile = null;
    private final Map<Integer, IEntity> entityMap = new HashMap<>();

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

        initializeTableView();

        importButton.setOnAction(event -> startImport());
        importButton.setDisable(true);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        AnchorPane buttonAnchorPane = new AnchorPane(importButton, closeButton);
        AnchorPane.setLeftAnchor(importButton, 0.0);
        AnchorPane.setRightAnchor(closeButton, 0.0);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Import Javelin Files"),
                new Separator(),
                gridPane,
                new Separator(),
                tableView,
                new Separator(),
                buttonAnchorPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setFillWidth(true);
        mainVBox.setPrefWidth(1000);

        return mainVBox;
    }

    private void initializeTableView() {
        TableColumn<IvtsFileView, String> statusTableColumn = new TableColumn<>("Status");
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<IvtsFileView, String> fileNameTableColumn = new TableColumn<>("File Name");
        fileNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        TableColumn<IvtsFileView, String> fileDateTableColumn = new TableColumn<>("File Date");
        fileDateTableColumn.setCellValueFactory(new PropertyValueFactory<>("fileDate"));

        TableColumn<IvtsFileView, Integer> idTableColumn = new TableColumn<>("ID");
        idTableColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<IvtsFileView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<IvtsFileView, String> affiliationTableColumn = new TableColumn<>("Affiliation");
        affiliationTableColumn.setCellValueFactory(new PropertyValueFactory<>("affiliation"));

        TableColumn<IvtsFileView, String> entityTypeTableColumn = new TableColumn<>("EntityType");
        entityTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));

        tableView.getColumns().add(statusTableColumn);
        tableView.getColumns().add(fileNameTableColumn);
        tableView.getColumns().add(fileDateTableColumn);
        tableView.getColumns().add(idTableColumn);
        tableView.getColumns().add(nameTableColumn);
        tableView.getColumns().add(affiliationTableColumn);
        tableView.getColumns().add(entityTypeTableColumn);

        statusTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(10 / 100.0));
        fileNameTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(29 / 100.0));
        fileDateTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(7 / 100.0));
        idTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(3 / 100.0));
        nameTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(15 / 100.0));
        affiliationTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(7 / 100.0));
        entityTypeTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(28 / 100.0));

        tableView.setPlaceholder(new Label("no data"));
        tableView.setRowFactory(param -> new TableRow<>() {
            @Override
            public void updateItem(IvtsFileView item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("");
                    return;
                }
                if (!item.getStatus().isBlank() && !item.getStatus().equalsIgnoreCase("complete")) {
                    setStyle("-fx-text-background-color: red;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void selectMissionFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select mission folder");
        directoryChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("javelin"));
        File file = directoryChooser.showDialog(getStage());
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory("javelin", file.getParent());
            missionFolderTextField.setText(file.getAbsolutePath());
            loadMission(file);
        }
    }

    private void missionLoadComplete(final String message, final boolean hasError) {
        Platform.runLater(() -> {
            missionTextField.setText(message);
            importButton.setDisable(hasError);
            if (hasError) {
                missionTextField.setStyle("-fx-text-fill: red;");
            } else {
                missionTextField.setStyle(null);
            }
            progressDialog.close();
        });
    }

    private void loadMission(final File missionFolder) {
        new Thread(() -> {
            String missionName = missionFolder.getName();
            for (Mission mission : DataManager.getMissions()) {
                if (mission.getName().equalsIgnoreCase(missionName)) {
                    missionLoadComplete("mission already exists", true);
                    return;
                }
            }

            String cutSheetError = loadCutSheet(missionFolder);
            if (!cutSheetError.isBlank()) {
                missionLoadComplete(cutSheetError, true);
                return;
            }

            loadFiles(missionFolder);

            missionLoadComplete(missionName, false);
        }, "JavelinImportController::loadMission").start();

        progressDialog = new ProgressDialog(getStage());
        progressDialog.createAndShow();
        progressDialog.addStatus("loading mission data...");
    }

    private String loadCutSheet(File missionFolder) {
        boolean multipleCutSheets = false;
        cutSheetFile = null;
        List<File> files = getFiles(missionFolder);
        for (File file : files) {
            if (file.getName().endsWith(".csv")) {
                CutSheetFile testCutSheet = new CutSheetFile(file);
                if (testCutSheet.processFile()) {
                    if (cutSheetFile == null) {
                        cutSheetFile = testCutSheet;
                    } else {
                        multipleCutSheets = true;
                    }
                }
            }
        }

        if (cutSheetFile == null) {
            return "no cut sheet found";
        } else if (multipleCutSheets) {
            return "multiple cut sheets found";
        }

        return "";
    }

    private void loadFiles(File missionFolder) {
        tableView.getItems().clear();
        List<File> files = getFiles(missionFolder);
        for (File file : files) {
            IvtsFileView ivtsFileView = IvtsFileView.createIvtsFileView(file, cutSheetFile);
            if (ivtsFileView != null) {
                tableView.getItems().add(ivtsFileView);
            }
        }
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

    private void updateFileStatus(IvtsFileView ivtsFileView, String status) {
        Platform.runLater(() -> ivtsFileView.setStatus(status));
    }

    private void startImport() {
        Thread thread = new Thread(() -> {
            updateStatus("creating mission...");
            Mission mission = new Mission(missionTextField.getText(), cutSheetFile.getStartTime(), cutSheetFile.getStopTime());

            LocalDateTime localDateTime = Instant.ofEpochMilli(cutSheetFile.getStartTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
            int year = localDateTime.getYear();
            for (IvtsFileView ivtsFileView : tableView.getItems()) {
                IEntity entity = getOrCreateEntity(ivtsFileView.getId(), cutSheetFile);
                if (entity == null) {
                    updateFileStatus(ivtsFileView, "no config");
                    continue;
                }
                IvtsTspiFile ivtsTspiFile = new IvtsTspiFile(ivtsFileView.getFile(), ivtsFileView.getEpochOffset());
                if (ivtsTspiFile.processFile()) {
                    DataSource dataSource = DataManager.createDataSource(ivtsTspiFile.getFile().getAbsolutePath(),
                            mission.getTimestamp(), mission.getStopTime());
                    DataManager.addMissionDataSource(mission, dataSource);
                    for (TspiData tspiData : ivtsTspiFile.getTspiDataList()) {
                        entity.addTspi(tspiData);
                        DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), dataSource.getId());
                    }
                    updateFileStatus(ivtsFileView, "complete");
                } else {
                    updateFileStatus(ivtsFileView, "invalid file");
                }
            }

            Platform.runLater(() -> progressDialog.setComplete(true));
        }, "JavelinImportController::startImport");
        thread.start();

        progressDialog = new ProgressDialog(getStage(), this::cancelImport);
        progressDialog.createAndShow();
        progressDialog.setProgress(0);
    }

    private IEntity getOrCreateEntity(int id, CutSheetFile cutSheetFile) {
        IEntity entity = entityMap.get(id);
        if (entity != null) {
            return entity;
        }

        EntityDetail entityDetail = cutSheetFile.getEntityDetail(id);
        if (entityDetail == null) {
            return null;
        }

        EntityId entityId = new EntityId(Defaults.SITE_APP_ID_3DVIS, Defaults.APP_ID_BFT, id);
        entity = EntityManager.getEntity(entityId);

        if (entity == null) {
            entity = new PlaybackEntity(entityId);
            EntityManager.addEntity(entity);
            DatabaseLogger.addEntity(entity);
            DatabaseLogger.addEntityId(entityId);

        }

        DataSource dataSource = DataManager.createDataSource(entityDetail.getName(), cutSheetFile.getStartTime(), cutSheetFile.getStopTime());
        entity.addEntityDetail(entityDetail);
        DatabaseLogger.addEntityDetail(entityDetail, entityId, dataSource.getId());

        EntityScope entityScope = new EntityScope(cutSheetFile.getStartTime(), cutSheetFile.getStopTime());
        entity.addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, entityId, dataSource.getId());

        return entity;
    }

    private void updateStatus(final String status) {
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
