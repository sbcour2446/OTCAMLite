package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

import java.util.HashMap;
import java.util.Map;

public final class WeaponSystemResponse extends TimedData {

    public static final int NUMBER_OF_MESSAGES = 18;
    private static final Map<Integer, String> WEAPON_SYSTEM_NAME_MAP = new HashMap<>();

    public static void setWeaponSystemName(int id, String name) {
        WEAPON_SYSTEM_NAME_MAP.put(id, name);
    }

    private final Map<Integer, WeaponSystemResponseMessage> weaponSystemResponseMessageMap;

    public WeaponSystemResponse(long timestamp, Map<Integer, WeaponSystemResponseMessage> weaponSystemResponseMessages) {
        super(timestamp);
        this.weaponSystemResponseMessageMap = weaponSystemResponseMessages;
    }

    public Map<Integer, WeaponSystemResponseMessage> getWeaponSystemResponseMessageMap() {
        return weaponSystemResponseMessageMap;
    }

    public record WeaponSystemResponseMessage(double azimuth, int weaponSystemId) {
        public String getWeaponSystemName() {
            String name = WEAPON_SYSTEM_NAME_MAP.get(weaponSystemId);
            if (name == null) {
                name = String.valueOf(weaponSystemId);
            }
            return name;
        }
    }
}
