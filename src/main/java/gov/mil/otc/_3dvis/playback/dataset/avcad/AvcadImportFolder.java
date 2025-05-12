package gov.mil.otc._3dvis.playback.dataset.avcad;

import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AvcadImportFolder extends ImportFolder {

    /* Directory Layout
        'avcad'
        |  'lidar'
        |  |  {lidar xml files}
        |  'sensor'
        |  |  {entity name}
        |  |  |  'p10'
        |  |  |  |  {p10 files}
        |  |  |  'logs'
        |  |  |  |  {log files}
     */

    public static final String FOLDER_NAME = "avcad";

    public static AvcadImportFolder scanAndCreate(File folder) {
        AvcadImportFolder importFolder = new AvcadImportFolder(folder);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private AvcadConfigurationImportObject avcadConfigurationImportObject = null;

    public AvcadImportFolder(File folder) {
        super(folder);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        // get AVCAD configuration first
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(AvcadConfigurationImportObject.NAME)){
                avcadConfigurationImportObject = AvcadConfigurationImportObject.scanAndCreate(file);
            }
        }

        if (avcadConfigurationImportObject == null) {
            //create default configuration file
            File file = new File(getObject().getAbsolutePath() + File.separator + AvcadConfigurationImportObject.NAME);
            avcadConfigurationImportObject = AvcadConfigurationImportObject.scanAndCreate(file);
        }

        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }

            ImportFolder importFolder = null;

            if (file.getName().equalsIgnoreCase(LidarImportFolder.FOLDER_NAME)) {
                importFolder = new LidarImportFolder(file);
                importFolder.validateAndScan(this);
            } else if (file.getName().equalsIgnoreCase(SensorImportFolder.FOLDER_NAME)) {
                importFolder = new SensorImportFolder(file, avcadConfigurationImportObject.getObject());
                importFolder.validateAndScan(this);
            }

            if (importFolder != null) {
                if (importFolder.isNew() || importFolder.isModified()) {
                    setModified(true);
                }
                importFolderList.add(importFolder);
            }
        }

        return true;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        treeItems.add(new TreeItem<>(avcadConfigurationImportObject));
        treeItems.addAll(super.getChildTreeItems());
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        listView.getItems().add(avcadConfigurationImportObject);
        for (ImportFolder importFolder : importFolderList) {
            listView.getItems().add(importFolder);
        }
        return new VBox(UiConstants.SPACING, listView);
    }
}
