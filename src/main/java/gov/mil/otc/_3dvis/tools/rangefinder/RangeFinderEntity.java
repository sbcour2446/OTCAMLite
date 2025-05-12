package gov.mil.otc._3dvis.tools.rangefinder;

import gov.mil.otc._3dvis.entity.base.EntityId;

import java.util.Objects;

public class RangeFinderEntity {

    public static final RangeFinderEntity ALL = new RangeFinderEntity();
    private final EntityId entityId;
    private final boolean isAll;

    private RangeFinderEntity() {
        entityId = null;
        isAll = true;
    }

    public RangeFinderEntity(EntityId entityId) {
        this.entityId = entityId;
        this.isAll = false;
    }

    public EntityId getEntityId() {
        return entityId;
    }

    public boolean isAll() {
        return isAll;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RangeFinderEntity rangeFinderEntity = (RangeFinderEntity) o;
        if (isAll && rangeFinderEntity.isAll) {
            return true;
        } else if (entityId != null) {
            return entityId.equals(rangeFinderEntity.entityId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId, isAll);
    }

    @Override
    public String toString() {
        if (isAll) {
            return "ALL";
        } else if (entityId == null){
            return "not set";
        } else {
            return entityId.toString();
        }
    }
}
