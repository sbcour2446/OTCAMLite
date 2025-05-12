package gov.mil.otc._3dvis.playback.dataset.entity;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import gov.mil.otc._3dvis.entity.base.EntityConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityConfigurationFile {

    public static final String NAME = "EntityConfigurations.json";

    public static EntityConfigurationFile load(File file) {
        EntityConfigurationFile entityConfigurationFile = null;
        Gson gson = new Gson();
        if (file.exists() && file.canRead()) {
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(file.getPath()));
                String fileString = new String(fileContent);
                entityConfigurationFile = gson.fromJson(fileString, EntityConfigurationFile.class);
            } catch (Exception e) {
                String message = String.format("Unable to read playback configuration file %s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
        if (entityConfigurationFile == null) {
            entityConfigurationFile = new EntityConfigurationFile();
            if (file.exists() && file.canWrite()) {
                try {
                    Files.copy(file.toPath(), new File(file.getAbsolutePath() + ".bak").toPath());
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "EntityConfigurationFile::load", e);
                }
                try (FileWriter fileWriter = new FileWriter(file, false)) {
                    Gson prettyGson = gson.newBuilder().setPrettyPrinting().create();
                    String serializedObject = prettyGson.toJson(entityConfigurationFile);
                    fileWriter.write(serializedObject);
                    fileWriter.flush();
                } catch (IOException e) {
                    String message = String.format("Unable to save entity configuration file %s", file.getAbsolutePath());
                    Logger.getGlobal().log(Level.WARNING, message, e);
                }
            }
        }
        return entityConfigurationFile;
    }

    @SerializedName("entity configurations")
    private final List<EntityConfiguration> entityConfigurationList = new ArrayList<>();

    public void addEntityConfiguration(EntityConfiguration entityConfiguration) {
        entityConfigurationList.add(entityConfiguration);
    }

    public EntityConfiguration getEntityConfiguration(String entityName) {
        for (EntityConfiguration entityConfiguration : entityConfigurationList) {
            if (entityConfiguration.getName().equalsIgnoreCase(entityName)) {
                return entityConfiguration;
            }
        }
        return null;
    }

    public List<EntityConfiguration> getEntityConfigurationList() {
        return entityConfigurationList;
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
                String message = String.format("Unable to save entity configurations file %s", absoluteFilePath);
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
    }
}
