package gov.mil.otc._3dvis.ui.tools;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.TrackingAttribute;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.IEntityListener;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.IntegerStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TrackingSettingsController implements IEntityListener {

    private static TrackingSettingsController trackingSettingsController;
    private final TableView<TrackingEntry> entryTable = new TableView<>();
    private final ObservableList<TrackingEntry> trackingEntries = FXCollections.observableArrayList();
    private final Stage stage = new Stage();

    public static synchronized void show() {
        if (trackingSettingsController == null) {
            trackingSettingsController = new TrackingSettingsController();
        }
        trackingSettingsController.stage.show();
    }

    private TrackingSettingsController() {
        initialize();
    }

    private void initialize() {
        initializeTable();
        loadTable();

        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Tracking");

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> onAddAction());
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(event -> onRemoveAction());

        HBox buttonHBox = new HBox(UiConstants.SPACING, addButton, removeButton);
        buttonHBox.setPadding(new Insets(UiConstants.SPACING));

        BorderPane borderPane = new BorderPane(
                entryTable,
                null,
                null,
                buttonHBox,
                null);
        borderPane.setPadding(new Insets(UiConstants.SPACING));

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Tracking");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
        stage.setOnCloseRequest(event -> onCloseRequest());
        stage.setOnShowing(event -> onShowing());
    }

    private void onShowing() {
        loadTable();
        EntityManager.addEntityListener(this);
    }

    private void onCloseRequest() {
        EntityManager.removeEntityListener(this);
    }

    private void onAddAction() {
        IEntity entity = EntityPicker.show(stage);
        if (entity != null) {
            createTrackingEntity(entity);
        }
    }

    private void onRemoveAction() {
        List<TrackingEntry> selectedEntries = new ArrayList<>(entryTable.getSelectionModel().getSelectedItems());
        for (TrackingEntry trackingEntry : selectedEntries) {
            removeTrackingEntry(trackingEntry);
        }
    }

    @Override
    public void onEntityAdded(IEntity entity) {
        //do nothing
    }

    @Override
    public void onEntityUpdated(IEntity entity) {
        //todo update name column
    }

    @Override
    public void onEntityDisposed(IEntity entity) {
        //todo remove entity if necessary?
    }

    private void createTrackingEntity(IEntity entity) {
        TrackingEntry trackingEntry = new TrackingEntry(entity.getEntityId());

        EntityDetail entityDetail = entity.getEntityDetail();
        if (entityDetail != null) {
            trackingEntry.setName(entityDetail.getName());
        }

        TrackingAttribute trackingAttribute = entity.getTrackingAttribute();
        trackingEntry.setEnabled(trackingAttribute.isEnabled());
        trackingEntry.setCutoff(trackingAttribute.getCutoff());
        trackingEntry.setColor(trackingAttribute.getColor());
        trackingEntry.setDrawVerticals(trackingAttribute.isDrawVerticals());

        if (!entryTable.getItems().contains(trackingEntry)) {
            entryTable.getItems().add(trackingEntry);
        }
    }

    private void removeTrackingEntry(TrackingEntry trackingEntry) {
        IEntity entity = EntityManager.getEntity(trackingEntry.getEntityId());
        if (entity != null) {
            entity.getTrackingAttribute().setEnabled(false);
            trackingEntries.remove(trackingEntry);
        }
    }

    private void loadTable() {
        entryTable.getItems().clear();
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity.getTrackingAttribute() != null && entity.getTrackingAttribute().isEnabled()) {
                createTrackingEntity(entity);
            }
        }
    }

    private void initializeTable() {
        TableColumn<TrackingEntry, Boolean> enabledTableColumn = new TableColumn<>("Enabled");
        enabledTableColumn.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        enabledTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(enabledTableColumn));
        enabledTableColumn.setEditable(true);

        TableColumn<TrackingEntry, TableColumn<?, ?>> entityTableColumn = new TableColumn<>("Entity");

        TableColumn<TrackingEntry, EntityId> entityIdTableColumn = new TableColumn<>("ID");
        entityIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityId"));

        TableColumn<TrackingEntry, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        entityTableColumn.getColumns().add(entityIdTableColumn);
        entityTableColumn.getColumns().add(nameTableColumn);

        TableColumn<TrackingEntry, Color> colorTableColumn = new TableColumn<>("Color");
        colorTableColumn.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorTableColumn.setCellFactory(param -> new ColorTableCell<>(colorTableColumn));

        TableColumn<TrackingEntry, Integer> cutoffTableColumn = new TableColumn<>("Cutoff");
        cutoffTableColumn.setCellValueFactory(new PropertyValueFactory<>("cutoff"));
        cutoffTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        cutoffTableColumn.setEditable(true);

        TableColumn<TrackingEntry, Boolean> drawVerticalsTableColumn = new TableColumn<>("Draw Verticals");
        drawVerticalsTableColumn.setCellValueFactory(new PropertyValueFactory<>("drawVerticals"));
        drawVerticalsTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(enabledTableColumn));
        drawVerticalsTableColumn.setEditable(true);

        centerColumn(enabledTableColumn);
        centerColumn(entityIdTableColumn);
        centerColumn(nameTableColumn);
        centerColumn(colorTableColumn);
        centerColumn(cutoffTableColumn);
        centerColumn(drawVerticalsTableColumn);

        entryTable.getColumns().add(enabledTableColumn);
        entryTable.getColumns().add(entityTableColumn);
        entryTable.getColumns().add(colorTableColumn);
        entryTable.getColumns().add(cutoffTableColumn);
        entryTable.getColumns().add(drawVerticalsTableColumn);

        entryTable.setItems(trackingEntries);

        entryTable.setEditable(true);
    }

    private void centerColumn(TableColumn<?, ?> column) {
        column.setStyle("-fx-alignment: CENTER");
    }

    public static class ColorTableCell<T> extends TableCell<T, Color> {
        private final ColorPicker colorPicker;

        public ColorTableCell(TableColumn<T, Color> column) {
            colorPicker = new ColorPicker();
            colorPicker.editableProperty().bind(column.editableProperty());
            colorPicker.disableProperty().bind(column.editableProperty().not());
            colorPicker.setOnShowing(event -> {
                final TableView<T> tableView = getTableView();
                tableView.getSelectionModel().select(getTableRow().getIndex());
                tableView.edit(tableView.getSelectionModel().getSelectedIndex(), column);
            });
            colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (isEditing()) {
                    commitEdit(newValue);
                }
            });
            colorPicker.setStyle("-fx-background-color: transparent;");
            colorPicker.getStyleClass().add("button");
            colorPicker.setMaxWidth(Double.MAX_VALUE);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(Color item, boolean empty) {
            super.updateItem(item, empty);

            setText(null);
            if (empty) {
                setGraphic(null);
            } else {
                colorPicker.setValue(item);
                setGraphic(colorPicker);
            }
        }
    }

    public static class TrackingEntry {
        private final EntityId entityId;
        private final BooleanProperty enabled = new SimpleBooleanProperty(false);
        private final StringProperty name = new SimpleStringProperty("");
        private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.YELLOW);
        private final IntegerProperty cutoff = new SimpleIntegerProperty(3600000);
        private final BooleanProperty drawVerticals = new SimpleBooleanProperty(false);

        private TrackingEntry(EntityId entityId) {
            this.entityId = entityId;
        }

        public EntityId getEntityId() {
            return entityId;
        }

        public boolean isEnabled() {
            return enabled.get();
        }

        public BooleanProperty enabledProperty() {
            IEntity entity = EntityManager.getEntity(entityId);
            if (entity != null) {
                entity.getTrackingAttribute().setEnabled(enabled.get());
            }
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled.set(enabled);
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public Color getColor() {
            return color.get();
        }

        public ObjectProperty<Color> colorProperty() {
            IEntity entity = EntityManager.getEntity(entityId);
            if (entity != null) {
                entity.getTrackingAttribute().setColor(color.get());
            }
            return color;
        }

        public void setColor(Color color) {
            this.color.set(color);
        }

        public int getCutoff() {
            return cutoff.get();
        }

        public IntegerProperty cutoffProperty() {
            IEntity entity = EntityManager.getEntity(entityId);
            if (entity != null) {
                entity.getTrackingAttribute().setCutoff(cutoff.get());
            }
            return cutoff;
        }

        public void setCutoff(int cutoff) {
            this.cutoff.set(cutoff);
        }

        public boolean isDrawVerticals() {
            return drawVerticals.get();
        }

        public BooleanProperty drawVerticalsProperty() {
            IEntity entity = EntityManager.getEntity(entityId);
            if (entity != null) {
                entity.getTrackingAttribute().setDrawVerticals(drawVerticals.get());
            }
            return drawVerticals;
        }

        public void setDrawVerticals(boolean drawVerticals) {
            this.drawVerticals.set(drawVerticals);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TrackingEntry trackingEntry = (TrackingEntry) o;
            return entityId.equals(trackingEntry.entityId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityId);
        }
    }
}
