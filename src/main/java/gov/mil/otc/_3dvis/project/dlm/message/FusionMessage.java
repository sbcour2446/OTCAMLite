package gov.mil.otc._3dvis.project.dlm.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * The fusion message.  This message contains fusion data generated from the algorithm running on the LMS board.
 * <p>
 * See the
 * Close Terrain Shaping Obstacle (CTSO) Munition, Wide Area Top Attack, XM204
 * Dispenser Launcher Module (DLM) Internal Message Contents Document.
 */
public class FusionMessage extends DlmMessage {

    public static final int MESSAGE_CODE = 0x61A3;

    private long frame;          // Frame	4	Frame number
    private double frameTime; // Frame Time	8	Frame time
    private long numSR;          // Num SR	4	Number of SR
    private long numRD;          // Num RD	4	Number of RD
    private long numTD;          // Num Track	4	Number of tracks
    private final List<Sensor> sensorList = new ArrayList<>();
    private final List<Radar> radarList = new ArrayList<>();
    private final List<Track> trackList = new ArrayList<>();
    private int radarEnable;    // Radar Enable	1	Radar enable state
    private int radarMode;      // Radar Mode	1	Radar mode
    private int commitType;     // Commit Type	1	Commit flag
    private int effectType;     // Effect Type	1	Effect type
    private int effectNum;      // Effect Num	1	Effect Number
    private long launchSecond;   // Launch Second	4	Seconds at time of launch
    private int launchMilli;   // Launch Millisecond	2	Milliseconds at time of launch

    /**
     * The Constructor.
     */
    private FusionMessage() {
    }

    /**
     * Creates the message from the data byte stream.
     *
     * @param data The message data.
     * @return The decoded message, null if error.
     */
    public static FusionMessage create(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        FusionMessage fusionMessage = new FusionMessage();
        if (fusionMessage.deserialize(byteBuffer)) {
            return fusionMessage;
        }
        return null;
    }

    /**
     * Deserialize the data byte stream.
     *
     * @param byteBuffer The message data.
     * @return True is successful, otherwise false.
     */
    private boolean deserialize(ByteBuffer byteBuffer) {
        if (byteBuffer.remaining() < 24) {
            return false;
        }

        frame = getUnsignedInt(byteBuffer);
        frameTime = byteBuffer.getDouble();
        numSR = getUnsignedInt(byteBuffer);
        numRD = getUnsignedInt(byteBuffer);
        numTD = getUnsignedInt(byteBuffer);

        for (int i = 0; i < numSR; i++) {
            if (byteBuffer.remaining() < 36) {
                return false;
            }
            Sensor sensor = new Sensor();
            sensor.deserialize(byteBuffer);
            sensorList.add(sensor);
        }

        for (int i = 0; i < numRD; i++) {
            if (byteBuffer.remaining() < 20) {
                return false;
            }
            Radar radar = new Radar();
            radar.deserialize(byteBuffer);
            radarList.add(radar);
        }

        for (int i = 0; i < numTD; i++) {
            if (byteBuffer.remaining() < 56) {
                return false;
            }
            Track track = new Track();
            track.deserialize(byteBuffer);
            trackList.add(track);
        }

        if (byteBuffer.remaining() < 11) {
            return false;
        }

        radarEnable = getUnsignedByte(byteBuffer);
        radarMode = getUnsignedByte(byteBuffer);
        commitType = getUnsignedByte(byteBuffer);
        effectType = getUnsignedByte(byteBuffer);
        effectNum = getUnsignedByte(byteBuffer);
        launchSecond = getUnsignedInt(byteBuffer);
        launchMilli = getUnsignedShort(byteBuffer);

        return true;
    }

    public long getTimestamp() {
        return covertTime(frameTime);
    }

    public long getFrame() {
        return frame;
    }

    public double getFrameTime() {
        return frameTime;
    }

    public long getNumSR() {
        return numSR;
    }

    public long getNumRD() {
        return numRD;
    }

    public long getNumTD() {
        return numTD;
    }

    public List<Sensor> getSensorList() {
        return sensorList;
    }

