package gov.mil.otc._3dvis.playback.dataset;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.file.ImportFile;
import gov.mil.otc._3dvis.data.tpsi.TspiDataFile;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.widgets.status.StatusLine;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ImportObject<T> {

    private final T object;
    private final String name;
    private boolean isNew = true;
    private boolean isMissing = false;
    private boolean isModified = false;
    private boolean isImported = false;

    public ImportObject(T object, String name) {
        this.object = object;
        this.name = name;
    }

    public boolean determineIsNew() {
        if (object instanceof File file) {
            isNew = determineIsNew(file);
        } else if (object instanceof ImportFile importFile) {
            isNew = determineIsNew(importFile.getFile());
        } else if (object instanceof TspiDataFile tspiDataFile) {
            isNew = determineIsNew(tspiDataFile.getFile());
        }
        return true;
    }

    public boolean determineIsNew(File file) {
        for (DataSource dataSource : DataManager.getDataSources()) {
            if (dataSource.getName().equalsIgnoreCase(file.getAbsolutePath())) {
                return false;
            }
        }
        return true;
    }

    public T getObject() {
        return object;
    }

    public String getName() {
        return name;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isMissing() {
        return isMissing;
    }

    public void setMissing(boolean missing) {
        isMissing = missing;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    public boolean isImported() {
        return isImported;
    }

    public void setImported(boolean imported) {
        isImported = imported;
    }

    public abstract VBox getDisplayPane();

    public boolean importObject(StatusLine statusLine) {
        try {
            doImport();
        } catch (Exception e) {
            statusLine.addError("import failed: " + getName());
            Logger.getGlobal().log(Level.INFO, "ImportObject::importObject:" + getName(), e);
            return false;
        }
        return true;
    }

    public boolean importObject(IEntity entity, StatusLine statusLine) {
        try {
            doImport(entity);
        } catch (Exception e) {
            statusLine.addError("import failed: " + getName());
            Logger.getGlobal().log(Level.INFO, "ImportObject::importObject:" + getName(), e);
            return false;
        }
        return true;
    }

    public abstract void doImport();

    public abstract void doImport(IEntity entity);

    @Override
    public String toString() {
        return name;
    }
}
