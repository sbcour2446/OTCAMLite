package gov.mil.otc._3dvis.project.shadow;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.datamodel.aircraft.TspiExtendedData;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.entity.aircraft.AircraftEntity;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.util.RayCastingSupport;

import java.util.List;

public class ShadowEntity extends AircraftEntity {

    private static final double RADIUS = 1000.0;
    private final TimedDataSet<TspiExtendedData> tspiExtendedDataTimedDataSet = new TimedDataSet<>();
    private final TimedDataSet<UasPayloadData> uasPayloadDataTimedDataSet = new TimedDataSet<>();
    private final Footprint footprint = new Footprint();
    private final Line centerline = new Line();
    private double stablizedAz;
    private double stablizedEl;
    private boolean useReportedCenter = true;
    private boolean showing = false;

    public ShadowEntity(EntityId entityId) {
        super(entityId);
    }

    public void addTspiExtendedData(TspiExtendedData tspiExtendedData) {
        tspiExtendedDataTimedDataSet.add(tspiExtendedData);
    }

    public void addTspiExtendedDataList(List<TspiExtendedData> tspiExtendedList) {
        tspiExtendedDataTimedDataSet.addAll(tspiExtendedList);
    }

    public TspiExtendedData getTspiExtendedData() {
        return tspiExtendedDataTimedDataSet.getCurrent();
    }

    public void addUasPayloadData(UasPayloadData uasPayloadData) {
        uasPayloadDataTimedDataSet.add(uasPayloadData);
    }

    public UasPayloadData getUasPayloadData() {
        return uasPayloadDataTimedDataSet.getCurrent();
    }

    @Override
    public void dispose() {
        EntityLayer.remove(centerline);
        EntityLayer.remove(footprint);
        super.dispose();
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        boolean hasChange = super.update(time, entityFilter);
        boolean hasTspiChange = tspiExtendedDataTimedDataSet.updateTime(time);
        boolean hasPayloadChange = uasPayloadDataTimedDataSet.updateTime(time);

        if (hasTspiChange || hasPayloadChange) {
            updateStatusDisplay();
        }

        if (hasChange || hasTspiChange || hasPayloadChange) {
            updatePayloadDisplay();
        }

        return hasChange || hasTspiChange || hasPayloadChange;
    }

