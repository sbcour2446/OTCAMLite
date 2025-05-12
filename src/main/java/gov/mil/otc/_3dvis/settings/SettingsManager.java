package gov.mil.otc._3dvis.settings;

import com.google.gson.Gson;
import gov.nasa.worldwind.WorldWind;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsManager {

    private static final SettingsManager SINGLETON = new SettingsManager();
    private final Gson gson = new Gson();
    private final String settingsFolderPath;
    private final String settingsFilePath;
    private final String preferencesFolderPath;
    private final String preferencesFilePath;
    private Preferences preferences = null;
    private Settings settings = null;

    private SettingsManager() {
        preferencesFolderPath = System.getProperty("user.home") + File.separator + ".3dvis";
        preferencesFilePath = preferencesFolderPath + File.separator + "preferences.json";
        settingsFolderPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
        settingsFilePath = settingsFolderPath + File.separator + "settings.json";
    }

    public static boolean initialize() {
        return SINGLETON.doInitialize();
    }

    public static void save() {
        SINGLETON.doSavePreferences();
        SINGLETON.doSaveSettings();
    }

    public static Settings getSettings() {
        return SINGLETON.settings;
    }

    public static Preferences getPreferences() {
        return SINGLETON.preferences;
    }

    private boolean doInitialize() {
        loadPreferences();
        loadSettings();

        if (preferences == null) {
            preferences = new Preferences();
            doSavePreferences();
        }

        if (settings == null) {
            settings = new Settings();
            doSaveSettings();
        }

        loadDatastore();

        return true;
    }

    private void loadPreferences() {
        File preferencesFolder = new File(preferencesFolderPath);
        if (preferencesFolder.exists()) {
            File settingsFile = new File(preferencesFilePath);
            if (settingsFile.exists() && settingsFile.canRead()) {
                try {
                    byte[] fileContent = Files.readAllBytes(Paths.get(preferencesFilePath));
                    String fileString = new String(fileContent);
                    preferences = gson.fromJson(fileString, Preferences.class);
                } catch (Exception e) {
                    String message = String.format("Unable to read preferences file %s", preferencesFilePath);
                    Logger.getGlobal().log(Level.WARNING, message, e);
                }
            }
        }
    }

    private void loadSettings() {
        File settingsFolder = new File(settingsFolderPath);
        if (settingsFolder.exists()) {
            File settingsFile = new File(settingsFilePath);
            if (settingsFile.exists() && settingsFile.canRead()) {
                try {
                    byte[] fileContent = Files.readAllBytes(Paths.get(settingsFilePath));
                    String fileString = new String(fileContent);
                    settings = gson.fromJson(fileString, Settings.class);
                } catch (Exception e) {
                    String message = String.format("Unable to read settings file %s", settingsFilePath);
                    Logger.getGlobal().log(Level.WARNING, message, e);
                }
            }
        }
    }

    private void doSavePreferences() {
        File tempFile = new File(preferencesFilePath);
        if ((tempFile.exists() && tempFile.canWrite())
                || (tempFile.getParentFile().exists() && tempFile.getParentFile().canWrite())) {
            try (FileWriter fileWriter = new FileWriter(preferencesFilePath, false)) {
                Gson prettyGson = gson.newBuilder().setPrettyPrinting().create();
                String serializedObject = prettyGson.toJson(preferences);
                fileWriter.write(serializedObject);
                fileWriter.flush();
            } catch (IOException e) {
                String message = String.format("Unable to save preferences file %s", preferencesFilePath);
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
    }

    private void doSaveSettings() {
        File tempFile = new File(settingsFilePath);
        if ((tempFile.exists() && tempFile.canWrite())
                || (tempFile.getParentFile().exists() && tempFile.getParentFile().canWrite())) {
            try (FileWriter fileWriter = new FileWriter(settingsFilePath, false)) {
                Gson prettyGson = gson.newBuilder().setPrettyPrinting().create();
                String serializedObject = prettyGson.toJson(settings);
                fileWriter.write(serializedObject);
                fileWriter.flush();
            } catch (IOException e) {
                String message = String.format("Unable to save settings file %s", settingsFilePath);
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
    }

    private void loadDatastore() {
        for (File file : settings.getDatastoreList()) {
            WorldWind.getDataFileStore().addLocation(file.getPath(), false);
        }
    }
}
