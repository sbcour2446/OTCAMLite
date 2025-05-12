package gov.mil.otc._3dvis.ui.tools.rangefinder;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RangeFinder {

    private static RangeFinder rangeFinder = null;
    private Stage rangeFinderStage = null;

    public static void show() {
        if (rangeFinder == null) {
            rangeFinder = new RangeFinder();
        }
        rangeFinder.doShow();
    }

    private RangeFinder() {
    }

    private void doShow() {
        if (rangeFinderStage == null) {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/rangeFinder.fxml"));
                Parent parent = fxmlLoader.load();
                Scene scene = new Scene(parent);
                rangeFinderStage = new Stage();
                rangeFinderStage.getIcons().add(ImageLoader.getLogo());
                rangeFinderStage.setResizable(true);
                rangeFinderStage.setTitle("Distance Tool");
                rangeFinderStage.setScene(scene);
                RangeFinderController controller = fxmlLoader.getController();
                controller.setStage(rangeFinderStage);
                ThemeHelper.applyTheme(scene);
                StageSizer stageSizer = new StageSizer("Range Finder");
                stageSizer.setStage(rangeFinderStage, MainApplication.getInstance().getStage());
                rangeFinderStage.setOnCloseRequest(event -> rangeFinderStage = null);
            } catch (IOException e) {
                Logger.getGlobal().log(Level.WARNING, "Could not load the rangeFinder.fxml file", e);
            }
        } else {
            rangeFinderStage.toFront();
        }
    }
}
