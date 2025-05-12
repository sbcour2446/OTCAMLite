package gov.mil.otc._3dvis.ui.tools;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.UtilityPane;
import gov.mil.otc._3dvis.ui.widgets.coordinates.MultiCoordinateController;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class GoToLocationController extends UtilityPane {

    public static synchronized void show() {
        new GoToLocationController().createAndShow(Pos.TOP_LEFT);
    }

    private final MultiCoordinateController multiCoordinateController = new MultiCoordinateController();

    @Override
    protected String getTitle() {
        return "GoTo Location";
    }

    @Override
    protected Pane createContentPane() {
        VBox vBox = new VBox(UiConstants.SPACING);
        vBox.setPadding(new Insets(UiConstants.SPACING));

        Button gotoButton = new Button("Go To");
        gotoButton.setOnAction(actionEvent -> goToLocation());

        Button cancelButton = new Button("Close");
        cancelButton.setOnAction(actionEvent -> close());

        HBox hBox = new HBox(UiConstants.SPACING, gotoButton, cancelButton);
        hBox.setAlignment(Pos.BASELINE_RIGHT);

        vBox.getChildren().addAll(multiCoordinateController, hBox);

        return vBox;
    }

    private void goToLocation() {
        Position position = multiCoordinateController.getPosition();
        if (position != null) {
            View view = WWController.getView();
            if (view != null) {
                double distance = view.getCenterPoint().distanceTo3(view.getEyePoint());
                view.goTo(new Position(position.latitude, position.longitude, 0), distance);
            }
        }
    }
}
