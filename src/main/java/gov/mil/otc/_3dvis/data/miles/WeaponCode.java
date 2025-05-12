package gov.mil.otc._3dvis.data.miles;

import java.util.HashMap;
import java.util.Map;

/**
 * Available weapon codes from MILES.
 */
public enum WeaponCode {
    /**
     * Universal weapon code.
     */
    CODE_00(0, "Universal Kill"),
    /**
     * Maverick, AGES Hellfire, TWGSS TOW missile.
     */
    CODE_01(1, "Missile: Maverick), AGES Hellfire), TWGSS TOW"),
    /**
     * Hellfire missile.
     */
    CODE_02(2, "Missile: Hellfire"),
    /**
     * AT-3 Sagger missile.
     */
    CODE_03(3, "Missile: AT-3 Sagger"),
    /**
     * Mortar weapon.
     */
    CODE_04(4, "Mortar: 60mm), 81mm), 107mm), 120mm), 160mm), 240mm"),
    /**
     * Mine
     */
    CODE_05(5, "Mine: M15 Track Cutter"),
    /**
     * Weapon X
     */
    CODE_06(6, "Weapon X"),
    /**
     * TOW, AT4 Spigot, AT-8 Songster
     */
    CODE_07(7, "TOW), AT4 Spigot), AT-8 Songster"),
    /**
     * M47 Dragon
     */
    CODE_08(8, "M47 Dragon), AT-5), RPG-16"),
    /**
     * Flameflower, M202, Javelin.
     */
    CODE_09(9, "Flame Thrower), M202), JAVELIN"),
    /**
     * M 21 AT mine. 125 mm gun.
     */
    CODE_0A(10, "Mine: M21 AT; Main Gun: 125mm"),
    /**
     * Claymore AP, M16.
     */
    CODE_0B(11, "Mine: M81A1 Claymore AP), M16"),
    /**
     * 105 mm gun.
     */
    CODE_0C(12, "Main Gun: 105mm"),
    /**
     * Howitzer.
     */
    CODE_0D(13, "Howitzer: 152mm), 122mm), 155mm), 100mm), 122mm BM21"),
    /**
     * 2.75 inch rocket, 57 mm rocket, main gun 73 mm.
     */
    CODE_0E(14, "Rocket: 2.75in), 57mm Rocket; Main Gun: 73mm"),
    /**
     * 66 mm M72 LAW, 70 mm Viper, AT-4
     */
    CODE_0F(15, "Rocket: 66 mm M72 LAW), 70mm Viper), AT-4"),
    /**
     * 120 mm main gun.
     */
    CODE_10(16, "Main Gun: 120mm"),
    /**
     * 90 mm rifle.
     */
    CODE_11(17, "Rifle: 90mm"),
    /**
     * Howitzer 203 mm, 105 mm, 122 mm, 155 mm.
     */
    CODE_12(18, "Howitzer: 203mm), 105mm), 122 mm), 155mm"),
    /**
     * 40 mm Mark 19 AGS, 40mm M203 grenade.
     */
    CODE_13(19, "40mm Mark 19 AGS), 40mm M203 Grenade"),
    /**
     * Rockeye cluster bomb, SMAW.
     */
    CODE_14(20, "Bomb), Cluster: Rockeye), SMAW"),
    /**
     * 30 MM GAU-9 Avenger, AH-64.
     */
    CODE_15(21, "Gun: 30mm GAU-8 Avenger), AH-64"),
    /**
     * AA 23mm, 25 mm main gun.
     */
    CODE_16(22, "Gun), AA: 23mm; Main Gun: 25mm"),
    /**
     * 20 mm Vulcan, main gun 30 mm.
     */
    CODE_17(23, "Gun), AA: 20mm Vulcan; Main Gun: 30mm"),
    /**
     * .50 caliber machine gun, M2, M85.
     */
    CODE_18(24, "Machine Gun .50 cal), M2), M85), etc."),
    /**
     * Chaparral, SA-9 Gasgkin, SA-13 Gopher, ASET IV.
     */
    CODE_19(25, "Chaparral), SA-9 Gaskin), SA-13 Gopher), ASET IV"),
    /**
     * Stinger missile.
     */
    CODE_1A(26, "Missile: Stinger"),
    /**
     * M16, M60, M240, Coax.
     */
    CODE_1B(27, "M16), M60), M240), Coax), etc."),
    /**
     * Heavy miss:, 105 mm, 152 mm, 73 mm, Viper.
     */
    CODE_1C(28, "Heavy Miss: 105mm), 152mm), 73mm), Viper), etc."),
    /**
     * Light miss: Rifle, machine gun, 20 mm.
     */
    CODE_1D(29, "Light Miss: Rifle), Machine Gun), 20mm), etc."),
    /**
     * Optical resurrect.
     */
    CODE_1E(30, "Optical Resurrect"),
    /**
     * Heavy spare miss.
     */
    CODE_1F(31, "Heavy Spare Miss"),
    /**
     * IFS Actuation.
     */
    CODE_20(32, "IFS Actuation"),
    /**
     * Missile, SA-14 Gremlin.
     */
    CODE_21(33, "Missile: SA-14 Gremlin"),
    /**
     * AA 23 mm.
     */
    CODE_22(34, "Gun AA: 23mm"),
    /**
     * Controller gun.
     */
    CODE_23(35, "Controller gun"),
    /**
     * Optical reset.
     */
    CODE_24(36, "Optical Reset"),
    /**
     * Invalid.
     */
    INVALID(255, "Invalid Code");

    /**
     * Integer value for the weapon code.
     */
    final int value;

    /**
     * Name of weapon code.
     */
    final String enumName;

    /**
     * Weapon code to type map.
     */
    private static final Map<Integer, WeaponCode> EVENT_CODE_MAP = new HashMap<>();

    static {
        for (WeaponCode weaponCode : WeaponCode.values()) {
            EVENT_CODE_MAP.put(weaponCode.value, weaponCode);
        }
    }

    /**
     * Constructor.
     *
     * @param value    Integer value.
     * @param enumName Name of the weapon code.
     */
    WeaponCode(int value, String enumName) {
        this.value = value;
        this.enumName = enumName;
    }

    /**
     * Returns a string representation of the enumerated type.
     *
     * @return A string representation of the enumerated type.
     */
    @Override
    public String toString() {
        return this.enumName;
    }

    /**
     * Get WeaponCode enumeration from value.
     *
     * @param i The enumeration value.
     * @return The WeaponCode if valid, otherwise the INVALID enumeration.
     */
    public static WeaponCode fromInt(int i) {
        WeaponCode weaponCode = EVENT_CODE_MAP.get(i);
        if (weaponCode == null) {
            return WeaponCode.INVALID;
        }
        return weaponCode;
    }
}
