package gov.mil.otc._3dvis.data.jbcp;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.data.jbcp.UtoRecord;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV file for the Unit Table of Organization (UTO) content.
 */
public class UtoFile extends CsvFile {

    /**
     * Expected column count in the file.
     */
    private static final int EXPECTED_COLUMN_COUNT = 26;

    /**
     * The column index representing the URN.
     */
    private static final int URN_COLUMN = 1;

    /**
     * The column index representing the name of the entry.
     */
    private static final int NAME_COLUMN = 3;

    /**
     * The column index representing the MIL-STD 2525 symbol code.
     */
    private static final int SYMBOL_CODE_COLUMN = 8;

    /**
     * The list of UTPRecords.
     */
    private final List<UtoRecord> utoRecordList = new ArrayList<>();

    public UtoFile(File file) {
        super(file);

        hasHeader = false;
    }

    public List<UtoRecord> getUtoRecordList() {
        return utoRecordList;
    }

    public UtoRecord getUtoRecord(int urn) {
        for (UtoRecord utoRecord : utoRecordList) {
            if (utoRecord.getUrn() == urn) {
                return utoRecord;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processLine(String[] fields) {
        if (fields == null || fields.length != EXPECTED_COLUMN_COUNT) {
            return;
        }

        Integer urn;
        try {
            urn = Integer.parseInt(sanitizeString(fields[URN_COLUMN]));
        } catch (Exception e) {
            urn = null;
        }

        String name = sanitizeString(fields[NAME_COLUMN]);
        String milStd2525Symbol = sanitizeString(fields[SYMBOL_CODE_COLUMN]);

        if (urn != null) {
            utoRecordList.add(new UtoRecord(urn, name, milStd2525Symbol));
        }
    }
}
