package gov.mil.otc._3dvis.project.dlm.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The DWELL message.  Contains DWELL message data received from teh TSS (Radar).
 * <p>
 * See the
 * Close Terrain Shaping Obstacle (CTSO) Munition, Wide Area Top Attack, XM204
 * Dispenser Launcher Module (DLM) Internal Message Contents Document.
 */
public class DwellMessage extends DlmMessage {

    public static final int MESSAGE_CODE = 0x618F;

    private int startCode;
    private int dwellCode;
    private int dwellLen;
    private int dwellCs;
    private int tracks;         // Num Tracks	1	Number of active tracks (0, 1, 2, 3, or 4)
    private long dwellId;    // Dwell ID	4	Dwell ID since unit manufacture
    private long sec;        // UTC Seconds	4	Number of seconds since midnight January 1, 2000
    private int msec;        // UTC Milliseconds	2	Number of milliseconds since last second change
    private long radarTime;    // Radar Time	4	msec since power on or synch command
    private boolean sync;    // Radar Sync	1	0x00:  Radar time not synchronized
    private int freqChannel;    // Frequency Channel	1	Valid Range 50-185
    private int priChannel;    // PRI Channel	2	Valid range 2000-10000
    private int fftLength;    // FFT Length	1	0x00 � 0
    private int weighting;    // Weighting	1	0x00 � Rectangular
    private int rngGateTime;    // RNG Gate Start Time	2	Valid range 0 � 1000
    private int ttsTemp;    // TSS Temp	2	TSS Internal Temperature
    private int peakClutter;    // Peak Cluttering	1	Distance in meters to Peak Clutter
    private int radarStatus;    // Radar Status Word	1	bit 0:  Radar Fail (1=Fail)
    private int interference;    // Interference Word	1	bit 0:  Interference Detected (1=Detected);bit 1:  Broadband Interference (1=Detected)
    private int radarMode;    // Radar Mode Word	1	0x00 � AV; // 0x01 � AP
    private final ActiveTarget[] activeTargets;

    /**
     * The Constructor.
     */
    private DwellMessage() {
        activeTargets = new ActiveTarget[]{
                new ActiveTarget(),
                new ActiveTarget(),
                new ActiveTarget(),
                new ActiveTarget()
        };
    }

