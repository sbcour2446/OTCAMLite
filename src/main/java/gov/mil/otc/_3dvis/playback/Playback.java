package gov.mil.otc._3dvis.playback;

import gov.mil.otc._3dvis.playback.dataset.PlaybackImportFolder;

import java.io.File;

public class Playback {

    private final String name;
    private final File file;
    private final long creationTime;
    private final PlaybackImportFolder playbackImportFolder;

    public Playback(String name, File file, long creationTime) {
        this.name = name;
        this.file = file;
        this.creationTime = creationTime;
        playbackImportFolder = new PlaybackImportFolder(file);
    }

    public Playback(String name, PlaybackImportFolder playbackImportFolder, long creationTime) {
        this.name = name;
        this.file = playbackImportFolder.getObject();
        this.creationTime = creationTime;
        this.playbackImportFolder = playbackImportFolder;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public PlaybackImportFolder getPlaybackImportFolder() {
        return playbackImportFolder;
    }

    public boolean verify() {
        return file.exists();
    }

    public void cancel() {
        playbackImportFolder.requestCancel();
    }
}
