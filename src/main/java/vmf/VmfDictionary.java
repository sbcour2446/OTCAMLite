/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

/**
 *
 * @author hansen
 */
public class VmfDictionary {

    static final String CR = System.lineSeparator();

    public static String getIdentity(int identity) {
        switch (identity) {
            case 0:
                return "PENDING";
            case 1:
                return "UNKNOWN";
            case 2:
                return "ASSUMED FRIEND";
            case 3:
                return "FRIEND";
            case 4:
                return "NEUTRAL";
            case 5:
                return "SUSPECT";
            case 6:
                return "HOSTILE";
            case 7:
                return "EXERCISE PENDING";
            case 8:
                return "EXERCISE UNKNOWN";
            case 9:
                return "EXERCISE ASSUMED FRIEND";
            case 10:
                return "EXERCISE FRIEND";
            case 11:
                return "EXERCISE NEUTRAL";
            case 12:
                return "JOKER";
            case 13:
                return "FAKER";
            default:
                return "UNDEFINED";
        }
    }

    public static String getDimention(int dim) {
        switch (dim) {
            case 0:
                return "SPACE";
            case 1:
                return "AIR";
            case 2:
                return "GROUND UNITS";
            case 3:
                return "GROUND WEAPONS";
            case 4:
                return "GROUND VEHICLES";
            case 5:
                return "GROUND SENSORS";
            case 6:
                return "GROUND SPECIAL EQUIPMENT";
            case 7:
                return "GROUND INSTALLATIONS";
            case 8:
                return "SEA SURFACE";
            case 9:
                return "SEA SUBSURFACE";
            case 10:
                return "SOF ";
            case 11:
                return "TACTICAL GRAPHICS, TASKS";
            case 12:
                return "TACTICAL GRAPHICS, C2 AND GENERAL MANEUVER";
            case 13:
                return "TACTICAL GRAPHICS, MOBILITY/SURVIVABILITY";
            case 14:
                return "TACTICAL GRAPHICS, FIRE SUPPORT";
            case 15:
                return "TACTICAL GRAPHICS, COMBAT SERVICE SUPPORT(CSS)";
            case 16:
                return "TACTICAL GRAPHICS, OTHER";
            case 17:
                return "INTELLIGENCE, SPACE";
            case 18:
                return "INTELLIGENCE, AIR";
            case 19:
                return "INTELLIGENCE, GROUND";
            case 20:
                return "INTELLIGENCE, SEA SURFACE";
            case 21:
                return "INTELLIGENCE, SEA SUBSURFACE";
            case 22:
                return "STABILITY OPERATIONS, INDIVIDUALS";
            case 23:
                return "STABILITY OPERATIONS, VIOLENT ACTIVITIES";
            case 24:
                return "STABILITY OPERATIONS, LOCATIONS";
            case 25:
                return "STABILITY OPERATIONS, OPERATIONS";
            case 26:
                return "STABILITY OPERATIONS, ITEMS";
            case 27:
                return "STABILITY OPERATIONS, NON-MILITARY GROUP OR ORGANIZATION";
            case 28:
                return "STABILITY OPERATIONS, RAPE";
            case 29:
                return "EMERGENCY MANAGEMENT, INCIDENT";
            case 30:
                return "EMERGENCY MANAGEMENT, NATURAL EVENTS";
            case 31:
                return "EMERGENCY MANAGEMENT, OPERATIONS";
            case 32:
                return "EMERGENCY MANAGEMENT, INFRASTRUCTURE";
            default:
                return "UNDEFINED";
        }
    }

    public static String getType(int dim, int type, int subtype) {
        String s = getDimention(dim) + " - ";
        switch (dim) {
            case 1:
                s += getDim1Type(type, subtype);
                break;
            case 2:
                s += getDim2Type(type, subtype);
                break;
            case 4:
                s += getDim4Type(type, subtype);
                break;
            case 7:
                s += getDim7Type(type, subtype);
                break;
        }
        return s;
    }

    public static String getDim1Type(int type, int subtype) {
        switch (type) {
            case 0:
                return "MILITARY FIXED WING";
            case 1:
                return "MILITARY ROTARY WING";
            case 2:
                return "MILITARY LIGHTER THAN AIR";
            case 3:
                return "WEAPON";
            case 4:
                return "CIVIL AIRCRAFT";
            case 5:
                return "VIP AIRCRAFT";
            case 6:
                return "VIP AIRCRAFT ESCORT";
            case 7:
                return "DECOY";
            case 8:
                return "UNKNOWN";
            default:
                return "UNDEFINED";
        }
    }

    public static String getDim2Type(int type, int subtype) {
        switch (type) {
            case 0:
                return "AIR DEFENSE";
            case 1:
                return "ARMOR";
            case 2:
                return "ANTI ARMOR";
            case 3:
                return "AVIATION";
            case 4:
                return "INFANTRY";
            case 5:
                return "ENGINEER";
            case 6:
                return "FIELD ARTILLERY";
            case 7:
                return "RECONNAISSANCE";
            case 8:
                return "MISSILE (SURF-SURF)";
            case 9:
                return "INTERNAL SECURITY FORCES";
            case 10:
                return "NBC";
            case 11:
                return "MILITARY INTELLIGENCE";
            case 12:
                return "LAW ENFORCEMENT UNIT";
            case 13:
                return "SIGNAL UNIT";
            case 14:
                return "INFO WARFARE UNIT";
            case 15:
                return "LANDING SUPPORT";
            case 16:
                return "EXPLOSIVE ORDNANCE DISPOSAL";
            case 17:
                return "ADMINISTRATIVE";
            case 18:
                return "MEDICAL";
            case 19:
                return "SUPPLY";
            case 20:
                return "TRANSPORTATION";
            case 21:
                return "MAINTENANCE";
            case 22:
                return "SPECIAL C2 HQ COMPONENT";
            case 23:
                return "UNKNOWN";
        }
        return "UNDEFINED";
    }

    public static String getDim3Type(int type, int subtype) {
        switch (type) {
            case 0:
                return "MISSILE LAUNCHER";
            case 1:
                return "SINGLE ROCKET LAUNCHER";
            case 2:
                return "MULTIPLE ROCKET LAUNCHER";
            case 3:
                return "ANTI TANK ROCKET LAUNCHER";
            case 4:
                return "RIFLE/AUTOMATIC WEAPON";
            case 5:
                return "GRENADE LAUNCHER";
            case 6:
                return "MORTAR";
            case 7:
                return "HOWITZER";
            case 8:
                return "ANTI TANK GUN";
            case 9:
                return "DIRECT FIRE GUN";
            case 10:
                return "AIR DEFENSE GUN ";
            case 11:
                return "UNKNOWN";
            default:
                return "UNDEFINED";
        }
    }

