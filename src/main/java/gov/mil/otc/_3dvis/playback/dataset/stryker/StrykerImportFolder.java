package gov.mil.otc._3dvis.playback.dataset.stryker;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.manual.TirImportFolder;
import gov.mil.otc._3dvis.playback.dataset.otcam.OtcamImportFolder;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StrykerImportFolder extends ImportFolder {

    /* Directory Layout
        'otcam' //otcam folder set
        'tir' //tir folder set
        'manual_data' //manual_data folder set
        {entity folder} //stryker entity folder set
     */

    public static final String FOLDER_NAME = "stryker";

    public static StrykerImportFolder scanAndCreate(File file) {
        StrykerImportFolder strykerImportFolder = new StrykerImportFolder(file);
        if (strykerImportFolder.validateAndScan()) {
            return strykerImportFolder;
        }
        return null;
    }

    private final List<StrykerEntityImportFolder> strykerEntityImportFolderList = new ArrayList<>();
    private OtcamImportFolder otcamImportFolder = null;
    private TirImportFolder tirImportFolder = null;

    public StrykerImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(OtcamImportFolder.FOLDER_NAME)) {
                otcamImportFolder = OtcamImportFolder.scanAndCreate(file);
            } else if (file.getName().equalsIgnoreCase(TirImportFolder.FOLDER_NAME)) {
                tirImportFolder = TirImportFolder.scanAndCreate(file);
            }
        }
        for (File file : files) {
            if (!file.getName().equalsIgnoreCase(OtcamImportFolder.FOLDER_NAME) &&
                    !file.getName().equalsIgnoreCase(TirImportFolder.FOLDER_NAME)) {
                StrykerEntityImportFolder strykerEntityImportFolder = StrykerEntityImportFolder.scanAndCreate(file, otcamImportFolder);
                if (strykerEntityImportFolder != null) {
                    strykerEntityImportFolderList.add(strykerEntityImportFolder);
                }
            }
        }

        if (otcamImportFolder == null) {
            setMissing(true);
        }

        if (strykerEntityImportFolderList.isEmpty()) {
            setMissing(true);
        }

        return true;
    }

    @Override
    public VBox getDisplayPane() {
        ListView<String> listView = new ListView<>();

        String item;
        item = "otcam db:";
        listView.getItems().add(item);
        if (otcamImportFolder != null) {
            item = String.format("  %d db files", otcamImportFolder.getOtcamImportObjectList().size());
        } else {
            item = "  ***none***";
        }
        listView.getItems().add(item);

        item = "tir files:";
        listView.getItems().add(item);
        if (tirImportFolder != null) {
            item = String.format("  %d db files", tirImportFolder.getTirImportObjectList().size());
        } else {
            item = "  ***none***";
        }
        listView.getItems().add(item);

        item = "stryker entity sets:";
        listView.getItems().add(item);
        if (strykerEntityImportFolderList.isEmpty()) {
            item = "  ***none***";
            listView.getItems().add(item);
        } else {
            for (StrykerEntityImportFolder strykerEntityImportFolder : strykerEntityImportFolderList) {
                item = "  " + strykerEntityImportFolder.getName();
                listView.getItems().add(item);
            }
        }

        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        // todo
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        if (otcamImportFolder != null) {
            treeItems.add(otcamImportFolder.getTreeItem());
        }
        if (tirImportFolder != null) {
            treeItems.add(tirImportFolder.getTreeItem());
        }
        for (StrykerEntityImportFolder strykerEntityImportFolder : strykerEntityImportFolderList) {
            treeItems.add(strykerEntityImportFolder.getTreeItem());
        }
        return treeItems;
    }
}
