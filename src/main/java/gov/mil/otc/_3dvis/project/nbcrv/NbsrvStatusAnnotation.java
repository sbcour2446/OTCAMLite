package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;

public class NbsrvStatusAnnotation extends StatusAnnotation {

    public NbsrvStatusAnnotation(NbcrvEntity entity) {
        super(entity);
    }

    @Override
    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder();

        addRow(stringBuilder, "ID", getEntity().getEntityId().toString());

        EntityDetail entityDetail = getEntity().getEntityDetail();
        if (entityDetail == null) {
            return stringBuilder.toString();
        }

        addRow(stringBuilder, "Name", entityDetail.getName());
        addRow(stringBuilder, "Source", entityDetail.getSource());
        addRow(stringBuilder, "Description", entityDetail.getEntityType().getDescription());
        addRow(stringBuilder, "Location", getLocationString());

        for (Device device : ((NbcrvEntity) getEntity()).getDeviceList()) {
            DeviceState deviceState = device.getCurrentDeviceState();
            String value = "";
            if (deviceState != null) {
                if (deviceState.isAlert()) {
                    value += deviceState.getAlertReason();
                } else {
                    value += deviceState.getState();
                }
            }
            addRow(stringBuilder, device.getName(), value);
        }

        return stringBuilder.toString();
    }
}
