package gov.mil.otc._3dvis.ui.widgets.coordinates;

import gov.nasa.worldwind.geom.Position;

public interface ICoordinatesController {

    void clear();
    Position getPosition();
    void setPosition(Position position);
}
