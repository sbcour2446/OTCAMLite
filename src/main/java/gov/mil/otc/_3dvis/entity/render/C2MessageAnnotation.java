package gov.mil.otc._3dvis.entity.render;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.drag.DragContext;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

public class C2MessageAnnotation extends GlobeAnnotation implements SelectListener {

    private static final int WIDTH = 450;
    private final BasicDragger basicDragger;
    private final IEntity entity;
    private final C2MessageEvent c2MessageEvent;
    private Point lastScreenPoint = null;

    public C2MessageAnnotation(IEntity entity, C2MessageEvent c2MessageEvent) {
        super("", entity.getPosition());
        this.entity = entity;
        this.c2MessageEvent = c2MessageEvent;
        this.basicDragger = new BasicDragger(WWController.getWorldWindowPanel());
        WWController.getWorldWindowPanel().addSelectListener(this);
        initialize();
    }

    private void initialize() {
        attributes.setFont(Common.FONT_MEDIUM_BOLD);
        attributes.setBorderColor(Color.BLUE);
        attributes.setBackgroundColor(new Color(0, 100, 255, 150));
        attributes.setTextColor(Color.WHITE);
        attributes.setCornerRadius(0);
        attributes.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        attributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
        if (entity.isMunition() || entity.isAircraft()) {
            setAltitudeMode(WorldWind.ABSOLUTE);
        } else {
            setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        }
        attributes.setDrawOffset(new Point(50, 100));
        attributes.setSize(new Dimension(WIDTH, 0));
        attributes.setInsets(new Insets(10, 10, 10, 10));
        setDragEnabled(true);
        setPickEnabled(true);

        text = c2MessageEvent.getMessage();
    }

    @Override
    public void selected(SelectEvent event) {
        if (event.getEventAction().equals(SelectEvent.DRAG) && event.getTopObject() == this) {
            basicDragger.selected(event);
        }
    }

    @Override
    public void drag(DragContext dragContext) {
        int changeX = dragContext.getPoint().x - dragContext.getPreviousPoint().x;
        int changeY = dragContext.getPoint().y - dragContext.getPreviousPoint().y;

        AnnotationAttributes attrs = getAttributes();
        int newX = attrs.getDrawOffset().x + changeX;
        int newY = attrs.getDrawOffset().y - changeY;
        attrs.setDrawOffset(new Point(newX, newY));
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        if (dc.isPickingMode() && this.getPickSupport() == null) {
            return;
        }

        Vec4 point = this.getAnnotationDrawPoint(dc);
        if (point == null) {
            return;
        }

        if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(point) < 0) {
            return;
        }

        Vec4 screenPoint = dc.getView().project(point);
        if (screenPoint == null) {
            return;
        }

        Dimension size = this.getPreferredSize(dc);

        if (lastScreenPoint == null) {
            lastScreenPoint = new Point((int) screenPoint.x, (int) screenPoint.y);
        } else if (dc.getView().equals(WWController.getWorldWindowPanel().getView())) {
            Point newScreenPoint = new Point((int) screenPoint.x, (int) screenPoint.y);
            int changeX = newScreenPoint.x - lastScreenPoint.x;
            int changeY = newScreenPoint.y - lastScreenPoint.y;

            lastScreenPoint = newScreenPoint;
            AnnotationAttributes attrs = getAttributes();
            int newX = attrs.getDrawOffset().x - changeX;
            int newY = attrs.getDrawOffset().y - changeY;

            if ((screenPoint.y + newY) < 0) {
                newY = (int) -screenPoint.y;
            } else if ((screenPoint.y + newY + size.height) > dc.getView().getViewport().height) {
                newY = dc.getView().getViewport().height - (int) screenPoint.y - size.height;
            }

            int middle = size.width / 2;
            if ((screenPoint.x + newX - middle) < 0) {
                newX = (int) -screenPoint.x + size.width / 2;
            } else if ((screenPoint.x + newX + middle) > dc.getView().getViewport().width) {
                newX = dc.getView().getViewport().width - (int) screenPoint.x - middle;
            }

            attrs.setDrawOffset(new Point(newX, newY));
        }

        Position pos = dc.getGlobe().computePositionFromPoint(point);

        // Scale and opacity depending on distance from eye
        double[] scaleAndOpacity = computeDistanceScaleAndOpacity(dc, point, size);

        this.drawTopLevelAnnotation(dc, (int) screenPoint.x, (int) screenPoint.y, size.width, size.height,
                scaleAndOpacity[0], scaleAndOpacity[1], pos);
    }

    @Override
    public Dimension getPreferredSize(DrawContext dc) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Clamp the caller specified size.
        Dimension size = new Dimension(this.getAttributes().getSize());
        if (size.width < 1) {
            size.width = 1;
        }
        if (size.height < 0) {
            size.height = 0;
        }

        // Compute the size of this annotation's inset region.
        java.awt.Rectangle insetBounds = this.computeInsetBounds(size.width, size.height);
        Dimension insetSize = new Dimension(insetBounds.width, insetBounds.height);

        // Wrap the text to fit inside the annotation's inset bounds. Then adjust the inset bounds to the wrapped
        // text, depending on the annotation's attributes.
        insetSize = this.adjustSizeToText(dc, insetSize.width, insetSize.height);

        // Adjust the inset bounds to the child annotations.
        java.awt.Insets insets = this.getAttributes().getInsets();
        return new Dimension(
                insetSize.width + (insets.left + insets.right),
                insetSize.height + (insets.top + insets.bottom));
    }

    public IEntity getEntity() {
        return entity;
    }
}
