package gov.mil.otc._3dvis.playback.dataset;

import gov.mil.otc._3dvis.data.mission.MissionConfiguration;
import gov.mil.otc._3dvis.entity.base.IEntity;
import javafx.scene.layout.VBox;

public class MissionConfigurationImportObject extends ImportObject<MissionConfiguration> {

    public MissionConfigurationImportObject(MissionConfiguration object, String name) {
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
