package gov.mil.otc._3dvis.layer.timeline;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.util.awt.TextRenderer;
import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static gov.mil.otc._3dvis.layer.Constant.*;

public class TimelineAnnotation extends ScreenAnnotation implements OrderedRenderable {

    private static final Color CENTER_LINE_COLOR = Color.RED;
    private static final int FADE_WIDTH = 200;
    private final TextRenderer textRenderer = new TextRenderer(Common.FONT_MEDIUM);
    private long timeInMillis = 0;
    private int lastPickPointX = -1;
    private boolean isDragging = false;
    private long dragTime;
    private long markStartTime = -1;
    private long markStopTime = -1;

    public TimelineAnnotation(String text, Point position) {
        super(text, position);

        initialize();
    }

    private void initialize() {
        getAttributes().setCornerRadius(0);
        getAttributes().setFrameShape(AVKey.SHAPE_RECTANGLE);
        getAttributes().setLeader(AVKey.SHAPE_NONE);
        getAttributes().setOpacity(0);
        setAlwaysOnTop(true);
    }

    protected synchronized void setTime(long millis) {
        timeInMillis = millis;
    }

    public void zoomIn() {
        Zoom.zoomIn();
    }

    public void zoomOut() {
        Zoom.zoomOut();
    }

    public void doDrag(SelectEvent event) {
        isDragging = true;
        if (lastPickPointX == -1) {
            lastPickPointX = event.getPickPoint().x;
            dragTime = TimeManager.getTime();
        } else {
            int diff = lastPickPointX - event.getPickPoint().x;
            long timeDiff = diff * Zoom.getZoom().getMillisPerPixel();
            long newTime = dragTime + timeDiff;
            long systemTime = System.currentTimeMillis();
            if (newTime > systemTime) {
                newTime = systemTime;
            }
            TimeManager.setTime(newTime);
            setTime(newTime);
            dragTime = newTime;
            lastPickPointX = event.getPickPoint().x;
        }
    }

    public void doDragEnd() {
        if (lastPickPointX != -1) {
            TimeManager.setTime(timeInMillis);
        }
        isDragging = false;
        lastPickPointX = -1;
    }

    public boolean isDragging() {
        return isDragging;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setMark(long startTime, long stopTime) {
        markStartTime = startTime;
        markStopTime = stopTime;
    }

    public void clearMark() {
        markStartTime = -1;
        markStopTime = -1;
    }

    @Override
    public void render(DrawContext dc) {
        if (!isDragging) {
            setTime(TimeManager.getTime());
        }
        renderTimeline(dc);
    }

    private void renderTimeline(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler stackHandler = new OGLStackHandler();
        stackHandler.pushModelviewIdentity(gl);

        try {
            gl.glDisable(GL.GL_DEPTH_TEST);

            Rectangle viewport = dc.getView().getViewport();
            stackHandler.pushProjectionIdentity(gl);
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -1, 1);
            stackHandler.pushModelviewIdentity(gl);

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

            Zoom zoom = Zoom.getZoom();
            int width = dc.getView().getViewport().width - (TIMELINE_INSET * 2);
            long timeWidth = width * zoom.getMillisPerPixel();
            long systemTime = System.currentTimeMillis();
            long startTime = timeInMillis - timeWidth / 2;

            Calendar time = getFirstLabelTime(startTime, zoom);

            int leftEdge = TIMELINE_INSET + 1;
            int rightEdge = width + TIMELINE_INSET - ZOOM_BAR_WIDTH;
            int startX = TIMELINE_INSET + (int) ((time.getTimeInMillis() - startTime) / zoom.getMillisPerPixel());

            drawSmallTicks(gl, startX, leftEdge, rightEdge, time.getTimeInMillis(), systemTime, zoom);
            drawLabelTicks(gl, startX, leftEdge, rightEdge, time.getTimeInMillis(), systemTime, zoom);
            drawCenterLine(gl, viewport.width / 2);
            drawMark(gl, leftEdge, viewport.width / 2, rightEdge, zoom);
        } finally {
            gl.glEnable(GL.GL_DEPTH_TEST);
            stackHandler.pop(gl);
        }
    }

