package gov.mil.otc._3dvis.entity.tena;

import OTC.OTCAM.OtcamEntity.PublicationState;
import OTC.OTCAM.OtcamEntity.SDOpointer;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.tena.TenaUtility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OtcamEntity extends LvcEntity {

    private String militarySymbolReceived = "";

    protected OtcamEntity(EntityId entityId) {
        super(entityId);
    }

    protected OtcamEntity(LvcEntity lvcEntity) {
        super(lvcEntity);
    }

    protected OtcamEntity(PlaybackEntity playbackEntity, SDOpointer sdoPointer) {
        super(playbackEntity, sdoPointer);
    }

    public static void processDiscoveryEvent(PublicationState publicationState, SDOpointer sdoPointer) {
        EntityId entityId = EntityId.fromTena(publicationState.get_entityID());
        IEntity entity = EntityManager.getEntity(entityId);
        OtcamEntity otcamEntity;
        if (entity == null) {
            otcamEntity = new OtcamEntity(entityId);
            otcamEntity.sdoPointer = sdoPointer;
            otcamEntity.processDiscoveryEvent(publicationState);
            EntityManager.addEntity(otcamEntity, true);
        } else if (entity instanceof PlaybackEntity) {
            otcamEntity = new OtcamEntity((PlaybackEntity) entity, sdoPointer);
            otcamEntity.processDiscoveryEvent(publicationState);
            EntityManager.addEntity(otcamEntity, true);
        } else if (entity instanceof OtcamEntity) {
            otcamEntity = (OtcamEntity) entity;
            otcamEntity.processDiscoveryEvent(publicationState);
        } else {
            Logger.getGlobal().log(Level.WARNING, "Entity already created of a different type." +
                    " Need to implement something here.");
        }
    }

    public void processOtcamPublicationState(PublicationState publicationState) {
        EntityDetail entityDetail = getLastEntityDetail();
        EntityType entityType = EntityType.fromTenaType(publicationState.get_entityType());
        Affiliation affiliation = Affiliation.getEnum(publicationState.get_affiliation().ordinal());
        String name = getName(publicationState, entityDetail);
        String militarySymbol = getMilitarySymbol(publicationState, entityDetail, affiliation);
        int urn = getUrn(publicationState, entityDetail);
        RtcaState rtcaState = RtcaState.create(publicationState.get_rtcaState());
        String source = publicationState.get_identifier() + " - " + publicationState.get_sourceIdentifier();
        int milesPid = publicationState.is_milesPid_set() ? publicationState.get_milesPid().intValue() : 0;
        boolean outOfComms = publicationState.is_outOfComms_set() && publicationState.get_outOfComms();

        if (entityDetail == null
                || !entityDetail.getEntityType().equals(entityType)
                || !entityDetail.getAffiliation().equals(affiliation)
                || !entityDetail.getName().equals(name)
                || !entityDetail.getMilitarySymbol().equals(militarySymbol)
                || entityDetail.getUrn() != urn
                || !entityDetail.getRtcaState().equals(rtcaState)
                || !entityDetail.getSource().equals(source)
                || entityDetail.getMilesPid() != milesPid
                || entityDetail.isOutOfComms() != outOfComms) {
            long timestamp = publicationState.getMetadata().getTimeOfCommit() / TenaUtility.MILLI_NANO;
            EntityDetail newEntityDetail = new EntityDetail.Builder()
                    .setTimestamp(timestamp)
                    .setEntityType(entityType)
                    .setAffiliation(affiliation)
                    .setName(name)
                    .setMilitarySymbol(militarySymbol)
                    .setUrn(urn)
                    .setRtcaState(rtcaState)
                    .setSource(source)
                    .setMilesPid(milesPid)
                    .setOutOfComms(outOfComms)
                    .build();
            addEntityDetail(newEntityDetail);
            DataSource dataSource = DataManager.getOrCreateRealTimeDataSource();
            DatabaseLogger.addEntityDetail(newEntityDetail, getEntityId(), dataSource.getId());
        }

        processPublicationStateTspi(publicationState);
    }

    public String getMilitarySymbol(PublicationState publicationState, EntityDetail entityDetail, Affiliation affiliation) {
        String militarySymbol;
        if (publicationState.is_militarySymbol_set() && !publicationState.get_militarySymbol().equals(militarySymbolReceived)) {
            militarySymbolReceived = publicationState.get_militarySymbol();
            militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbolReceived, affiliation);
        } else if (entityDetail != null && entityDetail.getAffiliation().equals(affiliation)) {
            militarySymbol = entityDetail.getMilitarySymbol();
        } else {
            militarySymbol = MilitarySymbolUtility.getDefaultMilitarySymbol(affiliation);
            militarySymbolReceived = "";
        }
        return militarySymbol;
    }

    public int getUrn(PublicationState publicationState, EntityDetail entityDetail) {
        int urn;
        if (publicationState.is_urn_set()) {
            urn = publicationState.get_urn().intValue();
        } else if (entityDetail != null) {
            urn = entityDetail.getUrn();
        } else {
            urn = 0;
        }
        return urn;
    }

    @Override
    public void processPublicationState(TENA.LVC.Entity.PublicationState publicationState) {
        if (publicationState instanceof PublicationState) {
            processOtcamPublicationState((PublicationState) publicationState);
        } else {
            super.processPublicationState(publicationState);
        }
    }
}
