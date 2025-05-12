package gov.mil.otc._3dvis.ui.tools.rangefinder;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.tools.rangefinder.*;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerValidationListener;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class RangeFinderController2 implements IRangeLineListener {

    private static final String INVALID_ENTRY = "Invalid Entry";
    private static final String FIND_ENTITY = "find entity...";
    private static final String ALL_ENTITY = "ALL";
    private static RangeFinderController2 rangeFinderController2 = null;
    private final Stage stage = new Stage();
    private final CheckMenuItem alwaysOnTopCheckMenuItem = new CheckMenuItem("Always on Top");
    private final RadioMenuItem displayByIdMenuItem = new RadioMenuItem("Display by ID");
    private final RadioMenuItem displayByNameMenuItem = new RadioMenuItem("Display by Name");
    private final RadioMenuItem displayByDescriptionMenuItem = new RadioMenuItem("Display by Description");
    private final ComboBox<Object> sourceComboBox = new ComboBox<>();
    private final ComboBox<Object> targetComboBox = new ComboBox<>();
    private final TextField minRangeTextField = new TextField();
    private final TextField maxRangeTextField = new TextField();
    private final ColorPicker colorPicker = new ColorPicker();
    private final CheckBox showLinesCheckBox = new CheckBox("Show lines");
    private final CheckBox showGroundPathCheckBox = new CheckBox("Follow terrain");
    private final CheckBox ignoreFiltersCheckBox = new CheckBox("Ignore filters");
    private final Button updateButton = new Button("Update");
    private final Button removeButton = new Button("Remove");
    private final TableView<RangeFinderEntry> entryTable = new TableView<>();
    private final TableView<RangeLine> rangeLineTable = new TableView<>();
    private DisplayType displayType = DisplayType.BY_ID;
    private RangeFinderEntity sourceEntity = null;
    private RangeFinderEntity targetEntity = null;

    public static synchronized void show() {
        if (rangeFinderController2 == null) {
            rangeFinderController2 = new RangeFinderController2();
        }
        rangeFinderController2.stage.show();
    }

    private RangeFinderController2() {
        MenuItem saveMenuItem = new MenuItem("Save");
        saveMenuItem.setOnAction(event -> save());
        MenuItem loadMenuItem = new MenuItem("Load");
        loadMenuItem.setOnAction(event -> load());
        MenuItem closeMenuItem = new MenuItem("Close");
        closeMenuItem.setOnAction(event -> stage.close());

        Menu fileMenu = new Menu("File", null, saveMenuItem, loadMenuItem, closeMenuItem);

        alwaysOnTopCheckMenuItem.setOnAction(event -> stage.setAlwaysOnTop(alwaysOnTopCheckMenuItem.isSelected()));

        displayByIdMenuItem.setOnAction(event -> handleDisplayTypeChange());
        displayByNameMenuItem.setOnAction(event -> handleDisplayTypeChange());
        displayByDescriptionMenuItem.setOnAction(event -> handleDisplayTypeChange());

        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(displayByIdMenuItem, displayByNameMenuItem, displayByDescriptionMenuItem);

        Menu viewMenu = new Menu("View", null, alwaysOnTopCheckMenuItem, new SeparatorMenuItem(),
                displayByIdMenuItem, displayByNameMenuItem, displayByDescriptionMenuItem);

        sourceComboBox.setMinWidth(300);
        sourceComboBox.getItems().add(FIND_ENTITY);
        sourceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                handleSourceEntitySelectionChange(oldValue, newValue));

        targetComboBox.setMinWidth(300);
        targetComboBox.getItems().add(RangeFinderEntity.ALL);
        targetComboBox.getItems().add(FIND_ENTITY);
        targetComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                handleTargetEntitySelectionChange(oldValue, newValue));

        HBox sourceEntityHBox = new HBox(UiConstants.SPACING, new TextWithStyleClass("Source:"), sourceComboBox);
        sourceEntityHBox.setAlignment(Pos.CENTER_LEFT);
        sourceEntityHBox.setPadding(new Insets(0, 0, 0, UiConstants.SPACING));

        HBox targetEntityHBox = new HBox(UiConstants.SPACING, new TextWithStyleClass("Target:"), targetComboBox);
        targetEntityHBox.setAlignment(Pos.CENTER_LEFT);
        targetEntityHBox.setPadding(new Insets(0, UiConstants.SPACING, 0, 0));

        HBox entityHBox = new HBox(UiConstants.SPACING, sourceEntityHBox, targetEntityHBox);
        entityHBox.setAlignment(Pos.CENTER_LEFT);

        minRangeTextField.setMinWidth(100);
        minRangeTextField.setText("0");
        minRangeTextField.textProperty().addListener(new IntegerValidationListener(minRangeTextField,
                0, Integer.MAX_VALUE));
        maxRangeTextField.setMinWidth(100);
        maxRangeTextField.setText("10000");
        maxRangeTextField.textProperty().addListener(new IntegerValidationListener(maxRangeTextField,
                0, Integer.MAX_VALUE));

        colorPicker.setValue(Color.TEAL);

        showLinesCheckBox.setSelected(true);
        showGroundPathCheckBox.setSelected(true);

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> onAddButton());
        updateButton.setDisable(true);
        updateButton.setOnAction(event -> onUpdateButton());
        removeButton.setDisable(true);
        removeButton.setOnAction(event -> onRemoveButton());

        HBox rangeAndColorHBox = new HBox(UiConstants.SPACING, new TextWithStyleClass("Min Range (m):"), minRangeTextField,
                new TextWithStyleClass("Max Range (m):"), maxRangeTextField,
                new TextWithStyleClass("Color:"), colorPicker);
        rangeAndColorHBox.setAlignment(Pos.CENTER_LEFT);

        HBox optionsHBox = new HBox(UiConstants.SPACING, showLinesCheckBox, showGroundPathCheckBox, ignoreFiltersCheckBox);
        optionsHBox.setAlignment(Pos.CENTER_LEFT);

        HBox buttonHBox = new HBox(UiConstants.SPACING, addButton, updateButton, removeButton);
        buttonHBox.setAlignment(Pos.CENTER_LEFT);

        TitledPane entryPane = new TitledPane("Entries", new VBox(UiConstants.SPACING,
                entityHBox, rangeAndColorHBox, optionsHBox, buttonHBox, entryTable));

        TitledPane rangeLinePane = new TitledPane("Ranges", new VBox(UiConstants.SPACING, rangeLineTable));

        BorderPane borderPane = new BorderPane(new VBox(UiConstants.SPACING, entryPane, rangeLinePane),
                new MenuBar(fileMenu, viewMenu),
                null, null, null);

        Scene scene = new Scene(borderPane);
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setScene(scene);
        stage.setTitle("Range Finder");
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Range Finder");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());

        initializeEntryTable();
        initializeRangePairTable();
        refresh();
    }

    @Override
    public void onRangeLineAdded(RangeLine rangeLine) {
        Platform.runLater(() -> rangeLineTable.getItems().add(rangeLine));
    }

    @Override
    public void onRangeLineRemoved(RangeLine rangeLine) {
        Platform.runLater(() -> rangeLineTable.getItems().remove(rangeLine));
    }

    private void initializeEntryTable() {
        TableColumn<RangeFinderEntry, Boolean> enabledTableColumn = new TableColumn<>("Enabled");
        enabledTableColumn.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        enabledTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(enabledTableColumn));
        enabledTableColumn.setEditable(true);

        TableColumn<RangeFinderEntry, RangeFinderEntity> sourceTableColumn = new TableColumn<>("Source");
        sourceTableColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceTableColumn.setCellFactory(new RangeFinderEntityTableColumnCellFactory<>());

        TableColumn<RangeFinderEntry, RangeFinderEntity> targetTableColumn = new TableColumn<>("Target");
        targetTableColumn.setCellValueFactory(new PropertyValueFactory<>("target"));
        targetTableColumn.setCellFactory(new RangeFinderEntityTableColumnCellFactory<>());

        TableColumn<RangeFinderEntry, Integer> minRangeTableColumn = new TableColumn<>("Min Range");
        minRangeTableColumn.setCellValueFactory(new PropertyValueFactory<>("minRange"));

        TableColumn<RangeFinderEntry, Integer> maxRangeTableColumn = new TableColumn<>("Max Range");
        maxRangeTableColumn.setCellValueFactory(new PropertyValueFactory<>("maxRange"));

        TableColumn<RangeFinderEntry, Object> colorTableColumn = new TableColumn<>("Color");
        colorTableColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorTableColumn.setCellFactory(new ColorTableColumnCellFactory<>());

        TableColumn<RangeFinderEntry, Boolean> showLinesTableColumn = new TableColumn<>("Show Lines");
        showLinesTableColumn.setCellValueFactory(new PropertyValueFactory<>("showLines"));
        showLinesTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(showLinesTableColumn));
        showLinesTableColumn.setEditable(false);

        TableColumn<RangeFinderEntry, Boolean> followTerrainTableColumn = new TableColumn<>("Follow Terrain");
        followTerrainTableColumn.setCellValueFactory(new PropertyValueFactory<>("followTerrain"));
        followTerrainTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(followTerrainTableColumn));
        followTerrainTableColumn.setEditable(false);

        TableColumn<RangeFinderEntry, Boolean> ignoreFiltersTableColumn = new TableColumn<>("Ignore Filters");
        ignoreFiltersTableColumn.setCellValueFactory(new PropertyValueFactory<>("ignoreFilters"));
        ignoreFiltersTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(ignoreFiltersTableColumn));
        ignoreFiltersTableColumn.setEditable(false);

        entryTable.getColumns().add(enabledTableColumn);
        entryTable.getColumns().add(sourceTableColumn);
        entryTable.getColumns().add(targetTableColumn);
        entryTable.getColumns().add(minRangeTableColumn);
        entryTable.getColumns().add(maxRangeTableColumn);
        entryTable.getColumns().add(colorTableColumn);
        entryTable.getColumns().add(showLinesTableColumn);
        entryTable.getColumns().add(followTerrainTableColumn);
        entryTable.getColumns().add(ignoreFiltersTableColumn);

        entryTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> entityTableSelectedChange());
        entryTable.setEditable(true);
        rangeLineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void initializeRangePairTable() {
        TableColumn<RangeLine, RangeFinderEntity> sourceTableColumn = new TableColumn<>("Source");
        sourceTableColumn.setCellValueFactory(new PropertyValueFactory<>("source"));
        sourceTableColumn.setCellFactory(new RangeFinderEntityTableColumnCellFactory<>());

        TableColumn<RangeLine, RangeFinderEntity> targetTableColumn = new TableColumn<>("Target");
        targetTableColumn.setCellValueFactory(new PropertyValueFactory<>("target"));
        targetTableColumn.setCellFactory(new RangeFinderEntityTableColumnCellFactory<>());

        TableColumn<RangeLine, Double> slantRangeTableColumn = new TableColumn<>("Slant Range");
        slantRangeTableColumn.setCellValueFactory(new PropertyValueFactory<>("slantRange"));
        slantRangeTableColumn.setCellFactory(new DistanceTableColumnCellFactory<>());

        TableColumn<RangeLine, Double> groundDistanceTableColumn = new TableColumn<>("Path Distance");
        groundDistanceTableColumn.setCellValueFactory(new PropertyValueFactory<>("pathDistance"));
        groundDistanceTableColumn.setCellFactory(new DistanceTableColumnCellFactory<>());

        TableColumn<RangeLine, Object> colorTableColumn = new TableColumn<>("Color");
        colorTableColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorTableColumn.setCellFactory(new ColorTableColumnCellFactory<>());

        rangeLineTable.getColumns().add(sourceTableColumn);
        rangeLineTable.getColumns().add(targetTableColumn);
        rangeLineTable.getColumns().add(slantRangeTableColumn);
        rangeLineTable.getColumns().add(groundDistanceTableColumn);
        rangeLineTable.getColumns().add(colorTableColumn);

        rangeLineTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        rangeLineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        sourceTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);
        targetTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);
        slantRangeTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        groundDistanceTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        colorTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 10);

        rangeLineTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (oldSelection != null) {
                oldSelection.setHighlight(false);
            }
            if (newSelection != null) {
                newSelection.setHighlight(true);
            }
        });
    }

    private void save() {
        List<SerializedRangeFinderEntry> serializedRangeFinderEntryList = new ArrayList<>();
        for (RangeFinderEntry rangeFinderEntry : entryTable.getItems()) {
            serializedRangeFinderEntryList.add(new SerializedRangeFinderEntry(rangeFinderEntry));
        }
        SettingsManager.getSettings().setRangeFinderEntryList(serializedRangeFinderEntryList);
    }

    private void load() {
        List<SerializedRangeFinderEntry> serializedRangeFinderEntryList = SettingsManager.getSettings().getRangeFinderEntryList();
        if (serializedRangeFinderEntryList != null) {
            loadRangeFinderEntries(serializedRangeFinderEntryList);
        }
    }

    private void loadRangeFinderEntries(List<SerializedRangeFinderEntry> serializedRangeFinderEntryList) {
        for (SerializedRangeFinderEntry serializedRangeFinderEntry : serializedRangeFinderEntryList) {
            RangeFinderEntry rangeFinderEntry = serializedRangeFinderEntry.toRangeFinderEntry();
            rangeFinderEntry.addEntryListener(this);
            entryTable.getItems().add(rangeFinderEntry);
        }
    }

    private void handleSourceEntitySelectionChange(Object oldValue, Object newValue) {
        if (newValue instanceof RangeFinderEntity) {
            targetEntity = (RangeFinderEntity) newValue;
            sourceTargetChange();
        } else if (FIND_ENTITY.equals(newValue)) {
            IEntity entity = EntityPicker.show(stage);
            if (entity != null) {
                sourceEntity = new RangeFinderEntity(entity.getEntityId());
                if (!sourceComboBox.getItems().contains(sourceEntity)) {
                    sourceComboBox.getItems().add(0, sourceEntity);
                }
                sourceComboBox.getSelectionModel().select(sourceEntity);
            } else {
                sourceComboBox.getSelectionModel().select(oldValue);
            }
        }
    }

    private void handleTargetEntitySelectionChange(Object oldValue, Object newValue) {
        if (newValue instanceof RangeFinderEntity) {
            targetEntity = (RangeFinderEntity) newValue;
            sourceTargetChange();
        } else if (FIND_ENTITY.equals(newValue)) {
            IEntity entity = EntityPicker.show(stage);
            if (entity != null) {
                targetEntity = new RangeFinderEntity(entity.getEntityId());
                if (!targetComboBox.getItems().contains(targetEntity)) {
                    targetComboBox.getItems().add(0, targetEntity);
                }
                targetComboBox.getSelectionModel().select(targetEntity);
            } else {
                targetComboBox.getSelectionModel().select(oldValue);
            }
        }
    }

    private void handleDisplayTypeChange() {
        if (displayByIdMenuItem.isSelected()) {
            displayType = DisplayType.BY_ID;
        } else if (displayByNameMenuItem.isSelected()) {
            displayType = DisplayType.BY_NAME;
        } else if (displayByDescriptionMenuItem.isSelected()) {
            displayType = DisplayType.BY_DESCRIPTION;
        } else {
            displayType = DisplayType.BY_ID;
        }
        refresh();
    }

    private void onAddButton() {
        if (sourceEntity == null) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Must select a source", true, stage);
            return;
        }

        if (targetEntity == null) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Must select a target", true, stage);
            return;
        }

        int minRange;
        try {
            minRange = Integer.parseInt(minRangeTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Invalid min range", true, stage);
            return;
        }

        int maxRange;
        try {
            maxRange = Integer.parseInt(maxRangeTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Invalid max range", true, stage);
            return;
        }

        Color color = colorPicker.getValue();

        RangeFinderEntry rangeFinderEntry = new RangeFinderEntry(sourceEntity, targetEntity);
        if (entryTable.getItems().contains(rangeFinderEntry)) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Entry already exists", true, stage);
        } else {
            rangeFinderEntry.setMinRange(minRange);
            rangeFinderEntry.setMaxRange(maxRange);
            rangeFinderEntry.setColor(color);
            rangeFinderEntry.setShowLines(showLinesCheckBox.isSelected());
            rangeFinderEntry.setFollowTerrain(showGroundPathCheckBox.isSelected());
            rangeFinderEntry.setIgnoreFilters(ignoreFiltersCheckBox.isSelected());
            rangeFinderEntry.addEntryListener(this);
            entryTable.getItems().add(rangeFinderEntry);
        }
    }

    private void onUpdateButton() {
        if (sourceEntity == null) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Must select a source", true, stage);
            return;
        }

        if (targetEntity == null) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Must select a target", true, stage);
            return;
        }

        int minRange;
        try {
            minRange = Integer.parseInt(minRangeTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Invalid min range", true, stage);
            return;
        }

        int maxRange;
        try {
            maxRange = Integer.parseInt(maxRangeTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Invalid max range", true, stage);
            return;
        }

        Color color = colorPicker.getValue();

        RangeFinderEntry rangeFinderEntry = new RangeFinderEntry(sourceEntity, targetEntity);
        int index = entryTable.getItems().indexOf(rangeFinderEntry);
        if (index < 0) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Entry does not exist", true, stage);
        } else {
            rangeFinderEntry = entryTable.getItems().get(index);
            rangeFinderEntry.setMinRange(minRange);
            rangeFinderEntry.setMaxRange(maxRange);
            rangeFinderEntry.setColor(color);
            rangeFinderEntry.setShowLines(showLinesCheckBox.isSelected());
            rangeFinderEntry.setFollowTerrain(showGroundPathCheckBox.isSelected());
            rangeFinderEntry.setIgnoreFilters(ignoreFiltersCheckBox.isSelected());
            rangeFinderEntry.updateRangeLines();
        }
    }

    private void onRemoveButton() {
        List<RangeFinderEntry> rangeFinderEntries = new ArrayList<>(entryTable.getSelectionModel().getSelectedItems());
        for (RangeFinderEntry rangeFinderEntry : rangeFinderEntries) {
            removeRangeFinderEntry(rangeFinderEntry);
        }
    }

    private void refresh() {
        entryTable.refresh();
    }

    private void entityTableSelectedChange() {
        RangeFinderEntry rangeFinderEntry = entryTable.getSelectionModel().getSelectedItem();
        if (rangeFinderEntry != null) {
//            sourceComboBox.getSelectionModel().select(rangeFinderEntry.getSource());
//            targetComboBox.getSelectionModel().select(rangeFinderEntry.getTarget());
            minRangeTextField.setText(String.valueOf(rangeFinderEntry.getMinRange()));
            maxRangeTextField.setText(String.valueOf(rangeFinderEntry.getMaxRange()));
            colorPicker.setValue(rangeFinderEntry.getColor());
            showLinesCheckBox.setSelected(rangeFinderEntry.getShowLines());
            showGroundPathCheckBox.setSelected(rangeFinderEntry.isFollowTerrain());
            ignoreFiltersCheckBox.setSelected(rangeFinderEntry.isIgnoreFilters());
            updateButton.setDisable(false);
            removeButton.setDisable(false);
        } else {
            updateButton.setDisable(true);
            removeButton.setDisable(true);
        }
    }

    private void sourceTargetChange() {
        if (sourceEntity == null || targetEntity == null) {
//            entryTable.getSelectionModel().select(null);
            return;
        }

        RangeFinderEntry rangeFinderEntry = new RangeFinderEntry(sourceEntity, targetEntity);
        if (entryTable.getItems().contains(rangeFinderEntry)) {
            entryTable.getSelectionModel().select(rangeFinderEntry);
        } else {
            entryTable.getSelectionModel().select(null);
        }
    }

    private void removeRangeFinderEntry(RangeFinderEntry rangeFinderEntry) {
        rangeFinderEntry.removeEntryListener(this);
        rangeFinderEntry.setEnabled(false);
        rangeLineTable.getItems().removeIf(rangeLine -> rangeFinderEntry.equals(rangeLine.getRangeFinderEntry()));
        entryTable.getItems().remove(rangeFinderEntry);
    }

    private String getEntityDisplay(RangeFinderEntity rangeFinderEntity) {
        if (rangeFinderEntity == null) {
            return "not defined";
        }

        if (rangeFinderEntity.isAll()) {
            return rangeFinderEntity.toString();
        }

        if (displayType == DisplayType.BY_ID) {
            return rangeFinderEntity.getEntityId().toString();
        }

        IEntity entity = EntityManager.getEntity(rangeFinderEntity.getEntityId());
        if (entity == null) {
            return rangeFinderEntity.getEntityId().toString();
        }

        EntityDetail entityDetail = entity.getEntityDetail();
        if (entityDetail == null) {
            return rangeFinderEntity.getEntityId().toString();
        }

        if (displayType == DisplayType.BY_NAME) {
            return entityDetail.getName();
        } else {
            return entityDetail.getEntityType().getDescription();
        }
    }

    private final class RangeFinderEntityTableColumnCellFactory<T> implements Callback<TableColumn<T, RangeFinderEntity>,
            TableCell<T, RangeFinderEntity>> {
        @Override
        public TableCell<T, RangeFinderEntity> call(TableColumn param) {
            return new TableCell<>() {
                @Override
                public void updateItem(RangeFinderEntity item,
                                       boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(getEntityDisplay(item));
                    } else {
                        setText("");
                    }
                }
            };
        }
    }

    private static final class DistanceTableColumnCellFactory<T> implements Callback<TableColumn<T, Double>,
            TableCell<T, Double>> {
        @Override
        public TableCell<T, Double> call(TableColumn param) {
            return new TableCell<>() {
                @Override
                public void updateItem(Double item,
                                       boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(String.format("%.1f", item));
                    } else {
                        setText("");
                    }
                }
            };
        }
    }

    private static final class ColorTableColumnCellFactory<T> implements Callback<TableColumn<T, Object>,
            TableCell<T, Object>> {
        @Override
        public TableCell<T, Object> call(TableColumn param) {
            return new TableCell<>() {
                @Override
                public void updateItem(Object item,
                                       boolean empty) {
                    super.updateItem(item, empty);
                    if (item instanceof Color) {
                        Color color = (Color) item;
                        String colorString = String.format("#%02X%02X%02X", (int) (color.getRed() * 255),
                                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));
                        setStyle("-fx-background-color: " + colorString + ";");
                    } else {
                        setStyle("");
                    }
                }
            };
        }
    }
}
