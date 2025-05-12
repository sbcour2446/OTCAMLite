package gov.mil.otc._3dvis.playback.dataset.p10;

import gov.mil.otc._3dvis.data.gps.P10DataLog;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.tspi.TspiImportObject;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class P10ImportFolder extends ImportFolder {

    /* Directory Layout
        'p10'
        |  'p10_config.csv
        |  {entity name}
        |  |  {p10 files}
    --OR--
        'p10' // entity provided
        |  {p10 files}
     */

    public static String FOLDER_NAME = "p10";

    public static P10ImportFolder scanAndCreate(File folder) {
        P10ImportFolder importFolder = new P10ImportFolder(folder);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private final List<TspiImportObject> tspiImportObjectList = new ArrayList<>();

    protected P10ImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }

            if (file.isDirectory()) {
                P10EntityImportFolder importFolder = P10EntityImportFolder.scanAndCreate(file);
                if (importFolder != null) {
                    if (importFolder.isNew() || importFolder.isModified()) {
                        setModified(true);
                    }
                    importFolderList.add(importFolder);
                }
            } else if (file.getName().toLowerCase().endsWith(".csv")) {
                if (file.getName().equalsIgnoreCase("p10_config.csv")) {
                    //todo process configuration file
                } else {
                    P10DataLog p10DataLog = new P10DataLog(file);
                    TspiImportObject tspiImportObject = new TspiImportObject(p10DataLog, file.getName());
                    if (tspiImportObject.determineIsNew()) {
                        setModified(true);
                        if (!p10DataLog.processFile() || p10DataLog.getTspiDataList().isEmpty()) {
                            tspiImportObject.setMissing(true);
                        }
                    }
                    tspiImportObjectList.add(tspiImportObject);
                }
            }
        }
        return true;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (TspiImportObject importObject : tspiImportObjectList) {
            listView.getItems().add(importObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport(IEntity entity) {
        for (TspiImportObject tspiImportObject : tspiImportObjectList) {
            tspiImportObject.importObject(entity, importStatusLine);
        }
    }
}
