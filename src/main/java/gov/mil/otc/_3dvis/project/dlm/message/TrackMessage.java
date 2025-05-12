package gov.mil.otc._3dvis.project.dlm.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * The track message.  Contains track data generated from teh algorithm running on teh LMS board.
 * <p>
 * See the
 * Close Terrain Shaping Obstacle (CTSO) Munition, Wide Area Top Attack, XM204
 * Dispenser Launcher Module (DLM) Internal Message Contents Document.
 */
public class TrackMessage extends DlmMessage {

    public static final int MESSAGE_CODE = 0x6193;

    private final List<Track> trackList = new ArrayList<>();

    /**
     * The Constructor.
     */
    private TrackMessage() {
    }

    /**
     * Creates the message from the data byte stream.
     *
     * @param data The message data.
     * @return The decoded message, null if error.
     */
    public static TrackMessage create(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        TrackMessage trackMessage = new TrackMessage();
        if (trackMessage.deserialize(byteBuffer)) {
            return trackMessage;
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
        if (byteBuffer.remaining() >= 80) {
            while (byteBuffer.remaining() >= 80) {
                Track track = new Track();
                track.deserialize(byteBuffer);
                trackList.add(track);
            }
            return true;
        }
        return false;
    }

    public List<Track> getTrackList() {
        return trackList;
    }

    public enum GenClass {
        UNKNOWN("unknown"),
        HTV("heavy tracked vehicle"),
        LTV("light tracked vehicle"),
        HWV("heavy wheeled vehicle"),
        LWV("light wheeled vehicle"),
        SLWV("super light wheeled vehicle"),
        RWAH("rotary wing aircraft"),
        BATS("battlefield sound"),
        PERS("personnel"),
        GDV("any ground vehicle"),
        HGV("heavy ground vehicle"),
        LGV("light ground vehicle"),
        TGV("tracked vehicle");
        final String description;

        GenClass(String description) {
            this.description = description;
        }

        public static GenClass getEnum(int ordinal) {
            if (ordinal < 0 || ordinal >= GenClass.values().length) {
                return GenClass.UNKNOWN;
            } else {
                return GenClass.values()[ordinal];
            }
        }

        public String getDescription() {
            return description;
        }
    }

    public class Track {

        private long frame;                  // Frame	4
        private long sec;            // Time	4
        private int ms;            // MS	2
        private int slot;            // Slot	2
        private int trackNumber;        // Track Number	2
        private int state;            // State	1
        private double maxSPL;        // Max SPL	4
        private float bearing;        // Bearing	4
        private float trackQuality;           // Track Quality	4
        private double trackedBearingRate;    // Tracked Bearing Rate	4
        private double peakFrequency;    // Peak Frequency	4
        private long sumPeakResID;        // Sum Peak Res ID	4
        private double fundFrequency;    // Fund Frequency	4
        private double dFundFrequency;    // DFund Frequency	4
        private long sumFundResID;        // Sum Fund Res ID	4
        private double wgtSNR;        // Wgt SNR	4
        private double lFRat;        // LFRat	4
        private int tic;            // Tic	2
        private long genClass;        // GenClass	4
        private double idf;        // IDF	4
        private int confidence;        // Confidence	2
        private int numberCycle;        // Number Cycle	2
        private double nnValue;        // NN Value	4
        private int numberBlades;        // Number Blades	2
        private int truthString;        // Truth String	1

        public void deserialize(ByteBuffer byteBuffer) {
            frame = getUnsignedInt(byteBuffer);
            sec = getUnsignedInt(byteBuffer);
            ms = getUnsignedShort(byteBuffer);
            slot = getUnsignedShort(byteBuffer);
            trackNumber = getUnsignedShort(byteBuffer);
            state = byteBuffer.get();
            maxSPL = byteBuffer.getFloat();
            bearing = byteBuffer.getFloat();
            trackQuality = byteBuffer.getFloat();
            trackedBearingRate = byteBuffer.getFloat();
            peakFrequency = byteBuffer.getFloat();
            sumPeakResID = getUnsignedInt(byteBuffer);
            fundFrequency = byteBuffer.getFloat();
            dFundFrequency = byteBuffer.getFloat();
            sumFundResID = getUnsignedInt(byteBuffer);
            wgtSNR = byteBuffer.getFloat();
            lFRat = byteBuffer.getFloat();
            tic = getUnsignedShort(byteBuffer);
            genClass = getUnsignedInt(byteBuffer);
            idf = byteBuffer.getFloat();
            confidence = getUnsignedShort(byteBuffer);
            numberCycle = getUnsignedShort(byteBuffer);
            nnValue = byteBuffer.getFloat();
            numberBlades = getUnsignedShort(byteBuffer);
            truthString = byteBuffer.get();
        }

        public long getTimestamp() {
            return covertTime(sec, ms);
        }

        public long getFrame() {
            return frame;
        }

        public long getSec() {
            return sec;
        }

        public int getMs() {
            return ms;
        }

        public int getSlot() {
            return slot;
        }

        public int getTrackNumber() {
            return trackNumber;
        }

        public int getState() {
            return state;
        }

        public double getMaxSPL() {
            return maxSPL;
        }

        public float getBearing() {
            return bearing;
        }

        public float getTrackQuality() {
            return trackQuality;
        }

        public double getTrackedBearingRate() {
            return trackedBearingRate;
        }

        public double getPeakFrequency() {
            return peakFrequency;
        }

        public long getSumPeakResID() {
            return sumPeakResID;
        }

        public double getFundFrequency() {
            return fundFrequency;
        }

        public double getdFundFrequency() {
            return dFundFrequency;
        }

        public long getSumFundResID() {
            return sumFundResID;
        }

        public double getWgtSNR() {
            return wgtSNR;
        }

        public double getlFRat() {
            return lFRat;
        }

        public int getTic() {
            return tic;
        }

        public GenClass getGenClass() {
            return TrackMessage.GenClass.getEnum((int) genClass);
        }

        public double getIdf() {
            return idf;
        }

        public int getConfidence() {
            return confidence;
        }

        public int getNumberCycle() {
            return numberCycle;
        }

        public double getNnValue() {
            return nnValue;
        }

        public int getNumberBlades() {
            return numberBlades;
        }

        public int getTruthString() {
            return truthString;
        }
    }
}
