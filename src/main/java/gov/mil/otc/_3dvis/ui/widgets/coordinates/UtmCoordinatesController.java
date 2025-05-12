package gov.mil.otc._3dvis.ui.widgets.coordinates;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.validation.DoubleValidationListener;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UtmCoordinatesController extends Pane implements ICoordinatesController {

    private final Spinner<Integer> zoneColumnSpinner = new Spinner<>(1, 60, 1);
    private final ComboBox<Character> zoneRowComboBox = new ComboBox<>();
    private final TextField eastingTextField = new TextField();
    private final TextField northingTextField = new TextField();
    private final TextField altitudeTextField = new TextField();

    public UtmCoordinatesController(Position position) {
        initialize();
        setPosition(position);
    }

    private void initialize() {
        zoneColumnSpinner.setMaxWidth(Double.MAX_VALUE);

        zoneRowComboBox.getItems().add('N');
        zoneRowComboBox.getItems().add('S');
        zoneRowComboBox.setMaxWidth(Double.MAX_VALUE);

        eastingTextField.textProperty().addListener(new DoubleValidationListener(eastingTextField, 166000, 8340000));
        northingTextField.textProperty().addListener(new DoubleValidationListener(northingTextField, 0, 10000000));

        GridPane gridPane = new GridPane();
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);
        gridPane.getColumnConstraints().get(2).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(3).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        gridPane.add(new Label("Zone Column"), 0, rowIndex);
        gridPane.add(zoneColumnSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Hemisphere"), 0, rowIndex);
        gridPane.add(zoneRowComboBox, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Easting"), 0, rowIndex);
        gridPane.add(eastingTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Northing"), 0, rowIndex);
        gridPane.add(northingTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Altitude"), 0, rowIndex);
        gridPane.add(altitudeTextField, 1, rowIndex);

        getChildren().add(gridPane);
    }

    @Override
    public void clear() {
        altitudeTextField.setText("");
    }

    @Override
    public Position getPosition() {
        double altitude = 0;
        try {
            altitude = Double.parseDouble(altitudeTextField.getText());
        } catch (Exception e) {
            Logger.getGlobal().log(Level.FINEST, "UtmCoordinatesController::getPosition", e);
        }
        try {
            String hemisphere = zoneRowComboBox.getValue() == 'N' ? AVKey.NORTH : AVKey.SOUTH;
            double easting = Double.parseDouble(eastingTextField.getText());
            double northing = Double.parseDouble(northingTextField.getText());
            UTMCoord utmCoord = UTMCoord.fromUTM(zoneColumnSpinner.getValue(), hemisphere, easting, northing, WWController.getGlobe());
            return new Position(utmCoord.getLatitude(), utmCoord.getLongitude(), altitude);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.FINEST, "UtmCoordinatesController::getPosition", e);
        }
        return null;
    }

    @Override
    public void setPosition(Position position) {
        if (position == null) {
            clear();
        } else {
            try {
                UTMCoord utmCoord = UTMCoord.fromLatLon(position.getLatitude(), position.getLongitude());
                zoneColumnSpinner.getValueFactory().setValue(utmCoord.getZone());
                zoneRowComboBox.setValue(Objects.equals(utmCoord.getHemisphere(), AVKey.NORTH) ? 'N' : 'S');
                northingTextField.setText(String.valueOf(utmCoord.getNorthing()));
                eastingTextField.setText(String.valueOf(utmCoord.getEasting()));
                altitudeTextField.setText(String.valueOf(position.getAltitude()));
            } catch (Exception e) {
                Logger.getGlobal().log(Level.FINEST, "UtmCoordinatesController::setPosition", e);
                clear();
            }
        }
    }
}
