package gov.mil.otc._3dvis.overlay;

import gov.mil.otc._3dvis.datamodel.EntityTypeUtility;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class IconMap {

    private static final IconMap SINGLETON = new IconMap();
    private final Map<String, String> map = new HashMap<>();

    private IconMap() {
        try (InputStream inputStream = EntityTypeUtility.class.getResourceAsStream("icon_map.csv")) {
            if (inputStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length == 2) {
                        map.put(values[0], values[1]);
                    }
                }
            } else {
                Logger.getGlobal().log(Level.WARNING, "File does not exists, icon_map.csv");
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "Error reading icon_map.csv", e);
        }
    }

    public static String get(String id) {
        return SINGLETON.map.get(id);
    }
}
