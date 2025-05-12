package gov.mil.otc._3dvis.project.nbcrv;

public enum RegionType {
    UNKNOWN,
    POINT,
    POLYGON,
    ARC;

    public static RegionType fromName(String name) {
        try {
            return RegionType.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return RegionType.UNKNOWN;
        }
    }
}
