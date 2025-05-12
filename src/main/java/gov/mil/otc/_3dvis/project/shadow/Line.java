package gov.mil.otc._3dvis.project.shadow;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Line extends Path {

    private final AtomicBoolean updated = new AtomicBoolean(false);
    private short stipplePattern = (short) 0xAAAA;
    private Position p1 = null;
    private Position p2 = null;

    public Line() {
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setOutlineMaterial(Material.BLACK);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setOutlineWidth(2);
        shapeAttributes.setOutlineStippleFactor(5);
        shapeAttributes.setOutlineStipplePattern(stipplePattern);
        setAttributes(shapeAttributes);
    }

    public void setEndpoints(Position p1, Position p2){
        this.p1 = p1;
        this.p2 = p2;
        updated.set(true);
    }

    public void setLineSolid(boolean tf) {
        if (tf) {
            stipplePattern = (short) 0xFFFF;
        } else {
            stipplePattern = (short) 0xAAAA;
        }
        updated.set(true);
    }

    @Override
    public void preRender(DrawContext dc) {
        if (updated.get()) {
            updated.set(false);
            getAttributes().setOutlineStipplePattern(stipplePattern);
            List<Position> endPoints = new ArrayList<>();
            endPoints.add(p1);
            endPoints.add(p2);
            setPositions(endPoints);
        }
        super.preRender(dc);
    }
}
