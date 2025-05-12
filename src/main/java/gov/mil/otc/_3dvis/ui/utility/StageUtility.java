package gov.mil.otc._3dvis.ui.utility;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class StageUtility {

    public static void centerStage(final Stage stage, final Stage parentStage) {
        final ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
            double stageWidth = newValue.doubleValue();
            stage.setX(parentStage.getX() + parentStage.getWidth() / 2 - stageWidth / 2);
        };
        final ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
            double stageHeight = newValue.doubleValue();
            stage.setY(parentStage.getY() + parentStage.getHeight() / 2 - stageHeight / 2);
        };

        stage.widthProperty().addListener(widthListener);
        stage.heightProperty().addListener(heightListener);

        //Once the window is visible, remove the listeners
        stage.setOnShown(e -> {
            stage.widthProperty().removeListener(widthListener);
            stage.heightProperty().removeListener(heightListener);
        });
    }

    public static void centerStage(final Stage stage) {
        final ChangeListener<Number> widthListener = (observable, oldValue, newValue) -> {
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((primScreenBounds.getWidth() - newValue.doubleValue()) / 2);
        };
        final ChangeListener<Number> heightListener = (observable, oldValue, newValue) -> {
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            stage.setY((primScreenBounds.getHeight() - newValue.doubleValue()) / 2);
        };

        stage.widthProperty().addListener(widthListener);
        stage.heightProperty().addListener(heightListener);

        //Once the window is visible, remove the listeners
        stage.setOnShown(e -> {
            stage.widthProperty().removeListener(widthListener);
            stage.heightProperty().removeListener(heightListener);
        });
    }

    private StageUtility() {
    }
}
