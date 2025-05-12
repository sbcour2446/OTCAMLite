package gov.mil.otc._3dvis.playback.dataset.avcad;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.p10.P10ImportFolder;
import gov.mil.otc._3dvis.project.avcad.AvcadConfiguration;
import gov.mil.otc._3dvis.project.avcad.SensorEntity;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SensorEntityImportFolder extends ImportFolder {

    /* Directory Layout
        (entity name)
        |  'p10'
        |  |  (p10 files)
        |  'sensor_log'
        |  |  (log files)
     */

    public static SensorEntityImportFolder scanAndCreate(File folder, AvcadConfiguration avcadConfiguration) {
        SensorEntityImportFolder importFolder = new SensorEntityImportFolder(folder, avcadConfiguration);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private final List<P10ImportFolder> p10ImportFolderList = new ArrayList<>();
    private final List<SensorLogImportFolder> sensorLogImportFolderList = new ArrayList<>();
    private final AvcadConfiguration avcadConfiguration;

    protected SensorEntityImportFolder(File folder, AvcadConfiguration avcadConfiguration) {
        super(folder);
        this.avcadConfiguration = avcadConfiguration;
    }

    @Override
    protected boolean scanFiles(File[] files) {
        boolean hasTspi = false;
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }
            if (file.getName().equalsIgnoreCase(P10ImportFolder.FOLDER_NAME)) {
                P10ImportFolder importFolder = P10ImportFolder.scanAndCreate(file);
                if (importFolder != null) {
                    if (importFolder.isNew() || importFolder.isModified()) {
                        setModified(true);
                    }
                    if (!importFolder.isMissing()) {
                        hasTspi = true;
                    }
                    p10ImportFolderList.add(importFolder);
                }
            } else if (file.getName().equalsIgnoreCase(SensorLogImportFolder.FOLDER_NAME)) {
                SensorLogImportFolder importFolder = SensorLogImportFolder.scanAndCreate(file, avcadConfiguration);
                if (importFolder != null) {
                    if (importFolder.isNew() || importFolder.isModified()) {
                        setModified(true);
                    }
                    sensorLogImportFolderList.add(importFolder);
                }
            }
        }

        if (!hasTspi) {
            setMissing(true);
        }

        return true;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        for (P10ImportFolder p10ImportFolder : p10ImportFolderList) {
            treeItems.add(p10ImportFolder.getTreeItem());
        }
        for (SensorLogImportFolder sensorLogImportFolder : sensorLogImportFolderList) {
            treeItems.add(sensorLogImportFolder.getTreeItem());
        }
        return treeItems;
    }

    ;

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (P10ImportFolder p10ImportFolder : p10ImportFolderList) {
            listView.getItems().add(p10ImportFolder);
        }
        for (SensorLogImportFolder sensorLogImportFolder : sensorLogImportFolderList) {
            listView.getItems().add(sensorLogImportFolder);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        IEntity entity = getOrCreateEntity();
        long startTime = 0;

        for (P10ImportFolder p10ImportFolder : p10ImportFolderList) {
            p10ImportFolder.importFolder(entity, importStatusLine);
        }

        for (SensorLogImportFolder sensorLogImportFolder : sensorLogImportFolderList) {
            sensorLogImportFolder.importFolder(entity, importStatusLine);
        }

        addEntityDetails(entity, startTime);
    }

    private SensorEntity getOrCreateEntity() {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof SensorEntity playbackEntity &&
                    entity.getEntityId().getApplication() == Defaults.APP_ID_AVCAD &&
                    getName().equals(entity.getLastEntityDetail().getName())) {
                return playbackEntity;
            }
        }

        EntityId entityId = DataManager.getNextGenericEntityId();
        SensorEntity entity = new SensorEntity(entityId, getName().toLowerCase().contains("ra"));
        EntityManager.addEntity(entity, false);
        return entity;
    }

    private void addEntityDetails(IEntity entity, long startTime) {
//        DataSource dataSource = DataManager.createDataSource(getObject().getAbsolutePath(), startTime, -1);

        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(startTime)
                .setName(getName())
                .setSource("AVCAD")
                .setAffiliation(Affiliation.FRIENDLY)
                .build();
        entity.addEntityDetail(entityDetail);
//        DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), dataSource.getId());
    }
}
