package gov.mil.otc._3dvis.ui.application.tena;

import gov.mil.otc._3dvis.tena.ConnectionState;
import gov.mil.otc._3dvis.tena.ITenaConnectionListener;
import gov.mil.otc._3dvis.tena.TenaController;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Timer;
import java.util.TimerTask;

public class TenaDisconnectController extends TransparentWindow implements ITenaConnectionListener {

    private final Button abortButton = new Button("Abort");

    public static synchronized void show() {
        new TenaDisconnectController().createAndShow(true);
    }

    private TenaDisconnectController() {
    }

    @Override
    protected Pane createContentPane() {
        abortButton.setDisable(true);
        abortButton.setOnAction(event -> close());

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(abortButton);

        VBox mainVBox = new VBox();
        mainVBox.setPadding(new Insets(10, 10, 10, 10));
        mainVBox.getChildren().add(new Label("Disconnecting from TENA Execution."));
        mainVBox.getChildren().add(new Label("Please wait..."));
        mainVBox.getChildren().add(hBox);

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        Timer timer = new Timer("TENA Disconnect Timer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                abortButton.setDisable(false);
            }
        }, 10000);

        TenaController.addConnectionListener(this);
        TenaController.disconnect();

        return true;
    }

    @Override
    public void onStatusChange(final ConnectionState connectionState) {
        if (connectionState == ConnectionState.DISCONNECTED) {
            Platform.runLater(this::close);
        }
    }
}
