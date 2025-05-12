package gov.mil.otc._3dvis.playback.dataset.tapets;

import gov.mil.otc._3dvis.data.tapets.TapetsLogFile;
import gov.mil.otc._3dvis.datamodel.EntityScope;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class TapetsLogFileImportObject extends ImportObject<TapetsLogFile> {

    public static TapetsLogFileImportObject scanAndCreate(File file) {
        TapetsLogFile tapetsLogFile = new TapetsLogFile(file);
        TapetsLogFileImportObject tapetsImportObject = new TapetsLogFileImportObject(tapetsLogFile, file.getName());
        if (tapetsImportObject.determineIsNew() && !tapetsLogFile.process()) {
            tapetsImportObject.setMissing(true);
        }
        return tapetsImportObject;
    }

    public TapetsLogFileImportObject(TapetsLogFile object, String name) {
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
        if (!isNew() || isMissing()) {
            return;
        }

        long firstTime = getObject().getTspiDataList().getFirst().getTimestamp();
        long lastTime = getObject().getTspiDataList().getLast().getTimestamp();
        //DataSource dataSource = DataManager.createDataSource(getObject().getAbsolutePath(), firstTime, lastTime);
        entity.addTspiList(getObject().getTspiDataList());
//        for (TspiData tspiData : getObject().getTspiDataList()) {
//            DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), dataSource.getId());
//        }
        EntityScope entityScope = new EntityScope(firstTime, lastTime);
        entity.addEntityScope(entityScope);
//        DatabaseLogger.addEntityScope(entityScope, entity.getEntityId(), dataSource.getId());

        setImported(true);
    }

    public int getUnitId() {
        return getObject().getUnitId();
    }

    public List<TspiData> getTspiDataList() {
        return getObject().getTspiDataList();
    }
}
