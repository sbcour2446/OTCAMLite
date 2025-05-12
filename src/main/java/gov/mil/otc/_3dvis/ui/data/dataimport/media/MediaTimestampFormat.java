package gov.mil.otc._3dvis.ui.data.dataimport.media;

public enum MediaTimestampFormat {
    NONE(""),
    METADATA("timestamp from metadata"),
    STANDARD("yyyyMMdd_HHmmss.[ext]"),
    STANDARD_MILLI("yyyyMMdd_HHmmssSSS.[ext]"),
    STANDARD_MEDIA_SET("[media set]_yyyyMMdd_HHmmss.[ext]"),
    DAY_OF_YEAR("yyyy_DDD_HHmmss.[ext]"),
    DAY_OF_YEAR_MILLI("yyyy_DDD_HHmmssSSS.[ext]"),
    DAY_OF_YEAR_MEDIA_SET("[media set]_yyyy_DDD_HHmmssSSS.[ext]"),
    SHADOW("[*]_[media set]_DDD_HH_mm_ss.mp4"),
    APACHE("[*]_[*]_[*]_yyyyMMdd_HHmmss[*].ts"),
    BLACK_HAWK("[media set]_DDD_MM_dd_HH_mm_ss_SSS.[ext]");
    private final String description;

    MediaTimestampFormat(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%-22s%s", name(), description);
    }
}
