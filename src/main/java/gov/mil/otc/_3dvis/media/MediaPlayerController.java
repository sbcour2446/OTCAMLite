package gov.mil.otc._3dvis.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.application.Platform;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaPlayerController {

    private static final int DEFAULT_SYNC_RATE = 1000; // 1 second
    private static final long SYNC_THRESHOLD = 1000;
    private static final long TIME_ADJUSTMENT_OFFSET = 500;

    private MediaPlayerFactory mediaPlayerFactory = null;
    private EmbeddedMediaPlayer embeddedMediaPlayer = null;
    private final ImageView videoImageView = new ImageView();
    private ImageViewVideoSurface imageViewVideoSurface;
    private final Tooltip tooltip = new Tooltip();
    private final IEntity entity;
    private final MediaSet mediaSet;
    private MediaFile currentMediaFile;
    private final Object playerMutex = new Object();
    private final Object monitorObject = new Object();
    private boolean shutdownRequested = false;
    private boolean fileNotFound = false;
    private boolean noVideAtThisTime = false;
    private boolean isMuted = true;
    private boolean needToCreatePlayer = true;

    protected MediaPlayerController(IEntity entity, MediaSet mediaSet) {
        this.entity = entity;
        this.mediaSet = mediaSet;

        ContextMenu contextMenu = createContextMenu();
        videoImageView.setOnContextMenuRequested(event -> contextMenu.show(videoImageView, event.getScreenX(), event.getScreenY()));

        tooltip.setText(mediaSet.getName());
        tooltip.setShowDuration(new Duration(10000));
        Tooltip.install(videoImageView, tooltip);

        new Thread(this::updateTask, "MediaPlayerController - updateTask").start();
    }

    private void createMediaPlayer() {
        Platform.runLater(() -> {
            try {
                synchronized (playerMutex) {
                    if (embeddedMediaPlayer != null) {
                        embeddedMediaPlayer.controls().stop();
                        embeddedMediaPlayer.release();
                    }
                    if (mediaPlayerFactory != null) {
                        mediaPlayerFactory.release();
                    }
                    List<String> options = List.of(isMuted ? "--no-audio" : "");
                    mediaPlayerFactory = new MediaPlayerFactory(options);
                    embeddedMediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
                    imageViewVideoSurface = new ImageViewVideoSurface(videoImageView);
                    imageViewVideoSurface.attach(embeddedMediaPlayer);
                    embeddedMediaPlayer.videoSurface().set(imageViewVideoSurface);
                    currentMediaFile = null;
                    playerMutex.notify();
                }
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "MediaPlayerController::createMediaPlayer", e);
            }
        });
    }

    public ImageView getVideoImageView() {
        return videoImageView;
    }

    public void close() {
        synchronized (monitorObject) {
            shutdownRequested = true;
            monitorObject.notifyAll();
        }
    }

    public void setMute(boolean mute) {
        isMuted = mute;
        needToCreatePlayer = true;
    }

    private ContextMenu createContextMenu() {
        MenuItem goToStartMenuItem = new MenuItem("goto start");
        goToStartMenuItem.setOnAction(event -> goToMediaStartTime());

        MenuItem reloadMenuItem = new MenuItem("Reload");
        reloadMenuItem.setOnAction(event -> {
            createMediaPlayer();
        });

        final CheckMenuItem muteCheckMenuItem = new CheckMenuItem("Mute");
        muteCheckMenuItem.setSelected(isMuted);
        muteCheckMenuItem.setOnAction(event -> {
            setMute(muteCheckMenuItem.isSelected());
        });

        MenuItem closeMenuItem = new MenuItem("Close");
        closeMenuItem.setOnAction(event -> close());

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(goToStartMenuItem);
        contextMenu.getItems().add(reloadMenuItem);
        contextMenu.getItems().add(muteCheckMenuItem);
        contextMenu.getItems().add(closeMenuItem);
        return contextMenu;
    }

    private void updateTask() {
        try {
            while (!shutdownRequested) {
                synchronized (monitorObject) {
                    monitorObject.wait(DEFAULT_SYNC_RATE);
                    if (shutdownRequested) {
                        break;
                    }
                }

                if (needToCreatePlayer) {
                    needToCreatePlayer = false;
                    createMediaPlayer();
                    try {
                        synchronized (playerMutex) {
                            playerMutex.wait();
                        }
                    } catch (InterruptedException e) {
                        Logger.getGlobal().log(Level.WARNING, "MediaPlayerController::updateTask", e);
                        Thread.currentThread().interrupt();
                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, "MediaPlayerController::updateTask", e);
                    }
                }

                if (currentMediaFile == null) {
                    loadMediaFile(embeddedMediaPlayer);
                } else {
                    checkAndUpdateTime(embeddedMediaPlayer);
                }
            }

            embeddedMediaPlayer.controls().stop();
            embeddedMediaPlayer.release();
            mediaPlayerFactory.release();
            MediaPlayerManager.removeMediaSet(entity, mediaSet);
        } catch (InterruptedException e) {
            Logger.getGlobal().log(Level.WARNING, "MediaPlayerController::updateTask", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "MediaPlayerController::updateTask", e);
        }
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
        currentMediaFile = mediaSet.getMediaFileAt(currentTime);

        if (currentMediaFile != null) {
            try {
                if (currentMediaFile.exists()) {
                    String mrl = "file:///" + currentMediaFile.getAbsolutePath();
                    embeddedMediaPlayer.media().startPaused(mrl);
                    videoImageView.setImage(null);
                    tooltip.setText(mediaSet.getName() + System.lineSeparator() + currentMediaFile.getAbsolutePath());
                    fileNotFound = false;
                } else {
                    currentMediaFile = null;
                    if (!fileNotFound) {
                        videoImageView.setImage(ImageLoader.getFxImage("/images/cannot_find_file.png"));
                        Logger.getGlobal().log(Level.WARNING, "MediaPlayerController::loadMediaFile:file does not exists");
                        fileNotFound = true;
                    }
                }
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "MediaPlayerController::loadMediaFile", e);
            }
            noVideAtThisTime = false;
        } else {
            if (!noVideAtThisTime) {
                embeddedMediaPlayer.controls().stop();
                videoImageView.setImage(ImageLoader.getFxImage("/images/no_video.png"));
                tooltip.setText(mediaSet.getName() + System.lineSeparator() + "no video at this time");
                noVideAtThisTime = true;
            }
        }
    }

    private void goToMediaStartTime() {
        long time = Long.MAX_VALUE;
        for (Long startTime : mediaSet.getMediaFiles().keySet()) {
            if (startTime < time) {
                time = startTime;
            }
        }

        if (time < Long.MAX_VALUE) {
            TimeManager.setTime(time);
        }
    }
}
