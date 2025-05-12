package gov.mil.otc._3dvis.data.tapets;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum TapetsRtca {
    UNKNOWN,
    ALIVE,
    NEAR_MISS,
    MOBILITY_KILL,
    FIREPOWER_KILL,
    CAT_KILL,
    COMM_KILL,
    HIT;

    public static TapetsRtca getEnum(int ordinal) {
        TapetsRtca returnValue = TapetsRtca.UNKNOWN;
        try {
            returnValue = TapetsRtca.values()[ordinal];
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return returnValue;
    }
}
