package gov.mil.otc._3dvis.vmf;

import gov.mil.otc._3dvis.datamodel.EntityType;

import java.util.Locale;

/**
 * VMF Data Dictionary Functions
 */
public class VmfDictionary {
    private static final String TYPE = "%s Type: %d";
    private static final String SUBTYPE = " (%s Subtype: %d)";
    private static final String NO_STATEMENT = "NO STATEMENT";

    // Private Constructor to hide public one
    //
    private VmfDictionary() {
    }

    /**
     * Return a printable Identity
     *
     * @param identity VMF Identity Field
     * @return a printable Identity
     */
    public static String getIdentity(int identity) {
        return switch (identity) {
            case 0 -> "PENDING";
            case 1 -> "UNKNOWN";
            case 2 -> "ASSUMED FRIEND";
            case 3 -> "FRIEND";
            case 4 -> "NEUTRAL";
            case 5 -> "SUSPECT";
            case 6 -> "HOSTILE";
            case 7 -> "EXERCISE PENDING";
            case 8 -> "EXERCISE UNKNOWN";
            case 9 -> "EXERCISE ASSUMED FRIEND";
            case 10 -> "EXERCISE FRIEND";
            case 11 -> "EXERCISE NEUTRAL";
            case 12 -> "JOKER";
            case 13 -> "FAKER";
            default -> "UNDEFINED";
        };
    }

    /**
     * Determine if entity is terrestrial using Dimension
     *
     * @param dim VMF Dimension
     * @return true if terrestrial otherwise, false
     */
    public static boolean isGroundDimension(int dim) {
        return switch (dim) {
            case 0, 1, 9 -> false;
            default -> true;
        };
    }

    /**
     * Get the DIS Entity Type.
     *
     * @param dimension   VMF Dimension
     * @param nationality VMF Nationality
     * @param type        VMF  Type
     * @param subtype     VMF  SubType
     * @return a EntityType object.
     */
    public static EntityType getEntityType(int dimension, int nationality, int type, int subtype) {
        return new EntityType(getDisKind(dimension), getDisDomain(dimension), getDisCountry(nationality),
                getDisCategory(dimension, type, subtype), 0, 0, 0);
    }

    /**
     * Get the DIS Kind for a given VMF Dimension
     *
     * @param dim VMF Dimension
     * @return the DIS Kind
     */
    public static int getDisKind(int dim) {
        return switch (dim) {
            case 2 -> 3;  // Ground units
            case 3 -> 2;  // Ground weapons
            case 4 -> 1;  // Ground vehicles
            case 5 -> 9;  // Ground sensors
            case 6 -> 6;  // Ground special equipment
            case 7 -> 5;  // Ground installations
            default -> 0; // Other
        };
    }

    /**
     * Get the DIS Domain for a given VMF Dimension
     *
     * @param dim VMF Dimension
     * @return the DIS Domain
     */
    public static int getDisDomain(int dim) {
        return switch (dim) {
            case 0 -> 5;  // Space
            case 1 -> 2;  // Air
            case 2 -> 1;  // Ground units
            case 3 -> 1;  // Ground weapons
            case 4 -> 1;  // Ground vehicles
            case 5 -> 1;  // Ground sensors
            case 6 -> 1;  // Ground special equipment
            case 7 -> 1;  // Ground installations
            case 8 -> 3;  // Sea surface
            case 9 -> 4;  // Sea subsurface
            default -> 0; // Other
        };
    }

    /**
     * Get the DIS Country for a given VMF Nationality
     *
     * @param nationality VMF Nationality
     * @return the DIS Country Code
     */
    public static int getDisCountry(int nationality) {
        return switch (nationality) {
            case 0 -> 255; // Assumed Friendly (USA)
            case 1 -> 1; // Afghanistan
            case 2 -> 2; // Albania
            case 3 -> 3; // Algeria
            case 4 -> 4; // American Samoa (United States)
            case 5 -> 5; // Andorra
            case 6 -> 6; // Angola
            case 7 -> 7; // Anguilla
            case 8 -> 8; // Antarctica (International)
            case 9 -> 9; // Antigua and Barbuda
            case 10 -> 10; // Argentina
            case 11 -> 244; // Armenia
            case 12 -> 11; // Aruba
            case 13 -> 12; // Ashmore and Cartier Islands (Australia)
            case 14 -> 13; // Australia
            case 15 -> 14; // Austria
            case 16 -> 245; // Azerbaijan
            case 17 -> 15; // Bahamas
            case 18 -> 16; // Bahrain
            case 19 -> 17; // Baker Island (United States)
            case 20 -> 18; // Bangladesh
            case 21 -> 19; // Barbados
            case 22 -> 20; // Bassas da India (France)
            case 23 -> 246; // Belarus
            case 24 -> 21; // Belgium
            case 25 -> 22; // Belize
            case 26 -> 23; // Benin (aka Dahomey)
            case 27 -> 24; // Bermuda (United Kingdom)
            case 28 -> 25; // Bhutan
            case 29 -> 26; // Bolivia
            case 30 -> 247; // Bosnia and Hercegovina
            case 31 -> 27; // Botswana
            case 32 -> 28; // Bouvet Island (Norway)
            case 33 -> 29; // Brazil
            case 34 -> 30; // British Indian Ocean Territory (United Kingdom)
            case 35 -> 31; // British Virgin Islands (United Kingdom)
            case 36 -> 32; // Brunei
            case 37 -> 33; // Bulgaria
            case 38 -> 34; // Burkina (aka Burkina Faso or Upper Volta
            case 39 -> 35; // Burma (Myanmar)
            case 40 -> 36; // Burundi
            case 41 -> 37; // Cambodia (aka Kampuchea)
            case 42 -> 38; // Cameroon
            case 43 -> 39; // Canada
            case 44 -> 40; // Republic of Cape Verde
            case 45 -> 41; // Cayman Islands (United Kingdom)
            case 46 -> 42; // Central African Republic
            case 47 -> 43; // Chad
            case 48 -> 44; // Chile
            case 49 -> 45; // People's Republic of China
            case 50 -> 46; // Christmas Island (Australia)
            case 51 -> 248; // Clipperton Island (France)
            case 52 -> 47; // Cocos (Keeling) Islands (Australia)
            case 53 -> 48; // Colombia
            case 54 -> 49; // Comoros
            case 55 -> 241; // Zaire
            case 56 -> 50; //  Republic of Congo
            case 57 -> 51; // Cook Islands (New Zealand)
            case 58 -> 52; // Coral Sea Islands (Australia)
            case 59 -> 53; // Costa Rica
            case 60 -> 107; // Cote D'Ivoire (aka Ivory Coast)
            case 61 -> 249; // Croatia
            case 62 -> 54; // Cuba
            case 63 -> 55; // Cyprus
            case 64 -> 56; // Czechoslovakia (separating into Czech Republic and Slovak Republic)
            case 65 -> 57; // Denmark
            case 66 -> 58; // Djibouti
            case 67 -> 59; // Dominica
            case 68 -> 60; // Dominican Republic
            case 70 -> 61; // Ecuador
            case 71 -> 62; // Egypt
            case 72 -> 63; // El Salvador
            case 73 -> 64; // Equatorial Guinea
            case 75 -> 250; // Estonia
            case 76 -> 65; // Ethiopia
            case 77 -> 66; // Europa Island (France)
            case 78 -> 67; // Falkland Islands (aka Islas Malvinas) (United Kingdom)
            case 79 -> 68; // Faroe Islands (Denmark)
            case 80 -> 69; // Fiji
            case 81 -> 70; // Finland
            case 82 -> 71; // France
            case 83 -> 72; // French Guiana (France)
            case 84 -> 73; // French Polynesia (France)
            case 85 -> 74; // French Southern and Antarctic Islands (France)
            case 86 -> 75; // Gabon
            case 87 -> 76; // The Gambia
            case 88 -> 77; // Gaza Strip (Israel)
            case 89 -> 251; // Georgia
            case 90 -> 78; // Germany
            case 91 -> 79; // Ghana
            case 92 -> 80; // Gibraltar (United Kingdom)
            case 93 -> 81; // Glorioso Islands (France)
            case 94 -> 82; // Greece
            case 95 -> 83; // Greenland (Denmark)
            case 96 -> 84; // Grenada
            case 97 -> 85; // Guadaloupe (France)
            case 98 -> 86; // Guam (United States)
            case 99 -> 87; // Guatemala
            case 100 -> 88; // Guernsey (United Kingdom)
            case 101 -> 89; // Guinea
            case 102 -> 90; // Guinea- Bissau
            case 103 -> 91; // Guyana
            case 104 -> 92; // Haiti
            case 105 -> 93; // Heard Island and McDonald Islands (Australia)
            case 106 -> 94; // Honduras
            case 107 -> 95; // Hong Kong (United Kingdom)
            case 108 -> 96; // Howland Island (United States)
            case 109 -> 97; // Hungary
            case 110 -> 98; // Iceland
            case 111 -> 99; // India
            case 112 -> 100; // Indonesia
            case 113 -> 101; // Iran
            case 114 -> 102; // Iraq
            case 115 -> 104; // Ireland
            case 116 -> 105; // Israel
            case 117 -> 106; // Italy
            case 118 -> 108; // Jamaica
            case 119 -> 109; // Jan Mayen (Norway)
            case 120 -> 110; // Japan
            case 121 -> 111; // Jarvis Island (United States)
            case 122 -> 112; // Jersey (United Kingdom)
            case 123 -> 113; // Johnston Atoll (United States)
            case 124 -> 114; // Jordan
            case 125 -> 115; // Juan de Nova Island
            case 126 -> 252; // Kazakhstan
            case 127 -> 116; // Kenya
            case 128 -> 117; // Kingman Reef (United States)
            case 129 -> 118; // Kiribati
            case 130 -> 119; // Democratic People's Republic of Korea
            case 131 -> 120; // Republic of (South) Korea
            case 132 -> 121; // Kuwait
            case 133 -> 253; // Kyrgyzstan
            case 134 -> 122; // Laos
            case 135 -> 254; // Latvia
            case 136 -> 123; // Lebanon
            case 137 -> 124; // Lesotho
            case 138 -> 125; // Liberia
            case 139 -> 126; // Libya
            case 140 -> 127; // Liechtenstein
            case 141 -> 255; // Lithuania
            case 142 -> 128; // Luxembourg
            case 143 -> 130; // Macau (Portugal)
            case 144 -> 256; // Macedonia
            case 145 -> 129; // Madagascar
            case 146 -> 131; // Malawi
            case 147 -> 132; // Malaysia
            case 148 -> 133; // Maldives
            case 149 -> 134; // Mali
            case 150 -> 135; // Malta
            case 151 -> 136; // Isle of Man (United Kingdom)
            case 152 -> 137; // Marshall Islands
            case 153 -> 138; // Martinique (France)
            case 154 -> 139; // Mauritania
            case 155 -> 140; // Mauritius
            case 156 -> 141; // Mayotte (France)
            case 157 -> 142; // Mexico
            case 158 -> 143; // Federative States of Micronesia
            case 159 -> 257; // Midway Islands (United States)
            case 160 -> 258; // Moldova
            case 161 -> 144; // Monaco
            case 162 -> 145; // Mongolia
            case 163 -> 146; // Montserrat (United Kingdom)
            case 164 -> 147; // Morocco
            case 165 -> 148; // Mozambique
            case 166 -> 149; // Namibia (South West Africa)
            case 167 -> 150; // Nauru
            case 168 -> 151; // Navassa Island (United States)
            case 169 -> 152; // Nepal
            case 170 -> 153; // Netherlands
            case 171 -> 154; // Netherlands Antilles (Curacao, Bonaire, Saba, Sint Maarten Sint Eustatius)
            case 172 -> 155; // New Caledonia (France)
            case 173 -> 156; // New Zealand
            case 174 -> 157; // Nicaragua
            case 175 -> 158; // Niger
            case 176 -> 159; // Nigeria
            case 177 -> 160; // Niue (New Zealand)
            case 178 -> 161; // Norfolk Island (Australia)
            case 179 -> 162; // Northern Mariana Islands (United States)
            case 180 -> 163; // Norway
            case 181 -> 164; // Oman
            case 183 -> 216; // Pacific Islands, Trust Territory of the (Palau)
            case 184 -> 165; // Pakistan
            case 185 -> 166; // Palmyra Atoll (United States)
            case 186 -> 168; // Panama
            case 187 -> 169; // Papua New Guinea
            case 188 -> 170; // Paracel Islands (International - Occupied by China, also claimed by Taiwan and Vietnam)
            case 189 -> 171; // Paraguay
            case 190 -> 172; // Peru
            case 191 -> 173; // Philippines
            case 192 -> 174; // Pitcairn Islands (United Kingdom)
            case 193 -> 175; // Poland
            case 194 -> 176; // Portugal
            case 195 -> 177; // Puerto Rico (United States)
            case 196 -> 178; // Qatar
            case 197 -> 179; // Reunion (France)
            case 198 -> 180; // Romania
            case 199 -> 222; // Commonwealth of Independent States or 260: Russia
            case 201 -> 182; // St. Kitts and Nevis
            case 202 -> 183; // St. Helena (United Kingdom)
            case 203 -> 184; // St. Lucia
            case 204 -> 185; // St. Pierre and Miquelon (France)
            case 205 -> 186; // St. Vincent and the Grenadines
            case 206 -> 236; // Western Samoa
            case 207 -> 187; // San Marino
            case 208 -> 188; // Sao Tome and Principe
            case 209 -> 189; // Saudi Arabia
            case 210 -> 190; // Senegal
            case 211 -> 191; // Seychelles
            case 212 -> 192; // Sierra Leone
            case 213 -> 193; // Singapore
            case 215 -> 262; // Slovenia
            case 216 -> 194; // Solomon Islands
            case 217 -> 195; // Somalia
            case 218 -> 197; // South Africa
            case 219 -> 196; // South Georgia and the South Sandwich Islands(United Kingdom)
            case 220 -> 198; // Spain
            case 221 ->
                    199; // Spratly Islands (International - parts occupied and claimed by China,Malaysia, Philippines, Taiwan, Vietnam)"
            case 222 -> 200; // Sri Lanka
            case 223 -> 201; // Sudan
            case 224 -> 202; // Suriname
            case 225 -> 203; // Svalbard (Norway)
            case 226 -> 204; // Swaziland
            case 227 -> 205; // Sweden
            case 228 -> 228; // Switzerland
            case 229 -> 207; // Syria
            case 230 -> 208; // Taiwan
            case 231 -> 263; // Tajikistan
            case 232 -> 209; // Tanzania
            case 233 -> 210; // Thailand
            case 234 -> 211; // Togo
            case 235 -> 212; // Tokelau (New Zealand)
            case 236 -> 213; // Tonga
            case 237 -> 214; // Trinidad and Tobago
            case 238 -> 215; // Tromelin Island (France)
            case 239 -> 217; // Tunisia
            case 240 -> 218; // Turkey
            case 241 -> 264; // Turkmenistan
            case 242 -> 219; // Turks and Caicos Islands (United Kingdom)
            case 243 -> 220; // Tuvalu
            case 244 -> 221; // Uganda
            case 245 -> 265; // Ukraine
            case 246 -> 223; // United Arab Emirates
            case 247 -> 224; // United Kingdom
            case 248 -> 225; // United States
            case 249 -> 226; // Uruguay
            case 250 -> 266; // Uzbekistan
            case 251 -> 227; // Vanuatu
            case 252 -> 228; // Vatican City (Holy See)
            case 253 -> 229; // Venezuela
            case 254 -> 230; // Vietnam
            case 255 -> 231; // Virgin Islands (United States)
            case 256 -> 232; // Wake Island (United States)
            case 257 -> 233; // Wallis and Futuna (France)
            case 258 -> 235; // West Bank (Israel)
            case 259 -> 234; // Western Sahara
            case 260 -> 237; // Yemen
            case 262 -> 242; // Zambia
            case 263 -> 243; // Zimbabwe
            case 343 -> 261; // Serbia and Montenegro (Montenegro to separate) also 259: Montenegro
            default -> 0; // Other
        };
    }

