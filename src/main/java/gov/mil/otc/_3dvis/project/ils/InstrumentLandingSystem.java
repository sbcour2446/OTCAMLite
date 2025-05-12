package gov.mil.otc._3dvis.project.ils;

import gov.mil.otc._3dvis.WWController;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.awt.*;

import static java.lang.Math.*;

public class InstrumentLandingSystem {

    public static final double RUNWAY_INTERSECTION_WIDTH_DEFAULT = 60.96;
    public static final double GLIDE_SLOPE_ANGLE_DEFAULT = 3.0;
    public static final double GLIDE_SLOPE_HEIGHT_ANGLE_DEFAULT = 0.7;
    public static final double MIDDLE_MARKER_DISTANCE_DEFAULT = 800;
    public static final double OUTER_MARKER_DISTANCE_DEFAULT = 10000;
    private final RenderableLayer renderableLayer = new RenderableLayer();
    private final Position localizerPosition;
    private final Position glideSlopePosition;
    private final double runwayIntersectionWidth;
    private final double glideSlopeAngle;
    private final double glideSlopeHeightAngle;
    private final double middleMarkerDistance;
    private final double outerMarkerDistance;

    protected InstrumentLandingSystem(Position localizerPosition, Position glideSlopePosition) {
        this(localizerPosition, glideSlopePosition, RUNWAY_INTERSECTION_WIDTH_DEFAULT,
                GLIDE_SLOPE_ANGLE_DEFAULT, GLIDE_SLOPE_HEIGHT_ANGLE_DEFAULT,
                MIDDLE_MARKER_DISTANCE_DEFAULT, OUTER_MARKER_DISTANCE_DEFAULT);
    }

    protected InstrumentLandingSystem(Position localizerPosition, Position glideSlopePosition,
                                      double runwayIntersectionWidth, double glideSlopeAngle,
                                      double glideSlopeHeightAngle, double middleMarkerDistance,
                                      double outerMarkerDistance) {
        this.localizerPosition = localizerPosition;
        this.glideSlopePosition = glideSlopePosition;
        this.middleMarkerDistance = middleMarkerDistance;
        this.outerMarkerDistance = outerMarkerDistance;
        this.runwayIntersectionWidth = runwayIntersectionWidth;
        this.glideSlopeAngle = glideSlopeAngle;
        this.glideSlopeHeightAngle = glideSlopeHeightAngle;

        create();
    }

    public void dispose() {
        renderableLayer.removeAllRenderables();
        WWController.removeLayer(renderableLayer);
    }

    public Position getLocalizerPosition() {
        return localizerPosition;
    }

    public Position getGlideSlopePosition() {
        return glideSlopePosition;
    }

    public double getRunwayIntersectionWidth() {
        return runwayIntersectionWidth;
    }


    public double getGlideSlopeAngle() {
        return glideSlopeAngle;
    }

    public double getGlideSlopeHeightAngle() {
        return glideSlopeHeightAngle;
    }

    public double getMiddleMarkerDistance() {
        return middleMarkerDistance;
    }

    public double getOuterMarkerDistance() {
        return outerMarkerDistance;
    }

