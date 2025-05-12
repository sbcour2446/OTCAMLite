package gov.mil.otc._3dvis.entity;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.base.IEntity;

import java.util.*;

public class EntityFilter {

    private final Map<Affiliation, Boolean> affiliationFilters = new EnumMap<>(Affiliation.class);
    private final List<Integer> entityKindFilters;
    private final List<String> sourceFilters;
    private boolean filterOutOfScope = true;
    private boolean filterOutOfComms = true;
    private boolean filterTimedOut = false;
    private boolean filterModified = true;

    public EntityFilter() {
        for (Affiliation affiliation : Affiliation.values()) {
            affiliationFilters.put(affiliation, true);
        }
        entityKindFilters = Collections.synchronizedList(new ArrayList<>());
        sourceFilters = Collections.synchronizedList(new ArrayList<>());
    }

    public EntityFilter(EntityFilter entityFilter) {
        affiliationFilters.putAll(entityFilter.affiliationFilters);
        entityKindFilters = Collections.synchronizedList(new ArrayList<>(entityFilter.entityKindFilters));
        sourceFilters = Collections.synchronizedList(new ArrayList<>(entityFilter.sourceFilters));
        filterOutOfScope = entityFilter.filterOutOfScope;
        filterOutOfComms = entityFilter.filterOutOfComms;
        filterTimedOut = entityFilter.filterTimedOut;
        filterModified = entityFilter.filterModified;
    }

    public void setAffiliationFilter(Affiliation affiliation, boolean isFiltered) {
        filterModified = affiliationFilters.get(affiliation) != isFiltered;
        affiliationFilters.put(affiliation, isFiltered);
    }

    public boolean getAffiliationFilter(Affiliation affiliation) {
        return affiliationFilters.get(affiliation);
    }

    public void addEntityKindFilter(Integer entityKind) {
        if (!entityKindFilters.contains(entityKind)) {
            entityKindFilters.add(entityKind);
            filterModified = true;
        }
    }

    public void removeEntityKindFilter(Integer entityKind) {
        if (entityKindFilters.contains(entityKind)) {
            entityKindFilters.remove(entityKind);
            filterModified = true;
        }
    }

    public boolean getEntityKindFilter(Integer entityKind) {
        return entityKindFilters.contains(entityKind);
    }

    public void addSourceFilter(String source) {
        if (!sourceFilters.contains(source)) {
            sourceFilters.add(source);
            filterModified = true;
        }
    }

    public void removeSourceFilter(String source) {
        if (sourceFilters.contains(source)) {
            sourceFilters.remove(source);
            filterModified = true;
        }
    }

    public boolean getSourceFilter(String source) {
        return sourceFilters.contains(source);
    }

    public boolean isFilterOutOfScope() {
        return filterOutOfScope;
    }

    public void setFilterOutOfScope(boolean filterOutOfScope) {
        filterModified = this.filterOutOfScope != filterOutOfScope;
        this.filterOutOfScope = filterOutOfScope;
    }

    public boolean isFilterOutOfComms() {
        return filterOutOfComms;
    }

    public void setFilterOutOfComms(boolean filterOutOfComms) {
        filterModified = this.filterOutOfComms != filterOutOfComms;
        this.filterOutOfComms = filterOutOfComms;
    }

    public boolean isFilterTimedOut() {
        return filterTimedOut;
    }

    public void setFilterTimedOut(boolean filterTimedOut) {
        filterModified = this.filterTimedOut != filterTimedOut;
        this.filterTimedOut = filterTimedOut;
    }

    public boolean isFilterModified() {
        return filterModified;
    }

    public void resetFilterModified() {
        filterModified = false;
    }

    public boolean isFiltered(IEntity entity) {
        if (!isFilterOutOfScope() && !entity.isInScope()) {
            return false;
        }

        if (!isFilterTimedOut() && entity.isTimedOut()) {
            return false;
        }

        EntityDetail entityDetail = entity.getEntityDetail();

        if (entityDetail == null) {
            return false;
        }

        if (!getAffiliationFilter(entityDetail.getAffiliation())) {
            return false;
        }

        if (!isFilterOutOfComms() && entityDetail.isOutOfComms()) {
            return false;
        }

        if (!entityKindFilters.isEmpty() && !entityKindFilters.contains(entityDetail.getEntityType().getKind())) {
            return false;
        }

        if (!sourceFilters.isEmpty() && !sourceFilters.contains(entityDetail.getSource())) {
            return false;
        }

        return true;
    }
}
