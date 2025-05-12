package gov.mil.otc._3dvis.playback.dataset.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.File;

public class MediaFileImportObject extends ImportObject<MediaFile> {

    public static MediaFileImportObject scanAndCreate(File file, MediaTimestampFormat mediaTimestampFormat) {
        MediaFile mediaFile = MediaFileFactory.create(file, mediaTimestampFormat);
        if (mediaFile != null) {
            MediaFileImportObject mediaFileImportObject = new MediaFileImportObject(mediaFile, mediaFile.getName());
            if (mediaFile.getStartTime() == 0) {
                mediaFileImportObject.setMissing(true);
            }
            mediaFileImportObject.determineIsNew();
            return mediaFileImportObject;
        }
        return null;
    }

    public MediaFileImportObject(MediaFile object, String name) {
        super(object, name);
    }

    @Override
    public VBox getDisplayPane() {
        return new VBox(UiConstants.SPACING, new Label(getObject().getName()));
    }

    @Override
    public void doImport() {
        // requires entity
    }

    @Override
    public void doImport(IEntity entity) {
        entity.getMediaCollection().addMediaFile(getObject());
//        DatabaseLogger.addMedia(getObject(), entity.getEntityId());
        setImported(true);
    }
}
