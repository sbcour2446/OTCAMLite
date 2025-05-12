package gov.mil.otc._3dvis.playback.dataset.p10;

import gov.mil.otc._3dvis.data.gps.P10DataLog;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.layout.VBox;

import java.io.File;

public class P10ImportObject extends ImportObject<P10DataLog> {

    public static P10ImportObject scanAndCreate(File file) {
        P10DataLog p10DataLog = new P10DataLog(file);
        P10ImportObject importObject = new P10ImportObject(p10DataLog);
        if (!p10DataLog.processFile()) {
            importObject.setMissing(true);
        }
        importObject.determineIsNew();
        return importObject;
    }

    public P10ImportObject(P10DataLog object) {
        super(object, object.getFile().getName());
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
