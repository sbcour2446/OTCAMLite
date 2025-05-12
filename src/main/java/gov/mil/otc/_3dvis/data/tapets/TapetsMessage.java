package gov.mil.otc._3dvis.data.tapets;

import gov.mil.otc._3dvis.data.miles.MilesMessage;
import gov.mil.otc._3dvis.utility.GpsLeapSecond;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Class representation of a TAPETS message.
 */
public class TapetsMessage {

    /**
     * The type of Miles.
     */
    public enum MilesType {
        /**
         * IWS
         */
        IWS,
        /**
         * WITS
         */
        WITS,
        /**
         * XXI
         */
        XXI,
        /**
         * Unknown
         */
        UNKNOWN
    }

    /**
     * Class representation of a vehicle type.
     */
    public enum VehicleType {
        /**
         * VEST Vehicle Type
         */
        VEST,
        /**
         * HMMWV Vehicle Type
         */
        HMMWV,
        /**
         * BRDM Vehicle Type
         */
        BRDM,
        /**
         * ZSU234 Vehicle Type
         */
        ZSU234,
        /**
         * M1974 Vehicle Type
         */
        M1974,
        /**
         * M113 Vehicle Type
         */
        M113,
        /**
         * M1_105 Vehicle Type
         */
        M1_105,
        /**
         * M901 Vehicle Type
         */
        M901,
        /**
         * M1 Vehicle Type
         */
        M1,
        /**
         * T80 Vehicle Type
         */
        T80,
        /**
         * M60A1 Vehicle Type
         */
        M60A1,
        /**
         * BMPII Vehicle Type
         */
        BMPII,
        /**
         * M2 Vehicle Type
         */
        M2,
        /**
         * M3 Vehicle Type
         */
        M3,
        /**
         * T72 Vehicle Type
         */
        T72,
        /**
         * BMPI Vehicle Type
         */
        BMPI,
        /**
         * ITS_HEAVY Vehicle Type
         */
        ITS_HEAVY,
        /**
         * ITS_MEDIUM Vehicle Type
         */
        ITS_MEDIUM,
        /**
         * ITS_NO Vehicle Type
         */
        ITS_NO,
        /**
         * AAV_P Vehicle Type
         */
        AAV_P,
        /**
         * AAV_CC Vehicle Type
         */
        AAV_CC,
        /**
         * LAV_25 Vehicle Type
         */
        LAV_25,
        /**
         * LAV_AT Vehicle Type
         */
        LAV_AT,
        /**
         * LAV_M Vehicle Type
         */
        LAV_M,
        /**
         * LAV_CC Vehicle Type
         */
        LAV_CC,
        /**
         * ICV Vehicle Type
         */
        ICV,
        /**
         * ATGM Vehicle Type
         */
        ATGM,
        /**
         * CV Vehicle Type
         */
        CV,
        /**
         * ESV Vehicle Type
         */
        ESV,
        /**
         * FSV Vehicle Type
         */
        FSV,
        /**
         * MC Vehicle Type
         */
        MC,
        /**
         * NBCRV Vehicle Type
         */
        NBCRV,
        /**
         * MEV Vehicle Type
         */
        MEV,
        /**
         * RV Vehicle Type
         */
        RV,
        /**
         * M1_lf Vehicle Type
         */
        M_1_LF,
        /**
         * BFIST Vehicle Type
         */
        BFIST,
        /**
         * UNKNOWN Vehicle Type
         */
        UNKNOWN
    }

    /**
     * Representation of vehicle type wits.
     */
    public enum VehicleTypeWits {
        /**
         * M1A2 Vehicle Type Wits
         */
        M1A2,
        /**
         * M1_120 Vehicle Type Wits
         */
        M1_120,
        /**
         * M1A1 Vehicle Type Wits
         */
        M1A1,
        /**
         * BMP2C Vehicle Type Wits
         */
        BMP2C,
        /**
         * M2A2 Vehicle Type Wits
         */
        M2A2,
        /**
         * M3A2 Vehicle Type Wits
         */
        M3A2,
        /**
         * T80 Vehicle Type Wits
         */
        T80,
        /**
         * T72 Vehicle Type Wits
         */
        T72,
        /**
         * M1974 Vehicle Type Wits
         */
        M1974,
        /**
         * ZSU Vehicle Type Wits
         */
        ZSU,
        /**
         * BMPI Vehicle Type Wits
         */
        BMPI,
        /**
         * BMPII Vehicle Type Wits
         */
        BMPII,
        /**
         * BRDM Vehicle Type Wits
         */
        BRDM,
        /**
         * M113 Vehicle Type Wits
         */
        M113,
        /**
         * HMMWV Vehicle Type Wits
         */
        HMMWV,
        /**
         * M901 Vehicle Type Wits
         */
        M901,
        /**
         * UNKNOWN Vehicle Type Wits
         */
        UNKNOWN
    }

