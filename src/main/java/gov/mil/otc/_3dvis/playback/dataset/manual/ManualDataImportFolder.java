package gov.mil.otc._3dvis.playback.dataset.manual;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class ManualDataImportFolder extends ImportFolder {

    public static String FOLDER_NAME = "manual_data";

    public static ManualDataImportFolder scanAndCreate(File file) {
        ManualDataImportFolder manualDataImportFolder = new ManualDataImportFolder(file);
        if (manualDataImportFolder.validateAndScan()) {
            return manualDataImportFolder;
        }
        return null;
    }

    protected ManualDataImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        return null;
    }

    @Override
    protected boolean scanFiles(File[] files) {
        return false;
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        // not implemented
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }
}
