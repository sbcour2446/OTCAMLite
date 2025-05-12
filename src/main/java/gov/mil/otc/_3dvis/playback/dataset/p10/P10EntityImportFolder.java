package gov.mil.otc._3dvis.playback.dataset.p10;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.gps.P10DataLog;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import gov.mil.otc._3dvis.playback.dataset.tspi.TspiImportObject;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class P10EntityImportFolder extends ImportFolder {

    /* Directory Layout
        {entity name}
        |  {p10 files}
     */

    public static P10EntityImportFolder scanAndCreate(File folder) {
        P10EntityImportFolder importFolder = new P10EntityImportFolder(folder);
        if (importFolder.validateAndScan()) {
            return importFolder;
        }
        return null;
    }

    private final List<TspiImportObject> tspiImportObjectList = new ArrayList<>();

    protected P10EntityImportFolder(File folder) {
        super(folder);
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (isCancelRequested()) {
                break;
            }

            if (file.getName().toLowerCase().endsWith(".csv")) {
                P10DataLog p10DataLog = new P10DataLog(file);
                TspiImportObject tspiImportObject = new TspiImportObject(p10DataLog, file.getName());
                if (!tspiImportObject.determineIsNew()) {
                    setModified(true);
                    if (!p10DataLog.processFile() || p10DataLog.getTspiDataList().isEmpty()) {
                        tspiImportObject.setMissing(true);
                    }
                }
                tspiImportObjectList.add(tspiImportObject);
            }
        }

        if (tspiImportObjectList.isEmpty()) {
            setMissing(true);
        }

        return true;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        return List.of();
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (TspiImportObject tspiImportObject : tspiImportObjectList) {
            listView.getItems().add(tspiImportObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        IEntity entity = getOrCreateEntity(getName());

        for (TspiImportObject tspiImportObject : tspiImportObjectList) {
            tspiImportObject.importObject(entity, importStatusLine);
        }
    }

    private IEntity getOrCreateEntity(String entityName) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof PlaybackEntity playbackEntity &&
                    entity.getEntityId().getApplication() == Defaults.APP_ID_P10 &&
                    entityName.equals(entity.getLastEntityDetail().getName())) {
                return playbackEntity;
            }
        }

        EntityId entityId = DataManager.getNextGenericEntityId();
        PlaybackEntity entity = new PlaybackEntity(entityId);
        EntityManager.addEntity(entity, false);
        return entity;
    }

    private void addEntityDetails(IEntity entity, long startTime) {
        DataSource dataSource = DataManager.createDataSource(getObject().getAbsolutePath(), startTime, -1);

        EntityType entityType = new EntityType(1, 2, 225, 50, 0, 0, 0);
        String militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, Affiliation.FRIENDLY);

        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(startTime)
                .setName(getName())
                .setSource("RPUAS")
                .setEntityType(entityType)
                .setMilitarySymbol(militarySymbol)
                .setAffiliation(Affiliation.FRIENDLY)
                .build();
        entity.addEntityDetail(entityDetail);
//        DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), dataSource.getId());

        EntityScope entityScope = new EntityScope(startTime);
        entity.addEntityScope(entityScope);
//        DatabaseLogger.addEntityScope(entityScope, entity.getEntityId(), dataSource.getId());
    }
}
