package gov.mil.otc._3dvis.event;

import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.nasa.worldwind.geom.Position;

import java.util.Objects;

public abstract class RtcaEvent extends Event {

    private final EntityId eventId;

    protected RtcaEvent(long eventTime, EntityId eventId) {
        super(eventTime);
        this.eventId = eventId;
    }

    protected RtcaEvent(long eventTime, long endTime, EntityId eventId) {
        super(eventTime, endTime);
        this.eventId = eventId;
    }

    public EntityId getEventId() {
        return eventId;
    }

    public abstract Position getEventLocation();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RtcaEvent rtcaEvent = (RtcaEvent) o;
        return getEventId().equals(rtcaEvent.getEventId()) &&
                getTimestamp() == rtcaEvent.getTimestamp();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEventId(), getTimestamp());
    }
}
