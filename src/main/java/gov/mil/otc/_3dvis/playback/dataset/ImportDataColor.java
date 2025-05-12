package gov.mil.otc._3dvis.playback.dataset;

public final class ImportDataColor {

    public static String IMPORTED = "-fx-text-fill: green;";
    public static String MISSING = "-fx-text-fill: red;";
    public static String MODIFIED = "-fx-text-fill: tan;";
    public static String NEW = "-fx-text-fill: cornflowerblue;";

    public static String getStyle(ImportObject<?> importObject) {
        if (importObject.isImported()) {
            return IMPORTED;
        } else if (importObject.isMissing()) {
            return MISSING;
        } else if (importObject.isNew()) {
            return NEW;
        } else if (importObject.isModified()) {
            return MODIFIED;
        }
        return "";
    }
}