    public static String getDim4Type(int type, int subtype) {
        switch (type) {
            case 0:
                return "ARMORED";
            case 1:
                return "UTILITY VEHICLE";
            case 2:
                return "ENGINEER VEHICLE";
            case 3:
                return "TRAIN LOCOMOTIVE";
            case 4:
                return "CIVILIAN VEHICLE";
            case 5:
                return "UNKNOWN";
            default:
                return "UNDEFINED";
        }
    }

    public static String getDim7Type(int type, int subtype) {
        switch (type) {
            case 0:
                return "RAW MATERIAL PRODUCTION STORAGE";
            case 1:
                return "PROCESSING FACILITY";
            case 2:
                return "DECON FACILITY";
            case 3:
                return "EQUIPMENT MANUFACTURE";
            case 4:
                return "SERVICE, RESEARCH, UTILITY";
            case 5:
                return "MILITARY MATERIAL";
            case 6:
                return "GOVERNMENT LEADERSHIP";
            case 7:
                return "MILITARY BASE/FACILITY";
            case 8:
                return "AIRPORT/AIRBASE";
            case 9:
                return "SEAPORT/NAVAL BASE";
            case 10:
                return "TRANSPORT FACILITY";
            case 11:
                return "MEDICAL FACILITY";
            case 12:
                return "HOSPITAL";
            case 13:
                return "TENTED CAMP";
            case 14:
                return "DISPLACED PERSONS, REFUGEES, EVACUEES CAMP";
            case 15:
                return "TRAINING CAMP";
            case 16:
                return "WAREHOUSE/STORAGE FACILITY";
            case 17:
                return "UNKNOWN";
            default:
                return "UNDEFINED";
        }
    }

