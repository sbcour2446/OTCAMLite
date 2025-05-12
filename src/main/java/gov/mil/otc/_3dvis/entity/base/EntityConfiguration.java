package gov.mil.otc._3dvis.entity.base;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityScope;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.utility.Utility;

import java.util.*;

public class EntityConfiguration {

    public static class ManualPosition {
        private final String timestamp;
        private final Double latitude;
        private final Double longitude;

        public ManualPosition(String timestamp, double latitude, double longitude) {
            this.timestamp = timestamp;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public long getTimestamp() {
            return Utility.parseTime(timestamp, Common.DATE_TIME_HHmm);
        }

        public Double getLatitude() {
            return latitude;
        }

        public Double getLongitude() {
            return longitude;
        }
    }

    public static class EntityScopeConfig {
        private final String startTime;
        private final String stopTime;

        public EntityScopeConfig(String startTime, String stopTime) {
            this.startTime = startTime;
            this.stopTime = stopTime;
        }

        public EntityScope toEntityScope() {
            return new EntityScope(Utility.parseTime(startTime, Common.DATE_TIME_HHmm),
                    Utility.parseTime(stopTime, Common.DATE_TIME_HHmm));
        }
    }

    private final String name;
    private final String description;
    private final EntityType entityType;
    private final Affiliation affiliation;
    private final String militarySymbol;
    private final int urn;
    private final int milesPid;
    private final String startTime;
    private final String stopTime;
    private final List<ManualPosition> manualPositionList = new ArrayList<>();
    private final List<EntityScopeConfig> entityScopeList = new ArrayList<>();
    private final Map<String, String> otherConfigurations = new HashMap<>();

    public EntityConfiguration(String name, String description, EntityType entityType, Affiliation affiliation,
                               String militarySymbol, int urn, int milesPid) {
        this(name, description, entityType, affiliation, militarySymbol, urn, milesPid, "", "",
                new ArrayList<>());
    }

    public EntityConfiguration(String name, String description, EntityType entityType, Affiliation affiliation,
                               String militarySymbol, int urn, int milesPid, String startTime, String stopTime,
                               List<ManualPosition> manualPositions) {
        this.name = name;
        this.description = description;
        this.entityType = entityType;
        this.affiliation = affiliation;
        this.militarySymbol = militarySymbol;
        this.urn = urn;
        this.milesPid = milesPid;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.manualPositionList.addAll(manualPositions);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public String getMilitarySymbol() {
        return militarySymbol;
    }

    public int getUrn() {
        return urn;
    }

    public int getMilesPid() {
        return milesPid;
    }

    public long getStartTime() {
        return Utility.parseTime(startTime, Common.DATE_TIME_HHmm);
    }

    public long getStopTime() {
        return Utility.parseTime(stopTime, Common.DATE_TIME_HHmm);
    }

    public List<ManualPosition> getManualPositionList() {
        return manualPositionList;
    }

    public List<EntityScope> getEntityScopeList() {
        List<EntityScope> entityScopes = new ArrayList<>();

        if (entityScopeList != null) {
            for (EntityScopeConfig entityScopeConfig : entityScopeList) {
                entityScopes.add(entityScopeConfig.toEntityScope());
            }
        }

        return entityScopes;
    }

    public Map<String, String> getOtherConfigurations() {
        return otherConfigurations;
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
        return name.equalsIgnoreCase(entityConfiguration.name) &&
                description.equalsIgnoreCase(entityConfiguration.description) &&
                entityType.equals(entityConfiguration.entityType) &&
                affiliation == entityConfiguration.affiliation &&
                militarySymbol.equalsIgnoreCase(entityConfiguration.militarySymbol) &&
                urn == entityConfiguration.urn &&
                milesPid == entityConfiguration.milesPid &&
                startTime.equals(entityConfiguration.startTime) &&
                stopTime.equals(entityConfiguration.stopTime) &&
                manualPositionList.equals(entityConfiguration.manualPositionList) &&
                otherConfigurations.equals(entityConfiguration.otherConfigurations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, entityType, affiliation, militarySymbol, urn, milesPid,
                startTime, stopTime, manualPositionList, otherConfigurations);
    }
}
