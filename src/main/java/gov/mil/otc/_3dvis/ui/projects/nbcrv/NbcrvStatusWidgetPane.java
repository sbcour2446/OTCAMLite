package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvEntity;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.tir.TirReader;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.viewer.image.ImageViewerManager;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class NbcrvStatusWidgetPane implements IWidgetPane {

    public static void show(IEntity entity) {
        NbcrvStatusWidgetPane nbcrvStatusWidgetPane = nbcrvStatusWidgetPaneMap.get(entity.getEntityId());
        if (nbcrvStatusWidgetPane == null) {
            nbcrvStatusWidgetPane = create(entity);
            if (nbcrvStatusWidgetPane == null) {
                return;
            }
            nbcrvStatusWidgetPaneMap.put(entity.getEntityId(), nbcrvStatusWidgetPane);
            WidgetPaneContainer.addWidgetPane(nbcrvStatusWidgetPane);
        }
        WidgetPaneContainer.showWidgetPane(nbcrvStatusWidgetPane);
    }

    private static final Map<EntityId, NbcrvStatusWidgetPane> nbcrvStatusWidgetPaneMap = new ConcurrentHashMap<>();

    private final VBox mainVBox = new VBox();
    private final TableView<DetectionView> detectionTableView = new TableView<>();
    private final TableViewExtra<DetectionView> detectionTableViewExtra = new TableViewExtra<>(detectionTableView);
    private final ListView<TimestampFile> screenshotListView = new ListView<>();
    private final ListView<TimestampFile> tirListView = new ListView<>();
    private final TextField operationalStatusTextField = new TextField();
    private final NbcrvDevicePane nbcrvDevicePane;
    private final NbcrvOptionsPane nbcrvOptionsPane = new NbcrvOptionsPane();
    private final NbcrvEntity entity;
    private final Timer updateTimer = new Timer("NbcrvStatusWidgetPane:updateTimer");

    private static NbcrvStatusWidgetPane create(IEntity entity) {
        if (entity instanceof NbcrvEntity) {
            return new NbcrvStatusWidgetPane((NbcrvEntity) entity);
        }
        return null;
    }

    public NbcrvStatusWidgetPane(NbcrvEntity entity) {
        this.entity = entity;
        nbcrvDevicePane = new NbcrvDevicePane(entity);
        initialize();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateDetectionTable();
                Platform.runLater(() -> {
                    screenshotListView.refresh();
                    tirListView.refresh();
                });
            }
        }, 1000, 100);
    }

    private void initialize() {
        initializeDetectionTableView();
        initializeImageList();
        initializeTirList();

        Tab statusTab = new Tab("Detections");
        statusTab.setContent(detectionTableView);
        Tab screenShotTab = new Tab("Screen Shots");
        screenShotTab.setContent(screenshotListView);
        Tab tirTab = new Tab("TIRs");
        tirTab.setContent(tirListView);
        Tab optionsTab = new Tab("Options");
        optionsTab.setContent(nbcrvOptionsPane.getPane());
        TabPane tabPane = new TabPane(statusTab, screenShotTab, tirTab, optionsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMinHeight(100);

        SplitPane splitPane = new SplitPane(tabPane, nbcrvDevicePane.getPane());
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setMaxHeight(Double.MAX_VALUE);

        mainVBox.getChildren().addAll(splitPane);

        URL url = ThemeHelper.class.getResource("/css/widget_pane_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            screenshotListView.getStylesheets().add(css);
            tirListView.getStylesheets().add(css);
        }
    }

    private void initializeDetectionTableView() {
        TableColumn<DetectionView, String> timeTableColumn = new TableColumn<>("Time");
        timeTableColumn.setCellValueFactory(new PropertyValueFactory<>("formattedTime"));
        TableColumn<DetectionView, String> sensorTableColumn = new TableColumn<>("Sensor");
        sensorTableColumn.setCellValueFactory(new PropertyValueFactory<>("deviceName"));
        TableColumn<DetectionView, String> isActiveTableColumn = new TableColumn<>("Active");
        isActiveTableColumn.setCellValueFactory(new PropertyValueFactory<>("isActive"));

        timeTableColumn.setSortable(false);
        sensorTableColumn.setSortable(false);

        timeTableColumn.prefWidthProperty().bind(detectionTableView.widthProperty().multiply(21.0 / 37));
        sensorTableColumn.prefWidthProperty().bind(detectionTableView.widthProperty().multiply(15.0 / 37));
        isActiveTableColumn.prefWidthProperty().bind(detectionTableView.widthProperty().multiply(0 / 37));

        detectionTableView.getColumns().add(timeTableColumn);
        detectionTableView.getColumns().add(sensorTableColumn);
        detectionTableView.getColumns().add(isActiveTableColumn);

        detectionTableView.getSortOrder().add(timeTableColumn);

        detectionTableView.setEditable(false);
        detectionTableView.setRowFactory(detectionViewTableView -> new DetectionTableRow());
        detectionTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                TimeManager.setTime(newValue.getTimestamp()));

        detectionTableViewExtra.setRowFactory();

        ObservableList<DetectionView> observableList = FXCollections.observableArrayList();
        SortedList<DetectionView> sortedList = new SortedList<>(observableList);
        sortedList.setComparator(Comparator.comparingLong(DetectionView::getTimestamp));
        detectionTableView.setItems(sortedList);

        for (Device device : entity.getDeviceList()) {
            for (NbcrvDetection nbcrvDetection : device.getNbcrvDetectionList()) {
                observableList.add(new DetectionView(nbcrvDetection));
            }
        }

        URL url = ThemeHelper.class.getResource("/css/widget_pane_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            detectionTableView.getStylesheets().add(css);
        }
    }

    private void initializeImageList() {
        screenshotListView.setCellFactory(timestampFileListView -> new TimestampFileCell());
        screenshotListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        screenshotListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ImageViewerManager.show(entity, entity.getTimedImageMap());
            TimeManager.setTime(newValue.getTimestamp());
        });
        for (Map.Entry<Long, File> entry : entity.getTimedImageMap().entrySet()) {
            screenshotListView.getItems().add(new TimestampFile(entry.getKey(), entry.getValue()));
        }
    }

    private void initializeTirList() {
        tirListView.setCellFactory(timestampFileListView -> new TimestampFileCell());
        tirListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tirListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TirReader.show(newValue.file);
        });
        for (TimedFile timedFile : entity.getTirFileList()) {
            tirListView.getItems().add(new TimestampFile(timedFile.getTimestamp(), timedFile.getFile()));
        }
    }

    private void updateDetectionTable() {
        int firstIndex = -1;
        int lastIndex = -1;
        boolean updated = false;
        for (int i = 0; i < detectionTableView.getItems().size(); i++) {
            DetectionView detectionView = detectionTableView.getItems().get(i);
            final boolean active = detectionView.getNbcrvEvent().isActive(TimeManager.getTime());
            if (active != detectionView.isIsActive()) {
                detectionView.setIsActive(active);
                updated = true;
                if (active) {
                    if (firstIndex == -1) {
                        firstIndex = i;
                        lastIndex = i;
                    } else {
                        lastIndex = i;
                    }
                }
            }
        }
        if (updated) {
            final int scrollToFirst = firstIndex;
            final int scrollToLast = lastIndex;
            Platform.runLater(() -> {
                detectionTableView.refresh();
                if (scrollToFirst > 0) {
                    detectionTableViewExtra.scrollToIndex(scrollToFirst, scrollToLast);
                }
            });
        }
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public Pane getPane() {
        return mainVBox;
    }

    @Override
    public void dispose() {
        nbcrvDevicePane.dispose();
        nbcrvStatusWidgetPaneMap.remove(entity.getEntityId());
        updateTimer.cancel();
    }

    public static class DetectionTableRow extends TableRow<DetectionView> {

        final Tooltip tooltip = new Tooltip();

        @Override
        public void updateItem(DetectionView detectionView, boolean empty) {
            super.updateItem(detectionView, empty);

            if (detectionView == null) {
                setStyle("");
            } else {
                PseudoClass active = PseudoClass.getPseudoClass("active");
                pseudoClassStateChanged(active, detectionView.isIsActive());

                tooltip.setText(detectionView.getNbcrvEvent().getDescription());

                if (!detectionView.isIsActive()) {
                    setStyle("");
                }
            }
        }
    }

    public class TimestampFileCell extends ListCell<TimestampFile> {

        final Tooltip tooltip = new Tooltip();

        public TimestampFileCell() {
            getStyleClass().add("list-view-transparent");
        }

        @Override
        public void updateItem(TimestampFile timestampFile, boolean empty) {
            super.updateItem(timestampFile, empty);

            if (timestampFile == null) {
                setGraphic(null);
                setStyle(null);
                setText(null);
            } else {
                File currentFile = ImageViewerManager.getCurrent(entity);

                if (timestampFile.getFile().equals(currentFile)) {
                    setStyle("-fx-background-color: red");
                } else {
                    setStyle("");
                }

                setText(timestampFile.toString());
                tooltip.setText(timestampFile.getFile().getAbsolutePath());
                setTooltip(tooltip);
            }
        }
    }

    public static class TimestampFile {

        private final long timestamp;
        private final File file;

        public TimestampFile(Long timestamp, File file) {
            this.timestamp = timestamp;
            this.file = file;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public File getFile() {
            return file;
        }

        public String toString() {
            return Utility.formatTime(timestamp);
        }
    }
}
