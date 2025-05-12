package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WeaponSystemResponseFile extends CsvFile {

    //302-21:10:03.000
    private static final String TIME_FORMAT = "yyyyDDD-HH:mm:ss.SSS";
    private static final String TIME_COLUMN = "Time";
    private static final String X_AOA_M627T02 = "_AOA_M627T02";
    private static final String X_AOA_VALID_M627T0 = "_AOA_VALID_M627T02";
    private static final String X_WPN_SYS_ID1_M627T02 = "_WPN_SYS_ID1_M627T02";
    private final List<WeaponSystemResponse> weaponSystemResponseList = new ArrayList<>();
    private boolean hasValidThreat = false;

    public WeaponSystemResponseFile(File file) {
        super(file);

        addColumn(TIME_COLUMN);

        String columnName;
        for (int i = 1; i <= WeaponSystemResponse.NUMBER_OF_MESSAGES; i++) {
            columnName = i + X_AOA_M627T02;
            addColumn(new ColumnInfo(columnName, true, true));
            columnName = i + X_AOA_VALID_M627T0;
            addColumn(new ColumnInfo(columnName, true, true));
            columnName = i + X_WPN_SYS_ID1_M627T02;
            addColumn(new ColumnInfo(columnName, true, true));
        }
    }

    public List<WeaponSystemResponse> getWeaponSystemResponseList() {
        return weaponSystemResponseList;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestampString = "2024" + fields[getColumnIndex(TIME_COLUMN)];
            long timestamp = Utility.parseTime(timestampString, TIME_FORMAT);

            if (timestamp <= 0) {
                return;
            }

            Map<Integer, WeaponSystemResponse.WeaponSystemResponseMessage> weaponSystemResponseMessageMap = new TreeMap<>();
            for (int i = 1; i <= WeaponSystemResponse.NUMBER_OF_MESSAGES; i++) {
                String azColumnName = i + X_AOA_M627T02;
                String vaColumnName = i + X_AOA_VALID_M627T0;
                String idColumnName = i + X_WPN_SYS_ID1_M627T02;
                ColumnInfo azColumnInfo = getColumn(azColumnName);
                ColumnInfo vaColumnInfo = getColumn(vaColumnName);
                ColumnInfo idColumnInfo = getColumn(idColumnName);
                if (azColumnInfo == null || azColumnInfo.getIndex() < 0 ||
                        vaColumnInfo == null || vaColumnInfo.getIndex() < 0 ||
                        idColumnInfo == null || idColumnInfo.getIndex() < 0) {
                    continue;
                }
                String azValue = fields[azColumnInfo.getIndex()];
                String vaValue = fields[vaColumnInfo.getIndex()];
                String idValue = fields[idColumnInfo.getIndex()];

                if (vaValue.isBlank() || azValue.isBlank()) {
                    continue;
                }

                int valid = Integer.parseInt(vaValue);
                if (valid != 1) {
                    continue;
                }

                double azimuth = Double.parseDouble(azValue);
                int wpnId = idValue.isBlank() ? 0 : Integer.parseInt(idValue);

                weaponSystemResponseMessageMap.put(i, new WeaponSystemResponse.WeaponSystemResponseMessage(azimuth, wpnId));
                hasValidThreat = true;
            }
            if (hasValidThreat) {
                weaponSystemResponseList.add(new WeaponSystemResponse(timestamp, weaponSystemResponseMessageMap));
                if (weaponSystemResponseMessageMap.isEmpty()) {
                    hasValidThreat = false;
                }
            }
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
