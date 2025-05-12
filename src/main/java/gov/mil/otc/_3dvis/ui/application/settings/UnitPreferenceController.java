package gov.mil.otc._3dvis.ui.application.settings;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.settings.UnitPreference;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class UnitPreferenceController extends TransparentWindow {

    public static void show() {
        new UnitPreferenceController().createAndShow();
    }

    @Override
    protected Pane createContentPane() {
        HBox positionUnitsHBox = new HBox(UiConstants.SPACING);
        ToggleGroup toggleGroup = new ToggleGroup();
        UnitPreference.PositionUnit currentPositionUnit = SettingsManager.getPreferences().getUnitPreference().getPositionUnit();
        for (UnitPreference.PositionUnit positionUnit : UnitPreference.PositionUnit.values()) {
            RadioButton radioButton = new RadioButton(positionUnit.getDescription());
            if (positionUnit.equals(currentPositionUnit)) {
                radioButton.setSelected(true);
            }
            radioButton.setOnAction(event -> {
                SettingsManager.getPreferences().getUnitPreference().setPositionUnit(positionUnit);
                EntityManager.refreshStatus();
            });
            positionUnitsHBox.getChildren().add(radioButton);
            toggleGroup.getToggles().add(radioButton);
        }

        TitledPane titledPane = new TitledPane("Position Units", positionUnitsHBox);
        titledPane.setCollapsible(false);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());
        HBox buttonHBox = new HBox(UiConstants.SPACING, closeButton);
        buttonHBox.setAlignment(Pos.CENTER_RIGHT);

        return new VBox(UiConstants.SPACING, titledPane, buttonHBox);
    }
}
