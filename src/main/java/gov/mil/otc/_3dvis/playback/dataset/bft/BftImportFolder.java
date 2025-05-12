package gov.mil.otc._3dvis.playback.dataset.bft;

import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BftImportFolder extends ImportFolder {

    /* Directory Layout
        {uto file}
        {pcap files}
     */

    public static final String FOLDER_NAME = "bft";

    public static BftImportFolder scanAndCreate(File folder) {
        BftImportFolder importFolder = new BftImportFolder(folder);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private UrnFilterImportObject urnFilterImportObject = null;
    private UtoImportObject utoImportObject = null;

    public BftImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        List<Integer> urnFilterList = new ArrayList<>();
        for (File file : files) {
            if (file.getName().toLowerCase().equalsIgnoreCase(UrnFilterImportObject.NAME)) {
                urnFilterImportObject = UrnFilterImportObject.scanAndCreate(file);
                urnFilterList = urnFilterImportObject.getUrnFilterList();
            }
        }

        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }

            if (file.getName().toLowerCase().endsWith(".pcapng") ||
                    file.getName().toLowerCase().endsWith(".pcap") ||
                    file.getName().toLowerCase().endsWith(".dvt")) {
                PcapImportObject pcapImportObject = PcapImportObject.scanAndCreate(file, urnFilterList);
                importObjectList.add(pcapImportObject);
            } else if (file.getName().toLowerCase().endsWith(".uto")) {
                utoImportObject = UtoImportObject.scanAndCreate(file);
            }
        }

        if (importObjectList.isEmpty()) {
            setMissing(true);
        }

        return true;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        if (urnFilterImportObject != null) {
            treeItems.add(new TreeItem<>(urnFilterImportObject));
        }
        treeItems.addAll(super.getChildTreeItems());
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        if (urnFilterImportObject != null) {
            listView.getItems().add(urnFilterImportObject);
        }
        for (ImportObject<?> importObject : importObjectList) {
            listView.getItems().add(importObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        for (ImportObject<?> importObject : importObjectList) {
            if (utoImportObject != null && importObject instanceof PcapImportObject pcapImportObject) {
                pcapImportObject.setUto(utoImportObject);
            }
            importObject.importObject(importStatusLine);
        }
    }
}
