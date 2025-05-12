package gov.mil.otc._3dvis.vmf;

import gov.nasa.worldwind.geom.Position;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * The VMF K07.01 Medical Evacuation (MEDEVAC) Request and Response Class
 */
public class K0701 extends VmfMessage {

    // Patient Priority
    //
    private final List<Priority> priorities = new ArrayList<>();

    // Casualty info G2
    //
    private int patientStatus;
    private int nationality;
    private int patientIdentity;
    private int medevacPriority;
    private String casualtyKey;
    private final List<Casualty> casualties = new ArrayList<>();

    // Enemy Data G6
    //
    private final List<EnemyData> enemeyData = new ArrayList<>();

    private int medEvacType;
    private int urn;
    private int entityIdSn;
    private int missionType;
    private int friendlyKia;
    private int friendlyWia;
    private int numLitter;
    private int numAmb;
    private int medicRequired;
    private int missionPriority;
    private int numPriority;
    private int weatherconditions;
    private int cloudBase;
    private int windSpeed;
    private Calendar dtg;
    private Position position;
    private String requestorCallSign;
    private String requestNumber;
    private String pickupTime;
    private String zoneName;
    private String contactFreq;
    private String contactCallsign;
    private String comments;
    private int terrainDescription;
    private int zoneSecurity;
    private int zoneMarking;
    private int zoneColor;
    private int zoneHot;

    /**
     * {@inheritDoc}
     */
    protected K0701(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
        dtg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        dtg.clear();
        position = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        if (header.getStdVersion() <= VMF_6017A) {
            return parseVerA(data);
        } else if (header.getStdVersion() <= VMF_6017D) {
            return parseVerBCD(data);
        } else {
            return false;
        }
    }

