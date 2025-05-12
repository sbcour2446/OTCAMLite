package gov.mil.otc._3dvis.playback.dataset.entity;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityConfiguration;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportFolder;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.csv.CsvTspiImportFolder;
import gov.mil.otc._3dvis.playback.dataset.media.MediaImportFolder;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import gov.nasa.worldwind.geom.Position;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.VBox;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityImportFolder extends ImportFolder {

    /* Directory Layout
        {entity name}
        |  'tspi_data_csv' //1553 data files, csv
        |  'video' //media folder set
        //extend for custom data sets
     */

    protected final EntityConfigurationImportObject entityConfigurationImportObject;
    protected CsvTspiImportFolder csvTspiImportFolder = null;
    protected MediaImportFolder mediaImportFolder = null;

    public EntityImportFolder(File folder, EntityConfigurationImportObject entityConfigurationImportObject) {
        super(folder);
        setExpandNode();
        this.entityConfigurationImportObject = entityConfigurationImportObject;

        if (this.entityConfigurationImportObject == null) {
            setMissing(true);
        }
    }

    @Override
    protected boolean scanFiles(File[] files) {
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(CsvTspiImportFolder.FOLDER_NAME)) {
                csvTspiImportFolder = new CsvTspiImportFolder(file);
                csvTspiImportFolder.validateAndScan(this);
            } else if (MediaImportFolder.isMediaFolder(file)) {
                mediaImportFolder = new MediaImportFolder(file, MediaTimestampFormat.DAY_OF_YEAR_MILLI);
                mediaImportFolder.validateAndScan(this);
            }
        }

        if (entityConfigurationImportObject == null || csvTspiImportFolder == null) {
            setMissing(true);
        }

        return true;
    }

    @Override
    protected List<TreeItem<ImportObject<?>>> getChildTreeItems() {
        List<TreeItem<ImportObject<?>>> treeItems = new ArrayList<>();
        if (csvTspiImportFolder != null) {
            treeItems.add(csvTspiImportFolder.getTreeItem());
        }
        if (mediaImportFolder != null) {
            treeItems.add(mediaImportFolder.getTreeItem());
        }
        return treeItems;
    }

    @Override
    public VBox getDisplayPane() {
        if (entityConfigurationImportObject != null) {
            return entityConfigurationImportObject.getDisplayPane();
        } else {
            return new VBox(UiConstants.SPACING, new Label("no configuration"));
        }
    }

    @Override
    public void doImport() {
        IEntity entity = getOrCreateEntity(getName(), PlaybackEntity.class);

        csvTspiImportFolder.importFolder(entity, importStatusLine);
        long startTime = csvTspiImportFolder.getFirstTimestamp();
        long stopTime = csvTspiImportFolder.getLastTimestamp();

        EntityConfiguration entityConfiguration = entityConfigurationImportObject.getObject();

        EntityType entityType = entityConfiguration.getEntityType();
        if (entityType == null) {
            entityType = EntityType.createUnknown();
        }

        Affiliation affiliation = entityConfiguration.getAffiliation();

        String militarySymbol = entityConfiguration.getMilitarySymbol();
        if (militarySymbol.isBlank()) {
            militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
        }
        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, affiliation);

        entity.addEntityDetail(new EntityDetail.Builder()
                .setTimestamp(startTime)
                .setName(getName())
                .setSource("MRWR")
                .setAffiliation(affiliation)
                .setEntityType(entityType)
                .setMilitarySymbol(militarySymbol)
                .setUrn(entityConfiguration.getUrn())
                .build());

        entity.addEntityScope(new EntityScope(startTime, stopTime));

        if (mediaImportFolder != null) {
            mediaImportFolder.importFolder(entity, importStatusLine);
        }

        setImported(true);
    }

    protected IEntity getOrCreateEntity(String entityName, Class<? extends IEntity> clazz) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity.getClass() == clazz &&
                    entity.getLastEntityDetail() != null &&
                    entityName.equals(entity.getLastEntityDetail().getName())) {
                return entity;
            }
        }

        EntityId entityId = DataManager.getNextGenericEntityId();
        try {
            Constructor<? extends IEntity> constructor = clazz.getConstructor(EntityId.class);
            IEntity entity = constructor.newInstance(entityId);
            EntityManager.addEntity(entity, false);
            return entity;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "EntityImportFolder:getOrCreateEntity", e);
            return null;
        }
    }

    protected void addManualPositions(IEntity entity) {
        EntityConfiguration entityConfiguration = entityConfigurationImportObject.getObject();
        if (entityConfiguration == null || entityConfiguration.getManualPositionList() == null) {
            return;
        }

        for (EntityConfiguration.ManualPosition manualPosition : entityConfiguration.getManualPositionList()) {
            entity.addTspi(new TspiData(manualPosition.getTimestamp(),
                    Position.fromDegrees(manualPosition.getLatitude(), manualPosition.getLongitude())));
        }
    }

    protected void addEntityScopes(IEntity entity) {
        EntityConfiguration entityConfiguration = entityConfigurationImportObject.getObject();
        if (entityConfiguration == null) {
            return;
        }

        if (entityConfiguration.getEntityScopeList() == null || entityConfiguration.getEntityScopeList().isEmpty()) {
            entity.addEntityScope(new EntityScope(entityConfiguration.getStartTime(), entityConfiguration.getStopTime()));
        } else {
            for (EntityScope entityScope : entityConfiguration.getEntityScopeList()) {
                entity.addEntityScope(entityScope);
            }
        }
    }
}
