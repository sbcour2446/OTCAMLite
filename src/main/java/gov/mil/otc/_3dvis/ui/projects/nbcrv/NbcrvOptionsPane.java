package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvEntity;
import gov.mil.otc._3dvis.project.nbcrv.WdlEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.SwingUtility;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvOptionsPane {

    private final Pane pane;

    public NbcrvOptionsPane() {
        pane = new HBox(new TabPane(
                new Tab("Rad/Nuc", createRadNucPane()),
                new Tab("WDL", createWdlPane()),
                new Tab("Color", createColorPane()),
                new Tab("Height", createHeightPane()),
                new Tab("Timeout", createTimeoutPane())
        ));
        pane.setPadding(new Insets(UiConstants.SPACING));
    }

    public Pane getPane() {
        return pane;
    }

    private TitledPane createRadNucPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        createRadNucRow(gridPane, rowIndex++, "Critical");
        createRadNucRow(gridPane, rowIndex++, "Marginal");
        createRadNucRow(gridPane, rowIndex, "Negligible");

        TitledPane titledPane = new TitledPane("Rad/Nuc Options", gridPane);
        titledPane.setCollapsible(false);

        return titledPane;
    }

    private void createRadNucRow(GridPane gridPane, int rowIndex, String thresholdName) {
        final Spinner<Double> spinner = new Spinner<>(0.0, Double.MAX_VALUE,
                SettingsManager.getSettings().getNbcrvSettings().getRadNucThreshold(thresholdName), .1);
        spinner.setMaxWidth(Double.MAX_VALUE);

        Button setButton = new Button("Set");
        setButton.setOnAction(event -> {
            try {
                SettingsManager.getSettings().getNbcrvSettings().setRadNucThreshold(thresholdName, spinner.getValue());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "NbcrvOptionsPane::createRadNucRow", e);
            }
        });

        gridPane.add(new TextWithStyleClass(thresholdName + ":"), 0, rowIndex);
        gridPane.add(spinner, 1, rowIndex);
        gridPane.add(setButton, 2, rowIndex);
    }

    private TitledPane createWdlPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        final Spinner<Integer> medianFilterWindowSpinner = new Spinner<>(0, 10000,
                SettingsManager.getSettings().getNbcrvSettings().getLidarMedianFilterWindowSize());
        final Spinner<Integer> clippedStartSpinner = new Spinner<>(0, 10000,
                SettingsManager.getSettings().getNbcrvSettings().getLidarClippedStartSize());
        final Spinner<Integer> maxConcentrationSpinner = new Spinner<>(0, 10000,
                SettingsManager.getSettings().getNbcrvSettings().getLidarMaxConcentration());
        final Spinner<Integer> concentrationThresholdSpinner = new Spinner<>(0, 10000,
                SettingsManager.getSettings().getNbcrvSettings().getLidarConcentrationThreshold());
        final Spinner<Integer> maxRangeSpinner = new Spinner<>(0, 10000,
                SettingsManager.getSettings().getNbcrvSettings().getLidarMaxRange());
        final Spinner<Double> distanceFactorSpinner = new Spinner<>(0.1, 2.0,
                SettingsManager.getSettings().getNbcrvSettings().getLidarDistanceFactor(), .1);
        final Spinner<Double> cloudLiineWidthSpinner = new Spinner<>(0.10, 1.00,
                SettingsManager.getSettings().getNbcrvSettings().getLidarCloudLineWidth(), .01);

        setupIntegerSpinner(medianFilterWindowSpinner);
        setupIntegerSpinner(clippedStartSpinner);
        setupIntegerSpinner(maxConcentrationSpinner);
        setupIntegerSpinner(concentrationThresholdSpinner);
        setupIntegerSpinner(maxRangeSpinner);
        setupDoubleSpinner(distanceFactorSpinner);
        setupDoubleSpinner(cloudLiineWidthSpinner);

        gridPane.add(new TextWithStyleClass("Median Filter Window Size:"), 0, rowIndex);
        gridPane.add(medianFilterWindowSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Clipped Start Size:"), 0, rowIndex);
        gridPane.add(clippedStartSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Max Concentration:"), 0, rowIndex);
        gridPane.add(maxConcentrationSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Concentration Threshold:"), 0, rowIndex);
        gridPane.add(concentrationThresholdSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Max Range:"), 0, rowIndex);
        gridPane.add(maxRangeSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Distance Factor:"), 0, rowIndex);
        gridPane.add(distanceFactorSpinner, 1, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Cloud Line Width (degrees):"), 0, rowIndex);
        gridPane.add(cloudLiineWidthSpinner, 1, rowIndex);

        rowIndex++;

        Button button = new Button("Set");
        button.setOnAction(event -> {
            SettingsManager.getSettings().getNbcrvSettings().setLidarMedianFilterWindowSize(medianFilterWindowSpinner.getValue());
            SettingsManager.getSettings().getNbcrvSettings().setLidarClippedStartSize(clippedStartSpinner.getValue());
            SettingsManager.getSettings().getNbcrvSettings().setLidarMaxConcentration(maxConcentrationSpinner.getValue());
            SettingsManager.getSettings().getNbcrvSettings().setLidarConcentrationThreshold(concentrationThresholdSpinner.getValue());
            SettingsManager.getSettings().getNbcrvSettings().setLidarMaxRange(maxRangeSpinner.getValue());
            SettingsManager.getSettings().getNbcrvSettings().setLidarDistanceFactor(distanceFactorSpinner.getValue());
            SettingsManager.getSettings().getNbcrvSettings().setLidarCloudLineWidth(cloudLiineWidthSpinner.getValue());
            notifyWdlUpdate();
        });

        gridPane.add(button, 1, rowIndex);

        TitledPane titledPane = new TitledPane("WDL Options", gridPane);
        titledPane.setCollapsible(false);

        return titledPane;
    }

    private void setupIntegerSpinner(Spinner<Integer> spinner) {
        spinner.setEditable(true);
        TextFormatter<Integer> formatter = new TextFormatter<>(spinner.getValueFactory().getConverter(), spinner.getValueFactory().getValue());
        spinner.getEditor().setTextFormatter(formatter);
        spinner.getValueFactory().valueProperty().bindBidirectional(formatter.valueProperty());
    }

    private void setupDoubleSpinner(Spinner<Double> spinner) {
        spinner.setEditable(true);
        TextFormatter<Double> formatter = new TextFormatter<>(spinner.getValueFactory().getConverter(), spinner.getValueFactory().getValue());
        spinner.getEditor().setTextFormatter(formatter);
        spinner.getValueFactory().valueProperty().bindBidirectional(formatter.valueProperty());
    }

    private void notifyWdlUpdate() {
        final ProgressDialog progressDialog = new ProgressDialog(MainApplication.getInstance().getStage());
        new Thread(() -> {
            for (IEntity entity : EntityManager.getEntities()) {
                if (entity instanceof WdlEntity) {
                    ((WdlEntity) entity).recalculateReadings();
                }
            }
            Platform.runLater(progressDialog::close);
        }, "NbcrvOptionsPane::notifyWdlUpdate").start();
        progressDialog.createAndShow();
    }

    private TitledPane createColorPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        createColorRow(gridPane, rowIndex++, "cSDS");
        createColorRow(gridPane, rowIndex++, "iMCAD");
        createColorRow(gridPane, rowIndex, "Other");

        TitledPane titledPane = new TitledPane("Color Options", gridPane);
        titledPane.setCollapsible(false);

        return titledPane;
    }

    private void createColorRow(GridPane gridPane, int rowIndex, String deviceName) {
        final ColorPicker colorPicker = new ColorPicker();
        colorPicker.setMaxWidth(Double.MAX_VALUE);
        colorPicker.setValue(SwingUtility.toFxColor(SettingsManager.getSettings().getNbcrvSettings().getDeviceColor(deviceName)));
        colorPicker.setOnAction(actionEvent -> updateDeviceColor(deviceName, colorPicker.getValue()));

        gridPane.add(new TextWithStyleClass(deviceName + ":"), 0, rowIndex);
        gridPane.add(colorPicker, 1, rowIndex);
    }

    private void updateDeviceColor(String deviceName, Color color) {
        java.awt.Color awtColor = SwingUtility.toAwtColor(color);
        SettingsManager.getSettings().getNbcrvSettings().setDeviceColor(deviceName, awtColor);
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof NbcrvEntity) {
                Device device = ((NbcrvEntity) entity).getDevice(deviceName);
                if (device != null) {
                    device.updateColor(awtColor);
                }
            }
        }
    }

    private TitledPane createHeightPane() {
        CheckBox showPitchRollCheckBox = new CheckBox("Show Pitch/Roll");
        showPitchRollCheckBox.setSelected(SettingsManager.getSettings().getNbcrvSettings().isUsePitch());
        showPitchRollCheckBox.setOnAction(event ->
                SettingsManager.getSettings().getNbcrvSettings().setUsePitch(showPitchRollCheckBox.isSelected()));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        createHeightRow(gridPane, rowIndex++, "cSDS");
        createHeightRow(gridPane, rowIndex++, "iMCAD");
        createHeightRow(gridPane, rowIndex, "Other");

        TitledPane titledPane = new TitledPane("Height and Elevation Options",
                new VBox(UiConstants.SPACING, showPitchRollCheckBox, gridPane));
        titledPane.setCollapsible(false);

        return titledPane;
    }

    private void createHeightRow(GridPane gridPane, int rowIndex, String deviceName) {
        String text = String.format("%4.2f", SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(deviceName));
        final TextField textField = new TextField(text);
        Button setButton = new Button("Set");
        setButton.setOnAction(event -> {
            try {
                double height = Double.parseDouble(textField.getText());
                SettingsManager.getSettings().getNbcrvSettings().setDeviceHeight(deviceName, height);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "NbcrvOptionsPane::createHeightRow", e);
            }

        });

        gridPane.add(new TextWithStyleClass(deviceName + ":"), 0, rowIndex);
        gridPane.add(textField, 1, rowIndex);
        gridPane.add(setButton, 2, rowIndex);
    }

    private TitledPane createTimeoutPane() {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        createTimeoutRow(gridPane, rowIndex++, "cSDS");
        createTimeoutRow(gridPane, rowIndex++, "iMCAD");
        createTimeoutRow(gridPane, rowIndex, "Other");

        TitledPane titledPane = new TitledPane("Timeout Options", gridPane);
        titledPane.setCollapsible(false);

        return titledPane;
    }

    private void createTimeoutRow(GridPane gridPane, int rowIndex, String deviceName) {
        final Spinner<Integer> spinner = new Spinner<>(0, Integer.MAX_VALUE,
                SettingsManager.getSettings().getNbcrvSettings().getDeviceTimeout(deviceName) / 1000);
        spinner.setMaxWidth(Double.MAX_VALUE);

        Button setButton = new Button("Set");
        setButton.setOnAction(event -> {
            try {
                int timeout = spinner.getValue();
                SettingsManager.getSettings().getNbcrvSettings().setDeviceTimeout(deviceName, timeout * 1000);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "NbcrvOptionsPane::createTimeoutRow", e);
            }
        });

        gridPane.add(new TextWithStyleClass(deviceName + ":"), 0, rowIndex);
        gridPane.add(spinner, 1, rowIndex);
        gridPane.add(setButton, 2, rowIndex);
    }
}

