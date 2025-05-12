package gov.mil.otc._3dvis.tir;

import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.File;

public class TirFileView {

    private final TimedFile timedFile;

    public TirFileView(TimedFile timedFile) {
        this.timedFile = timedFile;
    }

    public TimedFile getTimedFile() {
        return timedFile;
    }

    public File getFile() {
        return timedFile.getFile();
    }

    public long getTimestamp() {
        return timedFile.getTimestamp();
    }

    public String getTimestampString() {
        return Utility.formatTime(timedFile.getTimestamp());
    }
}
