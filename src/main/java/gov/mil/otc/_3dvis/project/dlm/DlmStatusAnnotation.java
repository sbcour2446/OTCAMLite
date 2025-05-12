package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;

public class DlmStatusAnnotation extends StatusAnnotation {

    public DlmStatusAnnotation(IEntity entity) {
        super(entity);
    }

    @Override
    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder(super.getStatusAnnotation());

        if (getEntity() instanceof IDlmEntity) {
            DlmDisplayManager dlmDisplayManager = ((IDlmEntity) getEntity()).getDlmDisplayManager();
            if (!dlmDisplayManager.getLmsVersionMessage().isBlank()) {
                addRow(stringBuilder, "LMS Version", dlmDisplayManager.getLmsVersionMessage());
            }
            if (!dlmDisplayManager.getOrdVersionMessage().isBlank()) {
                addRow(stringBuilder, "ORD Version", dlmDisplayManager.getOrdVersionMessage());
            }
        }

        return stringBuilder.toString();
    }
}
