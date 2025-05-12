package gov.mil.otc._3dvis.vmf;

import gov.nasa.worldwind.geom.Position;
import vmf.MsgBodyParser;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * The VMF K04.1 Observation Report Class
 */
public class K0401 extends VmfMessage {

    private static int defaultVmfVersion = 15;
    private final List<Report> reports = new ArrayList<>();
    private int observerUrn;
    private final Calendar observationTime;
    private Position observerPosition = Position.ZERO;
    private String comments;
    private String bodyText;

    /**
     * {@inheritDoc}
     */
    protected K0401(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
        observationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        observationTime.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        switch (header.getStdVersion()) {
            case VMF_6017C:
            case VMF_6017D:
                return parse6017D(data);
            default:
                byte[] dataBuff = Arrays.copyOfRange(data.data.array(), data.bytePosition, data.bytePosition + data.numOfBytes);
                bodyText = MsgBodyParser.parseVmf(defaultVmfVersion, 4, 1, dataBuff, false).trim();
                return true;
        }
    }

    /**
     * 6017D Message Parser
     *
     * @param data VmfDataBuffer data
     * @return true if successful parse otherwise, false
     */
    private boolean parse6017D(VmfDataBuffer data) {
        boolean fri;
        boolean gri1;
        boolean gri2;
        int day;
        int hour;
        int min;
        int sec;
        double latitude;
        double longitude;
        double altitude = 0;

        if (getHeader().isValid() && !getHeader().isAck()) {
            observerUrn = data.getInt(24);
            day = data.getInt(5);
            hour = data.getInt(5);
            min = data.getInt(6);
            sec = data.getFpi() ? data.getInt(6) : 0;
            observationTime.set(header.getYear(), header.getMonth(), day, hour, min, sec);
            if (data.getGpi()) {
                latitude = data.getInt(25) * LAT_CONVERSION_25BIT;
                longitude = data.getInt(26) * LON_CONVERSION_26BIT;
                if (longitude >= 180) {
                    longitude -= 360.0;
                }
                if (data.getFpi()) {
                    altitude = data.getInt(17) * FEET_TO_METERS;
                }
                if (data.getFpi()) {
                    altitude = data.getInt(13) * 25.0 * FEET_TO_METERS;
                }
                observerPosition = Position.fromDegrees(latitude, longitude, altitude);
            }

            do {
                gri1 = data.getGri();
                Report r = new Report();
                reports.add(r);
                r.identity = data.getInt(4);
                r.dimension = data.getInt(header.getStdVersion() > VMF_6017A ? 6 : 5);
                r.entityType = data.getInt(6);
                r.subtype = data.getFpiInt(6);
                if (header.getStdVersion() > VMF_6017A) {
                    r.iedStatus = data.getFpiInt(2);
                }
                r.staffComments = data.getFpiString(140);
                r.unitDesignator = data.getFpiString(175);
                r.nationality = data.getFpiInt(9);
                r.entitySize = data.getFpiInt(8);
                r.quantity = data.getFpiInt(14);
                r.size = data.getFpiInt(5);

                do {
                    gri2 = data.getGri();
                    EntityLocation loc = new EntityLocation();
                    r.entityLocations.add(loc);
                    loc.sn = data.getInt(32);
                    if (data.getGpi()) {
                        latitude = data.getInt(23) * LAT_CONVERSION_23BIT;
                        longitude = data.getInt(24) * LON_CONVERSION_24BIT;
                        if (longitude >= 180) {
                            longitude -= 360.0;
                        }
                    } else {
                        longitude = latitude = altitude = 0;
                    }
                    if (data.getFpi()) {
                        altitude = data.getInt(17) * FEET_TO_METERS;
                    }
                    if (data.getFpi()) {
                        altitude = data.getInt(13) * 25.0 * FEET_TO_METERS;
                    }
                    if (latitude > 0) {
                        loc.position = Position.fromDegrees(latitude, longitude, altitude);
                    } else {
                        loc.position = Position.ZERO;
                    }
                    if (data.getGpi()) {
                        loc.direction = data.getInt(9);
                        loc.distance = data.getInt(14);
                    }
                    if (data.getGpi()) {
                        loc.course = data.getInt(9);
                        loc.speed = data.getInt(11);
                    }
                    loc.activity = data.getFpiInt(6);
                    loc.action = data.getFpiInt(6);
                    if (data.getGpi()) {
                        if (header.getStdVersion() > VMF_6017A) {
                            if (data.getGpi()) {
                                data.getInt(1);
                                data.getInt(12);
                            }
                        } else {
                            data.getFpiInt(12); // SPACE SPECIFIC TYPE
                        }
                        loc.airType = data.getFpiInt(12);
                        loc.surfaceType = data.getFpiInt(12);
                        loc.subsurfaceType = data.getFpiInt(12);
                        loc.landType = data.getFpiInt(12);
                    }
                } while (gri2);

            } while (gri1);

            comments = data.getFpiString(1400);

            if (data.getGpi()) {
                data.getInt(14);
                data.getInt(14);
                data.getInt(28);
            }
        }
        return true;
    }

