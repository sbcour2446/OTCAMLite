package gov.mil.otc._3dvis.settings;

import gov.mil.otc._3dvis.datamodel.Affiliation;

import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

public class Defaults {

    public static final int ICON_OPACITY = 75;
    public static final int MUNITION_TIMEOUT = 5000;
    public static final int SITE_APP_ID_3DVIS = 0x14444;
    public static final int APP_ID_GENERIC = 0x0000;
    public static final int APP_ID_TAPETS = 0x5000;
    public static final int APP_ID_BLACKHAWK = 0x5001;
    public static final int APP_ID_BFT = 0x5002;
    public static final int APP_ID_NBCRV = 0x5003;
    public static final int APP_ID_JAVELIN = 0x5004;
    public static final int APP_ID_RPUAS = 0x5005;
    public static final int APP_ID_AVCAD = 0x5006;
    public static final int APP_ID_P10 = 0x5007;
    private static final Map<Affiliation, Color> AFFILIATION_COLOR = new EnumMap<>(Affiliation.class);
    
    static {
        for (Affiliation affiliation : Affiliation.values()) {
            Color color = switch (affiliation) {
                case OTHER, PENDING, UNKNOWN -> Color.YELLOW;
                case NONPARTICIPANT -> Color.LIGHT_GRAY;
                case FRIENDLY, ASSUMED_FRIEND -> Color.CYAN;
                case NEUTRAL -> Color.GREEN;
                case HOSTILE, SUSPECT, JOKER, FAKER -> Color.RED;
            };
            AFFILIATION_COLOR.put(affiliation, color);
        }
    }

    private Defaults() {
    }

    public static Color getAffiliationColor(Affiliation affiliation) {
        return AFFILIATION_COLOR.get(affiliation);
    }
}