    /**
     * Message Parser for Version A
     *
     * @param data VmfDataBuffer data
     * @return true if successful parse otherwise, false
     */
    private boolean parseVerA(VmfDataBuffer data) {
        double latitude;
        double longitude;
        double altitude;
        boolean gri;
        boolean fri;

        try {
            medEvacType = 0;
            requestorCallSign = data.getString(119);
            urn = data.getInt(24);
            dtg.set(data.getInt(7) + 2000, data.getInt(4) - 1,
                    data.getInt(5),
                    data.getInt(5),
                    data.getInt(6));
            requestNumber = data.getFpiString(35);
            missionType = data.getInt(2);
            friendlyKia = data.getFpiInt(14);
            friendlyWia = data.getFpiInt(14);
            do {
                gri = data.getGri();
                missionPriority = data.getInt(3);
                numLitter = data.getInt(7);
                numAmb = data.getInt(7);
                data.getFpiInt(5); // type
                data.getFpiInt(4); // BODY PART AFFECTED
                medicRequired = data.getInt(1);
                if (data.getFpi()) {
                    do {
                        fri = data.getGri();
                        data.getInt(3); // SPECIAL MEDEVAC EQUIPMENT
                    } while (fri);
                }
            } while (gri);
            data.getFpiInt(3);  // NATIONALITY
            data.getFpiInt(2); // NBC CONTAMINATION TYPE

            if (data.getGpi()) {  // pickup time G1
                data.getInt(5);
                data.getInt(5);
                data.getInt(6);
            }

            data.getFpiString(63);  // ZONE NAME

            if (data.getGpi()) {  // pickup location G4
                latitude = data.getInt(21) * LAT_CONVERSION_21BIT;
                longitude = data.getInt(22) * LON_CONVERSION_22BIT;
                if (longitude > 180.0) {
                    longitude -= 360;
                }
            } else {
                latitude = longitude = 0;
            }
            if (data.getFpi()) {
                altitude = data.getInt(17) * FEET_TO_METERS;
            } else {
                altitude = 0;
            }
            if (latitude != 0) {
                position = Position.fromDegrees(latitude, longitude, altitude);
            }
            data.getFpiString(56);  // AGENCY CONTACT FREQUENCY DESIGNATOR
            data.getFpiString(119); // ZONE CONTROLLER CALL SIGN
            data.getFpiInt(4);      // ZONE MARKING
            data.getInt(1);         // ZONE HOT

            if (data.getGpi()) {  // ENEMY DATA G3
                do {
                    gri = data.getGri();
                    data.getInt(4);     // DIRECTION TO THE ENEMY
                    data.getFpiInt(2);  // HOSTILE FIRE TYPE RECEIVED
                } while (gri);
            }
            data.getFpiInt(3);      // ZONE SECURITY
            data.getFpiInt(4);      // ZONE MARKING
            data.getFpiInt(4);      // ZONE MARKING COLOR

            weatherconditions = data.getFpiInt(5);  // WEATHER CONDITIONS
            cloudBase = data.getFpiInt(9);          // CLOUD BASE ALTITUDE, FEET
            windSpeed = data.getFpiInt(7);          // WIND SPEED
            comments = data.getFpiString(1400);

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Message Parser for Binary Coded Decimal Version
     *
     * @param data VmfDataBuffer data
     * @return true if successful parse otherwise, false
     */
    private boolean parseVerBCD(VmfDataBuffer data) {
        try {
            medEvacType = data.getInt(1);
            requestorCallSign = data.getString(119);
            urn = data.getInt(24);
            entityIdSn = data.getInt(32);
            dtg.set(data.getInt(7) + 2000, data.getInt(4) - 1, data.getInt(5), data.getInt(5), data.getInt(6));
            requestNumber = data.getFpiString(35);
            missionType = data.getInt(2);
            friendlyKia = data.getFpiInt(14);
            friendlyWia = data.getFpiInt(14);
            numLitter = data.getInt(7);
            numAmb = data.getInt(7);
            medicRequired = data.getInt(1);
            if (data.getGpi()) { // PATIENT PRIORITY
                boolean gri;
                do {
                    Priority p = new Priority();
                    gri = data.getGri();
                    p.intPriority = data.getInt(3); // priority
                    p.number = data.getInt(7); // number at this priority
                    priorities.add(p);
                } while (gri);
            }

            if (data.getGpi()) {  // CASUALTY info G2
                boolean gri;
                do {
                    gri = data.getGri();
                    Casualty c = new Casualty();
                    c.type = data.getInt(5); // type
                    c.bodyPart = data.getFpiInt(4); // BODY PART AFFECTED
                    c.contamination = data.getFpiInt(2); // NBC CONTAMINATION TYPE
                    if (data.getFpi()) {
                        boolean fri;
                        do {
                            fri = data.getGri();
                            c.specialEquipment.add(data.getInt(4)); // SPECIAL MEDEVAC EQUIPMENT
                        } while (fri);
                    }
                    casualties.add(c);
                } while (gri);
                casualtyKey = data.getFpiString(168); // CASUALTY KEY
                patientStatus = data.getFpiInt(3);  // PATIENT STATUS TYPE
                nationality = data.getFpiInt(9);  // NATIONALITY
                patientIdentity = data.getFpiInt(3);  // IDENTITY, PATIENT
                medevacPriority = data.getInt(3);     // MEDEVAC MISSION PRIORITY
            }

            if (data.getGpi()) {  // pickup time G3
                int day = data.getInt(5);     // day
                int hour = data.getInt(5);     // hour
                int min = data.getInt(6);     // minute
                pickupTime = String.format("%d %02d:%02d", day, hour, min);
            }

            if (data.getGpi()) {  // pickup location G4
                double latitude;
                double longitude;
                double altitude;
                latitude = data.getInt(21) * LAT_CONVERSION_21BIT;
                longitude = data.getInt(22) * LON_CONVERSION_22BIT;
                if (longitude > 180) {
                    longitude -= 180.0;
                }
                if (data.getFpi()) {
                    altitude = data.getInt(17) * FEET_TO_METERS;
                } else {
                    altitude = 0;
                }
                position = Position.fromDegrees(latitude, longitude, altitude);
                terrainDescription = data.getFpiInt(4);      // TERRAIN DESCRIPTION
                zoneName = data.getFpiString(63);  // ZONE NAME
                zoneSecurity = data.getFpiInt(3);      // ZONE SECURITY
                zoneMarking = data.getFpiInt(4);      // ZONE MARKING
                zoneColor = data.getFpiInt(4);      // ZONE MARKING COLOR
                zoneHot = data.getInt(1);         // ZONE HOT
            }

            if (data.getGpi()) {  // CONTACT INFORMATION G5
                contactFreq = data.getString(56);     // AGENCY CONTACT FREQUENCY DESIGNATOR
                contactCallsign = data.getFpiString(119); // ZONE CONTROLLER CALL SIGN
            }

            if (data.getGpi()) {  // ENEMY DATA G6
                boolean gri;
                do {
                    gri = data.getGri();
                    EnemyData e = new EnemyData();
                    e.direction = data.getInt(4);     // DIRECTION TO THE ENEMY
                    e.fireType = data.getFpiInt(2);  // HOSTILE FIRE TYPE RECEIVED
                    enemeyData.add(e);
                } while (gri);
            }

            weatherconditions = data.getFpiInt(5);  // WEATHER CONDITIONS
            cloudBase = data.getFpiInt(9);          // CLOUD BASE ALTITUDE, FEET
            windSpeed = data.getFpiInt(7);          // WIND SPEED
            comments = data.getFpiString(1400);

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public class EnemyData {

        private int direction;
        private int fireType;

        /**
         * Return a printable Fire Direction (DFI/DUI 372/405)
         *
         * @return a printable Fire Direction
         */
        public String getDirection() {
            String s = System.lineSeparator() + "Direction: ";
            return switch (direction) {
                case 0 -> s + "NORTH";
                case 1 -> s + "NORTH EAST";
                case 2 -> s + "EAST";
                case 3 -> s + "SOUTH EAST";
                case 4 -> s + "SOUTH";
                case 5 -> s + "SOUTH WEST";
                case 6 -> s + "WEST";
                case 7 -> s + "NORTH WEST";
                case 8 -> s + "SURROUNDED";
                default -> System.lineSeparator() + " (" + direction + ')';
            };

        }

        /**
         * Return a printable Fire Type (DFI/DUI 4172 001)
         *
         * @return a printable Fire Type
         */
        public String getFireType() {
            if (zoneColor == VmfDataBuffer.NO_INT) {
                return "";
            }
            String s = System.lineSeparator() + "Fire Type: ";
            return switch (fireType) {
                case 0 -> s + "SMALL ARMS";
                case 1 -> s + "MORTAR";
                case 2 -> s + "ARTILLERY";
                case 3 -> s + "ROCKETS";
                default -> "UNDEFINED" + " (" + fireType + ')';
            };

        }
    }

    /**
     * The Priority Class
     */
    public class Priority {
        int intPriority;
        int number;

        String getIntPriority() {
            return getMisionPriority(intPriority);
        }
    }

    /**
     * The Casualty Class
     */
    public class Casualty {

        int type;
        int bodyPart;
        int contamination;

        ArrayList<Integer> specialEquipment = new ArrayList<>();

        String getType() {
            String s = System.lineSeparator() + "Type: ";
            return switch (type) {
                case 0 -> s + "NON-BATTLE";
                case 1 -> s + "CUT";
                case 2 -> s + "BURN";
                case 3 -> s + "SICK";
                case 4 -> s + "FRACTURE";
                case 5 -> s + "AMPUTATION";
                case 6 -> s + "PERFORATION";
                case 7 -> s + "NUCLEAR";
                case 8 -> s + "EXHAUSTION";
                case 9 -> s + "BIOLOGICAL";
                case 10 -> s + "CHEMICAL";
                case 11 -> s + "SHOCK";
                case 12 -> s + "PUNCTURE WOUND";
                case 13 -> s + "OTHER";
                case 14 -> s + "WOUNDED IN ACTION";
                case 15 -> s + "DENTAL";
                case 16 -> s + "COMBAT STRESS";
                default -> "UNDEFINED (" + type + ')';
            };
        }

        /**
         * Return a printable Body Part
         *
         * @return a printable Body Part
         */
        String getBodyPart() {
            if (bodyPart == VmfDataBuffer.NO_INT) {
                return "";
            }
            String s = System.lineSeparator() + "Body Part: ";
            return switch (bodyPart) {
                case 0 -> s + "HEAD";
                case 1 -> s + "NECK";
                case 2 -> s + "ABDOMEN";
                case 3 -> s + "UPPER EXTREMITIES";
                case 4 -> s + "BACK";
                case 5 -> s + "FACE";
                case 6 -> s + "CHEST";
                case 7 -> s + "LOWER EXTREMITIES";
                case 8 -> s + "FRONT";
                case 9 -> s + "OBSTETRICAL/GYNECOLOGICAL";
                case 10 -> s + "OTHER";
                default -> s + "UNDEFINED (" + bodyPart + ")";
            };
        }

        /**
         * Return a printable Contamination
         *
         * @return a printable Contamination
         */
        String getContamination() {
            if (bodyPart == VmfDataBuffer.NO_INT) {
                return "";
            }
            String s = System.lineSeparator() + "Contamination: ";
            return switch (contamination) {
                case 0 -> s + "NONE";
                case 1 -> s + "RADIATION";
                case 2 -> s + "BIOLOGICAL";
                case 3 -> s + "CHEMICAL";
                default -> s + "UNDEFINED (" + contamination + ")";
            };
        }

        /**
         * Return a printable Equipment
         *
         * @param equipment Equipment Index
         * @return a printable Equipment
         */
        String getEquipment(int equipment) {
            if (equipment == VmfDataBuffer.NO_INT) {
                return "";
            }
            String s = System.lineSeparator() + "Equipment: ";
            return switch (equipment) {
                case 0 -> s + "EXTRACTION EQUIPMENT";
                case 1 -> s + "SEMI-RIGID LITTER";
                case 2 -> s + "BACKBOARD";
                case 3 -> s + "CERVICAL COLLAR";
                case 4 -> s + "JUNGLE PENETRATOR";
                case 5 -> s + "OXYGEN";
                case 6 -> s + "WHOLE BLOOD";
                case 7 -> s + "VENTILATOR";
                case 8 -> s + "HOIST";
                case 9 -> s + "NONE";
                default -> s + "UNDEFINED (" + equipment + ")";
            };
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        if (getHeader().isAck()) {
            return super.getSummary();
        } else {
            return super.getSummary() + (medEvacType == 0 ? "MEDEVAC Request" : "MEDEVAC Response");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        if (header.getStdVersion() <= VMF_6017A) {
            return getTextA();
        } else if (header.getStdVersion() <= VMF_6017D) {
            return getTextBCD();
        } else {
            return "No body decode available.";
        }

    }

    /**
     * Return the printable Message Text
     *
     * @return the printable Message Text
     */
    public String getTextA() {
        if (header.isAck()) {
            return header.getAckText();
        }
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = System.lineSeparator() + getSummary();
        if (!requestorCallSign.isEmpty()) {
            s += System.lineSeparator() + "Requestor: " + requestorCallSign;
        }
        s += System.lineSeparator() + "Urn: " + urn;
        s += System.lineSeparator() + "Time: " + sdfLong.format(dtg.getTime());
        if (!requestNumber.isEmpty()) {
            s += System.lineSeparator() + "Request Number: " + requestNumber;
        }
        if (position != null) {
            s += System.lineSeparator() + String.format("Location: %.5f, %.5f, %.1fm",
                    position.latitude.degrees, position.longitude.degrees, position.elevation);
        }
        if (!comments.isEmpty()) {
            s += System.lineSeparator() + "Comments: " + comments;
        }

        return s;
    }

    /**
     * Return the printable Optional Integer
     *
     * @param optionalInt the Optional Integer
     * @param name        The Name
     * @return the printable Optional Integer
     */
    String optionalInt(int optionalInt, String name) {
        return (optionalInt == VmfDataBuffer.NO_INT) ? "" : name + optionalInt;
    }

    public String getTextBCD() {
        if (header.isAck()) {
            return header.getAckText();
        }
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = System.lineSeparator() + getSummary();
        s += System.lineSeparator() + "Requestor: " + requestorCallSign;

        s += System.lineSeparator() + "Urn: " + urn;
        s += System.lineSeparator() + "Entity ID SN: " + entityIdSn;
        s += System.lineSeparator() + "Time: " + sdfLong.format(dtg.getTime());
        if (!requestNumber.isEmpty()) {
            s += System.lineSeparator() + "Request Number: " + requestNumber;
        }
        s += optionalInt(friendlyKia, "Friendly KIA: ");
        s += optionalInt(friendlyWia, "Friendly WIA: ");
        s += System.lineSeparator() + "Litter Patients: " + numLitter;
        s += System.lineSeparator() + "Ambulatory Patients: " + numAmb;
        s += System.lineSeparator() + "MedicRequired: " + (medicRequired == 1 ? "Yes" : "No");

        // priority
        for (Priority p : priorities) {
            s += System.lineSeparator() + "  Priority: " + p.getIntPriority();
            s += System.lineSeparator() + "  Number: " + p.number;
        }

        // Casulaty info
        if (!casualties.isEmpty()) {
            for (Casualty c : casualties) {
                s += c.getType();
                s += c.getBodyPart();
                s += c.getContamination();
                for (int i : c.specialEquipment) {
                    s += c.getEquipment(i);
                }
            }
            if (!casualtyKey.isEmpty()) {
                s += System.lineSeparator() + "Casualty Key: " + casualtyKey;
            }
            s += getPatientStatus();
            s += VmfDictionary.getNationality(nationality);
            s += getPatientIdentity();

            s += System.lineSeparator() + "Mission Priority: " + getMisionPriority(missionPriority);
        }
        // pickup time
        if (!pickupTime.isEmpty()) {
            s += System.lineSeparator() + "Pickup Time: " + pickupTime;
        }

        // pickup location
        if (position != null) {
            s += System.lineSeparator() + "Location: " + position;
            s += getTerrainDescription();
            s += zoneName.isEmpty() ? "" : System.lineSeparator() + "Zone Name: " + zoneName;
            s += getZoneSecurity();
            s += getZoneMarking();
            s += getZoneColor();
            s += System.lineSeparator() + "Zone Hot: " + ((zoneHot == 1) ? "true" : "false");
        }

        // contact info
        s += contactFreq.isEmpty() ? "" : System.lineSeparator() + "Contact Freq Des: " + contactFreq;
        s += contactCallsign.isEmpty() ? "" : System.lineSeparator() + "Contact Callsign: " + contactCallsign;
        // enemy data
        for (EnemyData e : enemeyData) {
            s += System.lineSeparator() + "Direction: " + e.direction;
            s += System.lineSeparator() + "Fire Type: " + e.fireType;
        }
        // weather
        if (!comments.isEmpty()) {
            s += System.lineSeparator() + "Comments: " + comments;
        }

        return s;
    }

    /**
     * Return a printable Patient Status (DFI/DUI 4122/004)
     *
     * @return a printable Patient Status
     */
    String getPatientStatus() {
        if (patientStatus == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = System.lineSeparator() + "Patient Status: ";
        return switch (patientStatus) {
            case 0 -> s + "U.S. MILITARY";
            case 1 -> s + "U.S. CIVILIAN";
            case 2 -> s + "NON U.S. MILITARY";
            case 3 -> s + "NON U.S. CIVILIAN";
            case 4 -> s + "ENEMY POW";
            default -> s + "UNDEFINED (" + patientStatus + ')';
        };
    }

    /**
     * Return a printable Patient Identity (DFI/DUI 376/402)
     *
     * @return a printable Patient Identity
     */
    String getPatientIdentity() {
        if (patientIdentity == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = System.lineSeparator() + "Patient Identity: ";
        return switch (patientIdentity) {
            case 0 -> s + "UNKNOWN MILITARY";
            case 1 -> s + "UNKNOWN CIVILIAN";
            case 2 -> s + "FRIEND MILITARY";
            case 3 -> s + "FRIEND CIVILIAN";
            case 4 -> s + "NEUTRAL MILITARY";
            case 5 -> s + "NEUTRAL CIVILIAN";
            case 6 -> s + "HOSTILE MILITARY";
            case 7 -> s + "HOSTILE CIVILIAN";
            default -> "UNDEFINED (" + patientIdentity + ')';
        };
    }

    /**
     * Return a printable Mision Priority
     *
     * @param missionPriority the Mission Priority
     * @return a printable Mision Priority
     */
    public String getMisionPriority(int missionPriority) {
        return switch (missionPriority) {
            case 0 -> "URGENT";
            case 1 -> "PRIORITY";
            case 2 -> "ROUTINE";
            case 3 -> "URGENT SURGERY";
            case 4 -> "CONVENIENCE";
            default -> "UNDEFINED (" + missionPriority + ")";
        };
    }

    /**
     * Return a printable Terrain Description (DFI/DUI 4115/002)
     *
     * @return a printable Terrain Description
     */
    public String getTerrainDescription() {
        if (terrainDescription == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = System.lineSeparator() + "Terrain";
        return switch (terrainDescription) {
            case 0 -> s + "WOODS";
            case 1 -> s + "TREES";
            case 2 -> s + "PLOWED FIELDS";
            case 3 -> s + "FLAT";
            case 4 -> s + "STANDING WATER";
            case 5 -> s + "MARSH";
            case 6 -> s + "URBAN/BUILT-UP AREA";
            case 7 -> s + "MOUNTAIN";
            case 8 -> s + "HILL";
            case 9 -> s + "SAND";
            case 10 -> s + "ROCKY";
            case 11 -> s + "VALLEY";
            case 12 -> s + "METAMORPHIC ICE";
            case 13 -> s + "UNKNOWN";
            case 14 -> s + "SEA";
            case 15 -> s + "NO STATEMENT";
            default -> "UNDEFINED (" + terrainDescription + ")";
        };
    }

    /**
     * Return a printable Zone Security (DFI/DUI 4136/001)
     *
     * @return a printable Zone Security
     */
    public String getZoneSecurity() {
        if (zoneSecurity == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = System.lineSeparator() + "Terrain";
        return switch (zoneSecurity) {
            case 0 -> s + "UNKNOWN";
            case 1 -> s + "NO ENEMY";
            case 2 -> s + "POSSIBLE ENEMY";
            case 3 -> s + "ENEMY IN AREA - USE CAUTION";
            case 4 -> s + "ENEMY IN AREA - ARMED ESCORT REQUIRED";
            default -> "UNDEFINED (" + zoneSecurity + ")";
        };
    }

    /**
     * Return a printable Zone Marking (DFI/DUI 4105/003)
     *
     * @return a printable Zone Marking
     */
    public String getZoneMarking() {
        if (zoneMarking == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = System.lineSeparator() + "Terrain";
        return switch (zoneMarking) {
            case 0 -> s + "SMOKE";
            case 1 -> s + "FLARES";
            case 2 -> s + "MIRROR";
            case 3 -> s + "GLIDE ANGLE INDICATOR LIGHT";
            case 4 -> s + "LIGHT";
            case 5 -> s + "PANELS";
            case 6 -> s + "FIRE";
            case 7 -> s + "LASER DESIGNATOR";
            case 8 -> s + "STROBE LIGHTS";
            case 9 -> s + "VEHICLE LIGHTS";
            case 10 -> s + "COLORED SMOKE";
            case 11 -> s + "WHITE PHOSPHORUS";
            case 12 -> s + "INFRARED";
            case 13 -> s + "ILLUMINATION";
            case 14 -> s + "FRATRICIDE FENCE";
            default -> "UNDEFINED (" + zoneMarking + ")";
        };
    }

    /**
     * Return a printable Zone Color (DFI/DUI 4118/001)
     *
     * @return a printable Zone Color
     */
    public String getZoneColor() {
        if (zoneColor == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = System.lineSeparator() + "Terrain";
        return switch (zoneColor) {
            case 0 -> s + "RED";
            case 1 -> s + "WHITE";
            case 2 -> s + "BLUE";
            case 3 -> s + "YELLOW";
            case 4 -> s + "GREEN";
            case 5 -> s + "ORANGE";
            case 6 -> s + "BLACK";
            case 7 -> s + "PURPLE";
            case 8 -> s + "BROWN";
            case 9 -> s + "TAN";
            case 10 -> s + "GRAY";
            case 11 -> s + "SILVER";
            case 12 -> s + "CAMOUFLAGE";
            case 13 -> s + "OTHER";
            default -> "UNDEFINED (" + zoneColor + ")";
        };
    }
}
