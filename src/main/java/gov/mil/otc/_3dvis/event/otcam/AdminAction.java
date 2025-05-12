package gov.mil.otc._3dvis.event.otcam;

import java.util.logging.Level;
import java.util.logging.Logger;

// Enumeration describing the Administrative Action to be performed
//
public enum AdminAction {
    OTHER,          // Unspecified/Other
    ACTIVATE,       // Set (or reset) Player Active
    INACTIVE,       // Set Player Inactive
    CHEAT_KILL,     // Send Cheat Kill
    RELOAD,         // Reload Player
    RESET,          // Reset Player
    RESURRECT,      // Resurrect Player
    VEHICLE_INIT,   // Send Vehicle Initialization
    WEAPON_FIRED;   // Force Weapon Fire Event

    public static AdminAction getEnum(int ordinal) {
        AdminAction adminAction = AdminAction.OTHER;
        try {
            adminAction = AdminAction.values()[ordinal];
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Invalid AdminAction value %d", ordinal), e);
        }
        return adminAction;
    }
}