    // DFI/DUI 4005/001
    public static String getMunitionsType(int t) {
        switch (t + 1) {
            case 1:
                return "ANTIAIRCRAFT, COMMON";
            case 2:
                return "COMMON";
            case 3:
                return "HIGH EXPLOSIVE";
            case 4:
                return "HIGH CAPACITY";
            case 5:
                return "ICM";
            case 6:
                return "ARMOR PIERCING";
            case 7:
                return "ANTIPERSONNEL";
            case 8:
                return "SMOKE GREEN";
            case 9:
                return "SMOKE RED";
            case 10:
                return "SMOKE YELLOW";
            case 11:
                return "SMOKE VIOLET";
            case 12:
                return "SMOKE HEXACHLORETHANE";
            case 13:
                return "ILLUMINATION";
            case 14:
                return "RIOT CONTROL AGENT CS";
            case 15:
                return "GAS GB";
            case 16:
                return "GAS H OR HD";
            case 17:
                return "WHITE PHOSPHORUS";
            case 18:
                return "MIX HE AND WHITE PHOSPHORUS";
            case 19:
                return "HE SPOTTING";
            case 20:
                return "HE ANTITANK";
            case 21:
                return "HE ROCKET-ASSISTED PROJECTILE, ROCKET ON";
            case 22:
                return "ICM DUAL PURPOSE";
            case 23:
                return "ANTIPERSONNEL MINE, LONG DELAY";
            case 24:
                return "ANTIPERSONNEL MINE, SHORT DELAY";
            case 25:
                return "ANTIMATERIAL MINE, LONG DELAY";
            case 26:
                return "ANTIMATERIAL MINE, SHORT DELAY";
            case 27:
                return "COPPERHEAD";
            case 28:
                return "MLRS ANTITANK MINE";
            case 29:
                return "MLRS TERMINAL HOMING MUNITIONS";
            case 30:
                return "WP, BASE EJECTED FELT WEDGES";
            case 31:
                return "GAS GB, BINARY";
            case 32:
                return "HE ROCKET ASSIST, ROCKET-OFF";
            case 33:
                return "M718 - AML - 155MM";
            case 34:
                return "M741 - AMS - 155MM";
            case 35:
                return "M692 - APL - 155MM";
            case 36:
                return "M731 - APS - 155MM";
            case 37:
                return "M712 - CPH - 155MM";
            case 38:
                return "M718A1 - AML - 155MM";
            case 39:
                return "M741A1 - AMS - 155MM";
            case 40:
                return "M107 - HEA - 155MM";
            case 41:
                return "L15A2B1 - HEK - 155MM";
            case 42:
                return "HEA - 60MM (HE M720 W/MO M734), 81MM (HE M821 W/MO M734), 107MM, 120MM (HE M934A1 W/MO M734A1), 5IN54, 5IN62";
            case 43:
                return "HEB - 60MM (HE M768 W/QDL M783), 81MM (HE M889 W/Q M935), 120MM (HE M933 W/Q M745)";
            case 44:
                return "M444 - HEC - 105MM";
            case 45:
                return "M449A1 - HEE - 155MM";
            case 46:
                return "HEF - 16IN50";
            case 47:
                return "M760 - HEG - 105MM";
            case 48:
                return "M314A3 - ILA - 105MM";
            case 49:
                return "L15A1 - HEK - 155MM";
            case 50:
                return "M795 - HEL - 155MM";
            case 51:
                return "M864 - HEM - 155MM";
            case 52:
                return "M548 - HEO, HER - 105MM";
            case 53:
                return "M314A2E1 - ILA - 105MM";
            case 54:
                return "M913 - HRO, HRR - 105MM";
            case 55:
                return "M60/WP - SMA - 105MM";
            case 56:
                return "ILA - 60MM (M83A3 W/M65A1), 81MM (M301A3 W/M84A1), 107MM, 120MM (M91 W/M776), 5IN54, 5IN62";
            case 57:
                return "M898 - SAD - 155MM";
            case 58:
                return "SMA - 60MM (M302A1 W/M527), 81MM (M375 W/M524), 107MM, 120MM (M929, W/MO M734A1), 5IN54, 5IN62";
            case 59:
                return "M84A1 - SMB - 105MM";
            case 60:
                return "M825 - SMC - 155MM";
            case 61:
                return "M825A1 - SMD - 155MM";
            case 62:
                return "JED";
            case 63:
                return "JEE";
            case 64:
                return "JTA";
            case 65:
                return "JTB";
            case 66:
                return "JTE";
            case 67:
                return "JMT";
            case 68:
                return "JTC";
            case 69:
                return "JTW";
            case 70:
                return "JEG";
            case 71:
                return "JEH";
            case 72:
                return "JEJ";
            case 73:
                return "JEK";
            case 74:
                return "JEL";
            case 75:
                return "JEM";
            case 76:
                return "JEN";
            case 77:
                return "JTH";
            case 78:
                return "JTJ";
            case 79:
                return "JTK";
            case 80:
                return "JTL";
            case 81:
                return "JTD";
            case 82:
                return "JTF";
            case 83:
                return "JTG";
            case 84:
                return "JTM";
            case 85:
                return "JEP";
            case 86:
                return "JEQ";
            case 87:
                return "JER";
            case 88:
                return "JML";
            case 89:
                return "JMU";
            case 90:
                return "JNB";
            case 91:
                return "JSA";
            case 92:
                return "NAV";
            case 93:
                return "CAS";
            case 94:
                return "CSA 107MM";
            case 95:
                return "HED - NGF";
            case 96:
                return "CSA 81MM";
            case 97:
                return "SALGP";
            case 98:
                return "SMOKE ORANGE";
            case 99:
                return "HEAT MAIN GUN ROUND";
            case 100:
                return "SABOT MAIN GUN ROUND";
            case 101:
                return "MPAT MAIN GUN ROUND";
            case 102:
                return "DISUSED";
            case 103:
                return "MACHINE GUN ROUND (0.50 CAL)";
            case 104:
                return "MACHINE GUN ROUND (7.62MM)";
            case 105:
                return "RIFLE GUN ROUND (5.56MM)";
            case 106:
                return "ANTI-TANK MINE";
            case 107:
                return "HAND GRENADE";
            case 108:
                return "FASCAM";
            case 109:
                return "TOW MISSILES";
            case 110:
                return "HELLFIRE SEMI-ACTIVE LASER (SAL)";
            case 111:
                return "MLRS HE";
            case 112:
                return "RIFLE GUN RD (7.62MM)";
            case 113:
                return "2.75 ROCKETS";
            case 114:
                return "25MM";
            case 115:
                return "STINGER MISSILE";
            case 116:
                return "HES 60MM (M888 W/Q M935), 81MM (M374A3 W/Q M567), 120MM (M933 W/MO M734)";
            case 117:
                return "HET 60MM (M49A5 W/Q M935), 81MM (M374A2 W/Q M567), 120MM (M934 W/MO M734)";
            case 118:
                return "SME 60MM (M722 W/M745), 81MM (M375A2 W/M524), 120MM (XM929 W/M745)";
            case 119:
                return "ILW - 60MM (M721 W/M776), 81MM (M853A1 W/M772), 120MM (M930W/M776)";
            case 120:
                return "LOSAT";
            case 121:
                return "M915 - HXF - 105MM";
            case 122:
                return "ILR - 60MM (M767 W/M776), 81MM (M816 W/M772), 120MM (M983 W/M776)";
            case 123:
                return "M483A1 - HEF - 155MM";
            case 124:
                return "M549 - HER - 155MM";
            case 125:
                return "M927 - HTO, HTR - 105MM";
            case 126:
                return "M60A2/WP - SMA - 105MM";
            case 127:
                return "NR109 - ILB - 155MM";
            case 128:
                return "XM395 - 120MM";
            case 129:
                return "DPM - 60MM, 81MM, 120MM-(XM984 W/UNK)";
            case 130:
                return "TAS - 60MM, 81MM, 120MM (M1027 W/TI M776)";
            case 131:
                return "SRM - 60MM (M766 W/Q M779), 81MM (M880 W/Q M775), 120MM";
            case 132:
                return "FRM - 60MM (M769 W/Q M775), 81MM (M879 W/M751), 120MM (M931 W/Q M781)";
            case 133:
                return "SMR - 60MM, 81MM (M819 W/TI M772), 120MM";
            case 134:
                return "HE - PD";
            case 135:
                return "HE - DELAY";
            case 136:
                return "HE - TIME";
            case 137:
                return "HE - VT";
            case 138:
                return "HE - CVT";
            case 139:
                return "HE - MFF";
            case 140:
                return "HF - TIME";
            case 141:
                return "HF - CVT";
            case 142:
                return "HF - MFF";
            case 143:
                return "ILLUM1 - TIME";
            case 144:
                return "ILLUM2 - TIME";
            case 145:
                return "ILLUM3 - TIME";
            case 146:
                return "WP - PD";
            case 147:
                return "WP - TIME";
            case 148:
                return "ICM - TIME";
            case 149:
                return "ERGM - TIME";
            case 150:
                return "MACHINE GUN RD (30MM)";
            case 151:
                return "5 INCH ROCKET";
            case 152:
                return "M549A1 - HER - 155MM";
            case 153:
                return "M485A1 - ILA - 155MM";
            case 154:
                return "MACHINE GUN RD (20MM)";
            case 155:
                return "HELLFIRE RADIO FREQUENCY";
            case 156:
                return "M485E2 - ILC - 155MM";
            case 157:
                return "M110/WP - SMA - 155MM";
            case 158:
                return "M110A1/WP - SMA - 155MM";
            case 159:
                return "M110A2/WP - SMA - 155MM";
            case 160:
                return "XM982 - XCL - 155MM";
            case 161:
                return "M107BG - HEA - 155MM";
            case 162:
                return "NM28 - HEA - 155MM";
            case 163:
                return "OE56 - HEA - 155MM";
            case 164:
                return "OE69 - HEA - 155MM";
            case 165:
                return "DM21 - HEA - 155MM";
            case 166:
                return "M107B2 - HEA - 155MM";
            case 167:
                return "M107C1 - HEA - 155MM";
            case 168:
                return "M485A2 - ILA - 155MM";
            case 169:
                return "M110E2/WP - SMA - 155MM";
            case 170:
                return "M110C1/WP - SMA - 155MM";
            case 171:
                return "DM45A1 - SMB - 155MM";
            case 172:
                return "M116C1 - SMB - 155MM";
            case 173:
                return "M116C2 - SMB - 155MM";
            case 174:
                return "M116A1 - SMB - 155MM";
            case 175:
                return "M1 - HEA - 105MM";
            case 176:
                return "M916 - HEF - 105MM";
            case 177:
                return "L15A2 - HEK - 155MM";
            case 178:
                return "M107BG - HEB - 155MM";
            case 179:
                return "NM28 - HEB - 155MM";
            case 180:
                return "OE56 - HEB - 155MM";
            case 181:
                return "OE69 - HEB - 155MM";
            case 182:
                return "DM21 - HEB - 155MM";
            case 183:
                return "M107B2 - HEB - 155MM";
            case 184:
                return "M107C1 - HEB - 155MM";
            case 185:
                return "M107 - HEB - 155MM";
            case 186:
                return "M1 - HEB - 105MM";
            case 187:
                return "M1076 - 155MM";
            case 188:
                return "M1065 - 155MM";
            case 189:
                return "DISUSED";
            case 190:
                return "M1077 - 155MM";
            case 191:
                return "M1065A1 - 155MM";
            case 192:
                return "DISUSED";
            case 193:
                return "M116A1/HC - SMB - 155MM";
            case 194:
                return "M60A1/WP - SMA - 105MM";
            case 195:
                return "TOW 2A";
            case 196:
                return "TOW 2B";
            case 197:
                return "TOW 2B AERO";
            case 198:
                return "TOW 2B CAPS";
            case 199:
                return "TOW BUNKER BUSTER";
            case 200:
                return "TOW PRACTICE/TRAINING";
            case 201:
                return "DRAGON";
            case 202:
                return "JAVELIN";
            case 203:
                return "JAVELIN P3I";
            case 204:
                return "JOINT COMMON MISSILE";
            case 205:
                return "DM702 - SAD - 155MM";
            case 206:
                return "DM702A1 - SAD - 155MM";
            case 207:
                return "155BONUS - SAD - 155MM";
            case 208:
                return "O155ACF1BONUS - SAD - 155MM";
            case 209:
                return "M864A1 - HEM - 155MM";
            case 210:
                return "M1028 CANISTER ROUND - 120MM";
            case 211:
                return "M908 - OR ROUND - 120MM";
            case 212:
                return "HE - 25MM";
            case 213:
                return "AP - 25MM";
            case 214:
                return "DU - 25MM";
            case 215:
                return "HEP M393A2/M393A3 - 105MM";
            case 216:
                return "HEAT M456A2 - 105MM";
            case 217:
                return "SABOT M900 - 105MM";
            case 218:
                return "CANISTER XM1040 - 105MM";
            case 219:
                return "GRENADE M430/M430A1 - 40MM";
            case 220:
                return "GRENADE M383 - 40MM";
            case 221:
                return "GRENADE M922A1 - 40MM";
            case 222:
                return "GRENADE M76IR";
            case 223:
                return "GRENADE M90";
            case 224:
                return "GRENADE UK-L8A1";
            case 225:
                return "GRENADE UK-L8A3 RP";
            case 226:
                return "60MM (M49A2E2 W/Q M935), 81MM (M374 W/Q M524), 120MM (M57 W/Q M935)";
            case 227:
                return "60MM (M1046 W/TI M783), 81MM, 120MM";
            case 228:
                return "60MM (M1061 W/MO 734A1), 81MM, 120MM";
            case 229:
                return "60MM (M50A2E1 W/Q M935), 81MM (M879E1 W/TI M772), 120MM";
            case 230:
                return "60MM (M49A2E2 W/Q M525), 81MM (M374 W/Q M567), 120MM (M934A1E1 W/MO M734A1)";
            case 231:
                return "60MM (M49A4 W/Q M935), 81MM (M374 W/VT M532), 120MM";
            case 232:
                return "60MM (M49A4 W/Q M525), 81MM (M374A2 W/Q M524), 120MM";
            case 233:
                return "60MM (M49A4E1 W/Q M935), 81MM (M374A2 W/VT M532), 120MM";
            case 234:
                return "60MM (M720A1 W/MO M734A1), 81MM, 120MM";
            case 235:
                return "60MM (M720E1 W/MO M734A1), 81MM, 120MM";
            case 236:
                return "60MM, 81MM (M819E1 W/TI M772), 120MM";
            case 237:
                return "60MM, 81MM (M374A3 W/Q M524), 120MM";
            case 238:
                return "60MM, 81MM (M374A3 W/VT M532), 120MM";
            case 239:
                return "60MM, 81MM (M821A1 W/MO M734), 120MM";
            case 240:
                return "60MM, 81MM (M821A2 W/MO M734A1), 120MM";
            case 241:
                return "60MM, 81MM (M889A1 W/Q M935), 120MM";
            case 242:
                return "60MM (M302A2 W/M936), 81MM, 120MM (M1039 W/M776)";
            case 243:
                return "60MM (M302E1 W/M527), 81MM, 120MM";
            case 244:
                return "60MM (M722A1 W/QDL M783), 81MM (M375A2 W/M567), 120MM";
            case 245:
                return "60MM, 81MM (M375A3 W/M524), 120MM";
            case 246:
                return "60MM, 81MM (M375A3 W/VT M532), 120MM";
            case 247:
                return "60MM, 81MM (M375A3 W/M567), 120MM";
            case 248:
                return "60MM (M50A2E1 W/Q M525), 81MM, 120MM";
            case 249:
                return "60MM (M50A3 W/Q M935), 81MM, 120MM";
            case 250:
                return "60MM (M50A3 W/Q M525), 81MM, 120MM";
            case 251:
                return "M982 - XCL - 155MM";
            case 252:
                return "M982A1 - XCL - 155MM";
            case 253:
                return "LAM";
            case 254:
                return "PAM";
            case 255:
                return "M1064 - 105MM";
            case 256:
                return "120MM (M934A2 W/MO M734A1)";
            case 257:
                return "M1101 W/MO M767A1";
            case 258:
                return "M1101 W/MO M782";
            case 259:
                return "M1107 W/MO M767A1";
            case 260:
                return "M1107 W/MO M782";
            case 261:
                return "M1105 W/M762A1";
            case 262:
                return "M1103 W/MO M767A1";
            case 263:
                return "M1103 W/MO M782";
            case 264:
                return "XM982A1 - XCL - 155MM";
            case 265:
                return "M720A2 W/M734A1 - 60MM";
            case 266:
                return "M721A1 W/M784 - 60MM";
            case 267:
                return "M767A1 W/M784 - 60MM";
            case 268:
                return "M768A1 W/M783 - 60MM";
            case 269:
                return "M888A1 W/M783 - 60MM";
            case 270:
                return "M816A1 W/M785 - 81MM";
            case 271:
                return "M819A1 W/M785 - 81MM";
            case 272:
                return "M821A3 W/M734A1 - 81MM";
            case 273:
                return "M853A2 W/M785 - 81MM";
            case 274:
                return "M879A1 W/M787 - 81MM";
            case 275:
                return "M889A2 W/M783 - 81MM";
            case 276:
                return "M889A3 W/M783 - 81MM";
            case 277:
                return "M395 - 120MM";
            case 278:
                return "M395A1 - 120MM";
            case 279:
                return "M395A2 - 120MM";
            case 280:
                return "M929A1 W/M734A1 - 120MM";
            case 281:
                return "M930A1 W/M776 - 120MM";
            case 282:
                return "M930A2 W/M776 - 120MM";
            case 283:
                return "M932 W/M781 - 120MM";
            case 284:
                return "M933A1 W/M783 - 120MM";
            case 285:
                return "M933A2 W/M783 - 120MM";
            case 286:
                return "M983A1 W/M784 - 120MM";
            case 287:
                return "M1102 W/MO M782";
            case 288:
                return "M1104 W/MO M782";
            case 289:
                return "M1106 W/TI M762A1";
            case 290:
                return "M1108 W/MO M782";
            case 291:
                return "M1109 W/O FUZE";
            case 292:
                return "M1110 W/TI M767A1";
            case 293:
                return "M1111 W/MO M782";
            case 294:
                return "M1066 - 155MM";
            case 295:
                return "M1130 - 105MM";
            case 296:
                return "M1131 - 105MM";
            case 297:
                return "M1130A1 - 105MM";
            case 298:
                return "M1131A1 - 105MM";
            case 299:
                return "XM1120 - 155MM";
            case 300:
                return "XM1121 - 155MM";
            case 301:
                return "XM1122 - 155MM";
            case 302:
                return "XM1123 - 155MM";
            case 303:
                return "XM1124 - 155MM";
            case 304:
                return "XM1125 - 155MM";
            case 305:
                return "XM1126 - 155MM";
            case 306:
                return "XM1127 - 155MM";
            case 307:
                return "XM1128 - 155MM";
            case 308:
                return "XM1129 - 155MM";
            case 309:
                return "XM1132 - 105MM";
            case 310:
                return "XM1133 - 105MM";
            case 311:
                return "XM1134 - 105MM";
            case 312:
                return "XM1135 - 105MM";
            case 313:
                return "M1136 - 105MM";
            case 314:
                return "M913A1 - 105MM";
            case 315:
                return "XM1138 - 105MM";
            case 316:
                return "M1A1 - 105MM";
            case 317:
                return "APS BIT MUNITION";
            case 318:
                return "APS SRCM MUNITION";
            case 319:
                return "APS LRCM MUNITION";
            case 320:
                return "APS ALTERNATIVE COUNTERMEASURES MUNITION";
            case 321:
                return "M720A3 W/MO M734A1 - 60MM";
            case 322:
                return "M720A4 W/MO M734A1 - 60MM";
            case 323:
                return "M821A4 W/MO M734A1 - 81MM";
            case 324:
                return "M821A5 W/MO M734A1 - 81MM";
            case 325:
                return "M768A2 W/QDL M783 - 60MM";
            case 326:
                return "M768A3 W/QDL M783 - 60MM";
            case 327:
                return "M889A4 W/QDL M783 - 81MM";
            case 328:
                return "M889A5 W/QDL M783 - 81MM";
            case 329:
                return "M933A3 W/QDL M783 - 120MM";
            case 330:
                return "M933A4 W/QDL M783 - 120MM";
            case 331:
                return "M888A2 W/QDL M783 - 60MM";
            case 332:
                return "M888A3 W/QDL M783 - 60MM";
            case 333:
                return "M721A2 W/TI M784 - 60MM";
            case 334:
                return "M721A3 W/TI M784 - 60MM";
            case 335:
                return "M853A3 W/TI M785 - 81MM";
            case 336:
                return "M853A4 W/TI M785 - 81MM";
            case 337:
                return "M930A3 W/TI M784 - 120MM";
            case 338:
                return "M930A4 W/TI M784 - 120MM";
            case 339:
                return "M767A2 W/TI M784 - 60MM";
            case 340:
                return "M767A3 W/TI M784 - 60MM";
            case 341:
                return "M816A2 W/TI M785 - 81MM";
            case 342:
                return "M816A3 W/TI M785 - 81MM";
            case 343:
                return "M984A1 W/UNK - 120MM";
            case 344:
                return "M984A2 W/UNK - 120MM";
            case 345:
                return "M769A1 W/Q M775 - 60MM";
            case 346:
                return "M769A2 W/Q M775 - 60MM";
            case 347:
                return "M879A2 W/Q M787 - 81MM";
            case 348:
                return "M879A3 W/Q M787 - 81MM";
            case 349:
                return "M931A1 W/Q M781 - 120MM";
            case 350:
                return "M931A2 W/Q M781 - 120MM";
            case 351:
                return "M1046A1 W/QDL M783 - 60MM";
            case 352:
                return "M1046A2 W/QDL M783 - 60MM";
            case 353:
                return "M1061A1 W/MO 734A1 - 60MM";
            case 354:
                return "M1061A2 W/MO 734A1 - 60MM";
            case 355:
                return "M722A2 W/QDL M783 - 60MM";
            case 356:
                return "M722A3 W/QDL M783 - 60MM";
            case 357:
                return "XM1113 - 105MM";
            case 358:
                return "XM1139 - 105MM";
            case 359:
                return "M1064A1 - 105MM";
            case 360:
                return "M314A1 - 105MM";
            case 361:
                return "XM1143 - 105MM";
            case 362:
                return "XM1144 - 105MM";
            case 363:
                return "XM1145 - 105MM";
            case 364:
                return "XM1146 - 105MM";
            case 365:
                return "XM1147 - 105MM";
            case 366:
                return "XM1148 - 105MM";
            case 367:
                return "XM1149 - 105MM";
            case 368:
                return "XM1150 - 155MM";
            case 369:
                return "XM1151 - 155MM";
            case 370:
                return "XM1152 - 155MM";
            case 371:
                return "XM1153 - 155MM";
            case 372:
                return "XM1154 - 155MM";
            case 373:
                return "XM1155 - 155MM";
            case 374:
                return "XM1156 - 155MM";
            case 375:
                return "XM1157 - 155MM";
            case 376:
                return "XM1158 - 155MM";
            case 377:
                return "XM1159 - 155MM";
            case 378:
                return "XM1160 - MORTAR";
            case 379:
                return "XM1161 - MORTAR";
            case 380:
                return "XM1162 - MORTAR";
            case 381:
                return "XM1163 - MORTAR";
            case 382:
                return "XM1164 - MORTAR";
            default:
                return "UNDEFINED";
        }
    }

