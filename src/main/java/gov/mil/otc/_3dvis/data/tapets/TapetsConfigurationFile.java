package gov.mil.otc._3dvis.data.tapets;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TapetsConfigurationFile extends CsvFile {

    //tapetsId,name,description,urn,affiliation,entityType
    private static final String TAPETS_ID_COLUMN = "tapetsId";
    private static final String START_TIME_COLUMN = "startTime";
    private static final String NAME_COLUMN = "name";
    private static final String DESCRIPTION_COLUMN = "description";
    private static final String URN_COLUMN = "urn";
    private static final String AFFILIATION_COLUMN = "affiliation";
    private static final String ENTITY_TYPE_COLUMN = "entityType";

    private final Map<Integer, TapetsConfiguration> tapetsConfigurationMap = new HashMap<>();

    public TapetsConfigurationFile(File file) {
        super(file);
        addColumn(TAPETS_ID_COLUMN);
        addColumn(START_TIME_COLUMN, true);
        addColumn(NAME_COLUMN);
        addColumn(DESCRIPTION_COLUMN, true);
        addColumn(URN_COLUMN, true);
        addColumn(AFFILIATION_COLUMN);
        addColumn(ENTITY_TYPE_COLUMN);
    }

    public Map<Integer, TapetsConfiguration> getTapetsConfigurationMap() {
        return tapetsConfigurationMap;
    }

    public TapetsConfiguration getTapetsConfiguration(int id) {
        return tapetsConfigurationMap.get(id);
    }

    @Override
    protected void processLine(String[] fields) {
        if (fields == null) {
            return;
        }

        String startTimeString = "";
        if (getColumnIndex(START_TIME_COLUMN) > -1) {
            startTimeString = fields[getColumnIndex(START_TIME_COLUMN)];
        }
        TapetsConfiguration tapetsConfiguration = TapetsConfiguration.create(
                fields[getColumnIndex(TAPETS_ID_COLUMN)],
                fields[getColumnIndex(NAME_COLUMN)],
                fields[getColumnIndex(DESCRIPTION_COLUMN)],
                fields[getColumnIndex(URN_COLUMN)],
                fields[getColumnIndex(AFFILIATION_COLUMN)],
                fields[getColumnIndex(ENTITY_TYPE_COLUMN)],
                startTimeString);
        if (tapetsConfiguration != null) {
            tapetsConfigurationMap.put(tapetsConfiguration.getTapetsId(), tapetsConfiguration);
        }
    }
}
