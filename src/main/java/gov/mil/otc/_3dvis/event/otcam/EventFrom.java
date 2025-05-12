package gov.mil.otc._3dvis.event.otcam;

import java.util.logging.Level;
import java.util.logging.Logger;

// Enumeration describing the originating event that caused this message
//
public enum EventFrom {
    OTHER("Unspecified/Other"),        // Unspecified/Other
    DIRECT("Direct Fire"),       // Direct Fire Event
    INDIRECT("Indirect Fire"),     // Indirect Fire Event
    MINE("Mine"),         // Mine or Area Denial
    NBC("NBC"),          // Nuclear, Biological or Chemical
    CHEAT("Cheat Detection"),        // Cheat Detection
    ADMIN("Admin");        // From an Admin source (e.g., 3DVis)
    final String description;

    EventFrom(String description) {
        this.description = description;
    }

    public static EventFrom getEnum(int ordinal) {
        EventFrom eventFrom = EventFrom.OTHER;
        try {
            eventFrom = EventFrom.values()[ordinal];
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Invalid EventFrom value %d", ordinal), e);
        }
        return eventFrom;
    }

    public String getDescription() {
        return description;
    }
}
