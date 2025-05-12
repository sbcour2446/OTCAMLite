package gov.mil.otc._3dvis.data.file;

import java.io.File;

public abstract class ImportFile {

    protected final File file;
    protected ProcessingCompleteListener processingCompleteListener;
    protected boolean isProcessing = false;
    protected boolean cancelRequested = false;
    protected boolean processSuccessful = false;
    protected double status = 0.0;

    public ImportFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void startProcessing(final ProcessingCompleteListener processingCompleteListener) {
        if (!isProcessing) {
            isProcessing = true;
            this.processingCompleteListener = processingCompleteListener;
            String threadName = "ImportFile: processFile: " + file.getName();
            new Thread(this::processFile, threadName).start();
        }
    }

    public void cancel() {
        cancelRequested = true;
    }

    public boolean isCanceled() {
        return cancelRequested;
    }

    public double getStatus() {
        return status;
    }

    public boolean isSuccessful() {
        return processSuccessful;
    }

    public boolean processFile() {
        processSuccessful = doProcessFile();
        isProcessing = false;
        if (processingCompleteListener != null) {
            processingCompleteListener.processingComplete(this, processSuccessful);
        }
        return processSuccessful;
    }

    protected abstract boolean doProcessFile();
}
