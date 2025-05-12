package gov.mil.otc._3dvis.datamodel.aircraft;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class TspiExtendedData extends TimedData {

    private final double altitudeType;
    private final double uSpeed;
    private final double vSpeed;
    private final double wSpeed;
    private final double uAcceleration;
    private final double vAcceleration;
    private final double wAcceleration;
    private final double phi;
    private final double theta;
    private final double psi;
    private final double phiDot;
    private final double thetaDot;
    private final double psiDot;

    private TspiExtendedData(long timestamp, double altitudeType,
                             double uSpeed, double vSpeed, double wSpeed,
                             double uAcceleration, double vAcceleration, double wAcceleration,
                             double phi, double theta, double psi,
                             double phiDot, double thetaDot, double psiDot) {
        super(timestamp);
        this.altitudeType = altitudeType;
        this.uSpeed = uSpeed;
        this.vSpeed = vSpeed;
        this.wSpeed = wSpeed;
        this.uAcceleration = uAcceleration;
        this.vAcceleration = vAcceleration;
        this.wAcceleration = wAcceleration;
        this.phi = phi;
        this.theta = theta;
        this.psi = psi;
        this.phiDot = phiDot;
        this.thetaDot = thetaDot;
        this.psiDot = psiDot;
    }

    public double getAltitudeType() {
        return altitudeType;
    }

    public double getUSpeed() {
        return uSpeed;
    }

    public double getVSpeed() {
        return vSpeed;
    }

    public double getWSpeed() {
        return wSpeed;
    }

    public double getUAcceleration() {
        return uAcceleration;
    }

    public double getVAcceleration() {
        return vAcceleration;
    }

    public double getWAcceleration() {
        return wAcceleration;
    }

    public double getPhi() {
        return phi;
    }

    public double getTheta() {
        return theta;
    }

    public double getPsi() {
        return psi;
    }

    public double getPhiDot() {
        return phiDot;
    }

    public double getThetaDot() {
        return thetaDot;
    }

    public double getPsiDot() {
        return psiDot;
    }

    public static class Builder {

        private long timestamp;
        private double altitudeType;
        private double uSpeed;
        private double vSpeed;
        private double wSpeed;
        private double uAcceleration;
        private double vAcceleration;
        private double wAcceleration;
        private double phi;
        private double theta;
        private double psi;
        private double phiDot;
        private double thetaDot;
        private double psiDot;

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setAltitudeType(double altitudeType) {
            this.altitudeType = altitudeType;
            return this;
        }

        public Builder setUSpeed(double uSpeed) {
            this.uSpeed = uSpeed;
            return this;
        }

        public Builder setVSpeed(double vSpeed) {
            this.vSpeed = vSpeed;
            return this;
        }

        public Builder setWSpeed(double wSpeed) {
            this.wSpeed = wSpeed;
            return this;
        }

        public Builder setUAcceleration(double uAcceleration) {
            this.uAcceleration = uAcceleration;
            return this;
        }

        public Builder setVAcceleration(double vAcceleration) {
            this.vAcceleration = vAcceleration;
            return this;
        }

        public Builder setWAcceleration(double wAcceleration) {
            this.wAcceleration = wAcceleration;
            return this;
        }

        public Builder setPhi(double phi) {
            this.phi = phi;
            return this;
        }

        public Builder setTheta(double theta) {
            this.theta = theta;
            return this;
        }

        public Builder setPsi(double psi) {
            this.psi = psi;
            return this;
        }

        public Builder setPhiDot(double phiDot) {
            this.phiDot = phiDot;
            return this;
        }

        public Builder setThetaDot(double thetaDot) {
            this.thetaDot = thetaDot;
            return this;
        }

        public Builder setPsiDot(double psiDot) {
            this.psiDot = psiDot;
            return this;
        }

        public TspiExtendedData build() {
            return new TspiExtendedData(timestamp, altitudeType,
                    uSpeed, vSpeed, wSpeed,
                    uAcceleration, vAcceleration, wAcceleration,
                    phi, theta, psi,
                    phiDot, thetaDot, psiDot);
        }
    }
}
