package gov.mil.otc._3dvis.datamodel;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

/**
 * This class represents the scope an entity object is valid.  The end-of-scope is initially set to null and will
 * keep the object in scope beyond its start time.  When a new scope is created for the object, the end-of-scope for
 * the previous scope, if not already set, will need to be set to a time prior to start of new scope.
 */
public class EntityScope extends TimedData {

    private final long stopTime;

    public EntityScope(long timestamp) {
        this(timestamp, Long.MAX_VALUE);
    }

    public EntityScope(long timestamp, long stopTime) {
        super(timestamp);
        this.stopTime = stopTime;
    }

    public long getStopTime() {
        return stopTime;
    }
}
