package gov.mil.otc._3dvis.playback.dataset.manual;

import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.tir.TirManager;
import gov.mil.otc._3dvis.tir.TirReader;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.layout.VBox;

import java.io.File;

public class TirImportObject extends ImportObject<TimedFile> {

    public static TirImportObject scanAndCreate(File file) {
        TimedFile timedFile = TirReader.processFile(file);
        TirImportObject tirImportObject = new TirImportObject(timedFile, file.getName());
        if (timedFile.getTimestamp() == 0) {
            tirImportObject.setMissing(true);
        }
        return tirImportObject;
    }

    public TirImportObject(TimedFile object, String name) {
        super(object, name);
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        TirManager.addTir(getObject());
    }

    @Override
    public void doImport(IEntity entity) {
        entity.addTir(getObject());
    }
}
