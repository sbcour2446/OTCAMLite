package gov.mil.otc._3dvis.vmf;

import gov.nasa.worldwind.geom.Position;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * The VMF K03.06 MAYDAY Message Class
 */
public class K0306 extends VmfMessage {

    private Position position;
    private Position observerPosition;
    private int urn;
    private int emergencyType;
    private int personnel;
    private int fuel;
    private int sysStatus;
    private final Calendar time;

    /**
     * {@inheritDoc}
     */
    protected K0306(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
        time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        position = observerPosition = Position.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        return switch (header.getStdVersion()) {
            case VMF_6017, VMF_6017A -> parse6017A(data);
            case VMF_6017B -> parse6017B(data);
            default -> parse6017D(data);
        };
    }

    /**
     * 6017D Message Parser
     *
     * @param data VmfDataBuffer data
     * @return true if successful parse otherwise, false
     */
    private boolean parse6017D(VmfDataBuffer data) {
//        boolean gri;
        boolean fri;
        double latitude;
        double longitude;
        double altitude;

        try {

            data.getInt(3);     // MAYDAY/ISOLATED PERSONNEL TYPE
            data.getInt(24);    // URN
            data.getInt(32);    // MAYDAY/IP EISN.
            data.getInt(4);     // EMERGENCY TYPE.
            data.getFpiInt(4);  // PERSONNEL INVOLVED
            if (data.getGpi()) {  // G1
                latitude = data.getInt(23) * LAT_CONVERSION_23BIT;
                longitude = data.getInt(24) * LON_CONVERSION_24BIT;
                if (longitude > 180) {
                    longitude -= 180.0;
                }
                altitude = 0;
                if (data.getFpi()) {
                    altitude = data.getInt(17) * FEET_TO_METERS;
                }
                if (data.getFpi()) {
                    altitude = data.getInt(22) * FEET_TO_METERS;
                }
                if (data.getFpi()) {
                    altitude = data.getInt(13) * 25.0 * FEET_TO_METERS;
                }
                position = Position.fromDegrees(latitude, longitude, altitude);

            }// end G1
            data.getInt(1);         // LOCATION QUALIFIER
            data.getFpiInt(4);      // LOCATION DERIVATION
            data.getFpiInt(4);      // LOCATION QUALITY
            data.getInt(4);         // month
            data.getInt(5);         // day
            data.getInt(5);         // hour
            data.getInt(6);         // minute
            if (data.getGpi()) {  // G2
                data.getFpiString(105);
                if (data.getGpi()) { // G3
                    data.getInt(4);         // IDENTITY, VMF
                    data.getInt(6);         // DIMENSION
                    data.getInt(6);         // ENTITY TYPE
                    data.getFpiInt(6);      // ENTITY SUBTYPE
                    data.getFpiInt(6);      // ENTITY SIZE/MOBILITY
                    data.getFpiInt(1);      // ENTITY STATUS
                    data.getFpiInt(9);      // NATIONALITY
                } // end G3
                if (data.getFpi()) { // R1
                    do {
                        fri = data.getGri();
                        data.getString(245);
                    } while (fri);
                }
                if (data.getFpi()) { // R2
                    do {
                        fri = data.getGri();
                        data.getString(140);
                    } while (fri);
                }
                data.getFpiString(140);

            } // end G2
            if (data.getGpi()) {  // G4
                latitude = data.getInt(23) * LAT_CONVERSION_23BIT;
                longitude = data.getInt(24) * LON_CONVERSION_24BIT;
                if (longitude > 180) {
                    longitude -= 180.0;
                }
                observerPosition = Position.fromDegrees(latitude, longitude);
                data.getInt(9);         // DIRECTION TO ENTITY
                data.getInt(18);        // RANGE TO ENTITY
            } // end G4

        } catch (Exception e) {

            return false;
        }

        return false;
    }

