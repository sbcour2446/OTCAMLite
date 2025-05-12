package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.IconImageHelper;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.settings.IconType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SensorEntity extends PlaybackEntity {

    private final TimedDataSet<ConnectionStatus> connectionStatusTimedDataSet = new TimedDataSet<>();
    private final TimedDataSet<SensorStatus> sensorStatusTimedDataSet = new TimedDataSet<>();
    private final List<ISensorStatusListener> sensorStatusListenerList = new ArrayList<>();
    private final TimedDataSet<AlarmAlert> alarmAlertTimedDataSet = new TimedDataSet<>();
    private final List<AlarmAlert> currentAlarmAlertList = new ArrayList<>();
    private final TimedDataSet<ShutdownStatus> shutdownStatusTimedDataSet = new TimedDataSet<>(true);
    private final boolean isRa;
    private boolean inAlert = false;
    private boolean inAlarm = false;
    private boolean isShutdown = false;
    private boolean inScope = false;
    private Color pinColor = Color.GRAY;

    public SensorEntity(EntityId entityId, boolean isRa) {
        super(entityId);
        this.isRa = isRa;
    }

    public void addConnectionStatus(ConnectionStatus connectionStatus) {
        connectionStatusTimedDataSet.add(connectionStatus);
    }

    public void addSensorStatus(SensorStatus sensorStatus) {
        sensorStatusTimedDataSet.add(sensorStatus);
    }

    public void addAlarmAlert(AlarmAlert alarmAlert) {
        alarmAlertTimedDataSet.add(alarmAlert);
    }

    public void addShutdownStatus(ShutdownStatus value) {
        List<ShutdownStatus> shutdownStatuses = shutdownStatusTimedDataSet.getAll();
        shutdownStatusTimedDataSet.clear();
        addToShutdownStatusList(shutdownStatuses, value);

        long currentStartTime = -1;
        long currentEndTime = -1;
        for (ShutdownStatus shutdownStatus : shutdownStatuses) {
            if (currentStartTime == -1) {
                currentStartTime = shutdownStatus.getTimestamp();
                currentEndTime = shutdownStatus.getEndTime();
            } else if (currentEndTime > shutdownStatus.getTimestamp()) {
                currentEndTime = Math.max(currentEndTime, shutdownStatus.getEndTime());
            } else {
                ShutdownStatus newShutdownStatus = new ShutdownStatus(currentStartTime, currentEndTime);
                shutdownStatusTimedDataSet.add(newShutdownStatus);
                currentStartTime = shutdownStatus.getTimestamp();
                currentEndTime = shutdownStatus.getEndTime();
            }
        }
        if (currentStartTime >= 0) {
            ShutdownStatus newShutdownStatus = new ShutdownStatus(currentStartTime, currentEndTime);
            shutdownStatusTimedDataSet.add(newShutdownStatus);
        }
    }

    private void addToShutdownStatusList(List<ShutdownStatus> shutdownStatuses, ShutdownStatus shutdownStatus) {
        for (int i = 0; i < shutdownStatuses.size(); i++) {
            if (shutdownStatus.getTimestamp() < shutdownStatuses.get(i).getTimestamp()) {
                shutdownStatuses.add(i, shutdownStatus);
                return;
            }
        }
        shutdownStatuses.add(shutdownStatus);
    }

    public void addListener(ISensorStatusListener sensorStatusListener) {
        synchronized (sensorStatusListenerList) {
            sensorStatusListenerList.add(sensorStatusListener);
        }
    }

    public void removeListener(ISensorStatusListener sensorStatusListener) {
        synchronized (sensorStatusListenerList) {
            sensorStatusListenerList.remove(sensorStatusListener);
        }
    }

    public SensorStatus getCurrentSensorStatus() {
        return sensorStatusTimedDataSet.getCurrent();
    }

    public List<AlarmAlert> getCurrentAlarmAlertList() {
        return currentAlarmAlertList;
    }

    public boolean isConnected() {
        ConnectionStatus connectionStatus = connectionStatusTimedDataSet.getCurrent();
        return connectionStatus != null && connectionStatus.isConnected();
    }

    public boolean isInAlert() {
        return inAlert;
    }

    public boolean isInAlarm() {
        return inAlarm;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    private boolean updateShutdownStatus(long time) {
        boolean previousValue = isShutdown;
        shutdownStatusTimedDataSet.updateTime(time);
        ShutdownStatus shutdownStatus = shutdownStatusTimedDataSet.getCurrent();
        isShutdown = shutdownStatus != null && shutdownStatus.isShutdown(time);
        return isShutdown != previousValue;
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        boolean hasChange = super.update(time, entityFilter);
        boolean hasStatusChange = sensorStatusTimedDataSet.updateTime(time);
        boolean hasScopeChange = inScope != isInScope();

        connectionStatusTimedDataSet.updateTime(time);
        boolean shutdownStatusChange = updateShutdownStatus(time);

        boolean alarmAlertChange = false;
        alarmAlertTimedDataSet.updateTime(time);
        AlarmAlert currentAlarmAlert = alarmAlertTimedDataSet.getCurrent();
        if (currentAlarmAlert != null && !currentAlarmAlert.isCleared(time) && !currentAlarmAlertList.contains(currentAlarmAlert)) {
            currentAlarmAlertList.add(alarmAlertTimedDataSet.getCurrent());
            alarmAlertChange = true;
        }

        List<AlarmAlert> alarmAlertsCleared = new ArrayList<>();
        for (AlarmAlert alarmAlert : currentAlarmAlertList) {
            if (alarmAlert.isCleared(time)) {
                alarmAlertsCleared.add(alarmAlert);
                if (alarmAlert.isAlert()) {
                    inAlert = false;
                } else {
                    inAlarm = false;
                }
                alarmAlertChange = true;
            } else if (alarmAlert.isAlert()) {
                inAlert = true;
            } else {
                inAlarm = true;
            }
        }
        for (AlarmAlert alarmAlert : alarmAlertsCleared) {
            currentAlarmAlertList.remove(alarmAlert);
        }

        Color newColor = pinColor;
        if (inScope && (alarmAlertChange || shutdownStatusChange)) {
            hasStatusChange = true;
            if (isShutdown) {
                newColor = Color.GRAY;
            } else if (inAlarm) {
                newColor = Color.RED;
            } else if (inAlert) {
                newColor = Color.YELLOW;
            } else {
                newColor = Color.GREEN;
            }
        }

        if (hasScopeChange) {
            inScope = isInScope();
            if (!inScope) {
                newColor = Color.GRAY;
            } else if (newColor == Color.GRAY) {
                newColor = Color.GREEN;
            }
        }

        if (pinColor != newColor) {
            pinColor = newColor;
            resetIcon();
        }

        if (hasStatusChange) {
            notifyListeners(alarmAlertChange);
            updateStatusDisplay();
        }

        return hasChange;
    }

    private void notifyListeners(boolean alarmAlertChange) {
        List<AlarmAlert> alarmAlertList = null;
        if (alarmAlertChange) {
            alarmAlertList = new ArrayList<>(currentAlarmAlertList);
        }
        synchronized (sensorStatusListenerList) {
            for (ISensorStatusListener sensorStatusListener : sensorStatusListenerList) {
                sensorStatusListener.changed(sensorStatusTimedDataSet.getCurrent(), alarmAlertList, alarmAlertChange);
            }
        }
    }

    @Override
    public BufferedImage createIcon() {
        return isRa ? IconImageHelper.getSquareIcon(pinColor, false) :
                IconImageHelper.getPinIcon(pinColor, false);
    }

    @Override
    public IconType getIconType() {
        return isRa ? IconType.SQUARE : IconType.PIN;
    }

    @Override
    protected SensorStatusAnnotation createStatusAnnotation() {
        return new SensorStatusAnnotation(this);
    }
}
