package gov.mil.otc._3dvis.playback.dataset.stanag;

import gov.mil.otc._3dvis.data.stanag.A101File;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.project.shadow.ShadowEntity;
import javafx.scene.layout.VBox;

public class A101ImportObject extends ImportObject<A101File> {

    public A101ImportObject(A101File object) {
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
        shadowEntity.addTspiList(getObject().getTspiDataList());
        shadowEntity.addTspiExtendedDataList(getObject().getTspiExtendedDataList());
    }
}