    private void drawMark(GL2 gl, int leftEdge, int center, int rightEdge, Zoom zoom) {
        long deltaStart = -1;
        long deltaStop = -1;

        if (markStartTime > 0) {
            deltaStart = (timeInMillis - markStartTime) / zoom.getMillisPerPixel();
            if (markStopTime > 0) {
                deltaStop = (timeInMillis - markStopTime) / zoom.getMillisPerPixel();
            } else {
                deltaStop = (timeInMillis - System.currentTimeMillis()) / zoom.getMillisPerPixel();
            }
        }

        long left = center - deltaStart;
        left = left > leftEdge ? left : leftEdge;
        left = left < rightEdge ? left : rightEdge + 1;

        long right = center - deltaStop;
        right = right < rightEdge ? right : rightEdge;
        right = right > leftEdge ? right : leftEdge - 1;


        float[] colorRGB = new Color(0, 100, 0, 255).getComponents(null);
        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], colorRGB[3]);
        gl.glBegin(GL2ES3.GL_QUADS);
        double y = (double) TIMELINE_TOP - TIMELINE_HEIGHT;
        gl.glVertex3d(left, y, 0);
        gl.glVertex3d(right, y, 0);
        gl.glVertex3d(right, y + 2, 0);
        gl.glVertex3d(left, y + 2, 0);
        gl.glEnd();
    }

    private void drawCenterLine(GL2 gl, int center) {
        float[] colorRGB = CENTER_LINE_COLOR.getComponents(null);
        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], colorRGB[3]);
        gl.glLineWidth(1);
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex3d(center, TIMELINE_TOP, 0);
        gl.glVertex3d(center, (double) TIMELINE_TOP - TIMELINE_HEIGHT, 0);
        gl.glEnd();
    }

    private void drawSmallTicks(GL2 gl, int startX, int leftEdge, int rightEdge, long startTime, long systemTime, Zoom zoom) {
        int tickX = startX;
        long tickTime = startTime;
        float[] colorRGB = Color.WHITE.getComponents(null);
        while (tickX < rightEdge) {
            if (tickX > leftEdge) {
                colorRGB[3] = getOpacity(tickTime, systemTime, tickX, leftEdge, rightEdge);
                drawSmallTick(gl, tickX, colorRGB);
            }
            tickX += zoom.getTickWidth();
            tickTime += zoom.getMillisPerTick();
        }
    }

    private void drawSmallTick(GL2 gl, int x, float[] colorRGB) {
        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], colorRGB[3]);
        gl.glLineWidth(1);
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex3d(x, TIMELINE_TOP - TIMELINE_HEIGHT + 1.0, 1);
        gl.glVertex3d(x, TIMELINE_TOP - 20.0, 0);
        gl.glEnd();
    }

    private void drawLabelTicks(GL2 gl, int startX, int leftEdge, int rightEdge, long startTime, long systemTime, Zoom zoom) {
        int tickX = startX;
        long tickTime = startTime;
        float[] colorRGB = zoom.getLabel() == Zoom.Label.YEAR
                ? Color.YELLOW.getComponents(null) : Color.WHITE.getComponents(null);
        Calendar time = getFirstLabelTime(startTime, zoom);
        while (tickX < rightEdge) {
            if (tickX > leftEdge) {
                colorRGB[3] = getOpacity(tickTime, systemTime, tickX, leftEdge, rightEdge);
                time.setTimeInMillis(tickTime);
                drawLabelTick(gl, tickX, colorRGB);
                renderText(textRenderer, tickX + 1, getLabelText(time, zoom), colorRGB, rightEdge);
            }
            tickX += zoom.getLabelWidthInPixels();
            tickTime += zoom.getLabelWidthInMillis();
        }
    }

    private void drawLabelTick(GL2 gl, int x, float[] colorRGB) {
        gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], colorRGB[3]);
        gl.glLineWidth(1);
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex3d(x, TIMELINE_TOP - 20.0, 1);
        gl.glVertex3d(x, TIMELINE_TOP - 1.0, 0);
        gl.glEnd();
    }

    private float getOpacity(long tickTime, long systemTime, int tickX, int leftEdge, int rightEdge) {
        float opacity = 1.0f;
        if (tickTime > systemTime) {
            opacity = 0.2f;
        }
        if (tickX > rightEdge - FADE_WIDTH) {
            opacity *= (float) (rightEdge - tickX) / FADE_WIDTH;
        } else if (tickX < leftEdge + FADE_WIDTH) {
            opacity *= (float) (tickX - leftEdge) / FADE_WIDTH;
        }
        return opacity;
    }

    private void renderText(TextRenderer textRenderer, int x, String text, float[] colorRGB, int rightEdge) {
        textRenderer.begin3DRendering();
        textRenderer.setColor(colorRGB[0], colorRGB[1], colorRGB[2], colorRGB[3]);
        Rectangle2D bounds = textRenderer.getBounds(text);
        if (x < rightEdge - bounds.getWidth()) {
            int offsetY = TIMELINE_TOP - (int) bounds.getHeight();
            textRenderer.draw(text, x, offsetY);
        }
        textRenderer.end3DRendering();
    }

    private Calendar getFirstLabelTime(long startTime, Zoom zoom) {
        Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        time.setTimeInMillis(startTime);
        switch (zoom.getLabel()) {
            case SECOND -> {
                time.set(Calendar.MILLISECOND, 0);
                time.set(Calendar.SECOND, 0);
            }
            case MINUTE -> {
                time.set(Calendar.MILLISECOND, 0);
                time.set(Calendar.SECOND, 0);
                time.set(Calendar.MINUTE, 0);
            }
            case HOUR, DAY -> {
                time.set(Calendar.MILLISECOND, 0);
                time.set(Calendar.SECOND, 0);
                time.set(Calendar.MINUTE, 0);
                time.set(Calendar.HOUR_OF_DAY, 0);
            }
            case WEEK, YEAR -> {
                time.set(Calendar.MILLISECOND, 0);
                time.set(Calendar.SECOND, 0);
                time.set(Calendar.MINUTE, 0);
                time.set(Calendar.HOUR_OF_DAY, 0);
                time.set(Calendar.DAY_OF_YEAR, 1);
            }
            default -> Logger.getGlobal().log(Level.WARNING, "INVALID Zoom");
        }
        return time;
    }

    private String getLabelText(Calendar time, Zoom zoom) {
        return switch (zoom.getLabel()) {
            case SECOND -> String.format("%02d", time.get(Calendar.SECOND));
            case MINUTE, HOUR -> String.format("%02d:%02d:%02d", time.get(Calendar.HOUR_OF_DAY),
                    time.get(Calendar.MINUTE), time.get(Calendar.SECOND));
            case DAY, WEEK -> String.format("%02d/%02d/%04d", time.get(Calendar.MONTH) + 1,
                    time.get(Calendar.DAY_OF_MONTH), time.get(Calendar.YEAR));
            case YEAR -> String.format("%04d", time.get(Calendar.MONTH) > 6 ? time.get(Calendar.YEAR) + 1 : time.get(Calendar.YEAR));
        };
    }

    @Override
    public double getDistanceFromEye() {
        return 0;
    }
}
