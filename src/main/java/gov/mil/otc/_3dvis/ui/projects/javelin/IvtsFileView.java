package gov.mil.otc._3dvis.ui.projects.javelin;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.project.javelin.CutSheetFile;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IvtsFileView {

    public static IvtsFileView createIvtsFileView(File file, CutSheetFile cutSheetFile) {
        try {
            String[] filenameParts = file.getName().split("[_|.]");
            if (filenameParts.length != 5) {
                return null;
            }

            int dayOfYear = Integer.parseInt(filenameParts[1]);
            int pip = Integer.parseInt(filenameParts[4]);
            EntityDetail entityDetail = cutSheetFile.getEntityDetail(pip);

            return new IvtsFileView(file, Utility.dayOfYearToEpoch(2023, dayOfYear), pip, entityDetail);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "IvtsFileView::createIvtsFileView:", e);
        }
        return null;
    }

    private final StringProperty status = new SimpleStringProperty("");
    private final File file;
    private final long epochOffset;
    private final int id;
    private final EntityDetail entityDetail;

    public IvtsFileView(File file, long epochOffset, int id, EntityDetail entityDetail) {
        this.file = file;
        this.epochOffset = epochOffset;
        this.id = id;
        this.entityDetail = entityDetail;
        if (entityDetail == null) {
            status.set("no config");
        }
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }

    public String getFileDate() {
        return Utility.formatTime(epochOffset, Common.DATE_ONLY);
    }

    public long getEpochOffset() {
        return epochOffset;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return entityDetail != null ? entityDetail.getName() : "";
    }

    public String getAffiliation() {
        return entityDetail != null ? entityDetail.getAffiliation().getName() : "";
    }

    public String getEntityType() {
        return entityDetail != null ? entityDetail.getEntityType().getDescription() : "";
    }

    public String getMilitarySymbol() {
        return entityDetail != null ? entityDetail.getMilitarySymbol() : "";
    }

    public boolean hasEntityDetails() {
        return entityDetail != null;
    }
}
