package gov.mil.otc._3dvis.project.shadow;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Footprint extends SurfacePolygon {

    private final AtomicBoolean updated = new AtomicBoolean(false);
    private Position uL = null;
    private Position uR = null;
    private Position lL = null;
    private Position lR = null;

    public Footprint() {
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setOutlineMaterial(Material.MAGENTA);
        shapeAttributes.setOutlineWidth(1);
        shapeAttributes.setOutlineOpacity(1);
        setAttributes(shapeAttributes);
        setValue(AVKey.ROLLOVER_TEXT, "Approximate camera view area.");
    }

    public void updateCorners(Position uL, Position uR, Position lL, Position lR) {
        this.uL = uL;
        this.uR = uR;
        this.lL = lL;
        this.lR = lR;
        updated.set(true);
    }

    @Override
    public void preRender(DrawContext dc) {
        if (updated.get()) {
            updated.set(false);
            List<Position> corners = new ArrayList<>();
            corners.add(uL);
            corners.add(uR);
            corners.add(lL);
            corners.add(lR);
            setOuterBoundary(corners);
        }
        super.preRender(dc);
    }
}
