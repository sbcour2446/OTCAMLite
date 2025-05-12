package gov.mil.otc._3dvis.event;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.nasa.worldwind.layers.RenderableLayer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public abstract class Event extends TimedData {

    protected static final String DELIMITER = " | ";
    protected static final int DEFAULT_TIMEOUT = 30000;
    protected long duration = DEFAULT_TIMEOUT;
    protected final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Common.DATE_TIME_WITH_MILLIS);
    protected final long endTime;
    protected boolean isVisible = false;
    protected String type;

    protected Event(long eventTime) {
        super(eventTime);
        setType();
        this.endTime = eventTime + duration;
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    protected Event(long eventTime, long endTime) {
        super(eventTime);
        this.endTime = endTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long millis) {
        duration = millis;
    }

    public boolean isActive(long time) {
        if (endTime <= 0) {
            return time >= getTimestamp() && time <= getTimestamp() + duration;
        } else {
            return time >= getTimestamp() && time <= endTime;
        }
    }

    public String getType() {
        return type;
    }

    protected abstract void setType();

    public abstract String getDescription();

    public abstract void update(long time, RenderableLayer layer);

    public abstract void dispose(RenderableLayer layer);
}
