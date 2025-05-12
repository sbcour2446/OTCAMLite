/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vmf;

import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;
import static vmf.VmfMessage.SDF_LONG;

/**
 *
 * @author Chris
 */
public class K04_01 extends VmfMessage {

    public class EntityLocation {

        Position position;
        long sn;
        int direction, distance, course, speed, activity, action;
        int landType, airType, surfaceType, subsurfaceType;

        String getText() {
            return position.toString() + CR;
        }
    }

    public class Report {

        public Position position;
        public Calendar time;
        public double speed, heading;
        public int identity, dimension, entityType, subtype, iedStatus;
        public int nationality, entitySize, size, quantity;
        public int urn;
        public String staffComments, unitDesignator;
        ArrayList<EntityLocation> entityLocations = new ArrayList<>();

        public String getSummary() {
//            String s = "";
//            for (Report r : reports) {
//                s += ":" + VmfDictionary.getType(dimension, entityType, subtype);
//            }
            return VmfDictionary.getType(dimension, entityType, subtype);
        }

        public String getText() {
            String text = CR + VmfDictionary.getIdentity(identity);
            text += CR + VmfDictionary.getType(dimension, entityType, subtype);
            text += CR + getIedStatus();
            text += staffComments.isEmpty() ? "" : CR + "Staff Comments: " + staffComments;
            // UNIT DESIGNATOR
            text += unitDesignator.isEmpty() ? "" : CR + "Designator: " + unitDesignator;
            // nationality
            // ENTITY SIZE/MOBILITY
            // quantity
            // size

            for (int i = 0; i < entityLocations.size(); i++) {
                text += CR + "  " + entityLocations.get(i).getText();
            }
            // INTELLIGENCE SUBNET
            // INTELLIGENCE NODE NUMBER
            // INTELLIGENCE ENTITY NUMBER
            return text;
        }

        // 4082 034
        String getIedStatus() {
            String s = CR + "IED Status: ";
            switch (iedStatus) {
                case VmfDataBuffer.NO_INT:
                    return "";
                case 0:
                    return s + "UNEXPLODED";
                case 1:
                    return s + "EXPLODED";
                case 2:
                    return s + "UNEXPLODED OPERATIONAL";
                case 3:
                    return s + "UNEXPLODED RENDERED SAFE";
                default:
                    return s + "UNDEFINED (" + iedStatus + ")";

            }
        }
    }

    ArrayList<Report> reports = new ArrayList<>();
    int observerUrn;
    Calendar observationTime;
    Position observerPosition = Position.ZERO;
    String comments, bodyText;

    public K04_01(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        observationTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        observationTime.clear();
        parsedOk = parse(data);
    }

    @Override
    public String getSummary() {
        String s = super.getSummary();
        if (bodyText.isEmpty()) {
            s += "Observer URN: " + observerUrn + " ";
//        s += "Observer Time: " + SDF_LONG.format(observationTime.getTime()) + " ";
            s += SDF_LONG.format(observationTime.getTime()) + ": ";
            for (Report report : reports) {
                s += report.getSummary();
            }
            s += comments;
        }
        return s;

    }

    @Override
    public String getText() {
        String s = super.getText();
        if (!getHeader().isAck()) {
            if (!bodyText.isEmpty()) {
                return s + CR + bodyText;
            }
            s += CR + "Observer URN: " + observerUrn;
            s += CR + "Observer Time: " + SDF_LONG.format(observationTime.getTime());
            for (Report report : reports) {
                s += report.getText();
            }
        }
        return s;
    }

    private boolean parse(VmfDataBuffer data) {
        switch (header.stdVersion) {
            case VMF_6017C:
            case VMF_6017D:
                return parse_6017D(data);
            default:
                byte[] dataBuff = Arrays.copyOfRange(data.data.array(), data.bytePosition, data.bytePosition + data.numOfBytes);
                bodyText = MsgBodyParser.parseVmf(defaultVmfVersion, 4, 1, dataBuff, verboseDecode).trim();
                return true;
        }
    }

    private boolean parse_6017D(VmfDataBuffer data) {
        boolean fri, gri1, gri2;
        int day, hour, min, sec;
        double latitude, longitude, altitude = 0;
        if (getHeader().isValid() && !getHeader().isAck()) {
            observerUrn = data.getInt(24);
            day = data.getInt(5);
            hour = data.getInt(5);
            min = data.getInt(6);
            sec = data.getFpi() ? data.getInt(6) : 0;
            observationTime.set(header.year, header.month, day, hour, min, sec);
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
                r.dimension = data.getInt(header.stdVersion > VMF_6017A ? 6 : 5);
                r.entityType = data.getInt(6);
                r.subtype = data.getFpiInt(6);
                if (header.stdVersion > VMF_6017A) {
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
                        if (header.stdVersion > VMF_6017A) {
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

}
