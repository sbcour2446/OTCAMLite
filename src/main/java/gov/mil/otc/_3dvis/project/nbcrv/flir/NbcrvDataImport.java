package gov.mil.otc._3dvis.project.nbcrv.flir;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvEntity;

import java.io.File;

public class NbcrvDataImport {

    public static void importNbcrvCsvFile(File file) {
        NbcrvFile nbcrvFile = new NbcrvFile(file);
        nbcrvFile.processFile();

        NbcrvEntity nbcrvEntity = new NbcrvEntity(new EntityId(1, 1, 1));
        nbcrvEntity.addDevices(nbcrvFile.getDeviceList());
        nbcrvEntity.addTspiList(nbcrvFile.getTspiDataList());
        EntityDetail entityDetail = new EntityDetail.Builder()
                .setName("NBCRV")
                .setSource(file.getName())
                .setTimestamp(nbcrvFile.getTspiDataList().get(0).getTimestamp())
                .setAffiliation(Affiliation.FRIENDLY)
                .build();
        nbcrvEntity.addEntityDetail(entityDetail);
        EntityManager.addEntity(nbcrvEntity, false);
    }
}
