package gov.mil.otc._3dvis.playback.dataset.mrwr;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvConfiguration;
import gov.mil.otc._3dvis.entity.base.EntityConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MrwrConfiguration {

    public static final String NAME = "MrwrConfiguration.json";

    private final CsvConfiguration csvConfiguration;
    private final Map<String, EntityConfiguration> entityConfigurationMap = new HashMap<>();

    public MrwrConfiguration(CsvConfiguration csvConfiguration, List<EntityConfiguration> entityConfigurations) {
        this.csvConfiguration = csvConfiguration;
        for (EntityConfiguration entityConfiguration : entityConfigurations) {
            entityConfigurationMap.put(entityConfiguration.getName(), entityConfiguration);
        }
    }

    public CsvConfiguration getCsvConfiguration() {
        return csvConfiguration;
    }

    public Map<String, EntityConfiguration> getEntityConfigurationMap() {
        return entityConfigurationMap;
    }

    public EntityConfiguration getEntityConfiguration(String name) {
        return entityConfigurationMap.get(name);
    }
}
