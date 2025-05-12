package gov.mil.otc._3dvis.playback.dataset.rpuas;

import gov.mil.otc._3dvis.data.tpsi.TspiDataFile;
import gov.mil.otc._3dvis.data.ulog.ULogFile;
import gov.mil.otc._3dvis.playback.dataset.tspi.TspiImportObject;

public class ULogImportObject extends TspiImportObject {

    public ULogImportObject(TspiDataFile object, String name) {
        super(object, name);
    }

    public ULogFile getULogFile() {
        if (getObject() instanceof ULogFile uLogFile) {
            return uLogFile;
        }
        return null;
    }
}
