package gov.mil.otc._3dvis.ui.tools.entitytable;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.base.IEntity;

import java.util.EnumMap;
import java.util.Map;

public class Filter {

    private final Map<Affiliation, Boolean> affiliationFilters = new EnumMap<>(Affiliation.class);
    private boolean filterOutOfComms = true;
    private boolean filterOutOfScope = true;
    private boolean filterTimedOut = true;

    public Filter() {
        for (Affiliation affiliation : Affiliation.values()) {
            affiliationFilters.put(affiliation, true);
        }
    }

    public void setAffiliationFilter(Affiliation affiliation, boolean filter) {
        affiliationFilters.put(affiliation, filter);
    }

    public void setFilterOutOfComms(boolean filterOutOfComms) {
        this.filterOutOfComms = filterOutOfComms;
    }

    public void setFilterOutOfScope(boolean filterOutOfScope) {
        this.filterOutOfScope = filterOutOfScope;
    }

    public void setFilterTimedOut(boolean filterTimedOut) {
        this.filterTimedOut = filterTimedOut;
    }

    public boolean isVisible(IEntity entity) {
        if (!filterOutOfScope && !entity.isInScope()) {
            return false;
        }

        if (!filterTimedOut && entity.isTimedOut()) {
            return false;
        }

        EntityDetail entityDetail = entity.getEntityDetail();
        if (entityDetail != null) {
            if (!filterOutOfComms && entityDetail.isOutOfComms()) {
                return false;
            }
            return affiliationFilters.get(entityDetail.getAffiliation());
        }

        return true;
    }
}