    /**
     * Get the DIS Category for a given VMF Dimension, Type and SubType
     *
     * @param dimension VMF Dimension
     * @param type      VMF Type
     * @param subtype   VMF SubType
     * @return The DIS Category
     */
    public static int getDisCategory(int dimension, int type, int subtype) {
        return switch (getDisKind(dimension)) { // DIS Kind
            case 1 -> // Platform
                    switch (getDisDomain(dimension)) { // DIS Domain
                        case 1 -> // DomainLand
                                switch (type) {
                                    case 0 -> // ARMORED
                                            switch (subtype) {
                                                case 0 -> 1; // TANK
                                                case 1 -> 3; // LIGHT TANK
                                                case 2 -> 2; // MEDIUM TANK
                                                case 3 -> 1; // HEAVY TANK
                                                case 4 -> 3; // LIGHT TANK RECOVERY VEHICLE
                                                case 5 -> 3; // MEDIUM TANK RECOVERY VEHICLE
                                                case 6 -> 3; // HEAVY TANK RECOVERY VEHICLE
                                                case 7 -> 2; // APC
                                                case 8 -> 3; // APC RECOVERY
                                                case 9 -> 2; // ARMORED INFANTRY
                                                case 10 -> 29; // C2V/ACV
                                                case 11 -> 3; // COMBAT SERVICE SUPPORT VEHICLE
                                                case 12 -> 2; // LIGHT ARMORED VEHICLE
                                                case 14 -> 3; // AMBULANCE, ARMORED
                                                default -> 0;
                                            };
                                    case 1 -> // UTILITY VEHICLE
                                            switch (subtype) {
                                                case 0 -> 82; // BUS
                                                case 1 -> 14; // SEMI
                                                case 2 -> 14; // SEMI, LIGHT
                                                case 3 -> 14; // SEMI, MEDIUM
                                                case 4 -> 14; // SEMI, HEAVY
                                                case 5 -> 83; // LIMITED CROSS COUNTRY TRUCK
                                                case 6 -> 83; // CROSS COUNTRY TRUCK
                                                case 7 -> 87; // WATER CRAFT
                                                case 8 -> 7; // TOW TRUCK
                                                case 9 -> 84; // TOW TRUCK, LIGHT
                                                case 10 -> 84; // TOW TRUCK, MEDIUM
                                                case 11 -> 7; // TOW TRUCK, HEAVY
                                                default -> 0;
                                            };
                                    case 2 -> // ENGINEER VEHICLE
                                            switch (subtype) {
                                                case 0 -> 3; // BRIDGE
                                                case 1 -> 3; // EARTHMOVER
                                                case 2 -> 87; // CONSTRUCTION VEHICLE
                                                case 3 -> 2; // MINE LAYING VEHICLE
                                                case 4 -> 1; // MINE CLEARING VEHICLE (MCV)
                                                case 5 -> 3; // ARMORED MOUNTED MCV
                                                case 6 -> 3; // TRAILER MOUNTED MCV
                                                case 7 -> 7; // ARMORED CARRIER WITH VOLCANO
                                                case 8 -> 9; // TRUCK MOUNTED WITH VOLCANO
                                                case 9 -> 3; // DOZER
                                                case 10 -> 3; // DOZER, ARMORED
                                                case 11 -> 3; // ARMORED ASSAULT
                                                case 12 -> 3; // ARMORED ENGINEER RECON VEHICLE (AERV)
                                                case 13 -> 18; // BACKHOE
                                                case 14 -> 3; // FERRY TRANSPORTER
                                                default -> 0;
                                            };
                                    case 3 -> 24; // TRAIN LOCOMOTIVE

                                    case 4 -> // CIVILIAN VEHICLE
                                            switch (subtype) {
                                                case 0 -> 81; // AUTOMOBILE
                                                case 1 -> 81; // COMPACT AUTOMOBILE
                                                case 2 -> 81; // MIDSIZE AUTOMOBILE
                                                case 3 -> 81; // SEDAN AUTOMOBILE
                                                case 4 -> 83; // OPEN-BED TRUCK
                                                case 5 -> 83; // PICK-UP OPEN-BED TRUCK
                                                case 6 -> 83; // SMALL OPEN-BED TRUCK
                                                case 7 -> 83; // LARGE OPEN-BED TRUCK
                                                case 8 -> 81; // MULTI-PASSENGER VEHICLE
                                                case 9 -> 83; // VAN, MULTI-PASSENGER VEHICLE
                                                case 10 -> 81; // SMALL BUS, MULTI-PASSENGER
                                                case 11 -> 82; // LARGE BUS, MULTI-PASSENGER
                                                case 12 -> 81; // UTILITY VEHICLE
                                                case 13 -> 81; // SPORT UTILITY VEHICLE (SUV)
                                                case 14 -> 83; // SMALL BOX TRUCK
                                                case 15 -> 83; // LARGE BOX TRUCK
                                                case 16 -> 83; // JEEP TYPE VEHICLE
                                                case 17 -> 81; // SMALL/LIGHT JEEP VEHICLE
                                                case 18 -> 81; // MEDIUM JEEP TYPE VEHICLE
                                                case 19 -> 81; // LARGE/HEAVY JEEP TYPE VEHICLE
                                                case 20 -> 85; // TRACTOR TRAILER TRUCK WITH BOX TRAILER
                                                case 21 -> 85; // SMALL/LIGHT BOX TRAILER, TRACTOR TRAILER TRUCK
                                                case 22 -> 85; // MEDIUM BOX TRAILER, TRACTOR TRAILER TRUCK
                                                case 23 -> 85; // LARGE/HEAVY BOX TRAILER, TRACTOR TRAILER TRUCK
                                                case 24 -> 85; // TRACTOR TRAILER TRUCK WITH FLATBED TRAILER
                                                case 25 -> 85; // SMALL/LIGHT FLATBED TRAILER TRACTOR TRAILER TRUCK
                                                case 26 -> 85; // MEDIUM FLATBED TRAILER, TRACTOR TRAILER TRUCK
                                                case 27 -> 85; // LARGE/HEAVY FLATBED TRAILER TRACTOR TRAILER TRUCK
                                                case 29 -> 85; // MISSILE SUPPORT VEHICLE
                                                case 30 -> 85; // MISSILE SUPPORT VEHICLE TRANSLOADER
                                                case 31 -> 85; // MISSILE SUPPORT VEHICLE TRANSPORTER
                                                case 32 -> 85; // MISSILE SUPPORT VEHICLE CRANE/LOADING DEVICE
                                                case 33 -> 85; // MISSILE SUPPORT VEHICLE PROPELLANT TRANSPORTER
                                                case 34 -> 85; // MISSILE SUPPORT VEHICLE WARHEAD TRANSPORTER
                                                default -> 0;
                                            };
                                    default -> 0;
                                };
                        case 2 -> // DomainAir
                                switch (type) {
                                    case 0 -> // MILITARY FIXED WING
                                            switch (subtype) {
                                                case 0 -> 3; // BOMBER
                                                case 1 -> 1; // FIGHTER
                                                case 2 -> 2; // INTERCEPTOR
                                                case 3 -> 40; // TRAINER
                                                case 4 -> 2; // ATTACK/STRIKE
                                                case 5 -> 5; // VSTOL
                                                case 6 -> 4; // TANKER
                                                case 7 -> 4; // CARGO AIRLIFT (TRANSPORT)
                                                case 8 -> 4; // CARGO AIRLIFT (LIGHT)
                                                case 9 -> 4; // CARGO AIRLIFT (MEDIUM)
                                                case 10 -> 4; // CARGO AIRLIFT (HEAVY)
                                                case 11 -> 6; // ELECTRONIC COUNTERMEASURES (ECM/JAMMER)
                                                case 12 -> 9; // MEDEVAC
                                                case 13 -> 7; // RECONNAISSANCE
                                                case 14 -> 5; // AIRBORNE EARLY WARNING (AEW)
                                                case 15 -> 5; // ELECTRONIC SURVEILLANCE MEASURES
                                                case 16 -> 6; // PHOTOGRAPHIC
                                                case 17 -> 5; // PATROL
                                                case 18 -> 22; // ANTI SURFACE WARFARE/ASUW
                                                case 19 -> 22; // MINE COUNTER MEASURES
                                                case 20 -> 0; // UTILITY
                                                case 21 -> 0; // UTILITY (LIGHT)
                                                case 22 -> 0; // UTILITY (MEDIUM)
                                                case 23 -> 0; // UTILITY (HEAVY)
                                                case 24 -> 5; // COMMUNICATIONS (C3I)
                                                case 25 -> 9; // SEARCH AND RESCUE (CSAR)
                                                case 26 -> 6; // AIRBORNE COMMAND POST (C2)
                                                case 27 -> 50; // DRONE (RPV/UAV)
                                                case 28 -> 22; // ANTISUBMARINE WARFARE (ASW) CARRIER BASED
                                                case 29 -> 25; // SPECIAL OPERATIONS FORCES (SOF)
                                                case 31 -> 50; // DRONE (RPV/UAV), ATTACK
                                                case 32 -> 50; // DRONE (RPV/UAV), BOMBER
                                                case 33 -> 50; // DRONE (RPV/UAV), CARGO
                                                case 34 -> 50; // DRONE (RPV/UAV), AIRBORNE COMMAND POST
                                                case 35 -> 50; // DRONE (RPV/UAV), FIGHTER
                                                case 36 -> 50; // DRONE (RPV/UAV), SEARCH & RESCUE (CSAR)
                                                case 37 -> 50; // DRONE (RPV/UAV), ELECTRONIC COUNTERMEASURES (JAMMER)
                                                case 38 -> 50; // DRONE (RPV/UAV), TANKER";
                                                case 39 -> 50; // DRONE (RPV/UAV), VSTOL
                                                case 40 -> 50; // DRONE (RPV/UAV), SPECIAL OPERATIONS FORCES (SOF)
                                                case 42 -> 50; // DRONE (RPV/UAV), MINE COUNTERMEASURES
                                                case 43 -> 50; // DRONE (RPV/UAV), ANTI-SURFACE WARFARE (ASUW)
                                                case 44 -> 50; // DRONE (RPV/UAV), PATROL
                                                case 45 ->
                                                        50; // DRONE (RPV/UAV), RECONNAISSANCE AIRBORNE EARLY WARNING (AEW)
                                                case 46 -> 50; // DRONE (RPV/UAV), ELECTRONIC SURVEILLANCE MEASURES
                                                case 47 -> 50; // DRONE (RPV/UAV), PHOTOGRAPHIC
                                                case 48 -> 50; // DRONE (RPV/UAV), ANTI- SUBMARINE WARFARE (ASW)
                                                case 49 -> 50; // DRONE (RPV/UAV), TRAINER
                                                case 50 -> 50; // DRONE (RPV/UAV), UTILITY
                                                case 51 -> 50; // DRONE (RPV/UAV), COMMUNICATIONS (C3I)
                                                case 52 -> 50; // DRONE (RPV/UAV), MEDIVAC
                                                default -> 0;
                                            };
                                    case 1 -> // MILITARY ROTARY WING
                                            switch (subtype) {
                                                case 0 -> 2; // ATTACK
                                                case 1 -> 22; // ANTISUBMARINE WARFARE/MPA
                                                case 2 -> 21; // UTILITY
                                                case 3 -> 21; // UTILITY (LIGHT)
                                                case 4 -> 21; // UTILITY (MEDIUM)
                                                case 5 -> 21; // UTILITY (HEAVY)
                                                case 6 -> 22; // MINE COUNTER MEASURES
                                                case 7 -> 9; // COMBAT SEARCH AND RESCUE (CSAR)
                                                case 8 -> 7; // RECONNAISSANCE
                                                case 9 -> 50; // DRONE (RPV/UAV)
                                                case 10 -> 4; // CARGO AIRLIFT (TRANSPORT)
                                                case 11 -> 4; // CARGO AIRLIFT (LIGHT)
                                                case 12 -> 4; // CARGO AIRLIFT (MEDIUM)
                                                case 13 -> 4; // CARGO AIRLIFT (HEAVY)
                                                case 14 -> 40; // TRAINER
                                                case 15 -> 9; // MEDEVAC
                                                case 16 -> 25; // SPECIAL OPERATIONS FORCES (SOF)
                                                case 17 -> 5; // AIRBORNE COMMAND POST (C2)
                                                case 18 -> 4; // TANKER
                                                case 19 -> 6; // ELECTRONIC COUNTERMEASURES (ECM/JAMMER)
                                                default -> 0;
                                            };

                                    case 2 -> 0; // MILITARY LIGHTER THAN AIR (Not Used)

                                    case 3 -> 1; // WEAPON Generic Air Weapon TODO: Define Air Weapons

                                    case 4 -> // CIVIL AIRCRAFT
                                            switch (subtype) {
                                                case 0 -> 83; // FIXED WING
                                                case 1 -> 90; // ROTARY WING
                                                case 2 -> 100; // LIGHTER THAN AIR
                                                default -> 0;
                                            };

                                    case 5 -> 0; // VIP AIRCRAFT (Not Used)

                                    case 6 -> 0; // VIP AIRCRAFT ESCORT (Not Used)

                                    case 7 -> 0; // DECOY (Not Used)

                                    default -> 0;
                                };
                        case 3 -> // DomainSurface
                                1; // Generic Ship TODO: Define Surface Platforms

                        case 4 -> // DomainSubsurface
                                1; // Generic Submarine TODO: Define Subsurface Platforms

                        case 5 -> // DomainSpace
                                1; // Generic Space Vehicle TODO: Define Space Platforms

                        default -> 0;
                    };
            case 2 -> // Munition
                    1; // Generic Munition TODO: Define Munitions

            case 3 -> // Lifeform
                    1; // Generic LifeForm TODO: Define LifeForms

            case 4 -> // Environmental
                    1; // Generic Environmental TODO: Define Environmentals

            case 5 -> // Cultural Feature
                    1; // Generic Cultural Feature TODO: Define Cultural Features

            case 6 -> // Supply
                    1; // Generic Supply TODO: Define Supplies

            case 7 -> // Radio
                    1; // Generic Radio TODO: Define Radios

            case 8 -> // Expendable
                    1; // Generic Expendable TODO: Define Expendables

            default -> 0;
        };
    }

