package gov.mil.otc._3dvis.playback.dataset.csv;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvConfiguration;
import gov.mil.otc._3dvis.data.file.delimited.csv.GenericTspiCsvFile;
import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.playback.dataset.tspi.TspiImportObject;

import java.io.File;

public class CsvTspiImportObject extends TspiImportObject {

    public static CsvTspiImportObject scanAndCreate(File file, CsvConfiguration csvConfiguration) {
        GenericTspiCsvFile genericTspiCsvFile = new GenericTspiCsvFile(file, csvConfiguration);
        CsvTspiImportObject importObject = new CsvTspiImportObject(genericTspiCsvFile);
        if (!genericTspiCsvFile.processFile()) {
            importObject.setMissing(true);
        }
        return importObject;
    }

    public CsvTspiImportObject(TspiCsvFile object) {
        super(object, object.getFile().getName());
    }
}
