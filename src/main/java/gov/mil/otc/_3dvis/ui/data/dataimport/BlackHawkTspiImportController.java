package gov.mil.otc._3dvis.ui.data.dataimport;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.file.ImportFile;
import gov.mil.otc._3dvis.data.file.ProcessingCompleteListener;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.project.blackhawk.BlackHawkEntity;
import gov.mil.otc._3dvis.project.blackhawk.FlightData;
import gov.mil.otc._3dvis.project.blackhawk.TspiFile;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.entity.CreateOrUpdateEntityPane;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlackHawkTspiImportController extends TransparentWindow
        implements ProcessingCompleteListener, ProgressDialog.CancelListener {

    private final CreateOrUpdateEntityPane createOrUpdateEntityPane = new CreateOrUpdateEntityPane(getStage());
    private final TextField yearTextField = new TextField();
    private final Label statusLabel = new Label();
    private final Button importButton = new Button("Import");
    private final Button closeButton = new Button("Close");
    private final GridPane gridPane = new GridPane();
    private final VBox mainVBox = new VBox(UiConstants.SPACING);
    private final File selectedFile;
    private TspiFile tspiFile;
    private ProgressDialog progressDialog;

    public static synchronized void show() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Black Hawk TSPI file to import.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("blackhawktspi"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Comma-Separated Values(*.csv)", "*.csv"));
        File file = fileChooser.showOpenDialog(MainApplication.getInstance().getStage());
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory("blackhawktspi", file.getParent());
            new BlackHawkTspiImportController(file).createAndShow();
        }
    }

    private BlackHawkTspiImportController(File file) {
        this.selectedFile = file;
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Import Black Hawk TSPI");
        titleLabel.setFont(Font.font(UiConstants.FONT_NAME, FontWeight.BOLD, 18));
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

        gridPane.add(new Label("File:"), 0, rowIndex);
        gridPane.add(new Label(selectedFile.getAbsolutePath()), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Separator(), 0, rowIndex, 2, 1);

        rowIndex++;

        Label entityLabel = new Label("Entity:");
        GridPane.setValignment(entityLabel, VPos.TOP);

        createOrUpdateEntityPane.setEntityClassFilter(List.of(BlackHawkEntity.class));
        createOrUpdateEntityPane.setName(getTailNumber());
        createOrUpdateEntityPane.setEntityType(
                new EntityType(1, 2, 225, 21, 2, 26, 0));
        createOrUpdateEntityPane.setAffiliation(Affiliation.FRIENDLY);

        gridPane.add(entityLabel, 0, rowIndex);
        gridPane.add(createOrUpdateEntityPane, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Separator(), 0, rowIndex, 2, 1);

        rowIndex++;

        yearTextField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (Boolean.FALSE.equals(newValue)) {
                int year = -1;
                try {
                    year = Integer.parseInt(yearTextField.getText());
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.FINEST, null, e);
                }
                if (year < 2010 || year > 2030) {
                    yearTextField.setText(String.valueOf(LocalDateTime.now().getYear()));
                }
            }
        });
        yearTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        yearTextField.setText(String.valueOf(LocalDateTime.now().getYear()));
        yearTextField.setMaxWidth(100);

        gridPane.add(new Label("Year:"), 0, rowIndex);
        gridPane.add(yearTextField, 1, rowIndex);

        statusLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 58));
        statusLabel.setTextFill(Color.GREEN);
        StackPane stackPane = new StackPane(gridPane, statusLabel);
        StackPane.setAlignment(statusLabel, Pos.CENTER);

        importButton.setOnAction(event -> importFile());
        closeButton.setOnAction(event -> close());

        HBox closeButtonHBox = new HBox(UiConstants.SPACING, importButton, closeButton);
        closeButtonHBox.setAlignment(Pos.BASELINE_RIGHT);

        mainVBox.getChildren().addAll(titleVBox, new Separator(), stackPane, new Separator(), closeButtonHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(500);

        return mainVBox;
    }

    private String getTailNumber() {
        String[] fields = selectedFile.getName().split("-");
        return fields.length > 3 ? fields[2] : "";
    }

    private void importFile() {
        if (createOrUpdateEntityPane.isCreateNew()) {
            if (createOrUpdateEntityPane.getEntityType() == null) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Must select an entity type.", getStage());
                return;
            } else if (createOrUpdateEntityPane.getName().isBlank()) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Must enter an entity name.", getStage());
                return;
            } else if (createOrUpdateEntityPane.getAffiliation() == null) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Must select an affiliation.", getStage());
                return;
            }
        } else if (createOrUpdateEntityPane.getEntity() == null) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must create or select an entity.", getStage());
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearTextField.getText());
        } catch (Exception e) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid year.", getStage());
            return;
        }

        startImport(year);
    }

    private void startImport(int year) {
        tspiFile = new TspiFile(selectedFile, year);
        tspiFile.startProcessing(this);
        mainVBox.setOpacity(.75);
        gridPane.setDisable(true);
        progressDialog = new ProgressDialog(getStage(), this);
        progressDialog.createAndShow();
        progressDialog.addStatus("processing file...");
        progressDialog.setProgress(0);
        Timer updateTimer = new Timer("UpdateTimer");
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                double status = tspiFile.getStatus();
                Platform.runLater(() -> progressDialog.setProgress(status));
            }
        }, 1000, 1000);
    }

    private BlackHawkEntity getBlackHawkEntity(DataSource dataSource) {
        if (createOrUpdateEntityPane.isCreateNew()) {
            BlackHawkEntity blackHawkEntity = new BlackHawkEntity(DataManager.getNextAvailableEntityId("BlackHawk"));
            EntityType entityType = createOrUpdateEntityPane.getEntityType();
            String militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
            militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, Affiliation.FRIENDLY);
            EntityDetail entityDetail = new EntityDetail.Builder()
                    .setTimestamp(dataSource.getStartTime())
                    .setEntityType(entityType)
                    .setAffiliation(createOrUpdateEntityPane.getAffiliation())
                    .setName(createOrUpdateEntityPane.getName())
                    .setSource(selectedFile.getName())
                    .setMilitarySymbol(militarySymbol)
                    .setUrn(createOrUpdateEntityPane.getUrn())
                    .build();
            blackHawkEntity.addEntityDetail(entityDetail);
            DatabaseLogger.addEntityDetail(entityDetail, blackHawkEntity.getEntityId(), dataSource.getId());
            return blackHawkEntity;
        } else if (createOrUpdateEntityPane.getEntity() instanceof BlackHawkEntity) {
            return (BlackHawkEntity) createOrUpdateEntityPane.getEntity();
        }

        return null;
    }

    private void completeImport(TspiFile tspiFile) {
        TspiData firstTspi = tspiFile.getTspiDataList().get(0);
        TspiData lastTspi = tspiFile.getTspiDataList().get(tspiFile.getTspiDataList().size() - 1);
        DataSource dataSource = DataManager.createDataSource(selectedFile.getAbsolutePath(), firstTspi.getTimestamp(),
                lastTspi.getTimestamp());
        BlackHawkEntity blackHawkEntity = getBlackHawkEntity(dataSource);
        if (blackHawkEntity == null) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must select or create valid entity.", getStage());
            return;
        }
        EntityScope entityScope = new EntityScope(firstTspi.getTimestamp(), lastTspi.getTimestamp());
        blackHawkEntity.addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, blackHawkEntity.getEntityId(), dataSource.getId());
        for (TspiData tspiData : tspiFile.getTspiDataList()) {
            TspiData tspiDataWithSource = new TspiData(tspiData);
            blackHawkEntity.addTspi(tspiDataWithSource);
            DatabaseLogger.addTspiData(tspiData, blackHawkEntity.getEntityId(), dataSource.getId());
        }
        for (FlightData flightData : tspiFile.getFlightDataList()) {
            blackHawkEntity.addFlightData(flightData);
            DatabaseLogger.addFlightData(flightData, blackHawkEntity.getEntityId(), dataSource.getId());
        }
        EntityManager.addEntity(blackHawkEntity, true);
        Platform.runLater(() -> {
            progressDialog.close();
            mainVBox.setOpacity(1.0);
            importButton.setDisable(true);
            statusLabel.setText("Complete");
        });
    }

    @Override
    public void processingComplete(ImportFile importFile, boolean successful) {
        Platform.runLater(() -> {
            if (successful) {
                if (tspiFile.getTspiDataList().isEmpty()) {
                    DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "No valid TSPI found.", getStage());
                    progressDialog.close();
                    mainVBox.setOpacity(1.0);
                } else {
                    progressDialog.addStatus("importing data...");
                    progressDialog.setProgress(-1);
                    new Thread(() -> completeImport(tspiFile), "BlackHawkTspiImportController:import").start();
                }
            } else {
                String message = importFile.isCanceled() ? "Import canceled." : "Error processing file.";
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, message, getStage());
                progressDialog.close();
                mainVBox.setOpacity(1.0);
            }
        });
    }

    @Override
    public void onCancel() {
        tspiFile.cancel();
    }
}
