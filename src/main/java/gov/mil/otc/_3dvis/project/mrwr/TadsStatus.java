package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class TadsStatus extends TimedData {

    private final double azimuth;
    private final double elevation;
    private final int range;
    private final int fov;

    protected TadsStatus(long timestamp, double azimuth, double elevation, int range, int fov) {
        super(timestamp);
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.range = range;
        this.fov = fov;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getElevation() {
        return elevation;
    }

    public int getRange() {
        return range;
    }

    public int getFov() {
        return fov;
    }
}