    /**
     * Representation of an RTCA status.
     */
    public enum RtcaStatus {
        /**
         * Alive RTCA status
         */
        ALIVE,
        /**
         * Mobility kill RTCA status
         */
        MOBILITY_KILL,
        /**
         * Firepower RTCA status
         */
        FIRERPOWER_KILL,
        /**
         * Cat kill RTCA status
         */
        CAT_KILL,
        /**
         * Commo kill RTCA status
         */
        COMMO_KILL,
        /**
         * Hit, but no kill RTCA status.
         */
        HIT_NO_KILL,
        /**
         * Miss RTCA status
         */
        MISS,
        /**
         * No percentage hit (PH), percentage kill (PK) RTCA status
         */
        NO_PHPK,
        /**
         * Suppression RTCA status
         */
        SUPPRESSION,
        /**
         * Unresolved RTCA status
         */
        UNRESOLVED,
        /**
         * Admin command that is unknown RTCA status
         */
        ADMIN_UNKN,
        /**
         * Killed by admin RTCA status
         */
        ADMIN_KILL
    }

    /**
     * Class representation of an Ards status.
     */
    public enum ArdsStatus {
        /**
         * Recorder_Command
         */
        RECORDER_COMMAND(0),
        /**
         * UNKN
         */
        UNKN(0),
        /**
         * AdminResurrect
         */
        ADMIN_RESURRECT(1),
        /**
         * Alive
         */
        ALIVE(1),
        /**
         * Near miss
         */
        NEAR_MISS(2),
        /**
         * Mobility kill
         */
        MOBILITY_KILL(3),
        /**
         * Firepower kill
         */
        FIREPOWER_KILL(4),
        /**
         * Admin kill
         */
        ADMIN_KILL(5),
        /**
         * Cat kill
         */
        CAT_KILL(5),
        /**
         * Commo kill
         */
        COMMO_KILL(6),
        /**
         * Hit
         */
        HIT(7);

        private final int value;

        ArdsStatus(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }
    }

    private static final int SIZE = 126;
    private static final byte PREAMBLE1 = '$';
    private static final byte PREAMBLE2 = 'E';
    private short unitId = -0;
    private short milesPid = 0;
    private byte vehicleType = 0;
    private byte milesType = 0;
    private static final byte SYNC1 = (byte) 0x9C;
    private static final byte SYNC2 = (byte) 0xA5;
    private short byteCount = 0;
    private final FastG fastG = new FastG();
    private static final byte EOM1 = '\r';
    private static final byte EOM2 = '\n';
    private static final long GPS_EPOCH = 315964800000L; // GPS epoch in Unix time
    private static final long MILLIS_WEEK = 604800000; // Milliseconds per Week (60 * 60 * 24 * 7 * 1000)
    private static final long GPS_ROLLOVER = (1024 * MILLIS_WEEK); // rollover period

    public int getUnitId() {
        return unitId;
    }

    public int getMilesPid() {
        return milesPid;
    }

    public long getTimestamp() {
        Calendar timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        timestamp.setTimeInMillis(GPS_EPOCH);

        long now = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        long diff = now - timestamp.getTimeInMillis();
        long lastRollover = GPS_EPOCH + (diff / GPS_ROLLOVER - 1) * GPS_ROLLOVER;
        long millis = lastRollover + (fastG.payloadG.gpsWeek * MILLIS_WEEK) + fastG.payloadG.gpsMilliSec;
        long gpsLeapSecond = GpsLeapSecond.getLeapSecond(millis);

        return millis + gpsLeapSecond;
    }

    public int getRtcaBits() {
        return (fastG.payloadG.quality >> 12) & 0x07;
    }

    public PayloadG getPayload() {
        return fastG.payloadG;
    }

    public byte getVehicleType() {
        return vehicleType;
    }

    public byte getMilesType() {
        return milesType;
    }

    public short getByteCount() {
        return byteCount;
    }

