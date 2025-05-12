package gov.mil.otc._3dvis.datamodel;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum Affiliation {

    UNKNOWN("Unknown"),
    OTHER("Other"),
    FRIENDLY("Friendly"),
    HOSTILE("Hostile"),
    NEUTRAL("Neutral"),
    NONPARTICIPANT("Non-participant"),
    PENDING("Pending"),
    ASSUMED_FRIEND("Assumed Friend"),
    SUSPECT("Suspect"),
    JOKER("Joker"),
    FAKER("Faker");
    final String name;

    Affiliation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Affiliation getEnum(int ordinal) {
        Affiliation affiliation = Affiliation.UNKNOWN;
        try {
            affiliation = Affiliation.values()[ordinal];
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return affiliation;
    }

    public static Affiliation fromName(String name) {
        for (Affiliation affiliation : Affiliation.values()) {
            if (affiliation.name.equalsIgnoreCase(name)) {
                return affiliation;
            }
        }
        return Affiliation.UNKNOWN;
    }
}
