package gov.mil.otc._3dvis.ui.utility;

import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.settings.WindowGeometry;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Sizes and stores window geometries by provided stage.
 */
public class StageSizer {
    /**
     * Offset margin for re-positioning the stage if it is out of bounds.
     */
    private static final double MARGIN = 50;

    /**
     * The name of the window to store in settings.
     */
    private final String entryName;

    /**
     * Constructor.
     *
     * @param entryName The name of the window to store in settings. This must be unique, but is not checked.
     */
    public StageSizer(String entryName) {
        this.entryName = entryName;
    }

    /**
     * Sets the stage and performs resizing and positioning, if necessary.
     *
     * @param stage The stage in question.
     */
    public void setStage(Stage stage) {
        resizeAndPosition(stage);
    }

    /**
     * Sets the stage and performs resizing and positioning, if necessary.  Use parent stage to position
     * if not already saved.
     *
     * @param stage  The stage in question.
     * @param parent The parent stage.
     */
    public void setStage(Stage stage, Stage parent) {
        initialize(parent);
        resizeAndPosition(stage);
    }

    /**
     * Initialize size from saved or create relative to parent.
     *
     * @param parent The parent stage.
     */
    private void initialize(Stage parent) {
        WindowGeometry windowGeometry = SettingsManager.getPreferences().getWindowGeometry(entryName);
        if (windowGeometry == null && parent != null) {
            windowGeometry = new WindowGeometry();
            double x = parent.getX() + 100;
            double y = parent.getY() + 100;
            windowGeometry.setX(x);
            windowGeometry.setY(y);
            SettingsManager.getPreferences().setWindowGeometry(entryName, windowGeometry);
        }
    }

    /**
     * Resizes and positions the stage on the JavaFX thread.
     *
     * @param stage The stage in question
     */
    private void resizeAndPosition(Stage stage) {
        if (Platform.isFxApplicationThread()) {
            doResizeAndPosition(stage);
        } else {
            Platform.runLater(() ->
                    doResizeAndPosition(stage));
        }
    }

    /**
     * Performs resizing and position of the launched stage.
     *
     * @param stage The launched stage.
     */
    private void doResizeAndPosition(Stage stage) {
        if (getX() != null) {
            stage.setX(getX());
        }

        if (getY() != null) {
            stage.setY(getY());
        }

        if (getWidth() != null) {
            stage.setWidth(getWidth());
        }

        if (getHeight() != null) {
            stage.setHeight(getHeight());
        }

        if (isMaximized() != null) {
            stage.setMaximized(isMaximized());
        }

        stage.show();

        // If the stage is not visible in any of the current screens, relocate it the primary screen.
        if (isWindowIsOutOfBounds(stage)) {
            moveToPrimaryScreen(stage);
        }
        // And now watch the stage to keep the properties updated.
        watchStage(stage);
    }

    /**
     * Checks if the stage is out of the visible bounds of the monitors.
     *
     * @param stage The stage to check if it is out of the visible bounds.
     * @return True if the window is out of the visible founds, false otherwise.
     */
    private boolean isWindowIsOutOfBounds(Stage stage) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = 0;
        double maxY = 0;

        for (Screen screen : Screen.getScreens()) {
            if (screen.getVisualBounds().getMinX() < minX) {
                minX = screen.getVisualBounds().getMinX();
            }
            if (screen.getVisualBounds().getMinY() < minY) {
                minY = screen.getVisualBounds().getMinY();
            }
            if (screen.getVisualBounds().getMaxX() > maxX) {
                maxX = screen.getVisualBounds().getMaxX();
            }
            if (screen.getVisualBounds().getMaxY() > maxY) {
                maxY = screen.getVisualBounds().getMaxY();
            }
        }

        double margin = 8.0;
        return !(stage.getX() >= (minX - margin) && stage.getX() <= (maxX + margin)
                && stage.getY() >= (minY - margin) && stage.getY() <= (maxY + margin));
    }

    /**
     * If the stage is out of visual bounds, set it to the primary screen and position it.
     *
     * @param stage The stage to position.
     */
    private void moveToPrimaryScreen(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX() + MARGIN);
        stage.setY(bounds.getMinY() + MARGIN);
    }

    /**
     * Watches the stage for changes to record to settings.
     *
     * @param stage The stage to watch.
     */
    private void watchStage(Stage stage) {
        // Get the current values
        SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setX(stage.getX());
        SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setY(stage.getY());
        SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setWidth(stage.getWidth());
        SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setHeight(stage.getHeight());
        SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setMaximized(stage.isMaximized());

        // Watch for future changes
        stage.xProperty().addListener((observable, old, x) ->
                SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setX((Double) x));
        stage.yProperty().addListener((observable, old, y) ->
                SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setY((Double) y));
        stage.widthProperty().addListener((observable, old, width) ->
                SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setWidth((Double) width));
        stage.heightProperty().addListener((observable, old, height) ->
                SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setHeight((Double) height));
        stage.maximizedProperty().addListener((observable, old, maximized) ->
                SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).setMaximized(maximized));
    }

    /**
     * Gets the width.
     *
     * @return The width if previously saved, null if no entry exists yet.
     */
    private Double getWidth() {
        return SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).getWidth();
    }

    /**
     * Gets the height.
     *
     * @return The height if previously saved, null if no entry exists yet.
     */
    private Double getHeight() {
        return SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).getHeight();
    }

    /**
     * Gets the x position.
     *
     * @return The x position if previously saved, null if no entry exists yet.
     */
    private Double getX() {
        return SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).getX();
    }

    /**
     * Gets the y position.
     *
     * @return The y position if previously saved, null if no entry exists yet.
     */
    private Double getY() {
        return SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).getY();
    }

    /**
     * Gets if the last entry was maximized.
     *
     * @return The maximization state if previously saved, null if no entry exists yet.
     */
    private Boolean isMaximized() {
        return SettingsManager.getPreferences().getOrCreateWindowGeometry(entryName).isMaximized();
    }
}