    public void deserialize(ByteBuffer b) {
        b.order(ByteOrder.LITTLE_ENDIAN);

        b.get(); // PREAMBLE1
        b.get(); // PREAMBLE2
        unitId = b.getShort();
        milesPid = b.getShort();
        vehicleType = b.get();
        milesType = b.get();
        b.get(); // SYNC1
        b.get(); // SYNC2
        byteCount = b.getShort();
        fastG.deserialize(b);
        b.get(); // EOM1
        b.get(); // EOM2
    }

    public class FastG {

        private byte msgId = 3;
        private final PayloadG payloadG = new PayloadG();
        private int checksum;
        private short calculatedChecksum;

        public byte getMsgId() {
            return msgId;
        }

        public PayloadG getPayloadG() {
            return payloadG;
        }

        public int getChecksum() {
            return checksum;
        }

        public short getCalculatedChecksum() {
            return calculatedChecksum;
        }

        private void deserialize(ByteBuffer b) {
            int i1 = b.position();
            msgId = b.get();
            payloadG.deserialize(b);
            int i2 = b.position();
            checksum = b.getShort();
            calculatedChecksum = calculateChecksum(b, i1, i2);
        }

        private short calculateChecksum(ByteBuffer b, int first, int last) {
            int calculatedCheckSum = 0;

            for (int i = first; i < last; i++) {
                if (i % 2 == 0) {
                    calculatedCheckSum += b.get(i) & 0xFF;
                } else {
                    calculatedCheckSum += (b.get(i) & 0xFF) << 8;
                }
            }

            return (short) ((~(short) calculatedCheckSum) + 1);
        }
    }

    public class PayloadG {

        private short unitId;
        private short gpsWeek;
        private long gpsMilliSec;
        private short quality;
        private byte prnCount;
        private byte mode;
        private double ecefX;
        private double ecefY;
        private double ecefZ;
        private int gdop;
        private int heading;
        private int pitch;
        private int roll;
        private int speed;
        private int velocityX;
        private int velocityY;
        private int velocityZ;
        private byte prnList1;
        private byte prnList2;
        private byte prnList3;
        private byte prnList4;
        private byte prnList5;
        private byte prnList6;
        private short reportType;
        private long reportValue;
        private MilesMessage miles = new MilesMessage();
        private byte discretes;

        public boolean containsMiles() {
            return (quality & 0x0F00) == 0x0F00;
        }

        public short getUnitId() {
            return unitId;
        }

        public short getGpsWeek() {
            return gpsWeek;
        }

        public long getGpsMilliSec() {
            return gpsMilliSec;
        }

        public short getQuality() {
            return quality;
        }

        public byte getPrnCount() {
            return prnCount;
        }

        public byte getMode() {
            return mode;
        }

        public double getEcefX() {
            return ecefX;
        }

        public double getEcefY() {
            return ecefY;
        }

        public double getEcefZ() {
            return ecefZ;
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

        public byte getPrnList1() {
            return prnList1;
        }

        public byte getPrnList2() {
            return prnList2;
        }

        public byte getPrnList3() {
            return prnList3;
        }

        public byte getPrnList4() {
            return prnList4;
        }

        public byte getPrnList5() {
            return prnList5;
        }

        public byte getPrnList6() {
            return prnList6;
        }

        public short getReportType() {
            return reportType;
        }

        public long getReportValue() {
            return reportValue;
        }

        public MilesMessage getMiles() {
            return miles;
        }

        public byte getDiscretes() {
            return discretes;
        }

        private void deserialize(ByteBuffer b) {
            unitId = b.getShort();
            gpsWeek = b.getShort();
            gpsMilliSec = b.getInt();
            quality = b.getShort();
            prnCount = b.get();
            mode = b.get();
            ecefX = b.getDouble();
            ecefY = b.getDouble();
            ecefZ = b.getDouble();
            gdop = b.getInt();
            heading = b.getInt();
            pitch = b.getInt();
            roll = b.getInt();
            speed = b.getInt();
            velocityX = b.getInt();
            velocityY = b.getInt();
            velocityZ = b.getInt();
            prnList1 = b.get();
            prnList2 = b.get();
            prnList3 = b.get();
            prnList4 = b.get();
            prnList5 = b.get();
            prnList6 = b.get();
            reportType = b.getShort();
            reportValue = b.getLong();
            miles.deserialize(b);
            discretes = b.get();
        }
    }
}
