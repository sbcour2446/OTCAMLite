package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class NbcrvState extends TimedData {

    private final double latitude;
    private final double longitude;
    private final double roll;
    private final double pitch;
    private final double yaw;
    private final double ptuYaw;
    private final double imcadTilt;
    private final double imcadAngle;
    private final double imcadWidth;
    private final double csdsTilt;
    private final double csdsAngle;
    private final double csdsWidth;

    public NbcrvState(long timestamp, double latitude, double longitude, double roll, double pitch, double yaw,
                         double ptuYaw, double imcadTilt, double imcadAngle, double imcadWidth,
                         double csdsTilt, double csdsAngle, double csdsWidth) {
        super(timestamp);
        this.latitude = latitude;
        this.longitude = longitude;
        this.roll = roll;
        this.pitch = pitch;
        this.yaw = yaw;
        this.ptuYaw = ptuYaw;
        this.imcadTilt = imcadTilt;
        this.imcadAngle = imcadAngle;
        this.imcadWidth = imcadWidth;
        this.csdsTilt = csdsTilt;
        this.csdsAngle = csdsAngle;
        this.csdsWidth = csdsWidth;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getRoll() {
        return roll;
    }

    public double getPitch() {
        return pitch;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPtuYaw() {
        return ptuYaw;
    }

    public double getImcadTilt() {
        return imcadTilt;
    }

    public double getImcadAngle() {
        return imcadAngle;
    }

    public double getImcadWidth() {
        return imcadWidth;
    }

    public double getCsdsTilt() {
        return csdsTilt;
    }

    public double getCsdsAngle() {
        return csdsAngle;
    }

    public double getCsdsWidth() {
        return csdsWidth;
    }
}
