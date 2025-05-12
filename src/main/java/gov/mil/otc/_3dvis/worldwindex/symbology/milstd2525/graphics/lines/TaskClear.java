/*
 *   Task CLEAR from MIL-STD 2525B
 *   2.X.1.5  (G*TPX-----****X)
 *
 *  GaN Corp. for Army OTC  (3DVis)
 *
 */
package gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.Arrays;
import java.util.List;

/**
 * @author Chris (GaN Corp.)
 */
public class TaskClear extends AbstractTask {

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this
     * class supports.
     */
    public static List<String> getSupportedGraphics() {
        return List.of(TacGrpSidc.TSK_CLR);
    }

    public TaskClear(String sidc) {
        super(sidc);
        this.setValue(AVKey.ROLLOVER_TEXT, "Task CLEAR");
    }

    //    assume arrows are at .2, .5, and .8
    //    arrow size is .1 for each wing
    //                    |p1
    //       ___________\ |
    //                  / |
    //                    |
    //     p3___________\ |
    //                  / |
    //                    |
    //       ___________\ |
    //                  / |
    //                    |p2
    //    
    //  so there will be 7 paths.  4 lines and 3 arrowheads

    /**
     * Create the list of positions that describe the arrow.
     *
     * @param dc Current draw context.
     */
    protected void createShapes(DrawContext dc) {
        if (isJbcp) {
            createJbcpShapes(dc);
        } else {

            this.paths = new Path[7];

            // Create a path for the line part of the arrow
            this.paths[0] = this.createPath(Arrays.asList(this.position1, this.position2));
            Position a2Tip = new Position(LatLon.interpolate(0.5, this.position1, this.position2), 0);
            this.paths[1] = this.createPath(Arrays.asList(a2Tip, this.position3));
            Position a1Tip = new Position(LatLon.interpolate(0.2, this.position1, this.position2), 0);
            Position a3Tip = new Position(LatLon.interpolate(0.8, this.position1, this.position2), 0);

            // Create the arrow tip points
            Globe globe = dc.getGlobe();
            Vec4 p1 = globe.computePointFromPosition(this.position3);
            Vec4 p2 = globe.computePointFromPosition(a2Tip);
            Vec4 a1TipPoint = globe.computePointFromPosition(a1Tip);
            Vec4 a3TipPoint = globe.computePointFromPosition(a3Tip);

            // Find vector in the direction of the mid(2) arrow
            Vec4 v21 = p1.subtract3(p2);

            // comput the tail points for arrows 1 and 3 
            Position a1Tail = globe.computePositionFromPoint(a1TipPoint.add3(v21));
            Position a3Tail = globe.computePositionFromPoint(a3TipPoint.add3(v21));
            this.paths[2] = this.createPath(Arrays.asList(a1Tip, a1Tail));
            this.paths[3] = this.createPath(Arrays.asList(a3Tip, a3Tail));

            // now compute and add the arrowheads to the path list  
            double arrowheadLength = v21.getLength3() * this.getArrowLength();
            this.paths[4] = createPath(this.computeArrowheadPositions(dc, p2, v21, arrowheadLength));
            this.paths[5] = createPath(this.computeArrowheadPositions(dc, a1TipPoint, v21, arrowheadLength));
            this.paths[6] = createPath(this.computeArrowheadPositions(dc, a3TipPoint, v21, arrowheadLength));
        }
    }

    /* -------------- JBCP ------------------------
    //    assume arrows are at .2, .5, and .8
    //    arrow size is .1 for each wing
    //                    |p1
    //       ___________\ |
    //                  / |
    //                    |
    //       ___________\ |
    //                  / |
    //                    |
    //       ___________\ |
    //                  / |
    //     p3             |p2
    //    
    //  so there will be 7 paths.  4 lines and 3 arrowheads
     */

    /**
     * Create the list of positions that describe the arrow.
     *
     * @param dc Current draw context.
     */
    protected void createJbcpShapes(DrawContext dc) {
        this.paths = new Path[7];

        // Create a path for the line part of the arrow
        this.paths[0] = this.createPath(Arrays.asList(this.position1, this.position2));
        Position a1Tip = new Position(LatLon.interpolate(0.15, this.position1, this.position2), 0);
        Position a2Tip = new Position(LatLon.interpolate(0.50, this.position1, this.position2), 0);
        Position a3Tip = new Position(LatLon.interpolate(0.85, this.position1, this.position2), 0);

        // Create the arrow tip points
        Globe globe = dc.getGlobe();
        Vec4 p1 = globe.computePointFromPosition(this.position1);
        Vec4 p2 = globe.computePointFromPosition(this.position2);
        Vec4 p3 = globe.computePointFromPosition(this.position3);

        // Find the direction vectors so that the perpendicular direction can be determined
        Vec4 v21 = p1.subtract3(p2);
        Vec4 v23 = p3.subtract3(p2);

        // project the third point onto the baseline and use that vector as the offset
        Vec4 offset = (v23).perpendicularTo3(v21);

        Vec4 a1TipPoint = globe.computePointFromPosition(a1Tip);
        Vec4 a2TipPoint = globe.computePointFromPosition(a2Tip);
        Vec4 a3TipPoint = globe.computePointFromPosition(a3Tip);

        // comput the tail points for arrows 1 and 3 
        Position a1Tail = globe.computePositionFromPoint(a1TipPoint.add3(offset));
        Position a2Tail = globe.computePositionFromPoint(a2TipPoint.add3(offset));
        Position a3Tail = globe.computePositionFromPoint(a3TipPoint.add3(offset));
        this.paths[1] = this.createPath(Arrays.asList(a1Tip, a1Tail));
        this.paths[2] = this.createPath(Arrays.asList(a2Tip, a2Tail));
        this.paths[3] = this.createPath(Arrays.asList(a3Tip, a3Tail));

        // now compute and add the arrowheads to the path list  
        double arrowheadLength = p1.subtract3(p2).getLength3() * this.getArrowLength();
        this.paths[4] = createPath(this.computeArrowheadPositions(dc, a1TipPoint, offset, arrowheadLength));
        this.paths[5] = createPath(this.computeArrowheadPositions(dc, a2TipPoint, offset, arrowheadLength));
        this.paths[6] = createPath(this.computeArrowheadPositions(dc, a3TipPoint, offset, arrowheadLength));
    }

}
