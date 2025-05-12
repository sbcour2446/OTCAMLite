package gov.mil.otc._3dvis.datamodel.aircraft;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class UasPayloadData extends TimedData {

    private final int sysOpModeState;
    private final int eoCameraStatus;
    private final int irPolarityStatus;
    private final int imageOutputState;
    private final double actCenterElAngle;
    private final double actVertFieldOfView;
    private final double actCenterAzAngle;
    private final double actHorFieldOfView;
    private final double actSensorRotAngle;
    private final boolean imagePosition;
    private final double latitude;
    private final double longitude;
    private final double altitude;
    private final double reportedRange;
    private final int preplanMode;
    private final int fLaserPointerStatus;
    private final int selLaserRangeFirstLast;
    private final int laserDesignatorCode;
    private final int laserDesignatorStatus;
    private final int pointingModeState;

    private UasPayloadData(long timestamp,
                           int sysOpModeState, int eoCameraStatus, int irPolarityStatus, int imageOutputState,
                           double actCenterElAngle, double actVertFieldOfView, double actCenterAzAngle,
                           double actHorFieldOfView, double actSensorRotAngle,
                           boolean imagePosition,
                           double latitude, double longitude, double altitude, double reportedRange,
                           int preplanMode, int fLaserPointerStatus, int selLaserRangeFirstLast,
                           int laserDesignatorCode, int laserDesignatorStatus, int pointingModeState
    ) {
        super(timestamp);
        this.sysOpModeState = sysOpModeState;
        this.eoCameraStatus = eoCameraStatus;
        this.irPolarityStatus = irPolarityStatus;
        this.imageOutputState = imageOutputState;
        this.actCenterElAngle = actCenterElAngle;
        this.actVertFieldOfView = actVertFieldOfView;
        this.actCenterAzAngle = actCenterAzAngle;
        this.actHorFieldOfView = actHorFieldOfView;
        this.actSensorRotAngle = actSensorRotAngle;
        this.imagePosition = imagePosition;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.reportedRange = reportedRange;
        this.preplanMode = preplanMode;
        this.fLaserPointerStatus = fLaserPointerStatus;
        this.selLaserRangeFirstLast = selLaserRangeFirstLast;
        this.laserDesignatorCode = laserDesignatorCode;
        this.laserDesignatorStatus = laserDesignatorStatus;
        this.pointingModeState = pointingModeState;
    }

    public int getSysOpModeState() {
        return sysOpModeState;
    }

    public int getEoCameraStatus() {
        return eoCameraStatus;
    }

    public int getIrPolarityStatus() {
        return irPolarityStatus;
    }

    public int getImageOutputState() {
        return imageOutputState;
    }

    public double getActCenterElAngle() {
        return actCenterElAngle;
    }

    public double getActVertFieldOfView() {
        return actVertFieldOfView;
    }

    public double getActCenterAzAngle() {
        return actCenterAzAngle;
    }

    public double getActHorFieldOfView() {
        return actHorFieldOfView;
    }

    public double getActSensorRotAngle() {
        return actSensorRotAngle;
    }

    public boolean isImagePosition() {
        return imagePosition;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public int getPointingModeState() {
        return pointingModeState;
    }

    public int getPreplanMode() {
        return preplanMode;
    }

    public double getReportedRange() {
        return reportedRange;
    }

    public int getfLaserPointerStatus() {
        return fLaserPointerStatus;
    }

    public int getSelLaserRangeFirstLast() {
        return selLaserRangeFirstLast;
    }

    public int getLaserDesignatorCode() {
        return laserDesignatorCode;
    }

    public int getLaserDesignatorStatus() {
        return laserDesignatorStatus;
    }

    public static class Builder {

        private long timestamp;
        private int sysOpModeState;
        private int eoCameraStatus;
        private int irPolarityStatus;
        private int imageOutputState;
        private double actCenterElAngle;
        private double actVertFieldOfView;
        private double actCenterAzAngle;
        private double actHorFieldOfView;
        private double actSensorRotAngle;
        private boolean imagePosition;
        private double latitude;
        private double longitude;
        private double altitude;
        private double reportedRange;
        private int preplanMode;
        private int fLaserPointerStatus;
        private int selLaserRangeFirstLast;
        private int laserDesignatorCode;
        private int laserDesignatorStatus;
        private int pointingModeState;

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setSysOpModeState(int sysOpModeState) {
            this.sysOpModeState = sysOpModeState;
            return this;
        }

        public Builder setEoCameraStatus(int eoCameraStatus) {
            this.eoCameraStatus = eoCameraStatus;
            return this;
        }

        public Builder setIrPolarityStatus(int irPolarityStatus) {
            this.irPolarityStatus = irPolarityStatus;
            return this;
        }

        public Builder setImageOutputState(int imageOutputState) {
            this.imageOutputState = imageOutputState;
            return this;
        }

        public Builder setActCenterElAngle(double actCenterElAngle) {
            this.actCenterElAngle = actCenterElAngle;
            return this;
        }

        public Builder setActVertFieldOfView(double actVertFieldOfView) {
            this.actVertFieldOfView = actVertFieldOfView;
            return this;
        }

        public Builder setActCenterAzAngle(double actCenterAzAngle) {
            this.actCenterAzAngle = actCenterAzAngle;
            return this;
        }

        public Builder setActHorFieldOfView(double actHorFieldOfView) {
            this.actHorFieldOfView = actHorFieldOfView;
            return this;
        }

        public Builder setActSensorRotAngle(double actSensorRotAngle) {
            this.actSensorRotAngle = actSensorRotAngle;
            return this;
        }

        public Builder setImagePosition(boolean imagePosition) {
            this.imagePosition = imagePosition;
            return this;
        }

        public Builder setLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder setLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder setAltitude(double altitude) {
            this.altitude = altitude;
            return this;
        }

        public Builder setReportedRange(double reportedRange) {
            this.reportedRange = reportedRange;
            return this;
        }

        public Builder setPreplanMode(int preplanMode) {
            this.preplanMode = preplanMode;
            return this;
        }

        public Builder setfLaserPointerStatus(int fLaserPointerStatus) {
            this.fLaserPointerStatus = fLaserPointerStatus;
            return this;
        }

        public Builder setSelLaserRangeFirstLast(int selLaserRangeFirstLast) {
            this.selLaserRangeFirstLast = selLaserRangeFirstLast;
            return this;
        }

        public Builder setLaserDesignatorCode(int laserDesignatorCode) {
            this.laserDesignatorCode = laserDesignatorCode;
            return this;
        }

        public Builder setLaserDesignatorStatus(int laserDesignatorStatus) {
            this.laserDesignatorStatus = laserDesignatorStatus;
            return this;
        }

        public Builder setPointingModeState(int pointingModeState) {
            this.pointingModeState = pointingModeState;
            return this;
        }

        public UasPayloadData build() {
            return new UasPayloadData(timestamp,
                    sysOpModeState, eoCameraStatus, irPolarityStatus, imageOutputState,
                    actCenterElAngle, actVertFieldOfView, actCenterAzAngle,
                    actHorFieldOfView, actSensorRotAngle,
                    imagePosition,
                    latitude, longitude, altitude, reportedRange,
                    preplanMode, fLaserPointerStatus, selLaserRangeFirstLast,
                    laserDesignatorCode, laserDesignatorStatus, pointingModeState);
        }
    }
}
