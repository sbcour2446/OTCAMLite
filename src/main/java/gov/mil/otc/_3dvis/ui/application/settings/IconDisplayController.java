package gov.mil.otc._3dvis.ui.application.settings;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.settings.IconDisplay;
import gov.mil.otc._3dvis.settings.IconType;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.StageUtility;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.EnumMap;
import java.util.Map;

public class IconDisplayController {

    private static IconDisplayController iconDisplayController = null;
    private final Stage stage = new Stage();
    private final Spinner<Integer> opacitySpinner;
    private final Map<IconType, Spinner<Integer>> spinnerMap = new EnumMap<>(IconType.class);
    private final CheckBox showLineToTerrainCheckBox = new CheckBox("Show line to terrain");

    public static synchronized void show() {
        if (iconDisplayController == null) {
            iconDisplayController = new IconDisplayController();
        }
        iconDisplayController.initialize();
        iconDisplayController.stage.show();
    }

    private IconDisplayController() {
        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(false);
        stage.setTitle("Icon Display Settings");

        IconDisplay iconDisplay = SettingsManager.getSettings().getIconDisplay();
        GridPane iconSizeGridPane = new GridPane();
        iconSizeGridPane.setHgap(10);
        iconSizeGridPane.setVgap(10);
        int rowIndex = 0;
        for (IconType iconType : IconType.values()) {
            Spinner<Integer> spinner = new Spinner<>(iconType.getMinimumSize(), iconType.getMaximumSize(), iconDisplay.getIconSize(iconType));
            spinnerMap.put(iconType, spinner);
            setupSpinner(spinner);
            iconSizeGridPane.add(new Label(iconType.name()), 0, rowIndex);
            iconSizeGridPane.add(spinner, 1, rowIndex);
            rowIndex++;
        }

        opacitySpinner = new Spinner<>(0, 100, iconDisplay.getIconOpacity());
        setupSpinner(opacitySpinner);

        GridPane iconOpacityGridPane = new GridPane();
        iconOpacityGridPane.setHgap(10);
        iconOpacityGridPane.setVgap(10);
        iconOpacityGridPane.add(new Label("Opacity"), 0, 0);
        iconOpacityGridPane.add(opacitySpinner, 1, 0);

        showLineToTerrainCheckBox.setSelected(SettingsManager.getPreferences().isLoadPlaybackOnStartup());
        VBox otherVBox = new VBox(UiConstants.SPACING, showLineToTerrainCheckBox);

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(event -> onApplyAction());
        Button defaultsButton = new Button("Defaults");
        defaultsButton.setOnAction(event -> onDefaultsAction());

        VBox vBox = new VBox(10,
                new TitledPane("Icon Size", iconSizeGridPane),
                new TitledPane("Icon Opacity", iconOpacityGridPane),
                new TitledPane("Other", otherVBox),
                new HBox(10, applyButton, defaultsButton));
        vBox.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageUtility.centerStage(stage, MainApplication.getInstance().getStage());
    }

    private void initialize() {
        IconDisplay iconDisplay = SettingsManager.getSettings().getIconDisplay();
        for (Map.Entry<IconType, Spinner<Integer>> entry : spinnerMap.entrySet()) {
            entry.getValue().getValueFactory().setValue(iconDisplay.getIconSize(entry.getKey()));
        }
        opacitySpinner.getValueFactory().setValue(iconDisplay.getIconOpacity());
    }

    private void setupSpinner(Spinner<Integer> spinner) {
        spinner.setEditable(true);
        TextFormatter<Integer> formatter = new TextFormatter<>(spinner.getValueFactory().getConverter(), spinner.getValueFactory().getValue());
        spinner.getEditor().setTextFormatter(formatter);
        spinner.getValueFactory().valueProperty().bindBidirectional(formatter.valueProperty());
    }

    private void onApplyAction() {
        for (Map.Entry<IconType, Spinner<Integer>> entry : spinnerMap.entrySet()) {
            SettingsManager.getSettings().getIconDisplay().setIconSize(entry.getKey(), entry.getValue().getValue());
        }
        SettingsManager.getSettings().getIconDisplay().setIconOpacity(opacitySpinner.getValue());
        SettingsManager.getSettings().getIconDisplay().setShowLineToTerrain(showLineToTerrainCheckBox.isSelected());
        EntityManager.updateDisplay();
    }

    private void onDefaultsAction() {
        for (Map.Entry<IconType, Spinner<Integer>> entry : spinnerMap.entrySet()) {
            entry.getValue().getValueFactory().setValue(entry.getKey().getDefaultSize());
        }
        opacitySpinner.getValueFactory().setValue(Defaults.ICON_OPACITY);
    }
}
