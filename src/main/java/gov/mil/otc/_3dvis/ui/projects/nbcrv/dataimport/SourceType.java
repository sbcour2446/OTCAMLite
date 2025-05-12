package gov.mil.otc._3dvis.ui.projects.nbcrv.dataimport;

public enum SourceType {
    UNKNOWN,
    ATC,
    BFT,
    FLIR,
    MANUAL_DATA,
    OADMS,
    OTCAM,
    UAS;

    public SourceType getEnumIngoreCase(String name) {
        for (SourceType sourceType : SourceType.values()){
            if(name.equalsIgnoreCase(sourceType.name())) {
                return sourceType;
            }
        }
        return SourceType.UNKNOWN;
    }
}
