package gov.mil.otc._3dvis.playback.dataset.stanag;

import gov.mil.otc._3dvis.data.stanag.A302File;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.project.shadow.ShadowEntity;
import javafx.scene.layout.VBox;

public class A302ImportObject extends ImportObject<A302File> {

    public A302ImportObject(A302File object) {
        super(object, object.getFile().getName());
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
        if (!(entity instanceof ShadowEntity shadowEntity)) {
            return;
        }
        for (UasPayloadData uasPayloadData : getObject().getUasPayloadDataList()) {
            shadowEntity.addUasPayloadData(uasPayloadData);
        }
    }
}
