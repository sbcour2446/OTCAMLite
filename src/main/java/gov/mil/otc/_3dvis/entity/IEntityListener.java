package gov.mil.otc._3dvis.entity;

import gov.mil.otc._3dvis.entity.base.IEntity;

public interface IEntityListener {

    void onEntityAdded(IEntity entity);

    void onEntityUpdated(IEntity entity);

    void onEntityDisposed(IEntity entity);
}
