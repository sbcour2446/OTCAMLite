package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.DeviceState;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class DeviceView {

    private final Device device;
    private boolean alert = false;
    private String state = "";
    private final ObjectProperty<Node> statusCircle = new SimpleObjectProperty<>();

    public DeviceView(Device device) {
        this.device = device;
        initialize();
    }

    private void initialize() {
        DeviceState deviceState = device.getCurrentDeviceState();
        Node node = createStatusCircle(deviceState);
        statusCircle.set(node);
    }

    public void update() {
        update(device.getCurrentDeviceState());
    }

    public void update(DeviceState deviceState) {
        if (hasChanges(deviceState)) {
            alert = deviceState != null && deviceState.isAlert();
            state = deviceState != null ? deviceState.getState() : "";
            Node node = createStatusCircle(deviceState);
            Platform.runLater(() -> statusCircle.set(node));
        }
    }

    private boolean hasChanges(DeviceState deviceState) {
        return deviceState == null ||
                deviceState.isAlert() != alert ||
                !deviceState.getState().equals(state);
    }


    private Node createStatusCircle(DeviceState deviceState) {
        Color stateColor = Color.GRAY;
        Color alertColor = Color.GRAY;
        if (deviceState != null) {
            stateColor = getStateColor(deviceState);
            alertColor = getAlertColor(deviceState);
        }
        Circle innerCircle = new Circle(5, stateColor);
        Circle outerCircle = new Circle(10, alertColor);
        return new StackPane(outerCircle, innerCircle);
    }

    private Color getStateColor(DeviceState deviceState) {
        if (deviceState.getState().equalsIgnoreCase("GC_OK") ||
                deviceState.getState().equalsIgnoreCase("OPR")) {
            return Color.GREEN;
        }
        return Color.YELLOW;
    }

    private Color getAlertColor(DeviceState deviceState) {
        if (deviceState.isAlert()) {
            return Color.RED;
        }
        return Color.GREEN;
    }

    public Device getDevice() {
        return device;
    }

    public String getName() {
        return device.getName();
    }

    public Node getStatusCircle() {
        return statusCircle.get();
    }

    public ObjectProperty<Node> statusCircleProperty() {
        return statusCircle;
    }
}
