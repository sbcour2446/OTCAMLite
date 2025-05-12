package gov.mil.otc._3dvis.playback.dataset.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaGroupImportFolder extends ImportFolder {

    public static MediaGroupImportFolder scanAndCreate(File folder, MediaTimestampFormat mediaTimestampFormat) {
        MediaGroupImportFolder mediaGroupImportFolder = new MediaGroupImportFolder(folder, mediaTimestampFormat);
        if (mediaGroupImportFolder.validateAndScan()) {
            return mediaGroupImportFolder;
        }
        return null;
    }

    private final MediaTimestampFormat mediaTimestampFormat;
    private final Map<String, MediaSetImportFolder> mediaSetImportFolderMap = new HashMap<>();
    private final List<MediaSetImportObject> mediaSetImportObjectList = new ArrayList<>();

    protected MediaGroupImportFolder(File folder, MediaTimestampFormat mediaTimestampFormat) {
        super(folder);
        this.mediaTimestampFormat = mediaTimestampFormat;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        return null;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (MediaSetImportObject mediaSetImportObject : mediaSetImportObjectList) {
            listView.getItems().add(mediaSetImportObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        // requires entity
    }

    public void doImport(IEntity entity) {
        for (MediaSetImportObject mediaSetImportObject : mediaSetImportObjectList) {
            mediaSetImportObject.importObject(entity, importStatusLine);
        }
        setImported(true);
    }

    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                MediaSetImportFolder mediaSetImportFolder
                        = MediaSetImportFolder.scanAndCreate(file, mediaTimestampFormat);
                if (mediaSetImportFolder != null) {

                }
            } else {
                MediaFileImportObject mediaFileImportObject
                        = MediaFileImportObject.scanAndCreate(file, mediaTimestampFormat);
                if (mediaFileImportObject != null) {

                }
            }
        }

        return true;
    }

    private void add(MediaFileImportObject mediaFileImportObject) {
        mediaSetImportFolderMap.get(mediaFileImportObject.getObject().getMediaSet());
    }
}
