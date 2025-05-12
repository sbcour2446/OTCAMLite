package gov.mil.otc._3dvis.data.file;

public interface ProcessingCompleteListener {

    void processingComplete(ImportFile importFile, boolean successful);
}
