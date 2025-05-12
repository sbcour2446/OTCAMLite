package gov.mil.otc._3dvis.layer.menu;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import gov.mil.otc._3dvis.layer.control.ControlButton;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;

import java.awt.*;
import java.awt.image.BufferedImage;

import static gov.mil.otc._3dvis.layer.Constant.TRANSPARENT;

public class MenuItem extends ControlButton {

    private static final String CHECKBOX_IMAGE = "/images/checkbox.png";
    private static final String CHECKBOX_SELECTED_IMAGE = "/images/checkbox_selected.png";
    private static final int HEIGHT = 20;
    private int baseX = 0;
    private int baseY = 0;
    private int width = 0;
    private boolean selected = false;

    public MenuItem(String text, Point position, Dimension size) {
        super(text, size, true);
        baseX = position.x;
        baseY = position.y;
        width = size.width;
        initialize();
    }

    private void initialize() {
        BufferedImage image = ImageLoader.getBufferedImage(selected ? CHECKBOX_SELECTED_IMAGE : CHECKBOX_IMAGE);
        getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
        getAttributes().setBackgroundColor(TRANSPARENT);
        getAttributes().setBorderWidth(0);
        getAttributes().setCornerRadius(0);
        getAttributes().setImageOffset(new Point(2, (HEIGHT - image.getHeight()) / 2));
        getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
        getAttributes().setImageScale(1.0);
        getAttributes().setImageSource(image);
        getAttributes().setInsets(new Insets(2, image.getWidth() + 4, 2, 0));
        getAttributes().setSize(new Dimension(width, HEIGHT));
        getAttributes().setTextAlign(AVKey.LEFT);
        getAttributes().setTextColor(Color.WHITE);
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        BufferedImage image = ImageLoader.getBufferedImage(selected ? CHECKBOX_SELECTED_IMAGE : CHECKBOX_IMAGE);
        getAttributes().setImageSource(image);
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2();
        gl.glEnable(GL.GL_SCISSOR_TEST);
        gl.glScissor(baseX, baseY, width, 200);
        super.doRenderNow(dc);
        gl.glDisable(GL.GL_SCISSOR_TEST);
    }
}
