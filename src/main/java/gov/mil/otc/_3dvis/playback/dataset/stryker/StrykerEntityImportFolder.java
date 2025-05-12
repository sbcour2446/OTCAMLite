package gov.mil.otc._3dvis.playback.dataset.stryker;

import gov.mil.otc._3dvis.data.otcam.OtcamUtility;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.manual.ManualDataImportFolder;
import gov.mil.otc._3dvis.playback.dataset.media.MediaImportFolder;
import gov.mil.otc._3dvis.playback.dataset.otcam.OtcamImportFolder;
import gov.mil.otc._3dvis.playback.dataset.otcam.OtcamImportObject;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StrykerEntityImportFolder extends ImportFolder {

    /* Directory Layout
        {entity folder}
        |  'media' //media folder set
        |  'manual_data' //media folder set
        |  'bft' //media folder set
     */

    public static StrykerEntityImportFolder scanAndCreate(File folder, OtcamImportFolder otcamImportFolder) {
        StrykerEntityImportFolder rpuasEntityDataSet = new StrykerEntityImportFolder(folder, otcamImportFolder);
        if (rpuasEntityDataSet.validateAndScan()) {
            rpuasEntityDataSet.setNew(rpuasEntityDataSet.determineIsNew(folder));
            return rpuasEntityDataSet;
        }
        return null;
    }

    private MediaImportFolder mediaImportFolder = null;
    private ManualDataImportFolder manualDataImportFolder = null;
    private final OtcamImportFolder otcamImportFolder;
    List<EntityId> entitiesFound = new ArrayList<>();

    private StrykerEntityImportFolder(File folder, OtcamImportFolder otcamImportFolder) {
        super(folder);
        this.otcamImportFolder = otcamImportFolder;
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (MediaImportFolder.isMediaFolder(file)) {
                mediaImportFolder = MediaImportFolder.scanAndCreate(file, MediaTimestampFormat.STANDARD);
            } else if (file.isDirectory() && file.getName().equalsIgnoreCase(ManualDataImportFolder.FOLDER_NAME)) {
                manualDataImportFolder = ManualDataImportFolder.scanAndCreate(file);
            }
        }

        for (IEntity entity : EntityManager.getEntities()) {
            if (entity.getName().equalsIgnoreCase(getName())) {
                if (!entitiesFound.contains(entity.getEntityId())) {
                    entitiesFound.add(entity.getEntityId());
                }
            }
        }
        for (OtcamImportObject otcamImportObject : otcamImportFolder.getOtcamImportObjectList()) {
            OtcamUtility otcamUtility = new OtcamUtility(otcamImportObject.getObject().getName());
            for (EntityId entityId : otcamUtility.getEntityIdsWithName(getName())) {
                if (!entitiesFound.contains(entityId)) {
                    entitiesFound.add(entityId);
                }
            }
        }
        return true;
    }

    @Override
    public VBox getDisplayPane() {
        ListView<String> listView = new ListView<>();

        String item;
        item = "Entities Found:";
        listView.getItems().add(item);
        if (entitiesFound.isEmpty()) {
            item = "  ***none***";
            listView.getItems().add(item);
        } else {
            for (EntityId entityId : entitiesFound) {
                item = String.format("  %s", entityId.toString());
                listView.getItems().add(item);
            }
        }

        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        if (mediaImportFolder != null) {
            treeItems.add(mediaImportFolder.getTreeItem());
        }
        if (manualDataImportFolder != null) {
            treeItems.add(manualDataImportFolder.getTreeItem());
        }
        return treeItems;
    }

    @Override
    public void doImport() {
        IEntity entity = getEntity(getName());

        if (entity == null) {
            return;
        }

        if (mediaImportFolder != null) {
            mediaImportFolder.importFolder(entity, importStatusLine);
        }

        if (manualDataImportFolder != null) {
            manualDataImportFolder.importFolder(entity, importStatusLine);
        }
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    private IEntity getEntity(String entityName) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entityName.equals(entity.getLastEntityDetail().getName())) {
                return entity;
            }
        }
        return null;
    }
}
