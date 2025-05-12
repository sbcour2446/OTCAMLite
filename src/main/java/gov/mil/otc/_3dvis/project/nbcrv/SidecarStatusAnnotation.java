package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;

public class SidecarStatusAnnotation extends StatusAnnotation {

    public SidecarStatusAnnotation(SidecarEntity entity) {
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
        addRow(stringBuilder, "Location", getLocationString());

        return stringBuilder.toString();
    }
}
