package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class DetectionView {

    private final NbcrvDetection nbcrvDetection;
    private final BooleanProperty isActive = new SimpleBooleanProperty();

    protected DetectionView(NbcrvDetection nbcrvDetection) {
        this.nbcrvDetection = nbcrvDetection;
    }

    protected boolean update(long time) {
        final boolean active = nbcrvDetection.isActive(time);
        if (active != isActive.get()) {
            Platform.runLater(() -> {
                setIsActive(active);
            });
            return true;
        }
        return false;
    }

    public NbcrvDetection getNbcrvEvent() {
        return nbcrvDetection;
    }

    public String getDeviceName() {
        return nbcrvDetection.getDeviceName();
    }

    public Long getTimestamp() {
        return nbcrvDetection.getTimestamp();
    }

    public String getFormattedTime() {
        return Utility.formatTime(nbcrvDetection.getTimestamp());
    }

    public boolean isIsActive() {
        return isActive.get();
    }

    public BooleanProperty isActiveProperty() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive.set(isActive);
    }
}
