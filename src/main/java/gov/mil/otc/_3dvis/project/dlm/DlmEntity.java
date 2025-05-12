package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.mil.otc._3dvis.entity.tena.LvcEntity;
import gov.mil.otc._3dvis.entity.tena.OtcamEntity;
import gov.mil.otc._3dvis.project.dlm.message.DlmMessage;
import gov.nasa.worldwind.geom.Position;

public class DlmEntity extends OtcamEntity implements IDlmEntity {

    private final DlmDisplayManager dlmDisplayManager = new DlmDisplayManager();

    public DlmEntity(EntityId entityId) {
        super(entityId);
    }

    public DlmEntity(LvcEntity lvcEntity) {
        super(lvcEntity);
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        boolean hasChange = super.update(time, entityFilter);

        Position position = getPosition();

        boolean isDisplayable = position != null && getEntityDetail() != null && !getEntityDetail().isOutOfComms() && isFiltered();

        dlmDisplayManager.update(time, getPosition(), isDisplayable);

        return hasChange;
    }

    @Override
    public boolean processMessage(DlmMessage dlmMessage, long messageTimeOverride) {
        return dlmDisplayManager.processMessage(dlmMessage, messageTimeOverride);
    }

    @Override
    public DlmDisplayManager getDlmDisplayManager() {
        return dlmDisplayManager;
    }

    @Override
    protected StatusAnnotation createStatusAnnotation() {
        return new DlmStatusAnnotation(this);
    }
}
