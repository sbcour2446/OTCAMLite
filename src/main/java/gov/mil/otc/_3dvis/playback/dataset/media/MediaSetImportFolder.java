package gov.mil.otc._3dvis.playback.dataset.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MediaSetImportFolder extends ImportFolder {

    public static MediaSetImportFolder scanAndCreate(File folder, MediaTimestampFormat mediaTimestampFormat) {
        MediaSetImportFolder mediaSetImportFolder = new MediaSetImportFolder(folder, mediaTimestampFormat);
        if (mediaSetImportFolder.validateAndScan()) {
            return mediaSetImportFolder;
        }
        return null;
    }

    private final MediaTimestampFormat mediaTimestampFormat;
    private final List<MediaFileImportObject> mediaFileImportObjectList = new ArrayList<>();

    protected MediaSetImportFolder(File folder, MediaTimestampFormat mediaTimestampFormat) {
        super(folder);
        this.mediaTimestampFormat = mediaTimestampFormat;
    }

    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            MediaFileImportObject mediaFileImportObject
                    = MediaFileImportObject.scanAndCreate(file, mediaTimestampFormat);
            if (mediaFileImportObject != null) {
                if (determineIsNew(file)) {
                    setNew(true);
                }
                mediaFileImportObjectList.add(mediaFileImportObject);
            }
        }

        return true;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        return List.of();
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
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
    public void doImport(IEntity entity) {
        for (MediaFileImportObject mediaFileImportObject : mediaFileImportObjectList) {
            mediaFileImportObject.importObject(entity, importStatusLine);
        }
        setImported(true);
    }
}
