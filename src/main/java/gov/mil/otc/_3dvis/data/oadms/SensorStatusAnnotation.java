package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;

import java.util.Map;

public class SensorStatusAnnotation extends StatusAnnotation {

    private final Sensor sensor;

    public SensorStatusAnnotation(IEntity entity, Sensor sensor) {
        super(entity);
        this.sensor = sensor;
//        setWidth(500);
//        setNameFieldWidth(25);
//        setCharactersPerLine(100);
    }

    @Override
    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder(super.getStatusAnnotation());

        if (sensor != null) {
            Reading reading = sensor.getReadings().getCurrent();
            if (reading != null) {
                for (Map.Entry<String, String> entry : reading.getValues().entrySet()) {
                    String value = entry.getValue();
                    if (value == null) {
                        value = "";
                    }
                    addRow(stringBuilder, entry.getKey(), value);
                }
            }
        }

        return stringBuilder.toString();
    }
}
