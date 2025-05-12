package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UrnMap extends CsvFile {

    private static final String IDENTIFIER_COLUMN = "identifier";
    private static final String URN_COLUMN = "urn";
    private static final String ROLE_NAME_COLUMN = "role";
    private final Map<Integer, UrnAndRole> urnAndRoleMap = new HashMap<>();

    private static class UrnAndRole {

        private final int urn;
        private final String role;

        public UrnAndRole(int urn, String role) {
            this.urn = urn;
            this.role = role;
        }

        public int getUrn() {
            return urn;
        }

        public String getRole() {
            return role;
        }
    }

    public UrnMap(File file) {
        super(file);

        addColumn(IDENTIFIER_COLUMN);
        addColumn(URN_COLUMN);
        addColumn(ROLE_NAME_COLUMN);
    }

    public int getUrn(int identifier) {
        UrnAndRole urnAndRole = urnAndRoleMap.get(identifier);
        if (urnAndRole != null) {
            return urnAndRole.urn;
        } else {
            return 0;
        }
    }

    public String getRole(int identifier) {
        UrnAndRole urnAndRole = urnAndRoleMap.get(identifier);
        if (urnAndRole != null) {
            return urnAndRole.role;
        } else {
            return "";
        }
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            int identifier = Integer.parseInt(fields[getColumnIndex(IDENTIFIER_COLUMN)].trim());
            int urn = Integer.parseInt(fields[getColumnIndex(URN_COLUMN)].trim());
            String roleName = fields[getColumnIndex(ROLE_NAME_COLUMN)].trim();
            urnAndRoleMap.put(identifier, new UrnAndRole(urn, roleName));
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.INFO, message, e);
        }
    }
}