    public List<Radar> getRadarList() {
        return radarList;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public int getRadarEnable() {
        return radarEnable;
    }

    public int getRadarMode() {
        return radarMode;
    }

    public int getCommitType() {
        return commitType;
    }

    public int getEffectType() {
        return effectType;
    }

    public int getEffectNum() {
        return effectNum;
    }

    public long getLaunchSecond() {
        return launchSecond;
    }

    public int getLaunchMilli() {
        return launchMilli;
    }

    /**
     * The sensor.
     */
    public static class Sensor {

        private double time;       // Time         8	Time
        private long sensorId;     // Sensor ID	4	Sensor ID
        private long targetId;     // Target ID	4	Target ID
        private double rawBearing; // Bearing RAW	4	Raw bearing data
        private double bearing;    // Bearing	4	Bearing data
        private double bearingRate;// Bearing Rate	4	Rate of change
        private long age;          // Age          4	How old the data is
        private long srClass;      // Class	4	Classification of SR

        public void deserialize(ByteBuffer byteBuffer) {
            time = byteBuffer.getDouble();
            sensorId = getUnsignedInt(byteBuffer);
            targetId = getUnsignedInt(byteBuffer);
            rawBearing = byteBuffer.getFloat();
            bearing = byteBuffer.getFloat();
            bearingRate = byteBuffer.getFloat();
            age = getUnsignedInt(byteBuffer);
            srClass = getUnsignedInt(byteBuffer);
        }

        public double getTime() {
            return time;
        }

        public long getSensorId() {
            return sensorId;
        }

        public long getTargetId() {
            return targetId;
        }

        public double getRawBearing() {
            return rawBearing;
        }

        public double getBearing() {
            return bearing;
        }

        public double getBearingRate() {
            return bearingRate;
        }

        public long getAge() {
            return age;
        }

        public long getSrClass() {
            return srClass;
        }
    }

    /**
     * The radar.
     */
    public static class Radar {

        private double time;      // Time         8	Time
        private long targetId;       // Target ID	4	Target ID
        private double range;      // Range	4	Range data
        private double rangeRate;  // Range Rate	4	Rate of change

        public void deserialize(ByteBuffer byteBuffer) {
            time = byteBuffer.getDouble();
            targetId = getUnsignedInt(byteBuffer);
            range = byteBuffer.getFloat();
            rangeRate = byteBuffer.getFloat();
        }

        public double getTime() {
            return time;
        }

        public long getTargetId() {
            return targetId;
        }

        public double getRange() {
            return range;
        }

        public double getRangeRate() {
            return rangeRate;
        }
    }

    /**
     * The track.
     */
    public static class Track {

        private long targetId;    // Target ID	4	Target ID
        private double time; // Time         8	Track Time
        private float trackX;// Track X	4	X Coordinate of Track; Positive Number = meters to EAST; Negative Number = meters to WEST
        private float trackY;// Track Y	4	Y Coordinate of Track; Positive Number = meters to NORTH; Negative Number = meters to SOUTH
        private float xDot;    // X Dot	4
        private float yDot;    // Y Dot	4
        private double p11;    // P11	4
        private double p12;    // P12	4
        private double p22;    // P22	4
        private double p33;    // P33	4
        private double p34;    // P34	4
        private double p44;    // P44	4
        private long classification; // Classification	4	Classification of track

        public void deserialize(ByteBuffer byteBuffer) {
            targetId = getUnsignedInt(byteBuffer);
            time = byteBuffer.getDouble();
            trackX = byteBuffer.getFloat();
            trackY = byteBuffer.getFloat();
            xDot = byteBuffer.getFloat();
            yDot = byteBuffer.getFloat();
            p11 = byteBuffer.getFloat();
            p12 = byteBuffer.getFloat();
            p22 = byteBuffer.getFloat();
            p33 = byteBuffer.getFloat();
            p34 = byteBuffer.getFloat();
            p44 = byteBuffer.getFloat();
            classification = getUnsignedInt(byteBuffer);
        }

        public long getTargetId() {
            return targetId;
        }

        public double getTime() {
            return time;
        }

        public float getTrackX() {
            return trackX;
        }

        public float getTrackY() {
            return trackY;
        }

        public float getxDot() {
            return xDot;
        }

        public float getyDot() {
            return yDot;
        }

        public double getP11() {
            return p11;
        }

        public double getP12() {
            return p12;
        }

        public double getP22() {
            return p22;
        }

        public double getP33() {
            return p33;
        }

        public double getP34() {
            return p34;
        }

        public double getP44() {
            return p44;
        }

        public long getClassification() {
            return classification;
        }
    }
}
