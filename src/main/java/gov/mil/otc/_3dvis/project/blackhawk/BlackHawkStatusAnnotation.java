package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.mil.otc._3dvis.project.dlm.DlmDisplayManager;
import gov.mil.otc._3dvis.project.dlm.IDlmEntity;

public class BlackHawkStatusAnnotation extends StatusAnnotation {

    public BlackHawkStatusAnnotation(IEntity entity) {
        super(entity);
    }

    @Override
    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder(super.getStatusAnnotation());

        if (getEntity() instanceof BlackHawkEntity blackHawkEntity) {
            FlightData flightData = blackHawkEntity.getFlightData();
            if (flightData != null) {
                addRow(stringBuilder, "Pressure Alt", String.format("%f", flightData.getPressureAltitude()));
                addRow(stringBuilder, "Radar Alt", String.format("%f", flightData.getRadarAltitude()));
                addRow(stringBuilder, "Heading", String.format("%f", flightData.getHeading()));
                addRow(stringBuilder, "Airspeed", String.format("%f", flightData.getAirspeed()));
                addRow(stringBuilder, "Wheel Status", flightData.getWheelStatus());
            }
        }

        return stringBuilder.toString();
    }
}
