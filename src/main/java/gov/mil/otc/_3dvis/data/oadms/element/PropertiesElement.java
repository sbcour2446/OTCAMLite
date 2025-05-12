package gov.mil.otc._3dvis.data.oadms.element;

import gov.nasa.worldwind.geom.Position;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropertiesElement {

    public static final String ELEMENT_NAME = "properties";
    private static final String ACTIVITY_NAME_ELEMENT = "activityName";
    private static final String ALERT_ELEMENT = "alert";
    private static final String ALERT_REASON_ELEMENT = "alertReason";
    private static final String DEPLOYED_ELEMENT = "deployed";
    private static final String HARDWARE_VERSION_ELEMENT = "hardwareVersion";
    private static final String MOVING_ELEMENT = "moving";
    private static final String SCAN_TILT_OFFSET = "scanTiltOffset";
    private static final String SOFTWARE_VERSION_ELEMENT = "softwareVersion";
    private static final String SPECIFICATION_VERSION_ELEMENT = "specificationVersion";
    private static final String SYSTEM_TIME_ELEMENT = "systemTime";

    public static PropertiesElement parse(Node node, long readingTime) {
        String activityName = "";
        boolean alert = false;
        String alertReason = "";
        boolean deployed = false;
        FaultsElement faultsElement = FaultsElement.FAULTS_ELEMENT_NONE;
        String hardwareVersion = "";
        IdentityElement identityElement = null;
        Boolean isMoving = null;
        double scanTiltOffset = 0.0;
        OperatingStateElement operatingStateElement = null;
        OrientationElement orientationElement = null;
        Position position = null;
        ScanZonesElement scanZonesElement = null;
        String softwareVersion = "";
        String specificationVersion = "";
        long timestamp = readingTime;
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node child = nodeList.item(i);
            if (child == null) {
                continue;
            }
            if (child.getNodeName().equalsIgnoreCase(ACTIVITY_NAME_ELEMENT)) {
                activityName = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(ALERT_ELEMENT)) {
                alert = ElementUtility.parseBoolean(child);
            } else if (child.getNodeName().equalsIgnoreCase(ALERT_REASON_ELEMENT)) {
                alertReason = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(DEPLOYED_ELEMENT)) {
                deployed = ElementUtility.parseBoolean(child);
            } else if (child.getNodeName().equalsIgnoreCase(FaultsElement.ELEMENT_NAME)) {
                faultsElement = FaultsElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(HARDWARE_VERSION_ELEMENT)) {
                hardwareVersion = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(IdentityElement.ELEMENT_NAME)) {
                identityElement = IdentityElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(MOVING_ELEMENT)) {
                isMoving = ElementUtility.parseBoolean(child);
            } else if (child.getNodeName().equalsIgnoreCase(SCAN_TILT_OFFSET)) {
                scanTiltOffset = ElementUtility.parseDouble(child);
            } else if (child.getNodeName().equalsIgnoreCase(OperatingStateElement.ELEMENT_NAME)) {
                operatingStateElement = OperatingStateElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(OrientationElement.ELEMENT_NAME)) {
                orientationElement = OrientationElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(PositionElement.ELEMENT_NAME)) {
                position = PositionElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(ScanZonesElement.ELEMENT_NAME)) {
                scanZonesElement = ScanZonesElement.parse(child);
            } else if (child.getNodeName().equalsIgnoreCase(SOFTWARE_VERSION_ELEMENT)) {
                softwareVersion = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(SPECIFICATION_VERSION_ELEMENT)) {
                specificationVersion = ElementUtility.parseString(child);
            } else if (child.getNodeName().equalsIgnoreCase(SYSTEM_TIME_ELEMENT)) {
//ignore and use reading time                timestamp = ElementUtility.parseSystemTime(child);
            }
        }

        return new PropertiesElement(activityName, alert, alertReason, deployed, faultsElement, hardwareVersion,
                identityElement, isMoving, scanTiltOffset, operatingStateElement, orientationElement, position,
                scanZonesElement, softwareVersion, specificationVersion, timestamp);
    }

    private final String activityName;
    private final boolean alert;
    private final String alertReason;
    private final boolean deployed;
    private final FaultsElement faultsElement;
    private final String hardwareVersion;
    private final IdentityElement identityElement;
    private final Boolean isMoving;
    private final double scanTiltOffset;
    private final OperatingStateElement operatingStateElement;
    private final OrientationElement orientationElement;
    private final Position position;
    private final ScanZonesElement scanZonesElement;
    private final String softwareVersion;
    private final String specificationVersion;
    private final long timestamp;

    protected PropertiesElement() {
        activityName = "";
        alert = false;
        alertReason = "";
        deployed = false;
        faultsElement = FaultsElement.FAULTS_ELEMENT_NONE;
        hardwareVersion = "";
        identityElement = null;
        isMoving = false;
        scanTiltOffset = 0.0;
        operatingStateElement = null;
        orientationElement = null;
        position = null;
        softwareVersion = "";
        specificationVersion = "";
        timestamp = 0;
        scanZonesElement = null;
    }

    public PropertiesElement(String activityName, boolean alert, String alertReason, boolean deployed,
                             FaultsElement faultsElement, String hardwareVersion, IdentityElement identityElement,
                             Boolean isMoving, double scanTiltOffset, OperatingStateElement operatingStateElement,
                             OrientationElement orientationElement, Position position, ScanZonesElement scanZonesElement,
                             String softwareVersion, String specificationVersion, long timestamp) {
        this.activityName = activityName;
        this.alert = alert;
        this.alertReason = alertReason;
        this.deployed = deployed;
        this.faultsElement = faultsElement;
        this.hardwareVersion = hardwareVersion;
        this.identityElement = identityElement;
        this.isMoving = isMoving;
        this.scanTiltOffset = scanTiltOffset;
        this.operatingStateElement = operatingStateElement;
        this.orientationElement = orientationElement;
        this.position = position;
        this.scanZonesElement = scanZonesElement;
        this.softwareVersion = softwareVersion;
        this.specificationVersion = specificationVersion;
        this.timestamp = timestamp;
    }

    public String getActivityName() {
        return activityName;
    }

    public boolean isAlert() {
        return alert;
    }

    public String getAlertReason() {
        return alertReason;
    }

    public boolean isDeployed() {
        return deployed;
    }

    public FaultsElement getFaultsElement() {
        return faultsElement;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public IdentityElement getIdentityElement() {
        return identityElement;
    }

    public Boolean getMoving() {
        return isMoving;
    }

    public double getScanTiltOffset() {
        return scanTiltOffset;
    }

    public OperatingStateElement getOperatingStateElement() {
        return operatingStateElement;
    }

    public OrientationElement getOrientationElement() {
        return orientationElement;
    }

    public Position getPosition() {
        return position;
    }

    public ScanZonesElement getScanZonesElement() {
        return scanZonesElement;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public String getSpecificationVersion() {
        return specificationVersion;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
