package gov.mil.otc._3dvis.ui.tools.mapsandterrain;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

public class RegionShape extends SurfaceSector {

    private boolean resizeable = false;
    private Position startPosition;
    private Position endPosition;
    private SurfaceSector borderShape;

    protected RegionShape(Sector sector) {
        super(sector);

        // Create the default border shape.
        setBorder(new SurfaceSector(sector));

        // The edges of the region shape should be constant lines of latitude and longitude.
        setPathType(AVKey.LINEAR);
        getBorder().setPathType(AVKey.LINEAR);

        // Setup default interior rendering attributes. Note that the interior rendering attributes are
        // configured so only the SurfaceSector's interior is rendered.
        ShapeAttributes interiorAttrs = new BasicShapeAttributes();
        interiorAttrs.setDrawOutline(false);
        interiorAttrs.setInteriorMaterial(new Material(Color.WHITE));
        interiorAttrs.setInteriorOpacity(0.1);
        setAttributes(interiorAttrs);
        setHighlightAttributes(interiorAttrs);

        // Setup default border rendering attributes. Note that the border rendering attributes are configured
        // so that only the SurfaceSector's outline is rendered.
        ShapeAttributes borderAttrs = new BasicShapeAttributes();
        borderAttrs.setDrawInterior(false);
        borderAttrs.setOutlineMaterial(new Material(Color.RED));
        borderAttrs.setOutlineOpacity(0.7);
        borderAttrs.setOutlineWidth(3);
        getBorder().setAttributes(borderAttrs);
        getBorder().setHighlightAttributes(borderAttrs);
    }

    public Color getInteriorColor() {
        return getAttributes().getInteriorMaterial().getDiffuse();
    }

    public void setInteriorColor(Color color) {
        ShapeAttributes shapeAttributes = getAttributes();
        shapeAttributes.setInteriorMaterial(new Material(color));
        setAttributes(shapeAttributes);
    }

    public Color getBorderColor() {
        return getBorder().getAttributes().getOutlineMaterial().getDiffuse();
    }

    public void setBorderColor(Color color) {
        ShapeAttributes shapeAttributes = getBorder().getAttributes();
        shapeAttributes.setOutlineMaterial(new Material(color));
        getBorder().setAttributes(shapeAttributes);
    }

    public double getInteriorOpacity() {
        return getAttributes().getInteriorOpacity();
    }

    public void setInteriorOpacity(double opacity) {
        ShapeAttributes shapeAttributes = getAttributes();
        shapeAttributes.setInteriorOpacity(opacity);
        setAttributes(shapeAttributes);
    }

    public double getBorderOpacity() {
        return getBorder().getAttributes().getOutlineOpacity();
    }

    public void setBorderOpacity(double opacity) {
        ShapeAttributes shapeAttributes = getBorder().getAttributes();
        shapeAttributes.setOutlineOpacity(opacity);
        getBorder().setAttributes(shapeAttributes);
    }

    public double getBorderWidth() {
        return getBorder().getAttributes().getOutlineWidth();
    }

    public void setBorderWidth(double width) {
        ShapeAttributes shapeAttributes = getBorder().getAttributes();
        shapeAttributes.setOutlineWidth(width);
        getBorder().setAttributes(shapeAttributes);
    }

    public void setSector(Sector sector) {
        super.setSector(sector);
        getBorder().setSector(sector);
    }

    protected boolean isResizeable() {
        return resizeable;
    }

    protected void setResizeable(boolean resizeable) {
        this.resizeable = resizeable;
    }

    protected Position getStartPosition() {
        return startPosition;
    }

    protected void setStartPosition(Position startPosition) {
        this.startPosition = startPosition;
    }

    protected Position getEndPosition() {
        return endPosition;
    }

    protected void setEndPosition(Position endPosition) {
        this.endPosition = endPosition;
    }

    protected SurfaceSector getBorder() {
        return borderShape;
    }

    protected void setBorder(SurfaceSector shape) {
        if (shape == null) {
            String message = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        borderShape = shape;
    }

    protected boolean hasSelection() {
        return getStartPosition() != null && getEndPosition() != null;
    }

    protected void clear() {
        setStartPosition(null);
        setEndPosition(null);
        setSector(Sector.EMPTY_SECTOR);
    }

    public void preRender(DrawContext dc) {
        // This is called twice: once during normal rendering, then again during ordered surface rendering. During
        // normal renering we pre-render both the interior and border shapes. During ordered surface rendering, both
        // shapes are already added to the DrawContext and both will be individually processed. Therefore we just
        // call our superclass behavior
        if (dc.isOrderedRenderingMode()) {
            super.preRender(dc);
            return;
        }

        doPreRender(dc);
    }

    @Override
    public void render(DrawContext dc) {
        if (dc.isPickingMode() && isResizeable())
            return;

        // This is called twice: once during normal rendering, then again during ordered surface rendering. During
        // normal renering we render both the interior and border shapes. During ordered surface rendering, both
        // shapes are already added to the DrawContext and both will be individually processed. Therefore we just
        // call our superclass behavior
        if (dc.isOrderedRenderingMode()) {
            super.render(dc);
            return;
        }

        if (!isResizeable()) {
            if (hasSelection()) {
                doRender(dc);
            }
            return;
        }

        PickedObjectList pos = dc.getPickedObjects();
        PickedObject terrainObject = pos != null ? pos.getTerrainObject() : null;

        if (terrainObject == null)
            return;

        if (getStartPosition() != null) {
            Position end = terrainObject.getPosition();
            if (!getStartPosition().equals(end)) {
                setEndPosition(end);
                setSector(Sector.boundingSector(getStartPosition(), getEndPosition()));
                doRender(dc);
            }
        } else {
            setStartPosition(pos.getTerrainObject().getPosition());
        }
    }

    protected void doPreRender(DrawContext dc) {
        doPreRenderInterior(dc);
        doPreRenderBorder(dc);
    }

    protected void doPreRenderInterior(DrawContext dc) {
        super.preRender(dc);
    }

    protected void doPreRenderBorder(DrawContext dc) {
        getBorder().preRender(dc);
    }

    protected void doRender(DrawContext dc) {
        doRenderInterior(dc);
        doRenderBorder(dc);
    }

    protected void doRenderInterior(DrawContext dc) {
        super.render(dc);
    }

    protected void doRenderBorder(DrawContext dc) {
        getBorder().render(dc);
    }
}
