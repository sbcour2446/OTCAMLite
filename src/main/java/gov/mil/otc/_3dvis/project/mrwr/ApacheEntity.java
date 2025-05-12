package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class ApacheEntity extends PlaybackEntity {

    private static final double RADIUS = 1000.0;
    private static final double HEADING_LINE_LENGTH_ANGLE = Math.asin(50.0 / 6378137.0);
    private static final double WPN_SYS_RSP_LINE_LENGTH_ANGLE = Math.asin(5000.0 / 6378137.0);

    private final TimedDataSet<TadsStatus> tadStatusTimedDataSet = new TimedDataSet<>();
    private final TimedDataSet<WeaponSystemResponse> weaponSystemResponseList = new TimedDataSet<>();
    private final List<ThreatLine> threatLineList = new ArrayList<>();
    private final Map<Integer, Path> weaponSystemResponseLineMap = new TreeMap<>();
    private final Path headingLine = new Path();
    private final Path tailLine = new Path();
    private final Path wingLeftLine = new Path();
    private final Path wingRightLine = new Path();
    private final Path tadsLine = new Path();
    private boolean showing = false;
    private boolean showTads = false;
    private boolean tadsShowing = false;
    private boolean showOrientationVisual = true;
    private boolean showWeaponSystemResponse = false;
    private boolean weaponSystemResponseShowing = false;
    private double stabilizedAz = 0;
    private double stabilizedEl = 0;

    public ApacheEntity(EntityId entityId) {
        super(entityId);

        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setOutlineMaterial(Material.BLACK);
//        shapeAttributes.setOutlineOpacity(.75);
//        shapeAttributes.setOutlineStippleFactor(1);
        shapeAttributes.setOutlineWidth(3);
        headingLine.setAttributes(shapeAttributes);

        shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setOutlineMaterial(Material.WHITE);
        shapeAttributes.setOutlineWidth(3);
        tailLine.setAttributes(shapeAttributes);

        shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setOutlineMaterial(Material.RED);
        shapeAttributes.setOutlineWidth(3);
        wingLeftLine.setAttributes(shapeAttributes);

        shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setOutlineMaterial(Material.GREEN);
        shapeAttributes.setOutlineWidth(3);
        wingRightLine.setAttributes(shapeAttributes);

        shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setOutlineMaterial(Material.GREEN);
        shapeAttributes.setOutlineOpacity(.5);
        shapeAttributes.setOutlineWidth(2);
        tadsLine.setAttributes(shapeAttributes);
    }

    @Override
    public void addContextMenuItems(ContextMenu contextMenu) {
        super.addContextMenuItems(contextMenu);

        MenuItem menuItem = new MenuItem("Show Status");
        menuItem.setOnAction(actionEvent -> {
            ApacheStatusWidgetPane.show(this);
        });
        contextMenu.getItems().add(menuItem);

        CheckMenuItem checkMenuItem1 = new CheckMenuItem("Show Orientation Visual");
        checkMenuItem1.setSelected(showOrientationVisual);
        checkMenuItem1.setOnAction(actionEvent -> {
            showOrientationVisual = checkMenuItem1.isSelected();
        });
        contextMenu.getItems().add(checkMenuItem1);

        CheckMenuItem checkMenuItem2 = new CheckMenuItem("Show Weapon System Response Lines");
        checkMenuItem2.setSelected(showWeaponSystemResponse);
        checkMenuItem2.setOnAction(actionEvent -> {
            showWeaponSystemResponse = checkMenuItem2.isSelected();
        });
        contextMenu.getItems().add(checkMenuItem2);

        CheckMenuItem checkMenuItem3 = new CheckMenuItem("Show TADS Line");
        checkMenuItem3.setSelected(showTads);
        checkMenuItem3.setOnAction(actionEvent -> {
            showTads = checkMenuItem3.isSelected();
        });
        contextMenu.getItems().add(checkMenuItem3);
    }

    @Override
    protected StatusAnnotation createStatusAnnotation() {
        return new ApacheStatusAnnotation(this);
    }

    @Override
    public void dispose() {
        ApacheStatusWidgetPane.close(this);
        EntityLayer.remove(headingLine);
        EntityLayer.remove(tailLine);
        EntityLayer.remove(wingLeftLine);
        EntityLayer.remove(wingRightLine);
        EntityLayer.remove(tadsLine);
        super.dispose();
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        boolean hasChange = super.update(time, entityFilter);
        hasChange |= tadStatusTimedDataSet.updateTime(time);

        TspiData tspiData = getCurrentTspi();
        boolean showVisual = showOrientationVisual && tspiData != null && getEntityDetail() != null && !getEntityDetail().isOutOfComms()
                && isFiltered();

        if (showVisual) {
            if (!showing) {
                showing = true;
                EntityLayer.add(headingLine);
                EntityLayer.add(tailLine);
                EntityLayer.add(wingLeftLine);
                EntityLayer.add(wingRightLine);
                updateHeadingLine(tspiData);
            } else if (hasChange) {
                updateHeadingLine(tspiData);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(headingLine);
            EntityLayer.remove(tailLine);
            EntityLayer.remove(wingLeftLine);
            EntityLayer.remove(wingRightLine);
        }

        TadsStatus tadsStatus = tadStatusTimedDataSet.getCurrent();
        boolean showTadsLine = showTads && showVisual && tspiData.isOrientationValid() && tadsStatus != null;
        if (showTadsLine) {
            if (!tadsShowing) {
                tadsShowing = true;
                EntityLayer.add(tadsLine);
            } else if (hasChange) {
                updateTadsLine(tspiData, tadsStatus);
            }
        } else if (tadsShowing) {
            tadsShowing = false;
            EntityLayer.remove(tadsLine);
        }

        for (ThreatLine threatLine : threatLineList) {
            threatLine.update(tspiData, showVisual);
        }

        boolean showWeaponSystemResponseLine = showWeaponSystemResponse && showVisual && tspiData.isOrientationValid();
        if (showWeaponSystemResponseLine) {
            if (weaponSystemResponseList.updateTime(time)) {
                updateWeaponSystemResponseLines(tspiData, true);
                updateStatusDisplay();
            }
        } else if (weaponSystemResponseShowing) {
            updateWeaponSystemResponseLines(tspiData, false);
        }

        return hasChange;
    }

    public void addTadStatuses(List<TadsStatus> tadsStatuses) {
        tadStatusTimedDataSet.addAll(tadsStatuses);
    }

    public void addThreatEntity(ThreatEntity threatEntity) {
        threatLineList.add(new ThreatLine(threatEntity));
    }

    public void addWeaponSystemResponses(List<WeaponSystemResponse> weaponSystemResponses) {
        weaponSystemResponseList.addAll(weaponSystemResponses);
    }

    public WeaponSystemResponse getCurrentWeaponSystemResponse() {
        return weaponSystemResponseList.getCurrent();
    }

    public List<ThreatLine> getThreatLineList() {
        return threatLineList;
    }

    private void updateHeadingLine(TspiData tspiData) {
        UTMCoord utm = UTMCoord.fromLatLon(tspiData.getPosition().getLatitude(), tspiData.getPosition().getLongitude());

        Position headingPosition = new Position(UTMCoord.locationFromUTMCoord(
                utm.getZone(), utm.getHemisphere(),
                utm.getEasting() + sin(tspiData.getHeading() * 0.0174533) * 40,
                utm.getNorthing() + cos(tspiData.getHeading() * 0.0174533) * 40,
                null),
                tspiData.getPosition().getElevation() + sin(tspiData.getPitch() * 0.0174533) * 40);

        Position tailPosition = new Position(UTMCoord.locationFromUTMCoord(
                utm.getZone(), utm.getHemisphere(),
                utm.getEasting() + sin((tspiData.getHeading() + 180) * 0.0174533) * 20,
                utm.getNorthing() + cos((tspiData.getHeading() + 180) * 0.0174533) * 20,
                null),
                tspiData.getPosition().getElevation() + sin((tspiData.getPitch() + 180) * 0.0174533) * 20);

        Position wingLeftPosition = new Position(UTMCoord.locationFromUTMCoord(
                utm.getZone(), utm.getHemisphere(),
                utm.getEasting() + sin((tspiData.getHeading() - 90) * 0.0174533) * 10,
                utm.getNorthing() + cos((tspiData.getHeading() - 90) * 0.0174533) * 10,
                null),
                tspiData.getPosition().getElevation() + sin(tspiData.getRoll() * 0.0174533) * 10);

        Position wingRightPosition = new Position(UTMCoord.locationFromUTMCoord(
                utm.getZone(), utm.getHemisphere(),
                utm.getEasting() + sin((tspiData.getHeading() + 90) * 0.0174533) * 10,
                utm.getNorthing() + cos((tspiData.getHeading() + 90) * 0.0174533) * 10,
                null),
                tspiData.getPosition().getElevation() + sin((tspiData.getRoll() + 180) * 0.0174533) * 10);

//
//        LatLon headingLatLon = Position.greatCircleEndPosition(tspiData.getPosition(),
//                Angle.fromDegrees(tspiData.getHeading()), Angle.fromRadians(HEADING_LINE_LENGTH_ANGLE));
        final List<Position> positions = new ArrayList<>();
        positions.add(tspiData.getPosition());//tspiData.getPosition());
        positions.add(headingPosition);//new Position(headingLatLon, tspiData.getPosition().elevation));
        final List<Position> positions2 = new ArrayList<>();
        positions2.add(tspiData.getPosition());
        positions2.add(tailPosition);
        final List<Position> positions3 = new ArrayList<>();
        positions3.add(tspiData.getPosition());
        positions3.add(wingLeftPosition);
        final List<Position> positions4 = new ArrayList<>();
        positions4.add(tspiData.getPosition());
        positions4.add(wingRightPosition);
        SwingUtilities.invokeLater(() -> {
            headingLine.setPositions(positions);
            tailLine.setPositions(positions2);
            wingLeftLine.setPositions(positions3);
            wingRightLine.setPositions(positions4);
            headingLine.setValue(AVKey.ROLLOVER_TEXT, String.format("%.2f°", tspiData.getHeading()));
        });
    }

    private void updateWeaponSystemResponseLines(TspiData tspiData, boolean show) {
        boolean showingSomething = false;
        WeaponSystemResponse weaponSystemResponse = getCurrentWeaponSystemResponse();
        for (int i = 1; i < WeaponSystemResponse.NUMBER_OF_MESSAGES; i++) {
            Path path = weaponSystemResponseLineMap.get(i);
            if (!show) {
                if (path != null) {
                    EntityLayer.remove(path);
                    weaponSystemResponseLineMap.remove(i);
                }
                continue;
            }

            WeaponSystemResponse.WeaponSystemResponseMessage weaponSystemResponseMessage = null;
            if (weaponSystemResponse != null) {
                weaponSystemResponseMessage = weaponSystemResponse.getWeaponSystemResponseMessageMap().get(i);
            }

            if (weaponSystemResponseMessage == null) {
                if (path != null) {
                    EntityLayer.remove(path);
                    weaponSystemResponseLineMap.remove(i);
                }
            } else {
                if (path == null) {
                    path = new Path();
                    ShapeAttributes shapeAttributes = new BasicShapeAttributes();
                    shapeAttributes.setDrawInterior(false);
                    shapeAttributes.setDrawOutline(true);
                    shapeAttributes.setOutlineMaterial(Material.YELLOW);
                    shapeAttributes.setOutlineOpacity(.5);
                    shapeAttributes.setOutlineWidth(2);
                    path.setAttributes(shapeAttributes);
                    EntityLayer.add(path);
                    weaponSystemResponseLineMap.put(i, path);
                }
                LatLon headingLatLon = Position.greatCircleEndPosition(tspiData.getPosition(),
                        Angle.fromDegrees(tspiData.getHeading() + weaponSystemResponseMessage.azimuth()),
                        Angle.fromRadians(WPN_SYS_RSP_LINE_LENGTH_ANGLE));
                final List<Position> positions = new ArrayList<>();
                final Path finalPath = path;
                final double azimuth = weaponSystemResponseMessage.azimuth();
                final String name = weaponSystemResponseMessage.getWeaponSystemName();
                positions.add(tspiData.getPosition());
                positions.add(new Position(headingLatLon, tspiData.getPosition().elevation));
                SwingUtilities.invokeLater(() -> {
                    finalPath.setPositions(positions);
                    finalPath.setValue(AVKey.ROLLOVER_TEXT, String.format("%.2f°%s%s", azimuth, System.lineSeparator(), name));
                });
                showingSomething = true;
            }
        }
        weaponSystemResponseShowing = showingSomething;
    }

    private void updateTadsLine(TspiData tspiData, TadsStatus tadsStatus) {
        computeAzEl(tspiData.getHeading() * 0.0174533, tspiData.getPitch() * 0.0174533, tspiData.getRoll() * 0.0174533,
                tadsStatus.getElevation() * 0.0174533, tadsStatus.getAzimuth() * 0.0174533);

        UTMCoord utm = UTMCoord.fromLatLon(tspiData.getPosition().getLatitude(), tspiData.getPosition().getLongitude());
        Position endPosition = new Position(UTMCoord.locationFromUTMCoord(
                utm.getZone(), utm.getHemisphere(),
                utm.getEasting() + sin(stabilizedAz) * tadsStatus.getRange(),
                utm.getNorthing() + cos(stabilizedAz) * tadsStatus.getRange(),
                null),
                tspiData.getPosition().getElevation() + sin(stabilizedEl) * tadsStatus.getRange());

        final List<Position> positions = new ArrayList<>();
        positions.add(tspiData.getPosition());
        positions.add(endPosition);
        SwingUtilities.invokeLater(() -> {
            tadsLine.setPositions(positions);
            tadsLine.setValue(AVKey.ROLLOVER_TEXT, String.format("Range: %d", tadsStatus.getRange()));
        });
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

        stabilizedAz = Math.atan2(vJ, vI);                                 // Composite Azimuth (in Radians)
        stabilizedEl = -Math.asin(vK / Math.sqrt(vI * vI + vJ * vJ + vK * vK));    // Composite Elevation (in Radians)
//        stablizedEl -= Deg4;
        // Azimuth is reported as 0 ->180 (through East) and 0 ->-180 (through East)
        // Convert to 0 ->359.

        if (stabilizedAz < 0) {
            stabilizedAz += 2 * Math.PI;
        }
    }
}
