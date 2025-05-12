package gov.mil.otc._3dvis.playback.dataset.otcam;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OtcamImportFolder extends ImportFolder {

    /* Directory Layout
        'otcam'
        |  {otcam db files}
     */

    public static final String FOLDER_NAME = "otcam";

    public static OtcamImportFolder scanAndCreate(File file) {
        OtcamImportFolder otcamImportFolder = new OtcamImportFolder(file);
        if (otcamImportFolder.validateAndScan()) {
            return otcamImportFolder;
        }
        return null;
    }

    private final List<OtcamImportObject> otcamImportObjectList = new ArrayList<>();

    public OtcamImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.getName().endsWith(".db")) {
                OtcamImportObject otcamImportObject = OtcamImportObject.scanAndCreate(file);
                if (otcamImportObject != null) {
                    if (otcamImportObject.determineIsNew()) {
                        setModified(true);
                    }
                    otcamImportObjectList.add(otcamImportObject);
                }
            }
        }
        return true;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        for (OtcamImportObject otcamImportObject : otcamImportObjectList) {
            treeItems.add(new TreeItem<>(otcamImportObject));
        }
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        for (OtcamImportObject otcamImportObject : otcamImportObjectList) {
            otcamImportObject.importObject(scanStatusLine);
        }
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    public List<OtcamImportObject> getOtcamImportObjectList() {
        return otcamImportObjectList;
    }
}
