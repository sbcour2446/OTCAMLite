package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.DeviceState;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class DeviceStatusPane {

    private final TextField alertTextField = new TextField();
    private final TextField stateTextField = new TextField();
    private final TextField activityTextField = new TextField();
    private final TextField timestampTextField = new TextField();
    private final TextField orientationTextField = new TextField();
    private final Pane pane = new Pane();
    private DeviceState deviceState = null;

    public DeviceStatusPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);

        int rowIndex = 0;

        gridPane.add(new Label("Alert:"), 0, rowIndex);
        gridPane.add(alertTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("State:"), 0, rowIndex);
        gridPane.add(stateTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Activity:"), 0, rowIndex);
        gridPane.add(activityTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Last Update:"), 0, rowIndex);
        gridPane.add(timestampTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Orientation:"), 0, rowIndex);
        gridPane.add(orientationTextField, 1, rowIndex);

        pane.getChildren().add(gridPane);
    }

    public Pane getPane() {
        return pane;
    }

    public void update(Device device) {
        DeviceState newDeviceState = null;
        if (device != null) {
            newDeviceState = device.getCurrentDeviceState();
        }

        if (newDeviceState == null) {
            reset();
        } else {
            if (hasChanges(newDeviceState)) {
                setValues(newDeviceState);
            }
        }
    }

    public void update(DeviceState deviceState) {
        if (deviceState == null) {
            reset();
        } else {
            if (hasChanges(deviceState)) {
                setValues(deviceState);
            }
        }
    }

    public void reset() {
        if (deviceState != null) {
            deviceState = null;
            Platform.runLater(() -> {
                alertTextField.setText("");
                stateTextField.setText("");
                activityTextField.setText("");
                timestampTextField.setText("");
                orientationTextField.setText("");
            });
        }
    }

    private boolean hasChanges(DeviceState newDeviceState) {
        return deviceState == null ||
                !deviceState.getAlertReason().equals(newDeviceState.getAlertReason()) ||
                deviceState.isAlert() != newDeviceState.isAlert() ||
                !deviceState.getState().equals(newDeviceState.getState()) ||
                !deviceState.getActivity().equals(newDeviceState.getActivity()) ||
                deviceState.getTimestamp() != newDeviceState.getTimestamp();
    }

    private void setValues(DeviceState newDeviceState) {
        deviceState = newDeviceState;
        Platform.runLater(() -> {
            String alertText;
            if (deviceState.getAlertReason().isEmpty()) {
                alertText = deviceState.isAlert() ? "Yes" : "No";
            } else {
                alertText = deviceState.getAlertReason();
            }
            alertTextField.setText(alertText);
            stateTextField.setText(deviceState.getStateDescription());
            activityTextField.setText(deviceState.getActivity());
            timestampTextField.setText(Utility.formatTime(deviceState.getTimestamp(), Common.TIME_ONLY_WITH_MILLIS));
            String orientationText = "";
            if (deviceState.getYaw() != null && deviceState.getPitch() != null) {
                orientationText = String.format("%4.2f, %4.2f", deviceState.getYaw(), deviceState.getPitch());
            }
            orientationTextField.setText(orientationText);
        });
    }
}
