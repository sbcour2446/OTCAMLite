package gov.mil.otc._3dvis.ui.tools.eventtable;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.event.RtcaEvent;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.UiConstants;
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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.*;


public class EventTableController {

    public static synchronized void show() {
        singleton.doShow();
    }

    private static final String NO_DATA = "no data to display";
    private static final EventTableController singleton = new EventTableController();
    private final Stage stage = new Stage();
    private final TableView<EventView> eventTableView = new TableView<>();
    private final TableView<RtcaEventView> rtcaEventTableView = new TableView<>();
    private final TableView<C2MessageEventView> c2MessageEventTableView = new TableView<>();
    private final ObservableList<EventView> eventViewObservableList = FXCollections.observableArrayList();
    private final ObservableList<RtcaEventView> rtcaEventViewObservableList = FXCollections.observableArrayList();
    private final ObservableList<C2MessageEventView> c2MessageEventViewObservableList = FXCollections.observableArrayList();
    private final SortedList<EventView> eventSortedList = new SortedList<>(eventViewObservableList);
    private final SortedList<RtcaEventView> rtcaEventSortedList = new SortedList<>(rtcaEventViewObservableList);
    private final SortedList<C2MessageEventView> c2MessageEventSortedList = new SortedList<>(c2MessageEventViewObservableList);
    private final List<Event> eventList = new ArrayList<>();

    private EventTableController() {
        initializeEventTable();
        initializeRtcaEventTable();
        initializeC2MessageEventTable();

        RadioButton allRadioButton = new RadioButton("All Events");
        RadioButton rtcaRadioButton = new RadioButton("RTCA Events");
        RadioButton c2MessgaeButton = new RadioButton("C2 Messages");
        ToggleGroup toggleGroup = new ToggleGroup();
        toggleGroup.getToggles().addAll(allRadioButton, rtcaRadioButton, c2MessgaeButton);
        allRadioButton.setOnAction(event -> {
            eventTableView.setVisible(true);
            rtcaEventTableView.setVisible(false);
            c2MessageEventTableView.setVisible(false);
        });
        rtcaRadioButton.setOnAction(event -> {
            eventTableView.setVisible(false);
            rtcaEventTableView.setVisible(true);
            c2MessageEventTableView.setVisible(false);
        });
        c2MessgaeButton.setOnAction(event -> {
            eventTableView.setVisible(false);
            rtcaEventTableView.setVisible(false);
            c2MessageEventTableView.setVisible(true);
        });
        allRadioButton.setSelected(true);

        HBox hBox = new HBox(UiConstants.SPACING, allRadioButton, rtcaRadioButton, c2MessgaeButton);
        hBox.setPadding(new Insets(UiConstants.SPACING));

        StackPane stackPane = new StackPane(eventTableView, rtcaEventTableView, c2MessageEventTableView);
        ScrollPane scrollPane = new ScrollPane(stackPane);
        scrollPane.setPadding(new Insets(0, UiConstants.SPACING, UiConstants.SPACING, UiConstants.SPACING));
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        rtcaEventTableView.setVisible(false);
        c2MessageEventTableView.setVisible(false);

        BorderPane borderPane = new BorderPane(scrollPane);
        borderPane.setTop(hBox);
        borderPane.setPrefWidth(800);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle("Event Table");
        stage.initOwner(MainApplication.getInstance().getStage());
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Event Table");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
    }

