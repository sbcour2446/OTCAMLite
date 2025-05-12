package gov.mil.otc._3dvis.entity.base;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.*;
import gov.mil.otc._3dvis.entity.render.EntityPlacemark;
import gov.mil.otc._3dvis.entity.render.EntityTrack;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.media.MediaCollection;
import gov.mil.otc._3dvis.settings.IconType;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.tools.TrackingSettingsController;
import gov.mil.otc._3dvis.ui.utility.SwingUtility;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public abstract class AbstractEntity implements IEntity {

    private final EntityId entityId;
    private final TimedDataSet<EntityScope> entityScopeTimedDataSet = new TimedDataSet<>(true);
    private final TimedDataSet<EntityDetail> entityDetailTimedDataSet = new TimedDataSet<>();
    private final TimedDataSet<TspiData> tspiDataTimedDataSet = new TimedDataSet<>();
    private final TimedDataSet<Event> eventTimedDataSet = new TimedDataSet<>();
    private final TimedDataSet<TimedFile> tirFileSet = new TimedDataSet<>();
    private final MediaCollection mediaCollection = new MediaCollection();
    private final TrackingAttribute trackingAttribute = new TrackingAttribute();
    private final List<IPositionChangeListener> positionChangeListenerList = Collections.synchronizedList(new ArrayList<>());
    private final List<IEntityStatusListener> statusListenerList = Collections.synchronizedList(new ArrayList<>());
    private boolean isFiltered = true; //if true, then show
    private volatile boolean isInScope = false;
    private boolean isTimedOut = false;
    private boolean isShowing = false;
    private boolean isResetIcon = false;
    private boolean isRefreshStatus = false;
    private boolean isMarked = false;
    private boolean isShowLabel = false;
    private Color labelColor = Color.WHITE;
    private Color markColor = Color.WHITE;
    private long timeout = 0;
    private long realtimeStart = -1;
    protected BufferedImage entityIcon = null;
    private EntityPlacemark entityPlacemark;
    private StatusAnnotation statusAnnotation;
    private EntityTrack entityTrack;
    private String entityGroupName = "";
    private EntityDisplay defaultEntityDisplay = new EntityDisplay(this);
    private EntityDisplay entityDisplay = null;

    protected AbstractEntity(EntityId entityId) {
        this.entityId = entityId;
    }

    protected AbstractEntity(AbstractEntity abstractEntity) {
        this.entityId = abstractEntity.entityId;
        synchronized (entityId) {
            entityScopeTimedDataSet.copy(abstractEntity.entityScopeTimedDataSet);
            entityDetailTimedDataSet.copy(abstractEntity.entityDetailTimedDataSet);
            tspiDataTimedDataSet.copy(abstractEntity.tspiDataTimedDataSet);
            eventTimedDataSet.copy(abstractEntity.eventTimedDataSet);
            tirFileSet.copy(abstractEntity.tirFileSet);
            positionChangeListenerList.addAll(abstractEntity.positionChangeListenerList);
            statusListenerList.addAll(abstractEntity.statusListenerList);
            mediaCollection.copy(abstractEntity.mediaCollection);
            isFiltered = abstractEntity.isFiltered;
            isInScope = abstractEntity.isInScope;
            isTimedOut = abstractEntity.isTimedOut;
            isShowing = abstractEntity.isShowing;
            isResetIcon = abstractEntity.isResetIcon;
            isRefreshStatus = abstractEntity.isRefreshStatus;
            isMarked = abstractEntity.isMarked;
            isShowLabel = abstractEntity.isShowLabel;
            labelColor = abstractEntity.labelColor;
            timeout = abstractEntity.timeout;
            realtimeStart = abstractEntity.realtimeStart;
            entityIcon = abstractEntity.entityIcon;
            entityPlacemark = abstractEntity.entityPlacemark;
            if (entityPlacemark != null) {
                entityPlacemark.setEntity(this);
            }
            statusAnnotation = abstractEntity.statusAnnotation;
            if (statusAnnotation != null) {
                statusAnnotation.setEntity(this);
            }
            entityTrack = abstractEntity.entityTrack;
        }
    }

    @Override
    public final EntityId getEntityId() {
        return entityId;
    }

    @Override
    public String getName() {
        EntityDetail entityDetail = getEntityDetail();
        if (entityDetail != null) {
            return entityDetail.getName();
        }

        entityDetail = getEntityDetailBefore(TimeManager.getTime());
        if (entityDetail != null) {
            return entityDetail.getName();
        }

        entityDetail = getEntityDetailAfter(TimeManager.getTime());
        if (entityDetail != null) {
            return entityDetail.getName();
        }

        return entityId.toString();
    }

    @Override
    public void addContextMenuItems(ContextMenu contextMenu) {
        if (supportsRtcaCommands()) {
            Menu menu = new Menu("Send Command");
            contextMenu.getItems().add(menu);
            for (RtcaCommand.Type type : RtcaCommand.Type.values()) {
                MenuItem menuItem = new MenuItem(type.toString());
                menuItem.setOnAction(event -> sendRtcaCommand(new RtcaCommand(this, type)));
                menu.getItems().add(menuItem);
            }
        }

        if (supportsTracking()) {
            Menu trackingMenu = new Menu("Tracking");
            CheckMenuItem checkMenuItem = new CheckMenuItem("Enabled");
            checkMenuItem.setSelected(getTrackingAttribute().isEnabled());
            checkMenuItem.setOnAction(event -> getTrackingAttribute().setEnabled(checkMenuItem.isSelected()));
            trackingMenu.getItems().add(checkMenuItem);
            MenuItem settingsMenuItem = new MenuItem("Settings");
            settingsMenuItem.setOnAction(event -> TrackingSettingsController.show());
            trackingMenu.getItems().add(settingsMenuItem);
            contextMenu.getItems().add(trackingMenu);
        }
    }

    @Override
    public void setRealtimeStart(long timestamp) {
        realtimeStart = timestamp;
    }

    @Override
    public void setRealtimeStop(long timestamp) {
        DataSource dataSource = DataManager.getOrCreateRealTimeDataSource();
        EntityScope entityScope = new EntityScope(realtimeStart, timestamp);
        addEntityScope(entityScope);
        DatabaseLogger.addEntityScope(entityScope, getEntityId(), dataSource.getId());
        realtimeStart = -1;
    }

    @Override
    public void addEntityScope(EntityScope value) {
        List<EntityScope> entityScopeList = entityScopeTimedDataSet.getAll();
        entityScopeTimedDataSet.clear();
        addToList(entityScopeList, value);

        long currentStartTime = -1;
        long currentStopTime = -1;
        for (EntityScope entityScope : entityScopeList) {
            if (currentStartTime == -1) {
                currentStartTime = entityScope.getTimestamp();
                currentStopTime = entityScope.getStopTime();
            } else if (currentStopTime > entityScope.getTimestamp()) {
                currentStopTime = Math.max(currentStopTime, entityScope.getStopTime());
            } else {
                EntityScope newEntityScope = new EntityScope(currentStartTime, currentStopTime);
                entityScopeTimedDataSet.add(newEntityScope);
                currentStartTime = entityScope.getTimestamp();
                currentStopTime = entityScope.getStopTime();
            }
        }
        if (currentStartTime >= 0) {
            EntityScope newEntityScope = new EntityScope(currentStartTime, currentStopTime);
            entityScopeTimedDataSet.add(newEntityScope);
        }
    }

    @Override
    public void addEntityDetail(EntityDetail entityDetail) {
        entityDetailTimedDataSet.add(entityDetail);
    }

    @Override
    public void addTspi(TspiData tspiData) {
        tspiDataTimedDataSet.add(tspiData);
    }

    @Override
    public void addTspiList(List<TspiData> tspiDataList) {
        tspiDataTimedDataSet.addAll(tspiDataList);
    }

    @Override
    public void addEvent(Event event) {
        eventTimedDataSet.add(event);
    }

    @Override
    public void addTir(TimedFile tir) {
        tirFileSet.add(tir);
    }

//    @Override
//    public void addMedia(MediaFile mediaFile) {
//        synchronized (mediaSetList) {
//            for (MediaSet mediaSet : mediaSetList) {
//                if (mediaSet.getName().equals(mediaFile.getMediaSet())) {
//                    mediaSet.addMediaFile(mediaFile);
//                    return;
//                }
//            }
//            MediaSet mediaSet = new MediaSet(mediaFile.getMediaSet());
//            mediaSet.addMediaFile(mediaFile);
//            mediaSetList.add(mediaSet);
//        }
//    }

//    @Override
//    public void addMediaSet(MediaSet mediaSet) {
//        mediaSetList.add(mediaSet);
//    }

    @Override
    public void addPositionChangeListener(IPositionChangeListener positionChangeListener) {
        positionChangeListenerList.add(positionChangeListener);
    }

    @Override
    public void removePositionChangeListener(IPositionChangeListener positionChangeListener) {
        positionChangeListenerList.remove(positionChangeListener);
    }

    public void addStatusListener(IEntityStatusListener entityStatusListener) {
        statusListenerList.add(entityStatusListener);
    }

    public void removeStatusListener(IEntityStatusListener entityStatusListener) {
        statusListenerList.remove(entityStatusListener);
    }

    protected void notifyStatusListeners() {
        synchronized (statusListenerList) {
            for (IEntityStatusListener statusListener : statusListenerList) {
                statusListener.onStatusChange(this);
            }
        }
    }

    @Override
    public EntityDetail getEntityDetail() {
        return entityDetailTimedDataSet.getCurrent();
    }

    @Override
    public EntityDetail getLastEntityDetail() {
        return entityDetailTimedDataSet.getLast();
    }

    @Override
    public EntityDetail getEntityDetailBefore(long timestamp) {
        return entityDetailTimedDataSet.getLastBefore(timestamp);
    }

    @Override
    public EntityDetail getEntityDetailAfter(long timestamp) {
        return entityDetailTimedDataSet.getNextAfter(timestamp);
    }

    @Override
    public TspiData getFirstTspi() {
        return tspiDataTimedDataSet.getFirst();
    }

    @Override
    public TspiData getCurrentTspi() {
        return tspiDataTimedDataSet.getCurrent();
    }

    @Override
    public Position getPosition() {
        TspiData tspiData = tspiDataTimedDataSet.getCurrent();
        if (tspiData != null) {
            return tspiData.getPosition();
        } else {
            return null;
        }
    }

    @Override
    public Position getPositionBefore(long timestamp) {
        TspiData tspiData = tspiDataTimedDataSet.getLastBefore(timestamp);
        if (tspiData != null) {
            return tspiData.getPosition();
        } else {
            return null;
        }
    }

    @Override
    public long getLastUpdateTime() {
        long tspiTime;
        TspiData tspiData = tspiDataTimedDataSet.getCurrent();
        if (tspiData != null) {
            tspiTime = tspiData.getTimestamp();
        } else {
            tspiTime = 0;
        }

        long entityDetailTime;
        EntityDetail entityDetail = getEntityDetail();
        if (entityDetail != null) {
            entityDetailTime = getEntityDetail().getTimestamp();
        } else {
            entityDetailTime = 0;
        }

        return Math.max(tspiTime, entityDetailTime);
    }

    @Override
    public Event getLastEvent() {
        return eventTimedDataSet.getCurrent();
    }

    @Override
    public TimedFile getLastTir() {
        return tirFileSet.getCurrent();
    }

    @Override
    public List<TimedFile> getAllTirs() {
        return tirFileSet.getAll();
    }

//    @Override
//    public boolean hasMedia() {
//        return !mediaSetList.isEmpty();
//    }
//
//    @Override
//    public List<MediaSet> getMedia() {
//        return mediaSetList;
//    }

    @Override
    public MediaCollection getMediaCollection() {
        return mediaCollection;
    }

    @Override
    public boolean isInScope() {
        return isInScope;
    }

    @Override
    public boolean isTimedOut() {
        return isTimedOut;
    }

    @Override
    public boolean isFiltered() {
        return isFiltered;
    }

    @Override
    public boolean isAircraft() {
        EntityDetail entityDetail = getEntityDetail();
        if (entityDetail != null) {
            return entityDetail.getEntityType().getDomain() == 2;
        }
        return false;
    }

    @Override
    public boolean isLifeForm() {
        EntityDetail entityDetail = getEntityDetail();
        if (entityDetail != null) {
            return entityDetail.getEntityType().getKind() == 3;
        }
        return false;
    }

    @Override
    public boolean isMunition() {
        EntityDetail entityDetail = getEntityDetail();
        if (entityDetail != null) {
            return entityDetail.getEntityType().getKind() == 2;
        }
        return false;
    }

    @Override
    public boolean isPlatform() {
        EntityDetail entityDetail = getEntityDetail();
        if (entityDetail != null) {
            return entityDetail.getEntityType().getKind() == 1;
        }
        return false;
    }

    @Override
    public TrackingAttribute getTrackingAttribute() {
        return trackingAttribute;
    }

    @Override
    public List<Position> getTracks() {
        List<Position> positions = new ArrayList<>();
        for (TspiData tspiData : tspiDataTimedDataSet.getHistory(trackingAttribute.getCutoff())) {
            positions.add(tspiData.getPosition());
        }
        return positions;
    }

    @Override
    public void toggleStatusAnnotation() {
        if (statusAnnotation == null) {
            statusAnnotation = createStatusAnnotation();
            EntityLayer.add(statusAnnotation);
        } else {
            EntityLayer.remove(statusAnnotation);
            statusAnnotation = null;
        }
    }

    @Override
    public BufferedImage createIcon() {
        return IconImageHelper.getIcon(this);
    }

    @Override
    public BufferedImage getIcon() {
        return entityIcon;
    }

    @Override
    public IconType getIconType() {
        if (isPlatform()) {
            return IconType.PLATFORM;
        } else if (isLifeForm()) {
            return IconType.LIFE_FORM;
        } else if (isMunition()) {
            return IconType.MUNITION;
        } else {
            return IconType.OTHER;
        }
    }

    @Override
    public void resetIcon() {
        isResetIcon = true;
    }

    @Override
    public void refreshStatus() {
        isRefreshStatus = true;
    }

    @Override
    public String getEntityGroupName() {
        return entityGroupName;
    }

    @Override
    public EntityDisplay getEntityDisplay() {
        return entityDisplay == null ? defaultEntityDisplay : entityDisplay;
    }

    @Override
    public void setEntityGroup(String entityGroupName, EntityDisplay entityDisplay) {
        this.entityGroupName = entityGroupName;

        if (!Objects.equals(this.entityDisplay, entityDisplay)) {
            this.entityDisplay = entityDisplay;
            resetIcon();
        }
    }

    @Override
    public void removeEntityGroup() {
        entityGroupName = "";
        if (entityDisplay != null) {
            entityDisplay = null;
            resetIcon();
        }
    }

    @Override
    public void setSelected(final boolean selected) {
        if (entityPlacemark != null) {
            SwingUtilities.invokeLater(() -> {
                if (entityPlacemark != null) {
                    entityPlacemark.setHighlighted(selected);
                }
            });
        }
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        synchronized (entityId) {
            boolean hasScopeChange = updateIsInScope(time);
            boolean hasDetailChange = entityDetailTimedDataSet.updateTime(time);
            boolean hasEventChange = eventTimedDataSet.updateTime(time);
            boolean hasTspiChange = tspiDataTimedDataSet.updateTime(time);
            boolean hasTirChange = tirFileSet.updateTime(time);
            boolean hasTimedOutChange;
            boolean hasFilterChange = false;

            if (hasTspiChange) {
                synchronized (positionChangeListenerList) {
                    for (IPositionChangeListener listener : positionChangeListenerList) {
                        listener.onPositionChange(getPosition());
                    }
                }
            }

            hasTimedOutChange = updateTimedOut(time, hasDetailChange);

            if (hasDetailChange || hasScopeChange || hasTimedOutChange || entityFilter.isFilterModified()) {
                hasFilterChange = checkFilter(entityFilter);
            }

            updateDisplay(hasDetailChange, hasEventChange, hasScopeChange, hasTspiChange, trackingAttribute.isModified());

            return hasDetailChange || hasEventChange || hasScopeChange || hasTspiChange || hasTimedOutChange || hasFilterChange;
        }
    }

    @Override
    public void dispose() {
        synchronized (entityId) {
            if (isShowing) {
                removeDisplay();
            }
            entityScopeTimedDataSet.clear();
            entityDetailTimedDataSet.clear();
            tspiDataTimedDataSet.clear();
            eventTimedDataSet.clear();
            tirFileSet.clear();
//            mediaSetList.clear();
            positionChangeListenerList.clear();
            statusListenerList.clear();
        }
    }

    protected StatusAnnotation createStatusAnnotation() {
        return new StatusAnnotation(this);
    }

    protected void setClampToGround(final boolean groundClamp) {
        if (entityPlacemark != null &&
                groundClamp != (entityPlacemark.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)) {
            SwingUtilities.invokeLater(() -> {
                if (groundClamp) {
                    entityPlacemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                } else {
                    entityPlacemark.setAltitudeMode(WorldWind.ABSOLUTE);
                }
            });
        }

        if (statusAnnotation != null &&
                groundClamp != (statusAnnotation.getAltitudeMode() == WorldWind.CLAMP_TO_GROUND)) {
            SwingUtilities.invokeLater(() -> {
                if (groundClamp) {
                    statusAnnotation.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                } else {
                    statusAnnotation.setAltitudeMode(WorldWind.ABSOLUTE);
                }
            });
        }
    }

    private void updateDisplay(boolean detailChange, boolean eventChange, boolean scopeChange, boolean tspiChange,
                               boolean isTrackingAttributeModified) {
        Position position = getPosition();
        boolean show = position != null && getEntityDetail() != null && isFiltered;
        if (show && !isShowing) {
            showDisplay();
        } else if (show) {
            if (detailChange || scopeChange || isResetIcon) {
                isResetIcon = false;
                updateIcon();
            }
            if (tspiChange || (entityPlacemark != null && !entityPlacemark.getPosition().equals(position))) {
                updateDisplayPosition(position);
            }
            if (detailChange || eventChange || tspiChange || isRefreshStatus) {
                isRefreshStatus = false;
                updateStatusDisplay();
            }
            if (isTrackingAttributeModified) {
                updateTracking();
            }
        } else if (isShowing) {
            removeDisplay();
        }
    }

    private void showDisplay() {
        isShowing = true;
        if (entityPlacemark == null) {
            entityPlacemark = new EntityPlacemark(this);
        }
        EntityLayer.add(entityPlacemark);
        updateTracking();
        updateIcon();
    }

    private void removeDisplay() {
        isShowing = false;
        EntityLayer.remove(entityPlacemark);
        if (statusAnnotation != null) {
            toggleStatusAnnotation();
        }
        if (entityTrack != null) {
            EntityLayer.remove(entityTrack);
            entityTrack = null;
        }
    }

    private void updateIcon() {
        entityIcon = null;
        defaultEntityDisplay = new EntityDisplay(this);

        SwingUtilities.invokeLater(() -> {
            if (entityPlacemark != null) {
                entityPlacemark.reset();
            }
        });
    }

    private void updateDisplayPosition(final Position position) {
        SwingUtilities.invokeLater(() -> {
            if (entityPlacemark != null) {
                entityPlacemark.setPosition(position);
            }
            if (statusAnnotation != null) {
                if (isMunition() || isAircraft()) {
                    statusAnnotation.setPosition(position);
                } else {
                    statusAnnotation.setPosition(new Position(position, 0));
                }
            }
            if (entityTrack != null) {
                entityTrack.setPositions(getTracks());
            }
        });
    }

    protected void updateStatusDisplay() {
        SwingUtilities.invokeLater(() -> {
            if (statusAnnotation != null) {
                statusAnnotation.updateText();
            }
        });
    }

    private void updateTracking() {
        if (trackingAttribute.isEnabled() && entityTrack == null) {
            entityTrack = EntityTrack.createTrack(this, entityPlacemark);
            EntityLayer.add(entityTrack);
        }
        if (!trackingAttribute.isEnabled() && entityTrack != null) {
            EntityLayer.remove(entityTrack);
            entityTrack = null;
        }
        if (entityTrack != null) {
            Material material = new Material(SwingUtility.toAwtColor(getTrackingAttribute().getColor()));
            entityTrack.getAttributes().setOutlineMaterial(material);
            entityTrack.getAttributes().setInteriorMaterial(entityTrack.getAttributes().getOutlineMaterial());
        }
    }

    @Override
    public boolean supportsRtcaCommands() {
        //override if supported
        return false;
    }

    @Override
    public boolean supportsTracking() {
        return true;
    }

    @Override
    public void sendRtcaCommand(RtcaCommand rtcaCommand) {
        //override or do nothing
    }

    @Override
    public String toString() {
        return entityId.toString();
    }

    private boolean updateTimedOut(long time, boolean hasDetailChange) {
        if (hasDetailChange) {
            if (isMunition()) {
                timeout = SettingsManager.getSettings().getMunitionTimeout();
            } else {
                timeout = 0;
            }
        }
        boolean previousTimedOut = isTimedOut;
        isTimedOut = timeout > 0 && (time - getLastUpdateTime()) > timeout;
        return previousTimedOut != isTimedOut;
    }

    private boolean checkFilter(EntityFilter entityFilter) {
        boolean previousValue = isFiltered;
        isFiltered = entityFilter.isFiltered(this);
        return previousValue != isFiltered;
    }

    private boolean updateIsInScope(long time) {
        boolean previousValue = isInScope;

        entityScopeTimedDataSet.updateTime(time);

        if (realtimeStart >= 0 && time > realtimeStart) {
            isInScope = true;
        } else {
            EntityScope entityScope = entityScopeTimedDataSet.getCurrent();
            isInScope = entityScope != null && entityScope.getTimestamp() < time && time < entityScope.getStopTime();
        }

        return isInScope != previousValue;
    }

    private void addToList(List<EntityScope> entityScopeList, EntityScope entityScope) {
        for (int i = 0; i < entityScopeList.size(); i++) {
            if (entityScope.getTimestamp() < entityScopeList.get(i).getTimestamp()) {
                entityScopeList.add(i, entityScope);
                return;
            }
        }
        entityScopeList.add(entityScope);
    }

    public List<EntityScope> getAllEntityScopes() {
        return entityScopeTimedDataSet.getAll();
    }

    public List<EntityDetail> getAllEntityDetails() {
        return entityDetailTimedDataSet.getAll();
    }

    public List<TspiData> getAllTspiData() {
        return tspiDataTimedDataSet.getAll();
    }

    public List<Event> getAllEvents() {
        return eventTimedDataSet.getAll();
    }
}
