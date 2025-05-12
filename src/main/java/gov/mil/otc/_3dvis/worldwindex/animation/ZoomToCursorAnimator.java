package gov.mil.otc._3dvis.worldwindex.animation;

import gov.nasa.worldwind.animation.MoveToDoubleAnimator;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.PropertyAccessor;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

import java.awt.*;

public class ZoomToCursorAnimator extends MoveToDoubleAnimator {

    private final BasicOrbitView orbitView;
    private final boolean endCenterOnSurface;
    private Point cursorPoint;
    private Position cursorPosition;

    public ZoomToCursorAnimator(BasicOrbitView orbitView, Double end, double smoothing,
                                PropertyAccessor.DoubleAccessor propertyAccessor, boolean endCenterOnSurface, Point cursorPoint) {
        super(end, smoothing, propertyAccessor);
        this.orbitView = orbitView;
        this.endCenterOnSurface = endCenterOnSurface;
        this.cursorPoint = cursorPoint;
        if (cursorPoint != null) {
            this.cursorPosition = orbitView.computePositionFromScreenPoint(cursorPoint.x, cursorPoint.y);
        }
    }

    public void setCursor(Point cursorPoint) {
        if (this.cursorPoint != null &&
                (this.cursorPoint.x != cursorPoint.x
                        || this.cursorPoint.y != cursorPoint.y)) {
            this.cursorPoint = cursorPoint;
            this.cursorPosition = orbitView.computePositionFromScreenPoint(cursorPoint.x, cursorPoint.y);
        }
    }

    @Override
    public Double nextDouble(double interpolant) {
        double newValue = (1 - interpolant) * propertyAccessor.getDouble() + interpolant * this.end;
        if (Math.abs(newValue - propertyAccessor.getDouble()) < minEpsilon) {
            this.stop();
            if (this.endCenterOnSurface) {
                orbitView.setViewOutOfFocus(true);
            }
            return (null);
        }
        return newValue;
    }

    @Override
    protected void setImpl(double interpolant) {
        Double newValue = this.nextDouble(interpolant);
        if (newValue == null) {
            return;
        }

        Position centerPosition = orbitView.getCenterPosition();
        if (cursorPosition != null && centerPosition != null) {
            double y = (centerPosition.latitude.degrees - cursorPosition.latitude.degrees) * (propertyAccessor.getDouble() - newValue) / propertyAccessor.getDouble();
            double x = (centerPosition.longitude.degrees - cursorPosition.longitude.degrees) * (propertyAccessor.getDouble() - newValue) / propertyAccessor.getDouble();
            double z = (centerPosition.elevation - cursorPosition.elevation) * (propertyAccessor.getDouble() - newValue) / propertyAccessor.getDouble();
            double newLatitude = centerPosition.latitude.degrees - y;
            double newLongitude = centerPosition.longitude.degrees - x;
            double newElevation = centerPosition.elevation - z;
            if (newLatitude > -90 && newLatitude < 90
                    && newLongitude > -180 && newLongitude < 180) {
                orbitView.setCenterPosition(Position.fromDegrees(newLatitude, newLongitude, newElevation));
                orbitView.setViewOutOfFocus(true);
            }
        }

        propertyAccessor.setDouble(newValue);
    }
}
