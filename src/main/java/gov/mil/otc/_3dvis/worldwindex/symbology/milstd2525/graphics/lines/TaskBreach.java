package gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.symbology.TacticalGraphicUtil;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.Arrays;
import java.util.List;

public class TaskBreach extends AbstractTask {

    boolean isBreach = true;

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this
     * class supports.
     */
    public static List<String> getSupportedGraphics() {
        return List.of(TacGrpSidc.TSK_BRH);
    }

    public TaskBreach(String symbolCode) {
        super(symbolCode);
        this.setValue(AVKey.ROLLOVER_TEXT, "Task BREACH");
        setArrowAngle(Angle.fromDegrees(90.0));
    }

    //                 
    //  4 _______________\ 1
    //   |                \
    //   |               
    //   |               
    //  3|                 6
    //   |               
    //   |               
    //   |_______________ / 2
    //  5                /
    //    
    //    3 paths, 1 line path and 2 ends
    @Override
    protected void createShapes(DrawContext dc) {
        if (isJbcp) {
            createShapesJbcp(dc);
        } else {
            paths = new Path[3];

            Globe globe = dc.getGlobe();

            // find points needed for computations
            Position center = new Position(LatLon.interpolate(0.5, this.position1, this.position2), 0);
            Vec4 p1 = globe.computePointFromPosition(this.position1);
            Vec4 p2 = globe.computePointFromPosition(this.position2);
            Vec4 p3 = globe.computePointFromPosition(this.position3);
            Vec4 p6 = globe.computePointFromPosition(center);

            // Find vector in the direction of the ends
            Vec4 dir = p3.subtract3(p6);
            Position pos4 = globe.computePositionFromPoint(p1.add3(dir));
            Position pos5 = globe.computePositionFromPoint(p2.add3(dir));

            // set the path for the lines
            this.paths[0] = this.createPath(Arrays.asList(this.position1, pos4, pos5, this.position2));

            // set the arrowhead paths
            double arrowheadLength = dir.getLength3() * this.getArrowLength() * .5;
            this.paths[1] = createPath(this.computeEndPositions(dc, p1, dir, arrowheadLength, isBreach));
            this.paths[2] = createPath(this.computeEndPositions(dc, p2, dir, arrowheadLength, !isBreach));
        }
    }

    protected void createShapesJbcp(DrawContext dc) {
        paths = new Path[3];

        Globe globe = dc.getGlobe();

        // find points needed for computations
        Vec4 p1 = globe.computePointFromPosition(this.position1);
        Vec4 p2 = globe.computePointFromPosition(this.position2);
        Vec4 p3 = globe.computePointFromPosition(this.position3);

        // Find the direction vectors so that the perpendicular direction can be determined
        Vec4 v21 = p1.subtract3(p2);
        Vec4 v23 = p3.subtract3(p2);

        // project the third point onto the baseline and use that vector as the offset
        Vec4 offset = (v23).perpendicularTo3(v21);

        Position pos4 = globe.computePositionFromPoint(p1.add3(offset));
        Position pos5 = globe.computePositionFromPoint(p2.add3(offset));

        // set the path for the lines
        this.paths[0] = this.createPath(Arrays.asList(this.position1, pos4, pos5, this.position2));

        // set the arrowhead paths
        double arrowheadLength = offset.getLength3() * this.getArrowLength() * .5;
        this.paths[1] = createPath(this.computeEndPositions(dc, p1, offset, arrowheadLength, isBreach));
        this.paths[2] = createPath(this.computeEndPositions(dc, p2, offset, arrowheadLength, !isBreach));
    }

    private List<Position> computeEndPositions(DrawContext dc, Vec4 tip, Vec4 dir, double length, boolean outward) {
        Globe globe = dc.getGlobe();
//                  c
//                /
//  _____________/ tip            
//              /
//             /a

        // Compute the length of the arrowhead
        double arrowHalfWidth = length * this.getArrowAngle().tanHalfAngle();
        dir = dir.normalize3();
        // Find the point at the base of the 
        Vec4 arrowBase = tip.add3(dir.multiply3(length));
        Vec4 normal = globe.computeSurfaceNormalAtPoint(arrowBase);
        // Compute a vector perpendicular to the segment and the normal vector
        Vec4 perpendicular = dir.cross3(normal);
        perpendicular = perpendicular.normalize3().multiply3(arrowHalfWidth);
        // Find points A and C
        Vec4 ptA = outward ? arrowBase.add3(perpendicular) : arrowBase.subtract3(perpendicular);
        Vec4 ptC = tip.add3(tip.subtract3(ptA));
        return TacticalGraphicUtil.asPositionList(globe, ptA, ptC);
    }

}
