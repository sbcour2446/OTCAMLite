package gov.mil.otc._3dvis.playback.dataset.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaImportFolder extends ImportFolder {

    public static boolean isMediaFolder(File folder) {
        return folder.isDirectory() &&
                (folder.getName().equalsIgnoreCase("media") ||
                        folder.getName().equalsIgnoreCase("video"));
    }


    public static boolean isVideoFolder(File folder) {
        return folder.isDirectory() && folder.getName().equalsIgnoreCase("video");
    }

    public static boolean isAudioFolder(File folder) {
        return folder.isDirectory() && folder.getName().equalsIgnoreCase("audio");
    }

    public static MediaImportFolder scanAndCreate(File folder, MediaTimestampFormat mediaTimestampFormat) {
        MediaImportFolder mediaDataSet = new MediaImportFolder(folder, mediaTimestampFormat);
        if (mediaDataSet.validateAndScan()) {
            return mediaDataSet;
        }
        return null;
    }

    private final MediaTimestampFormat mediaTimestampFormat;
    private final List<MediaSetImportFolder> mediaSetImportFolderList = new ArrayList<>();
    private final List<MediaFileImportObject> mediaFileImportObjectList = new ArrayList<>();

    public MediaImportFolder(File folder, MediaTimestampFormat mediaTimestampFormat) {
        super(folder);
        this.mediaTimestampFormat = mediaTimestampFormat;
    }

    public List<MediaSetImportFolder> getMediaSetImportFolderList() {
        return mediaSetImportFolderList;
    }

    public List<MediaFileImportObject> getMediaFileImportObjectList() {
        return mediaFileImportObjectList;
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                MediaSetImportFolder mediaSetImportFolder
                        = MediaSetImportFolder.scanAndCreate(file, mediaTimestampFormat);
                if (mediaSetImportFolder != null) {
                    mediaSetImportFolderList.add(mediaSetImportFolder);
                    if (mediaSetImportFolder.isNew()) {
                        setModified(true);
                    }
                }
            } else {
                MediaFileImportObject mediaFileImportObject
                        = MediaFileImportObject.scanAndCreate(file, mediaTimestampFormat);
                if (mediaFileImportObject != null) {
                    mediaFileImportObjectList.add(mediaFileImportObject);
                    if (mediaFileImportObject.isNew()) {
                        setModified(true);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (MediaSetImportFolder mediaSetImportFolder : mediaSetImportFolderList) {
            listView.getItems().add(mediaSetImportFolder);
        }
        for (MediaFileImportObject mediaFileImportObject : mediaFileImportObjectList) {
            listView.getItems().add(mediaFileImportObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        // requires entity
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> ImportFolders = new ArrayList<>();
        for (MediaSetImportFolder mediaSetImportFolder : mediaSetImportFolderList) {
            ImportFolders.add(mediaSetImportFolder.getTreeItem());
        }
        return ImportFolders;
    }

    @Override
    public void doImport(IEntity entity) {
        for (MediaSetImportFolder mediaSetImportFolder : mediaSetImportFolderList) {
            if (mediaSetImportFolder.isNew()) {
                mediaSetImportFolder.importFolder(entity, importStatusLine);
            }
        }
        for (MediaFileImportObject mediaFileImportObject : mediaFileImportObjectList) {
            if (mediaFileImportObject.isNew()) {
                mediaFileImportObject.importObject(entity, importStatusLine);
            }
        }
        setImported(true);
    }
}
