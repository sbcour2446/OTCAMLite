package gov.mil.otc._3dvis.data.database;

import gov.mil.otc._3dvis.entity.base.EntityId;

public class DatabaseObject<T> {

    private final T object;
    private final EntityId entityId;
    private final int sourceId;

    public DatabaseObject(T object, EntityId entityId) {
        this(object, entityId, -1);
    }

    public DatabaseObject(T object, int sourceId) {
        this(object, null, sourceId);
    }

    public DatabaseObject(T object, EntityId entityId, int sourceId) {
        this.object = object;
        this.entityId = entityId;
        this.sourceId = sourceId;
    }

    public T getObject() {
        return object;
    }

    public EntityId getEntityId() {
        return entityId;
    }

    public int getSourceId() {
        return sourceId;
    }
}
