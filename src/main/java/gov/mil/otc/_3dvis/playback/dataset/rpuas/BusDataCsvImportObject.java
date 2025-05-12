package gov.mil.otc._3dvis.playback.dataset.rpuas;

import gov.mil.otc._3dvis.data.tpsi.TspiDataFile;
import gov.mil.otc._3dvis.playback.dataset.tspi.TspiImportObject;
import gov.mil.otc._3dvis.project.rpuas.BusDataCsv;

public class BusDataCsvImportObject extends TspiImportObject {

    public BusDataCsvImportObject(TspiDataFile object, String name) {
        super(object, name);
    }

    public BusDataCsv getBusDataCsv() {
        if (getObject() instanceof BusDataCsv busDataCsv) {
            return busDataCsv;
        }
        return null;
    }
}
