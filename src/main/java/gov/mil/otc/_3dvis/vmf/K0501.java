package gov.mil.otc._3dvis.vmf;

import gov.nasa.worldwind.geom.Position;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * The VMF K05.01 Position Report Class
 */
public class K0501 extends VmfMessage {

    private final List<PositionReport> reports = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    protected K0501(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        boolean gri;
        double latitude;
        double longitude;
        double altitude = 0;

        do {
            PositionReport report = new PositionReport();
            gri = data.getGri();
            report.urn = data.getInt(24);
            latitude = data.getInt(25) / (16777215.0 / 90.0);
            if (latitude > 90) {
                latitude -= 90.0;
            }
            longitude = data.getInt(26) / (33554431.0 / 180.0);
            if (longitude > 180.0) {
                longitude -= 360.0;
            }
            report.locationDerivation = data.getInt(4);
            printf("Location Derivation = %d", report.locationDerivation);

            if (data.getFpi()) {
                report.locationQuality = data.getInt(4);
                printf("Location Quality = %d - %s", report.locationQuality, VmfDictionary.getPositionQuality(report.locationQuality));
            }
            boolean exInd = data.getFpi();
            if (data.getGpi()) {
                report.heading = data.getInt(9);
                printf("Heading = %f", report.heading);

                report.speed = data.getInt(11);
                printf("Speed = %f", report.speed);
            }
            if (data.getFpi()) {
                altitude = data.getInt(17) * FEET_TO_METERS;
                printf("Altitude(m) = %f", altitude);
            }
            if (data.getFpi()) {
                altitude = data.getInt(13) * 25.0 * FEET_TO_METERS;
                printf("Altitude(m) = %f", altitude);
            }
            report.position = Position.fromDegrees(latitude, longitude, altitude);
            printf("Position = %f. %f, %f", latitude, longitude, altitude);

            if (data.getGpi()) {
                if (data.getFpi()) {
                    int ModeICode = data.getInt(5);
                    printf("Mode I Code = %d", ModeICode);
                }
                if (data.getFpi()) {
                    int ModeIiCode = data.getInt(12);
                    printf("Mode II Code = %d", ModeIiCode);
                }
                if (data.getFpi()) {
                    int ModeIiiCode = data.getInt(12);
                    printf("Mode III Code = %d", ModeIiiCode);
                }
            }

            int year;
            int month;
            int day;
            int hour;
            int min;
            int sec;
            int dtgExt;

            if (data.getGpi()) {
                if (header.getStdVersion() > 7) {
                    year = data.getInt(7);
                    month = data.getInt(4);
                } else if (header.getYear() >= 0) {
                    year = header.getYear();
                    if (year < 100) {
                        year += 2000;
                    }
                    month = header.getMonth();
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
                }
                printf("Report Time = %d-%d-%d %d:%d:%d", year, month, day, hour, min, sec);
            }

            report.environment = data.getInt(2);
            String sEnvironment = switch(report.environment) {
                case 0 -> "Surface";
                case 1 -> "Sub Surface";
                case 2 -> "Land";
                case 3 -> "Air";
                default -> "";
            };
            printf("Environment = %d - %s", report.environment, sEnvironment);

            if (data.getGpi()) {
                if (data.getFpi()) {
                    report.specificType = data.getInt(12);
                    printf("Air Specific Type (see DFI 804/001) = %d", report.specificType);
                }
                if (data.getFpi()) {
                    report.specificType = data.getInt(12);
                    printf("Surface Specific Type (see DFI 808/001) = %d", report.specificType);
                }
                if (data.getFpi()) {
                    report.specificType = data.getInt(12);
                    printf("Sub Surface Specific Type (see DFI 809/001) = %d", report.specificType);
                }
                if (data.getFpi()) {
                    report.specificType = data.getInt(12);
                    printf("Land Specific Type (see DFI 810/001) = %d", report.specificType);
                }
            }

            reports.add(report);

        } while (gri);

        return true;
    }

    public static class PositionReport {

        private Position position;
        private final Calendar time;
        private double speed;
        private double heading;
        private int locationQuality;
        private int locationDerivation;
        private final int exerciseIndicator;
        private final int iffMode1;
        private final int iffMode2;
        private final int iffMode3;
        private int urn;
        private int environment;
        private int specificType;

        public Position getPosition() {
            return position;
        }

        public Calendar getTime() {
            return time;
        }

        public double getSpeed() {
            return speed;
        }

        public double getHeading() {
            return heading;
        }

        public int getLocationQuality() {
            return locationQuality;
        }

        public int getLocationDerivation() {
            return locationDerivation;
        }

        public int getExerciseIndicator() {
            return exerciseIndicator;
        }

        public int getIffMode1() {
            return iffMode1;
        }

        public int getIffMode2() {
            return iffMode2;
        }

        public int getIffMode3() {
            return iffMode3;
        }

        public int getUrn() {
            return urn;
        }

        public int getEnvironment() {
            return environment;
        }

        public int getSpecificType() {
            return specificType;
        }

        public PositionReport() {
            position = Position.ZERO;
            time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            time.clear();  // make sure the milliseconds field is zero for position reports
            locationQuality = locationDerivation = exerciseIndicator = iffMode1 = iffMode2 = iffMode3 = urn = environment = specificType = 0;
        }

        @Override
        public String toString() {
            SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return String.format("K05.01 Position Report for URN: %d Time: %s Position: %s",
                    urn,
                    sdfLong.format(time.getTime()),
                    position);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String text = header.toString();

        text += System.lineSeparator() + "Position Report";

        for (PositionReport r : reports) {
            text += r.toString();
        }

        return text;
    }

    /**
     * Return the K05.01 Position Reports
     *
     * @return the K05.01 Position Reports
     */
    public List<PositionReport> getReports() {
        return reports;
    }
}
