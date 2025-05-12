package gov.mil.otc._3dvis.data.gps;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.utility.Utility;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityConfiguration {

    private static final String TIME_FORMAT = "yyyyMMddHHmmss";

    public static EntityConfiguration create(String startTimeString, String idString, String name, String description,
                                             String urnString, String affiliationString, String entityTypeString) {
        try {
            long startTime = 0;
            if (!startTimeString.isBlank()) {
                startTime = Utility.parseTime(startTimeString, TIME_FORMAT);
            }
            int id = 0;
            if (!idString.isBlank()) {
                id = Integer.parseInt(idString);
            }
            int urn = 0;
            if (!urnString.isBlank()) {
                urn = Integer.parseInt(urnString);
            }
            Affiliation affiliation = Affiliation.fromName(affiliationString);
            EntityType entityType = EntityType.fromString(entityTypeString);
            return new EntityConfiguration(startTime, id, name, description, urn, affiliation, entityType);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "TapetsConfiguration::create", e);
        }
        return null;
    }

    private final long startTime;
    private final int id;
    private final String name;
    private final String description;
    private final int urn;
    private final Affiliation affiliation;
    private final EntityType entityType;

    public EntityConfiguration(long startTime, int id, String name, String description, int urn,
                               Affiliation affiliation, EntityType entityType) {
        this.startTime = startTime;
        this.id = id;
        this.name = name;
        this.description = description;
        this.urn = urn;
        this.affiliation = affiliation;
        this.entityType = entityType;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getUrn() {
        return urn;
    }

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityConfiguration entityConfiguration = (EntityConfiguration) o;
        return startTime == entityConfiguration.startTime &&
                id == entityConfiguration.id &&
                name.equalsIgnoreCase(entityConfiguration.name) &&
                description.equalsIgnoreCase(entityConfiguration.description) &&
                urn == entityConfiguration.urn &&
                affiliation == entityConfiguration.affiliation &&
                entityType.equals(entityConfiguration.entityType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, id, name, description, urn, affiliation, entityType);
    }
}
