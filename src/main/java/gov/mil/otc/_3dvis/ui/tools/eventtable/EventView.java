package gov.mil.otc._3dvis.ui.tools.eventtable;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.event.Event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class EventView {

    protected final Event event;
    protected final String timestamp;
    protected final String eventType;
    protected final String description;

    public EventView(Event event) {
        this.event = event;

        LocalDateTime localDateTime = Instant.ofEpochMilli(event.getTimestamp()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_WITH_MILLIS);
        timestamp = localDateTime.format(dateTimeFormatter);

        eventType = event.getType();
        description = event.getDescription();
    }

    public Event getEvent() {
        return event;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }
}
