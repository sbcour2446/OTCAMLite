package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class TargetTimedData extends TimedData {

    private final Double azimuth;
    private final Double elevation;
    private final int range;

    protected TargetTimedData(long timestamp, Double azimuth, Double elevation, Integer range) {
        super(timestamp);
        this.azimuth = azimuth;
        this.elevation = elevation;
        if (range == null) {
            this.range = 0;
        } else {
            this.range = range;
        }
    }

    public Double getAzimuth() {
        return azimuth;
    }

    public Double getElevation() {
        return elevation;
    }

    public int getRange() {
        return range;
    }
}
