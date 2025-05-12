package gov.mil.otc._3dvis.playback.dataset.avcad;

import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.project.avcad.AvcadConfiguration;

import java.io.File;

public class SensorImportFolder extends ImportFolder {

    /* Directory Layout
        'sensor'
        |  (entity name)
        |  |  'p10'
        |  |  |  (p10 files)
        |  |  'sensor_log'
        |  |  |  (log files)
     */

    public static String FOLDER_NAME = "sensor";

    public static SensorImportFolder scanAndCreate(File folder, AvcadConfiguration avcadConfiguration) {
        SensorImportFolder importFolder = new SensorImportFolder(folder, avcadConfiguration);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private final AvcadConfiguration avcadConfiguration;

    protected SensorImportFolder(File folder, AvcadConfiguration avcadConfiguration) {
        super(folder);
        setExpandNode();
        this.avcadConfiguration = avcadConfiguration;
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }

            SensorEntityImportFolder sensorEntityImportFolder = SensorEntityImportFolder.scanAndCreate(file, avcadConfiguration);
            if (sensorEntityImportFolder != null) {
                if (sensorEntityImportFolder.isNew() || sensorEntityImportFolder.isModified()) {
                    setModified(true);
                }
                importFolderList.add(sensorEntityImportFolder);
            }
        }
        return true;
    }
}
