package gov.mil.otc._3dvis.ui.tools.eventtable;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.MunitionDetonationEvent;
import gov.mil.otc._3dvis.event.MunitionFireEvent;
import gov.mil.otc._3dvis.event.RtcaEvent;
import gov.mil.otc._3dvis.event.otcam.OtcamEvent;
import gov.nasa.worldwind.geom.Position;

public class RtcaEventView extends EventView {

    private String shooterName;
    private String shooterAffiliation;
    private Position shooterLocation;
    private String targetName;
    private String targetAffiliation;
    private Position targetLocation;
    private String munition;
    private String result;

    public RtcaEventView(RtcaEvent event) {
        super(event);

        if (event instanceof OtcamEvent) {
            processOtcamEvent((OtcamEvent) event);
        } else if (event instanceof MunitionFireEvent) {
            processMunitionFireEvent((MunitionFireEvent) event);
        } else if (event instanceof MunitionDetonationEvent) {
            processMunitionDetonationEvent((MunitionDetonationEvent) event);
        }
    }

    private void processOtcamEvent(OtcamEvent otcamEvent) {
        IEntity shooterEntity = EntityManager.getEntity(otcamEvent.getShooterId());
        if (shooterEntity != null) {
            EntityDetail entityDetail = shooterEntity.getEntityDetailBefore(otcamEvent.getTimestamp());
            if (entityDetail != null) {
                shooterName = entityDetail.getName();
                shooterAffiliation = entityDetail.getAffiliation().getName();
            } else {
                shooterName = otcamEvent.getShooterId().toString();
            }
            shooterLocation = shooterEntity.getPositionBefore(otcamEvent.getTimestamp());
        } else {
            shooterName = otcamEvent.getShooterId().toString();
        }

        IEntity targetEntity = EntityManager.getEntity(otcamEvent.getTargetId());
        if (targetEntity != null) {
            EntityDetail entityDetail = targetEntity.getEntityDetailBefore(otcamEvent.getTimestamp());
            if (entityDetail != null) {
                targetName = entityDetail.getName();
                targetAffiliation = entityDetail.getAffiliation().getName();
            } else {
                targetName = otcamEvent.getTargetId().toString();
            }
            targetLocation = targetEntity.getPositionBefore(otcamEvent.getTimestamp());
        } else {
            targetName = otcamEvent.getTargetId().toString();
        }

        munition = otcamEvent.getMunitionDescription();
        result = otcamEvent.getResults();
    }

    private void processMunitionFireEvent(MunitionFireEvent munitionFireEvent) {
        IEntity shooterEntity = EntityManager.getEntity(munitionFireEvent.getShooterId());
        if (shooterEntity != null) {
            EntityDetail entityDetail = shooterEntity.getEntityDetailBefore(munitionFireEvent.getTimestamp());
            if (entityDetail != null) {
                shooterName = entityDetail.getName();
                shooterAffiliation = entityDetail.getAffiliation().getName();
            } else {
                shooterName = munitionFireEvent.getShooterId().toString();
            }
            shooterLocation = shooterEntity.getPositionBefore(munitionFireEvent.getTimestamp());
        } else {
            shooterName = munitionFireEvent.getShooterId().toString();
        }

        if (munitionFireEvent.getTargetId() != null) {
            IEntity targetEntity = EntityManager.getEntity(munitionFireEvent.getTargetId());
            if (targetEntity != null) {
                EntityDetail entityDetail = targetEntity.getEntityDetailBefore(munitionFireEvent.getTimestamp());
                if (entityDetail != null) {
                    targetName = entityDetail.getName();
                    targetAffiliation = entityDetail.getAffiliation().getName();
                } else {
                    targetName = munitionFireEvent.getTargetId().toString();
                }
                targetLocation = targetEntity.getPositionBefore(munitionFireEvent.getTimestamp());
            } else {
                targetName = munitionFireEvent.getTargetId().toString();
            }
        }

        if (munitionFireEvent.getMunition() != null) {
            munition = munitionFireEvent.getMunition().getDescription();
        }
        result = "";
    }

    private void processMunitionDetonationEvent(MunitionDetonationEvent munitionDetonationEvent) {
        shooterName = "";
        shooterLocation = null;
        targetName = "";
        targetLocation = munitionDetonationEvent.getImpactPosition();
        munition = munitionDetonationEvent.getMunition().getDescription();
        result = "";
    }

    public String getShooterName() {
        return shooterName;
    }

    public String getShooterAffiliation() {
        return shooterAffiliation;
    }

    public Position getShooterLocation() {
        return shooterLocation;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetAffiliation() {
        return targetAffiliation;
    }

    public Position getTargetLocation() {
        return targetLocation;
    }

    public String getMunition() {
        return munition;
    }

    public String getResult() {
        return result;
    }
}
