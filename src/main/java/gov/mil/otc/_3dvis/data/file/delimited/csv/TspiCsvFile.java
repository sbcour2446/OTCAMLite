package gov.mil.otc._3dvis.data.file.delimited.csv;

import gov.mil.otc._3dvis.data.tpsi.TspiDataFile;
import gov.mil.otc._3dvis.datamodel.TspiData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class TspiCsvFile extends CsvFile implements TspiDataFile {

    protected final List<TspiData> tspiDataList = new ArrayList<>();

    protected TspiCsvFile(File file) {
        super(file);
    }

    protected TspiCsvFile(File file, int headerLineNumber) {
        super(file, headerLineNumber);
    }

    @Override
    public List<TspiData> getTspiDataList() {
        return tspiDataList;
    }
}
