package gov.mil.otc._3dvis.playback.dataset.tspi;

import gov.mil.otc._3dvis.data.tpsi.TspiDataFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.layout.VBox;

import java.util.List;

public class TspiImportObject extends ImportObject<TspiDataFile> {

    public TspiImportObject(TspiDataFile object, String name) {
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

        List<TspiData> tspiData = getObject().getTspiDataList();
        if (tspiData.isEmpty()) {
            return;
        }

        entity.addTspiList(tspiData);

        setImported(true);
    }

    public long getFirstTimestamp() {
        if (getObject().getTspiDataList().isEmpty()) {
            return -1;
        }
        return getObject().getTspiDataList().getFirst().getTimestamp();
    }

    public long getLastTimestamp() {
        if (getObject().getTspiDataList().isEmpty()) {
            return -1;
        }
        return getObject().getTspiDataList().getLast().getTimestamp();
    }
}
