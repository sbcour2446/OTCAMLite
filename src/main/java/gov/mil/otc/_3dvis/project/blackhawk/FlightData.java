package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class FlightData extends TimedData {

    private final double pressureAltitude;
    private final double radarAltitude;
    private final double heading;
    private final double airspeed;
    private final String wheelStatus;

    public FlightData(long timestamp, double pressureAltitude, double radarAltitude,
                      double heading, double airspeed, String wheelStatus) {
        super(timestamp);
        this.pressureAltitude = pressureAltitude;
        this.radarAltitude = radarAltitude;
        this.heading = heading;
        this.airspeed = airspeed;
        this.wheelStatus = wheelStatus;
    }

    public double getPressureAltitude() {
        return pressureAltitude;
    }

    public double getRadarAltitude() {
        return radarAltitude;
    }

    public double getHeading() {
        return heading;
    }

    public double getAirspeed() {
        return airspeed;
    }

    public String getWheelStatus() {
        return wheelStatus;
    }
}
