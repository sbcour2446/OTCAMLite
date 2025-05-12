package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvConfiguration;
import gov.mil.otc._3dvis.data.file.delimited.csv.GenericTspiCsvFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApacheTspiFile extends GenericTspiCsvFile {

    private static final String TADS_AZ = "1553-2_TADS_AZ_OUTER_M206T01";
    private static final String TADS_EL = "1553-2_TADS_EL_OUTER_M206T01";
    private static final String TADS_RANGE = "1553-2_TADS_RANGE_M206R01";
    private static final String TADS_FOV = "1553-2_TADS_FOV_STAT_M206T01";

    private final List<TadsStatus> tadsStatusList = new ArrayList<>();

    private boolean containsTads = true;

    public ApacheTspiFile(File file, CsvConfiguration csvConfiguration) {
        super(file, csvConfiguration);

        addColumn(new ColumnInfo(TADS_AZ, true, true));
        addColumn(new ColumnInfo(TADS_EL, true, true));
        addColumn(new ColumnInfo(TADS_RANGE, true, true));
        addColumn(new ColumnInfo(TADS_FOV, true, true));

//        addColumn(TADS_AZ, true);
//        addColumn(TADS_EL, true);
//        addColumn(TADS_RANGE, true);
//        addColumn(TADS_FOV, true);
    }

    public List<TadsStatus> getTadStatusList() {
        return tadsStatusList;
    }

    @Override
    protected void processLine(String[] fields) {
        super.processLine(fields);

        if (!containsTads) {
            return;
        }

        int tadsAzIndex = getColumn(TADS_AZ) != null ? getColumn(TADS_AZ).getIndex() : -1;
        int tadsElIndex = getColumn(TADS_EL) != null ? getColumn(TADS_EL).getIndex() : -1;
        int tadsRangeIndex = getColumn(TADS_RANGE) != null ? getColumn(TADS_RANGE).getIndex() : -1;
        int tadsFovIndex = getColumn(TADS_FOV) != null ? getColumn(TADS_FOV).getIndex() : -1;

        if (tadsAzIndex < 0 || tadsElIndex < 0 || tadsRangeIndex < 0 || tadsFovIndex < 0) {
            containsTads = false;
            return;
        }

        long timestamp = getTimestamp(fields);
        if (timestamp < 0) {
            return;
        }


        try {
            String azimuthString = fields[tadsAzIndex];
            String elevationString = fields[tadsElIndex];
            String rangeString = fields[tadsRangeIndex];
            String fovString = fields[tadsFovIndex];

            if (azimuthString.isBlank() || elevationString.isBlank()) {
                return;
            }
            double azimuth = Double.parseDouble(azimuthString);
            double elevation = Double.parseDouble(elevationString);
            int range = -1;
            int fov = -1;

            if (!rangeString.isBlank()) {
                try {
                    range = Integer.parseInt(rangeString);
                } catch (Exception ex) {
                    //ignore;
                }
            }
            if (!fovString.isBlank()) {
                try {
                    fov = Integer.parseInt(fovString);
                } catch (Exception ex) {
                    //ignore;
                }
            }

            tadsStatusList.add(new TadsStatus(timestamp, azimuth, elevation, range, fov));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "ApacheTspiFile::processLine", e);
        }
    }
}
