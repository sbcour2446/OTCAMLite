package gov.mil.otc._3dvis.ui.utility;

import gov.mil.otc._3dvis.ui.TransparentWindow;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Comparator;
import java.util.TimeZone;

public class TimeZonePicker extends TransparentWindow {

    private final ComboBox<TimeZoneObject> timeZoneComboBox = new ComboBox<>();
    private TimeZone selectedTimeZone = null;

    public static TimeZone show(Stage parent) {
        TimeZonePicker timeZonePicker = new TimeZonePicker(parent);
        timeZonePicker.createAndShow(true);
        return timeZonePicker.selectedTimeZone;
    }

    private TimeZonePicker(Stage parent) {
        super(parent);
    }

    @Override
    protected Pane createContentPane() {
        TimeZoneObject initialTimeZoneObject = null;
        String[] ids = TimeZone.getAvailableIDs();
        for (String id : ids) {
            TimeZoneObject timeZoneObject = new TimeZoneObject(TimeZone.getTimeZone(id));
            timeZoneComboBox.getItems().add(timeZoneObject);
            if (TimeZone.getDefault().getID().equals(id)) {
                initialTimeZoneObject = timeZoneObject;
            }
        }
        if (initialTimeZoneObject != null) {
            timeZoneComboBox.getSelectionModel().select(initialTimeZoneObject);
        }
        timeZoneComboBox.getItems().sort(Comparator.comparingInt(o -> o.getTimeZone().getRawOffset()));

        Button okButton = new Button("OK");
        okButton.setOnAction(event -> onOkAction());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> onCancelAction());

        HBox closeButtonsHBox = new HBox(10);
        closeButtonsHBox.getChildren().addAll(okButton, cancelButton);

        return new VBox(createTitleLabel("TimeZone Picker"), new Separator(), timeZoneComboBox, new Separator(), closeButtonsHBox);
    }

    private void onOkAction() {
        if (timeZoneComboBox.getSelectionModel().getSelectedItem() != null) {
            selectedTimeZone = timeZoneComboBox.getSelectionModel().getSelectedItem().getTimeZone();
        }
        close();
    }

    private void onCancelAction() {
        selectedTimeZone = null;
        close();
    }

    private static final class TimeZoneObject {

        private final TimeZone timeZone;

        public TimeZoneObject(TimeZone timeZone) {
            this.timeZone = timeZone;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }

        @Override
        public String toString() {
            int offset = timeZone.getRawOffset() / 1000;
            int hour = offset / 3600;
            int minutes = (offset % 3600) / 60;
            return String.format("(GMT%+d:%02d) %s", hour, minutes, timeZone.getID());
        }
    }
}
