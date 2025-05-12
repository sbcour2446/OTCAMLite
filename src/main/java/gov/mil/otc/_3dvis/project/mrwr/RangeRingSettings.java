package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.utility.SwingUtility;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RangeRingSettings extends TransparentWindow {

    public static int show(ThreatEntity threatEntity) {
        RangeRingSettings rangeRingSettings = new RangeRingSettings(threatEntity);
        rangeRingSettings.createAndShow(true);
        return rangeRingSettings.value;
    }

    private final ThreatEntity threatEntity;
    private int value = 1000;
    private final TextField rangeTextField;
    private final ColorPicker colorPicker;

    private RangeRingSettings(ThreatEntity threatEntity) {
        this.threatEntity = threatEntity;
        rangeTextField = new TextField(String.valueOf(threatEntity.getRange()));
        colorPicker = new ColorPicker(SwingUtility.toFxColor(threatEntity.getRangeColor()));
    }

    @Override
    protected Pane createContentPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        gridPane.add(new TextWithStyleClass("Range:"), 0, rowIndex);
        gridPane.add(rangeTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Color:"), 0, rowIndex);
        gridPane.add(colorPicker, 1, rowIndex);

        Button button = new Button("Set");
        button.setOnAction(event -> onSetButton());
        return new VBox(UiConstants.SPACING, gridPane, button);
    }

    private void onSetButton() {
        int newValue;
        try {
            newValue = Integer.parseInt(rangeTextField.getText());
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "RangeRingSettings::createContentPane: invalid value");
            return;
        }
        if (newValue > 0 && newValue < ThreatEntity.MAX_RANGE) {
            value = newValue;
        } else {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                    "Enter a valid value between 0 and " + ThreatEntity.MAX_RANGE + ".", getStage());
            rangeTextField.setText(String.valueOf(threatEntity.getRange()));
            return;
        }

        threatEntity.setRange(value);

        Color newColor = SwingUtility.toAwtColor(colorPicker.getValue());
        threatEntity.setRangeColor(newColor);

        close();
    }
}
