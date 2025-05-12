package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.data.oadms.element.*;
import gov.mil.otc._3dvis.datamodel.timed.TimedData;

import java.util.ArrayList;
import java.util.List;

public class DeviceState extends TimedData {

    private final boolean alert;
    private final String alertReason;
    private final String state;
    private final String stateDescription;
    private final String fault;
    private final String major;
    private final String minor;
    private final String info;
    private final String activity;
    private final String operator;
    private final Double pitch;
    private final Double roll;
    private final Double yaw;
    private final List<ScanZone> scanZoneList = new ArrayList<>();
    private final List<ScanZone2> scanZone2List = new ArrayList<>();

    public DeviceState(DeviceElement deviceElement) {
        this(deviceElement.getPropertiesElement());
    }

    public DeviceState(PropertiesElement propertiesElement) {
        super(propertiesElement.getTimestamp());

        this.alert = propertiesElement.isAlert();
        this.alertReason = propertiesElement.getAlertReason();
        this.state = propertiesElement.getOperatingStateElement().getCode();
        this.stateDescription = propertiesElement.getOperatingStateElement().getSubcode();
        this.fault = propertiesElement.getFaultsElement().toString();
        this.major = "";
        this.minor = "";
        this.info = "";
        this.activity = propertiesElement.getActivityName();
        this.operator = "";
        this.pitch = propertiesElement.getOrientationElement() != null ?
                propertiesElement.getOrientationElement().getPitch() - propertiesElement.getScanTiltOffset() : null;
        this.roll = propertiesElement.getOrientationElement() != null ? propertiesElement.getOrientationElement().getRoll() : null;
        this.yaw = propertiesElement.getOrientationElement() != null ? propertiesElement.getOrientationElement().getYaw() : null;
        this.scanZoneList.addAll(initializeScanZones(propertiesElement.getTimestamp(), propertiesElement.getScanZonesElement()));
        this.scanZone2List.addAll(createScanZones(propertiesElement.getScanZonesElement()));
    }

    public DeviceState(long timestamp, boolean alert, String alertReason, String state, String stateDescription,
                       String major, String minor, String info, String activity, String operator,
                       Double pitch, Double roll, Double yaw) {
        super(timestamp);

        this.alert = alert;
        this.alertReason = alertReason;
        this.state = state;
        this.stateDescription = stateDescription;
        this.fault = "";
        this.major = major;
        this.minor = minor;
        this.info = info;
        this.activity = activity;
        this.operator = operator;
        this.pitch = pitch;
        this.roll = roll;
        this.yaw = yaw;
    }

    private List<ScanZone> initializeScanZones(long timestamp, ScanZonesElement scanZonesElement) {
        List<ScanZone> scanZones = new ArrayList<>();

        if (scanZonesElement == null || scanZonesElement.getScanZoneElements().isEmpty()) {
            return scanZones;
        }

        for (ScanZoneElement scanZoneElement : scanZonesElement.getScanZoneElements()) {
            scanZones.add(new ScanZone(timestamp, scanZoneElement.getPosition(), scanZoneElement.getArcAngle(),
                    scanZoneElement.getRadius(), scanZoneElement.getCenterline()));
        }
        return scanZones;
    }

    private List<ScanZone2> createScanZones(ScanZonesElement scanZonesElement) {
        List<ScanZone2> scanZones = new ArrayList<>();

        if (scanZonesElement == null || scanZonesElement.getScanZoneElements().isEmpty()) {
            return scanZones;
        }

        for (ScanZoneElement scanZoneElement : scanZonesElement.getScanZoneElements()) {
            scanZones.add(new ScanZone2(scanZoneElement, false));
        }
        return scanZones;
    }

    public boolean isAlert() {
        return alert;
    }

    public String getAlertReason() {
        return alertReason;
    }

    public String getState() {
        return state;
    }

    public String getStateDescription() {
        return stateDescription;
    }

    public String getFault() {
        return fault;
    }

    public String getMajor() {
        return major;
    }

    public String getMinor() {
        return minor;
    }

    public String getInfo() {
        return info;
    }

    public String getActivity() {
        return activity;
    }

    public String getOperator() {
        return operator;
    }

    public Double getPitch() {
        return pitch;
    }

    public Double getRoll() {
        return roll;
    }

    public Double getYaw() {
        return yaw;
    }

    public List<ScanZone> getScanZoneList() {
        return scanZoneList;
    }

    public List<ScanZone2> getScanZone2List() {
        return scanZone2List;
    }
}
