package gov.mil.otc._3dvis.project.javelin;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CutSheetFile extends CsvFile {

    private static final String ID_COLUMN = "id";
    private static final String NAME_COLUMN = "name";
    private static final String ENTITY_TYPE_COLUMN = "entityType";
    private static final String MILITARY_SYMBOL_COLUMN = "militarySymbol";
    private static final String AFFILIATION_COLUMN = "affiliation";

    private long startTime;
    private long stopTime;
    private final Map<Integer, EntityDetail> entityDetailMap = new HashMap<>();

    public CutSheetFile(File file) {
        super(file, 3);
        addColumn(ID_COLUMN);
        addColumn(NAME_COLUMN);
        addColumn(ENTITY_TYPE_COLUMN, true);
        addColumn(MILITARY_SYMBOL_COLUMN, true);
        addColumn(AFFILIATION_COLUMN, true);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public EntityDetail getEntityDetail(int id) {
        return entityDetailMap.get(id);
    }

    @Override
    public boolean processFile() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            String line = bufferedReader.readLine();
            String[] lineParts = line.split(",");
            if (lineParts.length < 2) {
                return false;
            }
            startTime = Utility.tryParseTime(lineParts[1]);

            line = bufferedReader.readLine();
            lineParts = line.split(",");
            if (lineParts.length < 2) {
                return false;
            }
            stopTime = Utility.tryParseTime(lineParts[1]);
        } catch (Exception e) {
            String message = String.format("CutSheetFile::processFile:Error processing file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return super.processFile();
    }

    @Override
    protected void processLine(String[] fields) {
        int id = Integer.parseInt(fields[getColumnIndex(ID_COLUMN)]);
        String name = fields[getColumnIndex(NAME_COLUMN)];
        EntityType entityType = EntityType.fromString(fields[getColumnIndex(ENTITY_TYPE_COLUMN)]);
        if (entityType == null) {
            entityType = EntityType.createUnknown();
        }
        String militarySymbol = fields[getColumnIndex(MILITARY_SYMBOL_COLUMN)];
        Affiliation affiliation = Affiliation.fromName(fields[getColumnIndex(AFFILIATION_COLUMN)]);
        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(startTime)
                .setName(name)
                .setSource("IVTS")
                .setEntityType(entityType)
                .setMilitarySymbol(militarySymbol)
                .setAffiliation(affiliation)
                .build();
        entityDetailMap.put(id, entityDetail);
    }
}
