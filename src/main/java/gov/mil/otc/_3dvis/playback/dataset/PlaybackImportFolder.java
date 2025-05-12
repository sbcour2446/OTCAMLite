package gov.mil.otc._3dvis.playback.dataset;

import gov.mil.otc._3dvis.playback.ConfigurationManager;
import gov.mil.otc._3dvis.playback.PlaybackConfiguration;
import gov.mil.otc._3dvis.ui.UiCommon;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.status.Result;
import gov.mil.otc._3dvis.ui.widgets.status.StatusLine;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlaybackImportFolder extends ImportFolder {

    /* Directory Layout
       {test name}
       |  'missionConfigurations.json' //optional
       |  {import data folders}
     */

    private final List<ImportFolder> importFolderList = new ArrayList<>();
    private MissionConfigurationsImportObject missionConfigurationsImportObject;
    private PlaybackConfiguration playbackConfiguration = null;

    public PlaybackImportFolder(File folder) {
        super(folder);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        StatusLine childStatus = scanStatusLine.createChildStatus("loading configuration");
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(PlaybackConfiguration.NAME)) {
                childStatus.appendText(" " + file + "...");
                playbackConfiguration = ConfigurationManager.load(file, PlaybackConfiguration.class);
                break;
            }
        }
        if (playbackConfiguration == null) {
            scanStatusLine.addError("missing configuration file");
            childStatus.setResult(new Result("failed, creating default", false));
            playbackConfiguration = new PlaybackConfiguration();
            playbackConfiguration.save(getObject().getAbsolutePath(), PlaybackConfiguration.NAME);
        } else {
            childStatus.setResult(new Result("complete", true));
        }

        importFolderList.clear();
        List<ImportFolder> foldersImporting = new ArrayList<>();
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            ImportFolder importFolder = ImportFolderFactory.create(file);
            if (importFolder != null) {
                importFolderList.add(importFolder);
                importFolder.startScan(scanStatusLine);
                foldersImporting.add(importFolder);
            } else if (MissionConfigurationsImportObject.isMissionConfigurationFile(file)) {
                missionConfigurationsImportObject = MissionConfigurationsImportObject.scanAndCreate(file);
            }
        }

        while (!foldersImporting.isEmpty() && !isCancelRequested()) {
            int i = 0;
            while (i < foldersImporting.size()) {
                if (foldersImporting.get(i).isScanning()) {
                    i++;
                } else {
                    foldersImporting.remove(i);
                }
            }
        }

        return !importFolderList.isEmpty();
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        for (ImportFolder ImportFolder : importFolderList) {
            treeItems.add(ImportFolder.getTreeItem());
        }
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        if (missionConfigurationsImportObject != null) {
            VBox vBox = new VBox(UiConstants.SPACING, UiCommon.createTitleLabel("Mission Configuration"));
            vBox.getChildren().add(missionConfigurationsImportObject.getDisplayPane());
            return vBox;
        }
        return null;
    }

    @Override
    public void doImport() {
        if (missionConfigurationsImportObject != null) {
            StatusLine importStatus = this.scanStatusLine.createChildStatus("importing mission configuration...");
            missionConfigurationsImportObject.importObject(importStatusLine);
            importStatus.setResult(new Result("complete", true));
        }
        for (ImportFolder importFolder : importFolderList) {
            StatusLine importStatus = this.scanStatusLine.createChildStatus("importing " + importFolder.getName() + "...");
            importFolder.importFolder(importStatusLine);
            importStatus.setResult(new Result("complete", true));
        }
    }
}