    /**
     * Return a printable Position Quality (DFI/DUI: 4119/005)
     *
     * @param positionQuality VMF Classification
     * @return a printable Classification
     */
    public static String getPositionQuality(int positionQuality) {
        return switch (positionQuality) {
            case 1 -> "<= 1 METER";
            case 2 -> "1-10 METERS";
            case 3 -> "10-25 METERS";
            case 4 -> "25-50 METERS";
            case 5 -> "50-75 RS";
            case 6 -> "75-100 METERS";
            case 7 -> "100-200 METERS";
            case 8 -> "200-500 METERS";
            case 9 -> "500-1000 METERS";
            case 10 -> "1-5 KILOMETERS";
            case 11 -> ">= 5 KILOMETERS";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable Classification (DFI/DUI: 4083/001)
     *
     * @param classification VMF Classification
     * @return a printable Classification
     */
    public static String getClassification(int classification) {
        return switch (classification) {
            case 1 -> "UNCLASSIFIED";
            case 2 -> "CONFIDENTIAL";
            case 3 -> "SECRET";
            case 4 -> "TOP SECRET";
            case 5 -> "EFTO";
            case 6 -> "SECRET NOFORN";
            case 7 -> "SECRET RESTRICTED";
            case 8 -> "CONFIDENTIAL FORMERLY RESTRICTED DATA";
            case 9 -> "SECRET FORMERLY RESTRICTED DATA";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable Service/Agency
     *
     * @param service VMF Service/Agency
     * @return a printable Service/Agency
     */
    public static String getService(String service) {
        return switch (service.toUpperCase(Locale.ROOT)) {
            case "A" -> "Army";
            case "M" -> "Marine";
            case "N" -> "Navy";
            case "F" -> "Air Force";
            case "C" -> "Coast Guard";
            case "S" -> "Space Force";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable URN Type
     *
     * @param urnType VMF URN Type
     * @return a printable URN Type
     */
    public static String getUrnType(int urnType) {
        return switch (urnType) {
            case 0 -> "Broadcast";
            case 1 -> "Individual";
            case 2 -> "Unit";
            case 3 -> "Multicast";
            case 4 -> "Well Known Multicast";
            case 5 -> "Non-Digitized Platform";
            case 6 -> "ABCS role";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable Role Code
     *
     * @param roleCode VMF Role Code
     * @return a printable Role Code
     */
    public static String getRoleCode(int roleCode) {
        return getEchelonCode(roleCode);
    }

    /**
     * Return a printable Echelon Code
     *
     * @param echelonCode VMF Echelon Code
     * @return a printable Echelon Code
     */
    public static String getEchelonCode(int echelonCode) {
        return switch (echelonCode) {
            case 1 -> "S2/G2 (Intelligence)";
            case 2 -> "S3/G3 (Operations)";
            case 3 -> "S4/G4 (Logistics)";
            case 4 -> "S6/G6 (Communications and/or Security officer)";
            case 5 -> "FIST (Fires)";
            case 6 -> "FSE (Fire Support Element)";
            case 7 -> "CDR/LDR (Commander/Leader)";
            case 8 -> "XO (Executive Officer)";
            case 9 -> "Supply Section";
            case 10 -> "CSM/SGM/1SG/PSG (Command Sergeants Major/Sergeants Major/First Sergeant/Platoon Sergeant)";
            case 11 -> "Squad/Section Leader";
            case 12 -> "SOCOM (Special Operations Command)";
            case 13 -> "TAF/OC (Tactical Air Force Operations Center)";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable System Type
     *
     * @param systemType VMF System Type
     * @return a printable System Type
     */
    public static String getSystemType(int systemType) {
        return switch (systemType) {
            case 1 -> "FBCB2";
            case 2 -> "FBCB2 JCR VEHICLE";
            case 3 -> "FBCB2 JCR TSG";
            case 4 -> "FBCB2 JCR NOC";
            case 5 -> "MTS-ES PLATFORM";
            case 6 -> "MTS-ES CONTROL STATION";
            case 7 -> "FOS";
            case 8 -> "LW";
            case 9 -> "MCS-L (NCU)";
            case 10 -> "ASAS-L (NCU)";
            case 11 -> "AIS";
            case 12 -> "GCSS-A";
            case 13 -> "AMPS";
            case 14 -> "DCARS/DCTS";
            case 15 -> "DTSS-L";
            case 16 -> "DTSS-H";
            case 17 -> "RADIO RELAY/RETRANS";
            case 18 -> "EPLRS GW";
            case 19 -> "EGRU";
            case 20 -> "SIV";
            case 21 -> "ABCS TOC-SVR";
            case 22 -> "UAV";
            case 23 -> "SEN";
            case 24 -> "ISYSCON";
            case 25 -> "GROUND TERMINAL DATA";
            case 26 -> "CGS";
            case 27 -> "ASAS";
            case 28 -> "AFATDS";
            case 29 -> "BCS3";
            case 30 -> "FAADC2I";
            case 31 -> "MCS";
            case 32 -> "MICAD";
            case 33 -> "FAAD GBS";
            case 34 -> "FAAD SC2";
            case 35 -> "LAN-MGR";
            case 36 -> "MDL";
            case 37 -> "EPLRS NM";
            case 38 -> "DAUVS";
            case 39 -> "IMETS";
            case 40 -> "TAIS";
            case 41 -> "AMDWS";
            case 42 -> "GCCS-A";
            case 43 -> "MFDC";
            case 44 -> "C2PC";
            case 45 -> "LEGACY MTS";
            case 46 -> "JTCW";
            case 47 -> "EDM";
            case 48 -> "IFTS";
            case 49 -> "IDM";
            case 50 -> "C2CE DDACT";
            case 51 -> "CPOF-C";
            case 52 -> "CPOF-DBRG";
            case 53 -> "DCGSAIOP";
            case 54 -> "DTSS";
            case 55 -> "MCS-GWY";
            case 56 -> "TIS";
            case 57 -> "FBCB2 JBCP HH";
            case 58 -> "FBCB2 JBCP VEHICLE";
            case 59 -> "FBCB2 JBCP CP";
            case 60 -> "FBCB2 JBCP NOC";
            case 61 -> "FBCB2 JBCP LOG";
            case 62 -> "SPARE 13";
            case 63 -> "SPARE 14";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable Dimension, Entity Type and Entity SubType  (DFI/DUIs: 4173/014, 15 and 18)
     *
     * @param dim     VMF Dimension
     * @param type    VMF Entity Type
     * @param subtype VMF Entity SubType
     * @return a printable Type
     */
    public static String getEntityType(int dim, int type, int subtype) {
        return switch (dim) {
            case 0 -> getDimension(dim) + " - " + getSpaceType(type) + " (" + getSpaceSubType(subtype) + ")";

            case 1 -> getDimension(dim) + " - " + getAirType(type) + " (" + getAirSubType(type, subtype) + ")";

            case 2 ->
                    getDimension(dim) + " - " + getGroundUnitType(type) + " (" + getGroundUnitSubType(type, subtype) + ")";
            case 3 ->
                    getDimension(dim) + " - " + getGroundWeaponType(type) + " (" + getGroundWeaponSubType(type, subtype) + ")";
            case 4 ->
                    getDimension(dim) + " - " + getGroundVehicleType(type) + " (" + getGroundVehicleSubType(type, subtype) + ")";
            case 5 ->
                    getDimension(dim) + " - " + getGroundSensorType(type) + " (" + getGroundSensorSubType(subtype) + ")";
            case 6 ->
                    getDimension(dim) + " - " + getGroundSpecialType(type) + " (" + getGroundSpecialSubType(subtype) + ")";
            case 7 ->
                    getDimension(dim) + " - " + getGroundInstallationType(type) + " (" + getGroundInstallationSubType(type, subtype) + ")";

            case 8 ->
                    getDimension(dim) + " - " + getSeaSurfaceType(type) + " (" + getSeaSurfaceSubType(type, subtype) + ")";

            case 9 ->
                    getDimension(dim) + " - " + getSeaSubsurfaceType(type) + " (" + getSeaSubsurfaceSubType(type, subtype) + ")";

            case 10 -> getDimension(dim) + " - " + getSofType(type) + " (" + getSofSubType(type, subtype) + ")";

            /* 11 - 16 Tactical Graphics */
            /* 17 - 21 Intelligence, */
            /* 22 - 28 Stability, */
            /* 29 - 32 Emergency Management, */

            default -> getDimension(dim) + " - " + "Type: " + type + " Subtype: " + subtype;
        };
    }

    /**
     * Return a printable Space SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param subtype VMF  Space SubType
     * @return a printable Space SubType
     */
    public static String getSpaceSubType(int subtype) {
        return switch (subtype) {
            case 0 -> NO_STATEMENT;
            case 1 -> String.format(SUBTYPE, "UNKNOWN", subtype);
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Air SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param type    VMF  Air Type
     * @param subtype VMF  Air SubType
     * @return a printable Air SubType
     */
    public static String getAirSubType(int type, int subtype) {
        return switch (type) {
            case 0 -> // MILITARY FIXED WING
                    switch (subtype) {
                        case 0 -> "BOMBER";
                        case 1 -> "FIGHTER";
                        case 2 -> "INTERCEPTOR";
                        case 3 -> "TRAINER";
                        case 4 -> "ATTACK/STRIKE";
                        case 5 -> "VSTOL";
                        case 6 -> "TANKER";
                        case 7 -> "CARGO AIRLIFT (TRANSPORT)";
                        case 8 -> "CARGO AIRLIFT (LIGHT)";
                        case 9 -> "CARGO AIRLIFT (MEDIUM)";
                        case 10 -> "CARGO AIRLIFT (HEAVY)";
                        case 11 -> "ELECTRONIC COUNTERMEASURES (ECM/JAMMER)";
                        case 12 -> "MEDEVAC";
                        case 13 -> "RECONNAISSANCE";
                        case 14 -> "AIRBORNE EARLY WARNING (AEW)";
                        case 15 -> "ELECTRONIC SURVEILLANCE MEASURES";
                        case 16 -> "PHOTOGRAPHIC";
                        case 17 -> "PATROL";
                        case 18 -> "ANTI SURFACE WARFARE/ASUW";
                        case 19 -> "MINE COUNTER MEASURES";
                        case 20 -> "UTILITY";
                        case 21 -> "UTILITY (LIGHT)";
                        case 22 -> "UTILITY (MEDIUM)";
                        case 23 -> "UTILITY (HEAVY)";
                        case 24 -> "COMMUNICATIONS (C3I)";
                        case 25 -> "SEARCH AND RESCUE (CSAR)";
                        case 26 -> "AIRBORNE COMMAND POST (C2)";
                        case 27 -> "DRONE (RPV/UAV)";
                        case 28 -> "ANTISUBMARINE WARFARE (ASW) CARRIER BASED";
                        case 29 -> "SPECIAL OPERATIONS FORCES (SOF)";
                        case 30 -> "UNKNOWN";
                        case 31 -> "DRONE (RPV/UAV), ATTACK";
                        case 32 -> "DRONE (RPV/UAV), BOMBER";
                        case 33 -> "DRONE (RPV/UAV), CARGO";
                        case 34 -> "DRONE (RPV/UAV), AIRBORNE COMMAND POST";
                        case 35 -> "DRONE (RPV/UAV), FIGHTER";
                        case 36 -> "DRONE (RPV/UAV), SEARCH & RESCUE (CSAR)";
                        case 37 -> "DRONE (RPV/UAV), ELECTRONIC COUNTERMEASURES (JAMMER)";
                        case 38 -> "DRONE (RPV/UAV), TANKER";
                        case 39 -> "DRONE (RPV/UAV), VSTOL";
                        case 40 -> "DRONE (RPV/UAV), SPECIAL OPERATIONS FORCES (SOF)";
                        case 42 -> "DRONE (RPV/UAV), MINE COUNTERMEASURES";
                        case 43 -> "DRONE (RPV/UAV), ANTI-SURFACE WARFARE (ASUW)";
                        case 44 -> "DRONE (RPV/UAV), PATROL";
                        case 45 -> "DRONE (RPV/UAV), RECONNAISSANCE AIRBORNE EARLY WARNING (AEW)";
                        case 46 -> "DRONE (RPV/UAV), ELECTRONIC SURVEILLANCE MEASURES";
                        case 47 -> "DRONE (RPV/UAV), PHOTOGRAPHIC";
                        case 48 -> "DRONE (RPV/UAV), ANTI- SUBMARINE WARFARE (ASW)";
                        case 49 -> "DRONE (RPV/UAV), TRAINER";
                        case 50 -> "DRONE (RPV/UAV), UTILITY";
                        case 51 -> "DRONE (RPV/UAV), COMMUNICATIONS (C3I)";
                        case 52 -> "DRONE (RPV/UAV), MEDIVAC";
                        case 53 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 1 -> // MILITARY ROTARY WING
                    switch (subtype) {
                        case 0 -> "ATTACK";
                        case 1 -> "ANTISUBMARINE WARFARE/MPA";
                        case 2 -> "UTILITY";
                        case 3 -> "UTILITY (LIGHT)";
                        case 4 -> "UTILITY (MEDIUM)";
                        case 5 -> "UTILITY (HEAVY)";
                        case 6 -> "MINE COUNTER MEASURES";
                        case 7 -> "COMBAT SEARCH AND RESCUE (CSAR)";
                        case 8 -> "RECONNAISSANCE";
                        case 9 -> "DRONE (RPV/UAV)";
                        case 10 -> "CARGO AIRLIFT (TRANSPORT)";
                        case 11 -> "CARGO AIRLIFT (LIGHT)";
                        case 12 -> "CARGO AIRLIFT (MEDIUM)";
                        case 13 -> "CARGO AIRLIFT (HEAVY)";
                        case 14 -> "TRAINER";
                        case 15 -> "MEDEVAC";
                        case 16 -> "SPECIAL OPERATIONS FORCES (SOF)";
                        case 17 -> "AIRBORNE COMMAND POST (C2)";
                        case 18 -> "TANKER";
                        case 19 -> "ELECTRONIC COUNTERMEASURES (ECM/JAMMER)";
                        case 20 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 3 -> // WEAPON
                    switch (subtype) {
                        case 0 -> "MISSILE IN FLIGHT";
                        case 1 -> "SURFACE/LAND LAUNCHED MISSILE";
                        case 2 -> "SURFACE TO SURFACE MISSILE (SSM)";
                        case 3 -> "SURFACE TO AIR MISSILE (SAM)";
                        case 4 -> "AIR LAUNCHED MISSILE";
                        case 5 -> "AIR TO SURFACE MISSILE (ASM)";
                        case 6 -> "AIR TO AIR MISSILE (AAM)";
                        case 7 -> "SUBSURFACE TO SURFACE MISSILE (S/SSM)";
                        case 8 -> "DECOY";
                        case 9 -> "UNKNOWN";
                        case 10 -> "ANTI BALLISTIC MISSILE (ABM)";
                        case 11 -> "AIR TO SPACE MISSILE";
                        case 12 -> "BOMB";
                        case 13 -> "SURFACE TO SUBSURFACE MISSILE";
                        case 14 -> "AIR TO AIR MISSILE (AAM)";
                        case 15 -> "CRUISE MISSILE";
                        case 16 -> "BALLISTIC MISSILE";
                        case 17 -> "UNDEFINED";
                        case 18 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 4 -> // CIVIL AIRCRAFT
                    switch (subtype) {
                        case 0 -> "FIXED WING";
                        case 1 -> "ROTARY WING";
                        case 2 -> "LIGHTER THAN AIR";
                        case 3 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    }; // MILITARY LIGHTER THAN AIR
            // VIP AIRCRAFT
            // VIP AIRCRAFT ESCORT
            case 2, 5, 6, 7 -> // DECOY
                    (subtype == 0) ? NO_STATEMENT : String.format(SUBTYPE, "UNDEFINED", subtype);
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Ground Unit SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param type    VMF  Ground Unit Type
     * @param subtype VMF  Ground Unit SubType
     * @return a printable Ground Unit SubType
     */
    public static String getGroundUnitSubType(int type, int subtype) {
        return switch (type) {
            case 0 -> // AIR DEFENSE
                    switch (subtype) {
                        case 0 -> "SHORT RANGE";
                        case 1 -> "CHAPARRAL";
                        case 2 -> "STINGER";
                        case 3 -> "VULCAN";
                        case 4 -> "AIR DEFENSE MISSILE";
                        case 5 -> "LIGHT MISSILE";
                        case 6 -> "AVENGER";
                        case 7 -> "MEDIUM MISSILE";
                        case 8 -> "HEAVY MISSILE";
                        case 9 -> "GUN UNIT";
                        case 10 -> "COMPOSIT";
                        case 11 -> "TARGETING UNIT";
                        case 12 -> "THEATER MISSILE DEFENSE UNIT";
                        case 13 -> "H/MAD";
                        case 14 -> "HAWK";
                        case 15 -> "PATRIOT";
                        case 16 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 1 -> // ARMOR
                    switch (subtype) {
                        case 0 -> "ARMOR TRACK";
                        case 1 -> "AIRBORNE, TRACK";
                        case 2 -> "AMPHIBIOUS, TRACK";
                        case 3 -> "AMPHIBIOUS RECOVERY, TRACK";
                        case 4 -> "LIGHT, TRACK";
                        case 5 -> "MEDIUM, TRACK";
                        case 6 -> "HEAVY, TRACK";
                        case 7 -> "RECOVERY, TRACK";
                        case 8 -> "ARMOR, WHEELED";
                        case 9 -> "AIR ASSAULT WHEELED";
                        case 10 -> "AIRBORNE WHEELED";
                        case 11 -> "AMPHIBIOUS WHEELED";
                        case 12 -> "AMPHIBIOUS RECOVERY WHEELED";
                        case 13 -> "LIGHT WHEELED";
                        case 14 -> "MEDIUM WHEELED";
                        case 15 -> "HEAVY WHEELED";
                        case 16 -> "RECOVERY WHEELED";
                        case 17 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 2 -> // ANTI ARMOR
                    switch (subtype) {
                        case 0 -> "DISMOUNTED";
                        case 1 -> "LIGHT";
                        case 2 -> "AIRBORNE";
                        case 3 -> "AIR ASSAULT";
                        case 4 -> "MOUNTAIN";
                        case 5 -> "ARCTIC";
                        case 6 -> "ARMORED";
                        case 7 -> "ARMORED TRACK";
                        case 8 -> "ARMORED WHEELED";
                        case 9 -> "ARMORED AIR ASSAULT";
                        case 10 -> "MOTORIZED";
                        case 11 -> "MOTORIZED AIR ASSAULT";
                        case 12 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 3 -> // AVIATION
                    switch (subtype) {
                        case 0 -> "FIXED WING";
                        case 1 -> "FIXED WING UTILITY";
                        case 2 -> "FIXED WING ATTACK";
                        case 3 -> "FIXED WING RECONNAISSANCE";
                        case 4 -> "ROTARY WING";
                        case 5 -> "ROTARY WING ATTACK";
                        case 6 -> "ROTARY WING SCOUT";
                        case 7 -> "ROTARY WING ANTI-SUBMARINE WARFARE";
                        case 8 -> "ROTARY WING UTILITY";
                        case 9 -> "ROTARY WING UTILITY LIGHT";
                        case 10 -> "ROTARY WING UTILITY MEDIUM";
                        case 11 -> "ROTARY WING UTILITY HEAVY";
                        case 12 -> "ROTARY WING C2";
                        case 13 -> "ROTARY WING MEDEVAC";
                        case 14 -> "ROTARY WING MINE COUNTERMEASURE";
                        case 15 -> "SEARCH AND RESCUE";
                        case 16 -> "COMPOSITE";
                        case 17 -> "V/STOL";
                        case 18 -> "UNMANNED AERIAL VEHICLE";
                        case 19 -> "UNMANNED FIXED WING";
                        case 20 -> "UNMANNED ROTARY WING";
                        case 21 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 4 -> // INFANTRY
                    switch (subtype) {
                        case 0 -> "LIGHT";
                        case 1 -> "MOTORIZED";
                        case 2 -> "MOUNTAIN";
                        case 3 -> "AIRBORNE";
                        case 4 -> "AIR ASSAULT";
                        case 5 -> "MECHANIZED";
                        case 6 -> "NAVAL";
                        case 7 -> "INFANTRY FIGHTING VEHICLE";
                        case 8 -> "ARCTIC";
                        case 9 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 5 -> // ENGINEER
                    switch (subtype) {
                        case 0 -> "COMBAT ENGINEER";
                        case 1 -> "AIR ASSAULT COMBAT ENGINEER";
                        case 2 -> "AIRBORNE COMBAT ENGINEER";
                        case 3 -> "ARCTIC COMBAT ENGINEER";
                        case 4 -> "LIGHT (SAPPER) COMBAT ENGINEER";
                        case 5 -> "MEDIUM COMBAT ENGINEER";
                        case 6 -> "HEAVY COMBAT ENGINEER";
                        case 7 -> "MECH (TRACK) COMBAT ENGINEER";
                        case 8 -> "MOTORIZED COMBAT ENGINEER";
                        case 9 -> "MOUNTAIN COMBAT ENGINEER";
                        case 10 -> "RECON COMBAT ENGINEER";
                        case 11 -> "CONSTRUCTION ENGINEER";
                        case 12 -> "NAVAL CONSTRUCTION ENGINEER";
                        case 13 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 6 -> // FIELD ARTILLERY
                    switch (subtype) {
                        case 0 -> "HOWITZER/GUN";
                        case 1 -> "SP HOWITZER/GUN";
                        case 2 -> "AIR ASSAULT HOWITZER/GUN";
                        case 3 -> "AIRBORNE HOWITZER/GUN";
                        case 4 -> "ARCTIC HOWITZER/GUN";
                        case 5 -> "MOUNTAIN HOWITZER/GUN";
                        case 6 -> "LIGHT HOWITZER/GUN";
                        case 7 -> "MEDIUM HOWITZER/GUN";
                        case 8 -> "HEAVY HOWITZER/GUN";
                        case 9 -> "AMPHIBIOUS HOWITZER/GUN";
                        case 10 -> "ROCKET";
                        case 11 -> "SINGLE ROCKET LAUNCHER";
                        case 12 -> "SP SINGLE ROCKET LAUNCHER";
                        case 13 -> "TRUCK SINGLE ROCKET LAUNCHER";
                        case 14 -> "TOWED SINGLE ROCKET LAUNCHER";
                        case 15 -> "MULTI ROCKET LAUNCHER";
                        case 16 -> "SP MULTI ROCKET LAUNCHER";
                        case 17 -> "TRUCK MULTI ROCKET LAUNCHER";
                        case 18 -> "TOWED MULTI ROCKET LAUNCHER";
                        case 19 -> "TARGET ACQUISITION";
                        case 20 -> "TARGET ACQUISITION RADAR";
                        case 21 -> "TARGET ACQUISITION SOUND";
                        case 22 -> "TARGET ACQUISITION FLASH";
                        case 23 -> "TARGET ACQUISITION COLT/FIST";
                        case 24 -> "TARGET ACQUISITION COLT/FIST DISMOUNTED";
                        case 25 -> "TARGET ACQUISITION COLT/FIST TRACKED";
                        case 26 -> "TARGET ACQUISITION ANGLICO";
                        case 27 -> "MORTAR";
                        case 28 -> "SP TRACKED MORTAR";
                        case 29 -> "SP WHEELED MORTAR";
                        case 30 -> "TOWED MORTAR";
                        case 31 -> "AIRBORNE TOWED MORTAR";
                        case 32 -> "AIR ASSAULT TOWED MORTAR";
                        case 33 -> "ARCTIC TOWED MORTAR";
                        case 34 -> "MOUNTAIN TOWED MORTAR";
                        case 35 -> "AMPHIBIOUS MORTAR";
                        case 36 -> "ARTILLERY SURVEY";
                        case 37 -> "AIR ASSAULT ARTILLERY SURVEY";
                        case 38 -> "AIRBORNE ARTILLERY SURVEY";
                        case 39 -> "LIGHT ARTILLERY SURVEY";
                        case 40 -> "MOUNTAIN ARTILLERY SURVEY METEOROLOGICAL";
                        case 42 -> "AIR ASSAULT METEOROLOGICAL";
                        case 43 -> "AIRBORNE METEOROLOGICAL";
                        case 44 -> "LIGHT METEOROLOGICAL";
                        case 45 -> "MOUNTAIN METEOROLOGICAL";
                        case 46 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 7 -> // RECONNAISSANCE
                    switch (subtype) {
                        case 0 -> "HORSE";
                        case 1 -> "CAVALRY";
                        case 2 -> "ARMORED CAVALRY";
                        case 3 -> "MOTORIZED CAVALRY";
                        case 4 -> "GROUND CAVALRY";
                        case 5 -> "AIR CAVALRY";
                        case 6 -> "ARCTIC";
                        case 7 -> "AIR ASSAULT";
                        case 8 -> "AIRBORNE";
                        case 9 -> "MOUNTAIN";
                        case 10 -> "LIGHT";
                        case 11 -> "MARINE";
                        case 12 -> "DIVISION MARINE";
                        case 13 -> "FORCE MARINE";
                        case 14 -> "LAR MARINE";
                        case 15 -> "LRS";
                        case 16 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 8 -> // MISSILE (SURFACE-SURFACE)
                    switch (subtype) {
                        case 0 -> "TACTICAL";
                        case 1 -> "STRATEGIC";
                        case 2 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 9 -> // INTERNAL SECURITY FORCES
                    switch (subtype) {
                        case 0 -> "RIVERINE";
                        case 1 -> "GROUND";
                        case 2 -> "DISMOUNTED GROUND";
                        case 3 -> "MOTORIZED GROUND";
                        case 4 -> "MECHANIZED GROUND";
                        case 5 -> "WHEELED MECHANIZED";
                        case 6 -> "RAILROAD";
                        case 7 -> "AVIATION";
                        case 8 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 10 -> // NBC
                    switch (subtype) {
                        case 0 -> "CHEMICAL";
                        case 1 -> "SMOKE/DECON";
                        case 2 -> "MECHANIZED SMOKE/DECON";
                        case 3 -> "MOTORIZED SMOKE/DECON";
                        case 4 -> "SMOKE";
                        case 5 -> "MOTORIZED SMOKE";
                        case 6 -> "ARMOR SMOKE";
                        case 7 -> "CHEMICAL RECON";
                        case 8 -> "CHEMICAL WHEELED ARMORED VEHICLE";
                        case 9 -> "CHEMICAL WHEELED ARMORED VEHICLE RECON SURV";
                        case 10 -> "NUCLEAR";
                        case 11 -> "BIOLOGICAL";
                        case 12 -> "BIOLOGICAL RECON EQUIPPED";
                        case 13 -> "DECONTAMINATION";
                        case 14 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 11 -> // MILITARY INTELLIGENCE
                    switch (subtype) {
                        case 0 -> "AERIAL EXPLOITATION";
                        case 1 -> "SIGINT";
                        case 2 -> "ELECTRONIC WARFARE (EW)";
                        case 3 -> "ARMORED WHEELED VEHICLE";
                        case 4 -> "DIRECTION FINDING";
                        case 5 -> "INTERCEPT";
                        case 6 -> "JAMMING";
                        case 7 -> "JAMMING THEATER";
                        case 8 -> "JAMMING CORPS";
                        case 9 -> "COUNTER INTELLIGENCE";
                        case 10 -> "SURVEILLANCE";
                        case 11 -> "GROUND SURVEILLANCE RADAR";
                        case 12 -> "SENSOR";
                        case 13 -> "SENSOR SCM";
                        case 14 -> "GROUND STATION MODULE";
                        case 15 -> "METEOROLOGICAL";
                        case 16 -> "OPERATIONS";
                        case 17 -> "TACTICAL EXPLOIT";
                        case 18 -> "INTERROGATION";
                        case 19 -> "JOINT INTEL CENTER";
                        case 20 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 12 -> // LAW ENFORCEMENT UNIT
                    switch (subtype) {
                        case 0 -> "SHORE PATROL";
                        case 1 -> "MILITARY POLICE";
                        case 2 -> "CIVILIAN LAW ENFORCEMENT";
                        case 3 -> "SECURITY POLICE (AIR)";
                        case 4 -> "CENTRAL INTELLIGENCE DIV (CID)";
                        case 5 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 13 -> // SIGNAL UNIT
                    switch (subtype) {
                        case 0 -> "AREA";
                        case 1 -> "COMMUNICATION CONFIGURED PACKAGE";
                        case 2 -> "LARGE COMMUNICATION CONFIGURED PACKAGE (LCCP)";
                        case 3 -> "COMMAND OPERATIONS";
                        case 4 -> "FORWARD COMMUNICATIONS";
                        case 5 -> "MSE";
                        case 6 -> "MSE SMALL EXTENSION NODE (SEN)";
                        case 7 -> "MSE LARGE EXTENSION NODE (LEN)";
                        case 8 -> "MSE NODE CENTER (NC)";
                        case 9 -> "RADIO UNIT";
                        case 10 -> "RADIO UNIT TACTICAL SATELLITE";
                        case 11 -> "RADIO UNIT TELETYPE CENTER";
                        case 12 -> "RADIO UNIT RELAY";
                        case 13 -> "SIGNAL SUPPORT";
                        case 14 -> "TELEPHONE SWITCH";
                        case 15 -> "ELECTRONIC RANGING";
                        case 16 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 17 -> // ADMINISTRATIVE
                    switch (subtype) {
                        case 0 -> "ADMIN THEATER";
                        case 1 -> "ADMIN CORPS";
                        case 2 -> "JAG";
                        case 3 -> "JAG THEATER";
                        case 4 -> "JAG CORPS";
                        case 5 -> "POSTAL";
                        case 6 -> "POSTAL THEATER";
                        case 7 -> "POSTAL CORPS";
                        case 8 -> "FINANCE";
                        case 9 -> "FINANCE THEATER";
                        case 10 -> "FINANCE CORPS";
                        case 11 -> "PERSONNEL SERVICES";
                        case 12 -> "PERSONNEL SERVICES THEATER";
                        case 13 -> "PERSONNEL SERVICES CORPS";
                        case 14 -> "MORTUARY/GRAVES REGISTRATION";
                        case 15 -> "MORTUARY/GRAVES REGISTRATION THEATER";
                        case 16 -> "MORTUARY/GRAVES REGISTRATION CORPS";
                        case 17 -> "RELIGIOUS/CHAPLAIN";
                        case 18 -> "RELIGIOUS/CHAPLAIN THEATER";
                        case 19 -> "RELIGIOUS/CHAPLAIN CORPS";
                        case 20 -> "PUBLIC AFFAIRS";
                        case 21 -> "PUBLIC AFFAIRS THEATER";
                        case 22 -> "PUBLIC AFFAIRS CORPS";
                        case 23 -> "PUBLIC AFFAIRS BROADCAST";
                        case 24 -> "PUBLIC AFFAIRS BROADCAST THEATER";
                        case 25 -> "PUBLIC AFFAIRS BROADCAST CORPS";
                        case 26 -> "PUBLIC AFFAIRS JOINT INFORMATION BUREAU (JIB)";
                        case 27 -> "PUBLIC AFFAIRS JIB THEATER";
                        case 28 -> "PUBLIC AFFAIRS JIB CORPS";
                        case 29 -> "REPLACEMENT HOLDING UNIT (RHU)";
                        case 30 -> "RHU THEATER";
                        case 31 -> "RHU CORPS";
                        case 32 -> "LABOR";
                        case 33 -> "LABOR THEATER";
                        case 34 -> "LABOR CORPS";
                        case 35 -> "MORAL, WELFARE, RECREATION (MWR)";
                        case 36 -> "MWR THEATER";
                        case 37 -> "MWR CORPS";
                        case 38 -> "QUARTERMASTER";
                        case 39 -> "QUARTERMASTER THEATER";
                        case 40 -> "QUARTERMASTER CORPS";
                        case 41 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 18 -> // MEDICAL
                    switch (subtype) {
                        case 0 -> "MEDICAL, THEATER";
                        case 1 -> "MEDICAL, CORPS";
                        case 2 -> "MEDICAL TREATMENT FACILITY";
                        case 3 -> "MEDICAL TREATMENT FACILITY THEATER";
                        case 4 -> "MEDICAL TREATMENT FACILITY CORPS";
                        case 5 -> "VETERINARY";
                        case 6 -> "VETERINARY THEATER";
                        case 7 -> "VETERINARY CORPS";
                        case 8 -> "DENTAL";
                        case 9 -> "DENTAL THEATER";
                        case 10 -> "DENTAL CORPS";
                        case 11 -> "PSYCHOLOGICAL";
                        case 12 -> "PSYCHOLOGICAL THEATER";
                        case 13 -> "PSYCHOLOGICAL CORPS";
                        case 14 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 19 -> // SUPPLY
                    switch (subtype) {
                        case 0 -> "SUPPLY, THEATER";
                        case 1 -> "SUPPLY, CORPS";
                        case 2 -> "CLASS I";
                        case 3 -> "CLASS I THEATER";
                        case 4 -> "CLASS I CORPS";
                        case 5 -> "CLASS II";
                        case 6 -> "CLASS II THEATER";
                        case 7 -> "CLASS II CORPS";
                        case 8 -> "CLASS III";
                        case 9 -> "CLASS III THEATER";
                        case 10 -> "CLASS III CORPS";
                        case 11 -> "CLASS III AVIATION";
                        case 12 -> "CLASS III AVIATION THEATER";
                        case 13 -> "CLASS III AVIATION CORPS";
                        case 14 -> "CLASS IV";
                        case 15 -> "CLASS IV THEATER";
                        case 16 -> "CLASS IV CORPS";
                        case 17 -> "CLASS V";
                        case 18 -> "CLASS V THEATER";
                        case 19 -> "CLASS V CORPS";
                        case 20 -> "CLASS VI";
                        case 21 -> "CLASS VI THEATER";
                        case 22 -> "CLASS VI CORPS";
                        case 23 -> "CLASS VII";
                        case 24 -> "CLASS VII THEATER";
                        case 25 -> "CLASS VII CORPS";
                        case 26 -> "CLASS VIII";
                        case 27 -> "CLASS VIII THEATER";
                        case 28 -> "CLASS VIII CORPS";
                        case 29 -> "CLASS IX";
                        case 30 -> "CLASS IX THEATER";
                        case 31 -> "CLASS IX CORPS";
                        case 32 -> "CLASS X";
                        case 33 -> "CLASS X THEATER";
                        case 34 -> "CLASS X CORPS";
                        case 35 -> "LAUNDRY/BATH";
                        case 36 -> "LAUNDRY/BATH THEATER";
                        case 37 -> "LAUNDRY/BATH CORPS";
                        case 38 -> "WATER";
                        case 39 -> "WATER THEATER";
                        case 40 -> "WATER CORPS";
                        case 42 -> "WATER PURIFICATION";
                        case 43 -> "WATER PURIFICATION THEATER";
                        case 44 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 20 -> // TRANSPORTATION
                    switch (subtype) {
                        case 0 -> "TRANSPORTATION, THEATER";
                        case 1 -> "TRANSPORTATION, CORPS";
                        case 2 -> "MOVING CONTROL CENTER (MCC)";
                        case 3 -> "MCC THEATER";
                        case 4 -> "MCC CORPS";
                        case 5 -> "RAILHEAD";
                        case 6 -> "RAILHEAD THEATER";
                        case 7 -> "RAILHEAD CORPS";
                        case 8 -> "SPOD/SPOE";
                        case 9 -> "SPOD/SPOE THEATER";
                        case 10 -> "SPOD/SPOE CORPS";
                        case 11 -> "APOD/APOE";
                        case 12 -> "APOD/APOE THEATER";
                        case 13 -> "APOD/APOE CORPS";
                        case 14 -> "MISSILE";
                        case 15 -> "MISSILE THEATER";
                        case 16 -> "MISSILE CORPS";
                        case 17 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 21 -> // MAINTENANCE
                    switch (subtype) {
                        case 0 -> "MAINTENANCE, THEATER";
                        case 1 -> "MAINTENANCE, CORPS";
                        case 2 -> "HEAVY";
                        case 3 -> "HEAVY THEATER";
                        case 4 -> "HEAVY CORPS";
                        case 5 -> "RECOVERY";
                        case 6 -> "RECOVERY THEATER";
                        case 7 -> "RECOVERY CORPS";
                        case 8 -> "ORDNANCE";
                        case 9 -> "ORDNANCE THEATER";
                        case 10 -> "ORDNANCE CORPS";
                        case 11 -> "ORDNANCE, MISSILE";
                        case 12 -> "ORDNANCE, MISSILE THEATER";
                        case 13 -> "ORDNANCE, MISSILE CORPS";
                        case 14 -> "ELECTROPTICAL";
                        case 15 -> "ELECTROPTICAL THEATER";
                        case 16 -> "ELECTROPTICAL CORPS";
                        case 17 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    }; // INFO WARFARE UNIT
            // LANDING SUPPORT
            // EXPLOSIVE ORDNANCE DISPOSAL
            case 14, 15, 16, 22 -> // SPECIAL C2 HQ COMPONENT
                    (subtype == 0) ? NO_STATEMENT : String.format(SUBTYPE, "UNDEFINED", subtype);
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Ground Weapon SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param type    VMF  Ground Weapon Type
     * @param subtype VMF  Ground Weapon SubType
     * @return a printable Ground Weapon SubType
     */
    public static String getGroundWeaponSubType(int type, int subtype) {
        return switch (type) {
            case 0 -> // MISSILE LAUNCHER
                    switch (subtype) {
                        case 0 -> "AIR DEFENSE";
                        case 1 -> "THEATER AD";
                        case 2 -> "THEATER TRANSPORTER LAUNCHER AND RADAR (TLAR)";
                        case 3 -> "THEATER TRANSPORTER ERECTOR LAUNCHER AND ERECTOR (TELAR)";
                        case 4 -> "LONG RANGE AD";
                        case 5 -> "LONG RANGE TRANSPORTER LAUNCHER AND RADAR (TLAR)";
                        case 6 -> "LONG RANGE TRANSPORTER ERECTOR LAUNCHER AND RADAR (TELAR)";
                        case 7 -> "INTERMEDIATE RANGE AD";
                        case 8 -> "INTERMEDIATE TRANSPORTER LAUNCHER AND RADAR (TLAR)";
                        case 9 -> "INTERMEDIATE TRANSPORTER ERECTOR LAUNCHER AND RADAR (TELAR)";
                        case 10 -> "SHORT RANGE AD";
                        case 11 -> "SHORT RANGE TRANSPORTER LAUNCHER AND RADAR (TLAR)";
                        case 12 -> "SHORT RANGE TRANSPORTER ERECTOR LAUNCHER AND RADAR (TELAR)";
                        case 13 -> "SURFACE TO SURFACE";
                        case 14 -> "LONG RANGE SS";
                        case 15 -> "INTERMEDIATE RANGE SS";
                        case 16 -> "SHORT RANGE SS";
                        case 17 -> "ANTITANK";
                        case 18 -> "LIGHT, AT";
                        case 19 -> "MEDIUM, AT";
                        case 20 -> "HEAVY, AT";
                        case 21 -> "TRANSPORTER ERECTOR LAUNCHER (TEL)";
                        case 22 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    }; // SINGLE ROCKET LAUNCHER
            // MULTIPLE ROCKET LAUNCHER
            // ANTI TANK ROCKET LAUNCHER
            // GRENADE LAUNCHER
            // MORTAR
            case 1, 2, 3, 5, 6, 10 -> // AIR DEFENSE GUN
                    switch (subtype) {
                        case 0 -> "LIGHT";
                        case 1 -> "MEDIUM";
                        case 2 -> "HEAVY";
                        case 3 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 4 -> // RIFLE/AUTOMATIC WEAPON
                    switch (subtype) {
                        case 0 -> "RIFLE";
                        case 1 -> "LIGHT MACHINE GUN";
                        case 2 -> "HEAVY LIGHT MACHINE GUN";
                        case 3 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 7 -> // HOWITZER
                    switch (subtype) {
                        case 0 -> "LIGHT TOWED HOWITZER";
                        case 1 -> "LIGHT SELF PROPELLED HOWITZER";
                        case 2 -> "MEDIUM TOWED";
                        case 3 -> "MEDIUM SELF PROPELLED";
                        case 4 -> "HEAVY TOWED";
                        case 5 -> "HEAVY SELF PROPELLED";
                        case 6 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 8 -> // ANTI TANK GUN
                    switch (subtype) {
                        case 0 -> "RECOILLESS";
                        case 1 -> "LIGHT";
                        case 2 -> "MEDIUM";
                        case 3 -> "HEAVY";
                        case 4 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 9 -> // DIRECT FIRE GUN
                    switch (subtype) {
                        case 0 -> "LIGHT DIRECT FIRE GUN";
                        case 1 -> "LIGHT SELF PROPELLED";
                        case 2 -> "MEDIUM";
                        case 3 -> "MEDIUM SELF PROPELLED";
                        case 4 -> "HEAVY";
                        case 5 -> "HEAVY SELF PROPELLED";
                        case 6 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Ground Vehicle SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param type    VMF  Ground Vehicle Type
     * @param subtype VMF  Ground Vehicle SubType
     * @return a printable Ground Vehicle SubType
     */
    public static String getGroundVehicleSubType(int type, int subtype) {
        return switch (type) {
            case 0 -> // ARMORED
                    switch (subtype) {
                        case 0 -> "TANK";
                        case 1 -> "LIGHT TANK";
                        case 2 -> "MEDIUM TANK";
                        case 3 -> "HEAVY TANK";
                        case 4 -> "LIGHT TANK RECOVERY VEHICLE";
                        case 5 -> "MEDIUM TANK RECOVERY VEHICLE";
                        case 6 -> "HEAVY TANK RECOVERY VEHICLE";
                        case 7 -> "APC";
                        case 8 -> "APC RECOVERY";
                        case 9 -> "ARMORED INFANTRY";
                        case 10 -> "C2V/ACV";
                        case 11 -> "COMBAT SERVICE SUPPORT VEHICLE";
                        case 12 -> "LIGHT ARMORED VEHICLE";
                        case 13 -> "UNKNOWN";
                        case 14 -> "AMBULANCE, ARMORED";
                        case 15 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 1 -> // UTILITY VEHICLE
                    switch (subtype) {
                        case 0 -> "BUS";
                        case 1 -> "SEMI";
                        case 2 -> "SEMI, LIGHT";
                        case 3 -> "SEMI, MEDIUM";
                        case 4 -> "SEMI, HEAVY";
                        case 5 -> "LIMITED CROSS COUNTRY TRUCK";
                        case 6 -> "CROSS COUNTRY TRUCK";
                        case 7 -> "WATER CRAFT";
                        case 8 -> "TOW TRUCK";
                        case 9 -> "TOW TRUCK, LIGHT";
                        case 10 -> "TOW TRUCK, MEDIUM";
                        case 11 -> "TOW TRUCK, HEAVY";
                        case 12 -> "AMBULANCE";
                        case 13 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 2 -> // ENGINEER VEHICLE
                    switch (subtype) {
                        case 0 -> "BRIDGE";
                        case 1 -> "EARTHMOVER";
                        case 2 -> "CONSTRUCTION VEHICLE";
                        case 3 -> "MINE LAYING VEHICLE";
                        case 4 -> "MINE CLEARING VEHICLE (MCV)";
                        case 5 -> "ARMORED MOUNTED MCV";
                        case 6 -> "TRAILER MOUNTED MCV";
                        case 7 -> "ARMORED CARRIER WITH VOLCANO";
                        case 8 -> "TRUCK MOUNTED WITH VOLCANO";
                        case 9 -> "DOZER";
                        case 10 -> "DOZER, ARMORED";
                        case 11 -> "ARMORED ASSAULT";
                        case 12 -> "ARMORED ENGINEER RECON VEHICLE (AERV)";
                        case 13 -> "BACKHOE";
                        case 14 -> "FERRY TRANSPORTER";
                        case 15 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 3 -> // TRAIN LOCOMOTIVE
                    (subtype == 0) ? NO_STATEMENT : String.format(SUBTYPE, "UNDEFINED", subtype);
            case 4 -> // CIVILIAN VEHICLE
                    switch (subtype) {
                        case 0 -> "AUTOMOBILE";
                        case 1 -> "COMPACT AUTOMOBILE";
                        case 2 -> "MIDSIZE AUTOMOBILE";
                        case 3 -> "SEDAN AUTOMOBILE";
                        case 4 -> "OPEN-BED TRUCK";
                        case 5 -> "PICK-UP OPEN-BED TRUCK";
                        case 6 -> "SMALL OPEN-BED TRUCK";
                        case 7 -> "LARGE OPEN-BED TRUCK";
                        case 8 -> "MULTI-PASSENGER VEHICLE";
                        case 9 -> "VAN, MULTI-PASSENGER VEHICLE";
                        case 10 -> "SMALL BUS, MULTI-PASSENGER";
                        case 11 -> "LARGE BUS, MULTI-PASSENGER";
                        case 12 -> "UTILITY VEHICLE";
                        case 13 -> "SPORT UTILITY VEHICLE (SUV)";
                        case 14 -> "SMALL BOX TRUCK";
                        case 15 -> "LARGE BOX TRUCK";
                        case 16 -> "JEEP TYPE VEHICLE";
                        case 17 -> "SMALL/LIGHT JEEP VEHICLE";
                        case 18 -> "MEDIUM JEEP TYPE VEHICLE";
                        case 19 -> "LARGE/HEAVY JEEP TYPE VEHICLE";
                        case 20 -> "TRACTOR TRAILER TRUCK WITH BOX TRAILER";
                        case 21 -> "SMALL/LIGHT BOX TRAILER, TRACTOR TRAILER TRUCK";
                        case 22 -> "MEDIUM BOX TRAILER, TRACTOR TRAILER TRUCK";
                        case 23 -> "LARGE/HEAVY BOX TRAILER, TRACTOR TRAILER TRUCK";
                        case 24 -> "TRACTOR TRAILER TRUCK WITH FLATBED TRAILER";
                        case 25 -> "SMALL/LIGHT FLATBED TRAILER TRACTOR TRAILER TRUCK";
                        case 26 -> "MEDIUM FLATBED TRAILER, TRACTOR TRAILER TRUCK";
                        case 27 -> "LARGE/HEAVY FLATBED TRAILER TRACTOR TRAILER TRUCK";
                        case 28 -> "PACK ANIMAL(S)";
                        case 29 -> "MISSILE SUPPORT VEHICLE";
                        case 30 -> "MISSILE SUPPORT VEHICLE TRANSLOADER";
                        case 31 -> "MISSILE SUPPORT VEHICLE TRANSPORTER";
                        case 32 -> "MISSILE SUPPORT VEHICLE CRANE/LOADING DEVICE";
                        case 33 -> "MISSILE SUPPORT VEHICLE PROPELLANT TRANSPORTER";
                        case 34 -> "MISSILE SUPPORT VEHICLE WARHEAD TRANSPORTER";
                        case 35 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Ground Sensor SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param subtype VMF  Ground Sensor SubType
     * @return a printable Ground Sensor SubType
     */
    public static String getGroundSensorSubType(int subtype) {
        return switch (subtype) {
            case 0 -> NO_STATEMENT;
            case 1 -> String.format(SUBTYPE, "UNKNOWN", subtype);
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Ground Special Equipment SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param subtype VMF  Ground Special Equipment SubType
     * @return a printable Ground Special Equipment SubType
     */
    public static String getGroundSpecialSubType(int subtype) {
        return switch (subtype) {
            case 0 -> NO_STATEMENT;
            case 1 -> String.format(SUBTYPE, "UNKNOWN", subtype);
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Ground Installation SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param type    VMF  Ground Installation Type
     * @param subtype VMF  Ground Installation SubType
     * @return a printable Ground Installation SubType
     */
    public static String getGroundInstallationSubType(int type, int subtype) {
        return switch (type) {
            case 0 -> // RAW MATERIAL PRODUCTION STORAGE
                    switch (subtype) {
                        case 0 -> "MINE";
                        case 1 -> "PETROLEUM/GAS/OIL";
                        case 2 -> "NBC";
                        case 3 -> "BIOHAZARD";
                        case 4 -> "CHEMICAL";
                        case 5 -> "NUCLEAR";
                        case 6 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 4 -> // SERVICE, RESEARCH, UTILITY
                    switch (subtype) {
                        case 0 -> "TECHNOLOGICAL RESEARCH FACILITY";
                        case 1 -> "TELECOMMUNICATIONS FACILITY";
                        case 2 -> "ELECTRIC POWER FACILITY";
                        case 3 -> "NUCLEAR PLANT";
                        case 4 -> "DAM";
                        case 5 -> "FOSSIL FUEL";
                        case 6 -> "PUBLIC WATER SERVICES";
                        case 7 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 5 -> // MILITARY MATERIAL
                    switch (subtype) {
                        case 0 -> "NUCLEAR ENERGY";
                        case 1 -> "ATOMIC ENERGY REACTOR";
                        case 2 -> "NUCLEAR MATERIAL PRODUCTION";
                        case 3 -> "WEAPONS GRADE";
                        case 4 -> "NUCLEAR MATERIAL STORAGE";
                        case 5 -> "AIRCRAFT PRODUCTION AND ASSEMBLY";
                        case 6 -> "AMMUNITION AND EXPLOSIVES PRODUCTION";
                        case 7 -> "ARMAMENT PRODUCTION";
                        case 8 -> "MILITARY VEHICLE PRODUCTION";
                        case 9 -> "ENGINEERING EQUIPMENT PRODUCTION";
                        case 10 -> "BRIDGE";
                        case 11 -> "CHEMICAL AND BIOLOGICAL WARFARE PRODUCTION";
                        case 12 -> "SHIP CONSTRUCTION";
                        case 13 -> "MISSILE AND SPACE SYSTEM PRODUCTION";
                        case 14 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    }; // PROCESSING FACILITY
            // DECON FACILITY
            // EQUIPMENT MANUFACTURE
            // GOVERNMENT LEADERSHIP
            // MILITARY BASE/FACILITY
            // AIRPORT/AIRBASE
            // SEAPORT/NAVAL BASE
            // TRANSPORT FACILITY
            // MEDICAL FACILITY
            // HOSPITAL
            // TENTED CAMP
            // DISPLACED PERSONS, REFUGEES, EVACUEES CAMP
            // TRAINING CAMP
            case 1, 2, 3, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 -> // WAREHOUSE/STORAGE FACILITY
                    switch (subtype) {
                        case 0 -> NO_STATEMENT;
                        case 1 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Sea Surface SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param type    VMF  Sea Surface Type
     * @param subtype VMF  Sea Surface SubType
     * @return a printable Sea Surface SubType
     */
    public static String getSeaSurfaceSubType(int type, int subtype) {
        return switch (type) {
            case 1 -> // COMBATANT, LINE
                    switch (subtype) {
                        case 0 -> "CARRIER";
                        case 1 -> "BATTLESHIP";
                        case 2 -> "CRUISER";
                        case 3 -> "DESTROYER";
                        case 4 -> "FRIGATE/CORVETTE";
                        case 5 -> "LITTORAL COMBATANT";
                        case 6 -> "ASW MISSION PACKAGE";
                        case 7 -> "MINE WARFARE MISSION PACKAGE";
                        case 8 -> "SURFACE WARFARE MISSION PACKAGE";
                        case 9 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 2 -> // COMBATANT, AMPHIBIOUS WARFARE SHIP
                    switch (subtype) {
                        case 0 -> "ASSAULT VESSEL";
                        case 1 -> "LANDING SHIP";
                        case 2 -> "LANDING SHIP, MEDIUM";
                        case 3 -> "LANDING SHIP TANK";
                        case 4 -> "LANDING CRAFT";
                        case 5 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 3 -> // COMBATANT, MINE WARFARE VESSEL
                    switch (subtype) {
                        case 0 -> "MINELAYER";
                        case 1 -> "MINESWEEPER";
                        case 2 -> "MINEHUNTER";
                        case 3 -> "MCM SUPPORT";
                        case 4 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 4 -> // COMBATANT, PATROL
                    switch (subtype) {
                        case 0 -> "ANTI SUBMARINE WARFARE";
                        case 1 -> "ANTI SURFACE WARFARE";
                        case 2 -> "ANTI-SHIP MISSILE PATROL CRAFT";
                        case 3 -> "TORPEDO PATROL CRAFT";
                        case 4 -> "GUN PATROL CRAFT";
                        case 5 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    }; // COMBATANT
            case 0, 5 -> // COMBATANT, HOVERCRAFT
                    switch (subtype) {
                        case 0 -> NO_STATEMENT;
                        case 1 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 6 -> // SURFACE DECOY
                    switch (subtype) {
                        case 0 -> "UNMANNED SURFACE VEHICLE";
                        case 1 -> "MINE COUNTERMEASURES SURFACE DRONE";
                        case 2 -> "ANTISUBMARINE WARFARE SURFACE DRONE";
                        case 3 -> "ANTISURFACE WARFARE SURFACE DRONE";
                        case 4 -> "REMOTE MULTI-MISSION VEHICLE";
                        case 5 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 7 -> // NAVY GROUP
                    switch (subtype) {
                        case 0 -> "NAVY TASK FORCE";
                        case 1 -> "NAVY TASK GROUP";
                        case 2 -> "NAVY TASK UNIT";
                        case 3 -> "CONVOY";
                        case 4 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 8 -> // NON COMBATANT
                    switch (subtype) {
                        case 0 -> "UNDERWAY REPLENISHMENT";
                        case 1 -> "FLEET SUPPORT";
                        case 2 -> "INTELLIGENCE";
                        case 3 -> "SERVICE & SUPPORT HARBOR";
                        case 4 -> "HOSPITAL SHIP";
                        case 5 -> "HOVERCRAFT";
                        case 6 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            case 9 -> // NON MILITARY
                    switch (subtype) {
                        case 0 -> "MERCHANT";
                        case 1 -> "CARGO";
                        case 2 -> "ROLL ON-ROLL OFF";
                        case 3 -> "OILER/TANKER";
                        case 4 -> "TUG";
                        case 5 -> "FERRY";
                        case 6 -> "PASSENGER";
                        case 7 -> "HAZARDOUS MATERIALS (HAZMAT)";
                        case 8 -> "TOWING VESSEL";
                        case 9 -> "FISHING";
                        case 10 -> "DRIFTER";
                        case 11 -> "DREDGE";
                        case 12 -> "TRAWLER";
                        case 13 -> "LEISURE CRAFT";
                        case 14 -> "LAW ENFORCEMENT VESSEL";
                        case 15 -> "HOVERCRAFT";
                        case 16 -> "FAST RECREATIONAL CRAFT";
                        case 17 -> "RIGID-HULL INFLATABLE BOAT";
                        case 18 -> "SPEED BOAT";
                        case 19 -> "PERSONAL WATERCRAFT";
                        case 20 -> String.format(SUBTYPE, "UNKNOWN", subtype);
                        default -> String.format(SUBTYPE, "UNDEFINED", subtype);
                    };
            default -> String.format(SUBTYPE, "UNDEFINED", subtype);
        };
    }

    /**
     * Return a printable Sea Subsurface SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param type    VMF  Sea Subsurface Type
     * @param subtype VMF  Sea Subsurface SubType
     * @return a printable Sea Subsurface SubType
     */
    public static String getSeaSubsurfaceSubType(int type, int subtype) {
        return "NOT PROCESSED"; // TODO: Reserved for future processing
    }

    /**
     * Return a printable SOF SubType for a given Type (DFI/DUI: 4173/016)
     *
     * @param type    VMF  SOF Type
     * @param subtype VMF  SOF SubType
     * @return a printable SOF SubType
     */
    public static String getSofSubType(int type, int subtype) {
        return "NOT PROCESSED"; // TODO: Reserved for future processing
    }

    /**
     * Return a printable Dimension (DFI/DUI: 4173/014)
     *
     * @param dim VMF Dimension
     * @return a printable Dimension
     */
    public static String getDimension(int dim) {
        return switch (dim) {
            case 0 -> "SPACE";
            case 1 -> "AIR";
            case 2 -> "GROUND UNITS";
            case 3 -> "GROUND WEAPONS";
            case 4 -> "GROUND VEHICLES";
            case 5 -> "GROUND SENSORS";
            case 6 -> "GROUND SPECIAL EQUIPMENT";
            case 7 -> "GROUND INSTALLATIONS";
            case 8 -> "SEA SURFACE";
            case 9 -> "SEA SUBSURFACE";
            case 10 -> "SOF ";
            case 11 -> "TACTICAL GRAPHICS, TASKS";
            case 12 -> "TACTICAL GRAPHICS, C2 AND GENERAL MANEUVER";
            case 13 -> "TACTICAL GRAPHICS, MOBILITY/SURVIVABILITY";
            case 14 -> "TACTICAL GRAPHICS, FIRE SUPPORT";
            case 15 -> "TACTICAL GRAPHICS, COMBAT SERVICE SUPPORT(CSS)";
            case 16 -> "TACTICAL GRAPHICS, OTHER";
            case 17 -> "INTELLIGENCE, SPACE";
            case 18 -> "INTELLIGENCE, AIR";
            case 19 -> "INTELLIGENCE, GROUND";
            case 20 -> "INTELLIGENCE, SEA SURFACE";
            case 21 -> "INTELLIGENCE, SEA SUBSURFACE";
            case 22 -> "STABILITY OPERATIONS, INDIVIDUALS";
            case 23 -> "STABILITY OPERATIONS, VIOLENT ACTIVITIES";
            case 24 -> "STABILITY OPERATIONS, LOCATIONS";
            case 25 -> "STABILITY OPERATIONS, OPERATIONS";
            case 26 -> "STABILITY OPERATIONS, ITEMS";
            case 27 -> "STABILITY OPERATIONS, NON-MILITARY GROUP OR ORGANIZATION";
            case 28 -> "STABILITY OPERATIONS, RAPE";
            case 29 -> "EMERGENCY MANAGEMENT, INCIDENT";
            case 30 -> "EMERGENCY MANAGEMENT, NATURAL EVENTS";
            case 31 -> "EMERGENCY MANAGEMENT, OPERATIONS";
            case 32 -> "EMERGENCY MANAGEMENT, INFRASTRUCTURE";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable Space Type (DFI/DUI: 4173/015)
     *
     * @param type VMF Space Type
     * @return a printable Space Type
     */
    public static String getSpaceType(int type) {
        return switch (type) {
            case 0 -> "SATELLITE";
            case 1 -> "CREWED SPACE VEHICLE";
            case 2 -> "SPACE STATION";
            case 3 -> "SPACE LAUNCH VEHICLE";
            case 4 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable Dim1Type (DFI/DUI: 4173/015)
     *
     * @param type VMF Dim1Type
     * @return a printable Dim1Type
     */
    public static String getAirType(int type) {
        return switch (type) {
            case 0 -> "MILITARY FIXED WING";
            case 1 -> "MILITARY ROTARY WING";
            case 2 -> "MILITARY LIGHTER THAN AIR";
            case 3 -> "WEAPON";
            case 4 -> "CIVIL AIRCRAFT";
            case 5 -> "VIP AIRCRAFT";
            case 6 -> "VIP AIRCRAFT ESCORT";
            case 7 -> "DECOY";
            case 8 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }


    /**
     * Return a printable Ground Type (DFI/DUI: 4173/015)
     *
     * @param type VMF  Ground Type
     * @return a printable  Ground Type
     */
    public static String getGroundUnitType(int type) {
        return switch (type) {
            case 0 -> "AIR DEFENSE";
            case 1 -> "ARMOR";
            case 2 -> "ANTI ARMOR";
            case 3 -> "AVIATION";
            case 4 -> "INFANTRY";
            case 5 -> "ENGINEER";
            case 6 -> "FIELD ARTILLERY";
            case 7 -> "RECONNAISSANCE";
            case 8 -> "MISSILE (SURF-SURF)";
            case 9 -> "INTERNAL SECURITY FORCES";
            case 10 -> "NBC";
            case 11 -> "MILITARY INTELLIGENCE";
            case 12 -> "LAW ENFORCEMENT UNIT";
            case 13 -> "SIGNAL UNIT";
            case 14 -> "INFO WARFARE UNIT";
            case 15 -> "LANDING SUPPORT";
            case 16 -> "EXPLOSIVE ORDNANCE DISPOSAL";
            case 17 -> "ADMINISTRATIVE";
            case 18 -> "MEDICAL";
            case 19 -> "SUPPLY";
            case 20 -> "TRANSPORTATION";
            case 21 -> "MAINTENANCE";
            case 22 -> "SPECIAL C2 HQ COMPONENT";
            case 23 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable Ground Weapon Type (DFI/DUI: 4173/015)
     *
     * @param type VMF  Ground Weapon Type
     * @return a printable  Ground Weapon Type
     */
    public static String getGroundWeaponType(int type) {
        return switch (type) {
            case 0 -> "MISSILE LAUNCHER";
            case 1 -> "SINGLE ROCKET LAUNCHER";
            case 2 -> "MULTIPLE ROCKET LAUNCHER";
            case 3 -> "ANTI TANK ROCKET LAUNCHER";
            case 4 -> "RIFLE/AUTOMATIC WEAPON";
            case 5 -> "GRENADE LAUNCHER";
            case 6 -> "MORTAR";
            case 7 -> "HOWITZER";
            case 8 -> "ANTI TANK GUN";
            case 9 -> "DIRECT FIRE GUN";
            case 10 -> "AIR DEFENSE GUN";
            case 11 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable Ground Vehicle Type (DFI/DUI: 4173/015)
     *
     * @param type VMF  Ground Vehicle Type
     * @return a printable  Ground Vehicle Type
     */
    public static String getGroundVehicleType(int type) {
        return switch (type) {
            case 0 -> "ARMORED";
            case 1 -> "UTILITY VEHICLE";
            case 2 -> "ENGINEER VEHICLE";
            case 3 -> "TRAIN LOCOMOTIVE";
            case 4 -> "CIVILIAN VEHICLE";
            case 5 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable Ground Sensor Type
     *
     * @param type VMF Ground Sensor Type
     * @return a printable Ground Sensor Type
     */
    public static String getGroundSensorType(int type) {
        return switch (type) {
            case 0 -> "RADAR";
            case 1 -> "EMPLACED SENSOR";
            case 2 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable Ground Special Type (DFI/DUI: 4173/015)
     *
     * @param type VMF  Ground Special Type
     * @return a printable  Ground Special Type
     */
    public static String getGroundSpecialType(int type) {
        return switch (type) {
            case 0 -> "LASER";
            case 1 -> "NBC EQUIPMENT";
            case 2 -> "FLAME THROWER";
            case 3 -> "LAND MINES";
            case 4 -> "CLAYMORE";
            case 5 -> "LESS THAN LETHAL";
            case 6 -> "IMPROVISED EXPLOSIVE DEVICE (IED)";
            case 7 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable Ground Installation Type (DFI/DUI: 4173/015)
     *
     * @param type VMF  Ground Installation Type
     * @return a printable  Ground Installation Type
     */
    public static String getGroundInstallationType(int type) {
        return switch (type) {
            case 0 -> "RAW MATERIAL PRODUCTION STORAGE";
            case 1 -> "PROCESSING FACILITY";
            case 2 -> "DECON FACILITY";
            case 3 -> "EQUIPMENT MANUFACTURE";
            case 4 -> "SERVICE, RESEARCH, UTILITY";
            case 5 -> "MILITARY MATERIAL";
            case 6 -> "GOVERNMENT LEADERSHIP";
            case 7 -> "MILITARY BASE/FACILITY";
            case 8 -> "AIRPORT/AIRBASE";
            case 9 -> "SEAPORT/NAVAL BASE";
            case 10 -> "TRANSPORT FACILITY";
            case 11 -> "MEDICAL FACILITY";
            case 12 -> "HOSPITAL";
            case 13 -> "TENTED CAMP";
            case 14 -> "DISPLACED PERSONS, REFUGEES, EVACUEES CAMP";
            case 15 -> "TRAINING CAMP";
            case 16 -> "WAREHOUSE/STORAGE FACILITY";
            case 17 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable Sea Surface Type (DFI/DUI: 4173/015)
     *
     * @param type VMF  Sea Surface Type
     * @return a printable Sea Surface Type
     */
    public static String getSeaSurfaceType(int type) {
        return switch (type) {
            case 0 -> "COMBATANT";
            case 1 -> "COMBATANT, LINE";
            case 2 -> "COMBATANT, AMPHIBIOUS WARFARE SHIP";
            case 3 -> "COMBATANT, MINE WARFARE VESSEL";
            case 4 -> "COMBATANT, PATROL";
            case 5 -> "COMBATANT, HOVERCRAFT";
            case 6 -> "SURFACE DECOY";
            case 7 -> "NAVY GROUP";
            case 8 -> "NON COMBATANT";
            case 9 -> "NON MILITARY";
            case 10 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable Sea Subsurface Type (DFI/DUI: 4173/015)
     *
     * @param type VMF  Sea Subsurface Type
     * @return a printable Sea Subsurface Type
     */
    public static String getSeaSubsurfaceType(int type) {
        return switch (type) {
            case 0 -> "SUBMARINE";
            case 1 -> "UNDERWATER WEAPON";
            case 2 -> "NON-SUBMARINE";
            case 3 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable SOF Type (DFI/DUI: 4173/015)
     *
     * @param type VMF  SOF Type
     * @return a printable SOF Type
     */
    public static String getSofType(int type) {
        return switch (type) {
            case 0 -> "SOF UNIT AVIATION";
            case 1 -> "SOF UNIT NAVAL";
            case 2 -> "SOF UNIT GROUND";
            case 3 -> "SOF UNIT SUPPORT";
            case 4 -> String.format(TYPE, "UNKNOWN", type);
            default -> String.format(TYPE, "UNDEFINED", type);
        };
    }

    /**
     * Return a printable MunitionsType (DFI/DUI 4005/001)
     *
     * @param munitionType VMF MunitionsType
     * @return a printable MunitionsType
     */
    public static String getMunitionsType(int munitionType) {
        return switch (munitionType + 1) {
            case 1 -> "ANTIAIRCRAFT, COMMON";
            case 2 -> "COMMON";
            case 3 -> "HIGH EXPLOSIVE";
            case 4 -> "HIGH CAPACITY";
            case 5 -> "ICM";
            case 6 -> "ARMOR PIERCING";
            case 7 -> "ANTIPERSONNEL";
            case 8 -> "SMOKE GREEN";
            case 9 -> "SMOKE RED";
            case 10 -> "SMOKE YELLOW";
            case 11 -> "SMOKE VIOLET";
            case 12 -> "SMOKE HEXACHLORETHANE";
            case 13 -> "ILLUMINATION";
            case 14 -> "RIOT CONTROL AGENT CS";
            case 15 -> "GAS GB";
            case 16 -> "GAS H OR HD";
            case 17 -> "WHITE PHOSPHORUS";
            case 18 -> "MIX HE AND WHITE PHOSPHORUS";
            case 19 -> "HE SPOTTING";
            case 20 -> "HE ANTITANK";
            case 21 -> "HE ROCKET-ASSISTED PROJECTILE, ROCKET ON";
            case 22 -> "ICM DUAL PURPOSE";
            case 23 -> "ANTIPERSONNEL MINE, LONG DELAY";
            case 24 -> "ANTIPERSONNEL MINE, SHORT DELAY";
            case 25 -> "ANTIMATERIAL MINE, LONG DELAY";
            case 26 -> "ANTIMATERIAL MINE, SHORT DELAY";
            case 27 -> "COPPERHEAD";
            case 28 -> "MLRS ANTITANK MINE";
            case 29 -> "MLRS TERMINAL HOMING MUNITIONS";
            case 30 -> "WP, BASE EJECTED FELT WEDGES";
            case 31 -> "GAS GB, BINARY";
            case 32 -> "HE ROCKET ASSIST, ROCKET-OFF";
            case 33 -> "M718 - AML - 155MM";
            case 34 -> "M741 - AMS - 155MM";
            case 35 -> "M692 - APL - 155MM";
            case 36 -> "M731 - APS - 155MM";
            case 37 -> "M712 - CPH - 155MM";
            case 38 -> "M718A1 - AML - 155MM";
            case 39 -> "M741A1 - AMS - 155MM";
            case 40 -> "M107 - HEA - 155MM";
            case 41 -> "L15A2B1 - HEK - 155MM";
            case 42 ->
                    "HEA - 60MM (HE M720 W/MO M734), 81MM (HE M821 W/MO M734), 107MM, 120MM (HE M934A1 W/MO M734A1), 5IN54, 5IN62";
            case 43 -> "HEB - 60MM (HE M768 W/QDL M783), 81MM (HE M889 W/Q M935), 120MM (HE M933 W/Q M745)";
            case 44 -> "M444 - HEC - 105MM";
            case 45 -> "M449A1 - HEE - 155MM";
            case 46 -> "HEF - 16IN50";
            case 47 -> "M760 - HEG - 105MM";
            case 48 -> "M314A3 - ILA - 105MM";
            case 49 -> "L15A1 - HEK - 155MM";
            case 50 -> "M795 - HEL - 155MM";
            case 51 -> "M864 - HEM - 155MM";
            case 52 -> "M548 - HEO, HER - 105MM";
            case 53 -> "M314A2E1 - ILA - 105MM";
            case 54 -> "M913 - HRO, HRR - 105MM";
            case 55 -> "M60/WP - SMA - 105MM";
            case 56 -> "ILA - 60MM (M83A3 W/M65A1), 81MM (M301A3 W/M84A1), 107MM, 120MM (M91 W/M776), 5IN54, 5IN62";
            case 57 -> "M898 - SAD - 155MM";
            case 58 -> "SMA - 60MM (M302A1 W/M527), 81MM (M375 W/M524), 107MM, 120MM (M929, W/MO M734A1), 5IN54, 5IN62";
            case 59 -> "M84A1 - SMB - 105MM";
            case 60 -> "M825 - SMC - 155MM";
            case 61 -> "M825A1 - SMD - 155MM";
            case 62 -> "JED";
            case 63 -> "JEE";
            case 64 -> "JTA";
            case 65 -> "JTB";
            case 66 -> "JTE";
            case 67 -> "JMT";
            case 68 -> "JTC";
            case 69 -> "JTW";
            case 70 -> "JEG";
            case 71 -> "JEH";
            case 72 -> "JEJ";
            case 73 -> "JEK";
            case 74 -> "JEL";
            case 75 -> "JEM";
            case 76 -> "JEN";
            case 77 -> "JTH";
            case 78 -> "JTJ";
            case 79 -> "JTK";
            case 80 -> "JTL";
            case 81 -> "JTD";
            case 82 -> "JTF";
            case 83 -> "JTG";
            case 84 -> "JTM";
            case 85 -> "JEP";
            case 86 -> "JEQ";
            case 87 -> "JER";
            case 88 -> "JML";
            case 89 -> "JMU";
            case 90 -> "JNB";
            case 91 -> "JSA";
            case 92 -> "NAV";
            case 93 -> "CAS";
            case 94 -> "CSA 107MM";
            case 95 -> "HED - NGF";
            case 96 -> "CSA 81MM";
            case 97 -> "SALGP";
            case 98 -> "SMOKE ORANGE";
            case 99 -> "HEAT MAIN GUN ROUND";
            case 100 -> "SABOT MAIN GUN ROUND";
            case 101 -> "MPAT MAIN GUN ROUND";
            case 102 -> "DISUSED";
            case 103 -> "MACHINE GUN ROUND (0.50 CAL)";
            case 104 -> "MACHINE GUN ROUND (7.62MM)";
            case 105 -> "RIFLE GUN ROUND (5.56MM)";
            case 106 -> "ANTI-TANK MINE";
            case 107 -> "HAND GRENADE";
            case 108 -> "FASCAM";
            case 109 -> "TOW MISSILES";
            case 110 -> "HELLFIRE SEMI-ACTIVE LASER (SAL)";
            case 111 -> "MLRS HE";
            case 112 -> "RIFLE GUN RD (7.62MM)";
            case 113 -> "2.75 ROCKETS";
            case 114 -> "25MM";
            case 115 -> "STINGER MISSILE";
            case 116 -> "HES 60MM (M888 W/Q M935), 81MM (M374A3 W/Q M567), 120MM (M933 W/MO M734)";
            case 117 -> "HET 60MM (M49A5 W/Q M935), 81MM (M374A2 W/Q M567), 120MM (M934 W/MO M734)";
            case 118 -> "SME 60MM (M722 W/M745), 81MM (M375A2 W/M524), 120MM (XM929 W/M745)";
            case 119 -> "ILW - 60MM (M721 W/M776), 81MM (M853A1 W/M772), 120MM (M930W/M776)";
            case 120 -> "LOSAT";
            case 121 -> "M915 - HXF - 105MM";
            case 122 -> "ILR - 60MM (M767 W/M776), 81MM (M816 W/M772), 120MM (M983 W/M776)";
            case 123 -> "M483A1 - HEF - 155MM";
            case 124 -> "M549 - HER - 155MM";
            case 125 -> "M927 - HTO, HTR - 105MM";
            case 126 -> "M60A2/WP - SMA - 105MM";
            case 127 -> "NR109 - ILB - 155MM";
            case 128 -> "XM395 - 120MM";
            case 129 -> "DPM - 60MM, 81MM, 120MM-(XM984 W/UNK)";
            case 130 -> "TAS - 60MM, 81MM, 120MM (M1027 W/TI M776)";
            case 131 -> "SRM - 60MM (M766 W/Q M779), 81MM (M880 W/Q M775), 120MM";
            case 132 -> "FRM - 60MM (M769 W/Q M775), 81MM (M879 W/M751), 120MM (M931 W/Q M781)";
            case 133 -> "SMR - 60MM, 81MM (M819 W/TI M772), 120MM";
            case 134 -> "HE - PD";
            case 135 -> "HE - DELAY";
            case 136 -> "HE - TIME";
            case 137 -> "HE - VT";
            case 138 -> "HE - CVT";
            case 139 -> "HE - MFF";
            case 140 -> "HF - TIME";
            case 141 -> "HF - CVT";
            case 142 -> "HF - MFF";
            case 143 -> "ILLUM1 - TIME";
            case 144 -> "ILLUM2 - TIME";
            case 145 -> "ILLUM3 - TIME";
            case 146 -> "WP - PD";
            case 147 -> "WP - TIME";
            case 148 -> "ICM - TIME";
            case 149 -> "ERGM - TIME";
            case 150 -> "MACHINE GUN RD (30MM)";
            case 151 -> "5 INCH ROCKET";
            case 152 -> "M549A1 - HER - 155MM";
            case 153 -> "M485A1 - ILA - 155MM";
            case 154 -> "MACHINE GUN RD (20MM)";
            case 155 -> "HELLFIRE RADIO FREQUENCY";
            case 156 -> "M485E2 - ILC - 155MM";
            case 157 -> "M110/WP - SMA - 155MM";
            case 158 -> "M110A1/WP - SMA - 155MM";
            case 159 -> "M110A2/WP - SMA - 155MM";
            case 160 -> "XM982 - XCL - 155MM";
            case 161 -> "M107BG - HEA - 155MM";
            case 162 -> "NM28 - HEA - 155MM";
            case 163 -> "OE56 - HEA - 155MM";
            case 164 -> "OE69 - HEA - 155MM";
            case 165 -> "DM21 - HEA - 155MM";
            case 166 -> "M107B2 - HEA - 155MM";
            case 167 -> "M107C1 - HEA - 155MM";
            case 168 -> "M485A2 - ILA - 155MM";
            case 169 -> "M110E2/WP - SMA - 155MM";
            case 170 -> "M110C1/WP - SMA - 155MM";
            case 171 -> "DM45A1 - SMB - 155MM";
            case 172 -> "M116C1 - SMB - 155MM";
            case 173 -> "M116C2 - SMB - 155MM";
            case 174 -> "M116A1 - SMB - 155MM";
            case 175 -> "M1 - HEA - 105MM";
            case 176 -> "M916 - HEF - 105MM";
            case 177 -> "L15A2 - HEK - 155MM";
            case 178 -> "M107BG - HEB - 155MM";
            case 179 -> "NM28 - HEB - 155MM";
            case 180 -> "OE56 - HEB - 155MM";
            case 181 -> "OE69 - HEB - 155MM";
            case 182 -> "DM21 - HEB - 155MM";
            case 183 -> "M107B2 - HEB - 155MM";
            case 184 -> "M107C1 - HEB - 155MM";
            case 185 -> "M107 - HEB - 155MM";
            case 186 -> "M1 - HEB - 105MM";
            case 187 -> "M1076 - 155MM";
            case 188 -> "M1065 - 155MM";
            case 189 -> "DISUSED";
            case 190 -> "M1077 - 155MM";
            case 191 -> "M1065A1 - 155MM";
            case 192 -> "DISUSED";
            case 193 -> "M116A1/HC - SMB - 155MM";
            case 194 -> "M60A1/WP - SMA - 105MM";
            case 195 -> "TOW 2A";
            case 196 -> "TOW 2B";
            case 197 -> "TOW 2B AERO";
            case 198 -> "TOW 2B CAPS";
            case 199 -> "TOW BUNKER BUSTER";
            case 200 -> "TOW PRACTICE/TRAINING";
            case 201 -> "DRAGON";
            case 202 -> "JAVELIN";
            case 203 -> "JAVELIN P3I";
            case 204 -> "JOINT COMMON MISSILE";
            case 205 -> "DM702 - SAD - 155MM";
            case 206 -> "DM702A1 - SAD - 155MM";
            case 207 -> "155BONUS - SAD - 155MM";
            case 208 -> "O155ACF1BONUS - SAD - 155MM";
            case 209 -> "M864A1 - HEM - 155MM";
            case 210 -> "M1028 CANISTER ROUND - 120MM";
            case 211 -> "M908 - OR ROUND - 120MM";
            case 212 -> "HE - 25MM";
            case 213 -> "AP - 25MM";
            case 214 -> "DU - 25MM";
            case 215 -> "HEP M393A2/M393A3 - 105MM";
            case 216 -> "HEAT M456A2 - 105MM";
            case 217 -> "SABOT M900 - 105MM";
            case 218 -> "CANISTER XM1040 - 105MM";
            case 219 -> "GRENADE M430/M430A1 - 40MM";
            case 220 -> "GRENADE M383 - 40MM";
            case 221 -> "GRENADE M922A1 - 40MM";
            case 222 -> "GRENADE M76IR";
            case 223 -> "GRENADE M90";
            case 224 -> "GRENADE UK-L8A1";
            case 225 -> "GRENADE UK-L8A3 RP";
            case 226 -> "60MM (M49A2E2 W/Q M935), 81MM (M374 W/Q M524), 120MM (M57 W/Q M935)";
            case 227 -> "60MM (M1046 W/TI M783), 81MM, 120MM";
            case 228 -> "60MM (M1061 W/MO 734A1), 81MM, 120MM";
            case 229 -> "60MM (M50A2E1 W/Q M935), 81MM (M879E1 W/TI M772), 120MM";
            case 230 -> "60MM (M49A2E2 W/Q M525), 81MM (M374 W/Q M567), 120MM (M934A1E1 W/MO M734A1)";
            case 231 -> "60MM (M49A4 W/Q M935), 81MM (M374 W/VT M532), 120MM";
            case 232 -> "60MM (M49A4 W/Q M525), 81MM (M374A2 W/Q M524), 120MM";
            case 233 -> "60MM (M49A4E1 W/Q M935), 81MM (M374A2 W/VT M532), 120MM";
            case 234 -> "60MM (M720A1 W/MO M734A1), 81MM, 120MM";
            case 235 -> "60MM (M720E1 W/MO M734A1), 81MM, 120MM";
            case 236 -> "60MM, 81MM (M819E1 W/TI M772), 120MM";
            case 237 -> "60MM, 81MM (M374A3 W/Q M524), 120MM";
            case 238 -> "60MM, 81MM (M374A3 W/VT M532), 120MM";
            case 239 -> "60MM, 81MM (M821A1 W/MO M734), 120MM";
            case 240 -> "60MM, 81MM (M821A2 W/MO M734A1), 120MM";
            case 241 -> "60MM, 81MM (M889A1 W/Q M935), 120MM";
            case 242 -> "60MM (M302A2 W/M936), 81MM, 120MM (M1039 W/M776)";
            case 243 -> "60MM (M302E1 W/M527), 81MM, 120MM";
            case 244 -> "60MM (M722A1 W/QDL M783), 81MM (M375A2 W/M567), 120MM";
            case 245 -> "60MM, 81MM (M375A3 W/M524), 120MM";
            case 246 -> "60MM, 81MM (M375A3 W/VT M532), 120MM";
            case 247 -> "60MM, 81MM (M375A3 W/M567), 120MM";
            case 248 -> "60MM (M50A2E1 W/Q M525), 81MM, 120MM";
            case 249 -> "60MM (M50A3 W/Q M935), 81MM, 120MM";
            case 250 -> "60MM (M50A3 W/Q M525), 81MM, 120MM";
            case 251 -> "M982 - XCL - 155MM";
            case 252 -> "M982A1 - XCL - 155MM";
            case 253 -> "LAM";
            case 254 -> "PAM";
            case 255 -> "M1064 - 105MM";
            case 256 -> "120MM (M934A2 W/MO M734A1)";
            case 257 -> "M1101 W/MO M767A1";
            case 258 -> "M1101 W/MO M782";
            case 259 -> "M1107 W/MO M767A1";
            case 260 -> "M1107 W/MO M782";
            case 261 -> "M1105 W/M762A1";
            case 262 -> "M1103 W/MO M767A1";
            case 263 -> "M1103 W/MO M782";
            case 264 -> "XM982A1 - XCL - 155MM";
            case 265 -> "M720A2 W/M734A1 - 60MM";
            case 266 -> "M721A1 W/M784 - 60MM";
            case 267 -> "M767A1 W/M784 - 60MM";
            case 268 -> "M768A1 W/M783 - 60MM";
            case 269 -> "M888A1 W/M783 - 60MM";
            case 270 -> "M816A1 W/M785 - 81MM";
            case 271 -> "M819A1 W/M785 - 81MM";
            case 272 -> "M821A3 W/M734A1 - 81MM";
            case 273 -> "M853A2 W/M785 - 81MM";
            case 274 -> "M879A1 W/M787 - 81MM";
            case 275 -> "M889A2 W/M783 - 81MM";
            case 276 -> "M889A3 W/M783 - 81MM";
            case 277 -> "M395 - 120MM";
            case 278 -> "M395A1 - 120MM";
            case 279 -> "M395A2 - 120MM";
            case 280 -> "M929A1 W/M734A1 - 120MM";
            case 281 -> "M930A1 W/M776 - 120MM";
            case 282 -> "M930A2 W/M776 - 120MM";
            case 283 -> "M932 W/M781 - 120MM";
            case 284 -> "M933A1 W/M783 - 120MM";
            case 285 -> "M933A2 W/M783 - 120MM";
            case 286 -> "M983A1 W/M784 - 120MM";
            case 287 -> "M1102 W/MO M782";
            case 288 -> "M1104 W/MO M782";
            case 289 -> "M1106 W/TI M762A1";
            case 290 -> "M1108 W/MO M782";
            case 291 -> "M1109 W/O FUZE";
            case 292 -> "M1110 W/TI M767A1";
            case 293 -> "M1111 W/MO M782";
            case 294 -> "M1066 - 155MM";
            case 295 -> "M1130 - 105MM";
            case 296 -> "M1131 - 105MM";
            case 297 -> "M1130A1 - 105MM";
            case 298 -> "M1131A1 - 105MM";
            case 299 -> "XM1120 - 155MM";
            case 300 -> "XM1121 - 155MM";
            case 301 -> "XM1122 - 155MM";
            case 302 -> "XM1123 - 155MM";
            case 303 -> "XM1124 - 155MM";
            case 304 -> "XM1125 - 155MM";
            case 305 -> "XM1126 - 155MM";
            case 306 -> "XM1127 - 155MM";
            case 307 -> "XM1128 - 155MM";
            case 308 -> "XM1129 - 155MM";
            case 309 -> "XM1132 - 105MM";
            case 310 -> "XM1133 - 105MM";
            case 311 -> "XM1134 - 105MM";
            case 312 -> "XM1135 - 105MM";
            case 313 -> "M1136 - 105MM";
            case 314 -> "M913A1 - 105MM";
            case 315 -> "XM1138 - 105MM";
            case 316 -> "M1A1 - 105MM";
            case 317 -> "APS BIT MUNITION";
            case 318 -> "APS SRCM MUNITION";
            case 319 -> "APS LRCM MUNITION";
            case 320 -> "APS ALTERNATIVE COUNTERMEASURES MUNITION";
            case 321 -> "M720A3 W/MO M734A1 - 60MM";
            case 322 -> "M720A4 W/MO M734A1 - 60MM";
            case 323 -> "M821A4 W/MO M734A1 - 81MM";
            case 324 -> "M821A5 W/MO M734A1 - 81MM";
            case 325 -> "M768A2 W/QDL M783 - 60MM";
            case 326 -> "M768A3 W/QDL M783 - 60MM";
            case 327 -> "M889A4 W/QDL M783 - 81MM";
            case 328 -> "M889A5 W/QDL M783 - 81MM";
            case 329 -> "M933A3 W/QDL M783 - 120MM";
            case 330 -> "M933A4 W/QDL M783 - 120MM";
            case 331 -> "M888A2 W/QDL M783 - 60MM";
            case 332 -> "M888A3 W/QDL M783 - 60MM";
            case 333 -> "M721A2 W/TI M784 - 60MM";
            case 334 -> "M721A3 W/TI M784 - 60MM";
            case 335 -> "M853A3 W/TI M785 - 81MM";
            case 336 -> "M853A4 W/TI M785 - 81MM";
            case 337 -> "M930A3 W/TI M784 - 120MM";
            case 338 -> "M930A4 W/TI M784 - 120MM";
            case 339 -> "M767A2 W/TI M784 - 60MM";
            case 340 -> "M767A3 W/TI M784 - 60MM";
            case 341 -> "M816A2 W/TI M785 - 81MM";
            case 342 -> "M816A3 W/TI M785 - 81MM";
            case 343 -> "M984A1 W/UNK - 120MM";
            case 344 -> "M984A2 W/UNK - 120MM";
            case 345 -> "M769A1 W/Q M775 - 60MM";
            case 346 -> "M769A2 W/Q M775 - 60MM";
            case 347 -> "M879A2 W/Q M787 - 81MM";
            case 348 -> "M879A3 W/Q M787 - 81MM";
            case 349 -> "M931A1 W/Q M781 - 120MM";
            case 350 -> "M931A2 W/Q M781 - 120MM";
            case 351 -> "M1046A1 W/QDL M783 - 60MM";
            case 352 -> "M1046A2 W/QDL M783 - 60MM";
            case 353 -> "M1061A1 W/MO 734A1 - 60MM";
            case 354 -> "M1061A2 W/MO 734A1 - 60MM";
            case 355 -> "M722A2 W/QDL M783 - 60MM";
            case 356 -> "M722A3 W/QDL M783 - 60MM";
            case 357 -> "XM1113 - 105MM";
            case 358 -> "XM1139 - 105MM";
            case 359 -> "M1064A1 - 105MM";
            case 360 -> "M314A1 - 105MM";
            case 361 -> "XM1143 - 105MM";
            case 362 -> "XM1144 - 105MM";
            case 363 -> "XM1145 - 105MM";
            case 364 -> "XM1146 - 105MM";
            case 365 -> "XM1147 - 105MM";
            case 366 -> "XM1148 - 105MM";
            case 367 -> "XM1149 - 105MM";
            case 368 -> "XM1150 - 155MM";
            case 369 -> "XM1151 - 155MM";
            case 370 -> "XM1152 - 155MM";
            case 371 -> "XM1153 - 155MM";
            case 372 -> "XM1154 - 155MM";
            case 373 -> "XM1155 - 155MM";
            case 374 -> "XM1156 - 155MM";
            case 375 -> "XM1157 - 155MM";
            case 376 -> "XM1158 - 155MM";
            case 377 -> "XM1159 - 155MM";
            case 378 -> "XM1160 - MORTAR";
            case 379 -> "XM1161 - MORTAR";
            case 380 -> "XM1162 - MORTAR";
            case 381 -> "XM1163 - MORTAR";
            case 382 -> "XM1164 - MORTAR";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable EffectAchieved (DFI/DUI 4069/001)
     *
     * @param effectAcheived VMF EffectAchieved
     * @return a printable EffectAchieved
     */
    public static String getEffectAchieved(int effectAcheived) {
        return switch (effectAcheived) {
            case 1 -> "NEUTRALIZED";
            case 2 -> "OBSCURED BY SMOKE";
            case 3 -> "POSITION MARKED";
            case 4 -> "DISRUPTED";
            case 5 -> "INTERDICTED";
            case 6 -> "DESTROYED";
            case 7 -> "ILLUMINATED";
            case 8 -> "UNOBSERVED";
            case 9 -> "NO EFFECT";
            case 10 -> "CANCELLED/ABORTED";
            case 11 -> "SUPPRESSED";
            case 12 -> "NEUTRALIZED BURNING";
            case 13 -> "BURNING";
            case 14 -> "UNKNOWN";
            default -> "UNDEFINED";
        };
    }

    /**
     * Return a printable Nationality (DFI/DUI 4127/005)
     *
     * @param nationality VMF Nationality
     * @return a printable Nationality
     */
    static String getNationality(int nationality) {
        return switch (nationality) {
            case VmfDataBuffer.NO_INT -> "";
            case 0 -> NO_STATEMENT;
            case 1 -> "AFGHANISTAN (AF)";
            case 2 -> "ALBANIA (AL)";
            case 3 -> "ALGERIA (AG)";
            case 4 -> "AMERICAN SOMOA (AQ)";
            case 5 -> "ANDORRA (AN)";
            case 6 -> "ANGOLA (AO)";
            case 7 -> "ANGUILLA (AV)";
            case 8 -> "ANTARCTICA (AY)";
            case 9 -> "ANTIGUA AND BARBUDA (AC)";
            case 10 -> "ARGENTINA (AR)";
            case 11 -> "ARMENIA (AM)";
            case 12 -> "ARUBA (AA)";
            case 13 -> "ASHMORE AND CARTER ISLANDS (AT)";
            case 14 -> "AUSTRALIA (AS)";
            case 15 -> "AUSTRIA (AU)";
            case 16 -> "AZERBAIJAN (AJ)";
            case 17 -> "BAHAMAS, THE (BF)";
            case 18 -> "BAHRAIN (BA)";
            case 19 -> "BAKER ISLAND (FQ)";
            case 20 -> "BANGLADESH (BG)";
            case 21 -> "BARBADOS (BB)";
            case 22 -> "BASSAS DA INDIA (BS)";
            case 23 -> "BELARUS (BO)";
            case 24 -> "BELGIUM (BE)";
            case 25 -> "BELIZE (BH)";
            case 26 -> "BENIN (BN)";
            case 27 -> "BERMUDA (BD)";
            case 28 -> "BHUTAN (BT)";
            case 29 -> "BOLIVIA (BL)";
            case 30 -> "BOZNIA AND HERZEGOVINA (BK)";
            case 31 -> "BOTSWANA (BC)";
            case 32 -> "BOUVET ISLAND (BV)";
            case 33 -> "BRAZIL (BR)";
            case 34 -> "BRITISH INDIAN OCEAN TERRITORY (IO)";
            case 35 -> "BRITISH VIRGIN ISLANDS (VI)";
            case 36 -> "BRUNEI (BX)";
            case 37 -> "BULGARIA (BU)";
            case 38 -> "BURKINA FASO (UV)";
            case 39 -> "BURMA (BM)";
            case 40 -> "BURUNDI (BY)";
            case 41 -> "CAMBODIA (CB)";
            case 42 -> "CAMEROON (CM)";
            case 43 -> "CANADA (CA)";
            case 44 -> "CAPE VERDE (CV)";
            case 45 -> "CAYMAN ISLANDS (CJ)";
            case 46 -> "CENTRAL AFRICAN REPUBLIC (CT)";
            case 47 -> "CHAD (CD)";
            case 48 -> "CHILE (CI)";
            case 49 -> "CHINA (CH)";
            case 50 -> "CHRISTMAS ISLAND (KT)";
            case 51 -> "CLIPPERTON ISLAND (IP)";
            case 52 -> "COCOS (KEELING) ISLANDS (CK)";
            case 53 -> "COLOMBIA (CO)";
            case 54 -> "COMOROS (CN)";
            case 55 -> "CONGO (CF)";
            case 56 -> "CONGO (DEMOCRATIC REPUBLIC OF THE) (CG)";
            case 57 -> "COOK ISLANDS (CW)";
            case 58 -> "CORAL SEA ISLANDS (CR)";
            case 59 -> "COSTA RICA (CS)";
            case 60 -> "COTE D'IVOIRE (IV)";
            case 61 -> "CROATIA (HR)";
            case 62 -> "CUBA (CU)";
            case 63 -> "CYPRUS (CY)";
            case 64 -> "CZECH REPUBLIC (EZ)";
            case 65 -> "DENMARK (DA)";
            case 66 -> "DJIBOUTI (DJ)";
            case 67 -> "DOMINICA (DO)";
            case 68 -> "DOMINICAN REPUBLIC (DR)";
            case 69 -> "EAST TIMOR (TT)";
            case 70 -> "ECUADOR (EC)";
            case 71 -> "EGYPT (EG)";
            case 72 -> "EL SALVADOR (ES)";
            case 73 -> "EQUATORIAL GUINEA (EK)";
            case 74 -> "ERITREA (ER)";
            case 75 -> "ESTONIA (EN)";
            case 76 -> "ETHIOPIA (ET)";
            case 77 -> "EUROPA ISLAND (EU)";
            case 78 -> "FALKLAND ISLANDS (FK)";
            case 79 -> "FAROE ISLANDS (FO)";
            case 80 -> "FIJI (FJ)";
            case 81 -> "FINLAND (FI)";
            case 82 -> "FRANCE (FR)";
            case 83 -> "FRENCH GUIANA (FG)";
            case 84 -> "FRENCH POLYNESIA (FP)";
            case 85 -> "FRENCH SOUTHERN AND ANTARCTIC LANDS (FS)";
            case 86 -> "GABON (GB)";
            case 87 -> "GAMBIA, THE (GA)";
            case 88 -> "GAZA STRIP (GZ)";
            case 89 -> "GEORGIA (GG)";
            case 90 -> "GERMANY (GM)";
            case 91 -> "GHANA (GH)";
            case 92 -> "GIBRALTAR (GI)";
            case 93 -> "GLORIOSO ISLANDS (GO)";
            case 94 -> "GREECE (GR)";
            case 95 -> "GREENLAND (GL)";
            case 96 -> "GRENADA (GJ)";
            case 97 -> "GUADELOUPE (GP)";
            case 98 -> "GUAM (GQ)";
            case 99 -> "GUATEMALA (GT)";
            case 100 -> "GUERNSEY (GK)";
            case 101 -> "GUINEA (GV)";
            case 102 -> "GUINEA-BISSAU (PU)";
            case 103 -> "GUYANA (GY)";
            case 104 -> "HAITI (HA)";
            case 105 -> "HEARD ISLAND AND MCDONALD ISLANDS (HM)";
            case 106 -> "HONDURAS (HO)";
            case 107 -> "HONG KONG (HK)";
            case 108 -> "HOWLAND ISLAND (HQ)";
            case 109 -> "HUNGARY (HU)";
            case 110 -> "ICELAND (IC)";
            case 111 -> "INDIA (IN)";
            case 112 -> "INDONESIA (ID)";
            case 113 -> "IRAN (IR)";
            case 114 -> "IRAQ (IZ)";
            case 115 -> "IRELAND (EI)";
            case 116 -> "ISRAEL (IS)";
            case 117 -> "ITALY (IT)";
            case 118 -> "JAMAICA (JM)";
            case 119 -> "JAN MAYEN (JN)";
            case 120 -> "JAPAN (JA)";
            case 121 -> "JARVIS ISLAND (DQ)";
            case 122 -> "JERSEY (JE)";
            case 123 -> "JOHNSTON ATOLL (JQ)";
            case 124 -> "JORDAN (JO)";
            case 125 -> "JUAN DE NOVA ISLAND (JU)";
            case 126 -> "KAZAKHSTAN (KZ)";
            case 127 -> "KENYA (KE)";
            case 128 -> "KINGMAN REEF (KQ)";
            case 129 -> "KIRIBATI (KR)";
            case 130 -> "KOREA, DEMOCRATIC PEOPLES REPUBLIC OF (KN)";
            case 131 -> "KOREA, REPUBLIC OF (KS)";
            case 132 -> "KUWAIT (KU)";
            case 133 -> "KYRGYZSTAN (KG)";
            case 134 -> "LAOS (LA)";
            case 135 -> "LATVIA (LG)";
            case 136 -> "LEBANON (LE)";
            case 137 -> "LESOTHO (LT)";
            case 138 -> "LIBERIA (LI)";
            case 139 -> "LIBYA (LY)";
            case 140 -> "LIECHTENSTEIN (LS)";
            case 141 -> "LITHUANIA (LH)";
            case 142 -> "LUXEMBOURG (LU)";
            case 143 -> "MACAU (MC)";
            case 144 -> "MACEDONIA (MK)";
            case 145 -> "MADAGASCAR (MA)";
            case 146 -> "MALAWI (MI)";
            case 147 -> "MALAYSIA (MY)";
            case 148 -> "MALDIVES (MV)";
            case 149 -> "MALI (ML)";
            case 150 -> "MALTA (MT)";
            case 151 -> "MAN, ISLE OF (IM)";
            case 152 -> "MARSHALL ISLANDS (RM)";
            case 153 -> "MARTINIQUE (MB)";
            case 154 -> "MAURITANIA (MR)";
            case 155 -> "MAURITIUS (MP)";
            case 156 -> "MAYOTTE (MF)";
            case 157 -> "MEXICO (MX)";
            case 158 -> "MICRONESIA, FEDERATED STATES OF (FM)";
            case 159 -> "MIDWAY ISLANDS (MQ)";
            case 160 -> "MOLDOVA (MD)";
            case 161 -> "MONACO (MN)";
            case 162 -> "MONGOLIA (MG)";
            case 163 -> "MONTSERRAT (MH)";
            case 164 -> "MOROCCO (MO)";
            case 165 -> "MOZAMBIQUE (MZ)";
            case 166 -> "NAMIBIA (WA)";
            case 167 -> "NAURU (NR)";
            case 168 -> "NAVASSA ISLAND (BQ)";
            case 169 -> "NEPAL (NP)";
            case 170 -> "NETHERLANDS (NL)";
            case 171 -> "NETHERLANDS ANTILLES (NT)";
            case 172 -> "NEW CALEDONIA (NC)";
            case 173 -> "NEW ZEALAND (NZ)";
            case 174 -> "NICARAGUA (NU)";
            case 175 -> "NIGER (NG)";
            case 176 -> "NIGERIA (NI)";
            case 177 -> "NIUE (NE)";
            case 178 -> "NORFOLK ISLAND (NF)";
            case 179 -> "NORTHERN MARIANA ISLANDS (CQ)";
            case 180 -> "NORWAY (NO)";
            case 181 -> "OMAN (MU)";
            case 182 -> "OTHER COUNTRY (OO)";
            case 183 -> "PAKISTAN (PK)";
            case 184 -> "PALAU (PS)";
            case 185 -> "PALMYRA ATOLL (LQ)";
            case 186 -> "PANAMA (PM)";
            case 187 -> "PAPUA NEW GUINEA (PP)";
            case 188 -> "PARACEL ISLANDS (PF)";
            case 189 -> "PARAGUAY (PA)";
            case 190 -> "PERU (PE)";
            case 191 -> "PHILIPPINES (RP)";
            case 192 -> "PITCAIRN ISLANDS (PC)";
            case 193 -> "POLAND (PL)";
            case 194 -> "PORTUGAL (PO)";
            case 195 -> "PUERTO RICO (RQ)";
            case 196 -> "QATAR (QA)";
            case 197 -> "REUNION (RE)";
            case 198 -> "ROMANIA (RO)";
            case 199 -> "RUSSIA (RS)";
            case 200 -> "RWANDA (RW)";
            case 201 -> "ST. KITTS AND NEVIS (SC)";
            case 202 -> "ST. HELENA (SH)";
            case 203 -> "ST. LUCIA (ST)";
            case 204 -> "ST. PIERRE AND MIQUELON (SB)";
            case 205 -> "ST. VINCENT AND THE GRENADINES (VC)";
            case 206 -> "SAMOA (WS)";
            case 207 -> "SAN MARINO (SM)";
            case 208 -> "SAO TOME AND PRINCIPE (TP)";
            case 209 -> "SAUDI ARABIA (SA)";
            case 210 -> "SENEGAL (SG)";
            case 211 -> "SEYCHELLES (SE)";
            case 212 -> "SIERRA LEONE (SL)";
            case 213 -> "SINGAPORE (SN)";
            case 214 -> "SLOVAKIA (LO)";
            case 215 -> "SLOVENIA (SI)";
            case 216 -> "SOLOMON ISLANDS (BP)";
            case 217 -> "SOMALIA (SO)";
            case 218 -> "SOUTH AFRICA (SF)";
            case 219 -> "SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS (SX)";
            case 220 -> "SPAIN (SP)";
            case 221 -> "SPRATLY ISLANDS (PG)";
            case 222 -> "SRI LANKA (CE)";
            case 223 -> "SUDAN (SU)";
            case 224 -> "SURINAME (NS)";
            case 225 -> "SVALBARD (SV)";
            case 226 -> "SWAZILAND (WZ)";
            case 227 -> "SWEDEN (SW)";
            case 228 -> "SWITZERLAND (SZ)";
            case 229 -> "SYRIA (SY)";
            case 230 -> "TAIWAN (TW)";
            case 231 -> "TAJIKISTAN (TI)";
            case 232 -> "TANZANIA (TZ)";
            case 233 -> "THAILAND (TH)";
            case 234 -> "TOGO (TO)";
            case 235 -> "TOKELAU (TL)";
            case 236 -> "TONGA (TN)";
            case 237 -> "TRINIDAD AND TOBAGO (TD)";
            case 238 -> "TROMELIN ISLAND (TE)";
            case 239 -> "TUNISIA (TS)";
            case 240 -> "TURKEY (TU)";
            case 241 -> "TURKMENISTAN (TX)";
            case 242 -> "TURKS AND CAICOS ISLANDS (TK)";
            case 243 -> "TUVALU (TV)";
            case 244 -> "UGANDA (UG)";
            case 245 -> "UKRAINE (UP)";
            case 246 -> "UNITED ARAB EMIRATES (AE)";
            case 247 -> "UNITED KINGDOM (UK)";
            case 248 -> "UNITED STATES (US)";
            case 249 -> "URUGUAY (UY)";
            case 250 -> "UZBEKISTAN (UZ)";
            case 251 -> "VANUATU (NH)";
            case 252 -> "VATICAN CITY (VT)";
            case 253 -> "VENEZUELA (VE)";
            case 254 -> "VIETNAM (VM)";
            case 255 -> "VIRGIN ISLANDS, US (VQ)";
            case 256 -> "WAKE ISLAND (WQ)";
            case 257 -> "WALLIS AND FUTUNA (WF)";
            case 258 -> "WEST BANK (WE)";
            case 259 -> "WESTERN SAHARA (WI)";
            case 260 -> "YEMEN (YM)";
            case 261 -> "YUGOSLAVIA (YI)";
            case 262 -> "ZAMBIA (ZA)";
            case 263 -> "ZIMBABWE (ZI)";
            case 264 -> "EXERCISE BLACK COUNTRY (OA)";
            case 265 -> "EXERCISE BLACK FORCES (OB)";
            case 266 -> "EXERCISE BLUE COUNTRY (OC)";
            case 267 -> "EXERCISE BLUE FORCE (OD)";
            case 268 -> "EXERCISE FRIENDLY COUNTRY (YC)";
            case 269 -> "EXERCISE FRIENDLY FORCE (YY)";
            case 270 -> "EXERCISE HOSTILE COUNTRY (XC)";
            case 271 -> "EXERCISE HOSTILE FORCE (XX)";
            case 272 -> "EXERCISE NEUTRAL COUNTRY (ZC)";
            case 273 -> "EXERCISE NEUTRAL FORCE (ZZ)";
            case 274 -> "EXERCISE ORANGE COUNTRY (OJ)";
            case 275 -> "EXERCISE ORANGE FORCE (OK)";
            case 276 -> "EXERCISE RED COUNTRY (OR)";
            case 277 -> "EXERCISE RED FORCE (OE)";
            case 278 -> "EXERCISE WHITE COUNTRY (ON)";
            case 279 -> "EXERCISE NATO FORCE (OT)";
            case 280 -> "EXERCISE PURPLE FORCE (OL)";
            case 281 -> "EXERCISE SPARE NUMBER ONE (XA)";
            case 282 -> "EXERCISE SPARE NUMBER TWO (XB)";
            case 283 -> "EXERCISE UNITED NATIONS FORCE (UU)";
            case 284 -> "EXERCISE FORMER WARSAW PACT FORCE (OW)";
            case 285 -> "NAT/ALL-1 THROUGH NAT/ALL-28";
            case 313 -> "SPANISH NORTH AFRICA (SQ)";
            case 314 -> "ABKHAZIA (AB)";
            case 315 -> "AZORES (AZ)";
            case 316 -> "BOPHUTHATSWANA (BW)";
            case 317 -> "CARIBBEAN (EXCLUDING ANTIGUA AND BARBUDA) (CC)";
            case 318 -> "CRIMEA (CX)";
            case 319 -> "CYPRUS, TURKISH REPUBLIC OF NORTHERN (TRNC) (KA)";
            case 320 -> "EASTERN CARIBBEAN COUNTRIES (EA)";
            case 321 -> "EASTERN EUROPEAN COUNTRIES (PW)";
            case 322 -> "FALKLAND ISLAND DEPENDENCIES (FL)";
            case 323 -> "FAR EAST COUNTRIES (FE)";
            case 324 -> "KOSOVO (KO)";
            case 325 -> "LATIN AMERICA (LM)";
            case 326 -> "MIDDLE EASTERN/NORTH AFRICAN COUNTRIES (ME)";
            case 327 -> "NAGORNO-KARABAKH (NK)";
            case 328 -> "NORDIC COUNTRIES (NN)";
            case 329 -> "NORTH OSSETIA (OS)";
            case 330 -> "SOUTH AMERICA (UT)";
            case 331 -> "SOUTH ASIA (AI)";
            case 332 -> "SOUTH MARIANA ISLAND (MS)";
            case 333 -> "SOUTH OSSETIA (OF)";
            case 334 -> "SOUTH PACIFIC ISLAND NATIONS OR TERRITORIES (PI)";
            case 335 -> "SOUTH EAST ASIA (EO)";
            case 336 -> "SUB-SAHARAN AFRICA (UB)";
            case 337 -> "TARTAR HOMELAND (TJ)";
            case 338 -> "TRANSKEI (TR)";
            case 339 -> "UNIDENTIFIED (UI)";
            case 340 -> "WEST EUROPEAN COUNTRIES (EW)";
            case 341 -> "WEST HEMISPHERE (HW)";
            case 342 -> "WORLDWIDE (GW)";
            case 343 -> "SERBIA AND MONTENEGRO (YI)";
            default -> "UNDEFINED (" + nationality + ')';
        };
    }
}
