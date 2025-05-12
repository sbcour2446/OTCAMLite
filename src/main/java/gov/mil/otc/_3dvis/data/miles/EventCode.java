package gov.mil.otc._3dvis.data.miles;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * MILES Event Codes.
 */
public enum EventCode {
    /**
     * Vehicle initialization.
     */
    VEHICLE_INIT(0x00),
    /**
     * Resurrection event.
     */
    RESURRECTION(0x01),
    /**
     * Reset event.
     */
    RESET(0x02),
    /**
     * Memory dump
     */
    MEMORY_DUMP_TO_SLID(0x03),
    /**
     * Optical-based resurrection.
     */
    OPTICAL_RESURRECTION(0x04),
    /**
     * Cheat kill.
     */
    CHEAT_KILL(0x05),
    /**
     * Detected controller key.
     */
    CONTROLLER_KEY_DETECT(0x06),
    /**
     * Time synchronization roll over.
     */
    TIME_SYNC_ROLLOVER(0x07),
    /**
     * Invalid power on sequence.
     */
    POWER_ON_INVALID(0x08),
    /**
     * Power off event.
     */
    POWER_OFF(0x09),
    /**
     * Weapon fired event.
     */
    WEAPON_FIRED(0x0A),
    /**
     * Trigger released event.
     */
    TRIGGER_RELEASED(0x0B),
    /**
     * Shot miss.
     */
    MISS(0x0E),
    /**
     * Shot hit.
     */
    HIT(0x0F),
    /**
     * Shot kill.
     */
    KILL(0x10),
    /**
     * Killed by mobility.
     */
    MOBILITY_KILL(0x11),
    /**
     * Killed by firepower.
     */
    FIREPOWER_KILL(0x12),
    /**
     * Killed by communications.
     */
    COMMUNICATIONS_KILL(0x13),
    /**
     * Low battery warning
     */
    LOW_BATTERY_WARNING(0x14),
    /**
     * Key in weapon event.
     */
    WEAPON_KEY_IN(0x15),
    /**
     * SAWE miss.
     */
    SAWE_MISS(0x19),
    /**
     * SAWE hit.
     */
    SAWE_HIT(0x1A),
    /**
     * SAWE kill.
     */
    SAWE_KILL(0x1B),
    /**
     * SAWE mobility kill.
     */
    SAWE_MOBILITY_KILL(0x1C),
    /**
     * SAWE firepower kill.
     */
    SAWE_FIREPOWER_KILL(0x1D),
    /**
     * SAWE communication kill.
     */
    SAWE_COMMUNICATIONS_KILL(0x1E),
    /**
     * SAWE Chemical contamination event.
     */
    SAWE_CHEMICAL_CONTAMINATION(0x1F),
    /**
     * SAWE reset.
     */
    SAWE_RESET(0x20),
    /**
     * BIT failure.
     */
    BIT_FAILURE(0x21),
    /**
     * SAWE initialization.
     */
    SAWE_INIT(0x22),
    /**
     * SAWE sleep.
     */
    SAWE_SLEEP(0x23),
    /**
     * SAWE wake.
     */
    SAWE_WAKE(0x24),
    /**
     * Reference corner.
     */
    REFERENCE_CORNER(0x25),
    /**
     * SAW low battery.
     */
    SAWE_BATTERY_LOW(0x26),
    /**
     * Low PMI battery.
     */
    PMI_BATTERY_LOW(0x27),
    /**
     * Frequency change event.
     */
    FREQUENCY_CHANGE(0x28),
    /**
     * Initialization event.
     */
    INITIALIZATION(0x29),
    /**
     * CIS sleep.
     */
    CIS_SLEEP(0x2A),
    /**
     * CIS wake.
     */
    CIS_WAKE(0x2B),
    /**
     * SAWE shutdown.
     */
    SAWE_SHUTDOWN(0x2C),
    /**
     * Chemical contamination.
     */
    CHEMICAL_CONTAMINATION(0x2D),
    /**
     * Normal RTCA mode for SAWE.
     */
    NORMAL_SAWE_RTCA_MODE(0x2E),
    /**
     * No kill SAWE RTCA mode.
     */
    NO_KILL_SAWE_RTCA_MODE(0x2F),
    /**
     * Vehicle powered on.
     */
    VEHICLE_POWER_ON(0x30),
    /**
     * Vehicle powered off.
     */
    VEHICLE_POWER_OFF(0x31),
    /**
     * NBC system off.
     */
    NBC_SYSTEM_OFF(0x32),
    /**
     * Blower on.
     */
    BLOWER_ON(0x33),
    /**
     * Pressure on.
     */
    PRESSURE_ON(0x34),
    /**
     * NBC error.
     */
    NBC_ERROR(0x35),
    /**
     * MIC A operator error.
     */
    MIC_A_OPERATOR_ERROR(0x36),
    /**
     * MIC A on.
     */
    MIC_A_ON(0x37),
    /**
     * MIC A off.
     */
    MIC_A_OFF(0x38),
    /**
     * MIC A system.
     */
    MIC_A_SYS(0x39),
    /**
     * MIC B operator error.
     */
    MIC_B_OPERATOR_ERROR(0x3A),
    /**
     * MIC B on.
     */
    MIC_B_ON(0x3B),
    /**
     * MIC B off.
     */
    MIC_B_OFF(0x3C),
    /**
     * MIC B system error.
     */
    MIC_B_SYS_ERROR(0x3D),
    /**
     * GB 0.
     */
    GB_0(0x40),
    /**
     * GB 1.
     */
    GB_1(0x41),
    /**
     * GB 2.
     */
    GB_2(0x42),
    /**
     * GB 3.
     */
    GB_3(0x43),
    /**
     * GB 4.
     */
    GB_4(0x44),
    /**
     * GB 5.
     */
    GB_5(0x45),
    /**
     * GB 6.
     */
    GB_6(0x46),
    /**
     * GB 7.
     */
    GB_7(0x47),
    /**
     * GB 8.
     */
    GB_8(0x48),
    /**
     * GB 9.
     */
    GB_9(0x49),
    /**
     * GB A.
     */
    GB_A(0x4A),
    /**
     * GB B.
     */
    GB_B(0x4B),
    /**
     * GB C.
     */
    GB_C(0x4C),
    /**
     * GB D.
     */
    GB_D(0x4D),
    /**
     * GB E.
     */
    GB_E(0x4E),
    /**
     * GB F.
     */
    GB_F(0x4F),
    /**
     * Optical reset.
     */
    OPTICAL_RESET(0x52),
    /**
     * Disassociated.
     */
    DISASSOCIATED(0xA6),
    /**
     * Associated.
     */
    ASSOCIATED(0xAF),
    /**
     * Unknown.
     */
    UNKNOWN(0xFF);

    /**
     * The hex value associated with the enum flag.
     */
    private final int value;

    /**
     * The event code map.
     */
    private static final Map<Integer, EventCode> EVENT_CODE_MAP = new HashMap<>();

    static {
        for (EventCode eventCode : EventCode.values()) {
            EVENT_CODE_MAP.put(eventCode.value, eventCode);
        }
    }

    /**
     * Constructor.
     *
     * @param value The flag integer value.
     */
    EventCode(int value) {
        this.value = value;
    }

    /**
     * Gets the event code from the provided integer value.
     *
     * @param i The integer flag.
     * @return The {@link EventCode}.
     */
    public static EventCode fromInt(int i) {
        EventCode eventCode = EVENT_CODE_MAP.get(i);
        if (eventCode == null) {
            return EventCode.UNKNOWN;
        }
        return eventCode;
    }

    /**
     * Deserializes an integer from a byte buffer and creates a {@link EventCode}.
     *
     * @param byteBuffer The byte buffer containing an event code integer.
     * @return The corresponding event code.
     */
    public static EventCode deserialize(ByteBuffer byteBuffer) {
        return fromInt(byteBuffer.get());
    }
}
