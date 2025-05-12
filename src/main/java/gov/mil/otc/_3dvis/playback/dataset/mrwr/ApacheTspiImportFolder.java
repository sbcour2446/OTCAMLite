package gov.mil.otc._3dvis.playback.dataset.mrwr;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvConfiguration;
import gov.mil.otc._3dvis.playback.ConfigurationManager;
import gov.mil.otc._3dvis.playback.dataset.csv.CsvConfigurationImportObject;
import gov.mil.otc._3dvis.playback.dataset.csv.CsvTspiImportFolder;

import java.io.File;

public class ApacheTspiImportFolder extends CsvTspiImportFolder {

    public ApacheTspiImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        if (csvConfiguration == null) {
            for (File file : files) {
                if (file.getName().equalsIgnoreCase(CsvConfiguration.NAME)) {
                    csvConfiguration = ConfigurationManager.load(file, CsvConfiguration.class);
                    break;
                }
            }
        }

        if (csvConfiguration == null) {
            csvConfiguration = new CsvConfiguration(0, "start field (optional)",
                    1, "timestamp format (YYYY-MM-dd HH:mm:ss.SSS)",
                    false, 2024);
            csvConfiguration.addFieldConfiguration(CsvConfiguration.Field.TIMESTAMP,
                    new CsvConfiguration.FieldConfiguration("header name", CsvConfiguration.ValueType.TIMESTAMP));
            String filename = getObject().getAbsolutePath() + File.separator + CsvConfiguration.NAME;
            ConfigurationManager.safeSave(csvConfiguration, filename);
            return false;
        }

        csvConfigurationImportObject = new CsvConfigurationImportObject(csvConfiguration);

        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.getName().toLowerCase().endsWith(".csv")) {
                ApacheTspiImportObject importObject = ApacheTspiImportObject.scanAndCreate(file, csvConfigurationImportObject.getObject());
                importObjectList.add(importObject);
            }
        }
        return true;
    }
}
