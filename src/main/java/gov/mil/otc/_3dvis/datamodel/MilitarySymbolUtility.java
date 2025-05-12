package gov.mil.otc._3dvis.datamodel;

import gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.MilStd2525IconRetrieverEx;
import gov.nasa.worldwind.avlist.AVListImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MilitarySymbolUtility {

    private static final MilitarySymbolUtility SINGLETON = new MilitarySymbolUtility();
    private final Map<String, String> validatedCodes = new ConcurrentHashMap<>();
    private final MilStd2525IconRetrieverEx milStd2525IconRetriever;

    private MilitarySymbolUtility() {
        milStd2525IconRetriever = new MilStd2525IconRetrieverEx("jar:file:milstd2525-symbols.jar!");
    }

    public static String validateSymbolCode(String militarySymbol, Affiliation affiliation) {
        if (militarySymbol.trim().isEmpty() || militarySymbol.length() != 15) {
            militarySymbol = getDefaultMilitarySymbol(affiliation);
        }

        militarySymbol = militarySymbol.toUpperCase();

        if ((militarySymbol.charAt(0) != 'S'
                && militarySymbol.charAt(0) != 'I'
                && militarySymbol.charAt(0) != 'O'
                && militarySymbol.charAt(0) != 'G'
                && militarySymbol.charAt(0) != 'W'
                && militarySymbol.charAt(0) != 'E')) {
            militarySymbol = getDefaultMilitarySymbol(affiliation);
        }

        militarySymbol = militarySymbol.substring(0, 1) + getIdentityChar(affiliation) + militarySymbol.substring(2, 15);

        if (militarySymbol.charAt(2) == '-') {
            militarySymbol = militarySymbol.substring(0, 2) + 'Z' + militarySymbol.substring(3, 15);
        }

        if (militarySymbol.charAt(3) == '-'
                && (militarySymbol.charAt(0) == 'S'
                || militarySymbol.charAt(0) == 'I'
                || militarySymbol.charAt(0) == 'O')) {
            militarySymbol = militarySymbol.substring(0, 3) + 'P' + militarySymbol.substring(4, 15);
        }

        if (!SINGLETON.validatedCodes.containsKey(militarySymbol)) {
            if (checkImage(militarySymbol)) {
                SINGLETON.validatedCodes.put(militarySymbol, militarySymbol);
            } else {
                SINGLETON.validatedCodes.put(militarySymbol, getDefaultMilitarySymbol(affiliation));
            }
        }

        return SINGLETON.validatedCodes.get(militarySymbol);
    }

    public static boolean checkImage(String militarySymbol) {
        try {
            return SINGLETON.milStd2525IconRetriever.createIcon(militarySymbol, new AVListImpl()) != null;
        } catch (Exception e) {
            String message = "Invalid military symbol " + militarySymbol;
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
    }

    public static String getDefaultMilitarySymbol(Affiliation affiliation) {
        String code = "S";
        code += getIdentityChar(affiliation);
        code += "GP-----------";
        return code;
    }

    public static char getIdentityChar(Affiliation affiliation) {
        return switch (affiliation) {
            case FRIENDLY, ASSUMED_FRIEND -> 'F';
            case NEUTRAL, NONPARTICIPANT -> 'N';
            case HOSTILE, SUSPECT, JOKER, FAKER -> 'H';
            default -> 'U';
        };
    }
}
