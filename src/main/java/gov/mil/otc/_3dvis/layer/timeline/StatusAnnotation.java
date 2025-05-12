package gov.mil.otc._3dvis.layer.timeline;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.awt.TextRenderer;
import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.settings.UnitPreference;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.time.TimeRate;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.OrderedRenderable;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.util.OGLStackHandler;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static gov.mil.otc._3dvis.layer.Constant.*;

public class StatusAnnotation extends ScreenAnnotation implements OrderedRenderable {

    private static final TextRenderer TEXT_RENDERER_DATE = new TextRenderer(Common.FONT_MEDIUM);
    private static final TextRenderer TEXT_RENDERER_TIME = new TextRenderer(Common.FONT_XLARGE_BOLD);
    private final SimpleDateFormat simpleDateFormatDate = new SimpleDateFormat("MMM dd yyyy");
    private final SimpleDateFormat simpleDateFormatTime = new SimpleDateFormat("HH:mm:ss.SSS");
    private final SimpleDateFormat simpleDateFormatTimeLocal = new SimpleDateFormat("HH:mm zzz");
    private double stabilizedTimeWidth = 0;
    private double stabilizedDateWidth = 0;
    private double stabilizedDateHeight = 0;
    private long timeInMillis = 0;
    private boolean initialized = false;
    private boolean showLocalTimeZone = false;


    public StatusAnnotation() {
        super("", new Point());
    }

    public void initialize() {
        if (!initialized) {
            initialized = true;

            simpleDateFormatDate.setTimeZone(TimeZone.getTimeZone("UTC"));
            simpleDateFormatTime.setTimeZone(TimeZone.getTimeZone("UTC"));

            getAttributes().setCornerRadius(0);
            getAttributes().setFrameShape(AVKey.SHAPE_RECTANGLE);
            getAttributes().setLeader(AVKey.SHAPE_NONE);
            getAttributes().setOpacity(0);

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(timeInMillis);

            TEXT_RENDERER_DATE.begin3DRendering();
            Rectangle2D bounds = TEXT_RENDERER_DATE.getBounds(simpleDateFormatDate.format(calendar.getTime()));
            stabilizedDateWidth = bounds.getWidth() + 1;
            stabilizedDateHeight = bounds.getHeight() + 1;
            TEXT_RENDERER_DATE.end3DRendering();

            TEXT_RENDERER_TIME.begin3DRendering();
            bounds = TEXT_RENDERER_TIME.getBounds(simpleDateFormatTime.format(calendar.getTime()));
            stabilizedTimeWidth = bounds.getWidth() + 1;
            TEXT_RENDERER_TIME.end3DRendering();
        }
    }

