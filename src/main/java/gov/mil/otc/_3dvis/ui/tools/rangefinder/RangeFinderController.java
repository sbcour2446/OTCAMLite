package gov.mil.otc._3dvis.ui.tools.rangefinder;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.IEntityListener;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.tools.rangefinder.*;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerValidationListener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class RangeFinderController implements Initializable, IEntityListener, IRangeLineListener {

    private static final String INVALID_ENTRY = "Invalid Entry";
    private DisplayType displayType = DisplayType.BY_ID;

    @FXML
    private CheckMenuItem alwaysOnTopCheckMenuItem;
    @FXML
    private RadioMenuItem displayByIdMenuItem;
    @FXML
    private RadioMenuItem displayByNameMenuItem;
    @FXML
    private RadioMenuItem displayByDescriptionMenuItem;
    @FXML
    private ComboBox<RangeFinderEntity> sourceComboBox;
    @FXML
    private ComboBox<RangeFinderEntity> targetComboBox;
    @FXML
    private TextField minRangeTextField;
    @FXML
    private TextField maxRangeTextField;
    @FXML
    private TableView<RangeFinderEntry> entryTable;
    @FXML
    private TableView<RangeLine> rangeLineTable;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private CheckBox hideLinesCheckBox;
    @FXML
    private CheckBox followTerrainCheckBox;
    @FXML
    private CheckBox ignoreFiltersCheckBox;
    @FXML
    private Button updateButton;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        alwaysOnTopCheckMenuItem.setOnAction(event -> getStage().setAlwaysOnTop(alwaysOnTopCheckMenuItem.isSelected()));

        maxRangeTextField.textProperty().addListener(new IntegerValidationListener(maxRangeTextField,
                0, Integer.MAX_VALUE));

        sourceComboBox.setCellFactory(new ComboBoxCellFactory());
        sourceComboBox.setButtonCell(sourceComboBox.getCellFactory().call(null));
        sourceComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> sourceTargetChange());

        targetComboBox.setCellFactory(new ComboBoxCellFactory());
        targetComboBox.setButtonCell(targetComboBox.getCellFactory().call(null));
        targetComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> sourceTargetChange());

        colorPicker.setValue(Color.TEAL);

        initializeEntryTable();
        initializeRangePairTable();

        loadCurrentEntries();

        EntityManager.addEntityListener(this);

        refresh();
    }

    @Override
    public void onEntityAdded(final IEntity entity) {
        addEntity(new RangeFinderEntity(entity.getEntityId()));
    }

    @Override
    public void onEntityUpdated(IEntity entity) {
        checkEntityUpdate(entity);
    }

    @Override
    public void onEntityDisposed(IEntity entity) {
        removeEntity(new RangeFinderEntity(entity.getEntityId()));
    }

    @Override
    public void onRangeLineAdded(RangeLine rangeLine) {
        Platform.runLater(() -> rangeLineTable.getItems().add(rangeLine));
    }

    @Override
    public void onRangeLineRemoved(RangeLine rangeLine) {
        Platform.runLater(() -> rangeLineTable.getItems().remove(rangeLine));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(event -> EntityManager.removeEntityListener(this));
    }

    public Stage getStage() {
        return this.stage;
    }

    @FXML
    private void saveOnAction() {
        List<SerializedRangeFinderEntry> serializedRangeFinderEntryList = new ArrayList<>();
        for (RangeFinderEntry rangeFinderEntry : entryTable.getItems()) {
            serializedRangeFinderEntryList.add(new SerializedRangeFinderEntry(rangeFinderEntry));
        }
        SettingsManager.getSettings().setRangeFinderEntryList(serializedRangeFinderEntryList);
    }

    @FXML
    private void loadOnAction() {
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

    @FXML
    private void closeOnAction() {
        getStage().close();
    }

    @FXML
    private void onDisplayByMenuItemAction() {
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

    @FXML
    private void addButtonOnAction() {
        RangeFinderEntity source = sourceComboBox.getSelectionModel().getSelectedItem();
        if (source == null) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Must select a source", true, getStage());
            return;
        }

        RangeFinderEntity target = targetComboBox.getSelectionModel().getSelectedItem();
        if (target == null) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Must select a target", true, getStage());
            return;
        }

        int minRange;
        try {
            minRange = Integer.parseInt(minRangeTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Invalid min range", true, getStage());
            return;
        }

        int maxRange;
        try {
            maxRange = Integer.parseInt(maxRangeTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Invalid max range", true, getStage());
            return;
        }

        Color color = colorPicker.getValue();

        RangeFinderEntry rangeFinderEntry = new RangeFinderEntry(source, target);
        if (entryTable.getItems().contains(rangeFinderEntry)) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Entry already exists", true, getStage());
        } else {
            rangeFinderEntry.setMinRange(minRange);
            rangeFinderEntry.setMaxRange(maxRange);
            rangeFinderEntry.setColor(color);
            rangeFinderEntry.setShowLines(hideLinesCheckBox.isSelected());
            rangeFinderEntry.setFollowTerrain(followTerrainCheckBox.isSelected());
            rangeFinderEntry.setIgnoreFilters(ignoreFiltersCheckBox.isSelected());
            rangeFinderEntry.addEntryListener(this);
            entryTable.getItems().add(rangeFinderEntry);
            RangeFinderManager.addRangeFinderEntry(rangeFinderEntry);
        }
    }

    @FXML
    private void updateButtonOnAction() {
        RangeFinderEntity source = sourceComboBox.getSelectionModel().getSelectedItem();
        if (source == null) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Must select a source", true, getStage());
            return;
        }

        RangeFinderEntity target = targetComboBox.getSelectionModel().getSelectedItem();
        if (target == null) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Must select a target", true, getStage());
            return;
        }

        int minRange;
        try {
            minRange = Integer.parseInt(minRangeTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Invalid min range", true, getStage());
            return;
        }

        int maxRange;
        try {
            maxRange = Integer.parseInt(maxRangeTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Invalid max range", true, getStage());
            return;
        }

        Color color = colorPicker.getValue();

        RangeFinderEntry rangeFinderEntry = new RangeFinderEntry(source, target);
        int index = entryTable.getItems().indexOf(rangeFinderEntry);
        if (index < 0) {
            DialogUtilities.showErrorDialog(INVALID_ENTRY, "Entry does not exist", true, getStage());
        } else {
            rangeFinderEntry = entryTable.getItems().get(index);
            rangeFinderEntry.setMinRange(minRange);
            rangeFinderEntry.setMaxRange(maxRange);
            rangeFinderEntry.setColor(color);
            rangeFinderEntry.setShowLines(!hideLinesCheckBox.isSelected());
            rangeFinderEntry.setFollowTerrain(followTerrainCheckBox.isSelected());
            rangeFinderEntry.setIgnoreFilters(ignoreFiltersCheckBox.isSelected());
            rangeFinderEntry.updateRangeLines();
        }
    }

    @FXML
    private void removeButtonOnAction() {
        List<RangeFinderEntry> rangeFinderEntries = new ArrayList<>(entryTable.getSelectionModel().getSelectedItems());
        for (RangeFinderEntry rangeFinderEntry : rangeFinderEntries) {
            removeRangeFinderEntry(rangeFinderEntry);
        }
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

        TableColumn<RangeFinderEntry, Boolean> hideLinesTableColumn = new TableColumn<>("Hide Lines");
        hideLinesTableColumn.setCellValueFactory(new PropertyValueFactory<>("hideLines"));
        hideLinesTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(hideLinesTableColumn));
        hideLinesTableColumn.setEditable(false);

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
        entryTable.getColumns().add(hideLinesTableColumn);
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

        rangeLineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        sourceTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);
        targetTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);
        slantRangeTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        groundDistanceTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        colorTableColumn.setMaxWidth(1f * Integer.MAX_VALUE * 10);
    }

    private void loadCurrentEntries() {
        for (RangeFinderEntry rangeFinderEntry : RangeFinderManager.getRangeFinderEntries()) {
            for (RangeLine rangeLine : rangeFinderEntry.getRangeLines()) {
                rangeLineTable.getItems().add(rangeLine);
            }
            rangeFinderEntry.addEntryListener(this);
            entryTable.getItems().add(rangeFinderEntry);
        }
    }

    private void addEntity(final RangeFinderEntity rangeFinderEntity) {
        Platform.runLater(() -> {
            if (!sourceComboBox.getItems().contains(rangeFinderEntity)) {
                sourceComboBox.getItems().add(rangeFinderEntity);
            }
            if (!targetComboBox.getItems().contains(rangeFinderEntity)) {
                targetComboBox.getItems().add(rangeFinderEntity);
            }
        });
    }

    private void removeEntity(final RangeFinderEntity rangeFinderEntity) {
        Platform.runLater(() -> {
            sourceComboBox.getItems().remove(rangeFinderEntity);
            targetComboBox.getItems().remove(rangeFinderEntity);
        });
    }

    private void checkEntityUpdate(IEntity entity) {
        RangeFinderEntity rangeFinderEntity = new RangeFinderEntity(entity.getEntityId());
        if (!entity.isFiltered()) {
            if (sourceComboBox.getItems().contains(rangeFinderEntity)) {
                removeEntity(rangeFinderEntity);
            }
        } else {
            if (!sourceComboBox.getItems().contains(rangeFinderEntity)) {
                addEntity(rangeFinderEntity);
            }
        }
    }

    private void refresh() {
        RangeFinderEntity selectedSource = sourceComboBox.getSelectionModel().getSelectedItem();
        RangeFinderEntity selectedTarget = targetComboBox.getSelectionModel().getSelectedItem();

        sourceComboBox.getItems().clear();
        targetComboBox.getItems().clear();

        ObservableList<RangeFinderEntity> sourceList = FXCollections.observableArrayList();
        ObservableList<RangeFinderEntity> targetList = FXCollections.observableArrayList();

        targetList.add(RangeFinderEntity.ALL);

        for (IEntity entity : EntityManager.getEntities()) {
            RangeFinderEntity rangeFinderEntity = new RangeFinderEntity(entity.getEntityId());
            if (entity.isFiltered()) {
                sourceList.add(rangeFinderEntity);
                targetList.add(rangeFinderEntity);
            }
        }

        sourceList.sort(new TrackingEntityComparator());
        targetList.sort(new TrackingEntityComparator());

        sourceComboBox.getItems().setAll(sourceList);
        targetComboBox.getItems().setAll(targetList);

        sourceComboBox.getSelectionModel().select(selectedSource);
        targetComboBox.getSelectionModel().select(selectedTarget);

        entryTable.refresh();
    }

    private void entityTableSelectedChange() {
        RangeFinderEntry rangeFinderEntry = entryTable.getSelectionModel().getSelectedItem();
        if (rangeFinderEntry != null) {
            sourceComboBox.getSelectionModel().select(rangeFinderEntry.getSource());
            targetComboBox.getSelectionModel().select(rangeFinderEntry.getTarget());
            minRangeTextField.setText(String.valueOf(rangeFinderEntry.getMinRange()));
            maxRangeTextField.setText(String.valueOf(rangeFinderEntry.getMaxRange()));
            colorPicker.setValue(rangeFinderEntry.getColor());
            hideLinesCheckBox.setSelected(rangeFinderEntry.getShowLines());
            followTerrainCheckBox.setSelected(rangeFinderEntry.isFollowTerrain());
            ignoreFiltersCheckBox.setSelected(rangeFinderEntry.isIgnoreFilters());
            updateButton.setDisable(false);
        } else {
            updateButton.setDisable(true);
        }
    }

    private void sourceTargetChange() {
        RangeFinderEntity source = sourceComboBox.getSelectionModel().getSelectedItem();
        RangeFinderEntity target = targetComboBox.getSelectionModel().getSelectedItem();

        if (source == null || target == null) {
            entryTable.getSelectionModel().select(null);
            return;
        }

        RangeFinderEntry rangeFinderEntry = new RangeFinderEntry(source, target);
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
        RangeFinderManager.removeRangeFinderEntry(rangeFinderEntry);
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

    private final class TrackingEntityComparator implements Comparator<RangeFinderEntity> {

        @Override
        public int compare(RangeFinderEntity o1, RangeFinderEntity o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }

            if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            } else if (o1.isAll() && o2.isAll()) {
                return 0;
            } else if (o1.isAll()) {
                return -1;
            } else if (o2.isAll()) {
                return 1;
            }

            if (displayType == DisplayType.BY_ID) {
                return o1.getEntityId().compareTo(o2.getEntityId());
            }

            EntityDetail entityDetail1 = getEntityDetail(o1);
            EntityDetail entityDetail2 = getEntityDetail(o2);

            if (entityDetail1 == null && entityDetail2 == null) {
                return o1.getEntityId().compareTo(o2.getEntityId());
            } else if (entityDetail1 == null) {
                return 1;
            } else if (entityDetail2 == null) {
                return -1;
            }

            if (displayType == DisplayType.BY_NAME) {
                return entityDetail1.getName().compareTo(entityDetail2.getName());
            } else {
                return entityDetail1.getEntityType().compareTo(entityDetail2.getEntityType());
            }
        }

        private EntityDetail getEntityDetail(RangeFinderEntity rangeFinderEntity) {
            IEntity entity = EntityManager.getEntity(rangeFinderEntity.getEntityId());
            if (entity == null) {
                return null;
            } else {
                return entity.getEntityDetail();
            }
        }
    }

    private final class ComboBoxCellFactory implements Callback<ListView<RangeFinderEntity>, ListCell<RangeFinderEntity>> {
        @Override
        public ListCell<RangeFinderEntity> call(ListView<RangeFinderEntity> param) {
            return new ListCell<>() {
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
