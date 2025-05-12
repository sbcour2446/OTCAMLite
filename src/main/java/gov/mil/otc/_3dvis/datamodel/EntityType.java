package gov.mil.otc._3dvis.datamodel;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityType implements Comparable<EntityType> {

    public enum Kind {

        UNKNOWN,
        PLATFORM,
        MUNITION,
        LIFE_FORM,
        ENVIRONMENTAL,
        CULTURAL_FEATURE,
        SUPPLY,
        RADIO,
        EXPENDABLE,
        SENSOR_EMITTER
    }

    private final int kind;
    private final int domain;
    private final int country;
    private final int category;
    private final int subcategory;
    private final int specific;
    private final int extra;
    private String description;

    public EntityType(int kind, int domain, int country, int category, int subcategory, int specific, int extra) {
        this.kind = kind;
        this.domain = domain;
        this.country = country;
        this.category = category;
        this.subcategory = subcategory;
        this.specific = specific;
        this.extra = extra;
        this.description = getDescription();
    }

    public EntityType(EntityType entityType) {
        this.kind = entityType.kind;
        this.domain = entityType.domain;
        this.country = entityType.country;
        this.category = entityType.category;
        this.subcategory = entityType.subcategory;
        this.specific = entityType.specific;
        this.extra = entityType.extra;
        this.description = entityType.description;
    }

    public static EntityType createUnknown() {
        return new EntityType(0, 0, 0, 0, 0, 0, 0);
    }

    public boolean isUnknown() {
        return kind == 0 && domain == 0 && country == 0 && category == 0 && subcategory == 0 && specific == 0 && extra == 0;
    }

    public int getKind() {
        return kind;
    }

    public int getDomain() {
        return domain;
    }

    public int getCountry() {
        return country;
    }

    public int getCategory() {
        return category;
    }

    public int getSubcategory() {
        return subcategory;
    }

    public int getSpecific() {
        return specific;
    }

    public int getExtra() {
        return extra;
    }

    public String getDescription() {
        if (description == null) {
            description = EntityTypeUtility.getDescription(this);
        }
        return description;
    }

    /**
     * Creates a EntityType from a string (*.*.*.*.*.*.*).
     *
     * @param entityTypeString The DIS Entity Type string.
     * @return The DIS Entity Type object, null if unsuccessful.
     */
    public static EntityType fromString(String entityTypeString) {
        if (entityTypeString != null) {
            try {
                String[] fields = entityTypeString.split("\\.");
                if (fields.length == 7) {
                    int kind = Integer.parseInt(fields[0]);
                    int domain = Integer.parseInt(fields[1]);
                    int country = Integer.parseInt(fields[2]);
                    int category = Integer.parseInt(fields[3]);
                    int subcategory = Integer.parseInt(fields[4]);
                    int specific = Integer.parseInt(fields[5]);
                    int extra = Integer.parseInt(fields[6]);
                    return new EntityType(kind, domain, country, category, subcategory, specific, extra);
                }
            } catch (NumberFormatException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
        return null;
    }

    public static EntityType fromTenaType(TENA.LVC.EntityType.ImmutableLocalClass entityType) {
        return new EntityType(
                entityType.get_kind().intValue(),
                entityType.get_domain().intValue(),
                entityType.get_country().intValue(),
                entityType.get_category().intValue(),
                entityType.get_subcategory().intValue(),
                entityType.get_specific().intValue(),
                entityType.get_extra().intValue());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityType entityType = (EntityType) o;
        return kind == entityType.kind
                && domain == entityType.domain
                && country == entityType.country
                && category == entityType.category
                && subcategory == entityType.subcategory
                && specific == entityType.specific
                && extra == entityType.extra;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, domain, country, category, subcategory, specific, extra);
    }

    @Override
    public String toString() {
        String results = "";
        results += this.kind;
        results += ".";
        results += this.domain;
        results += ".";
        results += this.country;
        results += ".";
        results += this.category;
        results += ".";
        results += this.subcategory;
        results += ".";
        results += this.specific;
        results += ".";
        results += this.extra;
        return results;
    }

    @Override
    public int compareTo(EntityType o) {
        if (kind > o.kind) {
            return 1;
        } else if (kind < o.kind) {
            return -1;
        } else if (domain > o.domain) {
            return 1;
        } else if (domain < o.domain) {
            return -1;
        } else if (country > o.country) {
            return 1;
        } else if (country < o.country) {
            return -1;
        } else if (category > o.category) {
            return 1;
        } else if (category < o.category) {
            return -1;
        } else if (subcategory > o.subcategory) {
            return 1;
        } else if (subcategory < o.subcategory) {
            return -1;
        } else if (specific > o.specific) {
            return 1;
        } else if (specific < o.specific) {
            return -1;
        } else if (extra > o.extra) {
            return 1;
        } else if (extra < o.extra) {
            return -1;
        } else {
            return 0;
        }
    }
}
