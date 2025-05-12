package gov.mil.otc._3dvis.overlay;

import java.util.Map;

public final class Dimension {

    private static final Map<Integer, String> DIMENSION_MAP = Map.ofEntries(
            Map.entry(0, "S-P-"),
            Map.entry(1, "S-A-"),
            Map.entry(2, "S-G-"),
            Map.entry(3, "S-G-"),
            Map.entry(4, "S-G-"),
            Map.entry(5, "S-G-"),
            Map.entry(6, "S-G-"),
            Map.entry(7, "S-G-"),
            Map.entry(8, "S-S-"),
            Map.entry(9, "S-U-"),
            Map.entry(10, "S-F-"),
            Map.entry(11, "G-T-"),
            Map.entry(12, "G-G-"),
            Map.entry(13, "G-M-"),
            Map.entry(14, "G-F-"),
            Map.entry(15, "G-S-"),
            Map.entry(16, "G-O-"),
            Map.entry(17, "W-A-"),
            Map.entry(18, "I-P-"),
            Map.entry(19, "I-A-"),
            Map.entry(20, "I-G-"),
            Map.entry(21, "I-S-"),
            Map.entry(22, "I-U-"),
            Map.entry(23, "O-V-"),
            Map.entry(24, "O-L-"),
            Map.entry(25, "O-O-"),
            Map.entry(26, "O-I-"),
            Map.entry(27, "WO--")
    );

    private Dimension() {
    }

    public static String get(int i) {
        return DIMENSION_MAP.get(i);
    }
}
