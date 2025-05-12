package gov.mil.otc._3dvis.worldwindex.view.orbit;

import gov.mil.otc._3dvis.ui.contextmenu.ContextMenuController;
import gov.mil.otc._3dvis.worldwindex.animation.ZoomToCursorAnimator;
import gov.nasa.worldwind.animation.AnimationController;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;
import gov.nasa.worldwind.view.orbit.OrbitViewPropertyAccessor;

import java.awt.event.MouseEvent;

public class OrbitViewInputHandlerEx extends OrbitViewInputHandler {

    private boolean zoomToCursor = true;

    public OrbitViewInputHandlerEx() {
        super();

        getAttributes().getActionMap(ViewInputAttributes.DEVICE_MOUSE)
                .getActionAttributes(ViewInputAttributes.VIEW_MOVE_TO).setMouseActionListener(null);

        //todo uncomment to disable right click action
//        getAttributes().getActionMap(ViewInputAttributes.DEVICE_MOUSE)
//                .getActionAttributes(ViewInputAttributes.VIEW_ROTATE).setMouseActionListener(null);
    }

    public boolean isZoomToCursor() {
        return zoomToCursor;
    }

    public void setZoomToCursor(boolean zoomToCursor) {
        this.zoomToCursor = zoomToCursor;
    }

    public void goTo(Position lookAtPos, Angle heading, Angle pitch, double distance) {
        stopAnimators();
        addPanToAnimator(lookAtPos, heading, pitch, distance, true);
        this.getView().firePropertyChange(AVKey.VIEW, null, getView());
    }

    private void doChangeZoom(BasicOrbitView view,
                              AnimationController animControl,
                              double change, ViewInputAttributes.ActionAttributes attrib) {
        view.computeAndSetViewCenterIfNeeded();
        double smoothing = attrib.getSmoothingValue();
        if (!(attrib.isEnableSmoothing() && this.isEnableSmoothing())) {
            smoothing = 0.0;
        }

        if (smoothing == 0.0) {
            if (animControl.get(VIEW_ANIM_ZOOM) != null) {
                animControl.remove(VIEW_ANIM_ZOOM);
            }
            view.setZoom(computeNewZoom(view, view.getZoom(), change));
        } else if (zoomToCursor && attrib.getMouseActions() != null && !attrib.getMouseActions().isEmpty()
                && ((ViewInputAttributes.ActionAttributes.MouseAction) attrib.getMouseActions().get(0)).mouseButton == 507) {
            double newZoom;
            ZoomToCursorAnimator zoomAnimator = (ZoomToCursorAnimator) animControl.get(VIEW_ANIM_ZOOM);

            if (zoomAnimator == null || !zoomAnimator.hasNext()) {
                newZoom = computeNewZoom(view, view.getZoom(), change);
                zoomAnimator = new ZoomToCursorAnimator(view, newZoom, smoothing,
                        OrbitViewPropertyAccessor.createZoomAccessor(view), true, getMousePoint());
                animControl.put(VIEW_ANIM_ZOOM, zoomAnimator);
            } else {
                newZoom = computeNewZoom(view, zoomAnimator.getEnd(), change);
                zoomAnimator.setEnd(newZoom);
                zoomAnimator.setCursor(getMousePoint());
            }

            zoomAnimator.start();
        }
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    @Override
    protected void changeZoom(BasicOrbitView view,
                              AnimationController animControl,
                              double change, ViewInputAttributes.ActionAttributes attrib) {
        if (!zoomToCursor) {
            super.changeZoom(view, animControl, change, attrib);
        } else {
            doChangeZoom(view, animControl, change, attrib);
        }
    }

    @Override
    protected void handleMouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            ContextMenuController.show(e.getXOnScreen(), e.getYOnScreen());
        }
    }
}
