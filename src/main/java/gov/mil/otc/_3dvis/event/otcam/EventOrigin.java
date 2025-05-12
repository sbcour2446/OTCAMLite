package gov.mil.otc._3dvis.event.otcam;

import java.util.logging.Level;
import java.util.logging.Logger;

// Enumeration describing the sender authority for this message
//
public enum EventOrigin {
    OTHER("Other"),      // Unspecified/Other
    ADMIN("Admin"),      // From an OTCAM Console (the highest authority)
    RTCA("RTCA Engine"),       // From the RTCA Engine Platform or designated Weapons Effect Simulator (WES)
    USER("user-level"),       // From a user-level system such as a platform Gateway
    CONTROLLER("Controller"); // Controller Gun
    final String description;

    EventOrigin(String description) {
        this.description = description;
    }

    public static EventOrigin getEnum(int ordinal) {
        EventOrigin eventOrigin = EventOrigin.OTHER;
        try {
            eventOrigin = EventOrigin.values()[ordinal];
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Invalid EventOrigin value %d", ordinal), e);
        }
        return eventOrigin;
    }

    public String getDescription() {
        return description;
    }
}
