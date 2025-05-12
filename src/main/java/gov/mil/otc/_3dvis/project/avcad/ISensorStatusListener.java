package gov.mil.otc._3dvis.project.avcad;

import java.util.List;

public interface ISensorStatusListener {

    void changed(SensorStatus sensorStatus, List<AlarmAlert> alarmAlerts, boolean alarmAlertChange);
}
