package gov.mil.otc._3dvis.playback.dataset;

import gov.mil.otc._3dvis.entity.base.IEntity;
import javafx.scene.layout.VBox;

public class NoImportObject extends ImportObject<String> {

    public NoImportObject(String object, String name) {
        super(object, name);
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        // not implemented
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }
}
