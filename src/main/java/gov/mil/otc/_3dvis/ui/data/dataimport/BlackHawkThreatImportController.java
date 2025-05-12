package gov.mil.otc._3dvis.ui.data.dataimport;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.file.ImportFile;
import gov.mil.otc._3dvis.data.file.ProcessingCompleteListener;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.project.blackhawk.CcmEventFile;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.DateTimePicker;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.entity.entitytype.EntityTypePicker;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.ZoneOffset;

public class BlackHawkThreatImportController extends TransparentWindow implements ProcessingCompleteListener, ProgressDialog.CancelListener {

    private final VBox mainVBox = new VBox(UiConstants.SPACING);
    private final DateTimePicker scopeStartPicker = new DateTimePicker();
    private final DateTimePicker scopeEndPicker = new DateTimePicker();
    private final TableView<ThreatView> tableView = new TableView<>();
    private final Label statusLabel = new Label();
    private final Button importButton = new Button("Import");
    private final Button closeButton = new Button("Close");
    private final GridPane gridPane = new GridPane();
    private final File selectedFile;
    private ProgressDialog progressDialog;
    private CcmEventFile ccmEventFile;

    public static synchronized void show() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Black Hawk TSPI file to import.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("blackhawktspi"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Comma-Separated Values(*.csv)", "*.csv"));
        File file = fileChooser.showOpenDialog(MainApplication.getInstance().getStage());
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory("blackhawktspi", file.getParent());
            new BlackHawkThreatImportController(file).createAndShow();
        }
    }

    private BlackHawkThreatImportController(File file) {
        this.selectedFile = file;
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Import Black Hawk Threat");
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
        gridPane.add(new Label(selectedFile.getAbsolutePath()), 1, rowIndex, 2, 1);

        rowIndex++;

        gridPane.add(new Separator(), 0, rowIndex, 3, 1);

        rowIndex++;

        Region spacer = new Region();
        spacer.setMinWidth(UiConstants.SPACING);
        HBox hBox = new HBox(UiConstants.SPACING, new Label("Start:"), scopeStartPicker,
                spacer,
                new Label("End:"), scopeEndPicker);
        hBox.setAlignment(Pos.CENTER_LEFT);

        gridPane.add(new Label("Scope:"), 0, rowIndex);
        gridPane.add(hBox, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Separator(), 0, rowIndex, 3, 1);

        rowIndex++;

        initializeTable();

        Label threatLabel = new Label("Threat:");
        GridPane.setValignment(threatLabel, VPos.TOP);

        gridPane.add(threatLabel, 0, rowIndex);
        gridPane.add(tableView, 1, rowIndex, 2, 1);

        statusLabel.setFont(Font.font(UiConstants.FONT_NAME, FontWeight.BOLD, 58));
        statusLabel.setTextFill(Color.GREEN);
        StackPane stackPane = new StackPane(gridPane, statusLabel);
        StackPane.setAlignment(statusLabel, Pos.CENTER);

        importButton.setOnAction(event -> importData());
        closeButton.setOnAction(event -> getStage().close());

        HBox closeButtonHBox = new HBox(UiConstants.SPACING, importButton, closeButton);
        closeButtonHBox.setAlignment(Pos.BASELINE_RIGHT);

        mainVBox.getChildren().addAll(titleVBox, new Separator(), stackPane, new Separator(), closeButtonHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        progressDialog = new ProgressDialog(getStage(), this);
        progressDialog.addStatus("loading file...");
        progressDialog.createAndShow();
        ccmEventFile = new CcmEventFile(selectedFile);
        ccmEventFile.startProcessing(this);
        return true;
    }

    private void initializeTable() {
        TableColumn<ThreatView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<ThreatView, String> systemTableColumn = new TableColumn<>("System");
        systemTableColumn.setCellValueFactory(new PropertyValueFactory<>("system"));
        TableColumn<ThreatView, Hyperlink> entityTypeTableColumn = new TableColumn<>("Entity Type");
        entityTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityTypeHyperlink"));
        TableColumn<ThreatView, String> descriptionTableColumn = new TableColumn<>("Description");
        descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        centerColumn(nameTableColumn);
        centerColumn(systemTableColumn);
        centerColumn(entityTypeTableColumn);
        centerColumn(descriptionTableColumn);

        tableView.getColumns().add(nameTableColumn);
        tableView.getColumns().add(systemTableColumn);
        tableView.getColumns().add(entityTypeTableColumn);
        tableView.getColumns().add(descriptionTableColumn);

        nameTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(7 / 37.0));
        systemTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(7 / 37.0));
        entityTypeTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(7 / 37.0));
        descriptionTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(15.0 / 37.0));

        tableView.setMinWidth(800);
    }

    private void centerColumn(TableColumn<?, ?> column) {
        column.setStyle("-fx-alignment: CENTER");
    }

    private void importData() {
        long startTime = scopeStartPicker.getDateTimeValue().toEpochSecond(ZoneOffset.UTC) * 1000;
        long stopTime = scopeEndPicker.getDateTimeValue().toEpochSecond(ZoneOffset.UTC) * 1000;
        DataSource dataSource = DataManager.createDataSource(selectedFile.getAbsolutePath(),
                startTime, stopTime);
        for (ThreatView threatView : tableView.getItems()) {
            EntityId entityId = DataManager.getNextAvailableEntityId("BlackHawk Threat");
            PlaybackEntity playbackEntity = new PlaybackEntity(entityId);
            TspiData tspiData = threatView.getAdHocEntity().getTspiDataList().get(0);
            String militarySymbol = EntityTypeUtility.getTacticalSymbol(threatView.getAdHocEntity().getEntityType());
            militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol,
                    threatView.getAdHocEntity().getAffiliation());
            playbackEntity.addEntityDetail(new EntityDetail.Builder()
                    .setTimestamp(tspiData.getTimestamp())
                    .setEntityType(threatView.getAdHocEntity().getEntityType())
                    .setName(threatView.getAdHocEntity().getName())
                    .setSource(threatView.getAdHocEntity().getSource())
                    .setAffiliation(threatView.getAdHocEntity().getAffiliation())
                    .setMilitarySymbol(militarySymbol)
                    .build());
            playbackEntity.addTspi(tspiData);
            DatabaseLogger.addTspiData(tspiData, entityId, dataSource.getId());
            for (Event event : threatView.getAdHocEntity().getEventList()) {
                playbackEntity.addEvent(event);
                EventManager.addEvent(event);
            }
            EntityScope entityScope = new EntityScope(startTime, -1);
            playbackEntity.addEntityScope(entityScope);
            DatabaseLogger.addEntityScope(entityScope, entityId, dataSource.getId());
            EntityManager.addEntity(playbackEntity, true);
        }
        gridPane.setDisable(true);
        statusLabel.setText("Complete");
    }

    @Override
    public void processingComplete(ImportFile importFile, boolean successful) {
        if (successful) {
            Platform.runLater(() -> {
                progressDialog.close();
                for (AdHocEntity adHocEntity : ccmEventFile.getEntityList()) {
                    tableView.getItems().add(new ThreatView(adHocEntity));
                }
                scopeStartPicker.setDateTimeValue(ccmEventFile.getStartTime());
                scopeEndPicker.setDateTimeValue(ccmEventFile.getStopTime());
            });
        } else {
            Platform.runLater(() -> {
                progressDialog.close();
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid Threat File", getStage());
                importButton.setDisable(true);
            });
        }
    }

    @Override
    public void onCancel() {
        progressDialog.close();
    }

    public class ThreatView {

        private final AdHocEntity adHocEntity;
        private final String name;
        private final String system;
        private final Hyperlink entityTypeHyperlink = new Hyperlink();
        private final StringProperty description = new SimpleStringProperty();
        private EntityType entityType;

        public ThreatView(AdHocEntity adHocEntity) {
            this.adHocEntity = adHocEntity;
            name = adHocEntity.getName();
            system = adHocEntity.getSource();
            entityType = adHocEntity.getEntityType();
            entityTypeHyperlink.setText(entityType.toString());
            entityTypeHyperlink.setOnAction(event -> {
                EntityType selectedEntityType = EntityTypePicker.show(getStage(), entityType);
                if (selectedEntityType != null) {
                    entityType = selectedEntityType;
                    entityTypeHyperlink.setText(entityType.toString());
                    description.set(entityType.getDescription());
                }
            });
            description.set(entityType.getDescription());
        }

        public AdHocEntity getAdHocEntity() {
            return adHocEntity;
        }

        public String getName() {
            return name;
        }

        public String getSystem() {
            return system;
        }

        public Hyperlink getEntityTypeHyperlink() {
            return entityTypeHyperlink;
        }

        public String getDescription() {
            return description.get();
        }

        public StringProperty descriptionProperty() {
            return description;
        }

        public EntityType getEntityType() {
            return entityType;
        }
    }
}
