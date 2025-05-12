package gov.mil.otc._3dvis.entity.tena;

import TENA.LVC.Entity.PublicationState;
import TENA.LVC.Entity.SDOpointer;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.RtcaCommand;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.tena.TenaController;
import gov.mil.otc._3dvis.tena.TenaUtility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LvcEntity extends AbstractEntity {

    protected SDOpointer sdoPointer;

    protected LvcEntity(EntityId entityId) {
        super(entityId);
    }

    protected LvcEntity(LvcEntity lvcEntity) {
        super(lvcEntity);
        sdoPointer = lvcEntity.sdoPointer;
    }

    protected LvcEntity(PlaybackEntity playbackEntity, SDOpointer sdoPointer) {
        super(playbackEntity);
        this.sdoPointer = sdoPointer;
    }

    public static void processDiscoveryEvent(PublicationState publicationState, SDOpointer sdoPointer) {
        EntityId entityId = EntityId.fromTena(publicationState.get_entityID());
        IEntity entity = EntityManager.getEntity(entityId);
        LvcEntity lvcEntity;
        if (entity == null) {
            lvcEntity = new LvcEntity(entityId);
            lvcEntity.sdoPointer = sdoPointer;
            lvcEntity.processDiscoveryEvent(publicationState);
            EntityManager.addEntity(lvcEntity, true);
        } else if (entity instanceof PlaybackEntity) {
            lvcEntity = new LvcEntity((PlaybackEntity) entity, sdoPointer);
            lvcEntity.processDiscoveryEvent(publicationState);
            EntityManager.addEntity(lvcEntity, true);
        } else if (entity instanceof LvcEntity) {
            lvcEntity = (LvcEntity) entity;
            lvcEntity.processDiscoveryEvent(publicationState);
        } else {
            Logger.getGlobal().log(Level.WARNING, "Entity already created of a different type." +
                    " Need to implement something here.");
        }
    }

    public SDOpointer getSdoPointer() {
        return sdoPointer;
    }

    public void processDiscoveryEvent(PublicationState publicationState) {
        long timestamp = publicationState.getMetadata().getTimeOfDiscovery() / TenaUtility.MILLI_NANO;
        setRealtimeStart(timestamp);
        processPublicationState(publicationState);
    }

    public void processPublicationState(PublicationState publicationState) {
        EntityDetail entityDetail = getEntityDetail();
        EntityType entityType = EntityType.fromTenaType(publicationState.get_entityType());
        Affiliation affiliation = Affiliation.getEnum(publicationState.get_affiliation().ordinal());
        String name = getName(publicationState, entityDetail);
        RtcaState rtcaState = RtcaState.create(publicationState);
        String source = publicationState.get_identifier() + " - " + publicationState.get_sourceIdentifier();

        if (entityDetail == null
                || !entityDetail.getEntityType().equals(entityType)
                || !entityDetail.getAffiliation().equals(affiliation)
                || !entityDetail.getName().equals(name)
                || !entityDetail.getRtcaState().equals(rtcaState)
                || !entityDetail.getSource().equals(source)) {
            long timestamp = publicationState.get_tspi().get_time().get_nanosecondsSince1970() / TenaUtility.MILLI_NANO;
            String militarySymbol = MilitarySymbolUtility.getDefaultMilitarySymbol(affiliation);
            EntityDetail newEntityDetail = new EntityDetail.Builder()
                    .setTimestamp(timestamp)
                    .setEntityType(entityType)
                    .setAffiliation(affiliation)
                    .setName(name)
                    .setMilitarySymbol(militarySymbol)
                    .setRtcaState(rtcaState)
                    .setSource(source)
                    .build();
            addEntityDetail(newEntityDetail);
            DataSource dataSource = DataManager.getOrCreateRealTimeDataSource();
            DatabaseLogger.addEntityDetail(newEntityDetail, getEntityId(), dataSource.getId());
        }

        processPublicationStateTspi(publicationState);
    }

    protected void processPublicationStateTspi(PublicationState publicationState) {
        TspiData tspiData = TspiData.create(publicationState.get_tspi());
        if (tspiData != null) {
            addTspi(tspiData);
            DataSource dataSource = DataManager.getOrCreateRealTimeDataSource();
            DatabaseLogger.addTspiData(tspiData, getEntityId(), dataSource.getId());
        }
    }

    protected String getName(PublicationState publicationState, EntityDetail entityDetail) {
        String name;
        if (publicationState.is_entityMarking_set()) {
            name = publicationState.get_entityMarking();
        } else if (entityDetail != null) {
            name = entityDetail.getName();
        } else {
            name = null;
        }
        return name;
    }

    @Override
    public boolean supportsRtcaCommands() {
        return true;
    }

    @Override
    public void sendRtcaCommand(RtcaCommand rtcaCommand) {
        TenaController.sendRtcaCommand(rtcaCommand);
    }
}
