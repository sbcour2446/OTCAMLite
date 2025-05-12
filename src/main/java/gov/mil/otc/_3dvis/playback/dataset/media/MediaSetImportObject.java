package gov.mil.otc._3dvis.playback.dataset.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.media.MediaSet;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class MediaSetImportObject extends ImportObject<MediaSet> {

    public MediaSetImportObject(MediaSet object, String name) {
        super(object, name);
    }

    @Override
    public VBox getDisplayPane() {
        ListView<String> listView = new ListView<>();
        for (MediaFile mediaFile : getObject().getMediaFiles().values()) {
            String item = mediaFile.getName();
            listView.getItems().add(item);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        // requires entity
    }

    public void doImport(IEntity entity) {
        if (isMissing()) {
            return;
        }
        for (MediaFile mediaFile : getObject().getMediaFiles().values()) {
            entity.getMediaCollection().addMediaFile(mediaFile);
//            DatabaseLogger.addMedia(mediaFile, entity.getEntityId());
        }
    }
}
