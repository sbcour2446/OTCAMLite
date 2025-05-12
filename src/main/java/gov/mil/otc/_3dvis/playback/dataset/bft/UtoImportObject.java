package gov.mil.otc._3dvis.playback.dataset.bft;

import gov.mil.otc._3dvis.data.jbcp.UtoFile;
import gov.mil.otc._3dvis.data.jbcp.UtoRecord;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class UtoImportObject extends ImportObject<UtoFile> {

    public static UtoImportObject scanAndCreate(File file) {
        UtoFile utoFile = new UtoFile(file);
        UtoImportObject importObject = new UtoImportObject(utoFile);
        if (!utoFile.processFile()) {
            importObject.setMissing(true);
        }
        importObject.determineIsNew();
        return importObject;
    }

    public UtoImportObject(UtoFile object) {
        super(object, object.getFile().getName());
    }

    public Map<Integer, AdHocEntity> getEntityMap() {
        Map<Integer, AdHocEntity> entityMap = new HashMap<>();
        for (UtoRecord utoRecord : getObject().getUtoRecordList()) {
            AdHocEntity adHocEntity = entityMap.get(utoRecord.getUrn());
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                entityMap.put(utoRecord.getUrn(), adHocEntity);
            }

            adHocEntity.setAffiliation(Affiliation.FRIENDLY);
            adHocEntity.setUrn(utoRecord.getUrn());
            adHocEntity.setName(utoRecord.getName());
            adHocEntity.setMilitarySymbol(utoRecord.getMilStd2525Symbol());
        }

        return entityMap;
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
}
