package gov.mil.otc._3dvis.playback.dataset.tapets;

import gov.mil.otc._3dvis.data.tapets.TapetsConfiguration;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.scene.layout.VBox;

public class TapetsConfigurationImportObject extends ImportObject<TapetsConfiguration> {

    public TapetsConfigurationImportObject(int id) {
        super(null, String.valueOf(id));
        setMissing(true);
    }

    public TapetsConfigurationImportObject(TapetsConfiguration object) {
        super(object, String.valueOf(object.getTapetsId()));
    }

    public String getTapetsId() {
        return super.getName();
    }

    public String getName() {
        return getObject() != null ? getObject().getName() : "-";
    }

    public String getDescription() {
        return getObject() != null ? getObject().getDescription() : "-";
    }

    public String getUrn() {
        return getObject() != null ? String.valueOf(getObject().getUrn()) : "-";
    }

    public String getAffiliation() {
        return getObject() != null ? getObject().getAffiliation().getName() : "-";
    }

    public String getEntityType() {
        return getObject() != null ? getObject().getEntityType().toString() : "-";
    }

    public String getStartTime() {
        return getObject() != null ? Utility.formatTime(getObject().getStartTime()) : "-";
    }


    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {

    }

    @Override
    public void doImport(IEntity entity) {

    }
}
