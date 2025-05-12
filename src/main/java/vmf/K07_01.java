/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import static vmf.VmfMessage.CR;

/**
 * K07.01 Medical Evacuation (MEDEVAC) Request and Response
 *
 * @author hansen
 */
public class K07_01 extends VmfMessage {

    public class EnemyData {

        int direction, fireType;

        // 372/405
        public String getDirection() {
            String s = CR + "Direction: ";
            switch (direction) {
                case 0:
                    return s + "NORTH";
                case 1:
                    return s + "NORTH EAST";
                case 2:
                    return s + "EAST";
                case 3:
                    return s + "SOUTH EAST";
                case 4:
                    return s + "SOUTH";
                case 5:
                    return s + "SOUTH WEST";
                case 6:
                    return s + "WEST";
                case 7:
                    return s + "NORTH WEST";
                case 8:
                    return s + "SURROUNDED";
                default:
                    return "UNDEFINED (" + direction + ')';
            }

        }

// 4172 001        
        public String getFireType() {
            if (zoneColor == VmfDataBuffer.NO_INT) {
                return "";
            }
            String s = CR + "Fire Type: ";
            switch (fireType) {
                case 0:
                    return s + "SMALL ARMS";
                case 1:
                    return s + "MORTAR";
                case 2:
                    return s + "ARTILLERY";
                case 3:
                    return s + "ROCKETS";
                default:
                    return "UNDEFINED (" + fireType + ')';
            }

        }
    }

    public class Priority {

        int priority, number;

        String getPriority() {
            return getMisionPriority(priority);
        }
    }

    public class Casualty {

        int type, bodyPart, contamination;
        ArrayList<Integer> specialEquipment = new ArrayList<>();

        String getType() {
            String s = CR + "Type: ";
            switch (type) {
                case 0:
                    return s + "NON-BATTLE";
                case 1:
                    return s + "CUT";
                case 2:
                    return s + "BURN";
                case 3:
                    return s + "SICK";
                case 4:
                    return s + "FRACTURE";
                case 5:
                    return s + "AMPUTATION";
                case 6:
                    return s + "PERFORATION";
                case 7:
                    return s + "NUCLEAR";
                case 8:
                    return s + "EXHAUSTION";
                case 9:
                    return s + "BIOLOGICAL";
                case 10:
                    return s + "CHEMICAL";
                case 11:
                    return s + "SHOCK";
                case 12:
                    return s + "PUNCTURE WOUND";
                case 13:
                    return s + "OTHER";
                case 14:
                    return s + "WOUNDED IN ACTION";
                case 15:
                    return s + "DENTAL";
                case 16:
                    return s + "COMBAT STRESS";
                default:
                    return "UNDEFINED (" + type + ')';
            }
        }

        String getBodyPart() {
            if (bodyPart == VmfDataBuffer.NO_INT) {
                return "";
            }
            String s = CR + "Body Part: ";
            switch (bodyPart) {
                case 0:
                    return s + "HEAD";
                case 1:
                    return s + "NECK";
                case 2:
                    return s + "ABDOMEN";
                case 3:
                    return s + "UPPER EXTREMITIES";
                case 4:
                    return s + "BACK";
                case 5:
                    return s + "FACE";
                case 6:
                    return s + "CHEST";
                case 7:
                    return s + "LOWER EXTREMITIES";
                case 8:
                    return s + "FRONT";
                case 9:
                    return s + "OBSTETRICAL/GYNECOLOGICAL";
                case 10:
                    return s + "OTHER";
                default:
                    return s + "UNDEFINED (" + bodyPart + ")";
            }
        }

        String getContamination() {
            if (bodyPart == VmfDataBuffer.NO_INT) {
                return "";
            }
            String s = CR + "Contamination: ";
            switch (contamination) {
                case 0:
                    return s + "NONE";
                case 1:
                    return s + "RADIATION";
                case 2:
                    return s + "BIOLOGICAL";
                case 3:
                    return s + "CHEMICAL";
                default:
                    return s + "UNDEFINED (" + contamination + ")";
            }
        }

