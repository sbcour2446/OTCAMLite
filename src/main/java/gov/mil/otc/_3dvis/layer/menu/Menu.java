package gov.mil.otc._3dvis.layer.menu;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ScreenAnnotation;

import java.awt.*;

import static gov.mil.otc._3dvis.layer.Constant.BACKGROUND_COLOR;

public class Menu extends ScreenAnnotation {

    private final int baseX;
    private final int baseY;
    private final int width;

    public Menu(String text, Point position, int width) {
        super(text, position);
        initialize();
        baseX = position.x;
        baseY = position.y;
        this.width = width;
    }

    private void initialize() {
        getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
        getAttributes().setBackgroundColor(BACKGROUND_COLOR);
        getAttributes().setBorderWidth(0);
        getAttributes().setCornerRadius(0);
        getAttributes().setInsets(new Insets(0, 0, 0, 0));
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2();
        gl.glEnable(GL.GL_SCISSOR_TEST);
        gl.glScissor(baseX, baseY, width, getAttributes().getSize().height);
        super.doRenderNow(dc);
        gl.glDisable(GL.GL_SCISSOR_TEST);
    }
}
