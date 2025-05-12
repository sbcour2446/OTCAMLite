package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.mil.otc._3dvis.utility.Utility;

import java.util.List;

public class SensorStatusAnnotation extends StatusAnnotation {

    public SensorStatusAnnotation(SensorEntity entity) {
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

        SensorEntity sensorEntity = (SensorEntity) getEntity();
        SensorStatus sensorStatus = sensorEntity.getCurrentSensorStatus();
        if (sensorStatus != null) {
            addRow(stringBuilder, "Timestamp", Utility.formatTime(sensorStatus.getTimestamp()));
            addRow(stringBuilder, "Result", sensorStatus.getResult());
            addRow(stringBuilder, "Cleared", sensorStatus.getConditionCleared());

            List<AlarmAlert> alarmAlerts =sensorEntity.getCurrentAlarmAlertList();
            StringBuilder text = new StringBuilder();
            String prefix = "";
            for (AlarmAlert alarmAlert : alarmAlerts) {
                text.append(prefix);
                text.append(alarmAlert.toString());
                prefix = ", ";
            }
            addRow(stringBuilder, "Alarm/Alert", text.toString());

            if (!sensorStatus.getScanType().equalsIgnoreCase("none")) {
                addRow(stringBuilder, "Scan Type", sensorStatus.getScanType());
            }
        }

        return stringBuilder.toString();
    }
}
