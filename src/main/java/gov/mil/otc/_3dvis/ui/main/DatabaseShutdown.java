package gov.mil.otc._3dvis.ui.main;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
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

public class DatabaseShutdown extends TransparentWindow {

    private final Button abortButton = new Button("Abort");

    public static synchronized void showShutdownAndCommit() {
        new DatabaseShutdown().createAndShow(true);
    }

    private DatabaseShutdown() {
    }

    @Override
    protected Pane createContentPane() {
        abortButton.setDisable(true);
        abortButton.setOnAction(event -> close());

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(abortButton);

        VBox mainVBox = new VBox();
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.getChildren().add(new Label("Saving data to database."));
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

        new Thread(() -> {
            DataManager.shutdown();
            Platform.runLater(this::close);
        }, "Database Shutdown Thread").start();

        return true;
    }
}
