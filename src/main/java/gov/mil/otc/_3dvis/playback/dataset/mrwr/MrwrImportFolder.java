package gov.mil.otc._3dvis.playback.dataset.mrwr;

import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MrwrImportFolder extends ImportFolder {

    /* Directory Layout
        'mrwr'
        |  'apache'
        |  |  {entity name} //EntityImportFolder
        |  |  |  'tspi_data_csv' //1553 data files, csv
        |  |  |  'video' //media folder set
        |  'threat'
        |  |  {entity name} //EntityImportFolder
        |  |  |  'data' //threat data files, tab delimited
        |  |  |  'video' //media folder set
     */

    public static final String FOLDER_NAME = "mrwr";

    private ApacheImportFolder apacheImportFolder = null;
    private ThreatImportFolder threatImportFolder = null;

    public MrwrImportFolder(File folder) {
        super(folder);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }
            if (file.getName().equalsIgnoreCase(ApacheImportFolder.FOLDER_NAME)) {
                apacheImportFolder = new ApacheImportFolder(file);
                apacheImportFolder.startScan(scanStatusLine);
            } else if (file.getName().equalsIgnoreCase(ThreatImportFolder.FOLDER_NAME)) {
                threatImportFolder = new ThreatImportFolder(file);
                threatImportFolder.startScan(scanStatusLine);
            }
        }

        if (apacheImportFolder != null) {
            apacheImportFolder.waitForScanComplete();
        }

        if (threatImportFolder != null) {
            threatImportFolder.waitForScanComplete();
        }

        if (apacheImportFolder == null) {
            scanStatusLine.addError("missing apache folder");
            setMissing(true);
        }

        return true;
    }

    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        if (apacheImportFolder != null) {
            treeItems.add(apacheImportFolder.getTreeItem());
        }
        if (threatImportFolder != null) {
            treeItems.add(threatImportFolder.getTreeItem());
        }
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        if (apacheImportFolder != null) {
            listView.getItems().add(apacheImportFolder);
        }
        if (threatImportFolder != null) {
            listView.getItems().add(threatImportFolder);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        if (apacheImportFolder != null) {
            apacheImportFolder.importFolder(importStatusLine);
        }

        if (threatImportFolder != null) {
            threatImportFolder.importFolder(importStatusLine);
        }
    }
}
