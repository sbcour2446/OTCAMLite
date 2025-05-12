package gov.mil.otc._3dvis.datamodel;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

import java.io.File;

public class TimedFile extends TimedData {

    public enum FileType {
        UNKNOWN,
        IMAGE,
        PDF,
        TIR,
        CSV,
        OTHER
    }

    private final File file;
    private final FileType fileType;
    private final String fileGroup;

    public TimedFile(long timestamp, File file, FileType fileType, String fileGroup) {
        super(timestamp);

        this.file = file;
        this.fileType = fileType;
        this.fileGroup = fileGroup;
    }

    public File getFile() {
        return file;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getFileGroup() {
        return fileGroup;
    }
}
