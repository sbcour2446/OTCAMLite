package gov.mil.otc._3dvis.settings;

import gov.nasa.worldwind.geom.Angle;

public class DlmSettings {

    private static final double RADAR_RADIUS = 150;
    private static final double LAUNCH_RADIUS = 50;
    private static final double LAUNCH_CONE_RADIUS = 10;
    private static final double LAUNCH_CONE_HEIGHT = 30;
    private static final int LAUNCH_DISPLAY_TIME = 5000;
    private static final long RANGE_DISPLAY_TIME = 1000;
    private static final double TARGET_RADIUS = 5;
    private static final double TARGET_HEIGHT = 1;
    private static final long TARGET_DISPLAY_TIME = 2000;
    private static final Angle TRACK_LINE_LENGTH = Angle.fromRadians(1 / 6371.0);
    private static final long TRACK_DISPLAY_TIME = 10000;

    private Double radarRadius;
    private Double launchRadius;
    private Double launchConeRadius;
    private Double launchConeHeight;
    private Integer launchDisplayTime;
    private Long rangeDisplayTime;
    private Double targetRadius;
    private Double targetHeight;
    private Long targetDisplayTime;
    private Angle trackLineLength;
    private Long trackDisplayTime;

    public double getRadarRadius() {
        if (radarRadius == null) {
            radarRadius = RADAR_RADIUS;
        }
        return radarRadius;
    }

    public void setRadarRadius(double radarRadius) {
        this.radarRadius = radarRadius;
    }

    public double getLaunchRadius() {
        if (launchRadius == null) {
            launchRadius = LAUNCH_RADIUS;
        }
        return launchRadius;
    }

    public void setLaunchRadius(double launchRadius) {
        this.launchRadius = launchRadius;
    }

    public double getLaunchConeRadius() {
        if (launchConeRadius == null) {
            launchConeRadius = LAUNCH_CONE_RADIUS;
        }
        return launchConeRadius;
    }

    public void setLaunchConeRadius(double launchConeRadius) {
        this.launchConeRadius = launchConeRadius;
    }

    public double getLaunchConeHeight() {
        if (launchConeHeight == null) {
            launchConeHeight = LAUNCH_CONE_HEIGHT;
        }
        return launchConeHeight;
    }

    public void setLaunchConeHeight(double launchConeHeight) {
        this.launchConeHeight = launchConeHeight;
    }

    public int getLaunchDisplayTime() {
        if (launchDisplayTime == null) {
            launchDisplayTime = LAUNCH_DISPLAY_TIME;
        }
        return launchDisplayTime;
    }

    public void setLaunchDisplayTime(int launchDisplayTime) {
        this.launchDisplayTime = launchDisplayTime;
    }

    public long getRangeDisplayTime() {
        if (rangeDisplayTime == null) {
            rangeDisplayTime = RANGE_DISPLAY_TIME;
        }
        return rangeDisplayTime;
    }

    public void setRangeDisplayTime(long rangeDisplayTime) {
        this.rangeDisplayTime = rangeDisplayTime;
    }

    public double getTargetRadius() {
        if (targetRadius == null) {
            targetRadius = TARGET_RADIUS;
        }
        return targetRadius;
    }

    public void setTargetRadius(double targetRadius) {
        this.targetRadius = targetRadius;
    }

    public double getTargetHeight() {
        if (targetHeight == null) {
            targetHeight = TARGET_HEIGHT;
        }
        return targetHeight;
    }

    public void setTargetHeight(double targetHeight) {
        this.targetHeight = targetHeight;
    }

    public long getTargetDisplayTime() {
        if (targetDisplayTime == null) {
            targetDisplayTime = TARGET_DISPLAY_TIME;
        }
        return targetDisplayTime;
    }

    public void setTargetDisplayTime(long targetDisplayTime) {
        this.targetDisplayTime = targetDisplayTime;
    }

    public Angle getTrackLineLength() {
        if (trackLineLength == null) {
            trackLineLength = TRACK_LINE_LENGTH;
        }
        return trackLineLength;
    }

    public void setTrackLineLength(Angle trackLineLength) {
        this.trackLineLength = trackLineLength;
    }

    public long getTrackDisplayTime() {
        if (trackDisplayTime == null) {
            trackDisplayTime = TRACK_DISPLAY_TIME;
        }
        return trackDisplayTime;
    }

    public void setTrackDisplayTime(long trackDisplayTime) {
        this.trackDisplayTime = trackDisplayTime;
    }
}
