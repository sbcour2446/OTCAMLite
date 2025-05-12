package gov.mil.otc._3dvis.project.blackhawk;

public enum AcquisitionType {

    SEARCH("search"),
    TRACK("track"),
    MG("missile guidance"),
    NA("");
    private final String description;

    AcquisitionType(String description) {
        this.description = description;
    }

    public static AcquisitionType fromString(String string) {
        for (AcquisitionType acquisitionType : values()) {
            if (acquisitionType.toString().equalsIgnoreCase(string)) {
                return acquisitionType;
            }
        }
        return NA;
    }

    public String getDescription() {
        return description;
    }
}
