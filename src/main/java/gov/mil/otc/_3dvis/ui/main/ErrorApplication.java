package gov.mil.otc._3dvis.ui.main;

import gov.mil.otc._3dvis.ui.AlreadyRunningApplication;
import gov.mil.otc._3dvis.ui.utility.StageUtility;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ErrorApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parameters parameters = getParameters();
        String error = parameters.getNamed().get("error");

        primaryStage.getIcons().add(ImageLoader.getLogo());
        primaryStage.setTitle("3DVis");
        Scene scene = new Scene(new VBox());
        primaryStage.setScene(scene);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageLoader.getLogo());
        alert.setTitle("3DVis Failed");
        alert.setContentText(error);
        alert.getDialogPane().setPadding(new Insets(10, 10, 10, 10));
        ThemeHelper.applyTheme(alert);
        StageUtility.centerStage(stage);
        alert.initStyle(StageStyle.TRANSPARENT);
        alert.getDialogPane().getScene().setFill(Color.TRANSPARENT);
        alert.getDialogPane().getScene().getRoot().getStyleClass().add("transparent");
        alert.showAndWait();

        primaryStage.close();
        Platform.exit();
    }
}