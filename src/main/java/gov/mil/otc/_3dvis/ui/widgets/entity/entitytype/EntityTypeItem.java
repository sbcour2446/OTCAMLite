package gov.mil.otc._3dvis.ui.widgets.entity.entitytype;

import gov.mil.otc._3dvis.datamodel.EntityType;

final class EntityTypeItem {
    private final EntityType entityType;
    private final String display;

    public EntityTypeItem(String displayOnly) {
        entityType = null;
        display = displayOnly;
    }

    public EntityTypeItem(EntityType entityType, String description) {
        this.entityType = entityType;
        display = entityType.toString() + " " + description;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public String toString() {
        return display;
    }
}
