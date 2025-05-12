package gov.mil.otc._3dvis.data.tapets;

import gov.mil.otc._3dvis.data.gps.GpsLeapSecond;
import gov.mil.otc._3dvis.data.miles.MilesMessage;

import java.nio.ByteBuffer;

public class TapetsRecord {

    public static final long MILLISECONDS_PER_WEEK = 604800000L;
    public static final long GPS_EPOCH_OFFSET = 315964800000L + MILLISECONDS_PER_WEEK * 1024;//315964800000l;

    private int unitId;
    private int milesPid;
    private int milesType;
    private int messageId;
    private int milesPid_payload;
    private long timestamp;
    private boolean solutionValid;
    private boolean headingValid;
    private boolean pitchValid;
    private boolean rollValid;
    private boolean speedValid;
    private boolean stale;
    private boolean accelerationValid;
    private int txRecord;
    private int rtca;
    private boolean recorderOn;
    private int prnCount;
    private int solution;
    private int method;
    private int kalman;
    private double x;
    private double y;
    private double z;
    private int gdop;
    private int heading;
    private int pitch;
    private int roll;
    private int speed;
    private int velocityX;
    private int velocityY;
    private int velocityZ;
    private int reportType;
    private double reportValue;
    private MilesMessage milesMessage = new MilesMessage();

    public void deserialize(ByteBuffer buffer) {
        buffer.get();//'$'
        buffer.get();//'E'
        unitId = buffer.getShort();
        milesPid = buffer.getShort();
        milesType = buffer.getShort();
        buffer.get();//0x9c
        buffer.get();//0xA5
        buffer.getShort();//bytecount
        messageId = buffer.get();
        milesPid_payload = buffer.getShort();
        int weeks = buffer.getShort() & 0xFFFF;
        long millis = buffer.getInt();
        timestamp = GPS_EPOCH_OFFSET + weeks * MILLISECONDS_PER_WEEK + millis;
        timestamp += GpsLeapSecond.getLeapSecond(timestamp);
        int flags = buffer.getShort();
        rtca = (flags >> 12) & 0x0007;
        prnCount = buffer.get();
        buffer.get();//mode
        x = buffer.getDouble();
        y = buffer.getDouble();
        z = buffer.getDouble();
        buffer.getInt();//gdop
        heading = buffer.getInt();
        pitch = buffer.getInt();
        roll = buffer.getInt();
        speed = buffer.getInt();
        velocityX = buffer.getInt();
        velocityY = buffer.getInt();
        velocityZ = buffer.getInt();
        buffer.get();//prn1
        buffer.get();//prn2
        buffer.get();//prn3
        buffer.get();//prn4
        buffer.get();//prn5
        buffer.get();//prn6
        reportType = buffer.getShort();
        reportValue = buffer.getDouble();
        milesMessage.deserialize(buffer);
        buffer.get();//descretes
        buffer.getShort();//checksum
        buffer.get();//CR
        buffer.get();//LF
    }
//
//    private CDL.RTCA processRtca(int value) {
//        CDL.RTCA rtca = CDL.RTCA.NOTHING;
//        switch (value) {
//            case 1: {
//                rtca = CDL.RTCA.ALIVE;
//                break;
//            }
//            case 2: {
//                rtca = CDL.RTCA.MISS;
//                break;
//            }
//            case 3: {
//                rtca = CDL.RTCA.MOBILITY_KILL;
//                break;
//            }
//            case 4: {
//                rtca = CDL.RTCA.FIREPOWER_KILL;
//                break;
//            }
//            case 5: {
//                rtca = CDL.RTCA.CAT_KILL;
//                break;
//            }
//            case 6: {
//                rtca = CDL.RTCA.COMMO_KILL;
//                break;
//            }
//            case 7: {
//                rtca = CDL.RTCA.HIT_NO_KILL;
//                break;
//            }
//        }
//        return rtca;
//    }

    public int getUnitId() {
        return unitId;
    }

    public int getMilesPid() {
        return milesPid;
    }

    public int getMilesType() {
        return milesType;
    }

    public int getMessageId() {
        return messageId;
    }

    public int getMilesPid_payload() {
        return milesPid_payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isSolutionValid() {
        return solutionValid;
    }

    public boolean isHeadingValid() {
        return headingValid;
    }

    public boolean isPitchValid() {
        return pitchValid;
    }

    public boolean isRollValid() {
        return rollValid;
    }

    public boolean isSpeedValid() {
        return speedValid;
    }

    public boolean isStale() {
        return stale;
    }

    public boolean isAccelerationValid() {
        return accelerationValid;
    }

    public int getTxRecord() {
        return txRecord;
    }

    public int getRtca() {
        return rtca;
    }

    public boolean isRecorderOn() {
        return recorderOn;
    }

    public int getPrnCount() {
        return prnCount;
    }

    public int getSolution() {
        return solution;
    }

    public int getMethod() {
        return method;
    }

    public int getKalman() {
        return kalman;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getGdop() {
        return gdop;
    }

    public int getHeading() {
        return heading;
    }

    public int getPitch() {
        return pitch;
    }

    public int getRoll() {
        return roll;
    }

    public int getSpeed() {
        return speed;
    }

    public int getVelocityX() {
        return velocityX;
    }

    public int getVelocityY() {
        return velocityY;
    }

    public int getVelocityZ() {
        return velocityZ;
    }

    public int getReportType() {
        return reportType;
    }

    public double getReportValue() {
        return reportValue;
    }

    public MilesMessage getMilesMessage() {
        return milesMessage;
    }
}
