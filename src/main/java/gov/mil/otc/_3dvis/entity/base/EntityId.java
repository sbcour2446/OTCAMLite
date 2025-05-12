package gov.mil.otc._3dvis.entity.base;

import java.util.Objects;

public class EntityId implements Comparable<EntityId> {

    public static final EntityId ENTITY_ID_UNKNOWN = new EntityId(-1, -1, -1);
    private final int site;
    private final int application;
    private final int id;

    public EntityId(int site, int application, int id) {
        this.site = site;
        this.application = application;
        this.id = id;
    }

    public static EntityId fromTena(TENA.LVC.EntityID.ImmutableLocalClass entityId) {
        if (entityId != null) {
            return new EntityId(
                    entityId.get_siteID().intValue(),
                    entityId.get_applicationID().intValue(),
                    entityId.get_objectID().intValue());
        } else {
            return new EntityId(0,0,0);
        }
    }

    public TENA.LVC.EntityID.ImmutableLocalClass toTenaEntityId() {
        return TENA.LVC.EntityID.LocalClass.create(
                TENA.UnsignedShort.valueOf(site),
                TENA.UnsignedShort.valueOf(application),
                TENA.UnsignedShort.valueOf(id));
    }

    public int getSite() {
        return site;
    }

    public int getApplication() {
        return application;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityId entityId = (EntityId) o;
        return site == entityId.site && application == entityId.application && id == entityId.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(site, application, id);
    }

    @Override
    public String toString() {
        return String.format("%d:%d:%d", site, application, id);
    }

    @Override
    public int compareTo(EntityId o) {
        if (site > o.site) {
            return 1;
        } else if (site < o.site) {
            return -1;
        } else if (application > o.application) {
            return 1;
        } else if (application < o.application) {
            return -1;
        } else if (id > o.id) {
            return 1;
        } else if (id < o.id) {
            return -1;
        } else {
            return 0;
        }
    }
}
