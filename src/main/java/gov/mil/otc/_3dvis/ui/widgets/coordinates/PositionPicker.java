package gov.mil.otc._3dvis.ui.widgets.coordinates;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.nasa.worldwind.geom.Position;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PositionPicker extends TransparentWindow {

    public static synchronized Position show(Stage parentStage) {
        return show(parentStage, null);
    }

    public static synchronized Position show(Stage parentStage, Position initialPosition) {
        PositionPicker positionPicker = new PositionPicker(parentStage, initialPosition);
        positionPicker.createAndShow(true, false, WindowPosition.TOP_LEFT);
        return positionPicker.selectedPosition;
    }

    private final MultiCoordinateController multiCoordinateController;
    private Position selectedPosition = null;

    protected PositionPicker(Stage parentStage, Position initialPosition) {
        super(parentStage);
        selectedPosition = initialPosition;
        multiCoordinateController = new MultiCoordinateController(selectedPosition);
    }

    @Override
    protected Pane createContentPane() {
        VBox vBox = new VBox(UiConstants.SPACING);
        vBox.setPadding(new Insets(UiConstants.SPACING));

        Button okButton = new Button("OK");
        okButton.setOnAction(event -> {
            selectedPosition = multiCoordinateController.getPosition();
            close();
        });

        Button cancelButton = new Button("Close");
        cancelButton.setOnAction(event -> {
            selectedPosition = null;
            close();
        });

        HBox hBox = new HBox(UiConstants.SPACING, okButton, cancelButton);
        hBox.setAlignment(Pos.BASELINE_RIGHT);

        vBox.getChildren().addAll(createTitleLabel("Select Location"), multiCoordinateController, hBox);

        return vBox;
    }
}
