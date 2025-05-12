package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.dlm.message.DlmMessage;

public interface IDlmEntity extends IEntity {

    boolean processMessage(DlmMessage dlmMessage, long messageTimeOverride);

    DlmDisplayManager getDlmDisplayManager();
}
