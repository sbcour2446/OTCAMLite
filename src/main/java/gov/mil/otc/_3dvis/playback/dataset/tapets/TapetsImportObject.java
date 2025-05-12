package gov.mil.otc._3dvis.playback.dataset.tapets;

import gov.mil.otc._3dvis.data.tapets.TapetsConfiguration;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class TapetsImportObject extends ImportObject<List<TapetsLogFileImportObject>> {

    private final int id;
    private final TapetsConfigurationImportObject tapetsConfigurationImportObject;
    private boolean hasImportedFile = false;

    public TapetsImportObject(int id, TapetsConfigurationImportObject tapetsConfigurationImportObject) {
        super(new ArrayList<>(), String.valueOf(id));
        this.tapetsConfigurationImportObject = tapetsConfigurationImportObject;
        this.id = id;
        if (tapetsConfigurationImportObject.isMissing()) {
            setMissing(true);
        }
    }

    public TapetsConfigurationImportObject getTapetsConfigurationImportObject() {
        return tapetsConfigurationImportObject;
    }

    public void addFile(TapetsLogFileImportObject tapetsImportObject) {
        if (!tapetsImportObject.isNew()) {
            hasImportedFile = true;
        } else {
            setModified(true);
        }
        getObject().add(tapetsImportObject);
    }

    @Override
    public boolean isNew() {
        return !hasImportedFile;
    }

    @Override
    public VBox getDisplayPane() {
        ImportObjectListView listView = new ImportObjectListView();
        for (TapetsLogFileImportObject tapetsImportObject : getObject()) {
            listView.getItems().add(tapetsImportObject);
        }
        return new VBox(UiConstants.SPACING, listView);
    }

    @Override
    public void doImport() {
        IEntity entity = getOrCreateEntity(id);
        if (tapetsConfigurationImportObject.getObject() != null) {
            addEntityDetails(entity, tapetsConfigurationImportObject.getObject());
        }
        for (TapetsLogFileImportObject tapetsImportObject : getObject()) {
            tapetsImportObject.doImport(entity);
        }
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    private IEntity getOrCreateEntity(int id) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity.getEntityId().getApplication() == Defaults.APP_ID_TAPETS
                    && entity.getEntityId().getId() == id) {
                return entity;
            }
        }

        EntityId entityId = new EntityId(Defaults.SITE_APP_ID_3DVIS, Defaults.APP_ID_TAPETS, id);
        IEntity entity = new PlaybackEntity(entityId);
        EntityManager.addEntity(entity, false);
        return entity;
    }

    private void addEntityDetails(IEntity entity, TapetsConfiguration tapetsConfiguration) {
        EntityType entityType = tapetsConfiguration.getEntityType();
        if (entityType == null) {
            entityType = EntityType.createUnknown();
        }
        Affiliation affiliation = tapetsConfiguration.getAffiliation();
        String militarySymbol = EntityTypeUtility.getTacticalSymbol(entityType);
        militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, affiliation);

        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(tapetsConfiguration.getStartTime())
                .setEntityType(entityType)
                .setAffiliation(tapetsConfiguration.getAffiliation())
                .setName(tapetsConfiguration.getName())
                .setSource("tapets_config")
                .setMilitarySymbol(militarySymbol)
                .setUrn(tapetsConfiguration.getUrn())
                .build();

        entity.addEntityDetail(entityDetail);
//        DataSource dataSource = DataManager.createDataSource("tapets_config", tapetsConfiguration.getStartTime(), -1);
//        DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), dataSource.getId());
    }
}
