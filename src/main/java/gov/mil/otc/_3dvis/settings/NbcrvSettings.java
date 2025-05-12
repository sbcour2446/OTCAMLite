package gov.mil.otc._3dvis.settings;

import com.google.gson.annotations.SerializedName;

import java.awt.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvSettings {

    private static final Map<String, Color> DEFAULT_DEVICE_COLOR = new TreeMap<>();
    private static final Map<String, Integer> DEFAULT_DEVICE_TIMEOUT = new TreeMap<>();
    private static final Map<String, Double> DEFAULT_RAD_NUC_THRESHOLD = new TreeMap<>();

    static {
        DEFAULT_DEVICE_COLOR.put("csds", Color.GREEN);
        DEFAULT_DEVICE_COLOR.put("imcad", Color.BLUE);
        DEFAULT_DEVICE_COLOR.put("other", Color.BLACK);
        DEFAULT_DEVICE_TIMEOUT.put("csds", 30000);
        DEFAULT_DEVICE_TIMEOUT.put("imcad", 10000);
        DEFAULT_DEVICE_TIMEOUT.put("other", 2000);
        DEFAULT_RAD_NUC_THRESHOLD.put("critical",1.0);
        DEFAULT_RAD_NUC_THRESHOLD.put("marginal",0.5);
        DEFAULT_RAD_NUC_THRESHOLD.put("negligible",0.2);
    }

    @SerializedName("device colors")
    private final Map<String, Integer> deviceColorMap = new ConcurrentHashMap<>();
    @SerializedName("device height")
    private final Map<String, Double> deviceHeightMap = new ConcurrentHashMap<>();
    @SerializedName("device timeout")
    private final Map<String, Integer> deviceTimeoutMap = new ConcurrentHashMap<>();
    @SerializedName("rad/nuc threshold")
    private final Map<String, Double> radNucThresholdMap = new ConcurrentHashMap<>();
    private boolean usePitch = false;
    private int oadmsTimeZoneOffset = 0;
    private int oadmsVideoOffset = 2500;
    private int lidarMedianFilterWindowSize = 60;
    private int lidarClippedStartSize = 200;
    private int lidarMaxConcentration = 2000;
    private int lidarConcentrationThreshold = 50;
    private int lidarMaxRange = 3000;
    private double lidarDistanceFactor = 1.1;
    private double lidarCloudLineWidth = .25;

    protected NbcrvSettings() {
        for (Map.Entry<String, Color> entry : DEFAULT_DEVICE_COLOR.entrySet()) {
            deviceColorMap.put(entry.getKey(), entry.getValue().getRGB());
        }
        for (Map.Entry<String, Integer> entry : DEFAULT_DEVICE_TIMEOUT.entrySet()) {
            deviceTimeoutMap.put(entry.getKey(), entry.getValue());
        }
    }

    public Color getDeviceColor(String deviceName) {
        String deviceNameLower = deviceName.toLowerCase();
        Integer rgba = deviceColorMap.get(deviceNameLower);
        if (rgba == null) {
            return new Color(deviceColorMap.get("other"));
        }
        return new Color(rgba);
    }

    public void setDeviceColor(String deviceName, Color color) {
        String deviceNameLower = deviceName.toLowerCase();
        deviceColorMap.put(deviceNameLower, color.getRGB());
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setDeviceColor::%s, %s", deviceName, color));
        }
    }

    public double getDeviceHeight(String deviceName) {
        String deviceNameLower = deviceName.toLowerCase();
        Double height = deviceHeightMap.get(deviceNameLower);
        if (height == null) {
            height = 3.0;
            deviceHeightMap.put(deviceNameLower, height);
        }
        return height;
    }

    public void setDeviceHeight(String deviceName, double height) {
        String deviceNameLower = deviceName.toLowerCase();
        deviceHeightMap.put(deviceNameLower, height);
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setDeviceHeight::%s, %f", deviceName, height));
        }
    }

    public int getDeviceTimeout(String deviceName) {
        String deviceNameLower = deviceName.toLowerCase();
        Integer timeout = deviceTimeoutMap.get(deviceNameLower);
        if (timeout == null) {
            return deviceTimeoutMap.get("other");
        }
        return timeout;
    }

    public void setDeviceTimeout(String deviceName, int timeout) {
        String deviceNameLower = deviceName.toLowerCase();
        deviceTimeoutMap.put(deviceNameLower, timeout);
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setDeviceTimeout::%s, %d", deviceName, timeout));
        }
    }

    public boolean isUsePitch() {
        return usePitch;
    }

    public void setUsePitch(boolean usePitch) {
        this.usePitch = usePitch;
    }

    public double getRadNucThreshold(String thresholdName) {
        String thresholdNameLower = thresholdName.toLowerCase();
        Double threshold = radNucThresholdMap.get(thresholdNameLower);
        if (threshold == null) {
            threshold = DEFAULT_RAD_NUC_THRESHOLD.get(thresholdNameLower);
            if (threshold == null) {
                return 0.0;
            }
            return threshold;
        }
        return threshold;
    }

    public void setRadNucThreshold(String thresholdName, double value) {
        String thresholdNameLower = thresholdName.toLowerCase();
        radNucThresholdMap.put(thresholdNameLower, value);
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setRadNucThreshold::%s, %f", thresholdName, value));
        }
    }

    public int getOadmsTimeZoneOffset() {
        return oadmsTimeZoneOffset;
    }

    public void setOadmsTimeZoneOffset(int oadmsTimeZoneOffset) {
        this.oadmsTimeZoneOffset = oadmsTimeZoneOffset;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setOadmsTimeZoneOffset::%d", oadmsTimeZoneOffset));
        }
    }

    public int getOadmsVideoOffset() {
        return oadmsVideoOffset;
    }

    public void setOadmsVideoOffset(int oadmsVideoOffset) {
        this.oadmsVideoOffset = oadmsVideoOffset;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setOadmsVideoOffset::%d", oadmsVideoOffset));
        }
    }

    public int getLidarMedianFilterWindowSize() {
        return lidarMedianFilterWindowSize;
    }

    public void setLidarMedianFilterWindowSize(int lidarMedianFilterWindowSize) {
        this.lidarMedianFilterWindowSize = lidarMedianFilterWindowSize;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLidarMedianFilterWindowSize::%d", lidarMedianFilterWindowSize));
        }
    }

    public int getLidarClippedStartSize() {
        return lidarClippedStartSize;
    }

    public void setLidarClippedStartSize(int lidarClippedStartSize) {
        this.lidarClippedStartSize = lidarClippedStartSize;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLidarClippedStartSize::%d", lidarClippedStartSize));
        }
    }

    public int getLidarMaxConcentration() {
        return lidarMaxConcentration;
    }

    public void setLidarMaxConcentration(int lidarMaxConcentration) {
        this.lidarMaxConcentration = lidarMaxConcentration;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLidarMaxConcentration::%d", lidarMaxConcentration));
        }
    }

    public int getLidarConcentrationThreshold() {
        return lidarConcentrationThreshold;
    }

    public void setLidarConcentrationThreshold(int lidarConcentrationThreshold) {
        this.lidarConcentrationThreshold = lidarConcentrationThreshold;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLidarConcentrationThreshold::%d", lidarConcentrationThreshold));
        }
    }

    public int getLidarMaxRange() {
        return lidarMaxRange;
    }

    public void setLidarMaxRange(int lidarMaxRange) {
        this.lidarMaxRange = lidarMaxRange;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLidarMaxRange::%d", lidarMaxRange));
        }
    }

    public double getLidarDistanceFactor() {
        return lidarDistanceFactor;
    }

    public void setLidarDistanceFactor(double lidarDistanceFactor) {
        this.lidarDistanceFactor = lidarDistanceFactor;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLidarDistanceFactor::%d", lidarDistanceFactor));
        }
    }

    public double getLidarCloudLineWidth() {
        return lidarCloudLineWidth;
    }

    public void setLidarCloudLineWidth(double lidarCloudLineWidth) {
        this.lidarCloudLineWidth = lidarCloudLineWidth;
        if (Logger.getGlobal().isLoggable(Level.CONFIG)) {
            Logger.getGlobal().log(Level.CONFIG, String.format("setLidarCloudLineWidth::%d", lidarCloudLineWidth));
        }
    }
}
