package gov.mil.otc._3dvis.data.file.delimited;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class SpaceSeparatedFile extends DelimitedFile {

    protected SpaceSeparatedFile(File file) {
        super(file);
    }

    @Override
    protected String[] getFields(String line) {
        List<String> validFields = new ArrayList<>();
        String[] fields = line.split(" ", -1);
        for (String field : fields) {
            if (!field.isBlank()) {
                validFields.add(field);
            }
        }
        return validFields.toArray(new String[0]);
    }
}
