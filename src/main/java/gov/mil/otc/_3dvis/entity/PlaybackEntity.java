package gov.mil.otc._3dvis.entity;

import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;

public class PlaybackEntity extends AbstractEntity {

    public PlaybackEntity(EntityId entityId) {
        super(entityId);
    }

    protected PlaybackEntity(AbstractEntity abstractEntity) {
        super(abstractEntity);
    }

    @Override
    public boolean supportsRtcaCommands() {
        return false;
    }

    @Override
    public void sendRtcaCommand(RtcaCommand rtcaCommand) {
        //not supported
    }
}
