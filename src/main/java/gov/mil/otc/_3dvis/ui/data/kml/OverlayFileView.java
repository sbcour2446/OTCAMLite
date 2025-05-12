package gov.mil.otc._3dvis.ui.data.kml;

import java.io.File;

public class OverlayFileView {

    private final File file;

    public OverlayFileView(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getAbsolutePath();
    }
}
