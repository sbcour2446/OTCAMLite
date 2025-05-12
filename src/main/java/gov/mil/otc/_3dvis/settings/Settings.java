package gov.mil.otc._3dvis.settings;

import com.google.gson.annotations.SerializedName;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.RtcaState;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.tools.rangefinder.SerializedRangeFinderEntry;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Settings {

    @SerializedName("booleans")
    private final Map<String, Boolean> booleanMap = new ConcurrentHashMap<>();

    @SerializedName("integers")
    private final Map<String, Integer> integerMap = new ConcurrentHashMap<>();

    @SerializedName("strings")
    private final Map<String, String> stringMap = new ConcurrentHashMap<>();

    @SerializedName("log level")
    private String logLevel;

    @SerializedName("datastore")
    private final List<String> datastoreList = Collections.synchronizedList(new ArrayList<>());

    @SerializedName("affiliation colors")
    private final Map<Affiliation, Integer> affiliationColorMap = new ConcurrentHashMap<>();

    @SerializedName("icon display")
    private IconDisplay iconDisplay;

    @SerializedName("munition timeout")
    private Integer munitionTimeout;

    @SerializedName("entity filter")
    private EntityFilter entityFilter;

    @SerializedName("range finder entries")
    private List<SerializedRangeFinderEntry> rangeFinderEntryList;

    @SerializedName("dlm settings")
    private DlmSettings dlmSettings;

    @SerializedName("nbcrv settings")
    private NbcrvSettings nbcrvSettings;

    @SerializedName("use remote database")
    private Boolean useRemoteDatabase;

    @SerializedName("remote database")
    private String remoteDatabase;

    @SerializedName("real-time logging")
    private Boolean isRealTimeLogging;

    @SerializedName("load database on startup")
    private Boolean isLoadDatabaseOnStartup;

    public Boolean getBoolean(String key) {
        return booleanMap.get(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return booleanMap.get(key) == null ? defaultValue : booleanMap.get(key);
    }

    public Integer getInteger(String key) {
        return integerMap.get(key);
    }

    public int getInteger(String key, int defaultValue) {
        return integerMap.get(key) == null ? defaultValue : integerMap.get(key);
    }

    public String getString(String key) {
        return stringMap.get(key);
    }

    public String getString(String key, String defaultValue) {
        return stringMap.get(key) == null ? defaultValue : stringMap.get(key);
    }

    public void setValue(String key, boolean value) {
        booleanMap.put(key, value);
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("set %s::%s", key, value));
        }
    }

    public void setValue(String key, Integer value) {
        integerMap.put(key, value);
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("set %s::%d", key, value));
        }
    }

    public void setValue(String key, String value) {
        stringMap.put(key, value);
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("set %s::%s", key, value));
        }
    }

    public Level getLogLevel() {
        if (logLevel == null || logLevel.isBlank()) {
            setLogLevel(Level.WARNING);
        }
        try {
            return Level.parse(logLevel);
        } catch (Exception e) {
            if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
                Logger.getGlobal().log(Level.CONFIG, "Invalid Log Level " + logLevel, e);
            }
        }
        return Level.WARNING;
    }

    public void setLogLevel(Level level) {
        this.logLevel = level.getName();
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLogLevel::%s", logLevel));
        }
    }

    public List<File> getDatastoreList() {
        List<File> files = new ArrayList<>();
        for (String fileName : datastoreList) {
            files.add(new File(fileName));
        }
        return files;
    }

    public void addDatastore(File file) {
        datastoreList.add(file.getAbsolutePath());
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("addDatastore::%s", file));
        }
    }

    public void removeDataStore(File file) {
        datastoreList.remove(file);
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("removeDataStore::%s", file));
        }
    }

    public Color getAffiliationColor(Affiliation affiliation) {
        Integer rgba = affiliationColorMap.get(affiliation);
        return rgba == null ? Defaults.getAffiliationColor(affiliation) : new Color(rgba);
    }

    public void setAffiliationColor(Affiliation affiliation, Color color) {
        affiliationColorMap.put(affiliation, color.getRGB());
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setAffiliationColor::%s, %s", affiliation, color));
        }
    }

    public Color getRtcaColor(RtcaState rtcaState) {
        if (rtcaState == null) {
            return Color.GRAY;
        } else if (rtcaState.isKillCatastrophic()) {
            return Color.RED;
        } else if (rtcaState.isKillFirepower() || rtcaState.isKillMobility() || rtcaState.isKillCommunication()) {
            return Color.YELLOW;
        } else {
            return Color.GREEN;
        }
    }

    public IconDisplay getIconDisplay() {
        if (iconDisplay == null) {
            iconDisplay = new IconDisplay();
        }
        return iconDisplay;
    }

    public int getMunitionTimeout() {
        if (munitionTimeout == null) {
            munitionTimeout = Defaults.MUNITION_TIMEOUT;
        }
        return munitionTimeout;
    }

    public void setMunitionTimeout(int munitionTimeout) {
        this.munitionTimeout = munitionTimeout;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setMunitionTimeout::%d", munitionTimeout));
        }
    }

    public EntityFilter getEntityFilter() {
        if (entityFilter == null) {
            entityFilter = new EntityFilter();
        }
        return entityFilter;
    }

    public List<SerializedRangeFinderEntry> getRangeFinderEntryList() {
        return rangeFinderEntryList;
    }

    public void setRangeFinderEntryList(List<SerializedRangeFinderEntry> rangeFinderEntryList) {
        this.rangeFinderEntryList = rangeFinderEntryList;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, "setRangeFinderEntryList::");
        }
    }

    public DlmSettings getDlmSetting() {
        if (dlmSettings == null) {
            dlmSettings = new DlmSettings();
        }
        return dlmSettings;
    }

    public NbcrvSettings getNbcrvSettings() {
        if (nbcrvSettings == null) {
            nbcrvSettings = new NbcrvSettings();
        }
        return nbcrvSettings;
    }

    public void setUseRemoteDatabase(boolean useRemoteDatabase) {
        this.useRemoteDatabase = useRemoteDatabase;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setUseRemoteDatabase::%s", useRemoteDatabase));
        }
    }

    public boolean useRemoteDatabase() {
        if (useRemoteDatabase == null) {
            useRemoteDatabase = false;
        }
        return useRemoteDatabase;
    }

    public String getRemoteDatabase() {
        if (remoteDatabase == null) {
            remoteDatabase = "";
        }
        return remoteDatabase;
    }

    public void setRemoteDatabase(String remoteDatabase) {
        this.remoteDatabase = remoteDatabase;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setRemoteDatabase::%s", remoteDatabase));
        }
    }

    public boolean isRealTimeLogging() {
        if (isRealTimeLogging == null) {
            isRealTimeLogging = true;
        }
        return isRealTimeLogging;
    }

    public void setRealTimeLogging(boolean realTimeLogging) {
        isRealTimeLogging = realTimeLogging;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setRealTimeLogging::%s", realTimeLogging));
        }
    }

    public boolean isLoadDatabaseOnStartup() {
        if (isLoadDatabaseOnStartup == null) {
            isLoadDatabaseOnStartup = true;
        }
        return isLoadDatabaseOnStartup;
    }

    public void setLoadDatabaseOnStartup(boolean loadDatabaseOnStartup) {
        this.isLoadDatabaseOnStartup = loadDatabaseOnStartup;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLoadDatabaseOnStartup::%s", loadDatabaseOnStartup));
        }
    }
}
