package gov.mil.otc._3dvis.layer.view;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.layer.control.ImageButton;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.awt.ViewInputHandler;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static gov.mil.otc._3dvis.layer.Constant.TIMELINE_INSET;
import static gov.mil.otc._3dvis.layer.Constant.TIMELINE_TOP;

public class ViewLayer extends AnnotationLayer implements SelectListener, MouseListener {

    private static final ViewLayer SINGLETON = new ViewLayer();
    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 0);
    private static final int PADDING = 2;
    private final ScreenAnnotation compassAnnotation = new ScreenAnnotation("", new Point(0, 0));
    private final ScreenAnnotation pitchAnnotation = new ScreenAnnotation("", new Point(0, 0));
    private final ScreenAnnotation zoomAnnotation = new ScreenAnnotation("", new Point(0, 0));

    private final ImageButton zoomIn = new ImageButton("/images/zoom_in.png",
            new Dimension(24, 24), true);
    private final ImageButton zoomOut = new ImageButton("/images/zoom_out.png",
            new Dimension(24, 24), true);
    private final ImageButton pitchUp = new ImageButton("/images/pitch_up.png",
            new Dimension(24, 24), true);
    private final ImageButton pitchDown = new ImageButton("/images/pitch_down.png",
            new Dimension(24, 24), true);
    private final ImageButton rotateLeft = new ImageButton("/images/rotate_left.png",
            new Dimension(16, 48), new Point(16, 0));
    private final ImageButton rotateRight = new ImageButton("/images/rotate_right.png",
            new Dimension(16, 48), new Point(-16, 0));
    private final ImageButton compass = new ImageButton("/images/compass.png",
            new Dimension(24, 48));

    private ImageButton currentAnnotation = null;
    private boolean initialized = false;
    private Timer buttonTimer = null;

    private ViewLayer() {
    }

    public static void initialize() {
        SINGLETON.doInitialize();
    }

    private void doInitialize() {
        if (!initialized) {
            initialized = true;

            compassAnnotation.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
            compassAnnotation.getAttributes().setBackgroundColor(BACKGROUND_COLOR);
            compassAnnotation.getAttributes().setBorderWidth(0);
            compassAnnotation.getAttributes().setFont(Common.FONT_SMALL_BOLD);
            compassAnnotation.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
            compassAnnotation.getAttributes().setInsets(new Insets(0, 0, 0, 0));
            compassAnnotation.getAttributes().setSize(new Dimension(48, 64));
            compassAnnotation.getAttributes().setTextAlign(AVKey.CENTER);
            compassAnnotation.getAttributes().setTextColor(Color.WHITE);
            compassAnnotation.setPickEnabled(false);

            pitchAnnotation.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
            pitchAnnotation.getAttributes().setBackgroundColor(BACKGROUND_COLOR);
            pitchAnnotation.getAttributes().setBorderWidth(0);
            pitchAnnotation.getAttributes().setCornerRadius(5);
            pitchAnnotation.getAttributes().setFont(Common.FONT_SMALL_BOLD);
            pitchAnnotation.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
            pitchAnnotation.getAttributes().setInsets(new Insets(0, 0, 0, 0));
            pitchAnnotation.getAttributes().setSize(new Dimension(48, 64));
            pitchAnnotation.getAttributes().setTextAlign(AVKey.CENTER);
            pitchAnnotation.getAttributes().setTextColor(Color.WHITE);
            pitchAnnotation.setPickEnabled(false);

            zoomAnnotation.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
            zoomAnnotation.getAttributes().setBackgroundColor(BACKGROUND_COLOR);
            zoomAnnotation.getAttributes().setBorderWidth(0);
            zoomAnnotation.getAttributes().setCornerRadius(2);
            zoomAnnotation.getAttributes().setFont(Common.FONT_SMALL_BOLD);
            zoomAnnotation.getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
            zoomAnnotation.getAttributes().setInsets(new Insets(0, 0, 0, 0));
            zoomAnnotation.getAttributes().setSize(new Dimension(48, 64));
            zoomAnnotation.getAttributes().setTextAlign(AVKey.CENTER);
            zoomAnnotation.getAttributes().setTextColor(Color.WHITE);
            zoomAnnotation.setPickEnabled(false);

            zoomIn.setValue(AVKey.HOVER_TEXT, "Zoom In");
            zoomOut.setValue(AVKey.HOVER_TEXT, "Zoom Out");
            pitchUp.setValue(AVKey.HOVER_TEXT, "Pitch Up");
            pitchDown.setValue(AVKey.HOVER_TEXT, "Pitch Down");
            rotateLeft.setValue(AVKey.HOVER_TEXT, "Rotate Left");
            rotateRight.setValue(AVKey.HOVER_TEXT, "Rotate Right");
            compass.setValue(AVKey.HOVER_TEXT, "Reset View");

            addAnnotation(zoomAnnotation);
            addAnnotation(zoomIn);
            addAnnotation(zoomOut);
            addAnnotation(pitchAnnotation);
            addAnnotation(pitchUp);
            addAnnotation(pitchDown);
            addAnnotation(compassAnnotation);
            addAnnotation(rotateLeft);
            addAnnotation(rotateRight);
            addAnnotation(compass);

            setName("ViewLayer");

            WWController.getWorldWindowPanel().getInputHandler().addMouseListener(this);
            WWController.getWorldWindowPanel().addSelectListener(this);
            WWController.addLayer(this);
        }
    }

    private void updateView() {
        OrbitView orbitView = WWController.getView();

        if (orbitView == null) {
            return;
        }

        if (currentAnnotation == zoomIn) {
            orbitView.setZoom(computeNewZoom(orbitView.getZoom(), -.8));
        } else if (currentAnnotation == zoomOut) {
            orbitView.setZoom(computeNewZoom(orbitView.getZoom(), .8));
            if (orbitView.getZoom() > 40000000) {
                orbitView.setZoom(40000000);
            }
        } else if (currentAnnotation == pitchUp) {
            orbitView.setPitch(computeNewPitch(orbitView.getPitch(), -1));
        } else if (currentAnnotation == pitchDown) {
            orbitView.setPitch(computeNewPitch(orbitView.getPitch(), 1));
        } else if (currentAnnotation == rotateLeft) {
            orbitView.setHeading(computeNewHeading(orbitView.getHeading(), 1));
        } else if (currentAnnotation == rotateRight) {
            orbitView.setHeading(computeNewHeading(orbitView.getHeading(), -1));
        }
    }

    private double computeNewZoom(double currentZoom, double amount) {
        double coeff = 0.05;
        double change = coeff * amount;
        double logZoom = currentZoom != 0 ? Math.log(currentZoom) : 0;
        return Math.exp(logZoom + change);
    }

    private Angle computeNewPitch(Angle currentPitch, double amount) {
        Angle newPitch = currentPitch.addDegrees(amount);
        if (newPitch.degrees < 0) {
            return Angle.ZERO;
        } else if (newPitch.degrees > 90) {
            return Angle.POS90;
        }
        return newPitch;
    }

    private Angle computeNewHeading(Angle currentHeading, double amount) {
        return currentHeading.addDegrees(amount);
    }

    private void handleMouseOverEvent(SelectEvent event) {
        if (currentAnnotation == event.getTopObject()) {
            return;
        }

        if (currentAnnotation != null) {
            currentAnnotation.highlight(false);
        }

        if (event.getTopObject() instanceof ImageButton) {
            currentAnnotation = (ImageButton) event.getTopObject();
        }

        if (currentAnnotation != null) {
            currentAnnotation.highlight(true);
        }
    }

    private void handleLeftPressEvent(SelectEvent event) {
        if (event.getTopObject() == compass) {
            View view = WWController.getWorldWindowPanel().getView();
            ViewInputHandler viewInputHandler = WWController.getWorldWindowPanel().getView().getViewInputHandler();
            if (viewInputHandler instanceof OrbitViewInputHandler) {
                ((OrbitViewInputHandler) viewInputHandler).addHeadingPitchRollAnimator(
                        view.getHeading(), Angle.ZERO, view.getPitch(), Angle.ZERO, view.getRoll(), Angle.ZERO);
            }
        }
    }

    private void startButtonTimer() {
        if (buttonTimer == null) {
            buttonTimer = new Timer(5, (ActionEvent event) -> updateView());
        }
        buttonTimer.start();
    }

    private void stopButtonTimer() {
        if (buttonTimer != null) {
            buttonTimer.stop();
        }
    }

    @Override
    public void doRender(DrawContext dc) {
        if (dc.getView().getViewport().width < TIMELINE_INSET * 4) {
            return;
        }

        int x = dc.getView().getViewport().width - PADDING - compassAnnotation.getAttributes().getSize().width / 2;
        int y = TIMELINE_TOP - compassAnnotation.getAttributes().getSize().height;
        compassAnnotation.setScreenPoint(new Point(x, y));
        compass.setScreenPoint(new Point(x, y));
        compass.setHeading(dc.getView().getHeading().degrees);
        rotateRight.setScreenPoint(new Point(x + 16, y));
        rotateLeft.setScreenPoint(new Point(x - 16, y));

        x = dc.getView().getViewport().width - PADDING - compassAnnotation.getAttributes().getSize().width
                - PADDING - zoomAnnotation.getAttributes().getSize().width / 2;
        zoomAnnotation.setScreenPoint(new Point(x, y));
        zoomIn.setScreenPoint(new Point(x, y + 1 + zoomOut.getAttributes().getSize().height));
        zoomOut.setScreenPoint(new Point(x, y + 1));

        x = dc.getView().getViewport().width - PADDING - compassAnnotation.getAttributes().getSize().width
                - PADDING - zoomAnnotation.getAttributes().getSize().width
                - PADDING - pitchAnnotation.getAttributes().getSize().width / 2;
        pitchAnnotation.setScreenPoint(new Point(x, y));
        pitchUp.setScreenPoint(new Point(x, y + 1 + pitchDown.getAttributes().getSize().height));
        pitchDown.setScreenPoint(new Point(x, y + 1));

        int centerElevation = 0;
        if (dc.getViewportCenterPosition() != null) {
            centerElevation = (int) dc.getViewportCenterPosition().elevation;
        }
        int eyeElevation = (int) Math.round(dc.getView().getEyePosition().getElevation() - centerElevation);
        String eyeElevationText;
        if (eyeElevation < 1000) {
            eyeElevationText = String.format("%dm", eyeElevation);
        } else if (eyeElevation < 1000000) {
            eyeElevationText = String.format("%dk", eyeElevation / 1000);
        } else {
            eyeElevationText = String.format("%dM", eyeElevation / 1000000);
        }

        zoomAnnotation.setText(eyeElevationText);
        pitchAnnotation.setText(String.format("%9.2f", dc.getView().getPitch().degrees));
        compassAnnotation.setText(String.format("%9.2f", dc.getView().getHeading().degrees));

        super.doRender(dc);
    }

    @Override
    public void selected(SelectEvent event) {
        if (event.getMouseEvent() != null && event.getMouseEvent().isConsumed()
                || event.getTopObject() == null
                || event.getTopPickedObject().getParentLayer() != this
                || !(event.getTopObject() instanceof AVList)) {
            if (currentAnnotation != null) {
                currentAnnotation.highlight(false);
                currentAnnotation = null;
            }
            return;
        }

        switch (event.getEventAction()) {
            case SelectEvent.ROLLOVER -> handleMouseOverEvent(event);
            case SelectEvent.DRAG -> event.consume();
            case SelectEvent.LEFT_PRESS -> {
                handleLeftPressEvent(event);
                startButtonTimer();
            }
            default -> {
                //do nothing
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        //do nothing
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        stopButtonTimer();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //do nothing
    }
}
