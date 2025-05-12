package gov.mil.otc._3dvis.data.gps;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EntityConfigurationFile extends CsvFile {

    //startTime,id,name,description,urn,affiliation,entityType
    private static final String START_TIME_COLUMN = "startTime";
    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String URN_COLUMN = "urn";
    private static final String AFFILIATION_COLUMN = "affiliation";
    private static final String ENTITY_TYPE_COLUMN = "entityType";

    public EntityConfigurationFile(File file) {
        super(file);
        addColumn(START_TIME_COLUMN, true);
        addColumn(ID_COLUMN, true);
        addColumn(NAME_COLUMN, true);
        addColumn(DESCRIPTION_COLUMN, true);
        addColumn(URN_COLUMN, true);
        addColumn(AFFILIATION_COLUMN, true);
        addColumn(ENTITY_TYPE_COLUMN, true);
    }

    public Map<Integer, EntityConfiguration> entityConfigurationIdMap = new HashMap<>();
    public Map<String, EntityConfiguration> entityConfigurationNameMap = new HashMap<>();

    public EntityConfiguration getEntityConfiguration(int id) {
        return entityConfigurationIdMap.get(id);
    }

    public EntityConfiguration getEntityConfiguration(String name) {
        return entityConfigurationNameMap.get(name);
    }

    @Override
    protected void processLine(String[] fields) {
        if (fields == null) {
            return;
        }

        EntityConfiguration entityConfiguration = EntityConfiguration.create(
                fields[getColumnIndex(START_TIME_COLUMN)],
                fields[getColumnIndex(ID_COLUMN)],
                fields[getColumnIndex(NAME_COLUMN)],
                fields[getColumnIndex(DESCRIPTION_COLUMN)],
                fields[getColumnIndex(URN_COLUMN)],
                fields[getColumnIndex(AFFILIATION_COLUMN)],
                fields[getColumnIndex(ENTITY_TYPE_COLUMN)]);
        if (entityConfiguration != null) {
            if (entityConfiguration.getId() > 0) {
                entityConfigurationIdMap.put(entityConfiguration.getId(), entityConfiguration);
            } else {
                entityConfigurationNameMap.put(entityConfiguration.getName(), entityConfiguration);
            }
        }
    }
}
