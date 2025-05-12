package gov.mil.otc._3dvis.project.shadow;

import gov.mil.otc._3dvis.datamodel.aircraft.TspiExtendedData;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.mil.otc._3dvis.project.blackhawk.BlackHawkEntity;
import gov.mil.otc._3dvis.project.blackhawk.FlightData;

public class ShadowStatusAnnotation extends StatusAnnotation {

    public ShadowStatusAnnotation(IEntity entity) {
        super(entity);
    }

    @Override
    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder(super.getStatusAnnotation());

        if (getEntity() instanceof ShadowEntity) {
            TspiExtendedData tspiExtendedData = ((ShadowEntity)getEntity()).getTspiExtendedData();
            if (tspiExtendedData != null) {
                addRow(stringBuilder, "Altitude Type", String.format("%f", tspiExtendedData.getAltitudeType()));
            }
            UasPayloadData uasPayloadData = ((ShadowEntity)getEntity()).getUasPayloadData();
            if (uasPayloadData != null) {
                addRow(stringBuilder, "Center Az", String.format("%f", uasPayloadData.getActCenterAzAngle()));
            }
        }

        return stringBuilder.toString();
    }
}
