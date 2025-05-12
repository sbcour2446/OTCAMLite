package gov.mil.otc._3dvis.tools.rangefinder;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

import java.util.*;

public class RangeFinderEntry {

    private final RangeFinderEntity source;
    private final RangeFinderEntity target;
    private final String timerName;
    private final IntegerProperty minRange = new SimpleIntegerProperty(0);
    private final IntegerProperty maxRange = new SimpleIntegerProperty(10000);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>();
    private final BooleanProperty enabled = new SimpleBooleanProperty(false);
    private final BooleanProperty showLines = new SimpleBooleanProperty(false);
    private final BooleanProperty followTerrain = new SimpleBooleanProperty(false);
    private final BooleanProperty ignoreFilters = new SimpleBooleanProperty(false);
    private final List<RangeLine> rangeLines = Collections.synchronizedList(new ArrayList<>());
    private final List<IRangeLineListener> entryListenerList = Collections.synchronizedList(new ArrayList<>());
    private Timer updateTimer;

    public RangeFinderEntry(RangeFinderEntity source, RangeFinderEntity target) {
        this.source = source;
        this.target = target;
        timerName = String.format("RangeFinderEntry:updateTimer %s:%s", source.toString(), target.toString());
    }

    public RangeFinderEntity getSource() {
        return source;
    }

    public RangeFinderEntity getTarget() {
        return target;
    }

    public int getMinRange() {
        return minRange.get();
    }

    public IntegerProperty minRangeProperty() {
        return minRange;
    }

    public void setMinRange(int minRange) {
        this.minRange.set(minRange);
    }

    public int getMaxRange() {
        return maxRange.get();
    }

    public IntegerProperty maxRangeProperty() {
        return maxRange;
    }

    public void setMaxRange(int maxRange) {
        this.maxRange.set(maxRange);
    }

    public Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public BooleanProperty enabledProperty() {
        handleEnabledChange();
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
        handleEnabledChange();
    }

    public boolean getShowLines() {
        return showLines.get();
    }

    public BooleanProperty showLinesProperty() {
        return showLines;
    }

    public void setShowLines(boolean showLines) {
        this.showLines.set(showLines);
    }

    public boolean isFollowTerrain() {
        return followTerrain.get();
    }

    public BooleanProperty followTerrainProperty() {
        return followTerrain;
    }

    public void setFollowTerrain(boolean followTerrain) {
        this.followTerrain.set(followTerrain);
    }

    public boolean isIgnoreFilters() {
        return ignoreFilters.get();
    }

    public BooleanProperty ignoreFiltersProperty() {
        return ignoreFilters;
    }

    public void setIgnoreFilters(boolean ignoreFilters) {
        this.ignoreFilters.set(ignoreFilters);
    }

    public List<RangeLine> getRangeLines() {
        return rangeLines;
    }

    public void updateRangeLines() {
        synchronized (rangeLines) {
            for (RangeLine rangeLine : rangeLines) {
                rangeLine.updateAttributes();
            }
        }
    }

    public void addEntryListener(IRangeLineListener entryListener) {
        entryListenerList.add(entryListener);
    }

    public void removeEntryListener(IRangeLineListener entryListener) {
        entryListenerList.remove(entryListener);
    }

    public void notifyRangeLineAdded(RangeLine rangeLine) {
        synchronized (entryListenerList) {
            for (IRangeLineListener entryListener : entryListenerList) {
                entryListener.onRangeLineAdded(rangeLine);
            }
        }
    }

    public void notifyRangeLineRemoved(RangeLine rangeLine) {
        synchronized (entryListenerList) {
            for (IRangeLineListener entryListener : entryListenerList) {
                entryListener.onRangeLineRemoved(rangeLine);
            }
        }
    }

    private synchronized void handleEnabledChange() {
        if (enabled.get()) {
            scheduleUpdateTimer();
        } else {
            cancelUpdateTimer();
            removeAllRangeLines();
        }
    }

    private synchronized void scheduleUpdateTimer() {
        if (updateTimer == null) {
            updateTimer = new Timer(timerName);
            updateTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    update();
                }
            }, 0, 1000);
        }
    }

    private synchronized void cancelUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    private void update() {
        if (!isEnabled()) {
            return;
        }

        List<RangeLine> rangeLinesToRemove = new ArrayList<>(rangeLines);

        IEntity sourceEntity = EntityManager.getEntity(source.getEntityId());
        if (!validateSourcePosition(sourceEntity)) {
            removeRangeLines(rangeLinesToRemove);
            return;
        }

        List<IEntity> targetEntities = getTargetEntities();
        for (IEntity targetEntity : targetEntities) {
            if (!targetEntity.isFiltered()) {
                continue;
            }
            RangeLine rangeLine = processTarget(sourceEntity, targetEntity);
            if (rangeLine != null) {
                if (rangeLines.contains(rangeLine)) {
                    rangeLinesToRemove.remove(rangeLine);
                } else {
                    rangeLine.activate();
                    rangeLines.add(rangeLine);
                    notifyRangeLineAdded(rangeLine);
                }
            }
        }

        removeRangeLines(rangeLinesToRemove);
    }

    private boolean validateSourcePosition(IEntity sourceEntity) {
        return sourceEntity != null && sourceEntity.getPosition() != null;
    }

    private List<IEntity> getTargetEntities() {
        if (target.isAll()) {
            return EntityManager.getEntities();
        } else {
            return List.of(EntityManager.getEntity(target.getEntityId()));
        }
    }

    private RangeLine processTarget(IEntity sourceEntity, IEntity targetEntity) {
        Position sourcePosition = sourceEntity.getPosition();
        Position targetPosition = targetEntity.getPosition();
        if (sourcePosition != null && targetPosition != null) {
            double distance1 = Utility.calculateDistance1(sourcePosition, targetPosition);
            if (!sourceEntity.isMunition() && !sourceEntity.isAircraft()) {
                sourcePosition = new Position(sourcePosition,
                        WWController.getGlobe().getElevation(sourcePosition.latitude, sourcePosition.longitude));
            }
            if (!targetEntity.isMunition() && !targetEntity.isAircraft()) {
                targetPosition = new Position(targetPosition,
                        WWController.getGlobe().getElevation(targetPosition.latitude, targetPosition.longitude));
            }
            double distance2 = Utility.calculateDistance2(sourcePosition, targetPosition);
            if (distance1 >= minRange.get() && distance1 <= maxRange.get()) {
                RangeLine rangeLine = new RangeLine(this, sourceEntity, targetEntity);
                rangeLine.setSlantRange(distance1);
                rangeLine.setPathDistance(distance2);
                return rangeLine;
            }
        }
        return null;
    }

    private void removeAllRangeLines() {
        synchronized (rangeLines) {
            for (RangeLine rangeLine : rangeLines) {
                rangeLine.dispose();
                notifyRangeLineRemoved(rangeLine);
            }
            rangeLines.clear();
        }
    }

    private void removeRangeLines(List<RangeLine> rangeLinesToRemove) {
        for (RangeLine rangeLine : rangeLinesToRemove) {
            rangeLine.dispose();
            rangeLines.remove(rangeLine);
            notifyRangeLineRemoved(rangeLine);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RangeFinderEntry rangeFinderEntry = (RangeFinderEntry) o;
        return source.equals(rangeFinderEntry.source) &&
                target.equals(rangeFinderEntry.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

}
