package gov.mil.otc._3dvis.playback.dataset.rpuas;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.rpuas.MissionConfiguration;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.layout.VBox;

public class RpuasMissionConfigurationImportObject extends ImportObject<MissionConfiguration> {

    public RpuasMissionConfigurationImportObject(MissionConfiguration object, String name) {
        super(object, name);
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        // not imported
    }

    @Override
    public void doImport(IEntity entity) {
        // not imported
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
