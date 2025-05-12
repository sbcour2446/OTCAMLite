package gov.mil.otc._3dvis.data.tpsi;

import gov.mil.otc._3dvis.datamodel.TspiData;

import java.io.File;
import java.util.List;

public interface TspiDataFile {

    File getFile();
    List<TspiData> getTspiDataList();
}