        String getEquipment(int i) {
            if (i == VmfDataBuffer.NO_INT) {
                return "";
            }
            String s = CR + "Equipment: ";
            switch (i) {
                case 0:
                    return s + "EXTRACTION EQUIPMENT";
                case 1:
                    return s + "SEMI-RIGID LITTER";
                case 2:
                    return s + "BACKBOARD";
                case 3:
                    return s + "CERVICAL COLLAR";
                case 4:
                    return s + "JUNGLE PENETRATOR";
                case 5:
                    return s + "OXYGEN";
                case 6:
                    return s + "WHOLE BLOOD";
                case 7:
                    return s + "VENTILATOR";
                case 8:
                    return s + "HOIST";
                case 9:
                    return s + "NONE";
                default:
                    return s + "UNDEFINED (" + i + ")";
            }
        }
    }

    // PATIENT PRIORITY
    ArrayList<Priority> priorities = new ArrayList<>();

    // CASUALTY info G2 
    int patientStatus, nationality, patientIdentity, medevacPriority;
    String casualtyKey;
    ArrayList<Casualty> casualties = new ArrayList<>();

    // ENEMY DATA G6
    ArrayList<EnemyData> enemeyData = new ArrayList<>();

    int medEvacType, urn, entityIdSn, missionType, friendlyKia, friendlyWia, numLitter, numAmb;
    int medicRequired, missionPriority, numPriority, weatherconditions, cloudBase, windSpeed;
    Calendar dtg;
    Position position = null;
    String requestorCallSign, requestNumber, pickupTime, zoneName, contactFreq, contactCallsign, comments;
    int terrainDescription, zoneSecurity, zoneMarking, zoneColor, zoneHot;

    public K07_01(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        dtg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        dtg.clear();
        position = null;
        parsedOk = parse(data);
    }

    private boolean parse(VmfDataBuffer data) {
        if (header.stdVersion <= VMF_6017A) {
            return parseVerA(data);
        } else if (header.stdVersion <= VMF_6017D) {
            return parseVerBCD(data);
        } else {
            return false;
        }
    }

