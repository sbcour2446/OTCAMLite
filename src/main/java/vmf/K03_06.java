/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import gov.nasa.worldwind.geom.Position;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * K03.06 MAYDAY Message
 *
 * @author hansen
 */
public class K03_06 extends VmfMessage {

    Position position, observerPosition;
    int urn, emergencyType, personnel, fuel, sysStatus;
    Calendar time;

    public K03_06(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        this.time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        position = observerPosition = Position.ZERO;
        parsedOk = parse(data);
    }

    private boolean parse(VmfDataBuffer data) {
        switch (header.stdVersion) {
            case VMF_6017:
            case VMF_6017A:
                return Parse6017A(data);
            case VMF_6017B:
                return Parse6017B(data);
            default:
                return Parse6017D(data);
        }
    }

    private boolean Parse6017D(VmfDataBuffer data) {
        boolean gri, fri;
        double latitude, longitude, altitude;
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

        } catch (Exception ex) {

            return false;
        }

        return false;
    }

    private boolean Parse6017A(VmfDataBuffer data) {
        double latitude, longitude;
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
            time.set(header.year, header.month, day, hour, min);
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
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    private boolean Parse6017B(VmfDataBuffer data) {
        double latitude, longitude;
        boolean gri;
        try { // TODO complete the K03.06 MAYDAY Message for 6017B
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
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    @Override
    public String getSummary() {
        return super.getSummary();
    }

    @Override
    public String getText() {
        if (getHeader().isAck()) {
            return getHeader().getAckText();
        }
        return super.getText();
    }

    String maydayType(int type) {
        switch (type) {
            case 0:
                return "OWN STATION MAYDAY/IP";
            case 1:
                return "MAYDAY/IP, KNOWN UNIT WITH LAT/LONG";
            case 2:
                return "MAYDAY/IP, UNKNOWN UNIT WITH LAT/LONG";
            case 3:
                return "MAYDAY/IP, KNOWN UNIT LOCATION; RANGE AND BEARING (TRUE)";
            case 4:
                return "MAYDAY/IP, UNKNOWN UNIT LOCATION; RANGE AND BEARING (TRUE)";
            case 5:
                return "MAYDAY/IP, DELETE";
            default:
                return "UNDEFINED";
        }
    }
}
