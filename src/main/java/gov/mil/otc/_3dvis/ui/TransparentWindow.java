package gov.mil.otc._3dvis.ui;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.main.menu.MainMenu;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public abstract class TransparentWindow {

    public enum WindowPosition {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        MIDDLE_LEFT,
        CENTER,
        MIDDLE_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT,
    }

    private final Stage stage = new Stage();
    private final Stage parentStage;
    private final Effect parentEffect = new BoxBlur();

    protected TransparentWindow() {
        this(MainApplication.getInstance().getStage());
    }

    protected TransparentWindow(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void createAndShow() {
        createAndShow(false);
    }

    public void createAndShow(boolean waitForResults) {
        createAndShow(waitForResults, true, WindowPosition.CENTER);
    }

    public void createAndShow(boolean waitForResults, boolean modal, WindowPosition position) {
        Pane contentPane = createContentPane();
        contentPane.getStyleClass().add("transparent-content");

        final Pane glassPane = new Pane();
        glassPane.getStyleClass().add("transparent-glass");

        StackPane stackPane = new StackPane();
        stackPane.getChildren().setAll(glassPane, contentPane);
        stackPane.setStyle("-fx-background: transparent;");

        Scene scene = new Scene(stackPane, Color.TRANSPARENT);
        ThemeHelper.applyTheme(scene);

        stage.getIcons().add(ImageLoader.getLogo());
        if (modal) {
            stage.initModality(Modality.WINDOW_MODAL);
            stage.showingProperty().addListener((observableValue, wasShowing, isShowing) ->
                    parentStage.getScene().getRoot().setEffect(
                            Boolean.TRUE.equals(isShowing) ? parentEffect : null
                    ));
        }
        stage.initOwner(parentStage);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setScene(scene);

        sizeAndPosition(position);

        if (!onShowing()) {
            return;
        }

        if (waitForResults) {
            stage.showAndWait();
        } else {
            stage.show();
        }
    }

    private void sizeAndPosition(final WindowPosition windowPosition) {
        final ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> {
            if (windowPosition == WindowPosition.CENTER) {
                stage.setX(parentStage.getX() + parentStage.getWidth() / 2 - stage.getWidth() / 2);
                stage.setY(parentStage.getY() + parentStage.getHeight() / 2 - stage.getHeight() / 2);
            } else if (windowPosition == WindowPosition.TOP_LEFT) {
                Bounds boundsInScene = MainApplication.getInstance().getSwingNode().localToScreen(
                        MainApplication.getInstance().getSwingNode().getBoundsInLocal());
                stage.setX(boundsInScene.getMinX() - 10);
                stage.setY(boundsInScene.getMinY() - 10);
            } else if (windowPosition == WindowPosition.TOP_RIGHT) {
                Bounds boundsInScene = MainApplication.getInstance().getSwingNode().localToScreen(
                        MainApplication.getInstance().getSwingNode().getBoundsInLocal());
                stage.setX(boundsInScene.getMaxX() + 10 - stage.getWidth());
                stage.setY(boundsInScene.getMinY() - 10);
            }
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            if (stage.getHeight() > bounds.getHeight() - 100) {
                stage.setHeight(bounds.getHeight() - 100);
            }
            if (stage.getWidth() > bounds.getWidth() - 100) {
                stage.setWidth(bounds.getWidth() - 100);
            }
        };

        final ChangeListener<Number> xPropertyListener = (observable, oldValue, newValue) -> {
            double parentX = newValue.doubleValue();
            if (windowPosition == WindowPosition.CENTER) {
                stage.setX(parentX + parentStage.getWidth() / 2 - stage.getWidth() / 2);
            } else if (windowPosition == WindowPosition.TOP_LEFT) {
                Bounds boundsInScene = MainApplication.getInstance().getSwingNode().localToScreen(
                        MainApplication.getInstance().getSwingNode().getBoundsInLocal());
                stage.setX(boundsInScene.getMinX() - 10);
                stage.setY(boundsInScene.getMinY() - 10);
            } else if (windowPosition == WindowPosition.TOP_RIGHT) {
                stage.setX(parentX + parentStage.getWidth() - stage.getWidth() + 4);
            }
        };

        final ChangeListener<Number> yPropertyListener = (observable, oldValue, newValue) -> {
            double parentY = newValue.doubleValue();
            double topHeight = parentStage.getHeight() - parentStage.getScene().getHeight();
            double menuBarHeight = MainMenu.getMainMenu().getHeight();
            if (windowPosition == WindowPosition.CENTER) {
                stage.setY(parentY + parentStage.getHeight() / 2 - stage.getHeight() / 2);
            } else if (windowPosition == WindowPosition.TOP_LEFT) {
                Bounds boundsInScene = MainApplication.getInstance().getSwingNode().localToScreen(
                        MainApplication.getInstance().getSwingNode().getBoundsInLocal());
                stage.setX(boundsInScene.getMinX() - 10);
                stage.setY(boundsInScene.getMinY() - 10);
            } else if (windowPosition == WindowPosition.TOP_RIGHT) {
                stage.setY(parentY + topHeight + menuBarHeight);
            }
        };

        final ChangeListener<Number> widthPropertyListener = (observable, oldValue, newValue) -> {
            double parentWidth = newValue.doubleValue();
            if (windowPosition == WindowPosition.CENTER) {
                stage.setX(parentStage.getX() + parentWidth / 2 - stage.getWidth() / 2);
            } else if (windowPosition == WindowPosition.TOP_LEFT) {
                Bounds boundsInScene = MainApplication.getInstance().getSwingNode().localToScreen(
                        MainApplication.getInstance().getSwingNode().getBoundsInLocal());
                stage.setX(boundsInScene.getMinX() - 10);
                stage.setY(boundsInScene.getMinY() - 10);
            } else if (windowPosition == WindowPosition.TOP_RIGHT) {
                stage.setX(parentStage.getX() + parentWidth - stage.getWidth() + 4);
            }
        };

        final ChangeListener<Number> heightPropertyListener = (observable, oldValue, newValue) -> {
            double parentHeight = newValue.doubleValue();
            double topHeight = parentStage.getHeight() - parentStage.getScene().getHeight();
            double menuBarHeight = MainMenu.getMainMenu().getHeight();
            if (windowPosition == WindowPosition.CENTER) {
                stage.setY(parentStage.getY() + parentHeight / 2 - stage.getHeight() / 2);
            } else if (windowPosition == WindowPosition.TOP_LEFT) {
                Bounds boundsInScene = MainApplication.getInstance().getSwingNode().localToScreen(
                        MainApplication.getInstance().getSwingNode().getBoundsInLocal());
                stage.setX(boundsInScene.getMinX() - 10);
                stage.setY(boundsInScene.getMinY() - 10);
            } else if (windowPosition == WindowPosition.TOP_RIGHT) {
                stage.setY(parentStage.getY() + topHeight + menuBarHeight);
            }
        };

        parentStage.xProperty().addListener(xPropertyListener);
        parentStage.yProperty().addListener(yPropertyListener);
        parentStage.widthProperty().addListener(widthPropertyListener);
        parentStage.heightProperty().addListener(heightPropertyListener);

        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);
    }

    public void close() {
        if (closeRequested()) {
            stage.close();
        }
    }

    protected boolean onShowing() {
        return true;
    }

    protected boolean closeRequested() {
        return true;
    }

    protected Stage getStage() {
        return stage;
    }

    protected Stage getParentStage() {
        return parentStage;
    }

    protected Label createTitleLabel(String title) {
        return UiCommon.createTitleLabel(title);
    }

    protected HBox createCloseButtonHBox() {
        Button button = new Button("Close");
        button.setOnAction(event -> close());
        HBox hBox = new HBox(UiConstants.SPACING, button);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        return hBox;
    }

    protected abstract Pane createContentPane();
}
