package gov.mil.otc._3dvis.media;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.utility.StageUtility;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaPlayer {

    private static final int SPACING = 10;
    private static final int DEFAULT_SYNC_RATE = 1000; // 1 second
    private static final long SYNC_THRESHOLD = 1000;
    private static final long TIME_ADJUSTMENT_OFFSET = 500;

    private final Stage stage = new Stage();
    private final MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
    private final EmbeddedMediaPlayer embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
    private final ImageView videoImageView = new ImageView();
    private final IEntity entity;
    private final MediaSet mediaSet;
    private MediaFile currentMediaFile;
    private final Object monitorObject = new Object();
    private boolean shutdownRequested = false;
    private boolean canExit = false;
    private boolean mute = true;

//    public static void show(IEntity entity, MediaSet mediaSet) {
//        MediaPlayer mediaPlayer = new MediaPlayer(entity, mediaSet);
//        mediaPlayer.doShow();
//    }

    protected MediaPlayer(IEntity entity, MediaSet mediaSet) {
        this.entity = entity;
        this.mediaSet = mediaSet;

        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setTitle(mediaSet.getName());

//        embeddedMediaPlayer.videoSurface().set(ImageViewVideoSurfaceFactory.videoSurfaceForImageView(videoImageView));

        CheckMenuItem muteCheckMenuItem = new CheckMenuItem("Mute");
        muteCheckMenuItem.setSelected(true);
        muteCheckMenuItem.setOnAction(event -> {
            mute = muteCheckMenuItem.isSelected();
            embeddedMediaPlayer.audio().setMute(mute);
        });
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(muteCheckMenuItem);
        videoImageView.setOnContextMenuRequested(event ->
                contextMenu.show(videoImageView, event.getScreenX(), event.getScreenY()));

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

    public void show() {
        stage.show();
        stage.setOnCloseRequest(this::handleWindowCloseRequest);
        new Thread(this::updateTask, "MediaPlayerController - updateTask").start();
    }

    private void handleWindowCloseRequest(WindowEvent event) {
        if (!canExit) {
            shutdownRequested = true;
            synchronized (monitorObject) {
                monitorObject.notifyAll();
            }
            event.consume();
        }
    }

    private void updateTask() {
//        final MediaPlayerFactory mediaPlayerFactory = new MediaPlayerFactory();
//        final EmbeddedMediaPlayer embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();

        embeddedMediaPlayer.audio().setMute(mute);

        Platform.runLater(() -> embeddedMediaPlayer
                .videoSurface().set(new ImageViewVideoSurface(videoImageView)));

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

//            if (embeddedMediaPlayer.audio().isMute() != mute) {
//                embeddedMediaPlayer.audio().setMute(mute);
//            }

            if (currentMediaFile == null) {
                loadMediaFile(embeddedMediaPlayer);
            } else {
                checkAndUpdateTime(embeddedMediaPlayer);
            }
        }

        embeddedMediaPlayer.controls().stop();
        embeddedMediaPlayer.release();
        mediaPlayerFactory.release();
        canExit = true;
        MediaPlayerManager.removeMediaSet(entity, mediaSet);
        Platform.runLater(stage::close);
    }

    private void checkAndUpdateTime(EmbeddedMediaPlayer embeddedMediaPlayer) {
        long currentMediaTime = embeddedMediaPlayer.status().time();
        long currentTime = TimeManager.getTime();
        long mediaOffset = currentTime - currentMediaFile.getStartTime();

        if (currentTime < currentMediaFile.getStartTime() || currentTime > currentMediaFile.getStopTime()) {
            embeddedMediaPlayer.controls().stop();
            loadMediaFile(embeddedMediaPlayer);
        } else {
            embeddedMediaPlayer.overlay().enable(false);
            if (TimeManager.isPaused() && embeddedMediaPlayer.status().isPlaying()) {
                embeddedMediaPlayer.controls().pause();
            } else if (!TimeManager.isPaused() && !embeddedMediaPlayer.status().isPlaying()) {
                embeddedMediaPlayer.controls().play();
            }

            checkRate(embeddedMediaPlayer);

            if (Math.abs(currentMediaTime - mediaOffset) > SYNC_THRESHOLD) {
                embeddedMediaPlayer.controls().setTime(mediaOffset + TIME_ADJUSTMENT_OFFSET);
            }
        }
    }

    private void checkRate(EmbeddedMediaPlayer embeddedMediaPlayer) {
        if (TimeManager.isLive()) {
            if (embeddedMediaPlayer.status().rate() != 1) {
                embeddedMediaPlayer.controls().setRate(1);
            }
        } else {
            if (embeddedMediaPlayer.status().rate() != (float) TimeManager.getRate().getValue()) {
                embeddedMediaPlayer.controls().setRate((float) TimeManager.getRate().getValue());
            }
        }
    }

    private void loadMediaFile(EmbeddedMediaPlayer embeddedMediaPlayer) {
        long currentTime = TimeManager.getTime();
        currentMediaFile = null;

        for (Long startTime : mediaSet.getMediaFiles().keySet()) {
            if (currentTime < startTime) {
                break;
            } else {
                MediaFile file = mediaSet.getMediaFiles().get(startTime);
                if (currentTime < file.getStopTime()) {
                    currentMediaFile = file;
                }
            }
        }

        String title = "";

        if (currentMediaFile != null) {
            if (!currentMediaFile.exists()) {
                Logger.getGlobal().log(Level.WARNING, "file does not exists");
                return;
            }

            String mrl = "file:///" + currentMediaFile.getAbsolutePath();
            embeddedMediaPlayer.media().startPaused(mrl);
            title = String.format("%s - %s - %s", entity.getEntityId(), mediaSet.getName(), currentMediaFile.getName());
            videoImageView.setImage(null);
        } else {
            embeddedMediaPlayer.controls().stop();
            videoImageView.setImage(ImageLoader.getFxImage("/images/novideo.png"));
        }

        updateTitle(title);
    }

    private void updateTitle(final String title) {
        Platform.runLater(() -> stage.setTitle(title));
    }
}
