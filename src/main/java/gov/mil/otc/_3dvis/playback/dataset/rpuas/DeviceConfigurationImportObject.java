package gov.mil.otc._3dvis.playback.dataset.rpuas;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.rpuas.MissionConfiguration;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.layout.VBox;

public class DeviceConfigurationImportObject extends ImportObject<MissionConfiguration> {

    public DeviceConfigurationImportObject(MissionConfiguration object, String name) {
        super(object, name);
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        // not implemented
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    public String getMissionName() {
        return getObject().getMissionName();
    }

    public long getStartTime() {
        return getObject().getStartTime();
    }

    public long getStopTime() {
        return getObject().getStopTime();
    }
}
