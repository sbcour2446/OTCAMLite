package gov.mil.otc._3dvis.ui.tools.entitytable;

import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.IEntityListener;
import gov.mil.otc._3dvis.entity.RtcaCommand;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.EntityTableSettings;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgets.tableview.ElevationTableColumnCellFactory;
import gov.mil.otc._3dvis.ui.widgets.tableview.LatLonTableColumnCellFactory;
import gov.mil.otc._3dvis.ui.widgets.tableview.MgrsTableColumnCellFactory;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityTableController implements IEntityListener {

    private static final String POSITION_FIELD = "position";
    private static EntityTableController entityTable = null;
    private final Stage stage = new Stage();
    private final TableView<EntityView> entityTableView = new TableView<>();
    private final ConcurrentHashMap<EntityId, EntityView> entityViewMap = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<IEntity> updateQueue = new ConcurrentLinkedQueue<>();
    private final ObservableList<EntityView> filteredList = FXCollections.observableArrayList();
    private final SortedList<EntityView> sortedList = new SortedList<>(filteredList);
    private final Filter filter = new Filter();
    private Thread updateThread;
    private boolean isRunning = false;

    public static synchronized void show() {
        if (entityTable == null) {
            entityTable = new EntityTableController();
        }
        entityTable.doShow();
    }

    private EntityTableController() {
        initializeTable();

        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle("Entity Table");
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.setOnCloseRequest(event -> {
            EntityManager.removeEntityListener(this);
            isRunning = false;
            synchronized (updateQueue) {
                updateQueue.notifyAll();
            }
            updateThread = null;
            removeAll();
        });
        Scene scene = new Scene(new BorderPane(
                entityTableView,
                new MenuBar(createFilterMenu(), createColumnsMenu()),
                null,
                null,
                null));
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Entity Table");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
    }

    @Override
    public void onEntityAdded(final IEntity entity) {
        if (!updateQueue.contains(entity)) {
            updateQueue.add(entity);
            synchronized (updateQueue) {
                updateQueue.notifyAll();
            }
        }
    }

    @Override
    public void onEntityUpdated(IEntity entity) {
        if (!updateQueue.contains(entity)) {
            updateQueue.add(entity);
            synchronized (updateQueue) {
                updateQueue.notifyAll();
            }
        }
    }

    @Override
    public void onEntityDisposed(IEntity entity) {
        removeOrIgnore(entity);
    }

    private void initializeTable() {
        createColumns();
        loadTablePreferences();

        entityTableView.setRowFactory(param -> {
            ContextMenu rowMenu = new ContextMenu();

            MenuItem menuItem = new MenuItem("Center on Entity");
            menuItem.setOnAction(event -> {
                EntityView selectedEntity = entityTableView.getSelectionModel().getSelectedItem();
                if (selectedEntity != null) {
                    moveToSelection(selectedEntity);
                }
            });
            rowMenu.getItems().add(menuItem);

            menuItem = new MenuItem("Go to first TSPI");
            menuItem.setOnAction(event -> {
                EntityView selectedEntity = entityTableView.getSelectionModel().getSelectedItem();
                if (selectedEntity != null) {
                    goToFirstTspi(selectedEntity);
                }
            });
            rowMenu.getItems().add(menuItem);

            Menu menu = new Menu("Send RTCA Command");
            rowMenu.getItems().add(menu);

            for (RtcaCommand.Type type : RtcaCommand.Type.values()) {
                menuItem = new MenuItem(type.toString());
                menuItem.setOnAction(event -> {
                    for (EntityView selectedEntity : entityTableView.getSelectionModel().getSelectedItems()) {
                        IEntity entity = EntityManager.getEntity(selectedEntity.getEntityId());
                        if (entity != null) {
                            entity.sendRtcaCommand(new RtcaCommand(entity, type));
                        }
                    }
                });
                menu.getItems().add(menuItem);
            }

            TableRow<EntityView> row = new TableRow<>();
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
            return row;
        });

        entityTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (oldSelection != null) {
                IEntity entity = EntityManager.getEntity(oldSelection.getEntityId());
                if (entity != null) {
                    entity.setSelected(false);
                }
            }
            if (newSelection != null) {
                IEntity entity = EntityManager.getEntity(newSelection.getEntityId());
                if (entity != null) {
                    entity.setSelected(true);
                }
            }
        });
    }

    private void createColumns() {
        TableColumn<EntityView, EntityId> entityIdTableColumn = new TableColumn<>("Entity ID");
        entityIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityId"));

        TableColumn<EntityView, String> sourceTableColumn = new TableColumn<>("Source");
        sourceTableColumn.setCellValueFactory(new PropertyValueFactory<>("source"));

        TableColumn<EntityView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<EntityView, String> descriptionTableColumn = new TableColumn<>("Description");
        descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<EntityView, String> affiliationTableColumn = new TableColumn<>("Affiliation");
        affiliationTableColumn.setCellValueFactory(new PropertyValueFactory<>("affiliation"));

        TableColumn<EntityView, String> urnTableColumn = new TableColumn<>("URN");
        urnTableColumn.setCellValueFactory(new PropertyValueFactory<>("urn"));

        TableColumn<EntityView, TableColumn<?, ?>> rtcaStateTableColumn = new TableColumn<>("RTCA");

        TableColumn<EntityView, String> rtcaAbbrColumn = new TableColumn<>("ABBR");
        rtcaAbbrColumn.setCellValueFactory(new PropertyValueFactory<>("rtcaState"));

        TableColumn<EntityView, ImageView> killCatastrophicTableColumn = new TableColumn<>("KKill");
        killCatastrophicTableColumn.setCellValueFactory(new PropertyValueFactory<>("killCatastrophic"));

        TableColumn<EntityView, ImageView> killMobilityTableColumn = new TableColumn<>("MKill");
        killMobilityTableColumn.setCellValueFactory(new PropertyValueFactory<>("killMobility"));

        TableColumn<EntityView, ImageView> killFirepowerTableColumn = new TableColumn<>("FKill");
        killFirepowerTableColumn.setCellValueFactory(new PropertyValueFactory<>("killFirepower"));

        TableColumn<EntityView, ImageView> killCommunicationTableColumn = new TableColumn<>("CKill");
        killCommunicationTableColumn.setCellValueFactory(new PropertyValueFactory<>("killCommunication"));

        TableColumn<EntityView, ImageView> suppressedTableColumn = new TableColumn<>("Suppressed");
        suppressedTableColumn.setCellValueFactory(new PropertyValueFactory<>("suppression"));

        TableColumn<EntityView, ImageView> jammedTableColumn = new TableColumn<>("Jammed");
        jammedTableColumn.setCellValueFactory(new PropertyValueFactory<>("jammed"));

        TableColumn<EntityView, ImageView> hitNoKillTableColumn = new TableColumn<>("Hit");
        hitNoKillTableColumn.setCellValueFactory(new PropertyValueFactory<>("hitNoKill"));

        TableColumn<EntityView, ImageView> missTableColumn = new TableColumn<>("Miss");
        missTableColumn.setCellValueFactory(new PropertyValueFactory<>("miss"));

        TableColumn<EntityView, ImageView> outOfCommsTableColumn = new TableColumn<>("Out-of-Comms");
        outOfCommsTableColumn.setCellValueFactory(new PropertyValueFactory<>("outOfComms"));

        TableColumn<EntityView, ImageView> outOfScopeTableColumn = new TableColumn<>("Out-of-Scope");
        outOfScopeTableColumn.setCellValueFactory(new PropertyValueFactory<>("outOfScope"));

        TableColumn<EntityView, ImageView> timedOutTableColumn = new TableColumn<>("Timed Out");
        timedOutTableColumn.setCellValueFactory(new PropertyValueFactory<>("timedOut"));

        TableColumn<EntityView, String> lastUpdateTableColumn = new TableColumn<>("Last Update");
        lastUpdateTableColumn.setCellValueFactory(new PropertyValueFactory<>("lastUpdate"));

        TableColumn<EntityView, TableColumn<?, ?>> positionTableColumn = new TableColumn<>("Position");

        TableColumn<EntityView, Position> latLonTableColumn = new TableColumn<>("Lat/Lon");
        latLonTableColumn.setCellValueFactory(new PropertyValueFactory<>(POSITION_FIELD));
        latLonTableColumn.setCellFactory(new LatLonTableColumnCellFactory<>());

        TableColumn<EntityView, Position> elevationTableColumn = new TableColumn<>("Elevation");
        elevationTableColumn.setCellValueFactory(new PropertyValueFactory<>(POSITION_FIELD));
        elevationTableColumn.setCellFactory(new ElevationTableColumnCellFactory<>());

        TableColumn<EntityView, Position> terrainTableColumn = new TableColumn<>("Terrain");
        terrainTableColumn.setCellValueFactory(new PropertyValueFactory<>(POSITION_FIELD));
        terrainTableColumn.setCellFactory(new TerrainTableColumnCellFactory<>());

        TableColumn<EntityView, Position> mgrsTableColumn = new TableColumn<>("MGRS");
        mgrsTableColumn.setCellValueFactory(new PropertyValueFactory<>(POSITION_FIELD));
        mgrsTableColumn.setCellFactory(new MgrsTableColumnCellFactory<>());

        TableColumn<EntityView, String> milesPidTableColumn = new TableColumn<>("MILES PID");
        milesPidTableColumn.setCellValueFactory(new PropertyValueFactory<>("milesPid"));

        TableColumn<EntityView, String> entityTypeTableColumn = new TableColumn<>("Entity Type");
        entityTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));

        TableColumn<EntityView, String> militarySymbolTableColumn = new TableColumn<>("Military Symbol");
        militarySymbolTableColumn.setCellValueFactory(new PropertyValueFactory<>("militarySymbol"));

        centerColumn(rtcaAbbrColumn);
        centerColumn(killCatastrophicTableColumn);
        centerColumn(killMobilityTableColumn);
        centerColumn(killFirepowerTableColumn);
        centerColumn(killCommunicationTableColumn);
        centerColumn(jammedTableColumn);
        centerColumn(suppressedTableColumn);
        centerColumn(hitNoKillTableColumn);
        centerColumn(missTableColumn);
        centerColumn(outOfCommsTableColumn);
        centerColumn(timedOutTableColumn);
        centerColumn(outOfScopeTableColumn);

        rtcaStateTableColumn.getColumns().add(rtcaAbbrColumn);
        rtcaStateTableColumn.getColumns().add(killCatastrophicTableColumn);
        rtcaStateTableColumn.getColumns().add(killMobilityTableColumn);
        rtcaStateTableColumn.getColumns().add(killFirepowerTableColumn);
        rtcaStateTableColumn.getColumns().add(killCommunicationTableColumn);
        rtcaStateTableColumn.getColumns().add(jammedTableColumn);
        rtcaStateTableColumn.getColumns().add(suppressedTableColumn);
        rtcaStateTableColumn.getColumns().add(hitNoKillTableColumn);
        rtcaStateTableColumn.getColumns().add(missTableColumn);

        positionTableColumn.getColumns().add(latLonTableColumn);
        positionTableColumn.getColumns().add(elevationTableColumn);
        positionTableColumn.getColumns().add(terrainTableColumn);
        positionTableColumn.getColumns().add(mgrsTableColumn);

        entityTableView.getColumns().add(entityIdTableColumn);
        entityTableView.getColumns().add(sourceTableColumn);
        entityTableView.getColumns().add(nameTableColumn);
        entityTableView.getColumns().add(descriptionTableColumn);
        entityTableView.getColumns().add(affiliationTableColumn);
        entityTableView.getColumns().add(urnTableColumn);
        entityTableView.getColumns().add(rtcaStateTableColumn);
        entityTableView.getColumns().add(outOfCommsTableColumn);
        entityTableView.getColumns().add(timedOutTableColumn);
        entityTableView.getColumns().add(outOfScopeTableColumn);
        entityTableView.getColumns().add(lastUpdateTableColumn);
        entityTableView.getColumns().add(positionTableColumn);
        entityTableView.getColumns().add(milesPidTableColumn);
        entityTableView.getColumns().add(entityTypeTableColumn);
        entityTableView.getColumns().add(militarySymbolTableColumn);

        for (TableColumn<EntityView, ?> tableColumn : entityTableView.getColumns()) {
            tableColumn.widthProperty().addListener((observable, oldValue, newValue) ->
                    SettingsManager.getPreferences().getEntityTableSettings().setColumnWidth(
                            tableColumn.getText(), tableColumn.widthProperty().get()));
        }

        entityTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        entityTableView.setPlaceholder(new Label("loading"));

        entityTableView.setItems(sortedList);

        sortedList.comparatorProperty().bind(entityTableView.comparatorProperty());
        entityTableView.getSortOrder().add(entityIdTableColumn);
        entityIdTableColumn.setSortType(TableColumn.SortType.ASCENDING);
    }

    private void loadTablePreferences() {
        EntityTableSettings entityTableSettings = SettingsManager.getPreferences().getEntityTableSettings();
        for (TableColumn<EntityView, ?> tableColumn : entityTableView.getColumns()) {
            tableColumn.setVisible(entityTableSettings.isColumnVisible(tableColumn.getText()));
            Double width = entityTableSettings.getColumnWidth(tableColumn.getText());
            if (width != null) {
                tableColumn.setPrefWidth(width);
            }
        }
    }

    private void centerColumn(TableColumn<?, ?> column) {
        column.setStyle("-fx-alignment: CENTER");
    }

    private Menu createFilterMenu() {
        CheckMenuItem outOfCommsCheckMenuItem = new CheckMenuItem("Show Out-of-Comms");
        outOfCommsCheckMenuItem.setSelected(true);
        outOfCommsCheckMenuItem.setOnAction(event -> {
            filter.setFilterOutOfComms(outOfCommsCheckMenuItem.isSelected());
            updateFilter();
        });

        CheckMenuItem outOfScopeCheckMenuItem = new CheckMenuItem("Show Out-of-Scope");
        outOfScopeCheckMenuItem.setSelected(true);
        outOfScopeCheckMenuItem.setOnAction(event -> {
            filter.setFilterOutOfScope(outOfScopeCheckMenuItem.isSelected());
            updateFilter();
        });

        CheckMenuItem timedOutCheckMenuItem = new CheckMenuItem("Show Timed-Out");
        timedOutCheckMenuItem.setSelected(true);
        timedOutCheckMenuItem.setOnAction(event -> {
            filter.setFilterTimedOut(timedOutCheckMenuItem.isSelected());
            updateFilter();
        });

        Menu affiliationMenu = new Menu("Affiliation");
        for (Affiliation affiliation : Affiliation.values()) {
            CheckMenuItem menuItem = new CheckMenuItem(affiliation.getName());
            menuItem.setSelected(true);
            menuItem.setOnAction(event -> {
                filter.setAffiliationFilter(affiliation, menuItem.isSelected());
                updateFilter();
            });
            affiliationMenu.getItems().add(menuItem);
        }

        Menu menu = new Menu("Filter");
        menu.getItems().add(outOfCommsCheckMenuItem);
        menu.getItems().add(outOfScopeCheckMenuItem);
        menu.getItems().add(timedOutCheckMenuItem);
        menu.getItems().add(affiliationMenu);

        return menu;
    }

    private Menu createColumnsMenu() {
        Menu columnsMenu = new Menu("Columns");
        EntityTableSettings entityTableSettings = SettingsManager.getPreferences().getEntityTableSettings();
        for (TableColumn<EntityView, ?> tableColumn : entityTableView.getColumns()) {
            if (tableColumn.getColumns().isEmpty()) {
                CheckMenuItem checkMenuItem = new CheckMenuItem(tableColumn.getText());
                tableColumn.setVisible(entityTableSettings.isColumnVisible(tableColumn.getText()));
                checkMenuItem.setSelected(entityTableSettings.isColumnVisible(tableColumn.getText()));
                checkMenuItem.setOnAction(event -> {
                    tableColumn.setVisible(checkMenuItem.isSelected());
                    SettingsManager.getPreferences().getEntityTableSettings().setColumnVisible(tableColumn.getText(),
                            checkMenuItem.isSelected());
                });
                columnsMenu.getItems().add(checkMenuItem);
            } else {
                for (TableColumn<EntityView, ?> tableSubColumn : tableColumn.getColumns()) {
                    CheckMenuItem checkMenuItem = new CheckMenuItem(tableSubColumn.getText());
                    tableSubColumn.setVisible(entityTableSettings.isColumnVisible(tableSubColumn.getText()));
                    checkMenuItem.setSelected(entityTableSettings.isColumnVisible(tableSubColumn.getText()));
                    checkMenuItem.setOnAction(event -> {
                        tableSubColumn.setVisible(checkMenuItem.isSelected());
                        SettingsManager.getPreferences().getEntityTableSettings().setColumnVisible(tableSubColumn.getText(),
                                checkMenuItem.isSelected());
                    });
                    columnsMenu.getItems().add(checkMenuItem);
                }
            }
        }
        return columnsMenu;
    }

    private void doShow() {
        if (!isRunning) {
            isRunning = true;
            updateThread = new Thread(EntityTableController.this::update, "Entity Table Update Thread");
            updateThread.start();
            new Thread(this::initializeEntities, "Entity Table Loading Thread").start();
        }
        entityTable.stage.show();
    }

    private void initializeEntities() {
        for (IEntity entity : EntityManager.getEntities()) {
            if (!updateQueue.contains(entity)) {
                updateQueue.add(entity);
            }
        }
        synchronized (updateQueue) {
            updateQueue.notifyAll();
        }

        Platform.runLater(() -> {
            entityTableView.setPlaceholder(new Label("no data to display"));
            entityTableView.sort();
            EntityManager.addEntityListener(this);
        });
    }

    private void update() {
        while (isRunning) {
            synchronized (updateQueue) {
                try {
                    updateQueue.wait();
                } catch (InterruptedException e) {
                    Logger.getGlobal().log(Level.WARNING, "", e);
                    Thread.currentThread().interrupt();
                }
            }
            while (!updateQueue.isEmpty() && isRunning) {
                IEntity entity = updateQueue.poll();
                boolean visible = filter.isVisible(entity);
                if (visible) {
                    addOrUpdate(entity);
                } else {
                    removeOrIgnore(entity);
                }
            }
        }
    }

    private void addOrUpdate(IEntity entity) {
        Platform.runLater(() -> {
            EntityView entityView = entityViewMap.get(entity.getEntityId());
            if (entityView == null) {
                entityView = new EntityView(entity);
                entityViewMap.put(entityView.getEntityId(), entityView);
            } else {
                entityView.update(entity);
            }
            if (!filteredList.contains(entityView)) {
                filteredList.add(entityView);
            }
        });
    }

    private void removeAll() {
        Platform.runLater(() -> {
            filteredList.clear();
            entityViewMap.clear();
        });
    }

    private void removeOrIgnore(IEntity entity) {
        Platform.runLater(() -> {
            EntityView entityView = entityViewMap.get(entity.getEntityId());
            if (entityView != null) {
                filteredList.remove(entityView);
            }
        });
    }

    private void updateFilter() {
        for (IEntity entity : EntityManager.getEntities()) {
            if (!updateQueue.contains(entity)) {
                updateQueue.add(entity);
            }
        }
        synchronized (updateQueue) {
            updateQueue.notifyAll();
        }
    }

    private void moveToSelection(EntityView selectedEntity) {
        IEntity entity = EntityManager.getEntity(selectedEntity.getEntityId());
        Position position = entity.getPosition();
        if (position != null) {
            View view = WWController.getView();
            if (view != null) {
                double distance = view.getCenterPoint().distanceTo3(view.getEyePoint());
                view.goTo(new Position(position.latitude, position.longitude, 0), distance);
            }
        }
    }

    private void goToFirstTspi(EntityView selectedEntity) {
        IEntity entity = EntityManager.getEntity(selectedEntity.getEntityId());
        TspiData tspi = entity.getFirstTspi();
        if (tspi != null) {
            TimeManager.setTime(tspi.getTimestamp());
        }
    }

    private static final class TerrainTableColumnCellFactory<T> implements Callback<TableColumn<T, Position>,
            TableCell<T, Position>> {
        @Override
        public TableCell<T, Position> call(TableColumn param) {
            return new TableCell<>() {
                @Override
                public void updateItem(Position item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        double elevation = WWController.getWorldWindowPanel().getModel().getGlobe().getElevation(
                                item.getLatitude(), item.getLongitude());
                        setText(String.format("%,d", (int) elevation));
                    } else {
                        setText("");
                        setGraphic(null);
                    }
                }
            };
        }
    }
}
