package gov.mil.otc._3dvis.datamodel;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.tena.TenaUtility;
import gov.nasa.worldwind.geom.Position;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TspiData extends TimedData {

    private final Position position;
    private final Double forwardVelocity;
    private final Double verticalVelocity;
    private final Double headingInDegrees;
    private final Double pitchInDegrees;
    private final Double rollInDegrees;
    private final boolean isRecovered;

    public TspiData(long timestamp,
                    Position position) {
        this(timestamp, position, null, null, null, null, null, false);
    }

    public TspiData(long timestamp,
                    Position position,
                    boolean isRecovered) {
        this(timestamp, position, null, null, null, null, null, isRecovered);
    }

    public TspiData(long timestamp,
                    Position position,
                    Double forwardVelocity,
                    Double heading,
                    boolean isRecovered) {
        this(timestamp, position, forwardVelocity, null, heading, null, null, isRecovered);
    }

    public TspiData(long timestamp,
                    Position position,
                    Double forwardVelocity,
                    Double verticalVelocity,
                    Double headingInDegrees,
                    Double pitchInDegrees,
                    Double rollInDegrees) {
        this(timestamp, position, forwardVelocity, verticalVelocity, headingInDegrees, pitchInDegrees, rollInDegrees, false);
    }

    public TspiData(long timestamp,
                    Position position,
                    Double forwardVelocity,
                    Double verticalVelocity,
                    Double headingInDegrees,
                    Double pitchInDegrees,
                    Double rollInDegrees,
                    boolean isRecovered) {
        super(timestamp);
        this.position = position;
        this.forwardVelocity = forwardVelocity;
        this.verticalVelocity = verticalVelocity;
        this.headingInDegrees = headingInDegrees;
        this.pitchInDegrees = pitchInDegrees;
        this.rollInDegrees = rollInDegrees;
        this.isRecovered = isRecovered;
    }

    public TspiData(TspiData tspiData) {
        super(tspiData.getTimestamp());
        this.position = tspiData.position;
        this.forwardVelocity = tspiData.forwardVelocity;
        this.verticalVelocity = tspiData.verticalVelocity;
        this.headingInDegrees = tspiData.headingInDegrees;
        this.pitchInDegrees = tspiData.pitchInDegrees;
        this.rollInDegrees = tspiData.rollInDegrees;
        this.isRecovered = tspiData.isRecovered;
    }

    public static TspiData create(TENA.TSPI.ImmutableLocalClass tspi) {
        Position position = TenaUtility.convertTenaPosition(tspi.get_position());
        if (position != null) {
            return new TspiData(tspi.get_time().get_nanosecondsSince1970() / TenaUtility.MILLI_NANO, position);
        } else {
            return null;
        }
    }

    public Position getPosition() {
        return position;
    }

    public Double getForwardVelocity() {
        return forwardVelocity;
    }

    public Double getVerticalVelocity() {
        return verticalVelocity;
    }

    public Double getHeading() {
        return headingInDegrees;
    }

    public Double getPitch() {
        return pitchInDegrees;
    }

    public Double getRoll() {
        return rollInDegrees;
    }

    public boolean isRecovered() {
        return isRecovered;
    }

    public boolean isOrientationValid() {
        return headingInDegrees != null && pitchInDegrees != null && rollInDegrees != null;
    }

    public TENA.TSPI.ImmutableLocalClass toTenaTspi() {
        try {
            return TENA.TSPI.LocalClass.create(
                    TENA.Time.LocalClass.create(getTimestamp() * TenaUtility.MILLI_NANO),
                    TENA.Position.LocalClass.create(
                            TENA.GeodeticPosition.LocalClass.create(
                                    TENA.GeodeticSRF.LocalClass.create(),
                                    getPosition().getLatitude().degrees,
                                    getPosition().getLongitude().degrees,
                                    getPosition().getAltitude())));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return TenaUtility.createUnknownTspi();
    }
}
