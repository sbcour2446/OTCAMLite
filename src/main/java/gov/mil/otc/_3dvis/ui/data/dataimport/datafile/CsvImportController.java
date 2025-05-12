package gov.mil.otc._3dvis.ui.data.dataimport.datafile;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.file.delimited.csv.GpsLogCsvFile;
import gov.mil.otc._3dvis.data.file.delimited.csv.GpsLogUtmCsvFile;
import gov.mil.otc._3dvis.data.gps.P10DataLog;
import gov.mil.otc._3dvis.data.gps.TrackStickCsvFile;
import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.rpuas.BusDataCsv;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.entity.CreateOrUpdateEntityPane;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;

public class CsvImportController extends TransparentWindow {

    private enum Format {
        CSV_TRACKSTICK("CSV - TrackStick"),
        CSV_P10("CSV - P10 Log"),
        CSV_AH64("CSV - AH-64"),
        CSV_UH60("CSV - UH-60"),
        CSV_RPUAS("CSV - RPUAS"),
        CSV_OTHER_DD("CSV - OTHER (DD)"),
        CSV_OTHER_UTM("CSV - OTHER (UTM)");
        final String description;

        Format(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    private static final int FIELD_MIN_WIDTH = 500;

    private final CreateOrUpdateEntityPane createOrUpdateEntityPane = new CreateOrUpdateEntityPane(getStage());
    private final Label statusLabel = new Label("load file");
    private final TextField filenameTextField = new TextField();
    private final ComboBox<Format> formatComboBox = new ComboBox<>();
    private final Button importDataButton = new Button("Import");
    private File selectedFile = null;
    private TspiCsvFile tspiCsvFile = null;

    public static synchronized void show() {
        new CsvImportController().createAndShow();
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Import Data File");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        filenameTextField.setEditable(false);
        filenameTextField.setMinWidth(FIELD_MIN_WIDTH);

        Button selectFileButton = new Button("...");
        selectFileButton.setOnAction(event -> onSelectFileAction());

        formatComboBox.setMaxWidth(Double.MAX_VALUE);
        for (Format format : Format.values()) {
            formatComboBox.getItems().add(format);
        }

        Button processFileButton = new Button("Load File");
        processFileButton.setOnAction(event -> loadFile());

        GridPane selectFileGridPane = new GridPane();
        selectFileGridPane.setVgap(UiConstants.SPACING);
        selectFileGridPane.setHgap(UiConstants.SPACING);
        selectFileGridPane.getColumnConstraints().add(new ColumnConstraints());
        selectFileGridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);

        selectFileGridPane.add(new Label("File:"), 0, 0);
        selectFileGridPane.add(filenameTextField, 1, 0);
        selectFileGridPane.add(selectFileButton, 2, 0);

        selectFileGridPane.add(new Label("Format:"), 0, 1);
        selectFileGridPane.add(formatComboBox, 1, 1);

        GridPane.setHalignment(processFileButton, HPos.CENTER);
        selectFileGridPane.add(processFileButton, 0, 2, 3, 1);

        createOrUpdateEntityPane.setEntityType(new EntityType(1, 1, 225, 6, 1, 0, 0));
        createOrUpdateEntityPane.setVisible(false);

        StackPane stackPane = new StackPane(statusLabel, createOrUpdateEntityPane);

        importDataButton.setOnAction(event -> onImportAction());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, importDataButton, cancelButton);
        buttonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING, titleLabel, new Separator(), selectFileGridPane, new Separator(),
                stackPane, new Separator(), buttonsHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));

        return mainVBox;
    }

    private void loadFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must select a file to import.", true, getStage());
            return;
        }

        Format format = formatComboBox.getSelectionModel().getSelectedItem();
        if (format == null) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must select a file format.", true, getStage());
            return;
        }

        if (processFile(format)) {
            createOrUpdateEntityPane.setVisible(true);
            statusLabel.setVisible(false);
            importDataButton.setDisable(false);
        }
    }

    private boolean processFile(Format format) {
        tspiCsvFile = switch (format) {
            case CSV_TRACKSTICK -> new TrackStickCsvFile(selectedFile);
            case CSV_P10 -> new P10DataLog(selectedFile);
            case CSV_AH64, CSV_UH60 -> null;
            case CSV_RPUAS -> new BusDataCsv(selectedFile);
            case CSV_OTHER_DD -> new GpsLogCsvFile(selectedFile);
            case CSV_OTHER_UTM -> new GpsLogUtmCsvFile(selectedFile);
        };

        if (tspiCsvFile == null || !tspiCsvFile.processFile()) {
            showProcessingError();
            return false;
        }

        if (tspiCsvFile.getTspiDataList().isEmpty()) {
            showProcessingError();
            return false;
        }

        return true;
    }

    private void showProcessingError() {
        DialogUtilities.showErrorDialog("Processing Error",
                "No data was extracted from the input file.  Check file or selected format.",
                true, getStage());
    }

    private void onImportAction() {
        importDataButton.setDisable(true);

        long startTime = tspiCsvFile.getTspiDataList().get(0).getTimestamp();
        long stopTime = tspiCsvFile.getTspiDataList().get(tspiCsvFile.getTspiDataList().size() - 1).getTimestamp();
        DataSource dataSource = DataManager.createDataSource(selectedFile.getAbsolutePath(), startTime, stopTime);

        IEntity entity = getEntity(dataSource);
        if (entity == null) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must create or select an entity.", getStage());
            return;
        }

        EntityScope entityScope = new EntityScope(startTime, stopTime);
        entity.addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, entity.getEntityId(), dataSource.getId());

        for (TspiData tspiData : tspiCsvFile.getTspiDataList()) {
            entity.addTspi(tspiData);
            DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), dataSource.getId());
        }
    }

    private IEntity getEntity(DataSource dataSource) {
        if (createOrUpdateEntityPane.isCreateNew()) {
            if (createOrUpdateEntityPane.getEntityType() == null) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Must select an entity type.", getStage());
                return null;
            } else if (createOrUpdateEntityPane.getName().isBlank()) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Must enter an entity name.", getStage());
                return null;
            } else if (createOrUpdateEntityPane.getAffiliation() == null) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Must select an affiliation.", getStage());
                return null;
            }

            PlaybackEntity playbackEntity = new PlaybackEntity(DataManager.getNextAvailableEntityId("AdHoc"));
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
            playbackEntity.addEntityDetail(entityDetail);
            DatabaseLogger.addEntityDetail(entityDetail, playbackEntity.getEntityId(), dataSource.getId());
            EntityManager.addEntity(playbackEntity, true);
            return playbackEntity;
        }

        return createOrUpdateEntityPane.getEntity();
    }

    private void onSelectFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to import.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("csvimport"));
        selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile != null) {
            filenameTextField.setText(selectedFile.getAbsolutePath());
            filenameTextField.setStyle(null);
            SettingsManager.getPreferences().setLastDirectory("csvimport", selectedFile.getParent());
        }
    }
}
