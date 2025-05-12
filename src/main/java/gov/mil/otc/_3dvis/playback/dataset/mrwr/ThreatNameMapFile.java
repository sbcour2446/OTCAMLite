package gov.mil.otc._3dvis.playback.dataset.mrwr;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.project.mrwr.WeaponSystemResponse;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreatNameMapFile extends CsvFile {

    public static final String NAME = "threatNameMapFile.csv";

    protected ThreatNameMapFile(File file) {
        super(file);

        addColumn("id");
        addColumn("name");
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            int id = Integer.parseInt(fields[getColumnIndex("id")]);
            String name = fields[getColumnIndex("name")];
            WeaponSystemResponse.setWeaponSystemName(id, name);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "ThreatNameMapFile::processLine", e);
        }
    }
}
