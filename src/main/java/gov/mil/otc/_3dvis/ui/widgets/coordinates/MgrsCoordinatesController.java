package gov.mil.otc._3dvis.ui.widgets.coordinates;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerValidationListener;
import gov.mil.otc._3dvis.utility.MgrsComponents;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import javafx.collections.FXCollections;
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MgrsCoordinatesController extends Pane implements ICoordinatesController {

    private final Spinner<Integer> zoneColumnSpinner = new Spinner<>(1, 60, 1);
    private final ComboBox<Character> zoneRowComboBox = new ComboBox<>();
    private final ComboBox<Character> squareColumnComboBox = new ComboBox<>();
    private final ComboBox<Character> squareRowComboBox = new ComboBox<>();
    private final TextField northingTextField = new TextField();
    private final TextField eastingTextField = new TextField();
    private final TextField altitudeTextField = new TextField();

    public MgrsCoordinatesController(Position position) {
        initialize();
        setPosition(position);
    }

    private void initialize() {
        zoneColumnSpinner.setMaxWidth(Double.MAX_VALUE);

        List<Character> zoneRowValues = new ArrayList<>();
        for (char a : MgrsComponents.ZONE_ROW_DESIGNATORS) {
            zoneRowValues.add(a);
        }
        zoneRowComboBox.itemsProperty().setValue(FXCollections.observableList(zoneRowValues));
        zoneRowComboBox.setMaxWidth(Double.MAX_VALUE);

        List<Character> sqRowColumnValues = new ArrayList<>();
        for (char a : MgrsComponents.SQUARE_ROW_COLUMN_DESIGNATORS) {
            sqRowColumnValues.add(a);
        }
        squareColumnComboBox.itemsProperty().setValue(FXCollections.observableList(sqRowColumnValues));
        squareColumnComboBox.setValue(MgrsComponents.SQUARE_ROW_COLUMN_DESIGNATORS[0]);
        squareColumnComboBox.setMaxWidth(Double.MAX_VALUE);

        squareRowComboBox.itemsProperty().setValue(FXCollections.observableList(sqRowColumnValues));
        squareRowComboBox.setValue(MgrsComponents.SQUARE_ROW_COLUMN_DESIGNATORS[0]);
        squareRowComboBox.setMaxWidth(Double.MAX_VALUE);

        eastingTextField.textProperty().addListener(new IntegerValidationListener(eastingTextField, 0, 99999));
        northingTextField.textProperty().addListener(new IntegerValidationListener(northingTextField, 0, 99999));

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

        gridPane.add(new Label("Zone Row"), 0, rowIndex);
        gridPane.add(zoneRowComboBox, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Square Column"), 0, rowIndex);
        gridPane.add(squareColumnComboBox, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Square Row"), 0, rowIndex);
        gridPane.add(squareRowComboBox, 1, rowIndex);

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
            Logger.getGlobal().log(Level.FINEST, "MgrsCoordinatesController::getPosition", e);
        }
        try {
            String mgrsString = String.valueOf(zoneColumnSpinner.getValue()) +
                    zoneRowComboBox.getValue() +
                    squareColumnComboBox.getValue() +
                    squareRowComboBox.getValue() +
                    String.format("%05d", Integer.parseInt(eastingTextField.getText())) +
                    String.format("%05d", Integer.parseInt(northingTextField.getText()));
            MGRSCoord mgrsCoord = MGRSCoord.fromString(mgrsString, WWController.getGlobe());
            return new Position(mgrsCoord.getLatitude(), mgrsCoord.getLongitude(), altitude);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.FINEST, "MgrsCoordinatesController::getPosition", e);
        }
        return null;
    }

    @Override
    public void setPosition(Position position) {
        if (position == null) {
            clear();
        } else {
            MGRSCoord mgrsCoord = MGRSCoord.fromLatLon(position.getLatitude(), position.getLongitude());
            MgrsComponents mgrsComponents = MgrsComponents.getMgrsComponents(mgrsCoord.toString());
            if (mgrsComponents == null) {
                clear();
            } else {
                zoneColumnSpinner.getValueFactory().setValue(mgrsComponents.getZone());
                zoneRowComboBox.setValue(mgrsComponents.getLatitudeBand());
                squareColumnComboBox.setValue(mgrsComponents.getSquareLetter1());
                squareRowComboBox.setValue(mgrsComponents.getSquareLetter2());
                northingTextField.setText(String.valueOf(mgrsComponents.getNorthing()));
                eastingTextField.setText(String.valueOf(mgrsComponents.getEasting()));
                altitudeTextField.setText(String.valueOf(position.getAltitude()));
            }
        }
    }
}
