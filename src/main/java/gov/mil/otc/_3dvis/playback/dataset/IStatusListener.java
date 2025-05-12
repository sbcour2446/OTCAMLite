package gov.mil.otc._3dvis.playback.dataset;

public interface IStatusListener {
    void onStatusUpdate(String status);

    void onError(String error);

    void onScanComplete(ImportFolder importFolder, boolean successful);

    void onImportComplete(ImportFolder importFolder, boolean successful);
}