    /**
     * 6017A Message Parser
     *
     * @param data VmfDataBuffer data
     * @return true if successful parse otherwise, false
     */
    private boolean parse6017A(VmfDataBuffer data) {
        double latitude;
        double longitude;
        boolean gri;

        try {
            urn = data.getInt(24);
            latitude = data.getInt(21) * LAT_CONVERSION_21BIT;
            longitude = data.getInt(22) * LON_CONVERSION_22BIT;
            if (longitude >= 180) {
                longitude -= 360.0;
            }
            position = Position.fromDegrees(latitude, longitude);
            int day = data.getInt(5);     // day
            int hour = data.getInt(5);     // hour
            int min = data.getInt(6);     // minute
            time.set(header.getYear(), header.getMonth(), day, hour, min);
            data.getFpiInt(17); // Fuel
            data.getFpiInt(5);  // AIRCRAFT SYSTEM STATUS
            if (data.getGpi()) {
                do {
                    gri = data.getGri();
                    data.getInt(7);     // ORDNANCE TYPE
                    data.getInt(10);    // QUANTITY OF AMMUNITION
                } while (gri);
            }
            emergencyType = data.getFpiInt(4);          // EMERGENCY TYPE
            personnel = data.getFpiInt(4);          // PERSONNEL INVOLVED
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 6017B Message Parser
     *
     * @param data VmfDataBuffer data
     * @return true if successful parse otherwise, false
     */
    private boolean parse6017B(VmfDataBuffer data) {
//        double latitude;
//        double longitude;
//        boolean gri;
//        try { // TODO complete the K03.06 MAYDAY Message for 6017B
//            urn = data.getInt(24);
//            latitude = data.getInt(21) * LAT_CONVERSION_21BIT;
//            longitude = data.getInt(22) * LON_CONVERSION_22BIT;
//            if (longitude >= 180) {
//                longitude -= 360.0;
//            }
//            position = Position.fromDegrees(latitude, longitude);
//            data.getInt(5);     // day
//            data.getInt(5);     // hour
//            data.getInt(6);     // minute
//            data.getFpiInt(17); // Fuel
//            data.getFpiInt(5);  // AIRCRAFT SYSTEM STATUS
//            if (data.getGpi()) {
//                do {
//                    gri = data.getGri();
//                    data.getInt(7);     // ORDNANCE TYPE
//                    data.getInt(10);    // QUANTITY OF AMMUNITION
//                } while (gri);
//            }
//            data.getFpiInt(4);          // EMERGENCY TYPE
//            data.getFpiInt(4);          // PERSONNEL INVOLVED
//        } catch (Exception ex) {
//            return false;
//        }
//
        return true;
    }

    public Position getPosition() {
        return position;
    }

    public Position getObserverPosition() {
        return observerPosition;
    }

    public int getUrn() {
        return urn;
    }

    public int getEmergencyType() {
        return emergencyType;
    }

    public int getPersonnel() {
        return personnel;
    }

    public int getFuel() {
        return fuel;
    }

    public int getSysStatus() {
        return sysStatus;
    }

    public Calendar getTime() {
        return time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        if (getHeader().isAck()) {
            return getHeader().getAckText();
        }
        return super.getText();
    }

    /**
     * Return a printable Mayday Type
     *
     * @param type The Mayday Type
     * @return a printable Mayday Type
     */
    private String maydayType(int type) {
        return switch (type) {
            case 0 -> "OWN STATION MAYDAY/IP";
            case 1 -> "MAYDAY/IP, KNOWN UNIT WITH LAT/LONG";
            case 2 -> "MAYDAY/IP, UNKNOWN UNIT WITH LAT/LONG";
            case 3 -> "MAYDAY/IP, KNOWN UNIT LOCATION; RANGE AND BEARING (TRUE)";
            case 4 -> "MAYDAY/IP, UNKNOWN UNIT LOCATION; RANGE AND BEARING (TRUE)";
            case 5 -> "MAYDAY/IP, DELETE";
            default -> "UNDEFINED";
        };
    }
}
