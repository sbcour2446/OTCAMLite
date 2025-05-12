package gov.mil.otc._3dvis.media;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.utility.StageUtility;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VlcMediaPlayerController {

    private static final int SPACING = 10;
    private static final int DEFAULT_SYNC_RATE = 1000; // 1 second
    private static final long SYNC_THRESHOLD = 1000;
    private static final long TIME_ADJUSTMENT_OFFSET = 500;

    private final Stage stage = new Stage();
    private final MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
    private final EmbeddedMediaPlayer embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
    private ImageView videoImageView = new ImageView();
    private MediaFile currentMediaFile;
    private final Object monitorObject = new Object();
    private boolean shutdownRequested = false;
    private boolean canExit = false;
    private final long startTime = TimeManager.getTime() - 10000;

    public static void show() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(MainApplication.getInstance().getStage());
        if (file != null) {
            VlcMediaPlayerController mediaPlayerController = new VlcMediaPlayerController(file);
            mediaPlayerController.doShow();
        }
    }

    private VlcMediaPlayerController(File file) {
        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setTitle("Media Player");

        embeddedMediaPlayer.videoSurface().set(new ImageViewVideoSurface(videoImageView));
        String mrl = "file:///" + file.getAbsolutePath();
        embeddedMediaPlayer.media().startPaused(mrl);

        VBox mainVBox = new VBox(videoImageView);
        videoImageView.fitWidthProperty().bind(mainVBox.widthProperty());
        videoImageView.fitHeightProperty().bind(mainVBox.heightProperty());
        videoImageView.setPreserveRatio(true);
        mainVBox.setPadding(new Insets(SPACING, SPACING, SPACING, SPACING));

        Scene scene = new Scene(mainVBox, 600, 400);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageUtility.centerStage(stage, MainApplication.getInstance().getStage());
    }

    private void doShow() {
        stage.show();
        stage.setOnCloseRequest(this::handleWindowCloseRequest);
        new Thread(this::updateTask, "MediaPlayerController - updateTask").start();
    }

    private void handleWindowCloseRequest(WindowEvent event) {
        if (!canExit) {
            shutdownRequested = true;
            synchronized (monitorObject) {
                monitorObject.notify();
            }
            event.consume();
        }
    }

    private void updateTask() {
        embeddedMediaPlayer.audio().setMute(true);

        while (!shutdownRequested) {
            synchronized (monitorObject) {
                try {
                    monitorObject.wait(DEFAULT_SYNC_RATE);
                    if (shutdownRequested) {
                        break;
                    }
                } catch (InterruptedException e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                    Thread.currentThread().interrupt();
                }
            }

            if (embeddedMediaPlayer.audio().isMute()) {
                embeddedMediaPlayer.audio().setMute(false);
            }

            long currentMediaTime = embeddedMediaPlayer.status().time();
            long mediaLength = embeddedMediaPlayer.status().length();
            long currentTime = TimeManager.getTime();
            long mediaOffset = currentTime - startTime;

            if (currentTime < startTime || currentTime > startTime + mediaLength) {
                embeddedMediaPlayer.controls().pause();
            } else {
                embeddedMediaPlayer.overlay().enable(false);
                if (TimeManager.isPaused() && embeddedMediaPlayer.status().isPlaying()) {
                    embeddedMediaPlayer.controls().pause();
                } else if (!TimeManager.isPaused() && !embeddedMediaPlayer.status().isPlaying()) {
                    embeddedMediaPlayer.controls().play();
                }

//                embeddedMediaPlayer.controls().setRate((float) PlaybackControl.getGlobal().getPlaybackSpeed().value);

                if (Math.abs(currentMediaTime - mediaOffset) > SYNC_THRESHOLD) {
                    embeddedMediaPlayer.controls().setTime(mediaOffset + TIME_ADJUSTMENT_OFFSET);
                }
            }
        }

        embeddedMediaPlayer.controls().stop();
        embeddedMediaPlayer.release();
        mediaPlayerFactory.release();
        canExit = true;
        Platform.runLater(stage::close);
    }
}
