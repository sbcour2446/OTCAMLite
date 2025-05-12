package gov.mil.otc._3dvis.vmf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * The VMF Situational Awareness Message (SDSA) Class
 */
public class Sdsa extends VmfMessage {

    private final List<Record> records = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    protected Sdsa(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        boolean gri;
        boolean gri1;
        data.getInt(3);
        data.getGpi();
        do {
            Record rec = new Record();
            gri = data.getGri();
            rec.urn = data.getInt(24);
            if (data.getGpi()) {
                rec.time.set(data.getInt(7), data.getInt(4), data.getInt(5), data.getInt(5), data.getInt(6), data.getInt(6));
                if (data.getGpi()) {
                    data.getInt(4);
                    data.getInt(4);
                }
                if (data.getFpi()) {
                    rec.sysClass = data.getInt(4);
                    printf("System Classification = %d - %s", rec.sysClass, VmfDictionary.getClassification(rec.sysClass));
                }
                if (data.getFpi()) {
                    rec.userClass = data.getInt(4);
                    printf("User Classification = %d - %s", rec.userClass, VmfDictionary.getClassification(rec.userClass));
                }
                if (data.getFpi()) {
                    rec.fullName = data.getString(448);
                    printf("Full Name = %s", rec.fullName);
                }
                if (data.getFpi()) {
                    rec.shortName = data.getString(210);
                    printf("Short Name = %s", rec.shortName);
                }
                if (data.getFpi()) {
                    String s = data.getString(7);
                    printf("Service/Agency = %s - %s", s, VmfDictionary.getService(s));
                }
                if (data.getFpi()) {
                    rec.nationality = data.getInt(9);
                    printf("Nationality = %d - %s", rec.nationality, VmfDictionary.getNationality(rec.nationality));
                }
                if (data.getFpi()) {
                    printf("Unit URN/OPCON URN = %d", data.getInt(24));
                }
                if (data.getFpi()) {
                    printf("ADCON URN = %d", data.getInt(24));
                }
                if (data.getFpi()) {
                    printf("Convoy or Patrol Group URN = %d", data.getInt(24));
                }
                if (data.getFpi()) {
                    printf("Convoy or Patrol Leader URN = %d", data.getInt(24));
                }
                int urnType = data.getInt(2);
                printf("URN Type = %d - %s", urnType, VmfDictionary.getUrnType(urnType));

                if (data.getFpi()) {
                    int systemType = data.getInt(6);
                    printf("System Type = %d - %s", systemType, VmfDictionary.getSystemType(systemType));
                }
                if (data.getFpi()) {
                    int roleCode = data.getInt(5);
                    printf("Role Code = %d - %s", roleCode, VmfDictionary.getRoleCode(roleCode));
                }
                if (data.getFpi()) {
                    StringBuilder s = new StringBuilder(data.getString(105));
                    printf("Symbol Code = %s", s);
                    if (s.length() == 15) {
                        if (s.charAt(3) == '*') {
                            s.setCharAt(3, 'P');
                        }
                        char order = s.charAt(14);
                        switch (order) {
                            case '-', 'A', 'E', 'C', 'G', 'N', 'S':
                                break;
                            default:
                                s.setCharAt(14, '-');
                                break;
                        }
                        rec.symbol = s.toString();
                    } else {
                        rec.symbol = "SFGP--------USG";
                    }

                    rec.symbol = rec.symbol.replaceAll("\\*", "-");
                }

                if (data.getGpi()) { // G4
                    rec.dimension = data.getInt(5); // DFI/DUI: 4173/014
                    printf("Dimension = %d", rec.dimension);

                    rec.type = data.getInt(6); // DFI/DUI: 4173/015
                    printf("Type = %d", rec.type);

                    if (data.getFpi()) {
                        rec.subType = data.getInt(6); // DFI/DUI: 4173/016
                        printf("Subtype = %d", rec.subType);
                    }

                    if (data.getFpi()) {
                        rec.size = data.getInt(8); // DFI/DUI: 4173/018
                        printf("Size = %d", rec.size);
                    }

                    rec.platformType = VmfDictionary.getEntityType(rec.dimension, rec.type, rec.subType);
                    printf("Entity Type = %s", rec.platformType);
                }

                if (data.getFpi()) {
                    String s = data.getString(105);
                    printf("Alias = %s", s);
                }

                if (data.getFpi()) { // Indicates which operation in device identification process to be performed.
                    int platformType = data.getInt(8); // DFI/DUI 4093/069 Now: 9100/009
                    printf("Device Identifier Designator = %d", platformType);
                }

                if (data.getGpi()) { // IPV4 PLATFORM IP G5
                    String ipString = String.format("%d.%d.%d.%d",
                            data.getInt(8), data.getInt(8), data.getInt(8), data.getInt(8));
                    printf("IP = %s", ipString);
                }

                if (data.getGpi()) { // IPV6 PLATFORM IP G6
                    for (int i = 0; i < 8; i++) {
                        data.getInt(8);
                    }
                }

                if (data.getGpi()) { // MCG IP ADDRESS G7
                    do {
                        gri1 = data.getGri();
                        if (data.getGpi()) { //
                            String ipString = String.format("%d.%d.%d.%d",
                                    data.getInt(8), data.getInt(8), data.getInt(8), data.getInt(8));
                            printf("Multicast Group (MCG) = %s", ipString);
                        }

                        if (data.getGpi()) { // IPV6 PLATFORM IP G6
                            for (int i = 0; i < 8; i++) {
                                data.getInt(8);
                            }
                        }

                    } while (gri1);
                }

                if (data.getGpi()) { // G10
                    do {
                        gri1 = data.getGri();
                        data.getInt(4);
                        data.getInt(16);
                        data.getInt(16);
                        data.getInt(16);
                        data.getInt(16);

                    } while (gri1);
                }

                if (data.getGpi()) { // G11
                    data.getInt(1);
                    data.getString(56);
                }

                data.skip(64);
            }

            records.add(records.size(), rec);

        } while (gri);

        return true;
    }

    /**
     * Return the SDSA Data Records
     *
     * @return the SDSA Data Records
     */
    public List<Record> getRecords() {
        return records;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return header.getShortText();
    }

    public class Record {

        private int urn;
        private int sysClass;
        private int userClass;
        private int dimension;
        private int type;
        private int subType;
        private int size;
        private int nationality;
        private String symbol;
        private String fullName;
        private String shortName;
        private final String alias;
        private String platformType;
        private final Calendar time;

        public int getUrn() {
            return urn;
        }

        public int getSysClass() {
            return sysClass;
        }

        public int getUserClass() {
            return userClass;
        }

        public int getDimension() {
            return dimension;
        }

        public int getType() {
            return type;
        }

        public int getSubType() {
            return subType;
        }

        public int getSize() {
            return size;
        }

        public int getNationality() {
            return nationality;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getFullName() {
            return fullName;
        }

        public String getShortName() {
            return shortName;
        }

        public String getAlias() {
            return alias;
        }

        public String getPlatformType() {
            return platformType;
        }

        public Calendar getTime() {
            return time;
        }

        public Record() {
            urn = sysClass = userClass = -1;
            dimension = type = subType = size = nationality = -1;
            symbol = fullName = shortName = alias = platformType = "";
            time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            time.clear();
        }
    }
}
