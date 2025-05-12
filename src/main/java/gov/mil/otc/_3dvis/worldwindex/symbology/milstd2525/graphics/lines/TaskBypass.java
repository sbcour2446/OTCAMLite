/*
 *   Task BYPASS from MIL-STD 2525B
 *   2.X.1.3  (G*TPY-----****X)
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
 * @author Chris
 */
public class TaskBypass extends AbstractTask {

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this
     * class supports.
     */
    public static List<String> getSupportedGraphics() {
        return List.of(TacGrpSidc.TSK_BYS);
    }

    public TaskBypass(String symbolCode) {
        super(symbolCode);
        this.setValue(AVKey.ROLLOVER_TEXT, "Task BYPASS");
    }

    //                 
    //  4 _______________\ 1
    //   |               /
    //   |               
    //   |               
    //  3|                 6
    //   |               
    //   |               
    //   |_______________\ 2
    //  5                /
    //    
    //    3 paths, 1 line path and 2 arrows
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

            // Find vector in the direction of the arrows
            Vec4 dir = p3.subtract3(p6);
            Position pos4 = globe.computePositionFromPoint(p1.add3(dir));
            Position pos5 = globe.computePositionFromPoint(p2.add3(dir));

            // set the path for the lines
            this.paths[0] = this.createPath(Arrays.asList(this.position1, pos4, pos5, this.position2));

            // set the arrowhead paths
            double arrowheadLength = dir.getLength3() * this.getArrowLength();
            this.paths[1] = createPath(this.computeArrowheadPositions(dc, p1, dir, arrowheadLength));
            this.paths[2] = createPath(this.computeArrowheadPositions(dc, p2, dir, arrowheadLength));

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
        this.paths[1] = createPath(this.computeArrowheadPositions(dc, p1, offset, arrowheadLength));
        this.paths[2] = createPath(this.computeArrowheadPositions(dc, p2, offset, arrowheadLength));
    }

}
