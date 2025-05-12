package gov.mil.otc._3dvis.ui.data.dataimport.bft;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.jbcp.UtoFile;
import gov.mil.otc._3dvis.data.jbcp.UtoRecord;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.CsvViewer;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.vmf.*;
import gov.mil.otc._3dvis.vmf.pcap.ProtocolParser;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BftImportController extends TransparentWindow {

    private final TextField filenameTextField = new TextField();
    private final CheckBox useUtoFile = new CheckBox("Use UTO");
    private final Hyperlink utoHyperlink = new Hyperlink();
    private final Label statusLabel = new Label("select a file");
    private final Button processFileButton = new Button("Load File");
    private final Button importDataButton = new Button("Import");
    private final TableView<BftEntityView> tableView = new TableView<>();
    private final Map<Integer, AdHocEntity> entityMap = new HashMap<>();
    private File selectedFile = null;
    private UtoFile utoFile = null;

    public static synchronized void show() {
        new BftImportController().createAndShow();
    }

    private BftImportController() {
    }

    @Override
    protected Pane createContentPane() {
        filenameTextField.setEditable(false);

        Button selectFileButton = new Button("...");
        selectFileButton.setOnAction(event -> onSelectFileAction());

        CheckBox filterUrnCheckBox = new CheckBox("Filter by URN");
        Hyperlink viewEditUrnFilterHyperLink = new Hyperlink("view/edit URN filter");
        viewEditUrnFilterHyperLink.setOnAction(actionEvent -> UrnFilterList.show(getStage(), new ArrayList<>()));
        HBox urnFilterHBox = new HBox(UiConstants.SPACING, filterUrnCheckBox, viewEditUrnFilterHyperLink);
        urnFilterHBox.setAlignment(Pos.CENTER_LEFT);
        CheckBox filterNoPliCheckBox = new CheckBox("Filter no position reports");
        CheckBox allOpforCheckBox = new CheckBox("Set all to OPFOR");

        processFileButton.setOnAction(event -> loadFile());
        processFileButton.setVisible(false);
        statusLabel.setVisible(true);

        StackPane stackPane = new StackPane(statusLabel, processFileButton);
        stackPane.setAlignment(Pos.CENTER_LEFT);

        GridPane fileGridPane = new GridPane();
        fileGridPane.setVgap(UiConstants.SPACING);
        fileGridPane.setHgap(UiConstants.SPACING);
        fileGridPane.getColumnConstraints().add(new ColumnConstraints());
        fileGridPane.getColumnConstraints().add(new ColumnConstraints());
        fileGridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        fileGridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        fileGridPane.add(new Label("File:"), 0, rowIndex);
        fileGridPane.add(filenameTextField, 1, rowIndex);
        fileGridPane.add(selectFileButton, 2, rowIndex);

        rowIndex++;

        fileGridPane.add(new Label("Options:"), 0, rowIndex);
        fileGridPane.add(urnFilterHBox, 1, rowIndex);

        rowIndex++;

        fileGridPane.add(filterNoPliCheckBox, 1, rowIndex);

        rowIndex++;

        fileGridPane.add(allOpforCheckBox, 1, rowIndex);

        rowIndex++;

        utoHyperlink.setStyle("-fx-text-fill: steelblue;");
        HBox hBox = new HBox(UiConstants.SPACING, useUtoFile, utoHyperlink);
        hBox.setAlignment(Pos.CENTER_LEFT);
        fileGridPane.add(hBox, 1, rowIndex);

        Button selectUtoFileButton = new Button("...");
        selectUtoFileButton.setOnAction(event -> onSelectUtoFileAction());
        fileGridPane.add(selectUtoFileButton, 2, rowIndex);

        rowIndex++;

        fileGridPane.add(new Label("Status:"), 0, rowIndex);
        fileGridPane.add(stackPane, 1, rowIndex);

        initializeTableView();

        importDataButton.setOnAction(event -> onImportAction());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, importDataButton, cancelButton);
        buttonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING, createTitleLabel("Import BFT Data"), new Separator(),
                fileGridPane, new Separator(), tableView, new Separator(), buttonsHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(800);
        mainVBox.setAlignment(Pos.CENTER);

        return mainVBox;
    }

    private void initializeTableView() {
        TableColumn<BftEntityView, CheckBox> importDataTableColumn = new TableColumn<>("Import");
        importDataTableColumn.setCellValueFactory(new PropertyValueFactory<>("importData"));
        importDataTableColumn.setStyle("-fx-alignment: CENTER");

        TableColumn<BftEntityView, Integer> urnTableColumn = new TableColumn<>("URN");
        urnTableColumn.setCellValueFactory(new PropertyValueFactory<>("urn"));

        TableColumn<BftEntityView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<BftEntityView, ComboBox<Affiliation>> affiliationTableColumn = new TableColumn<>("Affiliation");
        affiliationTableColumn.setCellValueFactory(new PropertyValueFactory<>("affiliation"));

        TableColumn<BftEntityView, Integer> positionRecordCountTableColumn = new TableColumn<>("Position Reports");
        positionRecordCountTableColumn.setCellValueFactory(new PropertyValueFactory<>("positionReportCount"));

        importDataTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(3.0 / 37));
        nameTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(18.0 / 37));
        affiliationTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(6.0 / 37));
        urnTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(4.0 / 37));
        positionRecordCountTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(5.0 / 37));

        tableView.getColumns().add(importDataTableColumn);
        tableView.getColumns().add(nameTableColumn);
        tableView.getColumns().add(affiliationTableColumn);
        tableView.getColumns().add(urnTableColumn);
        tableView.getColumns().add(positionRecordCountTableColumn);

        tableView.setEditable(true);
        URL url = ThemeHelper.class.getResource("/css/no_highlight_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            tableView.getStylesheets().add(css);
        }
    }

    private void updateStatus(final String status) {
        Platform.runLater(() -> statusLabel.setText(status));
    }

    private void loadFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Must select a file to import.", true, getStage());
            return;
        }

        processFileButton.setVisible(false);
        statusLabel.setVisible(true);
        statusLabel.setText("processing file...");
        importDataButton.setDisable(true);

        new Thread(() -> {
            if (useUtoFile.isSelected() && utoFile != null) {
                loadUto();
            }
            if (processFile()) {
                updateStatus("loading entity data...");
                for (AdHocEntity adHocEntity : entityMap.values()) {
                    tableView.getItems().add(new BftEntityView(adHocEntity));
                }

                Platform.runLater(() -> {
                    statusLabel.setText("file loaded successfully");
                    importDataButton.setDisable(false);
                });
            } else {
                updateStatus("error processing file");
            }
        }).start();
    }

    private void loadUto() {
        for (UtoRecord utoRecord : utoFile.getUtoRecordList()) {
            AdHocEntity adHocEntity = entityMap.get(utoRecord.getUrn());
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                entityMap.put(utoRecord.getUrn(), adHocEntity);
            }

            adHocEntity.setAffiliation(Affiliation.FRIENDLY);
            adHocEntity.setUrn(utoRecord.getUrn());
            adHocEntity.setName(utoRecord.getName());
            adHocEntity.setMilitarySymbol(utoRecord.getMilStd2525Symbol());
        }
    }

    private void applyUto(AdHocEntity adHocEntity) {
        UtoRecord utoRecord = utoFile.getUtoRecord(adHocEntity.getUrn());
        if (utoRecord != null) {
            adHocEntity.setName(utoRecord.getName());
            adHocEntity.setMilitarySymbol(utoRecord.getMilStd2525Symbol());
        }
    }

    private boolean processFile() {
        ProtocolParser protocolParser = new ProtocolParser();
        List<VmfMessage> messages = protocolParser.processPcap(selectedFile);
        for (VmfMessage message : messages) {
            if (message instanceof K0101) {
                System.out.println(message.getHeader().getName() + ":" + message.getSummary() + ":" + message.getText());
            }
            System.out.println(message.getHeader().getMessageType().getName());
            if (message instanceof K0501) {
                processK0501((K0501) message);
            } else if (message instanceof Sdsa) {
                processSdsa((Sdsa) message);
            } else {
                System.out.println(message.getHeader().getMessageType().getName());
            }
        }
        return !entityMap.isEmpty();
    }

    private void processK0501(K0501 k0501) {
        for (K0501.PositionReport report : k0501.getReports()) {
            if (report.getUrn() <= 0) {
                continue;
            }

            AdHocEntity adHocEntity = entityMap.get(report.getUrn());
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                adHocEntity.setName(String.valueOf(report.getUrn()));
                adHocEntity.setSource("BFT");
                adHocEntity.setAffiliation(Affiliation.FRIENDLY);
                adHocEntity.setUrn(report.getUrn());
                entityMap.put(report.getUrn(), adHocEntity);
            }

            if (report.getPosition().getLatitude().getDegrees() != 0 && report.getPosition().getLongitude().getDegrees() != 0) {
                adHocEntity.getTspiDataList().add(new TspiData(report.getTime().getTimeInMillis(), report.getPosition()));
            }
        }
    }

    private void processSdsa(Sdsa sdsa) {
        for (Sdsa.Record record : sdsa.getRecords()) {
            if (record.getUrn() <= 0) {
                continue;
            }

            AdHocEntity adHocEntity = entityMap.get(record.getUrn());
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                entityMap.put(record.getUrn(), adHocEntity);
            }

            EntityType entityType = VmfDictionary.getEntityType(
                    record.getDimension(),
                    record.getNationality(),
                    record.getType(),
                    record.getSubType());
            adHocEntity.setEntityType(entityType);

            adHocEntity.setAffiliation(Affiliation.FRIENDLY);
            adHocEntity.setUrn(record.getUrn());
            adHocEntity.setName(String.valueOf(record.getUrn()));

            // set name
            if (!record.getFullName().isEmpty()) {
                adHocEntity.setName(record.getFullName());
            } else if (!record.getShortName().isEmpty()) {
                adHocEntity.setName(record.getShortName());
            } else if (!record.getAlias().isEmpty()) {
                adHocEntity.setName(record.getAlias());
            }

            // set icon
            adHocEntity.setMilitarySymbol(record.getSymbol());
        }
    }

    private void showProcessingError() {
        DialogUtilities.showErrorDialog("Processing Error",
                "No data was extracted from the input file.  Check file or selected format.",
                true, getStage());
    }

    private void onImportAction() {
        importDataButton.setDisable(true);

        new Thread(() -> {
            long startTime = 0;//scopeStartPicker.getDateTimeValue().toEpochSecond(ZoneOffset.UTC) * 1000;
            long stopTime = System.currentTimeMillis();//scopeEndPicker.getDateTimeValue().toEpochSecond(ZoneOffset.UTC) * 1000;
            DataSource dataSource = DataManager.createDataSource(selectedFile.getAbsolutePath(), startTime, stopTime);

            for (AdHocEntity adHocEntity : entityMap.values()) {
                if (adHocEntity.getTspiDataList().isEmpty()) {
                    continue;
                }
                EntityId entityId = DataManager.getNextAvailableEntityId("BFT");
                PlaybackEntity playbackEntity = new PlaybackEntity(entityId);
                TspiData firstTspiData = adHocEntity.getTspiDataList().get(0);
                String militarySymbol = EntityTypeUtility.getTacticalSymbol(adHocEntity.getEntityType());
                militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, adHocEntity.getAffiliation());
                EntityDetail entityDetail = new EntityDetail.Builder()
                        .setTimestamp(firstTspiData.getTimestamp())
                        .setEntityType(adHocEntity.getEntityType())
                        .setName(adHocEntity.getName())
                        .setSource(adHocEntity.getSource())
                        .setAffiliation(adHocEntity.getAffiliation())
                        .setMilitarySymbol(militarySymbol)
                        .build();
                playbackEntity.addEntityDetail(entityDetail);
                DatabaseLogger.addEntityDetail(entityDetail, entityId, dataSource.getId());

                for (TspiData tspiData : adHocEntity.getTspiDataList()) {
                    playbackEntity.addTspi(tspiData);
                    DatabaseLogger.addTspiData(tspiData, entityId, dataSource.getId());
                }

                EntityScope entityScope = new EntityScope(startTime, stopTime);
                playbackEntity.addEntityScope(entityScope);
                DatabaseLogger.addEntityScope(entityScope, entityId, dataSource.getId());
                EntityManager.addEntity(playbackEntity, true);
            }

            Platform.runLater(() -> {
                statusLabel.setText("imported successfully");
            });

        }, "BftImportController::onImportAction").start();

    }

    private void onSelectFileAction() {
        processFileButton.setVisible(true);
        statusLabel.setVisible(false);
        tableView.getItems().clear();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to import.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("bftimport"));
        selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile != null) {
            filenameTextField.setText(selectedFile.getAbsolutePath());
            filenameTextField.setStyle(null);
            SettingsManager.getPreferences().setLastDirectory("bftimport", selectedFile.getParent());
        }
    }

    private void onSelectUtoFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select UTO file to import.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("bftimport"));
        File file = fileChooser.showOpenDialog(getStage());
        if (file != null) {
            utoFile = new UtoFile(file);
            if (utoFile.processFile()) {
                utoHyperlink.setText(utoFile.getFile().getName());
                utoHyperlink.setOnAction(event -> {
                    CsvViewer.show(utoFile.getFile());
                });
            } else {
                utoFile = null;
                utoHyperlink.setText("");
            }
        }
    }
}
