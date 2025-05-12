package gov.mil.otc._3dvis.playback.dataset.tapets;

import gov.mil.otc._3dvis.data.tapets.TapetsConfiguration;
import gov.mil.otc._3dvis.data.tapets.TapetsConfigurationFile;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiCommon;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TapetsImportFolder extends ImportFolder {

    /* Directory Layout
        'tapets'
        |  {tapets folders/files}
     */

    public static final String FOLDER_NAME = "tapets";

    public static TapetsImportFolder scanAndCreate(File folder) {
        TapetsImportFolder tapetsDataSet = new TapetsImportFolder(folder);
        if (tapetsDataSet.validateAndScan()) {
            return tapetsDataSet;
        }
        return null;
    }

    private final Map<Integer, TapetsImportObject> tapetsImportObjectMap = new HashMap<>();

    public TapetsImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        scanTapetsConfigurationFile(files);
        scanBinFiles(files);

        if (tapetsImportObjectMap.isEmpty()) {
            setMissing(true);
        }

        for (TapetsImportObject tapetsImportObject : tapetsImportObjectMap.values()) {
            if (tapetsImportObject.getObject().isEmpty()) {
                tapetsImportObject.setMissing(true);
            }
        }

        return true;
    }

    private void scanTapetsConfigurationFile(File[] files) {
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.getName().equalsIgnoreCase("tapets_config.csv")) {
                TapetsConfigurationFile tapetsConfigurationFile = new TapetsConfigurationFile(file);
                tapetsConfigurationFile.processFile();
                for (Map.Entry<Integer, TapetsConfiguration> entry :
                        tapetsConfigurationFile.getTapetsConfigurationMap().entrySet()) {
                    TapetsConfigurationImportObject configuration = new TapetsConfigurationImportObject(entry.getValue());
                    tapetsImportObjectMap.put(entry.getKey(), new TapetsImportObject(entry.getKey(), configuration));
                }
                return;
            }
        }
    }

    private void scanBinFiles(File[] files) {
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles();
                if (subFiles != null) {
                    scanBinFiles(subFiles);
                }
            } else if (file.getName().toLowerCase().endsWith(".bin")) {
                TapetsLogFileImportObject tapetsLogFileImportObject = TapetsLogFileImportObject.scanAndCreate(file);
                int unitId = tapetsLogFileImportObject.getUnitId();
                TapetsImportObject tapetsImportObject = tapetsImportObjectMap.get(unitId);
                if (tapetsImportObject == null) {
                    tapetsImportObject = new TapetsImportObject(unitId, new TapetsConfigurationImportObject(unitId));
                    tapetsImportObjectMap.put(unitId, tapetsImportObject);
                }
                tapetsImportObject.addFile(tapetsLogFileImportObject);
                if (tapetsImportObject.isNew() || tapetsImportObject.isModified() || tapetsImportObject.isMissing()) {
                    setModified(true);
                }
            }
        }
    }

    @Override
    public VBox getDisplayPane() {
        VBox vBox = new VBox(UiConstants.SPACING, UiCommon.createTitleLabel("TAPETS Configuration"));
        vBox.getChildren().add(new TapetsConfigurationFileView(tapetsImportObjectMap));
        return vBox;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        for (TapetsImportObject tapetsImportObject : tapetsImportObjectMap.values()) {
            treeItems.add(new TreeItem<>(tapetsImportObject));
        }
        return treeItems;
    }

    @Override
    public void doImport() {
        for (TapetsImportObject tapetsImportObject : tapetsImportObjectMap.values()) {
            tapetsImportObject.importObject(importStatusLine);
        }
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }
}
