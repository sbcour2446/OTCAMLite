/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import java.util.HashMap;
import java.util.Map;

public enum MsgType {
    // NETWORK CONTROL MESSAGES
    K00_01("K00.01", "Network Monitoring "),
    K00_02("K00.02", "System Coordination "),
    K00_03("K00.03", "Security "),
    K00_04("K00.04", "System Configuration "),
    // GENERAL INFORMATION EXCHANGE MESSAGES"
    K01_01("K01.01", "Free Text "),
    K01_02("K01.02", "Unit Reference Query/Response "),
    K01_03("K01.03", "Information Request "),
    K01_04("K01.04", "Geographical Reference Data "),
    K01_05("K01.05", "Network Assisted GPS Data "),
    K01_06("K01.06", "VMF Unit or Entity Reference to Link 16 Reference Identifier "),
    // FIRE SUPPORT OPERATION MESSAGES
    K02_01("K02.01", "Check Fire "),
    K02_02("K02.02", "Registration Data "),
    K02_03("K02.03", "Fire Support Meteorological Data "),
    K02_04("K02.04", "Call for Fire "),
    K02_05("K02.05", "Shell Report "),
    K02_06("K02.06", "Observer Mission Update "),
    K02_07("K02.07", "Survey Control Point "),
    K02_08("K02.08", "Schedule of Fires "),
    K02_09("K02.09", "Target Data "),
    K02_10("K02.10", "Fire Plan Mission/Fire Plan Cancellation "),
    K02_11("K02.11", "Ammunition Inventory "),
    K02_12("K02.12", "Command to Fire "),
    K02_13("K02.13", "Mission Clearance "),
    K02_14("K02.14", "Message to Observer "),
    K02_15("K02.15", "Fire Support Coordination Measures "),
    K02_16("K02.16", "End of Mission and Surveillance "),
    K02_17("K02.17", "Command and Control (C2) System Fire Mission Processing "),
    K02_18("K02.18", "Fire Unit Status "),
    K02_19("K02.19", "Target Query/Standing Request for Information "),
    K02_20("K02.20", "Survey Control Point Information Request "),
    K02_21("K02.21", "Request for Clearance to Fire "),
    K02_22("K02.22", "Subsequent Adjust "),
    K02_23("K02.23", "Fire Plan Orders "),
    K02_24("K02.24", "In Progress Mission Notification "),
    K02_25("K02.25", "End of Mission Notification "),
    K02_27("K02.27", "Close Air Support Request "),
    K02_28("K02.28", "Close Air Support Mission Battle Damage Assessment (CASBDA)Report "),
    K02_31("K02.31", "Mission Request Rejection "),
    K02_32("K02.32", "Close Air Support Request Acceptance "),
    K02_33("K02.33", "Close Air Support Aircrew Briefing "),
    K02_34("K02.34", "Aircraft On-Station "),
    K02_35("K02.35", "Aircraft Depart Initial Point "),
    K02_36("K02.36", "Aircraft Mission Update "),
    K02_37("K02.37", "Observer Readiness Report "),
    K02_38("K02.38", "Target Handover "),
    K02_39("K02.39", "Fire Support Mission Graphics "),
    K02_40("K02.40", "Cannon/Mortar Fire Orders "),
    K02_41("K02.41", "Fire Support Unit Deployment Command "),
    K02_42("K02.42", "Fire Plan Assignment Data "),
    K02_43("K02.43", "Rocket/Missile Munitions Effects Data "),
    K02_44("K02.44", "Target Element Data Entry "),
    K02_45("K02.45", "Rocket/Missile Launcher Orders "),
    K02_46("K02.46", "Rocket/Missile Operational Status Update "),
    K02_47("K02.47", "Launcher Configuration Update "),
    K02_48("K02.48", "Commander?s Fire Unit Guidance "),
    K02_49("K02.49", "Commander?s Fire Mission Guidance "),
    K02_50("K02.50", "Commander?s Target Acquisition Guidance "),
    K02_51("K02.51", "Fire Support Reply/Remarks "),
    K02_54("K02.54", "Howitzer Communications Initialization Data "),
    K02_55("K02.55", "Record of Fire "),
    K02_56("K02.56", "Fire Support Database Exchange "),
    K02_57("K02.57", "Aircraft Attack Position and Target Designation "),
    K02_58("K02.58", "CAS Aircraft Final Attack Control "),
    K02_59("K02.59", "Request for K02.57, Aircraft Attack Position and Target Designation "),
    K02_60("K02.60", "In-Flight Missile Status Report "),
    K02_61("K02.61", "Non-Line of Sight (NLOS)-Launch System (LS) Command "),
    // AIR OPERATIONS MESSAGES"
    K03_02("K03.02", "Engagement Report/Battle Damage Assessment "),
    K03_04("K03.04", "Assault Support Request "),
    K03_06("K03.06", "MAYDAY "),
    K03_07("K03.07", "Airspace Control Means Request/Reply "),
    // INTELLIGENCE OPERATIONS MESSAGES"
    K04_01("K04.01", "Observation Report "),
    K04_02("K04.02", "Land Route Report "),
    K04_03("K04.03", "Obstacle Report "),
    K04_04("K04.04", "Airborne Artillery Fire Control Radar (FCR) Report "),
    K04_08("K04.08", "Entity Emission Warning "),
    K04_09("K04.09", "Bridge Report "),
    K04_10("K04.10", "Initial Meaconing, Intrusion, Jamming, and Interference (MIJI) Report "),
    K04_13("K04.13", "Basic Weather Report "),
    K04_14("K04.14", "Forecast Meteorological Data "),
    K04_15("K04.15", "Observed Weather Information and Effects "),
    K04_17("K04.17", "Tactical Image Transfer "),
    K04_18("K04.18", "Sensor Data Link (SDL) Device Activation "),
    K04_19("K04.19", "Sensor Data Link (SDL) Relationship "),
    K04_20("K04.20", "Sensor Data Link (SDL) Configuration "),
    K04_21("K04.21", "Sensor Data Link (SDL) Status "),
    K04_22("K04.22", "Sensor Data Link (SDL) Information Request "),
    K04_23("K04.23", "Sensor Data Link (SDL) Entity and Contact Event Data "),
    K04_24("K04.24", "Sensor Data Link (SDL) NBC Data "),
    K04_25("K04.25", "Sensor Data Link (SDL) Data Acquisition Settings "),
    K04_26("K04.26", "Sensor Data Link (SDL) Mission Defined Area "),
    K04_27("K04.27", "Sensor Data Link (SDL) Action Request "),
    K04_28("K04.28", "Sensor Data Link (SDL) Heartbeat Response "),
    K04_29("K04.29", "Sensor Data Link (SDL) Ping or Special Response "),
    K04_30("K04.30", "Sensor Data Link (SDL) Device Position/System Clock "),
    K04_31("K04.31", "Sensor Data Link (SDL) Device Identifier "),
    K04_32("K04.32", "Beach Survey Report "),
    K04_33("K04.33", "Surf Observation Report "),
    K04_34("K04.34", "Confirmatory Beach Report "),
    K04_35("K04.35", "Improvised Explosive Device (IED) Report "),
    K04_36("K04.36", "Aircraft Landing Zone Report "),
    K04_37("K04.37", "Drop Zone Report "),
    // LAND COMBAT OPERATIONS MESSAGES"
    K05_01("K05.01", "Position Report "),
    K05_02("K05.02", "Nuclear, Biological, Chemical Report One (NBC 1) "),
    K05_03("K05.03", "Nuclear, Biological, Chemical Report Two (NBC 2) "),
    K05_04("K05.04", "Nuclear, Biological, Chemical Report Three (NBC 3) "),
    K05_05("K05.05", "Nuclear, Biological, Chemical Report Four (NBC 4) "),
    K05_06("K05.06", "Nuclear, Biological, Chemical Report Five (NBC 5) "),
    K05_07("K05.07", "Nuclear, Biological, Chemical Report Six (NBC 6) "),
    K05_08("K05.08", "Basic Wind Report "),
    K05_09("K05.09", "Chemical Downwind Report "),
    K05_10("K05.10", "Effective Downwind Report "),
    K05_11("K05.11", "Strike Warning "),
    K05_12("K05.12", "REDCON "),
    K05_13("K05.13", "Threat Warning "),
    K05_14("K05.14", "Situation Report "),
    K05_15("K05.15", "Field Orders "),
    K05_17("K05.17", "Overlay "),
    K05_18("K05.18", "MOPP "),
    K05_19("K05.19", "Entity Data "),
    K05_20("K05.20", "Execution Matrix "),
    K05_21("K05.21", "Movement Command/Response "),
    // MARITIME OPERATIONS MESSAGES"
    // COMBAT SERVICE SUPPORT MESSAGES"
    K07_01("K07.01", "Medical Evacuation (MEDEVAC) Request and Response "),
    K07_02("K07.02", "Casualty Report "),
    K07_03("K07.03", "Logistics Report "),
    K07_04("K07.04", "Personnel Status "),
    K07_05("K07.05", "EPW/Detainee Evacuation Request/Response "),
    K07_06("K07.06", "CTIL/BRIL Action "),
    K07_07("K07.07", "Medical Unit Situation Report "),
    K07_08("K07.08", "Mortuary Affairs Situation Report "),
    K07_09("K07.09", "Supply Point/Area Status Report "),
    K07_10("K07.10", "Emergency Resupply Request "),
    K07_11("K07.11", "Emergency Resupply Request Response "),
    K07_12("K07.12", "Task Management "),
    K07_13("K07.13", "Platform Status Report "),
    K07_14("K07.14", "Platform Service Alert "),
    K07_15("K07.15", "Platform Mobile Load Report "),
    K07_16("K07.16", "Rapid Request "),
    // SPECIAL OPERATIONS MESSAGES"
    K08_01("K08.01", "Prepositioned Supply Report "),
    // AIR DEFENSE/AIR SPACE CONTROL MESSAGES"
    K10_02("K10.02", "Low-Altitude Air Defense (LAAD) Damage Assessment Report "),
    // other non-VMF type 
    FILE("File", "Binary file"),
    SDSA("SDSA", "Self-Descriptive SA"), // special case of FILE
    OTHER("Other", "Other message type");
    final String vmfId, name;
    private static final Map<String, MsgType> stringToTypeMap = new HashMap<>();

    static {
        for (MsgType type : MsgType.values()) {
            stringToTypeMap.put(type.vmfId, type);
        }
    }

    private MsgType(String vmfId, String name) {
        this.vmfId = vmfId;
        this.name = name;
    }

    public static MsgType fromString(String id) {
        MsgType type = stringToTypeMap.get(id);
        if (type == null) {
            return MsgType.OTHER;
        }
        return type;
    }

    public static MsgType create(int umf, int fad, int number) {
        switch (umf) {
            case 1:
                return FILE;
            case 2:
//                try {
                String id = String.format("K%02d.%02d", fad, number);
                return fromString(id);
//                return valueOf(id);
//            } catch (Exception ex) {
//                return OTHER;
//            }
            default:
                return OTHER;
        }
    }

    public String getVmfId() {
        return vmfId;
    }

    public String getName() {
        return name;
    }

}
