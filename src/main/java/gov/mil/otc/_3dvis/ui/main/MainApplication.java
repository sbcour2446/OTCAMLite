package gov.mil.otc._3dvis.ui.main;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.datamodel.EntityTypeUtility;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.layer.timeline.TimelineLayer;
import gov.mil.otc._3dvis.layer.utility.ToolTipController;
import gov.mil.otc._3dvis.layer.view.ViewLayer;
import gov.mil.otc._3dvis.overlay.OverlayManager;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.settings.ViewPosition;
import gov.mil.otc._3dvis.tena.ConnectionState;
import gov.mil.otc._3dvis.tena.TenaController;
import gov.mil.otc._3dvis.tena.TenaNativeLibraryLoader;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.application.tena.TenaDisconnectController;
import gov.mil.otc._3dvis.ui.contextmenu.ContextMenuController;
import gov.mil.otc._3dvis.playback.PlaybackLoader;
import gov.mil.otc._3dvis.ui.main.menu.MainMenu;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import gov.mil.otc._3dvis.ui.widgets.entity.entitytype.EntityTypeTreeLoader;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.mil.otc._3dvis.utility.KeepAlive;
import gov.mil.otc._3dvis.worldwindex.view.orbit.BasicOrbitViewEx;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Position;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApplication extends Application {

    private static final String APPLICATION_NAME = "3DVis";
    private final Timer startupTimer = new Timer("Startup Timer");
    private static MainApplication instance = null;
    private Stage mainStage = null;
    private Scene mainScene = null;
    private final BorderPane mainPane = new BorderPane();
    private final StackPane stackPane = new StackPane();
    private final BorderPane loadingPane = new BorderPane();
    private final SwingNode swingNode = new SwingNode();
    private final List<Node> nodeList = new ArrayList<>();
    private boolean zoomComplete = false;
    private final Object initializationMutex = new Object();
    private boolean initializationComplete = false;
    private int startupCounter = 0;

    public void showErrorMessage(final String message) {
        if (Platform.isFxApplicationThread()) {
            DialogUtilities.showErrorDialog("Error", message, mainStage);
        } else {
            Platform.runLater(() -> DialogUtilities.showErrorDialog("Error", message, mainStage));
        }
    }

    public void addPane(Node node) {
        nodeList.add(node);
        stackPane.getChildren().add(node);
    }

    public void removePane(Node node) {
        stackPane.getChildren().remove(node);
        nodeList.remove(node);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        WWController.initialize();
        startInitialization();

        setInstance(this);
        mainStage = primaryStage;

        ContextMenuController.initialize(primaryStage);

        Platform.setImplicitExit(false);

        primaryStage.getIcons().add(ImageLoader.getLogo());
        primaryStage.setOnCloseRequest(this::handleWindowCloseRequest);
        primaryStage.setTitle(APPLICATION_NAME);

        createMainPane();

        mainScene = new Scene(mainPane, 1200, 800);
        ThemeHelper.applyTheme(mainScene);

        primaryStage.setScene(mainScene);

        StageSizer stageSizer = new StageSizer("3DVis MainWindow");
        stageSizer.setStage(primaryStage);

        startupTimer.schedule(new DelayedStartTask(), 2000);

        if (!new File("milstd2525-symbols.jar").exists()) {
            DialogUtilities.showWarningDialog("Missing File!",
                    "File \"milstd2525-symbols.jar\" is missing.", true, mainStage);
        }
    }

    private void createMainPane() {
        WorldWindowGLJPanel wwPanel = WWController.getWorldWindowPanel();
        javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.BorderLayout());
        panel.add(wwPanel, java.awt.BorderLayout.CENTER);

        swingNode.setContent(panel);

        stackPane.getChildren().add(swingNode);
        stackPane.getChildren().add(createLoadingPane());
        stackPane.getChildren().add(WidgetPaneContainer.getPane());

        mainPane.setTop(MainMenu.getMainMenu());
        mainPane.getTop().setDisable(true);
        mainPane.setCenter(stackPane);
    }

    private BorderPane createLoadingPane() {
        Label label = new Label("loading...");
        label.setAlignment(Pos.CENTER);
        label.setFont(new Font("Consolas Bold", 48));
        Glow glow = new Glow();
        glow.setLevel(5);
        label.setEffect(glow);

        loadingPane.setCenter(label);
        loadingPane.setStyle("-fx-background-color: black;");
        loadingPane.setOpacity(.5);
        return loadingPane;
    }

    public static MainApplication getInstance() {
        return instance;
    }

    public Stage getStage() {
        return mainStage;
    }

    public Scene getScene() {
        return mainScene;
    }

    public SwingNode getSwingNode() {
        return swingNode;
    }

    private static void setInstance(MainApplication mainApplication) {
        instance = mainApplication;
    }

    private void startInitialization() {
        new Thread(() -> {
            if (SettingsManager.getPreferences().getKeepAlive()) {
                KeepAlive.start();
            }
            EntityTypeUtility.initialize();
            TimeManager.initialize();
            TimelineLayer.initialize();
            ViewLayer.initialize();
            ToolTipController.initialize();
            OverlayManager.initialize();

            EntityTypeTreeLoader.start();

            DataManager.initialize();
            EntityManager.start();
            EventManager.start();

            if (SettingsManager.getPreferences().isLoadPlaybackOnStartup()) {
                Platform.runLater(() ->
                        PlaybackLoader.loadPlaybackAsync(SettingsManager.getPreferences().getLastPlayback(), getStage(), null));
            }

            initializationComplete = true;
            synchronized (initializationMutex) {
                initializationMutex.notifyAll();
            }
        }, "MainApplication - initialize").start();
    }

    private class DelayedStartTask extends TimerTask {

        @Override
        public void run() {
            if (startupCounter++ > 10) {
                DialogUtilities.showErrorDialog("Startup Failed!",
                        "Error starting graphics controller.", mainStage);
                mainStage.close();
            }

            if (WWController.getWorldWindowPanel().getView().getGlobe() == null) {
                startupTimer.schedule(new DelayedStartTask(), 1000);
                return;
            }

            if (WWController.getWorldWindowPanel().getView() instanceof BasicOrbitViewEx) {
                ViewPosition startupView = SettingsManager.getPreferences().getStartupView();
                ((BasicOrbitViewEx) WWController.getWorldWindowPanel().getView()).goTo(
                        startupView.getEyePosition(),
                        startupView.getHeading(),
                        startupView.getPitch(),
                        startupView.getEyeDistance());
            }

            while (!initializationComplete) {
                synchronized (initializationMutex) {
                    try {
                        initializationMutex.wait(100);
                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, null, e);
                    }
                }
            }

            loadingComplete();
        }

        private void loadingComplete() {
            Platform.runLater(() -> {
                stackPane.getChildren().remove(loadingPane);
                mainPane.getTop().setDisable(false);
            });
        }
    }

    private void handleWindowCloseRequest(WindowEvent event) {
        Logger.getGlobal().log(Level.INFO, "shutting down 3DVis");

        if (DialogUtilities.showYesNoDialog("Close 3DVis", "Are you sure you want to shutdown 3DVis?", mainStage)) {
            if (TenaNativeLibraryLoader.isInitialized() && (TenaController.getConnectionState() == ConnectionState.CONNECTED
                    || TenaController.getConnectionState() == ConnectionState.DISCONNECTING)) {
                TenaDisconnectController.show();
            }

            DatabaseShutdown.showShutdownAndCommit();

            View view = WWController.getWorldWindowPanel().getView();
            Position position = WWController.getWorldWindowPanel().getModel().getGlobe().computePositionFromPoint(view.getCenterPoint());
            ViewPosition startupView = new ViewPosition();
            startupView.setEyePosition(position);
            startupView.setHeading(view.getHeading());
            startupView.setPitch(view.getPitch());
            startupView.setEyeDistance(view.getCenterPoint().distanceTo3(view.getEyePoint()));
            SettingsManager.getPreferences().setStartupView(startupView);

            EventManager.shutdown();
            EntityManager.shutdown();
            WWController.shutdown();
            TimeManager.shutdown();

            EntityTypeUtility.shutdown();
            KeepAlive.shutdown();

            mainStage.close();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
                Thread.currentThread().interrupt();
            }

            Logger.getGlobal().log(Level.INFO, "exiting 3DVis");

            Platform.exit();
        } else {
            event.consume();
        }
    }
}
