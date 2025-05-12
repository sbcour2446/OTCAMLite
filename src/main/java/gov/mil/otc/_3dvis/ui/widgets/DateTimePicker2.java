package gov.mil.otc._3dvis.ui.widgets;

import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerValidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DateTimePicker2 extends HBox {

    private final DatePicker datePicker = new DatePicker();
    private final TextWithStyleClass hourLabel = new TextWithStyleClass("HH");
    private final TextField hourTextField = new TextField();
    private final TextWithStyleClass minuteLabel = new TextWithStyleClass("mm");
    private final TextField minuteTextField = new TextField("00");
    private final TextWithStyleClass secondLabel = new TextWithStyleClass("ss");
    private final TextField secondTextField = new TextField("00");
    private final TextWithStyleClass millisecondLabel = new TextWithStyleClass("SSS");
    private final TextField millisecondTextField = new TextField("000");
    private final ShowFields showFields;

    public enum ShowFields {
        ALL,
        YEAR_MONTH_DAY_HOUR_MINUTE_SECOND,
        YEAR_MONTH_DAY_HOUR_MINUTE,
        YEAR_MONTH_DAY_HOUR,
        YEAR_MONTH_DAY
    }

    public DateTimePicker2(long timestamp) {
        this(timestamp, ShowFields.ALL);
    }

    public DateTimePicker2(long timestamp, ShowFields showFields) {
        this.showFields = showFields;
        initialize();
        setTimestamp(timestamp);
    }

    private void initialize() {
        setSpacing(UiConstants.SPACING);
        setAlignment(Pos.CENTER_LEFT);
        getChildren().add(datePicker);

        if (showFields.ordinal() <= ShowFields.YEAR_MONTH_DAY_HOUR.ordinal()) {
            hourTextField.textProperty().addListener(
                    new IntegerValidationListener(hourTextField, 0, 23));
            hourTextField.setPrefWidth(30);
            hourTextField.setPadding(new Insets(0));
            hourTextField.setAlignment(Pos.CENTER);
            setMoveToNextField(hourTextField, minuteTextField, null, 2);
            HBox hBox = new HBox(2, hourLabel, hourTextField);
            hBox.setAlignment(Pos.CENTER_LEFT);
            getChildren().add(hBox);

            if (showFields.ordinal() <= ShowFields.YEAR_MONTH_DAY_HOUR_MINUTE.ordinal()) {
                minuteTextField.textProperty().addListener(
                        new IntegerValidationListener(minuteTextField, 0, 59));
                minuteTextField.setPrefWidth(30);
                minuteTextField.setPadding(new Insets(0));
                minuteTextField.setAlignment(Pos.CENTER);
                setMoveToNextField(minuteTextField, secondTextField, hourTextField, 2);
                hBox = new HBox(2, minuteLabel, minuteTextField);
                hBox.setAlignment(Pos.CENTER_LEFT);
                getChildren().add(hBox);

                if (showFields.ordinal() <= ShowFields.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND.ordinal()) {
                    secondTextField.textProperty().addListener(
                            new IntegerValidationListener(secondTextField, 0, 59));
                    secondTextField.setPrefWidth(30);
                    secondTextField.setPadding(new Insets(0));
                    secondTextField.setAlignment(Pos.CENTER);
                    setMoveToNextField(secondTextField, millisecondTextField, minuteTextField, 2);
                    hBox = new HBox(2, secondLabel, secondTextField);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    getChildren().add(hBox);

                    if (showFields.ordinal() <= ShowFields.ALL.ordinal()) {
                        millisecondTextField.textProperty().addListener(
                                new IntegerValidationListener(millisecondTextField, 0, 999));
                        millisecondTextField.setPrefWidth(40);
                        millisecondTextField.setPadding(new Insets(0));
                        millisecondTextField.setAlignment(Pos.CENTER);
                        setMoveToNextField(millisecondTextField, null, secondTextField, 3);
                        hBox = new HBox(2, millisecondLabel, millisecondTextField);
                        hBox.setAlignment(Pos.CENTER_LEFT);
                        getChildren().add(hBox);
                    }
                }
            }
        }
    }

    private void setMoveToNextField(final TextField currentField, final TextField nextField,
                                    final TextField previousField, final int width) {
        currentField.setOnKeyReleased(event -> {
            if (nextField != null && ((event.getCode() == KeyCode.RIGHT &&
                    currentField.getText().length() == currentField.getCaretPosition()) ||
                    (currentField.getCaretPosition() == width))) {
                nextField.requestFocus();
            } else if (previousField != null && event.getCode() == KeyCode.LEFT && currentField.getCaretPosition() == 0) {
                previousField.requestFocus();
            }
        });
    }

    public void setTimestamp(long timestamp) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
        datePicker.setValue(localDateTime.toLocalDate());
        hourTextField.setText(String.valueOf(localDateTime.getHour()));
        minuteTextField.setText(String.valueOf(localDateTime.getMinute()));
        secondTextField.setText(String.valueOf(localDateTime.getSecond()));
        millisecondTextField.setText(String.valueOf(localDateTime.getNano() / 1000000));
    }

    public long getTimestamp() {
        long timestamp = 0;
        try {
            int hour = 0;
            int minute = 0;
            int second = 0;
            int millisecond = 0;
            if (showFields.ordinal() <= ShowFields.YEAR_MONTH_DAY_HOUR.ordinal()) {
                hour = Integer.parseInt(hourTextField.getText());
                if (showFields.ordinal() <= ShowFields.YEAR_MONTH_DAY_HOUR_MINUTE.ordinal()) {
                    minute = Integer.parseInt(minuteTextField.getText());
                    if (showFields.ordinal() <= ShowFields.YEAR_MONTH_DAY_HOUR_MINUTE_SECOND.ordinal()) {
                        second = Integer.parseInt(secondTextField.getText());
                        if (showFields.ordinal() <= ShowFields.ALL.ordinal()) {
                            millisecond = Integer.parseInt(millisecondTextField.getText());
                        }
                    }
                }
            }
            LocalTime localTime = LocalTime.of(hour, minute, second, millisecond * 1000000);
            LocalDateTime localDateTime = LocalDateTime.of(datePicker.getValue(), localTime);
            timestamp = localDateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return timestamp;
    }

    public int getYear() {
        return datePicker.getValue().getYear();
    }
}
