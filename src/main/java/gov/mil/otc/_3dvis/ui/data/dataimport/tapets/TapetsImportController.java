package gov.mil.otc._3dvis.ui.data.dataimport.tapets;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.tapets.TapetsLogFile;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TapetsImportController extends TransparentWindow implements ProgressDialog.CancelListener {

    private final TextField battleRosterTextField = new TextField();
    private final Button cancelButton = new Button("Cancel");
    private final Button importButton = new Button("Import");
    private final TableView<TapetsImportEntity> tableView = new TableView<>();
    private final Map<Integer, TapetsImportEntity> tapetsImportEntityMap = new HashMap<>();
    private final File selectedFile;
    private boolean cancelRequested = false;
    private ProgressDialog progressDialog;

    public static synchronized void show() {
        File file = selectSourceFile();
        if (file != null) {
            new TapetsImportController(file).createAndShow();
        }
    }

    private static File selectSourceFile() {
        File initialDirectory = SettingsManager.getPreferences().getLastDirectory("tapetsimport");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(initialDirectory);
        File selectedFile = directoryChooser.showDialog(MainApplication.getInstance().getStage());
        if (selectedFile != null) {
            SettingsManager.getPreferences().setLastDirectory("tapetsimport", selectedFile.getParent());
        }
        return selectedFile;
    }

    private TapetsImportController(File file) {
        this.selectedFile = file;
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Import TAPETS Files");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        Label fileLabel = new Label(selectedFile.getAbsolutePath());
        VBox titleVBox = new VBox(UiConstants.SPACING, titleLabel, fileLabel);
        titleVBox.setAlignment(Pos.CENTER);

        initializeTableView();

        importButton.setDisable(true);
        importButton.setOnAction(event -> doImport());
        cancelButton.setOnAction(event -> cancel());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, importButton, cancelButton);
        buttonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING, titleVBox, new Separator(), tableView, new Separator(), buttonsHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(1200);

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        progressDialog = new ProgressDialog(getStage(), this);
        progressDialog.createAndShow();
        progressDialog.addStatus("scanning files...");
        progressDialog.setProgress(0);
        startFileScan();
        return true;
    }

    private void initializeTableView() {
        TableColumn<TapetsImportEntity, Checkbox> importDataTableColumn = new TableColumn<>("Import");
        importDataTableColumn.setCellValueFactory(new PropertyValueFactory<>("importData"));

        TableColumn<TapetsImportEntity, Integer> unitIdTableColumn = new TableColumn<>("Unit ID");
        unitIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("unitId"));
        multilineHeader(unitIdTableColumn, "Unit", "ID");

        TableColumn<TapetsImportEntity, RadioButton> createNewTableColumn = new TableColumn<>("Create New");
        createNewTableColumn.setCellValueFactory(new PropertyValueFactory<>("createNew"));
        multilineHeader(createNewTableColumn, "Create", "New");

        TableColumn<TapetsImportEntity, RadioButton> updateExistingTableColumn = new TableColumn<>("Update Existing");
        updateExistingTableColumn.setCellValueFactory(new PropertyValueFactory<>("updateExisting"));
        multilineHeader(updateExistingTableColumn, "Update", "Existing");

        TableColumn<TapetsImportEntity, Hyperlink> entityTableColumn = new TableColumn<>("Entity");
        entityTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityHyperlink"));

        TableColumn<TapetsImportEntity, TextField> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<TapetsImportEntity, Hyperlink> entityTypeTableColumn = new TableColumn<>("Entity Type");
        entityTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityTypeHyperlink"));

        TableColumn<TapetsImportEntity, Label> descriptionTableColumn = new TableColumn<>("Description");
        descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<TapetsImportEntity, ComboBox<Affiliation>> affiliationTableColumn = new TableColumn<>("Affiliation");
        affiliationTableColumn.setCellValueFactory(new PropertyValueFactory<>("affiliation"));

        TableColumn<TapetsImportEntity, TextField> urnTableColumn = new TableColumn<>("URN");
        urnTableColumn.setCellValueFactory(new PropertyValueFactory<>("urn"));

        centerColumn(importDataTableColumn);
        centerColumn(unitIdTableColumn);
        centerColumn(createNewTableColumn);
        centerColumn(updateExistingTableColumn);
        centerColumn(entityTableColumn);
        centerColumn(nameTableColumn);
        centerColumn(entityTypeTableColumn);
        centerColumn(descriptionTableColumn);
        centerColumn(affiliationTableColumn);
        centerColumn(urnTableColumn);

        importDataTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(3.0 / 60));
        unitIdTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(2.0 / 60));
        createNewTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(3.0 / 60));
        updateExistingTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(3.0 / 60));
        entityTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(4.0 / 60));
        nameTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(14.0 / 60));
        entityTypeTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(5.0 / 60));
        descriptionTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(15.0 / 60));
        affiliationTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(8.0 / 60));
        urnTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(2.0 / 60));

        tableView.getColumns().add(importDataTableColumn);
        tableView.getColumns().add(unitIdTableColumn);
        tableView.getColumns().add(createNewTableColumn);
        tableView.getColumns().add(updateExistingTableColumn);
        tableView.getColumns().add(entityTableColumn);
        tableView.getColumns().add(nameTableColumn);
        tableView.getColumns().add(entityTypeTableColumn);
        tableView.getColumns().add(descriptionTableColumn);
        tableView.getColumns().add(affiliationTableColumn);
        tableView.getColumns().add(urnTableColumn);

        tableView.setEditable(true);
        URL url = ThemeHelper.class.getResource("/css/no_highlight_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            tableView.getStylesheets().add(css);
        }
    }

    private void multilineHeader(TableColumn<TapetsImportEntity, ?> tableColumn, String top, String bottom) {
        VBox vBox = new VBox(new Label(top), new Label(bottom));
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(UiConstants.SPACING, 0, UiConstants.SPACING, 0));

        StackPane stack = new StackPane();
        stack.getChildren().add(vBox);
        stack.prefWidthProperty().bind(tableColumn.widthProperty().subtract(5));
        vBox.prefWidthProperty().bind(stack.prefWidthProperty());
        tableColumn.setGraphic(stack);
    }

    private void centerColumn(TableColumn<?, ?> column) {
        column.setStyle("-fx-alignment: CENTER");
    }

    private void startFileScan() {
        new Thread(() -> {
            getBinFiles(selectedFile);
            Platform.runLater(this::scanComplete);
        }, "TAPETS Import Scan Files Thread").start();
    }

    private void scanComplete() {
        loadTable();
        importButton.setDisable(false);
        progressDialog.close();
    }

    private void loadTable() {
        for (TapetsImportEntity tapetsImportEntity : tapetsImportEntityMap.values()) {
            tableView.getItems().add(tapetsImportEntity);
        }
    }

    private void getBinFiles(File parentFile) {
        File[] files = parentFile.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (cancelRequested) {
                break;
            }
            if (file.isDirectory()) {
                getBinFiles(file);
            } else if (file.getName().toLowerCase().endsWith(".bin")) {
                TapetsLogFile tapetsLogFile = new TapetsLogFile(file);
                if (tapetsLogFile.process()) {
                    int unitId = tapetsLogFile.getUnitId();
                    TapetsImportEntity tapetsImportEntity = tapetsImportEntityMap.get(unitId);
                    if (tapetsImportEntity == null) {
                        tapetsImportEntity = new TapetsImportEntity(getStage(), unitId);
                        tapetsImportEntityMap.put(unitId, tapetsImportEntity);
                    }
                    tapetsImportEntity.addFile(tapetsLogFile);
                }
            }
        }
    }

    private void selectBattleRosterFile() {
        //may want a battle roster implementation
    }

    private void doImport() {
        tableView.setDisable(true);
        progressDialog = new ProgressDialog(getStage(), this);
        new Thread(() -> {
            int count = 0;
            int totalCount = 0;
            for (TapetsImportEntity tapetsImportEntity : tapetsImportEntityMap.values()) {
                if (tapetsImportEntity.getImportData().isSelected()) {
                    totalCount++;
                }
            }
            for (TapetsImportEntity tapetsImportEntity : tapetsImportEntityMap.values()) {
                if (tapetsImportEntity.getImportData().isSelected()) {
                    updateProgress((double) ++count / totalCount,
                            String.format("Importing %d of %d", count, totalCount));
                    importEntity(tapetsImportEntity);
                }
            }
            importComplete();
        }, "TAPETS Import Thread").start();
    }

    private void importEntity(TapetsImportEntity tapetsImportEntity) {
        IEntity entity;
        if (tapetsImportEntity.getUpdateExisting().isSelected()) {
            entity = tapetsImportEntity.getEntity();
        } else {
            EntityId entityId = createEntityId(tapetsImportEntity.getUnitId());
            entity = EntityManager.getEntity(entityId);
            if (entity == null) {
                entity = createEntity(entityId, tapetsImportEntity);
            }
        }

        if (entity == null) {
            return;
        }

        for (TapetsLogFile tapetsLogFile : tapetsImportEntity.getTapetsLogFileList()) {
            if (cancelRequested) {
                break;
            }
            TspiData firstTspi = tapetsLogFile.getTspiDataList().get(0);
            TspiData lastTspi = tapetsLogFile.getTspiDataList().get(tapetsLogFile.getTspiDataList().size() - 1);
            DataSource dataSource = DataManager.createDataSource(tapetsLogFile.getAbsolutePath(),
                    firstTspi.getTimestamp(), lastTspi.getTimestamp());
            for (TspiData tspiData : tapetsLogFile.getTspiDataList()) {
                entity.addTspi(tspiData);
                DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), dataSource.getId());
            }
        }
    }

    private void updateProgress(final double progress, final String status) {
        Platform.runLater(() -> {
            progressDialog.setProgress(progress);
            progressDialog.addStatus(status);
        });
    }

    private void importComplete() {
        Platform.runLater(() -> {
            tapetsImportEntityMap.clear();
            progressDialog.close();
            cancelButton.setText("Close");
            importButton.setDisable(true);
            DialogUtilities.showInformationDialog("", "Import Complete", "", getStage());
        });
    }

    private EntityId createEntityId(int id) {
        return new EntityId(Defaults.SITE_APP_ID_3DVIS, Defaults.APP_ID_TAPETS, id);
    }

    private IEntity createEntity(EntityId entityId, TapetsImportEntity tapetsImportEntity) {
        EntityType entityType = tapetsImportEntity.getEntityType();
        Affiliation affiliation = tapetsImportEntity.getAffiliation().getValue();
        if (entityType == null) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must select an entity type.", true, getStage());
            return null;
        }

        String militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, affiliation);
        int urn = 0;
        String urnText = tapetsImportEntity.getUrn().getText();
        if (!urnText.isBlank()) {
            try {
                urn = Integer.parseInt(urnText);
            } catch (Exception e) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid URN.", true,
                        getStage());
                return null;
            }
        }
        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(tapetsImportEntity.getStartTime())
                .setEntityType(entityType)
                .setAffiliation(affiliation)
                .setName(tapetsImportEntity.getName().getText())
                .setSource("logfile")
                .setMilitarySymbol(militarySymbol)
                .setUrn(urn)
                .build();

        IEntity entity = new PlaybackEntity(entityId);
        entity.addEntityDetail(entityDetail);
        DataSource dataSource = DataManager.createDataSource("tapets import",
                tapetsImportEntity.getStartTime(), tapetsImportEntity.getStopTime());
        DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), dataSource.getId());
        EntityManager.addEntity(entity, true);
        return entity;
    }

    private void cancel() {
        cancelRequested = true;
        close();
    }

    @Override
    public void onCancel() {
        progressDialog.close();
    }
}
