package gov.mil.otc._3dvis.ui.data.report;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.report.Report;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.DateTimePicker2;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportGeneratorController extends TransparentWindow {

    private static final String SETTINGS_NAME = "reportgenerator";
    private static final int FIELD_MIN_WIDTH = 500;
    private final TextField outputTextField = new TextField("");
    private final TextField nameTextField = new TextField("");
    private final CheckBox filterDatesCheckBox = new CheckBox("Filter Dates");
    private final DateTimePicker2 startDateTimePicker = new DateTimePicker2(System.currentTimeMillis(),
            DateTimePicker2.ShowFields.YEAR_MONTH_DAY_HOUR_MINUTE);
    private final DateTimePicker2 stopDateTimePicker = new DateTimePicker2(System.currentTimeMillis(),
            DateTimePicker2.ShowFields.YEAR_MONTH_DAY_HOUR_MINUTE);
    private final Map<CheckBox, Report> reportMap = new HashMap<>();
    private ProgressDialog progressDialog;

    public static void show() {
        new ReportGeneratorController().createAndShow();
    }

    private ReportGeneratorController() {
    }

    @Override
    protected Pane createContentPane() {
        TextWithStyleClass titleLabel = new TextWithStyleClass("Report Generator");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));

        outputTextField.setText(SettingsManager.getPreferences().getLastDirectory(SETTINGS_NAME).getAbsolutePath());
        outputTextField.setEditable(false);
        outputTextField.setMinWidth(FIELD_MIN_WIDTH);
        Button outputButton = new Button("...");
        outputButton.setOnAction(event -> onSelectFileAction());

        GridPane gridPane = new GridPane();
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);

        int rowIndex = 0;

        gridPane.add(new TextWithStyleClass("Output:"), 0, rowIndex);
        gridPane.add(outputTextField, 1, rowIndex);
        gridPane.add(outputButton, 2, rowIndex);

        rowIndex++;

        gridPane.add(new TextWithStyleClass("Name:"), 0, rowIndex);
        gridPane.add(nameTextField, 1, rowIndex);

        rowIndex++;

        HBox reportHBox = new HBox(UiConstants.SPACING);
        for (Report report : DataManager.getReportManager().getAvailableReports()) {
            CheckBox checkBox = new CheckBox(report.getReportName());
            reportMap.put(checkBox, report);
            reportHBox.getChildren().add(checkBox);
        }

        gridPane.add(new TextWithStyleClass("Reports:"), 0, rowIndex);
        gridPane.add(reportHBox, 1, rowIndex);

        rowIndex++;

        filterDatesCheckBox.setOnAction(event -> {
            startDateTimePicker.setDisable(!filterDatesCheckBox.isSelected());
            stopDateTimePicker.setDisable(!filterDatesCheckBox.isSelected());
        });
        gridPane.add(new TextWithStyleClass("Date Range:"), 0, rowIndex);
        gridPane.add(filterDatesCheckBox, 1, rowIndex);

        rowIndex++;

        GridPane dateRangeGridPane = new GridPane();
        dateRangeGridPane.setVgap(UiConstants.SPACING);
        dateRangeGridPane.setHgap(UiConstants.SPACING);
        dateRangeGridPane.getColumnConstraints().add(new ColumnConstraints());
        dateRangeGridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);

        startDateTimePicker.setDisable(true);
        stopDateTimePicker.setDisable(true);

        dateRangeGridPane.add(new TextWithStyleClass("Start:"), 0, 0);
        dateRangeGridPane.add(startDateTimePicker, 1, 0);

        dateRangeGridPane.add(new TextWithStyleClass("Stop:"), 0, 1);
        dateRangeGridPane.add(stopDateTimePicker, 1, 1);

        gridPane.add(dateRangeGridPane, 1, rowIndex);

        Button generateButton = new Button("Generate Reports");
        generateButton.setOnAction(event -> generateReports());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        Hyperlink openHyperlink = new Hyperlink("open location");
        openHyperlink.setOnAction(event -> openOutputFolder());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, openHyperlink, generateButton, closeButton);
        buttonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING, titleLabel, new Separator(), gridPane, new Separator(), buttonsHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));

        return mainVBox;
    }

    private void onSelectFileAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select output location.");
        directoryChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory(SETTINGS_NAME));
        File selectedFile = directoryChooser.showDialog(getStage());
        if (selectedFile != null) {
            outputTextField.setText(selectedFile.getAbsolutePath());
            outputTextField.setStyle(null);
            SettingsManager.getPreferences().setLastDirectory(SETTINGS_NAME, selectedFile.getAbsolutePath());
        }
    }

    private void generateReports() {
        if (nameTextField.getText().isBlank()) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Enter a name for report set.", getStage());
            return;
        }

        final long startTime;
        final long stopTime;
        if (filterDatesCheckBox.isSelected()) {
            try {
                startTime = startDateTimePicker.getTimestamp();
                stopTime = stopDateTimePicker.getTimestamp();
            } catch (Exception e) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Enter valid date range.", getStage());
                return;
            }
        } else {
            startTime = -1;
            stopTime = -1;
        }

        final File outputDirectory = new File(outputTextField.getText() + File.separator + nameTextField.getText());
        if (outputDirectory.exists()) {
            String message = String.format("The report set %s already exists.%n" +
                    "Do you want to override these reports?", nameTextField.getText());
            if (!DialogUtilities.showYesNoDialog(DialogUtilities.INVALID_ENTRY, message, getStage())) {
                return;
            }
        } else {
            if (!outputDirectory.mkdirs()) {
                String message = String.format("Unable to create output folder.%n%s", outputDirectory.getAbsolutePath());
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, message, getStage());
                return;
            }
        }

        progressDialog = new ProgressDialog(getStage(), () -> {

        });
        progressDialog.addStatus("generating reports...");
        progressDialog.createAndShow();

        new Thread(() -> doGenerateReports(outputDirectory, startTime, stopTime), "Generate Reports Thread").start();
    }

    private void doGenerateReports(File outputDirectory, long startTime, long stopTime) {
        for (Map.Entry<CheckBox, Report> entry : reportMap.entrySet()) {
            if (entry.getKey().isSelected()) {
                DataManager.getReportManager().generateReport(entry.getValue(), outputDirectory.getAbsolutePath(),
                        startTime, stopTime);
            }
        }
        Platform.runLater(() -> progressDialog.close());
    }

    private void openOutputFolder() {
        File outputDirectory = new File(outputTextField.getText() + File.separator + nameTextField.getText());
        if (!outputDirectory.exists()) {
            outputDirectory = new File(outputTextField.getText());
            if (!outputDirectory.exists()) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Output directory does not exists.", getStage());
                return;
            }
        }
        try {
            Desktop.getDesktop().open(outputDirectory);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }
}
