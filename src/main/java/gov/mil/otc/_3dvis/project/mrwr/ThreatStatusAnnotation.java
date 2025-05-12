package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.mil.otc._3dvis.project.blackhawk.BlackHawkEntity;
import gov.mil.otc._3dvis.project.blackhawk.FlightData;

import java.util.Map;

public class ThreatStatusAnnotation extends StatusAnnotation {

    public ThreatStatusAnnotation(IEntity entity) {
        super(entity);
    }

    @Override
    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder(super.getStatusAnnotation());

        if (!(getEntity() instanceof ThreatEntity threatEntity)) {
            return stringBuilder.toString();
        }

        ValuePairTimedData currentStatus = threatEntity.getCurrentStatus();
        if (currentStatus == null) {
            return stringBuilder.toString();
        }

        for (Map.Entry<String, String> entry : currentStatus.getValueMap().entrySet()) {
            addRow(stringBuilder, entry.getKey(), entry.getValue());
        }

        return stringBuilder.toString();
    }
}
