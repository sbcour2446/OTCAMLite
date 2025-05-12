package gov.mil.otc._3dvis.playback.dataset.mrwr;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvConfiguration;
import gov.mil.otc._3dvis.data.file.delimited.csv.GenericTspiCsvFile;
import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.csv.CsvTspiImportObject;
import gov.mil.otc._3dvis.project.mrwr.ApacheEntity;
import gov.mil.otc._3dvis.project.mrwr.ApacheTspiFile;

import java.io.File;

public class ApacheTspiImportObject extends CsvTspiImportObject {

    public static ApacheTspiImportObject scanAndCreate(File file, CsvConfiguration csvConfiguration) {
        ApacheTspiFile apacheTspiFile = new ApacheTspiFile(file, csvConfiguration);
        ApacheTspiImportObject importObject = new ApacheTspiImportObject(apacheTspiFile);
        if (!apacheTspiFile.processFile()) {
            importObject.setMissing(true);
        }
        return importObject;
    }

    public ApacheTspiImportObject(TspiCsvFile object) {
        super(object);
    }

    @Override
    public void doImport(IEntity entity) {
        super.doImport(entity);
        if (getObject() instanceof ApacheTspiFile apacheTspiFile && entity instanceof ApacheEntity apacheEntity) {
            apacheEntity.addTadStatuses(apacheTspiFile.getTadStatusList());
        }
    }
}
