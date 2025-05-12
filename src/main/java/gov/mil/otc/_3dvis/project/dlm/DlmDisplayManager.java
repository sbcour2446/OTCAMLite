package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.datamodel.timed.BooleanTimedData;
import gov.mil.otc._3dvis.datamodel.timed.BooleanTimedDataSet;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.project.dlm.message.*;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.worldwindex.render.Circle;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DlmDisplayManager {

    private final Circle radarCircle = new Circle(Position.ZERO, SettingsManager.getSettings().getDlmSetting().getRadarRadius());
    private final Circle launchCircle = new Circle(Position.ZERO, SettingsManager.getSettings().getDlmSetting().getLaunchRadius());
    private final ConcurrentHashMap<Integer, Range> rangeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Target> targetMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Track> trackMap = new ConcurrentHashMap<>();
    private final BooleanTimedDataSet radarStatusTimedDataSet = new BooleanTimedDataSet();
    private final Map<Iteration, List<Launch>> launchMap = new ConcurrentHashMap<>();
    private final List<Launch> undefinedLaunchList = Collections.synchronizedList(new ArrayList<>());
    private boolean initialized = false;
    private boolean showing = false;
    private Position entityPosition;
    private boolean isDisplayable;
    private String lmsVersionMessage = "";
    private String ordVersionMessage = "";

    public DlmDisplayManager() {
        DataManager.getIterationManager().addListener(this::updateLaunchCounts);
        DataManager.getReportManager().addAvailableReport(new DlmLaunchReport());
        initializeIterations();
    }

    public List<Launch> getLaunchList() {
        List<Launch> allLaunches = new ArrayList<>();
        synchronized (launchMap) {
            for (List<Launch> launchList : launchMap.values()) {
                allLaunches.addAll(launchList);
            }
            allLaunches.addAll(undefinedLaunchList);
        }
        return allLaunches;
    }

    public List<Launch> getLaunchList(Iteration iteration) {
        if (iteration != null) {
            List<Launch> launches = launchMap.get(iteration);
            if (launches != null) {
                return new ArrayList<>(launches);
            }
        } else {
            return new ArrayList<>(undefinedLaunchList);
        }
        return new ArrayList<>();
    }


    protected void update(long time, Position entityPosition, boolean isDisplayable) {
        this.entityPosition = entityPosition;
        this.isDisplayable = isDisplayable;

        if (!initialized) {
            initialize();
        }

        updateReferences();

        if (radarStatusTimedDataSet.updateTime(time) && radarStatusTimedDataSet.getCurrent() != null) {
            updateRadarStatus(radarStatusTimedDataSet.getCurrent().isValue());
        }

        updateRanges(time);
        updateTargets(time);
        updateTracks(time);
        updateLaunchStatus(time);
    }

    protected void dispose() {
        if (showing) {
            showing = false;
            EntityLayer.remove(radarCircle);
            EntityLayer.remove(launchCircle);
        }
        synchronized (rangeMap) {
            for (Range range : rangeMap.values()) {
                range.dispose();
            }
        }
        synchronized (targetMap) {
            for (Target target : targetMap.values()) {
                target.dispose();
            }
        }
        synchronized (trackMap) {
            for (Track track : trackMap.values()) {
                track.dispose();
            }
        }
        synchronized (launchMap) {
            for (List<Launch> launchList : launchMap.values()) {
                for (Launch launch : launchList) {
                    launch.dispose();
                }
            }
        }
        synchronized (undefinedLaunchList) {
            for (Launch launch : undefinedLaunchList) {
                launch.dispose();
            }
        }
    }

    protected boolean processMessage(DlmMessage dlmMessage, long messageTimeOverride) {
        if (dlmMessage instanceof DwellMessage) {
            processDwellMessage((DwellMessage) dlmMessage, messageTimeOverride);
        } else if (dlmMessage instanceof FusionMessage) {
            processFusionMessage((FusionMessage) dlmMessage, messageTimeOverride);
        } else if (dlmMessage instanceof LaunchDiscreteTimeMessage) {
            processLaunchDiscreteTimeMessage((LaunchDiscreteTimeMessage) dlmMessage, messageTimeOverride);
        } else if (dlmMessage instanceof PingRadarStatusMessage) {
            processPingRadarStatusMessage((PingRadarStatusMessage) dlmMessage, messageTimeOverride);
        } else if (dlmMessage instanceof TrackMessage) {
            processTrackMessage((TrackMessage) dlmMessage, messageTimeOverride);
        } else if (dlmMessage instanceof LmsVersionMessage) {
            lmsVersionMessage = ((LmsVersionMessage) dlmMessage).getSoftwareVersion();
        } else if (dlmMessage instanceof OrdVersionMessage) {
            ordVersionMessage = ((OrdVersionMessage) dlmMessage).getSoftwareVersion();
        } else {
            if (dlmMessage instanceof GenericDlmMessage && Logger.getGlobal().isLoggable(Level.INFO)) {
                String logMessage = String.format("%x", ((GenericDlmMessage) dlmMessage).getMessageType());
                Logger.getGlobal().log(Level.INFO, logMessage);
            }
            return false;
        }
        return true;
    }

    protected String getLmsVersionMessage() {
        return lmsVersionMessage;
    }

    protected String getOrdVersionMessage() {
        return ordVersionMessage;
    }

    private void processDwellMessage(DwellMessage dwellMessage, long messageTimeOverride) {
        long timestamp = messageTimeOverride > 0 ? messageTimeOverride : dwellMessage.getTimestamp();
        for (DwellMessage.ActiveTarget activeTarget : dwellMessage.getActiveTargets()) {
            if (activeTarget.getUniqueId() != 0 && activeTarget.getActive() != 0) {
                Range range = rangeMap.get(activeTarget.getUniqueId());
                if (range == null) {
                    range = new Range(activeTarget.getUniqueId(), entityPosition);
                    rangeMap.put(activeTarget.getUniqueId(), range);
                }
                range.addRangeData(timestamp, activeTarget.getDetectRange() * 0.0625f);
            }
        }
    }

    private void processFusionMessage(FusionMessage fusionMessage, long messageTimeOverride) {
        long timestamp = messageTimeOverride > 0 ? messageTimeOverride : fusionMessage.getTimestamp();
        radarStatusTimedDataSet.addIfChange(new BooleanTimedData(timestamp, fusionMessage.getRadarEnable() == 1));
        for (FusionMessage.Track fusionMessageTrack : fusionMessage.getTrackList()) {
            Target target = targetMap.get(fusionMessageTrack.getTargetId());
            if (target == null) {
                target = new Target(fusionMessageTrack.getTargetId(), 0, entityPosition);
                targetMap.put(fusionMessageTrack.getTargetId(), target);
            }
            target.addTargetData(timestamp,
                    fusionMessageTrack.getTrackX(),
                    fusionMessageTrack.getTrackY(),
                    fusionMessageTrack.getxDot(),
                    fusionMessageTrack.getyDot());
        }
    }

    private void processLaunchDiscreteTimeMessage(LaunchDiscreteTimeMessage launchDiscreteTimeMessage, long messageTimeOverride) {
        long timestamp = messageTimeOverride > 0 ? messageTimeOverride : launchDiscreteTimeMessage.getTimestamp();
        addLaunch(new Launch(timestamp, 0));
    }

    private void processPingRadarStatusMessage(PingRadarStatusMessage pingRadarStatusMessage, long messageTimeOverride) {
        radarStatusTimedDataSet.addIfChange(new BooleanTimedData(messageTimeOverride, pingRadarStatusMessage.isRadarOn()));
    }

    private void processTrackMessage(TrackMessage trackMessage, long messageTimeOverride) {
        for (TrackMessage.Track trackMessageTrack : trackMessage.getTrackList()) {
            Track track = trackMap.get(trackMessageTrack.getTrackNumber());
            if (track == null) {
                track = new Track(trackMessageTrack.getTrackNumber(), entityPosition);
                trackMap.put(trackMessageTrack.getTrackNumber(), track);
            }
            long timestamp = messageTimeOverride > 0 ? messageTimeOverride : trackMessageTrack.getTimestamp();
            track.addTrackData(timestamp,
                    trackMessageTrack.getBearing(),
                    trackMessageTrack.getTrackQuality(),
                    trackMessageTrack.getGenClass());
        }
    }

    private void updateRadarStatus(final boolean isOn) {
        SwingUtilities.invokeLater(() -> radarCircle.getAttributes().setOutlineStippleFactor(isOn ? 0 : 2));
    }

    private void initialize() {
        initialized = true;

        ShapeAttributes radarCircleAttributes = new BasicShapeAttributes();
        radarCircleAttributes.setInteriorOpacity(.5);
        radarCircleAttributes.setDrawInterior(true);
        radarCircleAttributes.setDrawOutline(true);
        radarCircleAttributes.setOutlineWidth(3);
        radarCircleAttributes.setOutlineStippleFactor(2);
        radarCircleAttributes.setOutlineMaterial(new Material(Color.WHITE));
        radarCircleAttributes.setInteriorMaterial(new Material(Color.WHITE));
        radarCircle.setAttributes(radarCircleAttributes);
        radarCircle.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        radarCircle.setOffset(1);
        radarCircle.setValue(AVKey.ROLLOVER_TEXT, "150 Meters");

        ShapeAttributes launchCircleAttributes = radarCircleAttributes.copy();
        launchCircleAttributes.setInteriorMaterial(new Material(Color.ORANGE));
        launchCircleAttributes.setOutlineMaterial(new Material(Color.ORANGE));
        launchCircle.setAttributes(launchCircleAttributes);
        launchCircle.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        launchCircle.setOffset(1);
        launchCircle.setValue(AVKey.ROLLOVER_TEXT, "50 Meters");
    }

    private void updateReferences() {
        if (isDisplayable) {
            if (!showing) {
                showing = true;
                EntityLayer.add(radarCircle);
                EntityLayer.add(launchCircle);
            } else {
                updateVisualPosition(entityPosition);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(radarCircle);
            EntityLayer.remove(launchCircle);
        }
    }

    private void updateVisualPosition(final Position position) {
        SwingUtilities.invokeLater(() -> {
            radarCircle.setCenter(position);
            launchCircle.setCenter(position);
        });
    }

    private void updateRanges(long time) {
        synchronized (rangeMap) {
            for (Range range : rangeMap.values()) {
                range.update(time, entityPosition, isDisplayable);
            }
        }
    }

    private void updateTargets(long time) {
        synchronized (targetMap) {
            for (Target target : targetMap.values()) {
                target.update(time, entityPosition, isDisplayable);
            }
        }
    }

    private void updateTracks(long time) {
        synchronized (trackMap) {
            for (Track track : trackMap.values()) {
                track.update(time, entityPosition, isDisplayable);
            }
        }
    }

    private void updateLaunchStatus(long time) {
        synchronized (launchMap) {
            for (List<Launch> launchList : launchMap.values()) {
                for (Launch launch : launchList) {
                    launch.update(time, entityPosition, isDisplayable);
                }
            }
        }
        synchronized (undefinedLaunchList) {
            for (Launch launch : undefinedLaunchList) {
                launch.update(time, entityPosition, isDisplayable);
            }
        }
    }

    private void updateLaunchCounts(List<Iteration> iterationList) {
        synchronized (launchMap) {
            List<Launch> allLaunches = new ArrayList<>();
            for (List<Launch> launchList : launchMap.values()) {
                allLaunches.addAll(launchList);
            }
            allLaunches.addAll(undefinedLaunchList);
            launchMap.clear();
            undefinedLaunchList.clear();
            for (Iteration iteration : iterationList) {
                launchMap.put(iteration, new ArrayList<>());
            }

            for (Launch launch : allLaunches) {
                addLaunch(launch);
            }
        }
    }

    private void addLaunch(Launch launch) {
        boolean iterationFound = false;
        synchronized (launchMap) {
            for (Map.Entry<Iteration, List<Launch>> entry : launchMap.entrySet()) {
                if (entry.getKey().inIteration(launch.getStartTime())) {
                    addAndUpdateLaunch(launch, entry.getValue());
                    iterationFound = true;
                }
            }
        }
        if (!iterationFound) {
            synchronized (undefinedLaunchList) {
                addAndUpdateLaunch(launch, undefinedLaunchList);
            }
        }
    }

    private void addAndUpdateLaunch(Launch launch, List<Launch> launchList) {
        boolean launchInserted = false;
        for (int i = 0; i < launchList.size(); i++) {
            if (launchInserted) {
                launchList.get(i).setLaunchNumber(i);
            } else if (launch.getStartTime() < launchList.get(i).getStartTime()) {
                launchList.add(i, launch);
                launch.setLaunchNumber(i + 1);
                launchInserted = true;
            }
        }
        if (!launchInserted) {
            launchList.add(launch);
            launch.setLaunchNumber(launchList.size());
        }
    }

    private void initializeIterations() {
        synchronized (launchMap) {
            launchMap.clear();
            undefinedLaunchList.clear();
            for (Iteration iteration : DataManager.getIterationManager().getIterations()) {
                launchMap.put(iteration, new ArrayList<>());
            }
        }
    }
}