    // DFI/DUI 4069/001
    public static String getEffectAchieved(int i) {
        switch (i) {
            case 1:
                return "NEUTRALIZED";
            case 2:
                return "OBSCURED BY SMOKE";
            case 3:
                return "POSITION MARKED";
            case 4:
                return "DISRUPTED";
            case 5:
                return "INTERDICTED";
            case 6:
                return "DESTROYED";
            case 7:
                return "ILLUMINATED";
            case 8:
                return "UNOBSERVED";
            case 9:
                return "NO EFFECT";
            case 10:
                return "CANCELLED/ABORTED";
            case 11:
                return "SUPPRESSED";
            case 12:
                return "NEUTRALIZED BURNING";
            case 13:
                return "BURNING";
            case 14:
                return "UNKNOWN";
            default:
                return "UNDEFINED";
        }
    }

    // 4127/005
    static String getNationality(int nationality) {
        final String s = CR + "Nationality: ";
        switch (nationality) {
            case VmfDataBuffer.NO_INT:
                return "";
            case 0:
                return s + "NO STATEMENT";
            case 1:
                return s + "AFGHANISTAN (AF)";
            case 2:
                return s + "ALBANIA (AL)";
            case 3:
                return s + "ALGERIA (AG)";
            case 4:
                return s + "AMERICAN SOMOA (AQ)";
            case 5:
                return s + "ANDORRA (AN)";
            case 6:
                return s + "ANGOLA (AO)";
            case 7:
                return s + "ANGUILLA (AV)";
            case 8:
                return s + "ANTARCTICA (AY)";
            case 9:
                return s + "ANTIGUA AND BARBUDA (AC)";
            case 10:
                return s + "ARGENTINA (AR)";
            case 11:
                return s + "ARMENIA (AM)";
            case 12:
                return s + "ARUBA (AA)";
            case 13:
                return s + "ASHMORE AND CARTER ISLANDS (AT)";
            case 14:
                return s + "AUSTRALIA (AS)";
            case 15:
                return s + "AUSTRIA (AU)";
            case 16:
                return s + "AZERBAIJAN (AJ)";
            case 17:
                return s + "BAHAMAS, THE (BF)";
            case 18:
                return s + "BAHRAIN (BA)";
            case 19:
                return s + "BAKER ISLAND (FQ)";
            case 20:
                return s + "BANGLADESH (BG)";
            case 21:
                return s + "BARBADOS (BB)";
            case 22:
                return s + "BASSAS DA INDIA (BS)";
            case 23:
                return s + "BELARUS (BO)";
            case 24:
                return s + "BELGIUM (BE)";
            case 25:
                return s + "BELIZE (BH)";
            case 26:
                return s + "BENIN (BN)";
            case 27:
                return s + "BERMUDA (BD)";
            case 28:
                return s + "BHUTAN (BT)";
            case 29:
                return s + "BOLIVIA (BL)";
            case 30:
                return s + "BOZNIA AND HERZEGOVINA (BK)";
            case 31:
                return s + "BOTSWANA (BC)";
            case 32:
                return s + "BOUVET ISLAND (BV)";
            case 33:
                return s + "BRAZIL (BR)";
            case 34:
                return s + "BRITISH INDIAN OCEAN TERRITORY (IO)";
            case 35:
                return s + "BRITISH VIRGIN ISLANDS (VI)";
            case 36:
                return s + "BRUNEI (BX)";
            case 37:
                return s + "BULGARIA (BU)";
            case 38:
                return s + "BURKINA FASO (UV)";
            case 39:
                return s + "BURMA (BM)";
            case 40:
                return s + "BURUNDI (BY)";
            case 41:
                return s + "CAMBODIA (CB)";
            case 42:
                return s + "CAMEROON (CM)";
            case 43:
                return s + "CANADA (CA)";
            case 44:
                return s + "CAPE VERDE (CV)";
            case 45:
                return s + "CAYMAN ISLANDS (CJ)";
            case 46:
                return s + "CENTRAL AFRICAN REPUBLIC (CT)";
            case 47:
                return s + "CHAD (CD)";
            case 48:
                return s + "CHILE (CI)";
            case 49:
                return s + "CHINA (CH)";
            case 50:
                return s + "CHRISTMAS ISLAND (KT)";
            case 51:
                return s + "CLIPPERTON ISLAND (IP)";
            case 52:
                return s + "COCOS (KEELING) ISLANDS (CK)";
            case 53:
                return s + "COLOMBIA (CO)";
            case 54:
                return s + "COMOROS (CN)";
            case 55:
                return s + "CONGO (CF)";
            case 56:
                return s + "CONGO (DEMOCRATIC REPUBLIC OF THE) (CG)";
            case 57:
                return s + "COOK ISLANDS (CW)";
            case 58:
                return s + "CORAL SEA ISLANDS (CR)";
            case 59:
                return s + "COSTA RICA (CS)";
            case 60:
                return s + "COTE D'IVOIRE (IV)";
            case 61:
                return s + "CROATIA (HR)";
            case 62:
                return s + "CUBA (CU)";
            case 63:
                return s + "CYPRUS (CY)";
            case 64:
                return s + "CZECH REPUBLIC (EZ)";
            case 65:
                return s + "DENMARK (DA)";
            case 66:
                return s + "DJIBOUTI (DJ)";
            case 67:
                return s + "DOMINICA (DO)";
            case 68:
                return s + "DOMINICAN REPUBLIC (DR)";
            case 69:
                return s + "EAST TIMOR (TT)";
            case 70:
                return s + "ECUADOR (EC)";
            case 71:
                return s + "EGYPT (EG)";
            case 72:
                return s + "EL SALVADOR (ES)";
            case 73:
                return s + "EQUATORIAL GUINEA (EK)";
            case 74:
                return s + "ERITREA (ER)";
            case 75:
                return s + "ESTONIA (EN)";
            case 76:
                return s + "ETHIOPIA (ET)";
            case 77:
                return s + "EUROPA ISLAND (EU)";
            case 78:
                return s + "FALKLAND ISLANDS (FK)";
            case 79:
                return s + "FAROE ISLANDS (FO)";
            case 80:
                return s + "FIJI (FJ)";
            case 81:
                return s + "FINLAND (FI)";
            case 82:
                return s + "FRANCE (FR)";
            case 83:
                return s + "FRENCH GUIANA (FG)";
            case 84:
                return s + "FRENCH POLYNESIA (FP)";
            case 85:
                return s + "FRENCH SOUTHERN AND ANTARCTIC LANDS (FS)";
            case 86:
                return s + "GABON (GB)";
            case 87:
                return s + "GAMBIA, THE (GA)";
            case 88:
                return s + "GAZA STRIP (GZ)";
            case 89:
                return s + "GEORGIA (GG)";
            case 90:
                return s + "GERMANY (GM)";
            case 91:
                return s + "GHANA (GH)";
            case 92:
                return s + "GIBRALTAR (GI)";
            case 93:
                return s + "GLORIOSO ISLANDS (GO)";
            case 94:
                return s + "GREECE (GR)";
            case 95:
                return s + "GREENLAND (GL)";
            case 96:
                return s + "GRENADA (GJ)";
            case 97:
                return s + "GUADELOUPE (GP)";
            case 98:
                return s + "GUAM (GQ)";
            case 99:
                return s + "GUATEMALA (GT)";
            case 100:
                return s + "GUERNSEY (GK)";
            case 101:
                return s + "GUINEA (GV)";
            case 102:
                return s + "GUINEA-BISSAU (PU)";
            case 103:
                return s + "GUYANA (GY)";
            case 104:
                return s + "HAITI (HA)";
            case 105:
                return s + "HEARD ISLAND AND MCDONALD ISLANDS (HM)";
            case 106:
                return s + "HONDURAS (HO)";
            case 107:
                return s + "HONG KONG (HK)";
            case 108:
                return s + "HOWLAND ISLAND (HQ)";
            case 109:
                return s + "HUNGARY (HU)";
            case 110:
                return s + "ICELAND (IC)";
            case 111:
                return s + "INDIA (IN)";
            case 112:
                return s + "INDONESIA (ID)";
            case 113:
                return s + "IRAN (IR)";
            case 114:
                return s + "IRAQ (IZ)";
            case 115:
                return s + "IRELAND (EI)";
            case 116:
                return s + "ISRAEL (IS)";
            case 117:
                return s + "ITALY (IT)";
            case 118:
                return s + "JAMAICA (JM)";
            case 119:
                return s + "JAN MAYEN (JN)";
            case 120:
                return s + "JAPAN (JA)";
            case 121:
                return s + "JARVIS ISLAND (DQ)";
            case 122:
                return s + "JERSEY (JE)";
            case 123:
                return s + "JOHNSTON ATOLL (JQ)";
            case 124:
                return s + "JORDAN (JO)";
            case 125:
                return s + "JUAN DE NOVA ISLAND (JU)";
            case 126:
                return s + "KAZAKHSTAN (KZ)";
            case 127:
                return s + "KENYA (KE)";
            case 128:
                return s + "KINGMAN REEF (KQ)";
            case 129:
                return s + "KIRIBATI (KR)";
            case 130:
                return s + "KOREA, DEMOCRATIC PEOPLES REPUBLIC OF (KN)";
            case 131:
                return s + "KOREA, REPUBLIC OF (KS)";
            case 132:
                return s + "KUWAIT (KU)";
            case 133:
                return s + "KYRGYZSTAN (KG)";
            case 134:
                return s + "LAOS (LA)";
            case 135:
                return s + "LATVIA (LG)";
            case 136:
                return s + "LEBANON (LE)";
            case 137:
                return s + "LESOTHO (LT)";
            case 138:
                return s + "LIBERIA (LI)";
            case 139:
                return s + "LIBYA (LY)";
            case 140:
                return s + "LIECHTENSTEIN (LS)";
            case 141:
                return s + "LITHUANIA (LH)";
            case 142:
                return s + "LUXEMBOURG (LU)";
            case 143:
                return s + "MACAU (MC)";
            case 144:
                return s + "MACEDONIA (MK)";
            case 145:
                return s + "MADAGASCAR (MA)";
            case 146:
                return s + "MALAWI (MI)";
            case 147:
                return s + "MALAYSIA (MY)";
            case 148:
                return s + "MALDIVES (MV)";
            case 149:
                return s + "MALI (ML)";
            case 150:
                return s + "MALTA (MT)";
            case 151:
                return s + "MAN, ISLE OF (IM)";
            case 152:
                return s + "MARSHALL ISLANDS (RM)";
            case 153:
                return s + "MARTINIQUE (MB)";
            case 154:
                return s + "MAURITANIA (MR)";
            case 155:
                return s + "MAURITIUS (MP)";
            case 156:
                return s + "MAYOTTE (MF)";
            case 157:
                return s + "MEXICO (MX)";
            case 158:
                return s + "MICRONESIA, FEDERATED STATES OF (FM)";
            case 159:
                return s + "MIDWAY ISLANDS (MQ)";
            case 160:
                return s + "MOLDOVA (MD)";
            case 161:
                return s + "MONACO (MN)";
            case 162:
                return s + "MONGOLIA (MG)";
            case 163:
                return s + "MONTSERRAT (MH)";
            case 164:
                return s + "MOROCCO (MO)";
            case 165:
                return s + "MOZAMBIQUE (MZ)";
            case 166:
                return s + "NAMIBIA (WA)";
            case 167:
                return s + "NAURU (NR)";
            case 168:
                return s + "NAVASSA ISLAND (BQ)";
            case 169:
                return s + "NEPAL (NP)";
            case 170:
                return s + "NETHERLANDS (NL)";
            case 171:
                return s + "NETHERLANDS ANTILLES (NT)";
            case 172:
                return s + "NEW CALEDONIA (NC)";
            case 173:
                return s + "NEW ZEALAND (NZ)";
            case 174:
                return s + "NICARAGUA (NU)";
            case 175:
                return s + "NIGER (NG)";
            case 176:
                return s + "NIGERIA (NI)";
            case 177:
                return s + "NIUE (NE)";
            case 178:
                return s + "NORFOLK ISLAND (NF)";
            case 179:
                return s + "NORTHERN MARIANA ISLANDS (CQ)";
            case 180:
                return s + "NORWAY (NO)";
            case 181:
                return s + "OMAN (MU)";
            case 182:
                return s + "OTHER COUNTRY (OO)";
            case 183:
                return s + "PAKISTAN (PK)";
            case 184:
                return s + "PALAU (PS)";
            case 185:
                return s + "PALMYRA ATOLL (LQ)";
            case 186:
                return s + "PANAMA (PM)";
            case 187:
                return s + "PAPUA NEW GUINEA (PP)";
            case 188:
                return s + "PARACEL ISLANDS (PF)";
            case 189:
                return s + "PARAGUAY (PA)";
            case 190:
                return s + "PERU (PE)";
            case 191:
                return s + "PHILIPPINES (RP)";
            case 192:
                return s + "PITCAIRN ISLANDS (PC)";
            case 193:
                return s + "POLAND (PL)";
            case 194:
                return s + "PORTUGAL (PO)";
            case 195:
                return s + "PUERTO RICO (RQ)";
            case 196:
                return s + "QATAR (QA)";
            case 197:
                return s + "REUNION (RE)";
            case 198:
                return s + "ROMANIA (RO)";
            case 199:
                return s + "RUSSIA (RS)";
            case 200:
                return s + "RWANDA (RW)";
            case 201:
                return s + "ST. KITTS AND NEVIS (SC)";
            case 202:
                return s + "ST. HELENA (SH)";
            case 203:
                return s + "ST. LUCIA (ST)";
            case 204:
                return s + "ST. PIERRE AND MIQUELON (SB)";
            case 205:
                return s + "ST. VINCENT AND THE GRENADINES (VC)";
            case 206:
                return s + "SAMOA (WS)";
            case 207:
                return s + "SAN MARINO (SM)";
            case 208:
                return s + "SAO TOME AND PRINCIPE (TP)";
            case 209:
                return s + "SAUDI ARABIA (SA)";
            case 210:
                return s + "SENEGAL (SG)";
            case 211:
                return s + "SEYCHELLES (SE)";
            case 212:
                return s + "SIERRA LEONE (SL)";
            case 213:
                return s + "SINGAPORE (SN)";
            case 214:
                return s + "SLOVAKIA (LO)";
            case 215:
                return s + "SLOVENIA (SI)";
            case 216:
                return s + "SOLOMON ISLANDS (BP)";
            case 217:
                return s + "SOMALIA (SO)";
            case 218:
                return s + "SOUTH AFRICA (SF)";
            case 219:
                return s + "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS (SX)";
            case 220:
                return s + "SPAIN (SP)";
            case 221:
                return s + "SPRATLY ISLANDS (PG)";
            case 222:
                return s + "SRI LANKA (CE)";
            case 223:
                return s + "SUDAN (SU)";
            case 224:
                return s + "SURINAME (NS)";
            case 225:
                return s + "SVALBARD (SV)";
            case 226:
                return s + "SWAZILAND (WZ)";
            case 227:
                return s + "SWEDEN (SW)";
            case 228:
                return s + "SWITZERLAND (SZ)";
            case 229:
                return s + "SYRIA (SY)";
            case 230:
                return s + "TAIWAN (TW)";
            case 231:
                return s + "TAJIKISTAN (TI)";
            case 232:
                return s + "TANZANIA (TZ)";
            case 233:
                return s + "THAILAND (TH)";
            case 234:
                return s + "TOGO (TO)";
            case 235:
                return s + "TOKELAU (TL)";
            case 236:
                return s + "TONGA (TN)";
            case 237:
                return s + "TRINIDAD AND TOBAGO (TD)";
            case 238:
                return s + "TROMELIN ISLAND (TE)";
            case 239:
                return s + "TUNISIA (TS)";
            case 240:
                return s + "TURKEY (TU)";
            case 241:
                return s + "TURKMENISTAN (TX)";
            case 242:
                return s + "TURKS AND CAICOS ISLANDS (TK)";
            case 243:
                return s + "TUVALU (TV)";
            case 244:
                return s + "UGANDA (UG)";
            case 245:
                return s + "UKRAINE (UP)";
            case 246:
                return s + "UNITED ARAB EMIRATES (AE)";
            case 247:
                return s + "UNITED KINGDOM (UK)";
            case 248:
                return s + "UNITED STATES (US)";
            case 249:
                return s + "URUGUAY (UY)";
            case 250:
                return s + "UZBEKISTAN (UZ)";
            case 251:
                return s + "VANUATU (NH)";
            case 252:
                return s + "VATICAN CITY (VT)";
            case 253:
                return s + "VENEZUELA (VE)";
            case 254:
                return s + "VIETNAM (VM)";
            case 255:
                return s + "VIRGIN ISLANDS, US (VQ)";
            case 256:
                return s + "WAKE ISLAND (WQ)";
            case 257:
                return s + "WALLIS AND FUTUNA (WF)";
            case 258:
                return s + "WEST BANK (WE)";
            case 259:
                return s + "WESTERN SAHARA (WI)";
            case 260:
                return s + "YEMEN (YM)";
            case 261:
                return s + "YUGOSLAVIA (YI)";
            case 262:
                return s + "ZAMBIA (ZA)";
            case 263:
                return s + "ZIMBABWE (ZI)";
            case 264:
                return s + "EXERCISE BLACK COUNTRY (OA)";
            case 265:
                return s + "EXERCISE BLACK FORCES (OB)";
            case 266:
                return s + "EXERCISE BLUE COUNTRY (OC)";
            case 267:
                return s + "EXERCISE BLUE FORCE (OD)";
            case 268:
                return s + "EXERCISE FRIENDLY COUNTRY (YC)";
            case 269:
                return s + "EXERCISE FRIENDLY FORCE (YY)";
            case 270:
                return s + "EXERCISE HOSTILE COUNTRY (XC)";
            case 271:
                return s + "EXERCISE HOSTILE FORCE (XX)";
            case 272:
                return s + "EXERCISE NEUTRAL COUNTRY (ZC)";
            case 273:
                return s + "EXERCISE NEUTRAL FORCE (ZZ)";
            case 274:
                return s + "EXERCISE ORANGE COUNTRY (OJ)";
            case 275:
                return s + "EXERCISE ORANGE FORCE (OK)";
            case 276:
                return s + "EXERCISE RED COUNTRY (OR)";
            case 277:
                return s + "EXERCISE RED FORCE (OE)";
            case 278:
                return s + "EXERCISE WHITE COUNTRY (ON)";
            case 279:
                return s + "EXERCISE NATO FORCE (OT)";
            case 280:
                return s + "EXERCISE PURPLE FORCE (OL)";
            case 281:
                return s + "EXERCISE SPARE NUMBER ONE (XA)";
            case 282:
                return s + "EXERCISE SPARE NUMBER TWO (XB)";
            case 283:
                return s + "EXERCISE UNITED NATIONS FORCE (UU)";
            case 284:
                return s + "EXERCISE FORMER WARSAW PACT FORCE (OW)";
            case 285:
                return s + "NAT/ALL-1 THROUGH NAT/ALL-28";
            case 313:
                return s + "SPANISH NORTH AFRICA (SQ)";
            case 314:
                return s + "ABKHAZIA (AB)";
            case 315:
                return s + "AZORES (AZ)";
            case 316:
                return s + "BOPHUTHATSWANA (BW)";
            case 317:
                return s + "CARIBBEAN (EXCLUDING ANTIGUA AND BARBUDA) (CC)";
            case 318:
                return s + "CRIMEA (CX)";
            case 319:
                return s + "CYPRUS, TURKISH REPUBLIC OF NORTHERN (TRNC) (KA)";
            case 320:
                return s + "EASTERN CARIBBEAN COUNTRIES (EA)";
            case 321:
                return s + "EASTERN EUROPEAN COUNTRIES (PW)";
            case 322:
                return s + "FALKLAND ISLAND DEPENDENCIES (FL)";
            case 323:
                return s + "FAR EAST COUNTRIES (FE)";
            case 324:
                return s + "KOSOVO (KO)";
            case 325:
                return s + "LATIN AMERICA (LM)";
            case 326:
                return s + "MIDDLE EASTERN/NORTH AFRICAN COUNTRIES (ME)";
            case 327:
                return s + "NAGORNO-KARABAKH (NK)";
            case 328:
                return s + "NORDIC COUNTRIES (NN)";
            case 329:
                return s + "NORTH OSSETIA (OS)";
            case 330:
                return s + "SOUTH AMERICA (UT)";
            case 331:
                return s + "SOUTH ASIA (AI)";
            case 332:
                return s + "SOUTH MARIANA ISLAND (MS)";
            case 333:
                return s + "SOUTH OSSETIA (OF)";
            case 334:
                return s + "SOUTH PACIFIC ISLAND NATIONS OR TERRITORIES (PI)";
            case 335:
                return s + "SOUTH EAST ASIA (EO)";
            case 336:
                return s + "SUB-SAHARAN AFRICA (UB)";
            case 337:
                return s + "TARTAR HOMELAND (TJ)";
            case 338:
                return s + "TRANSKEI (TR)";
            case 339:
                return s + "UNIDENTIFIED (UI)";
            case 340:
                return s + "WEST EUROPEAN COUNTRIES (EW)";
            case 341:
                return s + "WEST HEMISPHERE (HW)";
            case 342:
                return s + "WORLDWIDE (GW)";
            case 343:
                return s + "SERBIA AND MONTENEGRO (YI)";
            default:
                return "UNDEFINED (" + nationality + ')';
        }
    }

}