    private boolean parseVerA(VmfDataBuffer data) {
        double latitude, longitude, altitude;
        boolean gri, fri;
        try {
            medEvacType = 0;
            requestorCallSign = data.getString(119);
            urn = data.getInt(24);
            dtg.set(data.getInt(7) + 2000, data.getInt(4) - 1, data.getInt(5), data.getInt(5), data.getInt(6));
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
                    p.priority = data.getInt(3); // priority
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
                double latitude, longitude, altitude = 0;
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

    @Override
    public String getSummary() {
        if (getHeader().isAck()) {
            return super.getSummary();
        } else {
            return super.getSummary() + (medEvacType == 0 ? "MEDEVAC Request" : "MEDEVAC Response");
        }
    }

    @Override
    public String getText() {
        if (header.stdVersion <= VMF_6017A) {
            return getTextA();
        } else if (header.stdVersion <= VMF_6017D) {
            return getTextBCD();
        } else {
            return "No body decode available.";
        }

    }

    public String getTextA() {
        if (header.isAck()) {
            return header.getAckText();
        }
        String s = CR + getSummary();
        if (!requestorCallSign.isEmpty()) {
            s += CR + "Requestor: " + requestorCallSign;
        }
        s += CR + "Urn: " + urn;
        s += CR + "Time: " + SDF_LONG.format(dtg.getTime());
        if (!requestNumber.isEmpty()) {
            s += CR + "Request Number: " + requestNumber;
        }
        if (position != null) {
            s += CR + String.format("Location: %.5f, %.5f, %.1fm",
                    position.latitude.degrees, position.longitude.degrees, position.elevation);
        }
        if (!comments.isEmpty()) {
            s += CR + "Comments: " + comments;
        }

        return s;
    }

    String optionalInt(int i, String name) {
        return (i == VmfDataBuffer.NO_INT) ? "" : name + i;
    }

    public String getTextBCD() {
        if (header.isAck()) {
            return header.getAckText();
        }
        String s = CR + getSummary();
        s += CR + "Requestor: " + requestorCallSign;

        s += CR + "Urn: " + urn;
        s += CR + "Entity ID SN: " + entityIdSn;
        s += CR + "Time: " + SDF_LONG.format(dtg.getTime());
        if (!requestNumber.isEmpty()) {
            s += CR + "Request Number: " + requestNumber;
        }
        s += optionalInt(friendlyKia, "Friendly KIA: ");
        s += optionalInt(friendlyWia, "Friendly WIA: ");
        s += CR + "Litter Patients: " + numLitter;
        s += CR + "Ambulatory Patients: " + numAmb;
        s += CR + "MedicRequired: " + (medicRequired == 1 ? "Yes" : "No");

        // priority
        for (Priority p : priorities) {
            s += CR + "  Priority: " + p.getPriority();
            s += CR + "  Number: " + p.number;
        }

        // Casulaty info
        if (casualties.size() > 0) {
            for (Casualty c : casualties) {
                s += c.getType();
                s += c.getBodyPart();
                s += c.getContamination();
                for (int i : c.specialEquipment) {
                    s += c.getEquipment(i);
                }
            }
            if (!casualtyKey.isEmpty()) {
                s += CR + "Casualty Key: " + casualtyKey;
            }
            s += getPatientStatus();
            s += VmfDictionary.getNationality(nationality);
            s += getPatientIdentity();

            s += CR + "Mission Priority: " + getMisionPriority(missionPriority);
        }
        // pickup time
        if (!pickupTime.isEmpty()) {
            s += CR + "Pickup Time: " + pickupTime;
        }

        // pickup location
        if (position != null) {
            s += CR + "Location: " + position;
            s += getTerrainDescription();
            s += zoneName.isEmpty() ? "" : CR + "Zone Name: " + zoneName;
            s += getZoneSecurity();
            s += getZoneMarking();
            s += getZoneColor();
            s += CR + "Zone Hot: " + ((zoneHot == 1) ? "true" : "false");
        }

        // contact info
        s += contactFreq.isEmpty() ? "" : CR + "Contact Freq Des: " + contactFreq;
        s += contactCallsign.isEmpty() ? "" : CR + "Contact Callsign: " + contactCallsign;
        // enemy data
        for (EnemyData e : enemeyData) {
            s += CR + "Direction: " + e.direction;
            s += CR + "Fire Type: " + e.fireType;
        }
        // weather
        if (!comments.isEmpty()) {
            s += CR + "Comments: " + comments;
        }

        return s;
    }

    // 4122/004
    String getPatientStatus() {
        if (patientStatus == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = CR + "Patient Status: ";
        switch (patientStatus) {
            case 0:
                return s + "U.S. MILITARY";
            case 1:
                return s + "U.S. CIVILIAN";
            case 2:
                return s + "NON U.S. MILITARY";
            case 3:
                return s + "NON U.S. CIVILIAN";
            case 4:
                return s + "ENEMY POW";
            default:
                return s + "UNDEFINED (" + patientStatus + ')';
        }
    }

    // 376 402
    String getPatientIdentity() {
        if (patientIdentity == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = CR + "Patient Identity: ";
        switch (patientIdentity) {
            case 0:
                return s + "UNKNOWN MILITARY";
            case 1:
                return s + "UNKNOWN CIVILIAN";
            case 2:
                return s + "FRIEND MILITARY";
            case 3:
                return s + "FRIEND CIVILIAN";
            case 4:
                return s + "NEUTRAL MILITARY";
            case 5:
                return s + "NEUTRAL CIVILIAN";
            case 6:
                return s + "HOSTILE MILITARY";
            case 7:
                return s + "HOSTILE CIVILIAN";
            default:
                return "UNDEFINED (" + patientIdentity + ')';
        }
    }

    public String getMisionPriority(int missionPriority) {
        switch (missionPriority) {
            case 0:
                return "URGENT";
            case 1:
                return "PRIORITY";
            case 2:
                return "ROUTINE";
            case 3:
                return "URGENT SURGERY";
            case 4:
                return "CONVENIENCE";
            default:
                return "UNDEFINED (" + missionPriority + ")";
        }
    }

    // 4115/002
    public String getTerrainDescription() {
        if (terrainDescription == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = CR + "Terrain: ";
        switch (terrainDescription) {
            case 0:
                return s + "WOODS";
            case 1:
                return s + "TREES";
            case 2:
                return s + "PLOWED FIELDS";
            case 3:
                return s + "FLAT";
            case 4:
                return s + "STANDING WATER";
            case 5:
                return s + "MARSH";
            case 6:
                return s + "URBAN/BUILT-UP AREA";
            case 7:
                return s + "MOUNTAIN";
            case 8:
                return s + "HILL";
            case 9:
                return s + "SAND";
            case 10:
                return s + "ROCKY";
            case 11:
                return s + "VALLEY";
            case 12:
                return s + "METAMORPHIC ICE";
            case 13:
                return s + "UNKNOWN";
            case 14:
                return s + "SEA";
            case 15:
                return s + "NO STATEMENT";
            default:
                return "UNDEFINED (" + terrainDescription + ")";
        }
    }

    // 4136 001
    public String getZoneSecurity() {
        if (zoneSecurity == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = CR + "Terrain: ";
        switch (zoneSecurity) {
            case 0:
                return s + "UNKNOWN";
            case 1:
                return s + "NO ENEMY";
            case 2:
                return s + "POSSIBLE ENEMY";
            case 3:
                return s + "ENEMY IN AREA - USE CAUTION";
            case 4:
                return s + "ENEMY IN AREA - ARMED ESCORT REQUIRED";
            default:
                return "UNDEFINED (" + zoneSecurity + ")";
        }
    }

    // 4105 003
    public String getZoneMarking() {
        if (zoneMarking == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = CR + "Terrain: ";
        switch (zoneMarking) {
            case 0:
                return s + "SMOKE";
            case 1:
                return s + "FLARES";
            case 2:
                return s + "MIRROR";
            case 3:
                return s + "GLIDE ANGLE INDICATOR LIGHT";
            case 4:
                return s + "LIGHT";
            case 5:
                return s + "PANELS";
            case 6:
                return s + "FIRE";
            case 7:
                return s + "LASER DESIGNATOR";
            case 8:
                return s + "STROBE LIGHTS";
            case 9:
                return s + "VEHICLE LIGHTS";
            case 10:
                return s + "COLORED SMOKE";
            case 11:
                return s + "WHITE PHOSPHORUS";
            case 12:
                return s + "INFRARED";
            case 13:
                return s + "ILLUMINATION";
            case 14:
                return s + "FRATRICIDE FENCE";
            default:
                return "UNDEFINED (" + zoneMarking + ")";
        }
    }

    // 4118 001
    public String getZoneColor() {
        if (zoneColor == VmfDataBuffer.NO_INT) {
            return "";
        }
        String s = CR + "Terrain: ";
        switch (zoneColor) {
            case 0:
                return s + "RED";
            case 1:
                return s + "WHITE";
            case 2:
                return s + "BLUE";
            case 3:
                return s + "YELLOW";
            case 4:
                return s + "GREEN";
            case 5:
                return s + "ORANGE";
            case 6:
                return s + "BLACK";
            case 7:
                return s + "PURPLE";
            case 8:
                return s + "BROWN";
            case 9:
                return s + "TAN";
            case 10:
                return s + "GRAY";
            case 11:
                return s + "SILVER";
            case 12:
                return s + "CAMOUFLAGE";
            case 13:
                return s + "OTHER";
            default:
                return "UNDEFINED (" + zoneColor + ")";
        }
    }

}
