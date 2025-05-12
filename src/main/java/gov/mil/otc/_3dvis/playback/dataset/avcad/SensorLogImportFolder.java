package gov.mil.otc._3dvis.playback.dataset.avcad;

import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.project.avcad.AvcadConfiguration;

import java.io.File;

public class SensorLogImportFolder extends ImportFolder {

    /* Directory Layout
       'sensor_log'
       |  (log files)
     */

    public static String FOLDER_NAME = "sensor_log";

    public static SensorLogImportFolder scanAndCreate(File folder, AvcadConfiguration avcadConfiguration) {
        SensorLogImportFolder importFolder = new SensorLogImportFolder(folder, avcadConfiguration);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private final AvcadConfiguration avcadConfiguration;

    protected SensorLogImportFolder(File folder, AvcadConfiguration avcadConfiguration) {
        super(folder);
        this.avcadConfiguration = avcadConfiguration;
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.getName().toLowerCase().endsWith(".csv")) {
                SensorLogImportObject sensorLogImportObject = SensorLogImportObject.scanAndCreate(file, avcadConfiguration);
                if (sensorLogImportObject.isNew() || sensorLogImportObject.isModified()) {
                    setModified(true);
                }
                importObjectList.add(sensorLogImportObject);
            }
        }
        return true;
    }
}
