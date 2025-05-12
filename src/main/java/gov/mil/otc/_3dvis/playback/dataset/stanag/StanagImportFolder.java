package gov.mil.otc._3dvis.playback.dataset.stanag;

import gov.mil.otc._3dvis.data.file.ImportFile;
import gov.mil.otc._3dvis.data.stanag.A101File;
import gov.mil.otc._3dvis.data.stanag.A302File;
import gov.mil.otc._3dvis.data.stanag.StanagFileFactory;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.List;

public class StanagImportFolder extends ImportFolder {

    /* Directory Layout
        'stanag'
        |  (stanag files)
     */

    public static String FOLDER_NAME = "stanag";

    public StanagImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.isDirectory()) {
                scanForImportObjects(file);
            } else if (file.getName().toLowerCase().endsWith(".csv")) {
                addImportFile(file);
            }
        }
        return true;
    }

    private void scanForImportObjects(File folder) {
        File[] fileList = folder.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (isCancelRequested()) {
                    break;
                }
                if (file.isDirectory()) {
                    scanForImportObjects(file);
                } else if (file.getName().toLowerCase().endsWith(".csv")) {
                    addImportFile(file);
                }
            }
        }
    }

    private void addImportFile(File file) {
        ImportFile importFile = StanagFileFactory.getStanagImportFile(file);
        if (importFile != null && importFile.processFile()) {
            if (importFile instanceof A101File a101File) {
                importObjectList.add(new A101ImportObject(a101File));
            } else if (importFile instanceof A302File a302File) {
                importObjectList.add(new A302ImportObject(a302File));
            }
        }
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        return null;
    }
}
