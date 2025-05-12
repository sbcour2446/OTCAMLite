package gov.mil.otc._3dvis.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.tools.media.CreateMediaClipController;
import gov.mil.otc._3dvis.ui.utility.StageUtility;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaPlayerWindow {

    private static final double PADDING = 5.0;
    private static final double ZOOM_NORMAL = 1.0;
    private static final double ZOOM_MULTIPLIER = 1.25;
    private final IEntity entity;
    private final String mediaGroup;
    private final Map<MediaSet, MediaPlayerController> mediaPlayerControllerMap = new HashMap<>();
    private final Stage stage = new Stage();
    private final MenuBar menuBar = new MenuBar();
    private final CheckMenuItem muteMenuItem = new CheckMenuItem("Mute");
    private final BorderPane borderPane = new BorderPane();
    private final GridPane gridPane = new GridPane();
    private final Object stageSizerMutex = new Object();
    private final Object updatingSizeMutex = new Object();
    private double zoom = ZOOM_NORMAL;
    private boolean isClosed = false;
    private boolean isMuted = true;
    private boolean isUpdating = false;
    private long lastSizeChangeTime = 0;
    private Layout layout = Layout.VERTICAL;
    CheckMenuItem autoSizeCheckMenuItem = new CheckMenuItem("Auto-size");

    private enum Layout {
        VERTICAL,
        HORIZONTAL,
        GRID
    }

    protected MediaPlayerWindow(IEntity entity, String mediaGroup) {
        this.entity = entity;
        this.mediaGroup = mediaGroup;

        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setTitle(entity.getEntityId().toString() + ": " + entity.getName() + "  " + mediaGroup);

        initializeMenuBar();

        gridPane.setVgap(PADDING);
        gridPane.setHgap(PADDING);
        gridPane.setStyle("-fx-background-color: black");
        gridPane.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));

        borderPane.setTop(menuBar);

        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            synchronized (stageSizerMutex) {
                lastSizeChangeTime = System.nanoTime();
                stageSizerMutex.notifyAll();
            }
        });
        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            synchronized (stageSizerMutex) {
                lastSizeChangeTime = System.nanoTime();
                stageSizerMutex.notifyAll();
            }
        });

        Scene scene = new Scene(borderPane, 600, 400);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageUtility.centerStage(stage, MainApplication.getInstance().getStage());
        stage.setOnCloseRequest(this::handleWindowCloseRequest);
        stage.show();

        new Thread(this::stageSizeProcess, "media player stage sizer").start();
    }

    public void setMute(boolean mute) {
        isMuted = mute;
        Platform.runLater(() -> muteMenuItem.setSelected(isMuted));
        for (MediaPlayerController mediaPlayerController : mediaPlayerControllerMap.values()) {
            mediaPlayerController.setMute(mute);
        }
    }

    public void addMedia(MediaSet mediaSet) {
        if (!mediaPlayerControllerMap.containsKey(mediaSet)) {
            MediaPlayerController mediaPlayerController = new MediaPlayerController(entity, mediaSet);
            mediaPlayerController.setMute(isMuted);
            mediaPlayerControllerMap.put(mediaSet, mediaPlayerController);
            updateLayout();
        }
    }

    public void removeMedia(final MediaSet mediaSet) {
        if (Platform.isFxApplicationThread()) {
            removeMediaFxThread(mediaSet);
        } else {
            Platform.runLater(() -> removeMediaFxThread(mediaSet));
        }
    }

    private void removeMediaFxThread(MediaSet mediaSet) {
        MediaPlayerController mediaPlayerController = mediaPlayerControllerMap.remove(mediaSet);
        if (mediaPlayerControllerMap.isEmpty()) {
            close();
        } else if (mediaPlayerController != null) {
            updateLayout();
        }
    }

    private void handleWindowCloseRequest(WindowEvent event) {
        for (MediaPlayerController mediaPlayerController : mediaPlayerControllerMap.values()) {
            mediaPlayerController.close();
        }
        close();
    }

    private void close() {
        synchronized (stageSizerMutex) {
            isClosed = true;
            stageSizerMutex.notifyAll();
        }
        stage.close();
        MediaPlayerManager.mediaPlayerWindowClosed(entity, mediaGroup);
    }

    private void initializeMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem createClipMenuItem = new MenuItem("Create Clip");
        createClipMenuItem.setOnAction(event ->
                CreateMediaClipController.show(entity, new ArrayList<>(mediaPlayerControllerMap.keySet())));
        muteMenuItem.setSelected(isMuted);
        muteMenuItem.setOnAction(event -> setMute(muteMenuItem.isSelected()));
        fileMenu.getItems().addAll(createClipMenuItem, muteMenuItem);

        Menu zoomMenu = new Menu("Zoom");
        MenuItem fitWindowMenuItem = new MenuItem("Fit Window");
        MenuItem zoomResetMenuItem = new MenuItem("Reset");
        MenuItem zoomInMenuItem = new MenuItem("Zoom In");
        MenuItem zoomOutMenuItem = new MenuItem("Zoom Out");

        autoSizeCheckMenuItem.setSelected(true);

        zoomResetMenuItem.setOnAction(event -> updateZoom(ZOOM_NORMAL));
        fitWindowMenuItem.setOnAction(event -> updateStageSize());
        zoomInMenuItem.setOnAction(event -> updateZoom(zoom * ZOOM_MULTIPLIER));
        zoomOutMenuItem.setOnAction(event -> updateZoom(zoom / ZOOM_MULTIPLIER));

        zoomMenu.getItems().add(zoomResetMenuItem);
        zoomMenu.getItems().add(fitWindowMenuItem);
        zoomMenu.getItems().add(zoomInMenuItem);
        zoomMenu.getItems().add(zoomOutMenuItem);
        zoomMenu.getItems().add(autoSizeCheckMenuItem);

        Menu layoutMenu = new Menu("Layout");
        ToggleGroup toggleGroup = new ToggleGroup();
        for (Layout l : Layout.values()) {
            RadioMenuItem radioMenuItem = new RadioMenuItem(l.name());
            toggleGroup.getToggles().add(radioMenuItem);
            layoutMenu.getItems().add(radioMenuItem);
            if (l.equals(layout)) {
                radioMenuItem.setSelected(true);
            }
            radioMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    changeLayout(l);
                }
            });
        }

        menuBar.getMenus().addAll(fileMenu, zoomMenu, layoutMenu);
    }

    private void changeLayout(Layout newLayout) {
        int currentColumnCount = getColumnCount(layout);
        int currentRowCount = getRowCount(layout, currentColumnCount);
        int newColumnCount = getColumnCount(newLayout);
        int newRowCount = getRowCount(newLayout, newColumnCount);

        double colRatio = (double) newColumnCount / currentColumnCount / zoom;
        double rowRatio = (double) newRowCount / currentRowCount / zoom;

        double height = gridPane.getHeight() * rowRatio + stage.getScene().getY() + menuBar.getHeight() + PADDING * 2;
        double width = gridPane.getWidth() * colRatio + PADDING * 3;

        stage.setHeight(height);
        stage.setWidth(width);

        layout = newLayout;
        updateLayout();
    }

    private int getColumnCount(Layout layout) {
        return switch (layout) {
            case GRID -> mediaPlayerControllerMap.values().size() > 2 ? 2 : 1;
            case VERTICAL -> 1;
            case HORIZONTAL -> mediaPlayerControllerMap.values().size();
        };
    }

    private int getRowCount(Layout layout, int columns) {
        return switch (layout) {
            case GRID -> (int) Math.round(Math.ceil(mediaPlayerControllerMap.values().size() / (double) columns));
            case VERTICAL -> mediaPlayerControllerMap.values().size();
            case HORIZONTAL -> 1;
        };
    }

    private void stageSizeProcess() {
        while (!isClosed) {
            synchronized (stageSizerMutex) {
                try {
                    stageSizerMutex.wait();
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                    Thread.currentThread().interrupt();
                }
            }

            isUpdating = true;

            while (!isClosed && isUpdating && zoom == ZOOM_NORMAL && autoSizeCheckMenuItem.isSelected()) {
                long lastChangeDelta = System.nanoTime() - lastSizeChangeTime;
                if (lastChangeDelta > 100000000) {
                    updateStageSize();
                    try {
                        synchronized (updatingSizeMutex) {
                            updatingSizeMutex.wait();
                            isUpdating = false;
                        }
                    } catch (Exception e) {
                        System.out.println("");
                    }
                }

                synchronized (updatingSizeMutex) {
                    try {
                        updatingSizeMutex.wait(100);
                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, null, e);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void updateStageSize() {
        Platform.runLater(() -> {
            final double height = gridPane.getHeight() + stage.getScene().getY() + menuBar.getHeight() + PADDING * 2;
            final double width = gridPane.getWidth() + PADDING * 3;
            synchronized (updatingSizeMutex) {
                if (stage.getHeight() != height) {
                    stage.setHeight(height);
                }
                if (stage.getWidth() != width) {
                    stage.setWidth(width);
                }
                updatingSizeMutex.notify();
            }
        });
    }

    private void updateLayout() {
        gridPane.getChildren().clear();

        if (zoom == ZOOM_NORMAL) {
            borderPane.setCenter(new HBox(new VBox(gridPane)));
        } else {
            ScrollPane scrollPane = new ScrollPane(gridPane);
            scrollPane.setPannable(true);
            borderPane.setCenter(scrollPane);
        }

        int columnIndex = 0;
        int rowIndex = 0;
        int columns = getColumnCount(layout);
        int rows = getRowCount(layout, columns);

        for (MediaPlayerController mediaPlayerController : mediaPlayerControllerMap.values()) {
            ImageView imageView = mediaPlayerController.getVideoImageView();
            imageView.setPreserveRatio(true);
            imageView.fitHeightProperty().bind(((stage.heightProperty()
                    .subtract(stage.getScene().yProperty())
                    .subtract(menuBar.heightProperty())
                    .subtract((rows + 3) * PADDING))
                    .divide(rows))
                    .multiply(zoom));
            imageView.fitWidthProperty().bind(stage.widthProperty()
                    .subtract((columns + 4) * PADDING)
                    .divide(columns)
                    .multiply(zoom));
            gridPane.add(imageView, columnIndex, rowIndex++);
            if (rowIndex == rows) {
                columnIndex++;
                rowIndex = 0;
            }
        }

        synchronized (stageSizerMutex) {
            lastSizeChangeTime = System.nanoTime();
            stageSizerMutex.notifyAll();
        }
    }

    private void updateZoom(double newZoom) {
        if (newZoom < ZOOM_NORMAL) {
            newZoom = ZOOM_NORMAL;
        }
        if (zoom != newZoom) {
            zoom = newZoom;
            updateLayout();
        }
    }
}
