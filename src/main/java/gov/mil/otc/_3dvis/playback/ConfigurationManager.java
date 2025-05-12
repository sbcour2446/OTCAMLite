package gov.mil.otc._3dvis.playback;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationManager {

    public static <T> T load(File file, Class<T> classOfT) {
        T configuration = null;
        Gson gson = new Gson();
        if (file.exists() && file.canRead()) {
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(file.getPath()));
                String fileString = new String(fileContent);
                configuration = gson.fromJson(fileString, classOfT);
            } catch (Exception e) {
                String message = String.format("Unable to read configuration file %s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
        return configuration;
    }

    public static void save(Object o, String filename) {
        File tempFile = new File(filename);
        if ((tempFile.exists() && tempFile.canWrite())
                || (tempFile.getParentFile().exists() && tempFile.getParentFile().canWrite())) {
            try (FileWriter fileWriter = new FileWriter(filename, false)) {
                Gson gson = new Gson();
                Gson prettyGson = gson.newBuilder().setPrettyPrinting().create();
                String serializedObject = prettyGson.toJson(o);
                fileWriter.write(serializedObject);
                fileWriter.flush();
            } catch (IOException e) {
                String message = String.format("Unable to save configurations file %s", filename);
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
    }

    /*
    Save file, if file exists, make copy of original
     */
    public static void safeSave(Object o, String filename) {
        File tempFile = new File(filename);

        if (tempFile.exists()) {
            String backupFilename = filename + ".bak";
            File backupFile = new File(backupFilename);
            int i = 0;
            while (backupFile.exists()) {
                backupFile = new File(backupFilename + (++i));
            }
            try {
                Files.copy(tempFile.toPath(), backupFile.toPath());
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ConfigurationManager::safeSave", e);
            }
        }

        if ((tempFile.exists() && tempFile.canWrite())
                || (tempFile.getParentFile().exists() && tempFile.getParentFile().canWrite())) {
            try (FileWriter fileWriter = new FileWriter(filename, false)) {
                Gson gson = new Gson();
                Gson prettyGson = gson.newBuilder().setPrettyPrinting().create();
                String serializedObject = prettyGson.toJson(o);
                fileWriter.write(serializedObject);
                fileWriter.flush();
            } catch (IOException e) {
                String message = String.format("Unable to save configurations file %s", filename);
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
    }
}
