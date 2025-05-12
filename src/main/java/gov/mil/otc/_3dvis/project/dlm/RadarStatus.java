package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class RadarStatus extends TimedData {

    private final boolean isOn;

    protected RadarStatus(long timestamp, boolean isOn) {
        super(timestamp);
        this.isOn = isOn;
    }

    public boolean isOn() {
        return isOn;
    }
}
