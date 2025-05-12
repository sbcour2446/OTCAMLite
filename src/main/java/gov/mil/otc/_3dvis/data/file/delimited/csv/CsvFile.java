package gov.mil.otc._3dvis.data.file.delimited.csv;

import gov.mil.otc._3dvis.data.file.delimited.DelimitedFile;

import java.io.File;

public abstract class CsvFile extends DelimitedFile {

    protected CsvFile(File file) {
        this(file, 1);
    }

    protected CsvFile(File file, int headerLineNumber) {
        super(file, headerLineNumber);
    }

    @Override
    protected String[] getFields(String line) {
//        return line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        return line.split(",", -1);
    }
}
