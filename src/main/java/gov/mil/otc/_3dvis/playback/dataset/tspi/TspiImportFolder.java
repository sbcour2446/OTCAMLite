package gov.mil.otc._3dvis.playback.dataset.tspi;

import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;

import java.io.File;

public class TspiImportFolder extends ImportFolder {

    protected TspiImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        return false;
    }

    public long getFirstTimestamp() {
        long firstTimestamp = Long.MAX_VALUE;
        for (ImportObject<?> importObject : importObjectList) {
            if (importObject instanceof TspiImportObject tspiImportObject) {
                long timestamp = tspiImportObject.getFirstTimestamp();
                if (timestamp > 0 && timestamp < firstTimestamp) {
                    firstTimestamp = timestamp;
                }
            }
        }
        return firstTimestamp;
    }

    public long getLastTimestamp() {
        long lastTimestamp = Long.MIN_VALUE;
        for (ImportObject<?> importObject : importObjectList) {
            if (importObject instanceof TspiImportObject tspiImportObject) {
                long timestamp = tspiImportObject.getLastTimestamp();
                if (timestamp > 0 && timestamp > lastTimestamp) {
                    lastTimestamp = timestamp;
                }
            }
        }
        return lastTimestamp;
    }
}
