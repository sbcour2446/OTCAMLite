package gov.mil.otc._3dvis.ui.data.dataimport.bft;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import javafx.scene.control.CheckBox;

public class BftEntityView {

    private final AdHocEntity adHocEntity;
    private final CheckBox importData = new CheckBox();

    public BftEntityView(AdHocEntity adHocEntity) {
        this.adHocEntity = adHocEntity;
        importData.setSelected(true); // for development
    }

    public AdHocEntity getAdHocEntity() {
        return adHocEntity;
    }

    public CheckBox getImportData() {
        return importData;
    }

    public Integer getUrn() {
        return adHocEntity.getUrn();
    }

    public String getName() {
        return adHocEntity.getName();
    }

    public Affiliation getAffiliation() {
        return adHocEntity.getAffiliation();
    }

    public Integer getPositionReportCount() {
        return adHocEntity.getTspiDataList().size();
    }
}
