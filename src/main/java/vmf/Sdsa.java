/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author hansen
 */
public class Sdsa extends VmfMessage {

    public class Record {

        public int urn, sysClass, userClass;
        public int dimension, entityType, entitySubtype, size;
        public String symbol, fullName, shortName, alias, platformType;
        public Calendar time;

        public Record() {
            urn = sysClass = userClass = -1;
            dimension = entityType = entitySubtype = size = -1;
            symbol = fullName = shortName = alias = platformType = "";
            time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            time.clear();
        }

    }

    ArrayList<Record> records = new ArrayList<>();

    public Sdsa(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        parsedOk = parse(data);
    }

    final boolean parse(VmfDataBuffer data) {

        boolean gri, gri1, gri2;
        data.getInt(3);
        if (data.getGpi()) {
        }
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
                }
                if (data.getFpi()) {
                    rec.userClass = data.getInt(4);
                }
                if (data.getFpi()) {
                    rec.fullName = data.getString(448);
                }
                if (data.getFpi()) {
                    rec.shortName = data.getString(210);
                }
                if (data.getFpi()) {
                    String s = data.getString(7);
                    printf("SERVICE/AGENCY = %s\n", s);
                }
                if (data.getFpi()) {
                    printf("Nationality = %d\n", data.getInt(9));
                }
                if (data.getFpi()) {
                    printf("UNIT URN/OPCON URN = %d\n", data.getInt(24));
                }
                if (data.getFpi()) {
                    printf("ADCON URN = %d\n", data.getInt(24));
                }
                if (data.getFpi()) {
                    printf("CONVOY OR PATROL GROUP URN = %d\n", data.getInt(24));
                }
                if (data.getFpi()) {
                    printf("CONVOY OR PATROL LEADER URN = %d\n", data.getInt(24));
                }
                printf("URN TYPE = %d\n", data.getInt(2));
                if (data.getFpi()) {
                    printf("SYSTEM TYPE = %d\n", data.getInt(6));
                }
                if (data.getFpi()) {
                    printf("ROLE CODE = %d\n", data.getInt(5));
                }
                if (data.getFpi()) {
//                    String s = data.getString(105);
                    StringBuilder s = new StringBuilder(data.getString(105));
                    printf("SYMBOL CODE = %s\n", s);
                    if (s.length() == 15) {
                        if (s.charAt(3) == '*') {
                            s.setCharAt(3, 'P');
                        }
                        char order = s.charAt(14);
                        switch (order) {
                            case '-':
                            case 'A':
                            case 'E':
                            case 'C':
                            case 'G':
                            case 'N':
                            case 'S':
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
                    rec.dimension = data.getInt(5);
                    rec.entityType = data.getInt(6);
                    if (data.getFpi()) {
                        rec.entitySubtype = data.getInt(6);
                    }
                    if (data.getFpi()) {
                        rec.size = data.getInt(8);
                    }
                    rec.platformType = VmfDictionary.getType(rec.dimension, rec.entityType, rec.entitySubtype);

                }

                if (data.getFpi()) {
                    String s = data.getString(105);
                    printf("Alias = %s\n", s);
                }

                if (data.getFpi()) {
                    int platformType = data.getInt(8);
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
                            printf("MCG = %s", ipString);
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

    @Override
    public String getText() {

        String text = header.getShortText();

        return text;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

}
