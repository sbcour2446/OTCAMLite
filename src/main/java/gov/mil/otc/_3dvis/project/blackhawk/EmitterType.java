package gov.mil.otc._3dvis.project.blackhawk;

public enum EmitterType {

    LAUNCH("Launch"),
    LASER("Laser"),
    RADAR("Radar");
    private final String description;

    EmitterType(String description) {
        this.description = description;
    }

    public static EmitterType fromString(String string) {
        for (EmitterType emitterType : values()) {
            if (emitterType.toString().equalsIgnoreCase(string)) {
                return emitterType;
            }
        }
        return null;
    }

    public String getDescription() {
        return description;
    }
}
