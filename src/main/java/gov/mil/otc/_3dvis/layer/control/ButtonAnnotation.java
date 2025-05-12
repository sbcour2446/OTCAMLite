package gov.mil.otc._3dvis.layer.control;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Annotation;
import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.WWTexture;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.OGLStackHandler;

import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

public class ButtonAnnotation extends ImageAnnotation {

    protected boolean enabled;
    protected boolean pressed;
    protected String actionCommand;
    protected double disabledOpacity;
    protected WWTexture pressedMaskTexture;
    protected EventListenerList listenerList = new EventListenerList();

    public ButtonAnnotation(Object imageSource, Object pressedMaskSource) {
        super(imageSource);
        setEnableSmoothing(false);
        setUseMipmaps(false);
        enabled = true;
        disabledOpacity = 0.6;
        setPressedMaskSource(pressedMaskSource);
    }

    public ButtonAnnotation(Object imageSource) {
        this(imageSource, null);
    }

    public ButtonAnnotation() {
        this(null);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }

    public double getDisabledOpacity() {
        return disabledOpacity;
    }

    public void setDisabledOpacity(double opacity) {
        if (opacity < 0 || opacity > 1) {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "opacity < 0 or opacity > 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        disabledOpacity = opacity;
    }

    public Object getPressedMaskSource() {
        return (pressedMaskTexture != null) ? pressedMaskTexture.getImageSource() : null;
    }

    public void setPressedMaskSource(Object source) {
        pressedMaskTexture = null;

        if (source != null) {
            pressedMaskTexture = new BasicWWTexture(source, false);
        }
    }

    public WWTexture getPressedMaskTexture() {
        return this.pressedMaskTexture;
    }

    public java.awt.event.ActionListener[] getActionListeners() {
        return listenerList.getListeners(java.awt.event.ActionListener.class);
    }

    public void addActionListener(java.awt.event.ActionListener listener) {
        listenerList.add(java.awt.event.ActionListener.class, listener);
    }

    public void removeActionListener(java.awt.event.ActionListener listener) {
        listenerList.remove(java.awt.event.ActionListener.class, listener);
    }

    @Override
    protected void setupAnnotationAttributes(Annotation annotation) {
        super.setupAnnotationAttributes(annotation);

        annotation.setPickEnabled(true);
    }

    protected void onButtonPressed(SelectEvent e) {
        MouseEvent mouseEvent = e.getMouseEvent();
        fireActionPerformed(mouseEvent.getID(), mouseEvent.getWhen(), mouseEvent.getModifiersEx());
    }

    protected void fireActionPerformed(int id, long when, int modifiers) {
        ActionEvent event = null;
        // Guaranteed to return a non-null array
        Object[] listeners = this.listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == java.awt.event.ActionListener.class) {
                // Lazily create the event:
                if (event == null) {
                    event = new java.awt.event.ActionEvent(this, id, this.getActionCommand(), when, modifiers);
                }

                ((java.awt.event.ActionListener) listeners[i + 1]).actionPerformed(event);
            }
        }
    }

    @Override
    public void drawContent(DrawContext dc, int width, int height, double opacity, Position pickPosition) {
        if (!isEnabled()) {
            opacity *= getDisabledOpacity();
        }

        super.drawContent(dc, width, height, opacity, pickPosition);
        drawPressedMask(dc, width, height, opacity, pickPosition);
    }

    protected void drawPressedMask(DrawContext dc, int width, int height, double opacity, Position pickPosition) {
        if (dc.isPickingMode()) {
            return;
        }

        if (!isPressed()) {
            return;
        }

        doDrawPressedMask(dc, width, height, opacity, pickPosition);
    }

    @Override
    protected void applyBackgroundTextureState(DrawContext dc, int width, int height, double opacity, WWTexture texture) {
        super.applyBackgroundTextureState(dc, width, height, opacity, texture);

        // Setup the mask to modulate with the existing fragment color. This will have the effect of multiplying
        // the button depressed mask colors with the button colors.
        if (getPressedMaskTexture() == texture) {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_ZERO, GL.GL_SRC_COLOR);
            gl.glColor4f(1f, 1f, 1f, 1f);
        }
    }

    protected void doDrawPressedMask(DrawContext dc, int width, int height, double opacity, Position pickPosition) {
        WWTexture texture = getPressedMaskTexture();
        if (texture == null) {
            return;
        }

        // Push state for blend enable, blending function, and current color. We set these OGL states in
        // applyBackgroundTextureState(), which is invoked by doDrawBackgroundTexture().
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();
        ogsh.pushAttrib(gl, GL.GL_COLOR_BUFFER_BIT | GL2.GL_CURRENT_BIT);
        try {
            doDrawBackgroundTexture(dc, width, height, 1, pickPosition, texture);
        } finally {
            ogsh.pop(gl);
        }
    }
}
