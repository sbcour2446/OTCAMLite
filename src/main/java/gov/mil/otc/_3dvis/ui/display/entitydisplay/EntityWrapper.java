package gov.mil.otc._3dvis.ui.display.entitydisplay;

import gov.mil.otc._3dvis.entity.base.IEntity;

final class EntityWrapper {

    private final IEntity entity;
    private EntityGroup currentGroup = null;

    EntityWrapper(IEntity entity) {
        this.entity = entity;
    }

    public EntityGroup getCurrentGroup() {
        return currentGroup;
    }

    public void removeCurrentGroup() {
        if (currentGroup != null) {
            currentGroup.removeEntity(entity);
        }
    }

    public void setCurrentGroup(EntityGroup currentGroup) {
        this.currentGroup = currentGroup;
        if (currentGroup != null) {
            currentGroup.addEntity(entity);
        }
    }

    @Override
    public String toString() {
        if (entity == null) {
            return "All";
        } else {
            return entity.getName() + " (" + entity.getEntityId() + ")";
        }
    }
}
