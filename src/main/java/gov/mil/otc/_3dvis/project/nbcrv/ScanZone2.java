package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.data.oadms.element.ScanZoneElement;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class ScanZone2 {

    private static final double INNER_OPACITY = 0.1;
    private static final double OUTER_OPACITY = 0.9;
    private final double arcAngle;
    private final double centerline;
    private final Position position;
    private final double radius;
    private final boolean isExclude;
    private Polygon renderable;

    protected ScanZone2(ScanZoneElement scanZoneElement, boolean isExclude) {
        arcAngle = scanZoneElement.getArcAngle();
        centerline = scanZoneElement.getCenterline();
        radius = scanZoneElement.getRadius();
        position = scanZoneElement.getPosition();
        this.isExclude = isExclude;
    }

    public Polygon createRenderable(Position position, double vehicleHeading) {
        double heading = centerline - 360 + vehicleHeading;
        List<Position> endPoints = new ArrayList<>();
        endPoints.add(new Position(position, 0));
        endPoints.addAll(getArcEndPoints(position, heading, arcAngle, radius, 0));

        renderable = new Polygon();
        renderable.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setInteriorMaterial(isExclude ? Material.DARK_GRAY : Material.LIGHT_GRAY);
        shapeAttributes.setInteriorOpacity(INNER_OPACITY);
        shapeAttributes.setOutlineMaterial(isExclude ? Material.DARK_GRAY : Material.LIGHT_GRAY);
        shapeAttributes.setOutlineOpacity(OUTER_OPACITY);
        shapeAttributes.setOutlineWidth(2);
        renderable.setAttributes(shapeAttributes);
        renderable.setOuterBoundary(endPoints);

        return renderable;
    }

    public Polygon getRenderable() {
        return renderable;
    }

    private List<Position> getArcEndPoints(Position position, double azimuth, double arcAngle, double radius, double pitch) {
        List<Position> endPoints = new ArrayList<>();
        if (position != null) {
            double range = radius;
            double angle = azimuth - arcAngle;
            double height = 0;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ScanZone2 scanZone2 = (ScanZone2) o;
        return arcAngle == scanZone2.arcAngle &&
                centerline == scanZone2.centerline &&
                position.equals(scanZone2.position) &&
                radius == scanZone2.radius &&
                isExclude == scanZone2.isExclude;
    }

    @Override
    public int hashCode() {
        return Objects.hash(arcAngle, centerline, position, radius, isExclude);
    }
}
