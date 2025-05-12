package gov.mil.otc._3dvis.ui.projects.blackhawk;

import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.project.blackhawk.TspiFile;

import java.util.ArrayList;
import java.util.List;

public class BlackHawkDataSet {

    private final int tailNumber;
    private final List<TspiFile> tspiFileList = new ArrayList<>();
    private List<MediaFile> mediaFileList = new ArrayList<>();
    private long startTime = System.currentTimeMillis();
    private long stopTime = 0;

    public BlackHawkDataSet(int tailNumber) {
        this.tailNumber = tailNumber;
    }

    public int getTailNumber() {
        return tailNumber;
    }

    public List<TspiFile> getTspiFileList() {
        return tspiFileList;
    }

    public void addTspiFile(TspiFile file) {
        tspiFileList.add(file);
        updateTimestamps(file);
    }

    public List<MediaFile> getMediaFileList() {
        return mediaFileList;
    }

    public void setMediaFileList(List<MediaFile> mediaFileList) {
        this.mediaFileList = mediaFileList;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    private void updateTimestamps(TspiFile tspiFile) {
        long firstTimestamp = tspiFile.getFirstTimestamp();
        if (firstTimestamp > 0 && firstTimestamp < startTime) {
            startTime = firstTimestamp;
        }
        long lastTimestamp = tspiFile.getLastTimestamp();
        if (lastTimestamp > stopTime) {
            stopTime = lastTimestamp;
        }
    }
}
