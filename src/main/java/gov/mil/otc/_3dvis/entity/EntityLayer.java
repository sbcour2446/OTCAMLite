package gov.mil.otc._3dvis.entity;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.entity.render.EntityPlacemark;
import gov.mil.otc._3dvis.entity.render.EntityTrack;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;
import gov.mil.otc._3dvis.ui.contextmenu.ContextMenuController;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;

import javax.swing.*;

public class EntityLayer implements SelectListener {

    private static final EntityLayer SINGLETON = new EntityLayer();
    private final RenderableLayer layer = new RenderableLayer();
    private EntityPlacemark lastPicked = null;

    private EntityLayer() {
        layer.setName("EntityLayer");
        WWController.addLayer(layer);
        WWController.getWorldWindowPanel().addSelectListener(this);
    }

    public static void add(final Renderable renderable) {
        SwingUtilities.invokeLater(() -> SINGLETON.layer.addRenderable(renderable));
    }

    public static void remove(final Renderable renderable) {
        SwingUtilities.invokeLater(() -> SINGLETON.layer.removeRenderable(renderable));
    }

    public static void add(final EntityPlacemark entityPlacemark) {
        SwingUtilities.invokeLater(() -> SINGLETON.layer.addRenderable(entityPlacemark));
    }

    public static void remove(final EntityPlacemark entityPlacemark) {
        SwingUtilities.invokeLater(() -> SINGLETON.layer.removeRenderable(entityPlacemark));
    }

    public static void add(final EntityTrack entityTrack) {
        SwingUtilities.invokeLater(() -> SINGLETON.layer.addRenderable(entityTrack));
    }

    public static void remove(final EntityTrack entityTrack) {
        SwingUtilities.invokeLater(() -> SINGLETON.layer.removeRenderable(entityTrack));
    }

    public static void add(final StatusAnnotation statusAnnotation) {
        SwingUtilities.invokeLater(() -> SINGLETON.layer.addRenderable(statusAnnotation));
    }

    public static void remove(final StatusAnnotation statusAnnotation) {
        SwingUtilities.invokeLater(() -> SINGLETON.layer.removeRenderable(statusAnnotation));
    }

    @Override
    public void selected(SelectEvent event) {
        String eventAction = event.getEventAction();
        if (SelectEvent.ROLLOVER.equals(eventAction)) {
            highlight(event.getTopObject());
        } else if (SelectEvent.RIGHT_PRESS.equals(eventAction)) {
            showContextMenu(event);
        } else if (SelectEvent.LEFT_PRESS.equals(eventAction)) {
            showStatusWindow(event.getTopObject());
        }
    }

    private void highlight(Object o) {
        if (lastPicked == o) {
            return; // same thing selected
        }

        if (o instanceof EntityTrack && lastPicked == ((EntityTrack) o).getEntityPlacemark()) {
            return;
        }

        // Turn off highlight if on.
        if (lastPicked != null) {
            lastPicked.setHighlighted(false);
            if (lastPicked.getEntityTrack() != null) {
                lastPicked.getEntityTrack().setHighlighted(false);
            }
            lastPicked = null;
        }

        // Turn on highlight if object selected.
        if (o instanceof EntityPlacemark) {
            lastPicked = (EntityPlacemark) o;
            lastPicked.setHighlighted(true);
            if (lastPicked.getEntityTrack() != null) {
                lastPicked.getEntityTrack().setHighlighted(true);
            }
        }
    }

    private void showContextMenu(SelectEvent event) {
        Object o = event.getTopObject();
        if (!(o instanceof EntityPlacemark)) {
            return;
        }

        EntityPlacemark entityPlacemark = (EntityPlacemark) o;
        ContextMenuController.show(event.getMouseEvent().getXOnScreen(), event.getMouseEvent().getYOnScreen(),
                entityPlacemark.getEntity());
    }

    private void showStatusWindow(Object o) {
        if (o instanceof EntityPlacemark) {
            ((EntityPlacemark) o).getEntity().toggleStatusAnnotation();
        }
    }
}
