package gov.mil.otc._3dvis.ui.data.iteration;

import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerValidationListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class IterationFormController extends TransparentWindow {

    private final TextField nameTextField = new TextField();
    private final DatePicker startDatePicker = new DatePicker();
    private final TextField startHourTextField = new TextField();
    private final DatePicker stopDatePicker = new DatePicker();
    private final TextField stopHourTextField = new TextField();
    private final CheckBox liveCheckBox = new CheckBox("is live iteration");
    private final Iteration initialIteration;
    private Iteration iteration = null;

    public static synchronized Iteration showLiveIteration(Stage parentStage) {
        long startTime = LocalDateTime.now(ZoneId.of("UTC")).atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        return show(new Iteration("", startTime, Long.MAX_VALUE), parentStage);
    }

    public static synchronized Iteration show(Stage parentStage) {
        long startTime = LocalDateTime.of(LocalDate.now(ZoneId.of("UTC")), LocalTime.of(0, 0))
                .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        long stopTime = startTime + 86400000;
        return show(new Iteration("", startTime, stopTime), parentStage);
    }

    public static synchronized Iteration show(long startTime, long stopTime, Stage parentStage) {
        return show(new Iteration("", startTime, stopTime), parentStage);
    }

    public static synchronized Iteration show(String name, long startTime, long stopTime, Stage parentStage) {
        return show(new Iteration(name, startTime, stopTime), parentStage);
    }

    public static synchronized Iteration show(Iteration initialIteration, Stage parentStage) {
        IterationFormController iterationFormController = new IterationFormController(
                initialIteration, parentStage);
        parentStage.setOpacity(.75);
        iterationFormController.createAndShow(true);
        parentStage.setOpacity(1.0);
        return iterationFormController.iteration;
    }

    private IterationFormController(Iteration initialIteration, Stage parentStage) {
        super(parentStage);
        this.initialIteration = initialIteration;
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Iteration Details");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        VBox titleVBox = new VBox(UiConstants.SPACING, titleLabel);
        titleVBox.setAlignment(Pos.CENTER);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        Label label = new Label("Name:");
        label.setMinWidth(Region.USE_PREF_SIZE);
        nameTextField.setText(initialIteration.getName());

        gridPane.add(label, 0, rowIndex);
        gridPane.add(nameTextField, 1, rowIndex, 2, 1);

        rowIndex++;

        LocalDateTime startDateTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(initialIteration.getStartTime()), ZoneId.of("UTC"));
        startDatePicker.setValue(startDateTime.toLocalDate());
        startHourTextField.setText(String.format("%02d%02d", startDateTime.getHour(),
                startDateTime.getMinute()));
        startHourTextField.textProperty().addListener(new IntegerValidationListener(startHourTextField,
                0, 2359));

        label = new Label("Date");
        label.setMinWidth(Region.USE_PREF_SIZE);
        HBox startDateHBox = new HBox(5, label, startDatePicker);
        startDateHBox.setAlignment(Pos.CENTER_LEFT);
        label = new Label("Time(HHmm)");
        label.setMinWidth(Region.USE_PREF_SIZE);
        HBox startTimeHBox = new HBox(5, label, startHourTextField);
        startTimeHBox.setAlignment(Pos.CENTER_LEFT);

        gridPane.add(new Label("Start:"), 0, rowIndex);
        gridPane.add(startDateHBox, 1, rowIndex);
        gridPane.add(startTimeHBox, 2, rowIndex);

        rowIndex++;

        long tempTime = initialIteration.getStopTime();
        if (tempTime == Long.MAX_VALUE) {
            liveCheckBox.setSelected(true);
            stopDatePicker.setDisable(true);
            stopHourTextField.setDisable(true);
            tempTime = LocalDateTime.of(LocalDate.now(ZoneId.of("UTC")), LocalTime.of(0, 0))
                    .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli() + 86400000;
        }
        LocalDateTime stopDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(tempTime), ZoneId.of("UTC"));
        stopDatePicker.setValue(stopDateTime.toLocalDate());
        stopHourTextField.setText(String.format("%02d%02d", stopDateTime.getHour(), stopDateTime.getMinute()));
        stopHourTextField.textProperty().addListener(new IntegerValidationListener(stopHourTextField,
                0, 2359));

        label = new Label("Date");
        label.setMinWidth(Region.USE_PREF_SIZE);
        HBox stopDateHBox = new HBox(5, label, stopDatePicker);
        stopDateHBox.setAlignment(Pos.CENTER_LEFT);
        label = new Label("Time(HHmm)");
        label.setMinWidth(Region.USE_PREF_SIZE);
        HBox stopTimeHBox = new HBox(5, label, stopHourTextField);
        stopTimeHBox.setAlignment(Pos.CENTER_LEFT);

        gridPane.add(new Label("Stop:"), 0, rowIndex);
        gridPane.add(stopDateHBox, 1, rowIndex);
        gridPane.add(stopTimeHBox, 2, rowIndex);

        rowIndex++;

        liveCheckBox.setOnAction(event -> {
            liveCheckBox.setSelected(liveCheckBox.isSelected());
            stopDatePicker.setDisable(liveCheckBox.isSelected());
            stopHourTextField.setDisable(liveCheckBox.isSelected());
        });

        gridPane.add(liveCheckBox, 1, rowIndex);

        Button okButton = new Button("OK");
        okButton.setOnAction(event -> onOk());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        HBox closeHBox = new HBox(UiConstants.SPACING, okButton, cancelButton);
        closeHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                titleVBox,
                new Separator(),
                gridPane,
                new Separator(),
                closeHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(500);

        return mainVBox;
    }

    private void onOk() {
        if (nameTextField.getText().isBlank()) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Enter iteration name.",
                    getStage());
            return;
        }

        long startTime;
        long stopTime;
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("Hmm");
            startTime = LocalDateTime.of(startDatePicker.getValue(),
                            LocalTime.parse(startHourTextField.getText(), dateTimeFormatter))
                    .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
            if (liveCheckBox.isSelected()) {
                stopTime = Long.MAX_VALUE;
            } else {
                stopTime = LocalDateTime.of(stopDatePicker.getValue(),
                                LocalTime.parse(stopHourTextField.getText(), dateTimeFormatter))
                        .atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
            }
        } catch (Exception e) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Enter valid date range.",
                    getStage());
            return;
        }

        if (startTime < 0 || startTime > stopTime) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Enter valid date range.",
                    getStage());
            return;
        }

        iteration = new Iteration(nameTextField.getText(), startTime, stopTime);
        close();
    }
}
