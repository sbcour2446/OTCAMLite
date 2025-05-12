package gov.mil.otc._3dvis.media;

import java.io.File;

public class MediaFile extends File {

    private final long startTime;
    private final String mediaGroup;
    private final String mediaSet;
    private long stopTime;
    private boolean useRelativePath;

    public MediaFile(String fileName, long startTime, String mediaGroup, String mediaSet) {
        this(fileName, startTime, -1, mediaGroup, mediaSet, false);
    }

    public MediaFile(String fileName, long startTime, long stopTime, String mediaGroup, String mediaSet) {
        this(fileName, startTime, stopTime, mediaGroup, mediaSet, false);
    }

    public MediaFile(String fileName, long startTime, long stopTime, String mediaSet, boolean useRelativePath) {
        this(fileName, startTime, stopTime, "", mediaSet, useRelativePath);
    }

    public MediaFile(String fileName, long startTime, long stopTime, String mediaGroup, String mediaSet, boolean useRelativePath) {
        super(fileName);
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.mediaGroup = mediaGroup;
        this.mediaSet = mediaSet;
        this.useRelativePath = useRelativePath;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public String getMediaGroup() {
        return mediaGroup;
    }

    public String getMediaSet() {
        return mediaSet;
    }

    public void setUseRelativePath(boolean useRelativePath) {
        this.useRelativePath = useRelativePath;
    }

    public boolean isUseRelativePath() {
        return useRelativePath;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
