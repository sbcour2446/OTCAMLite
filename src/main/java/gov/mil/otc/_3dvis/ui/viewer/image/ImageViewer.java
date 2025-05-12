package gov.mil.otc._3dvis.ui.viewer.image;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageViewer {

    private final Stage stage = new Stage();
    private final ImageView imageView = new ImageView();
    private final Timer timer = new Timer("ImageViewer");
    private final IEntity entity;
    private final Map<Long, File> screenshotMap;
    private File currentFile = null;

    public ImageViewer(IEntity entity, Map<Long, File> screenshotMap) {
        this.entity = entity;
        this.screenshotMap = screenshotMap;

        BorderPane borderPane = new BorderPane();
        imageView.fitHeightProperty().bind(borderPane.heightProperty());
        imageView.fitWidthProperty().bind(borderPane.widthProperty());
        imageView.setPreserveRatio(true);
        borderPane.setCenter(imageView);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setTitle("Image Viewer : " + entity.getName());
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);

        if (SettingsManager.getPreferences().getOrCreateWindowGeometry("Image Viewer").getHeight() == null) {
            SettingsManager.getPreferences().getOrCreateWindowGeometry("Image Viewer").setHeight(600);
        }
        if (SettingsManager.getPreferences().getOrCreateWindowGeometry("Image Viewer").getWidth() == null) {
            SettingsManager.getPreferences().getOrCreateWindowGeometry("Image Viewer").setWidth(800);
        }

        StageSizer stageSizer = new StageSizer("Image Viewer");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());

        stage.setOnCloseRequest(this::handleWindowCloseRequest);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 1000, 100);
    }

    public File getCurrentFile() {
        return currentFile;
    }

    private void handleWindowCloseRequest(WindowEvent event) {
        timer.cancel();
        ImageViewerManager.close(entity);
    }

    private void update() {
        long currentTime = TimeManager.getTime();
        File file = getFileAt(currentTime);

        if (file == null) {
            currentFile = null;
            imageView.setImage(null);
            updateTitle("Image Viewer : " + entity.getName());
            return;
        }

        if (!file.equals(currentFile)) {
            currentFile = file;
            updateTitle(entity.getName() + " : " + file.getName());
            try {
                FileInputStream input = new FileInputStream(file);
                Image image = new Image(input);
                imageView.setImage(image);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ImageViewer:update", e);
            }
        }
    }

    private void updateTitle(String value) {
        Platform.runLater(() -> {
            stage.setTitle(value);
        });
    }

    public File getFileAt(long currentTime) {
        File file = null;
        for (Map.Entry<Long, File> entry : screenshotMap.entrySet()) {
            long fileTime = entry.getKey();
            if (currentTime < fileTime) {
                break;
            } else {
                file = entry.getValue();
            }
        }
        return file;
    }
}
