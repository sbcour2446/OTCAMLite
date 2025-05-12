/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * K05.01 is made up of one or more position reports
 *
 * @author hansen
 */
public class K05_01 extends VmfMessage {

    public class PositionReport {

        public Position position;
        public Calendar time;
        public double speed, heading;
        public int locationQuality, locationDerivation, exerciseIndicator;
        public int iffMode1, iffMode2, iffMode3;
        public int urn;
        public int environment, specificType;

        public PositionReport() {
            position = Position.ZERO;
            time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            time.clear();  // make sure the milliseconds field is zero for position reports
            locationQuality = locationDerivation = exerciseIndicator = iffMode1 = iffMode2 = iffMode3 = urn = environment = specificType = 0;
        }

        @Override
        public String toString() {
            String ls = System.lineSeparator();
            String text = "Position Report" + ls;
            text += String.format("  URN: %d%s", urn, ls);
            text += String.format("  Time: %s%s", SDF_LONG.format(time.getTime()), ls);
            text += String.format("  Position: %s%s", position, ls);

            return text;
        }

    }

    ArrayList<PositionReport> reports;

    // 
    public K05_01(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        reports = new ArrayList<>();
        parsedOk = parse(data);
    }

    final boolean parse(VmfDataBuffer data) {

        boolean gri;
        int lat, lon;
        double latitude, longitude, altitude = 0;

        do {
            PositionReport report = new PositionReport();
            gri = data.getGri();
            report.urn = data.getInt(24);
            latitude = (double)data.getInt(25) / (16777215.0 / 90.0);
            if (latitude > 90) {
                latitude -= 90.0;
            }
            longitude = (double)data.getInt(26) / (33554431.0 / 180.0);
            if (longitude > 180.0) {
                longitude -= 360.0;
            }
            report.locationDerivation = data.getInt(4);

            if (data.getFpi()) {
                report.locationQuality = data.getInt(4);
            }
            boolean exInd = data.getFpi();
            if (data.getGpi()) {
                report.heading = data.getInt(9);
                report.speed = data.getInt(11);
            }
            if (data.getFpi()) {
                altitude = data.getInt(17) * FEET_TO_METERS;
            }
            if (data.getFpi()) {
                altitude = data.getInt(13) * 25.0 * FEET_TO_METERS;
            }
            report.position = Position.fromDegrees(latitude, longitude, altitude);

            if (data.getGpi()) {
                if (data.getFpi()) {
                    int ModeICode = data.getInt(5);
                }
                if (data.getFpi()) {
                    int ModeIiCode = data.getInt(12);
                }
                if (data.getFpi()) {
                    int ModeIiiCode = data.getInt(12);
                }
            }

            int year, month, day, hour, min, sec, dtgExt;
            if (data.getGpi()) {
                if (header.stdVersion > 7) {
                    year = data.getInt(7);
                    month = data.getInt(4);
                } else if (header.year >= 0) {
                    year = header.year;
                    if (year < 100) {
                        year += 2000;
                    }
                    month = header.month;
                } else {
                    // year and month have not been set, we have to use the year from the collection time
                    year = collectTime.get(Calendar.YEAR);
                    month = collectTime.get(Calendar.MONTH);
                }
                day = data.getInt(5);
                hour = data.getInt(5);
                min = data.getInt(6);
                sec = data.getInt(6);

                if (year > 2000) {
                    report.time.set(year, month, day, hour, min, sec);
                } else {
                }
            }

            report.environment = data.getInt(2);
            if (data.getGpi()) {
                if (data.getFpi()) {
                    report.specificType = data.getInt(12);
                }
                if (data.getFpi()) {
                    report.specificType = data.getInt(12);
                }
                if (data.getFpi()) {
                    report.specificType = data.getInt(12);
                }
                if (data.getFpi()) {
                    report.specificType = data.getInt(12);
                }
            }

            reports.add(report);

        } while (gri);

        return true;
    }

    @Override
    public String toString() {

        String text = header.toString();

        text += System.lineSeparator() + "Position Report";

        for (PositionReport r : reports) {
            text += r.toString();
        }

        return text;
    }

    public ArrayList<PositionReport> getReports() {
        return reports;
    }

}
