package gov.mil.otc._3dvis.playback.dataset.avcad;

import gov.mil.otc._3dvis.datamodel.EntityScope;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.avcad.*;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import javafx.scene.layout.VBox;

import java.io.File;

public class SensorLogImportObject extends ImportObject<SensorLogFile> {

    public static SensorLogImportObject scanAndCreate(File file, AvcadConfiguration avcadConfiguration) {
        SensorLogFile sensorLogFile = new SensorLogFile(file, avcadConfiguration);
        SensorLogImportObject importObject = new SensorLogImportObject(sensorLogFile);
        if (importObject.determineIsNew() && !sensorLogFile.processFile()) {
            importObject.setMissing(true);
        }
        sensorLogFile.validateAlarmAlerts();
        return importObject;
    }

    public SensorLogImportObject(SensorLogFile object) {
        super(object, object.getFile().getName());
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        // not implemented
    }

    @Override
    public void doImport(IEntity entity) {
        if ((!isNew() && !isModified()) || isMissing()) {
            return;
        }

        if (!(entity instanceof SensorEntity sensorEntity)) {
            return;
        }

        for (ConnectionStatus connectionStatus : getObject().getConnectionStatusList()) {
            sensorEntity.addConnectionStatus(connectionStatus);
        }
//        DataSource dataSource = DataManager.createDataSource(getObject().getFile().getAbsolutePath(),
//                getObject().getStartTime(), getObject().getStopTime());

        for (SensorStatus sensorStatus : getObject().getSensorStatusList()) {
            sensorEntity.addSensorStatus(sensorStatus);
//            DataManager.addSensorStatus(sensorStatus, entity.getEntityId(), dataSource.getId());
        }

        for (AlarmAlert alarmAlert : getObject().getAlarmAlertList()) {
            sensorEntity.addAlarmAlert(alarmAlert);
        }

        for (ShutdownStatus shutdownStatus : getObject().getShutdownStatusList()) {
            sensorEntity.addShutdownStatus(shutdownStatus);
        }

        entity.addEntityScope(new EntityScope(getObject().getStartTime(), getObject().getStopTime()));

        setImported(true);
    }
}
