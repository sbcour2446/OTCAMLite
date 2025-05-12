package gov.mil.otc._3dvis.event.otcam;

import java.util.logging.Level;
import java.util.logging.Logger;

// Enumeration describing the type of the event for this message
//
public enum EventType {
    OTHER("Other"),      // Unspecified/Other
    ADMIN("Administrative"),      // Administrative Event (e.g, from OTCAM application)
    EW("Electronic Warfare"),         // Non-kinetic Electronic Warfare Event (e.g., Jammer, Directed Energy, ...)
    ENGAGE("Engagement");     // Kinetic engagement (e.g., Missile, IED, Mine, ...)
    final String description;

    EventType(String description) {
        this.description = description;
    }

    public static EventType getEnum(int ordinal) {
        EventType eventType = EventType.OTHER;
        try {
            eventType = EventType.values()[ordinal];
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Invalid EventType value %d", ordinal), e);
        }
        return eventType;
    }

    public String getDescription() {
        return description;
    }
}