    private void updatePayloadDisplay() {
        Position position = getPosition();
        TspiExtendedData tspiExtendedData = tspiExtendedDataTimedDataSet.getCurrent();
        UasPayloadData uasPayloadData = uasPayloadDataTimedDataSet.getCurrent();

        Position centerPoint = null;
        Position uL = null;
        Position uR = null;
        Position lL = null;
        Position lR = null;

        if (position != null && tspiExtendedData != null && uasPayloadData != null) {
//            switch (uasPayloadData.getImageOutputState()) {
//                case 1:
//                    footprintMaterial = eoMaterial;
//                    break;//1 = EO
//                case 2:
//                    footprintMaterial = irMaterial;
//                    break;//2 = IR
//                default:
//                    footprintMaterial = Material.WHITE;
//                    //0 = None, 3 = Both, 4 = PAYLOAD-Specific
//            }

//            shadowLines.get(ShadowLineId.CENTERPOINT).setLineSolid(rec.LaserDesignatorStaus > 0);
//            shadowLines.get(ShadowLineId.CENTERPOINT).setColor(
//                    UasPayloadDisplay.getLaserModeColor(
//                            LaserMode.fromInt(rec.FLaserPointerStatus)));

            // only calculate of the payload data is valid and uncaged
            if ((uasPayloadData.getSysOpModeState() == 5) && (uasPayloadData.isImagePosition())) {

                // get the UAS payload location in WorldWind XYZ
                Vec4 payloadPoint = WWController.getGlobe().computePointFromPosition(position);

                computeAzEl(tspiExtendedData.getPsi(),
                        tspiExtendedData.getTheta(),
                        tspiExtendedData.getPhi(),
                        uasPayloadData.getActCenterElAngle(),
                        uasPayloadData.getActCenterAzAngle());

//                isVisible = true;

                // get the video center from either the STANAG or the stab
                if (useReportedCenter) {
                    // get the center point reported by the payload in WorldWind XYZ
                    centerPoint = Position.fromDegrees(uasPayloadData.getLatitude(), uasPayloadData.getLongitude(), uasPayloadData.getAltitude());

                    Angle azAngle = LatLon.linearAzimuth(position, centerPoint);
                    double distance = LatLon.linearDistance(position, centerPoint).radians * WWController.getGlobe().getRadius();
                    double deltaEl = position.elevation - centerPoint.elevation;
                    double el = Math.atan(distance / deltaEl);
                    double upperEl = el + uasPayloadData.getActVertFieldOfView() / 2;
                    double lowerEl = el - uasPayloadData.getActVertFieldOfView() / 2;
                    double upperDistance = Math.tan(upperEl) * deltaEl;
                    double lowerDistance = Math.tan(lowerEl) * deltaEl;
                    Angle upperAngle = Angle.fromRadians(upperDistance / WWController.getGlobe().getRadius());
                    Angle lowerAngle = Angle.fromRadians(lowerDistance / WWController.getGlobe().getRadius());

                    if (upperAngle.radians > 0) {
                        uL = new Position(LatLon.linearEndPosition(position,
                                azAngle.addRadians(-uasPayloadData.getActHorFieldOfView() / 2),
                                upperAngle),
                                centerPoint.elevation);
                        uR = new Position(LatLon.linearEndPosition(position,
                                azAngle.addRadians(uasPayloadData.getActHorFieldOfView() / 2),
                                upperAngle),
                                centerPoint.elevation);
                    }
                    lR = new Position(LatLon.linearEndPosition(position,
                            azAngle.addRadians(uasPayloadData.getActHorFieldOfView() / 2),
                            lowerAngle),
                            centerPoint.elevation);
                    lL = new Position(LatLon.linearEndPosition(position,
                            azAngle.addRadians(-uasPayloadData.getActHorFieldOfView() / 2),
                            lowerAngle),
                            centerPoint.elevation);
                } else {
                    // get the ground position we calculate from pointing angles
                    centerPoint = getGroundPosition(position, payloadPoint, 0, 0);

                    uL = getGroundPosition(position, payloadPoint, -uasPayloadData.getActHorFieldOfView() / 2,
                            uasPayloadData.getActVertFieldOfView() / 2);
                    uR = getGroundPosition(position, payloadPoint, uasPayloadData.getActHorFieldOfView() / 2,
                            uasPayloadData.getActVertFieldOfView() / 2);
                    lR = getGroundPosition(position, payloadPoint, uasPayloadData.getActHorFieldOfView() / 2,
                            -uasPayloadData.getActVertFieldOfView() / 2);
                    lL = getGroundPosition(position, payloadPoint, -uasPayloadData.getActHorFieldOfView() / 2,
                            -uasPayloadData.getActVertFieldOfView() / 2);
                }

                footprint.updateCorners(uL, uR, lR, lL);
                centerline.setEndpoints(position, centerPoint);

//                updateVisibility(centerPoint != null && showCenterLine, ShadowLineId.CENTERPOINT);
//                updateVisibility(uL != null && showCornerLines, ShadowLineId.UPPERLEFT);
//                updateVisibility(uR != null && showCornerLines, ShadowLineId.UPPERRIGHT);
//                updateVisibility(lR != null && showCornerLines, ShadowLineId.LOWERRIGHT);
//                updateVisibility(lL != null && showCornerLines, ShadowLineId.LOWERLEFT);
//
//                boundryValid = uL != null && uL != null && uL != null && uL != null;
//
//                if (boundryValid && showFootprint) {
//                    if (!footprint.isShown) {
//                        toolsVisualizationLayer.addShadowRenderable(footprint);
//                        footprint.isShown = true;
//                    }
//                } else if (footprint.isShown) {
//                    toolsVisualizationLayer.removeShadowRenderable(footprint);
//                    footprint.isShown = false;
//                }

            } else {
                // don't show display of the payload is not uncaged and computing a target location
//                for (ShadowLineId id : ShadowLineId.values()) {
//                    updateVisibility(false, id);
//                }
//                if (footprint.isShown) {
//                    toolsVisualizationLayer.removeShadowRenderable(footprint);
//                    footprint.isShown = false;
//                }
            }
        }

        boolean show = isFiltered() && centerPoint != null && uL != null && uR != null && lR != null && lL != null;

        if (!showing && show) {
            showing = true;
            EntityLayer.add(centerline);
            EntityLayer.add(footprint);
        } else if (showing && !show) {
            showing = false;
            EntityLayer.remove(centerline);
            EntityLayer.remove(footprint);
        }
    }

