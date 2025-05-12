package gov.mil.otc._3dvis.worldwindex.view.orbit;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

public class BasicOrbitViewEx extends BasicOrbitView {

    public void goTo(Position position, Angle heading, Angle pitch, double distance) {
        if (viewInputHandler instanceof OrbitViewInputHandlerEx) {
            ((OrbitViewInputHandlerEx) viewInputHandler).goTo(position, heading, pitch, distance);
        } else {
            viewInputHandler.goTo(position, distance);
        }
    }
}
