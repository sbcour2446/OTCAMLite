package gov.mil.otc._3dvis.data.stanag;

import gov.mil.otc._3dvis.data.file.ImportFile;

import java.io.File;

public class StanagFileFactory {

    public static ImportFile getStanagImportFile(File file) {
        if (file.getName().toLowerCase().startsWith("a101")) {
            return new A101File(file);
        } else if (file.getName().toLowerCase().startsWith("a302")) {
            return new A302File(file);
        }
        return null;
    }
}
