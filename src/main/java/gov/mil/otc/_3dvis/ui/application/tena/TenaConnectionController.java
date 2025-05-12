package gov.mil.otc._3dvis.ui.application.tena;

import TENA.UnsignedShort;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.tena.ConnectionState;
import gov.mil.otc._3dvis.tena.ITenaConnectionListener;
import gov.mil.otc._3dvis.tena.TenaController;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerValidationListener;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TenaConnectionController extends TransparentWindow implements ITenaConnectionListener {

    private final Label statusLabel = new Label();
    private final TextField applicationNameTextField = new TextField();
    private final TextField siteIdTextField = new TextField();
    private final TextField applicationIdTextField = new TextField();
    private final ComboBox<String> listenEndpointHostnameComboBox = new ComboBox<>();
    private final TextField listenEndpointPortTextField = new TextField();
    private final TextField emEndpointHostnameTextField = new TextField();
    private final TextField emEndpointPortTextField = new TextField();
    private final Button connectButton = new Button("Connect");
    private final Button disconnectButton = new Button("Disconnect");
    private boolean connectionRequested = false;

    public static synchronized void show() {
        new TenaConnectionController().createAndShow();
    }

    private TenaConnectionController() {
    }

    @Override
    protected Pane createContentPane() {
        statusLabel.setText(TenaController.getConnectionState().toString());
        siteIdTextField.textProperty().addListener(new IntegerValidationListener(siteIdTextField,
                0, UnsignedShort.MAX_VALUE.intValue()));
        applicationIdTextField.textProperty().addListener(new IntegerValidationListener(applicationIdTextField,
                0, UnsignedShort.MAX_VALUE.intValue()));

        String name = SettingsManager.getSettings().getString("tena.application.name", "");
        int siteId = SettingsManager.getSettings().getInteger("tena.id.site", Defaults.SITE_APP_ID_3DVIS);
        int appId = SettingsManager.getSettings().getInteger("tena.id.application", Defaults.SITE_APP_ID_3DVIS);

        applicationNameTextField.setText(name);
        siteIdTextField.setText(String.valueOf(siteId));
        applicationIdTextField.setText(String.valueOf(appId));

        String listenEndpointHostname = populateListenEndpointHostname();

        listenEndpointPortTextField.setText(SettingsManager.getSettings().getString("tena.endpoint.listen.port", "60000"));
        emEndpointHostnameTextField.setText(SettingsManager.getSettings().getString("tena.endpoint.em.hostname", listenEndpointHostname));
        emEndpointPortTextField.setText(SettingsManager.getSettings().getString("tena.endpoint.em.port", "55100"));

        Label label = new Label();
        Font currentFont = label.getFont();
        Font boldFont = Font.font(currentFont.getFamily(), FontWeight.BOLD, currentFont.getSize());

        Label titleLabel = new Label("TENA Connection");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 20));
        BorderPane titleBorderPane = new BorderPane(titleLabel);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);

        int rowIndex = 0;

        label = new Label("Status:");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex);
        gridPane.add(statusLabel, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Separator(), 0, rowIndex, 2, 1);

        rowIndex++;

        label = new Label("Application Name & ID");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex, 2, 1);
        GridPane.setHalignment(label, HPos.CENTER);

        rowIndex++;

        label = new Label("Name:");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex);
        gridPane.add(applicationNameTextField, 1, rowIndex);

        rowIndex++;

        label = new Label("Site:");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex);
        gridPane.add(siteIdTextField, 1, rowIndex);

        rowIndex++;

        label = new Label("Application:");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex);
        gridPane.add(applicationIdTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Separator(), 0, rowIndex, 2, 1);

        rowIndex++;

        label = new Label("Listen Endpoint");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex, 2, 1);
        GridPane.setHalignment(label, HPos.CENTER);

        rowIndex++;

        label = new Label("Hostname:");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex);
        gridPane.add(listenEndpointHostnameComboBox, 1, rowIndex);

        rowIndex++;

        label = new Label("Port:");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex);
        gridPane.add(listenEndpointPortTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Separator(), 0, rowIndex, 2, 1);

        rowIndex++;

        label = new Label("Execution Manager Endpoint");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex, 2, 1);
        GridPane.setHalignment(label, HPos.CENTER);

        rowIndex++;

        label = new Label("Hostname:");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex);
        gridPane.add(emEndpointHostnameTextField, 1, rowIndex);

        rowIndex++;

        label = new Label("Port:");
        label.setFont(boldFont);
        gridPane.add(label, 0, rowIndex);
        gridPane.add(emEndpointPortTextField, 1, rowIndex);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());
        connectButton.setOnAction(event -> connect());
        disconnectButton.setOnAction(event -> disconnect());
        HBox hBox = new HBox(connectButton, disconnectButton);
        BorderPane buttonBorderPane = new BorderPane(null, null, closeButton, null, hBox);

        VBox mainVBox = new VBox(UiConstants.SPACING, titleBorderPane, new Separator(), gridPane, new Separator(), buttonBorderPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        updateState(TenaController.getConnectionState());
        TenaController.addConnectionListener(this);
        return true;
    }

    private String populateListenEndpointHostname() {
        String listenEndpointHostname = SettingsManager.getSettings().getString("tena.endpoint.listen.hostname", "127.0.0.1");
        try {
            String localHostName = InetAddress.getLocalHost().getHostName();
            listenEndpointHostnameComboBox.getItems().add(localHostName);

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
                if (!networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress address = interfaceAddress.getAddress();
                    listenEndpointHostnameComboBox.getItems().add(address.getHostAddress());
                }
            }
        } catch (SocketException | UnknownHostException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        if (listenEndpointHostnameComboBox.getItems().contains(listenEndpointHostname)) {
            listenEndpointHostnameComboBox.getSelectionModel().select(listenEndpointHostname);
        } else {
            listenEndpointHostnameComboBox.getSelectionModel().selectFirst();
        }

        return getSelectedListenEndpointHostname();
    }

    private String getSelectedListenEndpointHostname() {
        String listenEndpointHostname = listenEndpointHostnameComboBox.getSelectionModel().getSelectedItem();
        return listenEndpointHostname == null ? "" : listenEndpointHostname;
    }

    private void connect() {
        connectionRequested = true;
        int siteId;
        try {
            siteId = Integer.parseInt(siteIdTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid site ID", true, getStage());
            return;
        }

        int appId;
        try {
            appId = Integer.parseInt(applicationIdTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid application ID", true, getStage());
            return;
        }

        String listenEndpointHostname = listenEndpointHostnameComboBox.getSelectionModel().getSelectedItem();

        int listenEndpointPort;
        try {
            listenEndpointPort = Integer.parseInt(listenEndpointPortTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid listen port", true, getStage());
            return;
        }

        String emEndpointHostname = emEndpointHostnameTextField.getText();
        int emEndpointPort;
        try {
            emEndpointPort = Integer.parseInt(emEndpointPortTextField.getText());
        } catch (NumberFormatException e) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Invalid EM port", true, getStage());
            return;
        }

        TenaController.connect("3DVis - " + applicationNameTextField.getText(),
                listenEndpointHostname, listenEndpointPort, emEndpointHostname, emEndpointPort);

        SettingsManager.getSettings().setValue("tena.application.name", applicationNameTextField.getText());
        SettingsManager.getSettings().setValue("tena.id.site", siteId);
        SettingsManager.getSettings().setValue("tena.id.application", appId);
        SettingsManager.getSettings().setValue("tena.endpoint.listen.hostname", listenEndpointHostname);
        SettingsManager.getSettings().setValue("tena.endpoint.listen.port", listenEndpointPort);
        SettingsManager.getSettings().setValue("tena.endpoint.em.hostname", emEndpointHostname);
        SettingsManager.getSettings().setValue("tena.endpoint.em.port", emEndpointPort);
    }

    private void disconnect() {
        TenaController.disconnect();
    }

    private void updateState(ConnectionState connectionState) {
        switch (connectionState) {
            case CONNECTED -> {
                if (connectionRequested) {
                    delayClose();
                }
                connectButton.setDisable(true);
                disconnectButton.setDisable(false);
                applicationNameTextField.setDisable(true);
                siteIdTextField.setDisable(true);
                applicationIdTextField.setDisable(true);
                listenEndpointHostnameComboBox.setDisable(true);
                listenEndpointPortTextField.setDisable(true);
                emEndpointHostnameTextField.setDisable(true);
                emEndpointPortTextField.setDisable(true);
            }
            case CONNECTING, DISCONNECTING -> {
                connectButton.setDisable(true);
                disconnectButton.setDisable(true);
                applicationNameTextField.setDisable(true);
                siteIdTextField.setDisable(true);
                applicationIdTextField.setDisable(true);
                listenEndpointHostnameComboBox.setDisable(true);
                listenEndpointPortTextField.setDisable(true);
                emEndpointHostnameTextField.setDisable(true);
                emEndpointPortTextField.setDisable(true);
            }
            case DISCONNECTED, FAILED -> {
                connectButton.setDisable(false);
                disconnectButton.setDisable(true);
                applicationNameTextField.setDisable(false);
                siteIdTextField.setDisable(false);
                applicationIdTextField.setDisable(false);
                listenEndpointHostnameComboBox.setDisable(false);
                listenEndpointPortTextField.setDisable(false);
                emEndpointHostnameTextField.setDisable(false);
                emEndpointPortTextField.setDisable(false);
            }
            default -> {
                String message = String.format("Unexpected value: %s", connectionState);
                Logger.getGlobal().log(Level.WARNING, message);
            }
        }

        statusLabel.setText(connectionState.toString());
    }

    private void delayClose() {
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(this::close);
        }).start();
    }

    @Override
    public void onStatusChange(final ConnectionState connectionState) {
        Platform.runLater(() -> updateState(connectionState));
    }
}