    private void doShow() {
        stage.show();

        final Timer updateTimer = new Timer("EventTable: updateTimer");
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateEventList();
            }
        }, 0, 1000);
        stage.setOnCloseRequest(event -> updateTimer.cancel());
    }

    private void initializeEventTable() {
        TableColumn<EventView, String> timestampTableColumn = new TableColumn<>("Event Time");
        timestampTableColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampTableColumn.setSortable(false);

        TableColumn<EventView, String> eventTypeTableColumn = new TableColumn<>("Event Type");
        eventTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("eventType"));
        eventTypeTableColumn.setSortable(false);

        TableColumn<EventView, String> descriptionTableColumn = new TableColumn<>("Description");
        descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        descriptionTableColumn.setSortable(false);

        eventTableView.getColumns().add(timestampTableColumn);
        eventTableView.getColumns().add(eventTypeTableColumn);
        eventTableView.getColumns().add(descriptionTableColumn);

        timestampTableColumn.prefWidthProperty().bind(eventTableView.widthProperty().multiply(6.0 / 37.0));
        eventTypeTableColumn.prefWidthProperty().bind(eventTableView.widthProperty().multiply(12.0 / 37.0));
        descriptionTableColumn.prefWidthProperty().bind(eventTableView.widthProperty().multiply(18.0 / 37.0));

        eventTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        eventTableView.setPlaceholder(new Label(NO_DATA));

        eventSortedList.setComparator(Comparator.comparingLong(o -> o.getEvent().getTimestamp()));
        eventTableView.setItems(eventSortedList);
        eventTableView.setRowFactory(param -> createRowFactory(eventTableView, EventView.class));
    }

    private void initializeRtcaEventTable() {
        TableColumn<RtcaEventView, String> timestampTableColumn = new TableColumn<>("Event Time");
        timestampTableColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timestampTableColumn.setSortable(false);

        TableColumn<RtcaEventView, String> eventTypeTableColumn = new TableColumn<>("Event Type");
        eventTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("eventType"));
        eventTypeTableColumn.setSortable(false);

        TableColumn<RtcaEventView, TableColumn<?, ?>> shooterTableColumn = new TableColumn<>("Source/Shooter");

        TableColumn<RtcaEventView, String> shooterNameTableColumn = new TableColumn<>("Name");
        shooterNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("shooterName"));
        shooterNameTableColumn.setSortable(false);

        TableColumn<RtcaEventView, String> shooterAffiliationTableColumn = new TableColumn<>("Affiliation");
        shooterAffiliationTableColumn.setCellValueFactory(new PropertyValueFactory<>("shooterAffiliation"));
        shooterAffiliationTableColumn.setSortable(false);

        TableColumn<RtcaEventView, Position> shooterLatLonTableColumn = new TableColumn<>("Lat/Lon");
        shooterLatLonTableColumn.setCellValueFactory(new PropertyValueFactory<>("shooterLocation"));
        shooterLatLonTableColumn.setCellFactory(new LatLonTableColumnCellFactory<>());
        shooterLatLonTableColumn.setSortable(false);

        TableColumn<RtcaEventView, Position> shooterElevationTableColumn = new TableColumn<>("Elevation");
        shooterElevationTableColumn.setCellValueFactory(new PropertyValueFactory<>("shooterLocation"));
        shooterElevationTableColumn.setCellFactory(new ElevationTableColumnCellFactory<>());
        shooterElevationTableColumn.setSortable(false);

        TableColumn<RtcaEventView, Position> shooterMgrsTableColumn = new TableColumn<>("MGRS");
        shooterMgrsTableColumn.setCellValueFactory(new PropertyValueFactory<>("shooterLocation"));
        shooterMgrsTableColumn.setCellFactory(new MgrsTableColumnCellFactory<>());
        shooterMgrsTableColumn.setSortable(false);

        shooterTableColumn.getColumns().add(shooterNameTableColumn);
        shooterTableColumn.getColumns().add(shooterAffiliationTableColumn);
        shooterTableColumn.getColumns().add(shooterLatLonTableColumn);
        shooterTableColumn.getColumns().add(shooterElevationTableColumn);
        shooterTableColumn.getColumns().add(shooterMgrsTableColumn);

        TableColumn<RtcaEventView, TableColumn<?, ?>> targetTableColumn = new TableColumn<>("Target/Impact");

        TableColumn<RtcaEventView, String> targetNameTableColumn = new TableColumn<>("Name");
        targetNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("targetName"));
        targetNameTableColumn.setSortable(false);

        TableColumn<RtcaEventView, String> targetAffiliationTableColumn = new TableColumn<>("Affiliation");
        targetAffiliationTableColumn.setCellValueFactory(new PropertyValueFactory<>("targetAffiliation"));
        targetAffiliationTableColumn.setSortable(false);

        TableColumn<RtcaEventView, Position> targetLatLonTableColumn = new TableColumn<>("Lat/Lon");
        targetLatLonTableColumn.setCellValueFactory(new PropertyValueFactory<>("targetLocation"));
        targetLatLonTableColumn.setCellFactory(new LatLonTableColumnCellFactory<>());
        targetLatLonTableColumn.setSortable(false);

        TableColumn<RtcaEventView, Position> targetElevationTableColumn = new TableColumn<>("Elevation");
        targetElevationTableColumn.setCellValueFactory(new PropertyValueFactory<>("targetLocation"));
        targetElevationTableColumn.setCellFactory(new ElevationTableColumnCellFactory<>());
        targetElevationTableColumn.setSortable(false);

        TableColumn<RtcaEventView, Position> targetMgrsTableColumn = new TableColumn<>("MGRS");
        targetMgrsTableColumn.setCellValueFactory(new PropertyValueFactory<>("targetLocation"));
        targetMgrsTableColumn.setCellFactory(new MgrsTableColumnCellFactory<>());
        targetMgrsTableColumn.setSortable(false);

        targetTableColumn.getColumns().add(targetNameTableColumn);
        targetTableColumn.getColumns().add(targetAffiliationTableColumn);
        targetTableColumn.getColumns().add(targetLatLonTableColumn);
        targetTableColumn.getColumns().add(targetElevationTableColumn);
        targetTableColumn.getColumns().add(targetMgrsTableColumn);

        TableColumn<RtcaEventView, String> munitionTableColumn = new TableColumn<>("Munition");
        munitionTableColumn.setCellValueFactory(new PropertyValueFactory<>("munition"));
        munitionTableColumn.setSortable(false);

        TableColumn<RtcaEventView, String> resultTableColumn = new TableColumn<>("Result");
        resultTableColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultTableColumn.setSortable(false);

        rtcaEventTableView.getColumns().add(timestampTableColumn);
        rtcaEventTableView.getColumns().add(eventTypeTableColumn);
        rtcaEventTableView.getColumns().add(shooterTableColumn);
        rtcaEventTableView.getColumns().add(targetTableColumn);
        rtcaEventTableView.getColumns().add(munitionTableColumn);
        rtcaEventTableView.getColumns().add(resultTableColumn);

        rtcaEventTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        rtcaEventTableView.setTableMenuButtonVisible(true);
        rtcaEventTableView.setPlaceholder(new Label(NO_DATA));

        rtcaEventSortedList.setComparator(Comparator.comparingLong(o -> o.getEvent().getTimestamp()));
        rtcaEventTableView.setItems(rtcaEventSortedList);
        rtcaEventTableView.setRowFactory(param -> createRowFactory(rtcaEventTableView, RtcaEventView.class));
        rtcaEventTableView.setVisible(false);
    }

    private void initializeC2MessageEventTable() {
        TableColumn<C2MessageEventView, String> originatorTimeTableColumn = new TableColumn<>("Originator Time");
        originatorTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("originatorTime"));
        originatorTimeTableColumn.setSortable(false);

        TableColumn<C2MessageEventView, String> messageTypeTableColumn = new TableColumn<>("Message Type");
        messageTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("messageType"));
        messageTypeTableColumn.setSortable(false);

        TableColumn<C2MessageEventView, String> senderUrnTableColumn = new TableColumn<>("Sender URN");
        senderUrnTableColumn.setCellValueFactory(new PropertyValueFactory<>("senderUrn"));
        senderUrnTableColumn.setSortable(false);

        TableColumn<C2MessageEventView, String> senderNameTableColumn = new TableColumn<>("Sender Name");
        senderNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("senderName"));
        senderNameTableColumn.setSortable(false);

        TableColumn<C2MessageEventView, String> destinationUrnListTableColumn = new TableColumn<>("Destination URN(s)");
        destinationUrnListTableColumn.setCellValueFactory(new PropertyValueFactory<>("destinationUrnList"));
        destinationUrnListTableColumn.setSortable(false);

        TableColumn<C2MessageEventView, String> destinationNamesTableColumn = new TableColumn<>("Destination Names");
        destinationNamesTableColumn.setCellValueFactory(new PropertyValueFactory<>("destinationNames"));
        destinationNamesTableColumn.setSortable(false);

        TableColumn<C2MessageEventView, Position> summaryTableColumn = new TableColumn<>("Summary");
        summaryTableColumn.setCellValueFactory(new PropertyValueFactory<>("summary"));
        summaryTableColumn.setSortable(false);

        TableColumn<C2MessageEventView, Position> messageTableColumn = new TableColumn<>("Message");
        messageTableColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageTableColumn.setSortable(false);

        c2MessageEventTableView.getColumns().add(originatorTimeTableColumn);
        c2MessageEventTableView.getColumns().add(messageTypeTableColumn);
        c2MessageEventTableView.getColumns().add(senderUrnTableColumn);
        c2MessageEventTableView.getColumns().add(senderNameTableColumn);
        c2MessageEventTableView.getColumns().add(destinationUrnListTableColumn);
        c2MessageEventTableView.getColumns().add(destinationNamesTableColumn);
        c2MessageEventTableView.getColumns().add(summaryTableColumn);
        c2MessageEventTableView.getColumns().add(messageTableColumn);

        c2MessageEventTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        c2MessageEventTableView.setTableMenuButtonVisible(true);
        c2MessageEventTableView.setPlaceholder(new Label(NO_DATA));

        c2MessageEventSortedList.setComparator(Comparator.comparingLong(o -> o.getEvent().getTimestamp()));
        c2MessageEventTableView.setItems(c2MessageEventSortedList);
        c2MessageEventTableView.setRowFactory(param -> createRowFactory(c2MessageEventTableView, C2MessageEventView.class));
        c2MessageEventTableView.setVisible(false);
    }

    private <T> TableRow<T> createRowFactory(TableView<? extends EventView> tableView, Class<T> classType) {
        if (tableView.getClass().equals(classType)) {
            return null;
        }

        final TableRow<T> row = new TableRow<>();
        final ContextMenu rowMenu = new ContextMenu();

        MenuItem eventTimeMenuItem = new MenuItem("go to event time");
        eventTimeMenuItem.setOnAction(event -> {
            EventView eventView = tableView.getSelectionModel().getSelectedItem();
            if (eventView != null) {
                moveToEventTime(eventView);
            }
        });
        MenuItem eventLocationMenuItem = new MenuItem("go to event location");
        eventLocationMenuItem.setOnAction(event -> {
            EventView eventView = tableView.getSelectionModel().getSelectedItem();
            if (eventView != null) {
                moveToEventLocation(eventView);
            }
        });
        MenuItem bothMenuItem = new MenuItem("go to both");
        bothMenuItem.setOnAction(event -> {
            EventView eventView = tableView.getSelectionModel().getSelectedItem();
            if (eventView != null) {
                moveToEventTime(eventView);
                moveToEventLocation(eventView);
            }
        });

        rowMenu.getItems().addAll(eventTimeMenuItem, eventLocationMenuItem, bothMenuItem);
        row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
        row.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                EventView eventView = tableView.getSelectionModel().getSelectedItem();
                if (eventView != null) {
                    moveToEventTime(eventView);
                    moveToEventLocation(eventView);
                }
            }
        });

        return row;
    }

    private void updateEventList() {
        final List<Event> newEvents = new ArrayList<>();
        for (Event event : EventManager.getEvents()) {
            synchronized (eventList) {
                if (!eventList.contains(event)) {
                    newEvents.add(event);
                }
            }
        }
        if (!newEvents.isEmpty()) {
            Platform.runLater(() -> addEvents(newEvents));
        }
    }

    private void addEvents(List<Event> eventsList) {
        synchronized (eventList) {
            for (Event event : eventsList) {
                if (!eventList.contains(event)) {
                    eventList.add(event);
                    eventViewObservableList.add(new EventView(event));
                    if (event instanceof RtcaEvent) {
                        rtcaEventViewObservableList.add(new RtcaEventView((RtcaEvent) event));
                    } else if (event instanceof C2MessageEvent) {
                        c2MessageEventViewObservableList.add(new C2MessageEventView((C2MessageEvent) event));
                    } else {

                    }
                }
            }
        }
    }

    private void moveToEventTime(EventView eventView) {
        TimeManager.setTime(eventView.getEvent().getTimestamp());
    }

    private void moveToEventLocation(EventView eventView) {
        if (eventView.getEvent() instanceof RtcaEvent) {
            Position position = ((RtcaEvent) eventView.getEvent()).getEventLocation();
            if (position != null) {
                View view = WWController.getView();
                if (view != null) {
                    double distance = view.getCenterPoint().distanceTo3(view.getEyePoint());
                    view.goTo(new Position(position.latitude, position.longitude, 0), distance);
                }
            }
        }
    }
}
