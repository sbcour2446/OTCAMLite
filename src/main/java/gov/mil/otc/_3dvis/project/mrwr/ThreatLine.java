package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ThreatLine {

    private static final Material DEFAULT_MATERIAL = Material.DARK_GRAY;
    private final ThreatEntity threatEntity;
    private final Path line = new Path();
    private boolean showing = false;
    private boolean isValid = false;
    private boolean inRange = false;
    private Position threatPosition = null;
    private double threatAngle;
    private int slantRange;
    private TspiData lastTspiData = null;

    public ThreatLine(ThreatEntity threatEntity) {
        this.threatEntity = threatEntity;

        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setOutlineMaterial(DEFAULT_MATERIAL);
        shapeAttributes.setOutlineOpacity(.5);
        shapeAttributes.setOutlineStippleFactor(2);
        shapeAttributes.setOutlineWidth(3);
        line.setAttributes(shapeAttributes);
    }

    public EntityId getEntityId() {
        return threatEntity.getEntityId();
    }

    public String getThreatName() {
        return threatEntity.getName();
    }

    public boolean isValid() {
        return isValid;
    }

    public double getThreatAngle() {
        return threatAngle;
    }

    public int getSlantRange() {
        return slantRange;
    }

    public boolean isInRange() {
        return inRange;
    }

    public void update(TspiData tspiData, boolean isVisible) {
        Position currentThreatPosition = threatEntity.getPosition();

        if (currentThreatPosition == null || tspiData == null || tspiData.getPosition() == null || !threatEntity.isFiltered()) {
            isValid = false;
            if (showing) {
                showing = false;
                EntityLayer.remove(line);
            }
            return;
        }

        boolean hasChange = false;
        isValid = true;

        if (!currentThreatPosition.equals(threatPosition)) {
            double groundElevation = currentThreatPosition.elevation;
            if (WWController.getGlobe() != null) {
                groundElevation = WWController.getGlobe().getElevation(currentThreatPosition.latitude,
                        currentThreatPosition.longitude);
            }

            threatPosition = new Position(currentThreatPosition, groundElevation);
            hasChange = true;
        }

        if (!tspiData.equals(lastTspiData)) {
            lastTspiData = tspiData;

            threatAngle = LatLon.greatCircleAzimuth(tspiData.getPosition(), threatPosition).degrees;
            if (threatAngle < 0) {
                threatAngle += 360.0;
            }
            threatAngle -= tspiData.getHeading();
            if (threatAngle < 0) {
                threatAngle += 360.0;
            }

            slantRange = (int) Utility.calculateDistance1(tspiData.getPosition(), threatPosition);
            inRange = slantRange <= threatEntity.getRange();
            if (slantRange < 0 || slantRange > ThreatEntity.MAX_RANGE) {
                isValid = false;
            }

            hasChange = true;
        }

        if (slantRange > threatEntity.getRange() * 1.1) {
            isVisible = false;
        }

        if (isVisible) {
            if (!showing) {
                showing = true;
                List<Position> positions = new ArrayList<>();
                positions.add(tspiData.getPosition());
                positions.add(threatPosition);
                line.setPositions(positions);
                line.setValue(AVKey.ROLLOVER_TEXT, String.format("%.2f\u00B0, %d(m)", threatAngle, slantRange));
                EntityLayer.add(line);
            } else if (hasChange) {
                updateVisual(tspiData.getPosition());
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(line);
        }
    }

    private void updateVisual(final Position position) {
        SwingUtilities.invokeLater(() -> {
            List<Position> positions = new ArrayList<>();
            positions.add(position);
            positions.add(threatPosition);
            line.setPositions(positions);
            line.setValue(AVKey.ROLLOVER_TEXT, String.format("%.2f\u00B0, %d(m)", threatAngle, slantRange));
            if (inRange) {
                line.getAttributes().setOutlineMaterial(Material.RED);
            } else {
                line.getAttributes().setOutlineMaterial(DEFAULT_MATERIAL);
            }
        });
    }
}
