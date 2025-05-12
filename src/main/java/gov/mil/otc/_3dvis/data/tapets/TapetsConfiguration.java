package gov.mil.otc._3dvis.data.tapets;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.utility.Utility;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TapetsConfiguration {

    private static final String TIME_FORMAT = "yyyyMMddHHmmss";

    public static TapetsConfiguration create(String tapetsIdString, String name, String description, String urnString,
                                             String affiliationString, String entityTypeString, String startTimeString) {
        try {
            int tapetsId = Integer.parseInt(tapetsIdString);
            int urn;
            try {
                urn = Integer.parseInt(urnString);
            } catch (Exception e) {
                urn = 0;
            }
            Affiliation affiliation = Affiliation.fromName(affiliationString);
            EntityType entityType = EntityType.fromString(entityTypeString);
            long startTime = Utility.parseTime(startTimeString, TIME_FORMAT);
            return new TapetsConfiguration(tapetsId, name, description, urn, affiliation, entityType, startTime);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "TapetsConfiguration::create", e);
        }
        return null;
    }

    private final int tapetsId;
    private final String name;
    private final String description;
    private final int urn;
    private final Affiliation affiliation;
    private final EntityType entityType;
    private final long startTime;

    public TapetsConfiguration(int tapetsId, String name, String description, int urn, Affiliation affiliation,
                               EntityType entityType, long startTime) {
        this.tapetsId = tapetsId;
        this.name = name;
        this.description = description;
        this.urn = urn;
        this.affiliation = affiliation;
        this.entityType = entityType;
        this.startTime = startTime;
    }

    public int getTapetsId() {
        return tapetsId;
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

    public long getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TapetsConfiguration tapetsConfiguration = (TapetsConfiguration) o;
        return tapetsId == tapetsConfiguration.tapetsId &&
                name.equalsIgnoreCase(tapetsConfiguration.name) &&
                description.equalsIgnoreCase(tapetsConfiguration.description) &&
                urn == tapetsConfiguration.urn &&
                affiliation == tapetsConfiguration.affiliation &&
                entityType.equals(tapetsConfiguration.entityType) &&
                startTime == tapetsConfiguration.startTime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tapetsId, name, description, urn, affiliation, entityType, startTime);
    }
}
