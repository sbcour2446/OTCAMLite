package gov.mil.otc._3dvis.settings;

import com.google.gson.annotations.SerializedName;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Preferences {

    @SerializedName("windows")
    private final Map<String, WindowGeometry> windowGeometryMap = new ConcurrentHashMap<>();

    @SerializedName("startup view")
    private ViewPosition startupView;

    @SerializedName("last directory")
    private final Map<String, String> lastDirectoryMap = new ConcurrentHashMap<>();

    @SerializedName("unit preference")
    private UnitPreference unitPreference;

    @SerializedName("keep alive")
    private Boolean keepAlive;

    @SerializedName("overlays")
    private List<String> overlayList = new ArrayList<>();

    @SerializedName("entity table")
    private EntityTableSettings entityTableSettings;

    @SerializedName("loadPlaybackOnStartup")
    private boolean loadPlaybackOnStartup = false;

    @SerializedName("lastPlayback")
    private String lastPlayback = "";

    public WindowGeometry getWindowGeometry(String key) {
        return windowGeometryMap.get(key);
    }

    public WindowGeometry getOrCreateWindowGeometry(String key) {
        return windowGeometryMap.computeIfAbsent(key, k -> new WindowGeometry());
    }

    public void setWindowGeometry(String key, WindowGeometry windowGeometry) {
        windowGeometryMap.put(key, windowGeometry);
    }

    public ViewPosition getStartupView() {
        if (startupView == null) {
            startupView = new ViewPosition();
            startupView.setEyePosition(Position.fromDegrees(31.0762854121048, -97.84455976551283));
            startupView.setHeading(Angle.ZERO);
            startupView.setPitch(Angle.ZERO);
            startupView.setEyeDistance(5000000);
        }
        return startupView;
    }

    public void setStartupView(ViewPosition startupView) {
        this.startupView = startupView;
    }

    public File getLastDirectory(String key) {
        String lastDirectoryPath = lastDirectoryMap.get(key);
        if (lastDirectoryPath == null || !(new File(lastDirectoryPath).exists())) {
            return new File(System.getProperty("user.home"));
        }
        return new File(lastDirectoryPath);
    }

    public void setLastDirectory(String key, String directory) {
        lastDirectoryMap.put(key, directory);
    }

    public UnitPreference getUnitPreference() {
        if (unitPreference == null) {
            unitPreference = new UnitPreference();
        }
        return unitPreference;
    }

    public boolean getKeepAlive() {
        if (keepAlive == null) {
            keepAlive = true;
        }
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setKeepAlive::%s", keepAlive));
        }
    }

    public void addOverlay(File file) {
        if (!overlayList.contains(file.getAbsolutePath())) {
            overlayList.add(file.getAbsolutePath());
        }
    }

    public List<File> getOverlayList() {
        List<File> overlays = new ArrayList<>();
        for (String overlay : overlayList) {
            overlays.add(new File(overlay));
        }
        return overlays;
    }

    public void removeOverlay(File file) {
        if (overlayList.contains(file.getAbsolutePath())) {
            overlayList.remove(file.getAbsolutePath());
        }
    }

    public EntityTableSettings getEntityTableSettings() {
        if (entityTableSettings == null) {
            entityTableSettings = new EntityTableSettings();
        }
        return entityTableSettings;
    }

    public boolean isLoadPlaybackOnStartup() {
        return loadPlaybackOnStartup;
    }

    public void setLoadPlaybackOnStartup(boolean loadPlaybackOnStartup) {
        this.loadPlaybackOnStartup = loadPlaybackOnStartup;
    }

    public String getLastPlayback() {
        return lastPlayback;
    }

    public void setLastPlayback(String lastPlayback) {
        this.lastPlayback = lastPlayback;
    }
}
