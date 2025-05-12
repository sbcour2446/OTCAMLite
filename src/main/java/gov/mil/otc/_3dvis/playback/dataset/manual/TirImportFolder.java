package gov.mil.otc._3dvis.playback.dataset.manual;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TirImportFolder extends ImportFolder {

    public static final String FOLDER_NAME = "tir";

    public static boolean isTirFolder(File folder) {
        return folder.isDirectory() && folder.getName().equalsIgnoreCase(FOLDER_NAME);
    }

    public static TirImportFolder scanAndCreate(File folder) {
        TirImportFolder tirImportFolder = new TirImportFolder(folder);
        if (tirImportFolder.validateAndScan()) {
            return tirImportFolder;
        }
        return null;
    }

    private List<TirImportObject> tirImportObjectList = new ArrayList<>();

    public TirImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                TirImportObject tirImportObject = TirImportObject.scanAndCreate(file);
                if (determineIsNew(file)) {
                    tirImportObject.setNew(true);
                    setModified(true);
                }
                tirImportObjectList.add(tirImportObject);
            }
        }
        return true;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        return new ArrayList<>();
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (TirImportObject tirImportObject : tirImportObjectList) {
            listView.getItems().add(tirImportObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        for (TirImportObject tirImportObject : tirImportObjectList) {
            tirImportObject.importObject(importStatusLine);
        }
    }

    @Override
    public void doImport(IEntity entity) {
        for (TirImportObject tirImportObject : tirImportObjectList) {
            tirImportObject.importObject(entity, importStatusLine);
        }
    }

    public List<TirImportObject> getTirImportObjectList() {
        return tirImportObjectList;
    }
}
