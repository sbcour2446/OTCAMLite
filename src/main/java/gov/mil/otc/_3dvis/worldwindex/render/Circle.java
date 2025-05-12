package gov.mil.otc._3dvis.worldwindex.render;

import gov.mil.otc._3dvis.WWController;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Circle extends Path {

    private static final int MIN_NUM_INTERVALS = 8;
    private static final int DEFAULT_NUM_INTERVALS = 64;
    private int intervals = DEFAULT_NUM_INTERVALS;
    private Position center;
    private double radius;
    private Angle heading = Angle.ZERO;

    public Circle(Position center, double radius) {
        super(new ArrayList<>());
        this.center = center;
        this.radius = radius;
        setPositions(createPositions());
    }

    public Circle(Position center, double radius, int intervals) {
        super(new ArrayList<>());
        this.center = center;
        this.radius = radius;
        this.intervals = intervals;
        setPositions(createPositions());
    }

    public void setCenter(Position center) {
        this.center = center;
        setPositions(createPositions());
    }

    public void setRadius(double radius) {
        this.radius = radius;
        setPositions(createPositions());
    }

    public void setHeading(double heading) {
        this.heading = Angle.fromDegrees(heading);
    }

    public java.util.List<Position> createPositions() {
        if (radius == 0) {
            return Collections.emptyList();
        }

        int numLocations = 1 + Math.max(MIN_NUM_INTERVALS, intervals);
        double da = (2 * Math.PI) / (numLocations - 1);
        double globeRadius = WWController.getWorldWindowPanel().getModel().getGlobe().getRadiusAt(
                center.getLatitude(), center.getLongitude());

        Position[] locations = new Position[numLocations];

        for (int i = 0; i < numLocations; i++) {
            double angle = (i != numLocations - 1) ? i * da : 0;
            double xLength = radius * Math.cos(angle);
            double yLength = radius * Math.sin(angle);
            double distance = Math.sqrt(xLength * xLength + yLength * yLength);
            // azimuth runs positive clockwise from north and through 360 degrees.
            double azimuth = (Math.PI / 2.0) - (Math.acos(xLength / distance) * Math.signum(yLength) - heading.radians);

            LatLon latLon = LatLon.greatCircleEndPosition(center, azimuth, distance / globeRadius);
            locations[i] = new Position(latLon, center.elevation);
        }

        return Arrays.asList(locations);
    }
}
