package gov.mil.otc._3dvis.playback;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.otcam.OtcamLoader;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.status.ICancelListener;
import gov.mil.otc._3dvis.ui.widgets.status.Result;
import gov.mil.otc._3dvis.ui.widgets.status.StatusDialog;
import gov.mil.otc._3dvis.ui.widgets.status.StatusLine;
import javafx.application.Platform;
import javafx.stage.Stage;

public class PlaybackLoader implements ICancelListener {

    public interface IImportCompleteListener {
        void onComplete(boolean successful);
    }

    public static void scanOnly(Playback playback, Stage stage, IImportCompleteListener importCompleteListener) {
        if (playback != null) {
            PlaybackLoader playbackLoader = new PlaybackLoader(playback);
            playbackLoader.importCompleteListener = importCompleteListener;
            playbackLoader.doScanAsync(stage);
        }
    }

    public static void loadPlaybackAsync(String name, Stage stage, IImportCompleteListener importCompleteListener) {
        for (Playback playback : DataManager.getPlaybackList()) {
            if (playback.getName().equalsIgnoreCase(name)) {
                loadPlaybackAsync(playback, stage, importCompleteListener);
                return;
            }
        }
    }

    public static void loadPlaybackAsync(Playback playback, Stage stage, IImportCompleteListener importCompleteListener) {
        if (playback != null) {
            PlaybackLoader playbackLoader = new PlaybackLoader(playback);
            playbackLoader.importCompleteListener = importCompleteListener;
            playbackLoader.doLoadAsync(stage);
        }
    }

    public static void unloadCurrentPlayback() {
        OtcamLoader.cancelAll();
        EntityManager.removeAllEntities();
        EventManager.removeAllEvents();
        DataManager.removeAllMissions();
        DataManager.setCurrentPlayback(null);
    }

    private StatusDialog statusDialog = null;
    private final ProgressDialog progressDialog = null;
    private final Playback playback;
    private static final Object loadingMutex = new Object();
    private boolean results;
    private IImportCompleteListener importCompleteListener;

    private PlaybackLoader(Playback playback) {
        this.playback = playback;
    }

    private void doScanAsync(Stage stage) {
        statusDialog = new StatusDialog(stage, this);
        statusDialog.createAndShow();
        new Thread(() -> {
            StatusLine statusLine = statusDialog.createStatusLine("scanning playback " + playback.getName() + " : " + playback.getFile().getAbsolutePath());
            playback.getPlaybackImportFolder().startScan(statusLine);
            playback.getPlaybackImportFolder().waitForScanComplete();

            results = !statusDialog.isCanceled();

            if (!statusDialog.isCanceled()) {
                statusLine.setResult(new Result("complete", true));
            } else {
                statusLine.setResult(new Result("canceled", false));
            }

            Platform.runLater(() -> {
                statusDialog.setComplete(true);
            });

            if (importCompleteListener != null) {
                importCompleteListener.onComplete(results);
            }
        }, "PlaybackLoader::doScanAsync").start();
    }

    private void doLoadAsync(Stage stage) {
        statusDialog = new StatusDialog(stage, this);
        statusDialog.createAndShow();
        new Thread(() -> {
            TimeManager.setPause(true);

            unloadCurrentPlayback();

            StatusLine statusLine = statusDialog.createStatusLine("loading playback " + playback.getName() + " : " + playback.getFile().getAbsolutePath());
            playback.getPlaybackImportFolder().startScan(statusLine);
            playback.getPlaybackImportFolder().waitForScanComplete();

            if (!statusDialog.isCanceled()) {
                StatusLine importStatusLine = statusDialog.createStatusLine("importing " + playback.getName() + "...");
                playback.getPlaybackImportFolder().importFolder(importStatusLine);
                importStatusLine.setResult(new Result("complete", true));
                DataManager.setCurrentPlayback(playback);
                statusDialog.createStatusLine("playback loaded");
            } else {
                statusDialog.createStatusLine("playback load canceled");
            }

            Platform.runLater(() -> {
                statusDialog.setComplete(true);
            });

            if (importCompleteListener != null) {
                importCompleteListener.onComplete(results);
            }

        }, "PlaybackLoader::doLoadAsync").start();
    }

    @Override
    public void onCancel() {
        playback.getPlaybackImportFolder().requestCancel();
        Platform.runLater(() -> {
            statusDialog.close();
        });
    }
}
