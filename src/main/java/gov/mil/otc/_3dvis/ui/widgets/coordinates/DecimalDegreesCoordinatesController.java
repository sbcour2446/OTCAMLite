package gov.mil.otc._3dvis.ui.widgets.coordinates;

import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.validation.DoubleValidationListener;
import gov.nasa.worldwind.geom.Position;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DecimalDegreesCoordinatesController extends Pane implements ICoordinatesController {

    private final TextField latitudeTextField = new TextField();
    private final TextField longitudeTextField = new TextField();
    private final TextField altitudeTextField = new TextField();

    public DecimalDegreesCoordinatesController(Position position) {
        initialize();
        setPosition(position);
    }

    private void initialize() {
        latitudeTextField.textProperty().addListener(new DoubleValidationListener(latitudeTextField, -90, 90));
        longitudeTextField.textProperty().addListener(new DoubleValidationListener(longitudeTextField, -180, 180));
        altitudeTextField.textProperty().addListener(new DoubleValidationListener(altitudeTextField, Double.MIN_VALUE, Double.MAX_VALUE));

        GridPane gridPane = new GridPane();
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        gridPane.add(new Label("Latitude (\u00b0)"), 0, rowIndex);
        gridPane.add(latitudeTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Longitude (\u00b0)"), 0, rowIndex);
        gridPane.add(longitudeTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Altitude (m)"), 0, rowIndex);
        gridPane.add(altitudeTextField, 1, rowIndex);

        getChildren().add(gridPane);
    }

    @Override
    public void clear() {
        latitudeTextField.setText("");
        longitudeTextField.setText("");
        altitudeTextField.setText("");
    }

    @Override
    public Position getPosition() {
        try {
            double latitude = Double.parseDouble(latitudeTextField.getText());
            double longitude = Double.parseDouble(longitudeTextField.getText());
            double altitude = 0;
            if (altitudeTextField.getText() != null && !altitudeTextField.getText().isEmpty()) {
                altitude = Double.parseDouble(altitudeTextField.getText());
            }
            return Position.fromDegrees(latitude, longitude, altitude);
        } catch (NumberFormatException e) {
            Logger.getGlobal().log(Level.FINEST, "DecimalDegreesCoordinatesController::getPosition", e);
        }
        return null;
    }

    @Override
    public void setPosition(Position position) {
        if (position == null) {
            clear();
        } else {
            latitudeTextField.setText(String.valueOf(position.getLatitude().getDegrees()));
            longitudeTextField.setText(String.valueOf(position.getLongitude().getDegrees()));
            altitudeTextField.setText(String.valueOf(position.getAltitude()));
        }
    }
}
