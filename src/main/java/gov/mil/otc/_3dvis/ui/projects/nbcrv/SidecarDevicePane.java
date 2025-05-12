package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.project.nbcrv.GenericDevice;
import gov.mil.otc._3dvis.project.nbcrv.GenericDeviceState;
import gov.mil.otc._3dvis.project.nbcrv.GenericDeviceStateListener;
import gov.mil.otc._3dvis.project.nbcrv.SidecarEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SidecarDevicePane implements GenericDeviceStateListener {

    private final Pane pane;
    private final SidecarEntity entity;
    private final ListView<GenericDevice> deviceListView = new ListView<>();
    private final ConcurrentHashMap<GenericDevice, GridPane> deviceViewMap = new ConcurrentHashMap<>();
    private final DeviceStatusPane deviceStatusPane = new DeviceStatusPane();
    private final GridPane gridPane = new GridPane();

    public SidecarDevicePane(SidecarEntity entity) {
        this.entity = entity;

        initializeDeviceList();

        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        deviceListView.setMinHeight(100);
        URL url = ThemeHelper.class.getResource("/css/widget_pane_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            deviceListView.getStylesheets().add(css);
        }

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(deviceListView);
        borderPane.setBottom(gridPane);

        TitledPane titledPane = new TitledPane("Sensors", borderPane);
        titledPane.setCollapsible(false);
        VBox.setVgrow(titledPane, Priority.ALWAYS);
        titledPane.setMaxHeight(Double.MAX_VALUE);

        pane = new VBox(titledPane);
    }

    public Pane getPane() {
        return pane;
    }

    private void initializeDeviceList() {
//        deviceListView.setCellFactory(deviceViewListView -> new DeviceCell());
        deviceListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        deviceListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && entity.isInScope()) {
                updateStatusPane(oldValue, newValue);
            } else {
                updateStatusPane(oldValue, null);
            }
        });

        for (GenericDevice device : entity.getDeviceList()) {
            deviceListView.getItems().add(device);
        }
    }

    private final Map<String, TextField> statusFieldMap = new HashMap<>();

    private void updateStatusPane(GenericDevice oldValue, GenericDevice newValue) {
        if (oldValue != null) {
            oldValue.removeListener(this);
        }

        createStatusPane(newValue);

        if (newValue != null) {
            newValue.addListener(this);
        }
    }

    private void createStatusPane(GenericDevice device) {
        statusFieldMap.clear();
        Platform.runLater(() -> {
            gridPane.getChildren().clear();
            if (device != null && !device.getGenericDeviceStates().isEmpty()) {
                int rowIndex = 0;
                GenericDeviceState deviceState = device.getGenericDeviceStates().get(0);
                for (String field : deviceState.getFieldNames()) {
                    TextField textField = new TextField();
                    statusFieldMap.put(field, textField);

                    gridPane.add(new TextWithStyleClass(field), 0, rowIndex);
                    gridPane.add(textField, 1, rowIndex);
                    rowIndex++;
                }
            }
        });
    }

    public void dispose() {
        for (GenericDevice device : entity.getDeviceList()) {
            device.removeListener(this);
        }
    }

    @Override
    public void changed(GenericDevice device, GenericDeviceState newDeviceState) {
        if (newDeviceState == null) {
            for (TextField textField : statusFieldMap.values()) {
                textField.setText("");
            }
        } else {
            for (String field : newDeviceState.getFieldNames()) {
                TextField textField = statusFieldMap.get(field);
                textField.setText(newDeviceState.getValue(field));
            }
        }
    }

    public static class DeviceCell extends ListCell<DeviceView> {

        @Override
        public void updateItem(DeviceView deviceView, boolean empty) {
            super.updateItem(deviceView, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (deviceView != null) {
                setText(deviceView.getName());
                setGraphic(deviceView.getStatusCircle());
            } else {
                setText("null");
                setGraphic(null);
            }
        }
    }
}
