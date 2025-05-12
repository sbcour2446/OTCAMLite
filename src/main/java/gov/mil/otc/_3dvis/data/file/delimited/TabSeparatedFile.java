package gov.mil.otc._3dvis.data.file.delimited;

import java.io.File;

public abstract class TabSeparatedFile extends DelimitedFile {

    protected TabSeparatedFile(File file) {
        super(file);
    }

    @Override
    protected String[] getFields(String line) {
        return line.split("\t", -1);
    }
}
