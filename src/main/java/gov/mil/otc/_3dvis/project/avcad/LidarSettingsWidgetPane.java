package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LidarSettingsWidgetPane implements IWidgetPane {

    private static final Map<EntityId, LidarSettingsWidgetPane> instances = new HashMap<>();

    public static void show(LidarEntity lidarEntity) {
        LidarSettingsWidgetPane lidarSettingsWidgetPane = instances.get(lidarEntity.getEntityId());
        if (lidarSettingsWidgetPane == null) {
            lidarSettingsWidgetPane = new LidarSettingsWidgetPane(lidarEntity);
            instances.put(lidarEntity.getEntityId(), lidarSettingsWidgetPane);
            WidgetPaneContainer.addWidgetPane(lidarSettingsWidgetPane);
        } else {
            WidgetPaneContainer.showWidgetPane(lidarSettingsWidgetPane);
        }
    }

    private final LidarEntity lidarEntity;
    private final VBox pane = new VBox();
    private final Slider opacitySlider = new Slider(0.0, 1.0, .1);
    private final CheckBox useSystemHeight = new CheckBox("Use system height");
    private final TextField heightTextField = new TextField();

    private LidarSettingsWidgetPane(LidarEntity lidarEntity) {
        this.lidarEntity = lidarEntity;
        initialize();
    }

    private void initialize() {
        opacitySlider.setValue(lidarEntity.getOpacity());
        opacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            lidarEntity.setOpacity(newValue.doubleValue());
        });
        TitledPane titledPane = new TitledPane("Opacity", opacitySlider);
        titledPane.setPadding(new Insets(UiConstants.SPACING));
        titledPane.setCollapsible(false);
        pane.getChildren().add(titledPane);

        useSystemHeight.setSelected(!lidarEntity.isOverrideHeight());
        heightTextField.setText(String.valueOf(lidarEntity.getOverrideHeight()));
        Button applyButton = new Button("Apply");
        applyButton.setOnAction(event -> setHeight());
        VBox vBox = new VBox(UiConstants.SPACING,
                useSystemHeight,
                new HBox(UiConstants.SPACING, heightTextField, new Label("meters")),
                applyButton);
        titledPane = new TitledPane("Cloud Height", vBox);
        titledPane.setPadding(new Insets(UiConstants.SPACING));
        titledPane.setCollapsible(false);
        pane.getChildren().add(titledPane);

        pane.setSpacing(UiConstants.SPACING);
    }

    public void setHeight() {
        try {
            double height = Double.parseDouble(heightTextField.getText());
            lidarEntity.setOverrideHeight(!useSystemHeight.isSelected(), height);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "LidarKmlFileImportObject::doImport", e);
            DialogUtilities.showErrorDialog("Lidar Height", "Incorrect value",
                    MainApplication.getInstance().getStage());
        }
    }

    @Override
    public String getName() {
        return "Lidar Settings";
    }

    @Override
    public Pane getPane() {
        return pane;
    }

    @Override
    public void dispose() {
        instances.remove(lidarEntity.getEntityId());
    }
}
