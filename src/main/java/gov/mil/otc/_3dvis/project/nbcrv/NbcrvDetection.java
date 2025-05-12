package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.data.oadms.element.AreaElement;
import gov.mil.otc._3dvis.data.oadms.element.DetailsElement;
import gov.mil.otc._3dvis.data.oadms.element.EventThreadEventElement;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class NbcrvDetection {

    public static NbcrvDetection create(EventThreadEventElement eventThreadEventElement) {
        RegionType regionType = RegionType.fromName(eventThreadEventElement.getRegionType());

        String description = getDescription(eventThreadEventElement);
        List<Position> positions = new ArrayList<>();
        double arcAngle = 0.0;
        double radius = 0.0;
        double direction = 0.0;

        if (eventThreadEventElement.getAreasElement() != null) {
            for (AreaElement areaElement : eventThreadEventElement.getAreasElement().getAreaElementList()) {
                if (areaElement.getGeographicArcElement() != null) {
                    arcAngle = areaElement.getGeographicArcElement().getArcAngle();
                    radius = areaElement.getGeographicArcElement().getRadius();
                    direction = areaElement.getGeographicArcElement().getCenterline();
                    positions.add(areaElement.getGeographicArcElement().getPosition());
                } else if (areaElement.getGeographicPolygonElement() != null) {
                    positions.addAll(areaElement.getGeographicPolygonElement().getPositionList());
                }
            }
        }

        if (regionType == RegionType.POINT && eventThreadEventElement.getPosition() != null) {
            positions.add(eventThreadEventElement.getPosition());
        }

        if (positions.isEmpty()) {
            return null;
        }

        return new NbcrvDetection(eventThreadEventElement.getTimestamp(), eventThreadEventElement.getSourceTypes(),
                regionType, description, positions, arcAngle, radius, direction);
    }

    public static NbcrvDetection create(long timestamp, String deviceName, String threadType, String materialName,
                                        String materialClass, double measurement, String units, String criticality,
                                        String threatHeading, String threatSpeed, String regionTypeString, double direction,
                                        double arcWidth, double arcRadius, List<Position> positions) {
        RegionType regionType = RegionType.fromName(regionTypeString);
        if (regionType == RegionType.UNKNOWN) {
            return null;
        }

        String measurementString;
        if (deviceName.toLowerCase().contains("merlin")) {
            measurement = measurement * 60 * 60 * 1000000;
            measurementString = String.format("%.2f\u00B5Gy/hr", measurement);
        } else {
            measurementString = measurement + System.lineSeparator() + units;
        }

        String description = "Detection: " + threadType
                + System.lineSeparator() + materialName
                + System.lineSeparator() + materialClass
                + System.lineSeparator() + measurementString
                + System.lineSeparator() + criticality;

        return new NbcrvDetection(timestamp, deviceName, regionType, description,
                positions, arcWidth, arcRadius, direction, measurement);
    }

    public static NbcrvDetection create(long timestamp, String deviceName, RegionType regionType,
                                        String description, List<Position> positions, double arcAngle, double radius,
                                        double direction, double measurement) {
        return new NbcrvDetection(timestamp, deviceName, regionType, description,
                positions, arcAngle, radius, direction, measurement);
    }

    public static NbcrvDetection create(long timestamp, String deviceName, RegionType regionType,
                                        String description, String positions, double arcAngle, double radius,
                                        double direction, double measurement) {
        List<Position> positionList = new ArrayList<>();
        String[] values = positions.split(",");
        int index = 0;
        while (index + 2 < values.length) {
            try {
                double latitude = Double.parseDouble(values[index++]);
                double longitude = Double.parseDouble(values[index++]);
                double altitude = Double.parseDouble(values[index++]);
                positionList.add(Position.fromDegrees(latitude, longitude, altitude));
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "NbcrvDetection::create", e);
            }
        }
        return new NbcrvDetection(timestamp, deviceName, regionType, description,
                positionList, arcAngle, radius, direction, measurement);
    }

    private static String getDescription(EventThreadEventElement eventThreadEventElement) {
        String reason = eventThreadEventElement.getReason();
        if (eventThreadEventElement.getDetailsElement() != null) {
            for (DetailsElement.DetailElement detailElement : eventThreadEventElement.getDetailsElement().getDetailElementList()) {
                if (detailElement.getName().equalsIgnoreCase("Material Name")) {
                    reason = detailElement.getValue();
                    break;
                }
            }
        }
        return reason;
    }

    protected static final Material ALERT_MATERIAL = new Material(Color.RED);
    protected static final Material NON_ALERT_MATERIAL = new Material(Color.YELLOW);

    protected final double opacity = 0.25;
    protected final long timestamp;
    protected final String deviceName;
    protected final RegionType regionType;
    protected final String description;
    protected final List<Position> positionList = new ArrayList<>();
    protected final double arcAngle;
    protected final double radius;
    protected final double direction;
    protected final double measurement;

    protected boolean isVisible = false;
    protected Renderable renderable = null;

    protected NbcrvDetection(long timestamp, String deviceName, RegionType regionType, String description,
                             List<Position> positions, double arcAngle, double radius, double direction) {
        this(timestamp, deviceName, regionType, description, positions, arcAngle, radius, direction, 0);
    }

    protected NbcrvDetection(long timestamp, String deviceName, RegionType regionType, String description,
                             List<Position> positions, double arcAngle, double radius, double direction,
                             double measurement) {
        this.timestamp = timestamp;
        this.deviceName = deviceName;
        this.regionType = regionType;
        this.description = description;
        this.positionList.addAll(positions);
        this.arcAngle = arcAngle;
        this.radius = radius;
        this.direction = direction;
        this.measurement = measurement;
    }

    private Position getScanLineEndPoint(Position position, double yaw, double pitch) {
        if (position != null) {
            double height = 3;
            if (SettingsManager.getSettings().getNbcrvSettings().isUsePitch()) {
                height = sin(pitch * 0.0174533) * 6000.0;
            }
            double range = 6.0 / 6371.0; // approximate distance in radians on a sphere
            return new Position(LatLon.greatCircleEndPosition(position, Angle.fromDegrees(yaw),
                    Angle.fromRadians(range)), height);
        } else {
            return null;
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public RegionType getRegionType() {
        return regionType;
    }

    public List<Position> getPositionList() {
        return positionList;
    }

    public String getPositionListString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Position position : positionList) {
            stringBuilder.append(position.getLatitude().degrees);
            stringBuilder.append(",");
            stringBuilder.append(position.getLongitude().degrees);
            stringBuilder.append(",");
            stringBuilder.append(position.getAltitude());
            stringBuilder.append(",");
        }
        return stringBuilder.toString();
    }

    public double getArcAngle() {
        return arcAngle;
    }

    public double getRadius() {
        return radius;
    }

    public double getDirection() {
        return direction;
    }

    public double getMeasurement() {
        return measurement;
    }

    public String getDescription() {
        return description;
    }

    private DeviceState lastDeviceState = null;

    public void update(long time, Position position, DeviceState deviceState) {
        boolean isActive = isActive(time);
        if (isActive) {
            if (!isVisible) {
                show(position, deviceState);
            }
            if (lastDeviceState != deviceState) {
                lastDeviceState = deviceState;
                updateRenderableColor();
            }
        } else if (isVisible) {
            hide();
        }
    }

    private void updateRenderableColor() {
        final Material material = getEventMaterial(lastDeviceState);
        if (renderable instanceof AbstractShape) {
            SwingUtilities.invokeLater(() -> {
                if (renderable != null) {
                    ((AbstractShape) renderable).getAttributes().setInteriorMaterial(material);
                    ((AbstractShape) renderable).getAttributes().setOutlineMaterial(material);
                }
            });
        }
    }

    public void dispose() {
        if (isVisible) {
            hide();
        }
    }

    private void show(Position position, DeviceState deviceState) {
        if (renderable == null) {
            renderable = createRenderable(position, deviceState);
        }
        if (renderable != null) {
            EntityLayer.add(renderable);
            isVisible = true;
        }
    }

    private void hide() {
        if (renderable != null) {
            EntityLayer.remove(renderable);
        }
        isVisible = false;
        renderable = null;
    }

    public boolean isActive(long time) {
        int timeout = SettingsManager.getSettings().getNbcrvSettings().getDeviceTimeout(deviceName);
        return time >= getTimestamp() && time <= getTimestamp() + timeout;
    }

    private Renderable createRenderable(Position position, DeviceState deviceState) {
        if (position == null) {
            return null;
        }

        return switch (regionType) {
            case ARC -> createArcArea(position, deviceState);
            case POINT -> createPointArea(position, deviceState);
            case POLYGON -> createPolygonArea(position, deviceState);
            case UNKNOWN -> null;
        };
    }

    private Renderable createArcArea(Position position, DeviceState deviceState) {
        if (positionList.isEmpty()) {
            return null;
        }

        List<Position> endPoints = new ArrayList<>();
        endPoints.add(new Position(position, SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(deviceName)));

        double pitch = SettingsManager.getSettings().getNbcrvSettings().isUsePitch() && deviceState != null
                && deviceState.getPitch() != null ? deviceState.getPitch() : 0;

        endPoints.addAll(getArcEndPoints(positionList.get(0), direction, arcAngle, radius, pitch));
        if (endPoints.size() < 3) {
            return null;
        }

        Polygon polygon = new Polygon();
        polygon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setInteriorMaterial(getEventMaterial(deviceState));
        shapeAttributes.setInteriorOpacity(opacity);
        shapeAttributes.setOutlineMaterial(getEventMaterial(deviceState));
        shapeAttributes.setOutlineOpacity(opacity);
        shapeAttributes.setOutlineWidth(2);
        polygon.setAttributes(shapeAttributes);
        polygon.setOuterBoundary(endPoints);
        polygon.setValue(AVKey.ROLLOVER_TEXT, description);
        return polygon;
    }

    private List<Position> getArcEndPoints(Position position, double azimuth, double arcAngle, double radius, double pitch) {
        List<Position> endPoints = new ArrayList<>();
        if (position != null) {
            double range = 6000.0;// * 1 / 6371000.0;
            double angle = azimuth - arcAngle;
            double height = SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(deviceName);
            UTMCoord utm = UTMCoord.fromLatLon(position.getLatitude(), position.getLongitude());
            endPoints.add(new Position(UTMCoord.locationFromUTMCoord(
                    utm.getZone(),
                    utm.getHemisphere(),
                    utm.getEasting() + sin((angle * 0.0174533)) * range,
                    utm.getNorthing() + cos((angle * 0.0174533)) * range,
                    null),
                    height + sin(pitch * 0.0174533) * range));

            angle = azimuth + arcAngle;
            endPoints.add(new Position(UTMCoord.locationFromUTMCoord(
                    utm.getZone(),
                    utm.getHemisphere(),
                    utm.getEasting() + sin((angle * 0.0174533)) * range,
                    utm.getNorthing() + cos((angle * 0.0174533)) * range,
                    null),
                    height + sin(pitch * 0.0174533) * range));
        }
        return endPoints;
    }

    private Renderable createPointArea(Position position, DeviceState deviceState) {
        Path path = new Path();
        path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setInteriorMaterial(getEventMaterial(deviceState));
        shapeAttributes.setInteriorOpacity(opacity);
        shapeAttributes.setOutlineMaterial(getEventMaterial(deviceState));
        shapeAttributes.setOutlineOpacity(opacity);
        shapeAttributes.setOutlineWidth(5);
        path.setAttributes(shapeAttributes);

        List<Position> endPoints = new ArrayList<>();
        endPoints.add(new Position(position, SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(deviceName)));
        double range = 1 / 6371.0;
        endPoints.add(new Position(LatLon.greatCircleEndPosition(position, Angle.fromDegrees(direction),
                Angle.fromRadians(range)), SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(deviceName)));
        path.setPositions(endPoints);
        path.setValue(AVKey.ROLLOVER_TEXT, description);

        return path;
    }

    private Renderable createPolygonArea(Position position, DeviceState deviceState) {
        if (positionList.size() < 3) {
            return null;
        }

        Polygon polygon = new Polygon();
        polygon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setInteriorMaterial(getEventMaterial(deviceState));
        shapeAttributes.setInteriorOpacity(opacity);
        shapeAttributes.setOutlineMaterial(getEventMaterial(deviceState));
        shapeAttributes.setOutlineOpacity(opacity);
        shapeAttributes.setOutlineWidth(2);
        polygon.setAttributes(shapeAttributes);
        List<Position> positions = new ArrayList<>();
        for (Position corner : positionList) {
            double height = SettingsManager.getSettings().getNbcrvSettings().getDeviceHeight(deviceName);
            Double pitch = deviceState.getPitch();
            if (SettingsManager.getSettings().getNbcrvSettings().isUsePitch() && pitch != null) {
                double distance1 = Utility.calculateDistance1(position, corner);
                height += sin(pitch * 0.0174533) * distance1;
            }

            positions.add(new Position(corner, height));
        }
        polygon.setOuterBoundary(positions);
        polygon.setValue(AVKey.ROLLOVER_TEXT, description);

        return polygon;
    }

    private Material getEventMaterial(DeviceState deviceState) {
        if (deviceState != null && deviceState.isAlert()) {
            return ALERT_MATERIAL;
        } else {
            return NON_ALERT_MATERIAL;
        }
    }
}