    public void setTime(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public void setShowLocalTimeZone(boolean value) {
        showLocalTimeZone = value;
    }

    public void setLocalTimeZone(TimeZone value) {
        simpleDateFormatTimeLocal.setTimeZone(value);
    }

    @Override
    public void render(DrawContext dc) {
        renderClock(dc);
        if (dc.getView().getViewport().getWidth() > TIMELINE_INSET * 3) {
            renderStatus(dc);
            renderPosition(dc);
        }
    }

    private void renderPosition(DrawContext dc) {
        Position currentPosition = getCurrentPosition(dc);
        if (currentPosition == null) {
            currentPosition = dc.getViewportCenterPosition();
        }
        if (currentPosition != null) {
            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            OGLStackHandler ogsh = new OGLStackHandler();

            int attrMask = GL.GL_DEPTH_BUFFER_BIT // for depth test enable, depth func, depth mask
                    | GL.GL_COLOR_BUFFER_BIT // for alpha test enable, alpha func, blend enable, blend func
                    | GL2.GL_CURRENT_BIT; // for current color

            try {
                ogsh.pushAttrib(gl, attrMask);
                gl.glDisable(GL.GL_DEPTH_TEST);

                Rectangle viewport = dc.getView().getViewport();
                ogsh.pushProjectionIdentity(gl);
                gl.glOrtho(0d, viewport.width, 0d, viewport.height, -1, 1);
                ogsh.pushModelviewIdentity(gl);

                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

                String coordinatesText = "";
                UnitPreference.PositionUnit positionUnit = SettingsManager.getPreferences().getUnitPreference().getPositionUnit();
                if (positionUnit == UnitPreference.PositionUnit.LAT_LON_DD) {
                    coordinatesText = String.format("%3.6f\u00b0, %3.6f\u00b0, %,dm",
                            currentPosition.getLatitude().degrees, currentPosition.getLongitude().degrees,
                            (int) Math.round(currentPosition.getElevation()));
                } else if (positionUnit == UnitPreference.PositionUnit.MGRS) {
                    coordinatesText = String.format("%s, %,dm",
                            MGRSCoord.fromLatLon(currentPosition.getLatitude(), currentPosition.getLongitude()),
                            (int) Math.round(currentPosition.getElevation()));
                }

                TEXT_RENDERER_DATE.begin3DRendering();
                TEXT_RENDERER_DATE.setColor(new Color(0x66aaff));
                Rectangle2D bounds = TEXT_RENDERER_DATE.getBounds(coordinatesText);
                int x = viewport.width - (int) bounds.getWidth() - PADDING * 2;
                TEXT_RENDERER_DATE.draw(coordinatesText, x, PADDING * 2);
                TEXT_RENDERER_DATE.end3DRendering();
            } finally {
                gl.glEnable(GL.GL_DEPTH_TEST);
                ogsh.pop(gl);
            }
        }
    }

    private Position getCurrentPosition(DrawContext dc) {
        if (dc.getPickedObjects() == null) {
            return null;
        }

        PickedObject po = dc.getPickedObjects().getTerrainObject();
        return po != null ? po.getPosition() : null;
    }

    @Override
    public double getDistanceFromEye() {
        return 0;
    }

    protected void renderClock(DrawContext dc) {
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

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(timeInMillis);

            int x = (int) (dc.getView().getViewport().width / 2.0 - stabilizedDateWidth / 2.0);
            int y = PADDING * 2;
            TEXT_RENDERER_DATE.begin3DRendering();
            TEXT_RENDERER_DATE.setColor(Color.YELLOW);
            TEXT_RENDERER_DATE.draw(simpleDateFormatDate.format(calendar.getTime()), x, y);
            TEXT_RENDERER_DATE.end3DRendering();

            x = (int) (dc.getView().getViewport().width / 2.0 - stabilizedTimeWidth / 2.0);
            y += stabilizedDateHeight + PADDING;
            TEXT_RENDERER_TIME.begin3DRendering();
            TEXT_RENDERER_TIME.setColor(Color.WHITE);
            TEXT_RENDERER_TIME.draw(simpleDateFormatTime.format(calendar.getTime()), x, y);
            TEXT_RENDERER_TIME.end3DRendering();

            if (showLocalTimeZone) {
                x = (int) (dc.getView().getViewport().width / 2.0 - stabilizedTimeWidth * 2.0);
                TEXT_RENDERER_TIME.begin3DRendering();
                TEXT_RENDERER_TIME.setColor(Color.WHITE);
                TEXT_RENDERER_TIME.draw(simpleDateFormatTimeLocal.format(calendar.getTime()), x, y);
                TEXT_RENDERER_TIME.end3DRendering();
            }
        } finally {
            gl.glEnable(GL.GL_DEPTH_TEST);
            stackHandler.pop(gl);
        }
    }

    protected void renderStatus(DrawContext dc) {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        OGLStackHandler ogsh = new OGLStackHandler();

        int attrMask = GL.GL_DEPTH_BUFFER_BIT // for depth test enable, depth func, depth mask
                | GL.GL_COLOR_BUFFER_BIT // for alpha test enable, alpha func, blend enable, blend func
                | GL2.GL_CURRENT_BIT; // for current color

        try {
            ogsh.pushAttrib(gl, attrMask);
            gl.glDisable(GL.GL_DEPTH_TEST);

            Rectangle viewport = dc.getView().getViewport();
            ogsh.pushProjectionIdentity(gl);
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -1, 1);
            ogsh.pushModelviewIdentity(gl);

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

            TEXT_RENDERER_DATE.begin3DRendering();
            TEXT_RENDERER_DATE.setColor(new Color(0x66aaff));
            String status = getStatusText();
            Rectangle2D bounds = TEXT_RENDERER_DATE.getBounds(status);
            int x = TIMELINE_INSET / 2 - (int) bounds.getWidth() / 2;
            int y = (TIMELINE_TOP - (int) bounds.getHeight() - PADDING);
            TEXT_RENDERER_DATE.draw(status, x, y);
            TEXT_RENDERER_DATE.end3DRendering();
        } finally {
            gl.glEnable(GL.GL_DEPTH_TEST);
            ogsh.pop(gl);
        }
    }

    private static String getStatusText() {
        String status;
        if (TimeManager.isLive()) {
            status = "Live";
        } else {
            if (TimeManager.isPaused()) {
                status = "Paused";
            } else {
                status = "Replay";
            }
            if (TimeManager.getRate() != TimeRate.SPEED_NORMAL) {
                status += " (" + TimeManager.getRate().toString() + ")";
            }
        }
        return status;
    }
}
