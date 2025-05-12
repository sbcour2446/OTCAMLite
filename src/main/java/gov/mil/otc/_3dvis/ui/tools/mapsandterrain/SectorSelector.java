package gov.mil.otc._3dvis.ui.tools.mapsandterrain;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.WWController;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceSector;
import gov.nasa.worldwind.util.Logging;
import javafx.scene.Cursor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class SectorSelector implements SelectListener, MouseListener, MouseMotionListener, RenderingListener {

    private enum Operation {
        NONE, MOVING, SIZING
    }

    private static final int NONE = 0;
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 4;
    private static final int WEST = 8;
    private static final int NORTHWEST = NORTH + WEST;
    private static final int NORTHEAST = NORTH + EAST;
    private static final int SOUTHWEST = SOUTH + WEST;
    private static final int SOUTHEAST = SOUTH + EAST;
    private static final double EDGE_FACTOR = 0.10;

    private final RegionShape regionShape = new RegionShape(Sector.EMPTY_SECTOR);
    private final RenderableLayer renderableLayer = new RenderableLayer();
    private boolean armed = false;
    private Operation operation = Operation.NONE;
    private int side = NONE;
    private Position previousPosition = null;
    private Sector previousSector = null;

    public static SectorSelector createSelector() {
        return new SectorSelector();
    }

    private SectorSelector() {
    }

    public void enable() {
        armed = true;

        renderableLayer.addRenderable(regionShape);
        WWController.addLayer(renderableLayer);
        WWController.getWorldWindowPanel().addRenderingListener(this);
        WWController.getWorldWindowPanel().addSelectListener(this);
        WWController.getWorldWindowPanel().getInputHandler().addMouseListener(this);
        WWController.getWorldWindowPanel().getInputHandler().addMouseMotionListener(this);
        setCursor(Cursor.CROSSHAIR);
    }

    public void disable() {
        setCursor(null);
        regionShape.clear();
        renderableLayer.removeRenderable(regionShape);
        WWController.removeLayer(renderableLayer);
        WWController.getWorldWindowPanel().removeRenderingListener(this);
        WWController.getWorldWindowPanel().removeSelectListener(this);
        WWController.getWorldWindowPanel().getInputHandler().removeMouseListener(this);
        WWController.getWorldWindowPanel().getInputHandler().removeMouseMotionListener(this);
    }

    @Override
    public void stageChanged(RenderingEvent event) {
        if (!event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP))
            return;

        // We notify of changes during this rendering stage because the sector is updated within the region shape's
        // render method.

        notifySectorChanged();
    }

    @Override
    public void selected(SelectEvent event) {
        if (event == null) {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().log(java.util.logging.Level.FINE, msg);
            throw new IllegalArgumentException(msg);
        }

        if (operation == Operation.NONE && event.getTopObject() != null
                && !(event.getTopPickedObject().getParentLayer() == renderableLayer)) {
            setCursor(null);
            return;
        }

        if (event.getEventAction().equals(SelectEvent.LEFT_PRESS)) {
            previousPosition = WWController.getWorldWindowPanel().getCurrentPosition();
        } else if (event.getEventAction().equals(SelectEvent.DRAG)) {
            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null) {
                return;
            }

            RegionShape dragObject = regionShape;

            if (operation == Operation.SIZING) {
                Sector newSector = this.resizeShape(dragObject, side);
                if (newSector != null) {
                    dragObject.setSector(newSector);
                }
                event.consume();
            } else {
                side = determineAdjustmentSide(dragObject);

                if (side == NONE || operation == Operation.MOVING) {
                    operation = Operation.MOVING;
                    dragWholeShape(dragEvent, dragObject);
                } else {
                    Sector newSector = resizeShape(dragObject, side);
                    if (newSector != null) {
                        dragObject.setSector(newSector);
                    }
                    operation = Operation.SIZING;
                }
                event.consume();
            }

            previousPosition = WWController.getWorldWindowPanel().getCurrentPosition();
            notifySectorChanged();
        } else if (event.getEventAction().equals(SelectEvent.DRAG_END)) {
            operation = Operation.NONE;
            previousPosition = null;
        } else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && operation == Operation.NONE) {
            if (event.getTopObject() == null || event.getTopPickedObject().isTerrain()) {
                setCursor(null);
                return;
            }

            if (!(event.getTopObject() instanceof Movable)) {
                return;
            }

            setCursor(determineAdjustmentSide((Movable) event.getTopObject()));
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (MouseEvent.BUTTON1_DOWN_MASK != e.getModifiersEx()) {
            return;
        }

        if (!armed) {
            return;
        }

        regionShape.setResizeable(true);
        regionShape.setStartPosition(null);
        armed = false;

        e.consume();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (MouseEvent.BUTTON1 != e.getButton()) return;

        if (regionShape.isResizeable()) setCursor(null);

        regionShape.setResizeable(false);

        e.consume(); // prevent view operations

//        firePropertyChange(SECTOR_PROPERTY, this.previousSector, null);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (MouseEvent.BUTTON1_DOWN_MASK != e.getModifiersEx())
            return;

        if (regionShape.isResizeable())
            e.consume(); // prevent view operations
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    private void notifySectorChanged() {
        if (regionShape.hasSelection() && this.getSector() != null && !this.getSector().equals(this.previousSector)) {
            previousSector = getSector();
        }
    }

    private Sector getSector() {
        return regionShape.hasSelection() ? regionShape.getSector() : null;
        // TODO: Determine how to handle date-line spanning sectors.
    }

    protected void setCursor(int sideName) {
        Cursor cursor = switch (sideName) {
            case NONE -> Cursor.OPEN_HAND;
            case NORTH -> Cursor.N_RESIZE;
            case SOUTH -> Cursor.S_RESIZE;
            case EAST -> Cursor.E_RESIZE;
            case WEST -> Cursor.W_RESIZE;
            case NORTHWEST -> Cursor.NW_RESIZE;
            case NORTHEAST -> Cursor.NE_RESIZE;
            case SOUTHWEST -> Cursor.SW_RESIZE;
            case SOUTHEAST -> Cursor.SE_RESIZE;
            default -> null;
        };

        setCursor(cursor);
    }

    private void setCursor(Cursor cursor) {
        MainApplication.getInstance().getSwingNode().setCursor(cursor);
    }

    private int determineAdjustmentSide(Movable dragObject) {
        if (dragObject instanceof SurfaceSector) {
            SurfaceSector quad = (SurfaceSector) dragObject;
            Sector s = quad.getSector(); // TODO: go over all sectors
            Position p = WWController.getWorldWindowPanel().getCurrentPosition();

            if (p == null) {
                return NONE;
            }

            double dN = Math.abs(s.getMaxLatitude().subtract(p.getLatitude()).degrees);
            double dS = Math.abs(s.getMinLatitude().subtract(p.getLatitude()).degrees);
            double dW = Math.abs(s.getMinLongitude().subtract(p.getLongitude()).degrees);
            double dE = Math.abs(s.getMaxLongitude().subtract(p.getLongitude()).degrees);

            double sLat = EDGE_FACTOR * s.getDeltaLatDegrees();
            double sLon = EDGE_FACTOR * s.getDeltaLonDegrees();

            if (dN < sLat && dW < sLon) return NORTHWEST;
            if (dN < sLat && dE < sLon) return NORTHEAST;
            if (dS < sLat && dW < sLon) return SOUTHWEST;
            if (dS < sLat && dE < sLon) return SOUTHEAST;
            if (dN < sLat) return NORTH;
            if (dS < sLat) return SOUTH;
            if (dW < sLon) return WEST;
            if (dE < sLon) return EAST;
        }

        return NONE;
    }

    private Sector resizeShape(Movable dragObject, int side) {
        if (dragObject instanceof SurfaceSector) {
            SurfaceSector quad = (SurfaceSector) dragObject;
            Sector s = quad.getSector(); // TODO: go over all sectors
            Position p = WWController.getWorldWindowPanel().getCurrentPosition();

            if (p == null || previousPosition == null) {
                return null;
            }

            Angle dLat = p.getLatitude().subtract(previousPosition.getLatitude());
            Angle dLon = p.getLongitude().subtract(previousPosition.getLongitude());

            Angle newMinLat = s.getMinLatitude();
            Angle newMinLon = s.getMinLongitude();
            Angle newMaxLat = s.getMaxLatitude();
            Angle newMaxLon = s.getMaxLongitude();

            if (side == NORTH) {
                newMaxLat = s.getMaxLatitude().add(dLat);
            } else if (side == SOUTH) {
                newMinLat = s.getMinLatitude().add(dLat);
            } else if (side == EAST) {
                newMaxLon = s.getMaxLongitude().add(dLon);
            } else if (side == WEST) {
                newMinLon = s.getMinLongitude().add(dLon);
            } else if (side == NORTHWEST) {
                newMaxLat = s.getMaxLatitude().add(dLat);
                newMinLon = s.getMinLongitude().add(dLon);
            } else if (side == NORTHEAST) {
                newMaxLat = s.getMaxLatitude().add(dLat);
                newMaxLon = s.getMaxLongitude().add(dLon);
            } else if (side == SOUTHWEST) {
                newMinLat = s.getMinLatitude().add(dLat);
                newMinLon = s.getMinLongitude().add(dLon);
            } else if (side == SOUTHEAST) {
                newMinLat = s.getMinLatitude().add(dLat);
                newMaxLon = s.getMaxLongitude().add(dLon);
            }

            return new Sector(newMinLat, newMaxLat, newMinLon, newMaxLon);
        }

        return null;
    }

    protected void dragWholeShape(DragSelectEvent dragEvent, Movable dragObject) {
        View view = WWController.getView();
        if (view == null) {
            return;
        }

        EllipsoidalGlobe globe = (EllipsoidalGlobe) WWController.getWorldWindowPanel().getModel().getGlobe();
        if (globe == null) {
            return;
        }

        // Compute ref-point position in screen coordinates.
        Position refPos = dragObject.getReferencePosition();
        if (refPos == null) {
            return;
        }

        Vec4 refPoint = globe.computePointFromPosition(refPos);
        Vec4 screenRefPoint = view.project(refPoint);

        // Compute screen-coord delta since last event.
        int dx = dragEvent.getPickPoint().x - dragEvent.getPreviousPickPoint().x;
        int dy = dragEvent.getPickPoint().y - dragEvent.getPreviousPickPoint().y;

        // Find intersection of screen coord ref-point with globe.
        double x = screenRefPoint.x + dx;
        double y = dragEvent.getMouseEvent().getComponent().getSize().height - screenRefPoint.y + dy - 1;
        Line ray = view.computeRayFromScreenPoint(x, y);
        Intersection[] inters = globe.intersect(ray, refPos.getElevation());

        if (inters != null) {
            // Intersection with globe. Move reference point to the intersection point.
            Position p = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
            dragObject.moveTo(p);
        }
    }
}
