package gov.mil.otc._3dvis.data.mission;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionConfigurations {

    public static MissionConfigurations load(File file) {
        MissionConfigurations missionConfigurations = null;
        Gson gson = new Gson();
        if (file.exists() && file.canRead()) {
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(file.getPath()));
                String fileString = new String(fileContent);
                missionConfigurations = gson.fromJson(fileString, MissionConfigurations.class);
                missionConfigurations.initialize();
            } catch (Exception e) {
                String message = String.format("Unable to read preferences file %s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
        if (missionConfigurations == null) {
            missionConfigurations = new MissionConfigurations();
            if (file.exists() && file.canWrite()) {
                try {
                    Files.copy(file.toPath(), new File(file.getAbsolutePath() + ".bak").toPath());
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "MissionConfigurations::load", e);
                }
                try (FileWriter fileWriter = new FileWriter(file, false)) {
                    Gson prettyGson = gson.newBuilder().setPrettyPrinting().create();
                    String serializedObject = prettyGson.toJson(missionConfigurations);
                    fileWriter.write(serializedObject);
                    fileWriter.flush();
                } catch (IOException e) {
                    String message = String.format("Unable to save mission configuration file %s", file.getAbsolutePath());
                    Logger.getGlobal().log(Level.WARNING, message, e);
                }
            }
        }
        return missionConfigurations;
    }

    @SerializedName("mission configurations")
    private final List<MissionConfiguration> missionConfigurationList = new ArrayList<>();

    private void initialize() {
        for (MissionConfiguration missionConfiguration : missionConfigurationList) {
            missionConfiguration.initialize();
        }
    }

    public void addMissionConfiguration(MissionConfiguration missionConfiguration) {
        missionConfigurationList.add(missionConfiguration);
    }

    public MissionConfiguration getMissionConfiguration(String missionName) {
        for (MissionConfiguration missionConfiguration : missionConfigurationList) {
            if (missionConfiguration.getMissionName().equalsIgnoreCase(missionName)) {
                return missionConfiguration;
            }
        }
        return null;
    }

    public List<MissionConfiguration> getMissionConfigurationList() {
        return missionConfigurationList;
    }

    public void save(String path, String filename) {
        String absoluteFilePath = path + File.separator + filename;
        File tempFile = new File(absoluteFilePath);
        if ((tempFile.exists() && tempFile.canWrite())
                || (tempFile.getParentFile().exists() && tempFile.getParentFile().canWrite())) {
            try (FileWriter fileWriter = new FileWriter(absoluteFilePath, false)) {
                Gson gson = new Gson();
                Gson prettyGson = gson.newBuilder().setPrettyPrinting().create();
                String serializedObject = prettyGson.toJson(this);
                fileWriter.write(serializedObject);
                fileWriter.flush();
            } catch (IOException e) {
                String message = String.format("Unable to save mission configurations file %s", absoluteFilePath);
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
    }
}
