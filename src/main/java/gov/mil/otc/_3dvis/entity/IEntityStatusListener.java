package gov.mil.otc._3dvis.entity;

import gov.mil.otc._3dvis.entity.base.IEntity;

public interface IEntityStatusListener {

    void onStatusChange(IEntity entity);
}