    private void create() {
        renderableLayer.setName("InstrumentLandingSystemSimulator");
        renderableLayer.setPickEnabled(true);
        WWController.addLayer(renderableLayer);

        double localizerRadius = runwayIntersectionWidth / 2;
        double glideSlopeRadius= glideSlopeHeightAngle / 2;

        UTMCoord localizerUtm = UTMCoord.fromLatLon(localizerPosition.getLatitude(), localizerPosition.getLongitude());
        UTMCoord gsUtm = UTMCoord.fromLatLon(glideSlopePosition.getLatitude(), glideSlopePosition.getLongitude());

        double x = gsUtm.getEasting() - localizerUtm.getEasting();
        double y = gsUtm.getNorthing() - localizerUtm.getNorthing();
        double distance = sqrt(x * x + y * y);
        double myAngle = atan2(x, y);

        double myh = sqrt(distance * distance + localizerRadius * localizerRadius);
        double mya = atan2(localizerRadius, distance);

        double localizerAngleLeft = myAngle - mya;
        double localizerAngleRight = myAngle + mya;
        double eastingDeltaLeft = sin(localizerAngleLeft) * myh;
        double northingDeltaLeft = cos(localizerAngleLeft) * myh;
        double eastingDeltaRight = sin(localizerAngleRight) * myh;
        double northingDeltaRight = cos(localizerAngleRight) * myh;

        UTMCoord runwayLeftUtm = UTMCoord.fromUTM(localizerUtm.getZone(), localizerUtm.getHemisphere(),
                localizerUtm.getEasting() + eastingDeltaLeft,
                localizerUtm.getNorthing() + northingDeltaLeft);

        UTMCoord runwayRightUtm = UTMCoord.fromUTM(localizerUtm.getZone(), localizerUtm.getHemisphere(),
                localizerUtm.getEasting() + eastingDeltaRight,
                localizerUtm.getNorthing() + northingDeltaRight);

        UTMCoord leftEndpointUtm = UTMCoord.fromUTM(localizerUtm.getZone(), localizerUtm.getHemisphere(),
                runwayLeftUtm.getEasting() + sin(localizerAngleLeft) * 10000,
                runwayLeftUtm.getNorthing() + cos(localizerAngleLeft) * 10000);

        UTMCoord rightEndpointUtm = UTMCoord.fromUTM(localizerUtm.getZone(), localizerUtm.getHemisphere(),
                runwayRightUtm.getEasting() + sin(localizerAngleRight) * 10000,
                runwayRightUtm.getNorthing() + cos(localizerAngleRight) * 10000);

        Position runwayEdgeLeft = new Position(runwayLeftUtm.getLatitude(), runwayLeftUtm.getLongitude(),
                glideSlopePosition.getElevation());

        Position runwayEdgeRight = new Position(UTMCoord.locationFromUTMCoord(
                localizerUtm.getZone(), localizerUtm.getHemisphere(),
                localizerUtm.getEasting() + eastingDeltaRight,
                localizerUtm.getNorthing() + northingDeltaRight,
                null), glideSlopePosition.getElevation());

        double elevationBottomAngle = sin((glideSlopeAngle - glideSlopeRadius) * 0.0174533);
        double elevationTopAngle = sin((glideSlopeAngle + glideSlopeRadius) * 0.0174533);

        Position bottomLeftEndpoint = new Position(leftEndpointUtm.getLatitude(), leftEndpointUtm.getLongitude(),
                glideSlopePosition.getElevation() + elevationBottomAngle * 10000);

        Position topLeftEndpoint = new Position(leftEndpointUtm.getLatitude(), leftEndpointUtm.getLongitude(),
                glideSlopePosition.getElevation() + elevationTopAngle * 10000);

        Position bottomRightEndpoint = new Position(rightEndpointUtm.getLatitude(), rightEndpointUtm.getLongitude(),
                glideSlopePosition.getElevation() + elevationBottomAngle * 10000);

        Position topRightEndpoint = new Position(rightEndpointUtm.getLatitude(), rightEndpointUtm.getLongitude(),
                glideSlopePosition.getElevation() + elevationTopAngle * 10000);

        Path landingLeftPath = new Path(localizerPosition, runwayEdgeLeft);
        landingLeftPath.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        landingLeftPath.setOffset(1);
        landingLeftPath.setFollowTerrain(true);

        Path landingRightPath = new Path(localizerPosition, runwayEdgeRight);
        landingRightPath.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        landingRightPath.setOffset(1);
        landingRightPath.setFollowTerrain(true);

        Path bottomLeftPath = new Path(runwayEdgeLeft, bottomLeftEndpoint);
        Path bottomRightPath = new Path(runwayEdgeRight, bottomRightEndpoint);
        Path topLeftPath = new Path(runwayEdgeLeft, topLeftEndpoint);
        Path topRightPath = new Path(runwayEdgeRight, topRightEndpoint);

        ShapeAttributes attributes = new BasicShapeAttributes();
        attributes.setOutlineWidth(3);
        attributes.setOutlineMaterial(new Material(new Color(100, 255, 100)));
        attributes.setDrawOutline(true);
        attributes.setOutlineOpacity(.5);

        landingLeftPath.setAttributes(attributes);
        landingRightPath.setAttributes(attributes.copy());
        bottomLeftPath.setAttributes(attributes.copy());
        bottomRightPath.setAttributes(attributes.copy());
        topLeftPath.setAttributes(attributes.copy());
        topRightPath.setAttributes(attributes.copy());

        renderableLayer.addRenderable(landingLeftPath);
        renderableLayer.addRenderable(landingRightPath);
        renderableLayer.addRenderable(bottomLeftPath);
        renderableLayer.addRenderable(bottomRightPath);
        renderableLayer.addRenderable(topLeftPath);
        renderableLayer.addRenderable(topRightPath);
    }
}
