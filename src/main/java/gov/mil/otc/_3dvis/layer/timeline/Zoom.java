package gov.mil.otc._3dvis.layer.timeline;

public class Zoom {

    public enum Label {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        YEAR
    }

    private static final Zoom[] ZOOM_VALUES = {
            new Zoom(Label.SECOND, 1000, 200, 20),
            new Zoom(Label.SECOND, 1000, 100, 10),
            new Zoom(Label.SECOND, 2000, 100, 10),
            new Zoom(Label.SECOND, 5000, 100, 10),
            new Zoom(Label.SECOND, 10000, 100, 10),
            new Zoom(Label.MINUTE, 60000, 200, 10),
            new Zoom(Label.MINUTE, 60000, 100, 10),
            new Zoom(Label.MINUTE, 300000, 200, 10),
            new Zoom(Label.MINUTE, 300000, 100, 10),
            new Zoom(Label.HOUR, 3600000, 180, 6),
            new Zoom(Label.HOUR, 3600000, 90, 6),
            new Zoom(Label.HOUR, 14400000, 180, 6),
            new Zoom(Label.DAY, 86400000, 192, 8),
            new Zoom(Label.DAY, 86400000, 96, 8),
            new Zoom(Label.WEEK, 604800000, 175, 25),
            new Zoom(Label.WEEK, 604800000, 105, 15),
            new Zoom(Label.WEEK, 1209600000, 140, 20),
            new Zoom(Label.YEAR, 31536000000L, 100, 20),
    };

    private static int currentIndex = 6;

    private final Label label;
    private final long labelWidthInMillis;
    private final long labelWidthInPixels;
    private final long tickWidth;
    private final long millisPerPixel;
    private final long millisPerTick;

    private Zoom(Label label, long labelWidthInMillis, long labelWidthInPixels, long tickWidth) {
        this.label = label;
        this.labelWidthInMillis = labelWidthInMillis;
        this.labelWidthInPixels = labelWidthInPixels;
        this.tickWidth = tickWidth;
        millisPerPixel = labelWidthInMillis / labelWidthInPixels;
        millisPerTick = tickWidth * millisPerPixel;
    }

    protected static void zoomIn() {
        synchronized (ZOOM_VALUES) {
            if (currentIndex > 0) {
                currentIndex--;
            }
        }
    }

    protected static void zoomOut() {
        synchronized (ZOOM_VALUES) {
            if (currentIndex < ZOOM_VALUES.length - 1) {
                currentIndex++;
            }
        }
    }

    protected static Zoom getZoom() {
        synchronized (ZOOM_VALUES) {
            return ZOOM_VALUES[currentIndex];
        }
    }

    protected Label getLabel() {
        return label;
    }

    protected long getLabelWidthInMillis() {
        return labelWidthInMillis;
    }

    protected long getLabelWidthInPixels() {
        return labelWidthInPixels;
    }

    protected long getTickWidth() {
        return tickWidth;
    }

    protected long getMillisPerPixel() {
        return millisPerPixel;
    }

    protected long getMillisPerTick() {
        return millisPerTick;
    }
}