    public static class EntityLocation {

        private Position position;
        private long sn;
        private int direction;
        private int distance;
        private int course;
        private int speed;
        private int activity;
        private int action;
        private int landType;
        private int airType;
        private int surfaceType;
        private int subsurfaceType;

        public Position getPosition() {
            return position;
        }

        public long getSn() {
            return sn;
        }

        public int getDirection() {
            return direction;
        }

        public int getDistance() {
            return distance;
        }

        public int getCourse() {
            return course;
        }

        public int getSpeed() {
            return speed;
        }

        public int getActivity() {
            return activity;
        }

        public int getAction() {
            return action;
        }

        public int getLandType() {
            return landType;
        }

        public int getAirType() {
            return airType;
        }

        public int getSurfaceType() {
            return surfaceType;
        }

        public int getSubsurfaceType() {
            return subsurfaceType;
        }

        String getText() {
            return position.toString() + System.lineSeparator();
        }
    }

    /**
     * The Report Class
     */
    public static class Report {

        private Position position;
        private Calendar time;
        private double speed;
        private double heading;
        private int identity;
        private int dimension;
        private int entityType;
        private int subtype;
        private int iedStatus;
        private int nationality;
        private int entitySize;
        private int size;
        private int quantity;
        private int urn;
        private String staffComments;
        private String unitDesignator;
        private final List<EntityLocation> entityLocations = new ArrayList<>();

        /**
         * Return a printable Summary String
         *
         * @return a printable Summary String
         */
        public String getSummary() {
//            String s = "";
//            for (Report r : reports) {
//                s += ":" + VmfDictionary.getType(dimension, entityType, subtype);
//            }
            return VmfDictionary.getEntityType(dimension, entityType, subtype);
        }

        /**
         * Return a printable Text String
         *
         * @return a printable Text String
         */
        public String getText() {
            String text = System.lineSeparator() + VmfDictionary.getIdentity(identity);
            text += System.lineSeparator() + VmfDictionary.getEntityType(dimension, entityType, subtype);
            text += System.lineSeparator() + getIedStatus();
            text += staffComments.isEmpty() ? "" : System.lineSeparator() + "Staff Comments: " + staffComments;
            // UNIT DESIGNATOR
            text += unitDesignator.isEmpty() ? "" : System.lineSeparator() + "Designator: " + unitDesignator;
            // nationality
            // ENTITY SIZE/MOBILITY
            // quantity
            // size

            for (EntityLocation entityLocation : entityLocations) {
                text += System.lineSeparator() + "  " + entityLocation.getText();
            }
            // INTELLIGENCE SUBNET
            // INTELLIGENCE NODE NUMBER
            // INTELLIGENCE ENTITY NUMBER
            return text;
        }

        /**
         * Return a printable IED Status String (DFI/DUI 4082 034)
         *
         * @return a printable IED Status String
         */
        String getIedStatus() {
            String s = System.lineSeparator() + "IED Status: ";
            return switch (iedStatus) {
                case VmfDataBuffer.NO_INT -> "";
                case 0 -> s + "UNEXPLODED";
                case 1 -> s + "EXPLODED";
                case 2 -> s + "UNEXPLODED OPERATIONAL";
                case 3 -> s + "UNEXPLODED RENDERED SAFE";
                default -> s + "UNDEFINED (" + iedStatus + ")";
            };
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = super.getSummary();
        if (bodyText.isEmpty()) {
            s += "Observer URN: " + observerUrn + " ";
//        s += "Observer Time: " + SDF_LONG.format(observationTime.getTime()) + " ";
            s += sdfLong.format(observationTime.getTime()) + ": ";
            for (Report report : reports) {
                s += report.getSummary();
            }
            s += comments;
        }
        return s;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = super.getText();
        if (!getHeader().isAck()) {
            if (!bodyText.isEmpty()) {
                return s + System.lineSeparator() + bodyText;
            }
            s += System.lineSeparator() + "Observer URN: " + observerUrn;
            s += System.lineSeparator() + "Observer Time: " + sdfLong.format(observationTime.getTime());
            for (Report report : reports) {
                s += report.getText();
            }
        }
        return s;
    }
}