    private void computeAzEl(
            double heading, // Heading is psi	(+ = Right Turn, 0 = North,  90 = East, 180 = South & 270 = West))
            double pitch, // Pitch is theta	(+ = Nose Up, +90 = Up, 0 = Level & -90 = Down)
            double roll, // Roll is phi		(+ = Right Side Down)
            double elevation, // Radar Elevation	(+ = Up)
            double azimuth // Radar Azimuth	(+ = Clockwise)
    ) {
        // Convert from Degrees to Radians
        //
        // Pre-compute the SIN's and COS's
        //
        double cosRoll = Math.cos(roll);
        double cosPitch = Math.cos(pitch);
        double cosHeading = Math.cos(heading);
        double cosElevation = Math.cos(-elevation);
        double cosAzimuth = Math.cos(azimuth);
        double sinRoll = Math.sin(roll);
        double sinPitch = Math.sin(pitch);
        double sinHeading = Math.sin(heading);
        double sinElevation = Math.sin(-elevation);
        double sinAzimuth = Math.sin(azimuth);

        // The body frame is a right-handed frame with the origin at the center of the rotation axis and is oriented as follows:
        //
        //		The positive X-axis (longitudinal axis) is movement in the direction of the nose.
        //		The positive Y-axis (lateral axis) is movement in the direction of the right wing.
        //		The positive Z-axis (vertival axis) is movement in the down direction.
        //
        // The order of rotation is Heading, Pitch and then Roll.
        //
        // In airborne radar systems the position of the target with respect to the aircraft is measured by antenna azimuth and
        // elevation angles at the time of target illumination. The position of the aircraft with respect to an inertial system
        // is measured by the inertial navigation system. Thus the position of the target in an inertial system can be obtained
        // by affecting a number of transformations first through the azimuth and elevation angles and then through the aircraft
        // attitude angles. To show this, we use North East Down (NED) inertial coordinate system (with the aircraft at the
        // origin)where the North axis points to the North the East axis to the East and the Down axis to the center of the earth.
        //
        double vI = cosAzimuth * cosElevation;
        double vJ = sinAzimuth * cosElevation;
        double vK = sinElevation;
        double vI1;
        double vJ1;

        // Rotate about the Roll
        //
        vJ1 = vJ * cosRoll - vK * sinRoll;
        vK = vJ * sinRoll + vK * cosRoll;
        vJ = vJ1;

        // Rotate about the Pitch
        //
        vI1 = vI * cosPitch + vK * sinPitch;
        vK = -vI * sinPitch + vK * cosPitch;
        vI = vI1;

        // Rotate about the Heading
        //
        vI1 = vI * cosHeading - vJ * sinHeading;
        vJ = vI * sinHeading + vJ * cosHeading;
        vI = vI1;

        stablizedAz = Math.atan2(vJ, vI);                                 // Composite Azimuth (in Radians)
        stablizedEl = -Math.asin(vK / Math.sqrt(vI * vI + vJ * vJ + vK * vK));    // Composite Elevation (in Radians)
//        stablizedEl -= Deg4;
        // Azimuth is reported as 0 ->180 (through East) and 0 ->-180 (through East)
        // Convert to 0 ->359.

        if (stablizedAz < 0) {
            stablizedAz += 2 * Math.PI;
        }
    }

    private Position getGroundPosition(Position position, Vec4 payloadPoint, double azOffset, double elOffset) {
        double az = stablizedAz + azOffset;
        double el = stablizedEl + elOffset;

        // computer a remote point a fixed distance away in the UTM system and then use that point to compute a direction vector
        UTMCoord uasUtm = UTMCoord.fromLatLon(position.latitude, position.longitude);
        UTMCoord remoteUtm = UTMCoord.fromUTM(uasUtm.getZone(), uasUtm.getHemisphere(),
                uasUtm.getEasting() + RADIUS * Math.sin(az) * Math.cos(el),
                uasUtm.getNorthing() + RADIUS * Math.cos(az) * Math.cos(el),
                null);
        Position remotePos = Position.fromDegrees(remoteUtm.getLatitude().degrees, remoteUtm.getLongitude().degrees,
                position.elevation + RADIUS * Math.sin(el));
        Vec4 remotePoint = WWController.getGlobe().computePointFromPosition(remotePos);

        // now compute the direction vector using the payload XYZ point and remote XYZ point
        Vec4 direction = remotePoint.subtract3(payloadPoint);

        // now ray cast the payload point to the intersection with the terrain to get the ground location
        return (RayCastingSupport.intersectRayWithTerrain(WWController.getGlobe(), payloadPoint, direction));
//        return (RayCastingSupport.intersectRayWithTerrain(globe, payloadPoint, direction, 50, 5));
    }

    @Override
    protected StatusAnnotation createStatusAnnotation() {
        return new ShadowStatusAnnotation(this);
    }
}
