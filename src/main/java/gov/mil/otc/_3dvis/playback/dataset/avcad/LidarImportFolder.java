package gov.mil.otc._3dvis.playback.dataset.avcad;

import gov.mil.otc._3dvis.playback.dataset.ImportFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LidarImportFolder extends ImportFolder {

    /* Directory Layout
        'lidar'
        |  (lidar xml files)
     */

    public static String FOLDER_NAME = "lidar";

    public static LidarImportFolder scanAndCreate(File folder) {
        LidarImportFolder importFolder = new LidarImportFolder(folder);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    protected LidarImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.isDirectory()) {
                scanForImportObjects(file);
            } else if (file.getName().toLowerCase().endsWith(".kml")) {
                LidarKmlFileImportObject importObject = LidarKmlFileImportObject.scanAndCreate(file);
                importObjectList.add(importObject);
            }
        }
        return true;
    }

    private void scanForImportObjects(File folder) {
        File[] fileList = folder.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (isCancelRequested()) {
                    break;
                }
                if (file.isDirectory()) {
                    scanForImportObjects(file);
                } else if (file.getName().toLowerCase().endsWith(".kml")) {
                    LidarKmlFileImportObject importObject = LidarKmlFileImportObject.scanAndCreate(file);
                    importObjectList.add(importObject);
                }
            }
        }
    }
}
