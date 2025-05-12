package gov.mil.otc._3dvis.project.rpuas;

import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;

import java.util.Map;

public class RpuasStatusAnnotation extends StatusAnnotation {


    public RpuasStatusAnnotation(RpuasEntity entity) {
        super(entity);
    }

    @Override
    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder(super.getStatusAnnotation());
        return stringBuilder.toString();
    }
}
