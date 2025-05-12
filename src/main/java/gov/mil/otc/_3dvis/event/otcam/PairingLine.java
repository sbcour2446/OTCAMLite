package gov.mil.otc._3dvis.event.otcam;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class PairingLine extends Path {

    PairingLine(Position position1, Position position2, boolean groundClamp, Color color) {
        super();
        setOffset(1);
        setAltitudeMode(groundClamp ? WorldWind.CLAMP_TO_GROUND : WorldWind.ABSOLUTE);
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setOutlineOpacity(.5);
        shapeAttributes.setOutlineMaterial(new Material(color));
        shapeAttributes.setOutlineWidth(5);
        setAttributes(shapeAttributes);
        List<Position> positions = new ArrayList<>();
        positions.add(position1);
        positions.add(position2);
        setPositions(positions);
    }
}
