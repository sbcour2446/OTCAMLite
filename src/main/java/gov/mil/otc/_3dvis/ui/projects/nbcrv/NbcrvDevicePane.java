package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.DeviceState;
import gov.mil.otc._3dvis.project.nbcrv.DeviceStateListener;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvEntity;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class NbcrvDevicePane implements DeviceStateListener {

    private final Pane pane;
    private final NbcrvEntity entity;
    private final ListView<DeviceView> deviceListView = new ListView<>();
    private final ConcurrentHashMap<Device, DeviceView> deviceViewMap = new ConcurrentHashMap<>();
    private final DeviceStatusPane deviceStatusPane = new DeviceStatusPane();

    public NbcrvDevicePane(NbcrvEntity entity) {
        this.entity = entity;

        initializeDeviceList();

        deviceListView.setMinHeight(100);
        URL url = ThemeHelper.class.getResource("/css/widget_pane_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            deviceListView.getStylesheets().add(css);
        }

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(deviceListView);
        borderPane.setBottom(deviceStatusPane.getPane());

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
        deviceListView.setCellFactory(deviceViewListView -> new DeviceCell());
        deviceListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        deviceListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && entity.isInScope()) {
                deviceStatusPane.update(newValue.getDevice());
            } else {
                deviceStatusPane.reset();
            }
        });

        ObservableList<DeviceView> items = FXCollections.observableArrayList(deviceView ->
                new Observable[]{deviceView.statusCircleProperty()});
        deviceListView.setItems(items);
        for (Device device : entity.getDeviceList()) {
            DeviceView deviceView = new DeviceView(device);
            items.add(deviceView);
            deviceViewMap.put(device, deviceView);
            device.addListener(this);
        }
    }

    public void dispose() {
        for (Device device : entity.getDeviceList()) {
            device.removeListener(this);
        }
    }

    @Override
    public void changed(Device device, DeviceState newDeviceState) {
        DeviceView deviceView = deviceViewMap.get(device);
        if (deviceView != null) {
            deviceView.update(newDeviceState);
            if (deviceView.equals(deviceListView.getSelectionModel().getSelectedItem())) {
                deviceStatusPane.update(newDeviceState);
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
