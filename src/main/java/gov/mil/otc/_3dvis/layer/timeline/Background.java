package gov.mil.otc._3dvis.layer.timeline;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.*;

import static gov.mil.otc._3dvis.layer.Constant.*;

public class Background extends ScreenAnnotation implements OrderedRenderable {

    protected Background() {
        super("", new Point());
        initialize();
    }

    private void initialize() {
        getAttributes().setCornerRadius(0);
        getAttributes().setFrameShape(AVKey.SHAPE_RECTANGLE);
        getAttributes().setLeader(AVKey.SHAPE_NONE);
        getAttributes().setOpacity(0);
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        renderBackground(dc);
        super.doRenderNow(dc);
    }

    private void renderBackground(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler stackHandler = new OGLStackHandler();

        int attrMask = GL.GL_DEPTH_BUFFER_BIT // for depth test enable, depth func, depth mask
                | GL.GL_COLOR_BUFFER_BIT // for alpha test enable, alpha func, blend enable, blend func
                | GL2.GL_CURRENT_BIT; // for current color

        Rectangle viewport = dc.getView().getViewport();

        try {
            stackHandler.pushAttrib(gl, attrMask);
            gl.glDisable(GL.GL_DEPTH_TEST);

            stackHandler.pushProjectionIdentity(gl);
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -1, 1);
            stackHandler.pushModelviewIdentity(gl);

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

            //main background
            float[] colorRGB = BACKGROUND_COLOR.getComponents(null);
            gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], colorRGB[3]);

            gl.glBegin(GL2ES3.GL_QUADS);
            gl.glVertex3d(0, 0, 0);
            gl.glVertex3d(viewport.width, 0, 0);
            gl.glVertex3d(viewport.width, TIMELINE_TOP, 0);
            gl.glVertex3d(0, TIMELINE_TOP, 0);
            gl.glEnd();
        } finally {
            gl.glEnable(GL.GL_DEPTH_TEST);
            stackHandler.pop(gl);
        }
    }

    @Override
    public double getDistanceFromEye() {
        return 0;
    }
}