    /**
     * Creates the message from the data byte stream.
     *
     * @param data The message data.
     * @return The decoded message, null if error.
     */
    public static DwellMessage create(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        DwellMessage dwellMessageNew = new DwellMessage();
        if (dwellMessageNew.deserialize(byteBuffer)) {
            return dwellMessageNew;
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
        if (byteBuffer.remaining() >= 158) {
            startCode = getUnsignedByte(byteBuffer);
            dwellCode = getUnsignedByte(byteBuffer);
            dwellLen = getUnsignedShort(byteBuffer);
            dwellCs = getUnsignedByte(byteBuffer);
            tracks = getUnsignedByte(byteBuffer);
            dwellId = getUnsignedInt(byteBuffer);
            sec = getUnsignedInt(byteBuffer);
            msec = byteBuffer.getShort();
            radarTime = getUnsignedInt(byteBuffer);
            sync = byteBuffer.get() == 0x01;
            freqChannel = getUnsignedByte(byteBuffer);
            priChannel = getUnsignedShort(byteBuffer);
            fftLength = getUnsignedByte(byteBuffer);
            weighting = getUnsignedByte(byteBuffer);
            rngGateTime = getUnsignedShort(byteBuffer);

            byteBuffer.position(byteBuffer.position() + 12); // skip 12 bytes

            ttsTemp = getUnsignedShort(byteBuffer);
            peakClutter = getUnsignedByte(byteBuffer);

            byteBuffer.position(byteBuffer.position() + 9); // skip 9 bytes

            radarStatus = getUnsignedByte(byteBuffer);
            interference = getUnsignedByte(byteBuffer);
            radarMode = getUnsignedByte(byteBuffer);
            activeTargets[0].deserialize(byteBuffer);        // Target Active 1	27	See Active Target Table
            activeTargets[1].deserialize(byteBuffer);        // Target Active 2	27	See Active Target Table
            activeTargets[2].deserialize(byteBuffer);        // Target Active 3	27	See Active Target Table
            activeTargets[3].deserialize(byteBuffer);        // Target Active 4	27	See Active Target Table

            return true;
        }
        return false;
    }

    public long getTimestamp() {
        return covertTime(sec, msec);
    }

    public int getStartCode() {
        return startCode;
    }

    public int getDwellCode() {
        return dwellCode;
    }

    public int getDwellLen() {
        return dwellLen;
    }

    public int getDwellCs() {
        return dwellCs;
    }

    public int getTracks() {
        return tracks;
    }

    public long getDwellId() {
        return dwellId;
    }

    public long getSec() {
        return sec;
    }

    public int getMsec() {
        return msec;
    }

    public long getRadarTime() {
        return radarTime;
    }

    public boolean isSync() {
        return sync;
    }

    public int getFreqChannel() {
        return freqChannel;
    }

    public int getPriChannel() {
        return priChannel;
    }

    public int getFftLength() {
        return fftLength;
    }

    public int getWeighting() {
        return weighting;
    }

    public int getRngGateTime() {
        return rngGateTime;
    }

    public int getTtsTemp() {
        return ttsTemp;
    }

    public int getPeakClutter() {
        return peakClutter;
    }

    public int getRadarStatus() {
        return radarStatus;
    }

    public int getInterference() {
        return interference;
    }

    public int getRadarMode() {
        return radarMode;
    }

    public ActiveTarget[] getActiveTargets() {
        return activeTargets;
    }

    /**
     * The active target.
     */
    public static class ActiveTarget {
        int active;                 // Active	1	0x00 for no track; 0x01 for active track
        int quality;        // Quality	1	Bit Definitions
        // 0x00 = No Track
        // bit 0:  1 = New Track
        // bit 1:  1 = Weak Track
        // bit 2:  1 = Firm Track
        // bit 3:  1 = Coasting Track
        // bit 4:  1 = Track in Target Range of interest
        // (i.e. 0x12 represents a weak track on a target in the range of interest)
        int uniqueId;              // Track ID	2	Unique track ID assigned by TSS
        int updates;        // Num Track Updates	2	Total number of updates for this Track ID
        long power;                  // Power	4	Last target power measurement
        int tgtClass;        // Class	1	Growth
        int detectRange;        // Detect Range	2	Current target range from detection
        int detectRangeRate;    // Detect Range Rate	2	Current target range rate from detection
        int detectRangeError;    // Detect Range RMS Error	1	Estimate of detection range error
        int detectRangeRateError;    // Detect Range Rate RMS Error	1	Estimate of detection range rate error
        int trackRange;        // Track Range	2	Current target range from tracker
        int trackRangeRate;    // Track Range Rate	2	Current target range rate from tracker
        int trackRangeError;    // Track Range RMS Error	1	Estimate of track range error
        int trackRangeRateError;    // Track Range Rate RMS Error	1	Estimate of track range rate error
        int probTracked;        // Probability of Tracked Vehicle	1	Probability of Tracked Vehicle
        int probWheeled;        // Probability of Wheeled Vehicle	1	Probability of Wheeled Vehicle
        int classFeatureCount;    // Class Feat Cross	1	Classification Feature Count
        int classFeatureSpread;    // Class Feat Spread	1	Classification Feature Spread

        private void deserialize(ByteBuffer byteBuffer) {
            active = getUnsignedByte(byteBuffer);
            quality = getUnsignedByte(byteBuffer);
            uniqueId = getUnsignedShort(byteBuffer);
            updates = getUnsignedShort(byteBuffer);
            power = getUnsignedInt(byteBuffer);
            tgtClass = getUnsignedByte(byteBuffer);
            detectRange = getUnsignedShort(byteBuffer);
            detectRangeRate = byteBuffer.getShort();
            detectRangeError = getUnsignedByte(byteBuffer);
            detectRangeRateError = getUnsignedByte(byteBuffer);
            trackRange = getUnsignedShort(byteBuffer);
            trackRangeRate = byteBuffer.getShort();
            trackRangeError = getUnsignedByte(byteBuffer);
            trackRangeRateError = getUnsignedByte(byteBuffer);
            probTracked = getUnsignedByte(byteBuffer);
            probWheeled = getUnsignedByte(byteBuffer);
            classFeatureCount = getUnsignedByte(byteBuffer);
            classFeatureSpread = getUnsignedByte(byteBuffer);
        }

        public int getActive() {
            return active;
        }

        public int getQuality() {
            return quality;
        }

        public int getUniqueId() {
            return uniqueId;
        }

        public int getUpdates() {
            return updates;
        }

        public long getPower() {
            return power;
        }

        public int getTgtClass() {
            return tgtClass;
        }

        public int getDetectRange() {
            return detectRange;
        }

        public int getDetectRangeRate() {
            return detectRangeRate;
        }

        public int getDetectRangeError() {
            return detectRangeError;
        }

        public int getDetectRangeRateError() {
            return detectRangeRateError;
        }

        public int getTrackRange() {
            return trackRange;
        }

        public int getTrackRangeRate() {
            return trackRangeRate;
        }

        public int getTrackRangeError() {
            return trackRangeError;
        }

        public int getTrackRangeRateError() {
            return trackRangeRateError;
        }

        public int getProbTracked() {
            return probTracked;
        }

        public int getProbWheeled() {
            return probWheeled;
        }

        public int getClassFeatureCount() {
            return classFeatureCount;
        }

        public int getClassFeatureSpread() {
            return classFeatureSpread;
        }
    }
}
