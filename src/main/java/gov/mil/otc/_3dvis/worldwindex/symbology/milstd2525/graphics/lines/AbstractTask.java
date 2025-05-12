package gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.symbology.TacticalGraphicUtil;
import gov.nasa.worldwind.symbology.milstd2525.AbstractMilStd2525TacticalGraphic;
import gov.nasa.worldwind.util.Logging;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class AbstractTask extends AbstractMilStd2525TacticalGraphic {

    /**
     * Default length of the arrowhead, as a fraction of the total line length.
     */
    public static final double DEFAULT_ARROWHEAD_LENGTH = 0.1;
    /**
     * Default angle of the arrowhead.
     */
    public static final Angle DEFAULT_ARROWHEAD_ANGLE = Angle.fromDegrees(60.0);

    protected boolean isJbcp = true;

    /**
     * Length of the arrowhead from base to tip, as a fraction of the total line
     * length.
     */
    protected Angle arrowAngle = DEFAULT_ARROWHEAD_ANGLE;
    /**
     * Angle of the arrowhead.
     */
    protected double arrowLength = DEFAULT_ARROWHEAD_LENGTH;
    /**
     * First control point.
     */
    protected Position position1;
    /**
     * Second control point.
     */
    protected Position position2;
    /**
     * Third control point.
     */
    protected Position position3;
    /**
     * Path used to render the line.
     */
    protected Path[] paths;

    protected AbstractTask(String symbolCode) {
        super(symbolCode);
    }

    public Angle getArrowAngle() {
        return arrowAngle;
    }

    public void setArrowAngle(Angle arrowAngle) {
        if (arrowAngle == null) {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (arrowAngle.degrees <= 0) {
            String msg = Logging.getMessage("generic.AngleOutOfRange");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.arrowAngle = arrowAngle;
    }

    public double getArrowLength() {
        return arrowLength;
    }

    /**
     * @param positions Control points that orient the graphic. Must provide at
     *                  least three points.
     */
    public void setPositions(Iterable<? extends Position> positions) {
        if (positions == null) {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        try {
            Iterator<? extends Position> iterator = positions.iterator();
            this.position1 = iterator.next();
            this.position2 = iterator.next();
            this.position3 = iterator.next();
        } catch (NoSuchElementException e) {
            String message = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.paths = null; // Need to recompute path for the new control points
    }

    public Iterable<? extends Position> getPositions() {
        return Arrays.asList(this.position1, this.position2, this.position3);
    }

    @Override
    protected void doRenderGraphic(DrawContext dc) {
        for (Path path : this.paths) {
            path.render(dc);
        }
    }

    @Override
    protected void applyDelegateOwner(Object owner) {
        if (this.paths == null) {
            return;
        }
        for (Path path : this.paths) {
            path.setDelegateOwner(owner);
        }
    }

    @Override
    public Position getReferencePosition() {
        return this.position1;
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
    protected abstract void createShapes(DrawContext dc);

    @Override
    protected void computeGeometry(DrawContext dc) {
        if (this.paths == null) {
            this.createShapes(dc);
        }
        super.computeGeometry(dc);
    }

    /**
     * Determine the positions that make up the arrowhead.
     *
     * @param dc     Current draw context.
     * @param tip    Point at the tip of the arrow head.
     * @param dir    Vector in the direction of the arrow head.
     * @param length Length of the arrowhead from base to tip.
     * @return Positions that define the arrowhead.
     */
    protected List<Position> computeArrowheadPositions(DrawContext dc, Vec4 tip, Vec4 dir, double length) {
        Globe globe = dc.getGlobe();
        // The arrowhead is drawn outlined for the Main Attack graphic, and as a single line for the Supporting Attack
        // graphic. When outlined, we need points A, B, C, D, E, and F. When not outlined the arrowhead is just points
        // A, B, and C.
        //
        //          F        _
        //         |\         |
        //        A\ \        | 1/2 width
        // ________B\ \ E    _|
        // Pt. 1    / /
        //        C/ /
        //         |/
        //         D
        //         | |
        //      Length
        @SuppressWarnings(value = {"UnnecessaryLocalVariable"})
        Vec4 ptB = tip;
        // Compute the length of the arrowhead
        double arrowHalfWidth = length * this.getArrowAngle().tanHalfAngle();
        dir = dir.normalize3();
        // Find the point at the base of the arrowhead
        Vec4 arrowBase = ptB.add3(dir.multiply3(length));
        Vec4 normal = globe.computeSurfaceNormalAtPoint(arrowBase);
        // Compute a vector perpendicular to the segment and the normal vector
        Vec4 perpendicular = dir.cross3(normal);
        perpendicular = perpendicular.normalize3().multiply3(arrowHalfWidth);
        // Find points A and C
        Vec4 ptA = arrowBase.add3(perpendicular);
        Vec4 ptC = arrowBase.subtract3(perpendicular);
        List<Position> positions;
        positions = TacticalGraphicUtil.asPositionList(globe, ptA, ptB, ptC);
        return positions;
    }

    /**
     * Create and configure the Path used to render this graphic.
     *
     * @param positions Positions that define the path.
     * @return New path configured with defaults appropriate for this type of
     * graphic.
     */
    protected Path createPath(List<Position> positions) {
        Path path = new Path(positions);
        path.setSurfacePath(true);
        path.setPathType(AVKey.GREAT_CIRCLE);
        path.setDelegateOwner(this.getActiveDelegateOwner());
        path.setAttributes(this.getActiveShapeAttributes());
        path.getAttributes().setOutlineWidth(3);
        return path;
    }

}
