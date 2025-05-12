package gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.symbology.milstd2525.graphics.lines.AbstractAxisArrow;
import gov.nasa.worldwind.util.Logging;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractAxisArrowEx extends AbstractAxisArrow {

    double arrowSize = .2;
    boolean isJbcp = true;

    protected AbstractAxisArrowEx(String sidc) {
        super(sidc);
        finalPointWidthOfRoute = false;
    }

    public Position modifyFinalPosition(Globe globe, Position pos1, Position pos2, Position posN) {
        setFinalPointWidthOfRoute(false);
        // the final point needs to be adjusted to support the JBCP methodolgy
        //
        Vec4 p1 = globe.computePointFromPosition(pos1);
        Vec4 p2 = globe.computePointFromPosition(pos2);
        Vec4 pN = globe.computePointFromPosition(posN);

        Vec4 v21 = p1.subtract3(p2);
        Vec4 v2N = pN.subtract3(p2);

        // project the third point onto the baseline and use that vector as the offset
        Vec4 offset = (v2N).perpendicularTo3(v21);

        Vec4 pFinal = globe.computePointFromPosition(Position.interpolate(arrowSize, pos1, pos2)).add3(offset);

        return globe.computePositionFromPoint(pFinal);
    }

    @Override
    protected double createArrowHeadPositions(List<Position> leftPositions, List<Position> rightPositions,
                                              List<Position> arrowHeadPositions, Globe globe) {
        Iterator<? extends Position> iterator = this.positions.iterator();

        Position pos1 = iterator.next();
        Position pos2 = iterator.next();

        Position posN = null;
        while (iterator.hasNext()) {
            posN = iterator.next();
        }

        if (posN == null) {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 pt1 = globe.computePointFromLocation(pos1);
        Vec4 pt2 = globe.computePointFromLocation(pos2);

        if (isJbcp) {
            posN = modifyFinalPosition(globe, pos1, pos2, posN); // modified for 3DVis by GaN for OTC
        }

        Vec4 pointN = globe.computePointFromLocation(posN);

        // Compute a vector that points from Pt. 1 toward Pt. 2
        Vec4 v12 = pt1.subtract3(pt2).normalize3();

        Vec4 point1Prime = pt1.add3(v12.multiply3(pointN.subtract3(pt1).dot3(v12)));

        // If the final control point determines the width of the route (not the width of the arrowhead) then compute
        // Point N from the final control point.
        if (this.isFinalPointWidthOfRoute()) {
            pointN = pointN.add3(pointN.subtract3(point1Prime));
            posN = globe.computePositionFromPoint(pointN);
        }

        Vec4 pointNPrime = point1Prime.subtract3(pointN.subtract3(point1Prime));

        Vec4 normal = globe.computeSurfaceNormalAtPoint(point1Prime);

        // Compute the distance from the center line to the left and right lines.
        double halfWidth = pointN.subtract3(point1Prime).getLength3() / 2;

        Vec4 offset = normal.cross3(v12).normalize3().multiply3(halfWidth);

        Vec4 pLeft = point1Prime.add3(offset);
        Vec4 pRight = point1Prime.subtract3(offset);

        Position posLeft = globe.computePositionFromPoint(pLeft);
        Position posRight = globe.computePositionFromPoint(pRight);

        leftPositions.add(posLeft);
        rightPositions.add(posRight);

        Position posNPrime = globe.computePositionFromPoint(pointNPrime);

        // Compute the scalar triple product of the vector 12, the normal vector, and a vector from the center line
        // toward Pt. N to determine if the offset points to the left or the right of the control line.
        double tripleProduct = offset.dot3(pointN.subtract3(point1Prime));
        if (tripleProduct < 0) {
            Position tmp = posN; // Swap N and N'
            posN = posNPrime;
            posNPrime = tmp;
        }

        arrowHeadPositions.add(posN);
        arrowHeadPositions.add(pos1);
        arrowHeadPositions.add(posNPrime);

        return halfWidth;
    }
}
