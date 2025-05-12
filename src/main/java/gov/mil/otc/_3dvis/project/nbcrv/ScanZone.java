package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.Wedge;

import java.awt.*;

public class ScanZone {

    private static final Material MATERIAL = new Material(Color.GRAY);
    private static final double OPACITY = 0.4;
    private static final long TIMEOUT = 1000;
    private final long timestamp;
    private final Wedge scanZone;
    private boolean showing = false;

    protected ScanZone(long timestamp, Position position, double arcAngle, double radius, double direction) {
        this.timestamp = timestamp;
        scanZone = createScanZone(position, arcAngle, radius, direction);
    }

    private Wedge createScanZone(Position position, double arcAngle, double radius, double direction) {
        Wedge wedge = new Wedge(position,
                Angle.fromDegrees(arcAngle),
                radius,
                .1,
                radius,
                Angle.fromDegrees(direction),
                Angle.ZERO, Angle.ZERO);
        wedge.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setInteriorMaterial(MATERIAL);
        shapeAttributes.setInteriorOpacity(OPACITY);
        shapeAttributes.setOutlineMaterial(MATERIAL);
        shapeAttributes.setOutlineOpacity(OPACITY);
        shapeAttributes.setOutlineWidth(2);
        wedge.setAttributes(shapeAttributes);
        return wedge;
    }

    protected boolean update(long time, Position position) {
        boolean show = position != null && time >= timestamp && time < timestamp + TIMEOUT;
        if (show) {
            if (!showing) {
                showing = true;
                scanZone.setCenterPosition(position);
                EntityLayer.add(scanZone);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(scanZone);
        }
        return showing;
    }
}
