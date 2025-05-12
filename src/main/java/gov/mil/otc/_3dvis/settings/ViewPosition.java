package gov.mil.otc._3dvis.settings;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

public class ViewPosition {

    private Position eyePosition;
    private Angle heading;
    private Angle pitch;
    private double eyeDistance;

    public Position getEyePosition() {
        return eyePosition;
    }

    public void setEyePosition(Position eyePosition) {
        this.eyePosition = eyePosition;
    }

    public Angle getHeading() {
        return heading;
    }

    public void setHeading(Angle heading) {
        this.heading = heading;
    }

    public Angle getPitch() {
        return pitch;
    }

    public void setPitch(Angle pitch) {
        this.pitch = pitch;
    }

    public double getEyeDistance() {
        return eyeDistance;
    }

    public void setEyeDistance(double eyeDistance) {
        this.eyeDistance = eyeDistance;
    }
}
