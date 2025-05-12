package gov.mil.otc._3dvis.playback.dataset;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.status.StatusLine;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ImportFolder extends ImportObject<File> {

    private boolean scanning = false;
    private boolean cancelRequested = false;
    protected StatusLine scanStatusLine = null;
    protected StatusLine importStatusLine = null;
    private boolean expandNode;
    protected final List<ImportFolder> importFolderList = new ArrayList<>();
    protected final List<ImportObject<?>> importObjectList = new ArrayList<>();
    protected final List<ImportFolder> scanningFolderList = new ArrayList<>();
    private final Object scanningMutex = new Object();

    protected ImportFolder(File folder) {
        super(folder, folder.getName());
    }

    public String getName() {
        return getObject().getName();
    }

    public boolean isScanning() {
        return scanning;
    }

    public boolean isCancelRequested() {
        return cancelRequested;
    }

    public void requestCancel() {
        cancelRequested = true;
        for (ImportFolder importFolder : scanningFolderList) {
            importFolder.requestCancel();
        }
    }

    public boolean isValid() {
        return getObject() != null && getObject().exists() && getObject().isDirectory();
    }

    public void startScan(Object o) {

    }

    public void startScan(StatusLine statusLine) {
        cancelRequested = false;
        scanning = true;
        scanStatusLine = statusLine.createChildStatus("scanning " + getName() + "...");
        Thread thread = new Thread(() -> {
            boolean successful = validateAndScan(this);
            if (cancelRequested) {
                scanStatusLine.setCanceled();
            } else if (!successful) {
                scanStatusLine.setFailed();
            } else {
                scanStatusLine.setComplete();
            }
            scanning = false;
            synchronized (scanningMutex) {
                scanningMutex.notify();
            }
        }, "startScan:" + getName());
        thread.start();
    }

    protected boolean validateAndScan() {
        if (!isValid()) {
            return false;
        }

        File[] files = getObject().listFiles();
        if (files == null || files.length == 0) {
            setMissing(true);
            return true;
        }

        return scanFiles(files);
    }

    public boolean validateAndScan(ImportFolder parentFolder) {
        if (!isValid()) {
            return false;
        }

        File[] files = getObject().listFiles();
        if (files == null || files.length == 0) {
            setMissing(true);
            return true;
        }

        return scanFiles(files);
    }

    public void waitForScanComplete() {
        synchronized (scanningMutex) {
            while (!cancelRequested && scanning) {
                try {
                    scanningMutex.wait(100);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "", e);
                }
            }
        }
    }

    protected abstract boolean scanFiles(File[] files);

    public TreeItem<ImportObject<?>> getTreeItem() {
        TreeItem<ImportObject<?>> treeItem = new TreeItem<>(this);
        treeItem.setExpanded(expandNode);
        List<TreeItem<ImportObject<?>>> childTreeItems = getChildTreeItems();
        if (childTreeItems != null) {
            for (TreeItem<ImportObject<?>> childTreeItem : childTreeItems) {
                treeItem.getChildren().add(childTreeItem);
            }
        }
        return treeItem;
    }

    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        for (ImportFolder importFolder : importFolderList) {
            treeItems.add(importFolder.getTreeItem());
        }
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (ImportFolder importFolder : importFolderList) {
            listView.getItems().add(importFolder);
        }
        for (ImportObject<?> importObject : importObjectList) {
            listView.getItems().add(importObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    public void importFolder(StatusLine statusLine) {
        importStatusLine = statusLine.createChildStatus("importing " + getName() + "...");

        if ((!isNew() && !isModified()) || isMissing()) {
            importStatusLine.addError("missing data");
            importStatusLine.setFailed();
            return;
        }

        try {
            doImport();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.INFO, "ImportObject::importObject:" + getName(), e);
            importStatusLine.setFailed();
            return;
        }

        importStatusLine.setComplete();
        setImported(true);
    }

    public void importFolder(IEntity entity, StatusLine statusLine) {
        importStatusLine = statusLine.createChildStatus("importing " + getName() + "...");

        if ((!isNew() && !isModified()) || isMissing()) {
            importStatusLine.addError("missing data");
            importStatusLine.setFailed();
            return;
        }

        try {
            doImport(entity);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.INFO, "ImportObject::importObject:" + getName(), e);
            importStatusLine.setFailed();
            return;
        }

        importStatusLine.setComplete();
        setImported(true);
    }

    @Override
    public void doImport() {
        if ((!isNew() && !isModified()) || isMissing()) {
            return;
        }
        for (ImportFolder importFolder : importFolderList) {
            importFolder.importFolder(importStatusLine);
        }
        for (ImportObject<?> importObject : importObjectList) {
            importObject.importObject(importStatusLine);
        }
    }

    @Override
    public void doImport(IEntity entity) {
        if ((!isNew() && !isModified()) || isMissing()) {
            return;
        }
        for (ImportFolder importFolder : importFolderList) {
            importFolder.importFolder(entity, importStatusLine);
        }
        for (ImportObject<?> importObject : importObjectList) {
            importObject.importObject(entity, importStatusLine);
        }
        setImported(true);
    }

    protected void setExpandNode() {
        expandNode = true;
    }

    public boolean determineIsNewMission(String name) {
        for (Mission mission : DataManager.getMissions()) {
            if (mission.getName().equalsIgnoreCase(name)) {
                return false;
            }
        }
        return true;
    }
}
