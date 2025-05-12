package gov.mil.otc._3dvis.playback.dataset.rpuas;

import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.project.rpuas.DeviceInformation;
import gov.mil.otc._3dvis.ui.UiCommon;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RpuasImportFolder extends ImportFolder {

    /* Directory Layout
        'rpuas'
        |  'missionConfigurations.json'
        |  {entity folders} //deviceId as folder name
        |  |  'bus' OR 'flight-logs'
        |  |  |  {files}
        |  |  'video' //media folder set
        |  |  'audio' //media folder set
        |  |  'tir' //tir folder set
     */

    public static final String FOLDER_NAME = "rpuas";

    public static RpuasImportFolder scanAndCreate(File folder) {
        RpuasImportFolder missionSetImportFolder = new RpuasImportFolder(folder);
        if (missionSetImportFolder.validateAndScan()) {
            return missionSetImportFolder;
        }
        return null;
    }

    private RpuasMissionConfigurationsImportObject rpuasMissionConfigurationsImportObject;

    public RpuasImportFolder(File folder) {
        super(folder);
        setExpandNode();
    }

    @Override
    protected boolean scanFiles(File[] files) {
        List<ImportFolder> foldersImporting = new ArrayList<>();
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.getName().equalsIgnoreCase("missionConfigurations.json")) {
                rpuasMissionConfigurationsImportObject = RpuasMissionConfigurationsImportObject.scanAndCreate(file);
                break;
            }
        }

        if (rpuasMissionConfigurationsImportObject == null ||
                rpuasMissionConfigurationsImportObject.getObject().getMissionConfigurationList().isEmpty()) {
            rpuasMissionConfigurationsImportObject = RpuasMissionConfigurationsImportObject.createTemplate(
                    getObject().getAbsolutePath(), "missionConfigurations.json");
            setMissing(true);
        }

        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (!file.isDirectory()) {
                continue;
            }

            DeviceInformation deviceInformation = rpuasMissionConfigurationsImportObject.getObject().getDeviceInformation(file.getName());
            if (deviceInformation != null) {
                RpuasEntityImportFolder importFolder = new RpuasEntityImportFolder(file,
                        rpuasMissionConfigurationsImportObject.getObject(), deviceInformation);
                importFolder.startScan(this);
                foldersImporting.add(importFolder);
            }
        }

        while (!foldersImporting.isEmpty() && !isCancelRequested()) {
            int i = 0;
            while (i < foldersImporting.size()) {
                if (foldersImporting.get(i).isScanning()) {
                    i++;
                } else {
                    foldersImporting.remove(i);
                }
            }
        }

        return true;
    }

    @Override
    public VBox getDisplayPane() {
        VBox vBox = new VBox(UiConstants.SPACING, UiCommon.createTitleLabel("Mission Configuration"));
        if (rpuasMissionConfigurationsImportObject != null) {
            vBox.getChildren().add(rpuasMissionConfigurationsImportObject.getDisplayPane());
        } else {
            vBox.getChildren().add(new Label("missing or invalid configuration file"));
        }
        return vBox;
    }

    @Override
    public void doImport() {
        super.doImport();
        if (rpuasMissionConfigurationsImportObject != null) {
            rpuasMissionConfigurationsImportObject.importObject(importStatusLine);
        }
    }
}
