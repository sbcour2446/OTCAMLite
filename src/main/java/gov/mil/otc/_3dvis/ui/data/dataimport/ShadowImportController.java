package gov.mil.otc._3dvis.ui.data.dataimport;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.file.ImportFile;
import gov.mil.otc._3dvis.data.file.ProcessingCompleteListener;
import gov.mil.otc._3dvis.data.stanag.A101File;
import gov.mil.otc._3dvis.data.stanag.A302File;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.datamodel.aircraft.TspiExtendedData;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.shadow.ShadowEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.MultipleProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import gov.mil.otc._3dvis.ui.widgets.entity.entitytype.EntityTypePicker;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ShadowImportController extends TransparentWindow implements ProcessingCompleteListener, MultipleProgressDialog.CancelListener {

    private static final String APPLICATION_NAME = "Shadow";
    private static final String SELECT_FILE = "select file";
    private final RadioButton createNewRadioButton = new RadioButton("create new");
    private final RadioButton updateExistingRadioButton = new RadioButton("update existing");
    private final TextField entityNameTextField = new TextField();
    private final Hyperlink tspiFileHyperlink = new Hyperlink(SELECT_FILE);
    private final Hyperlink payloadFileHyperlink = new Hyperlink(SELECT_FILE);
    private final Hyperlink entityHyperlink = new Hyperlink();
    private final Hyperlink entityTypeHyperlink = new Hyperlink();
    private final Label entityTypeLabel = new Label();
    private final GridPane gridPane = new GridPane();
    private final VBox mainVBox = new VBox(UiConstants.SPACING);
    private final Label importStatusLabel = new Label();
    private final Button importButton = new Button("Import");
    private final Button closeButton = new Button("Close");
    private final File initialFile;
    private A101File tspiFile;
    private A302File payloadFile;
    private ShadowEntity entity;
    private EntityType entityType = new EntityType(1, 2, 225, 50, 0, 0, 0);
    private MultipleProgressDialog progressDialog;

    public static synchronized void show() {
        File file = selectFile("TSPI file (A101)", MainApplication.getInstance().getStage());
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory(APPLICATION_NAME, file.getParent());
            new ShadowImportController(file).createAndShow();
        }
    }

    private static File selectFile(String fileDescription, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(String.format("Select Shadow %s.", fileDescription));
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory(APPLICATION_NAME));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Comma-Separated Values(*.csv)", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory(APPLICATION_NAME, file.getParent());
        }
        return file;
    }

    private ShadowImportController(File initialFile) {
        this.initialFile = initialFile;
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Import Shadow Data Files");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        VBox titleVBox = new VBox(UiConstants.SPACING, titleLabel);
        titleVBox.setAlignment(Pos.CENTER);

        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        tspiFileHyperlink.setOnAction(event -> selectTspiFile());

        gridPane.add(new Label("TSPI (A101):"), 0, rowIndex);
        gridPane.add(tspiFileHyperlink, 1, rowIndex);

        rowIndex++;

        payloadFileHyperlink.setOnAction(event -> selectPayloadFile());

        gridPane.add(new Label("Payload (A302):"), 0, rowIndex);
        gridPane.add(payloadFileHyperlink, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Separator(), 0, rowIndex, 2, 1);

        rowIndex++;

        Label entityLabel = new Label("Entity:");
        GridPane.setValignment(entityLabel, VPos.TOP);

        createNewRadioButton.setOnAction(event -> onCreateNewAction());
        updateExistingRadioButton.setOnAction(event -> onUpdateExistingAction());
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().add(createNewRadioButton);
        toggleGroup.getToggles().add(updateExistingRadioButton);
        createNewRadioButton.setSelected(true);

        entityHyperlink.setOnAction(event -> onSelectEntityAction());
        entityTypeHyperlink.setOnAction(event -> onSelectEntityTypeAction());
        entityTypeHyperlink.setText(entityType.toString());
        entityTypeLabel.setText(EntityTypeUtility.getDescription(entityType));

        GridPane entityGridPane = new GridPane();
        entityGridPane.setHgap(UiConstants.SPACING);
        entityGridPane.setVgap(UiConstants.SPACING);
        entityGridPane.add(new HBox(UiConstants.SPACING, createNewRadioButton, updateExistingRadioButton, entityHyperlink),
                0, 0, 3, 1);
        entityGridPane.add(new Label("Entity ID:"), 0, 1);
        entityGridPane.add(entityHyperlink, 1, 1, 2, 1);
        entityGridPane.add(new Label("Entity Type:"), 0, 2);
        entityGridPane.add(entityTypeHyperlink, 1, 2);
        entityGridPane.add(entityTypeLabel, 2, 2);
        entityGridPane.add(new Label("Name:"), 0, 3);
        entityGridPane.add(entityNameTextField, 1, 3, 2, 1);

        gridPane.add(entityLabel, 0, rowIndex);
        gridPane.add(entityGridPane, 1, rowIndex);

        importButton.setOnAction(event -> importFile());
        closeButton.setOnAction(event -> close());

        HBox closeButtonHBox = new HBox(UiConstants.SPACING, importButton, closeButton);
        closeButtonHBox.setAlignment(Pos.BASELINE_RIGHT);
        StackPane bottomStackPane = new StackPane(closeButtonHBox, importStatusLabel);
        StackPane.setAlignment(importStatusLabel, Pos.CENTER);

        mainVBox.getChildren().addAll(titleVBox, new Separator(), gridPane, new Separator(), bottomStackPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(500);

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        selectFiles(initialFile);
        return true;
    }

    private void selectFiles(File initialFile) {
        tspiFile = new A101File(initialFile);
        if (tspiFile.isValidFile()) {
            tspiFileHyperlink.setText(tspiFile.getFile().getAbsolutePath());
            if (payloadFile == null) {
                findPayloadFile(initialFile);
            }
        } else {
            tspiFile = null;
            tspiFileHyperlink.setText(SELECT_FILE);
        }
    }

    private void selectTspiFile() {
        File selectedFile = selectFile("TSPI file (A101)", MainApplication.getInstance().getStage());
        if (selectedFile != null) {
            tspiFile = new A101File(selectedFile);
            if (tspiFile.isValidFile()) {
                tspiFileHyperlink.setText(tspiFile.getFile().getAbsolutePath());
                if (payloadFile == null) {
                    findPayloadFile(selectedFile);
                }
            } else {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid file", getStage());
                tspiFile = null;
                tspiFileHyperlink.setText(SELECT_FILE);
            }
        }
    }

    private void selectPayloadFile() {
        File selectedFile = selectFile("Payload file (A302)", MainApplication.getInstance().getStage());
        if (selectedFile != null) {
            payloadFile = new A302File(selectedFile);
            if (payloadFile.isValidFile()) {
                payloadFileHyperlink.setText(payloadFile.getFile().getAbsolutePath());
                if (tspiFile == null) {
                    findTspiFile(selectedFile);
                }
            } else {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid file", getStage());
                payloadFile = null;
                payloadFileHyperlink.setText(SELECT_FILE);
            }
        }
    }

    private void findTspiFile(File selectedFile) {
        File[] files = selectedFile.getParentFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("A101")) {
                    tspiFile = new A101File(file);
                    if (tspiFile.isValidFile()) {
                        tspiFileHyperlink.setText(tspiFile.getFile().getAbsolutePath());
                        return;
                    } else {
                        tspiFile = null;
                        tspiFileHyperlink.setText(SELECT_FILE);
                    }
                }
            }
        }
    }

    private void findPayloadFile(File selectedFile) {
        File[] files = selectedFile.getParentFile().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().startsWith("A302")) {
                    payloadFile = new A302File(file);
                    if (payloadFile.isValidFile()) {
                        payloadFileHyperlink.setText(payloadFile.getFile().getAbsolutePath());
                        return;
                    } else {
                        payloadFile = null;
                        payloadFileHyperlink.setText(SELECT_FILE);
                    }
                }
            }
        }
    }

    private void onCreateNewAction() {
        entityHyperlink.setText("auto generated");
        entityHyperlink.setDisable(true);
        entityTypeHyperlink.setDisable(false);
        entityNameTextField.setDisable(false);
    }

    private void onUpdateExistingAction() {
        entityHyperlink.setText("select entity");
        entityHyperlink.setDisable(false);
        entityTypeHyperlink.setDisable(true);
        entityNameTextField.setDisable(true);
    }

    private void onSelectEntityAction() {
        IEntity selectedEntity = EntityPicker.show(getStage(), List.of(ShadowEntity.class));
        if (selectedEntity instanceof ShadowEntity) {
            entity = (ShadowEntity) selectedEntity;
            entityHyperlink.setText(entity.getEntityId().toString());
            EntityDetail entityDetail = entity.getLastEntityDetail();
            if (entityDetail != null) {
                entityTypeHyperlink.setText(entityDetail.getEntityType().toString());
                entityNameTextField.setText(entityDetail.getName());
            }
        }
    }

    private void onSelectEntityTypeAction() {
        EntityType selectedEntityType = EntityTypePicker.show(getStage(), entityType);
        if (selectedEntityType != null) {
            entityType = selectedEntityType;
            entityTypeHyperlink.setText(entityType.toString());
            entityTypeLabel.setText(EntityTypeUtility.getDescription(entityType));
        }
    }

    private void importFile() {
        if (tspiFile == null || payloadFile == null) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must select valid input files.", getStage());
            return;
        }

        if (createNewRadioButton.isSelected()) {
            if (entityType == null) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Must select an entity type.", getStage());
                return;
            } else if (entityNameTextField.getText().isBlank()) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Must enter an entity name.", getStage());
                return;
            }
            entity = new ShadowEntity(DataManager.getNextAvailableEntityId(APPLICATION_NAME));
        } else if (entity == null) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must create or select an entity.", getStage());
            return;
        }

        startImport();
    }

    private void startImport() {
        tspiFile.startProcessing(this);
        payloadFile.startProcessing(this);
        mainVBox.setOpacity(.75);
        importButton.setDisable(true);
        progressDialog = new MultipleProgressDialog(getStage(), this, 2);
        progressDialog.createAndShow();
        progressDialog.setStatus(0, "processing file...");
        progressDialog.setProgress(0, 0);
        progressDialog.setStatus(1, "processing file...");
        progressDialog.setProgress(1, 0);
        Timer updateTimer = new Timer("UpdateTimer");
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    progressDialog.setProgress(0, tspiFile.getStatus());
                    progressDialog.setProgress(1, payloadFile.getStatus());
                });
            }
        }, 1000, 1000);
    }

    boolean tspiFileComplete = false;
    boolean payloadFileComplete = false;

    private void completeImport(A101File a101File) {
        List<TspiData> tspiDataList = a101File.getTspiDataList();
        if (tspiDataList.isEmpty()) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    String.format("Error processing file %s.", a101File.getFile().getAbsolutePath()), getStage());
            progressDialog.close();
            mainVBox.setOpacity(1.0);
            importStatusLabel.setText("Import Failed");
            return;
        }
        TspiData firstTspi = tspiDataList.get(0);
        TspiData lastTspi = tspiDataList.get(tspiDataList.size() - 1);
        DataSource dataSource = DataManager.createDataSource(a101File.getFile().getAbsolutePath(), firstTspi.getTimestamp(),
                lastTspi.getTimestamp());

        String militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, Affiliation.FRIENDLY);
        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(dataSource.getStartTime())
                .setEntityType(entityType)
                .setAffiliation(Affiliation.FRIENDLY)
                .setName(entityNameTextField.getText())
                .setSource("Shadow Import")
                .setMilitarySymbol(militarySymbol)
                .setUrn(0)
                .build();
        entity.addEntityDetail(entityDetail);
        DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), dataSource.getId());
        EntityScope entityScope = new EntityScope(firstTspi.getTimestamp(), lastTspi.getTimestamp());
        entity.addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, entity.getEntityId(), dataSource.getId());
        for (TspiData tspiData : a101File.getTspiDataList()) {
            entity.addTspi(tspiData);
            DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), dataSource.getId());
        }
        for (TspiExtendedData tspiExtendedData : a101File.getTspiExtendedDataList()) {
            entity.addTspiExtendedData(tspiExtendedData);
            DatabaseLogger.addTspiExtendedData(tspiExtendedData, entity.getEntityId(), dataSource.getId());
        }
        EntityManager.addEntity(entity, true);
        tspiFileComplete = true;
        if (payloadFileComplete) {
            Platform.runLater(() -> {
                progressDialog.close();
                mainVBox.setOpacity(1.0);
                gridPane.setDisable(true);
                importStatusLabel.setText("Import Successful");
            });
        } else {
            Platform.runLater(() -> {
                progressDialog.setProgress(0, 1);
                progressDialog.setStatus(0, "Complete");
            });
        }
    }

    private void completeImport(A302File a302File) {
        long start = a302File.getUasPayloadDataList().get(0).getTimestamp();
        long stop = a302File.getUasPayloadDataList().get(a302File.getUasPayloadDataList().size() - 1).getTimestamp();
        DataSource dataSource = DataManager.createDataSource(a302File.getFile().getAbsolutePath(), start, stop);

        for (UasPayloadData uasPayloadData : a302File.getUasPayloadDataList()) {
            entity.addUasPayloadData(uasPayloadData);
            DatabaseLogger.addUasPayloadData(uasPayloadData, entity.getEntityId(), dataSource.getId());
        }

        payloadFileComplete = true;
        if (tspiFileComplete) {
            Platform.runLater(() -> {
                progressDialog.close();
                mainVBox.setOpacity(1.0);
                gridPane.setDisable(true);
                importStatusLabel.setText("Import Successful");
            });
        } else {
            Platform.runLater(() -> {
                progressDialog.setProgress(1, 1);
                progressDialog.setStatus(1, "Complete");
            });
        }
    }

    @Override
    public void processingComplete(ImportFile importFile, boolean successful) {
        if (!successful) {
            Platform.runLater(() -> {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        String.format("Error processing file %s.", importFile.getFile().getAbsolutePath()), getStage());
                progressDialog.close();
                mainVBox.setOpacity(1.0);
                importStatusLabel.setText("Import Failed");
            });
            return;
        }

        if (importFile instanceof A101File) {
            completeImport((A101File) importFile);
        } else if (importFile instanceof A302File) {
            completeImport((A302File) importFile);
        }
    }

    @Override
    public void onCancel() {
        tspiFile.cancel();
        payloadFile.cancel();
    }
}
