package gov.mil.otc._3dvis.tools;

import java.awt.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileViewer {

    public static void show(File file) {
        try {
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "FileViewer::show", e);
        }
    }

    private FileViewer() {
    }
}
