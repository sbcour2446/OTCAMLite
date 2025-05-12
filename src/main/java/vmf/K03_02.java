/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import static vmf.VmfDataBuffer.NO_INT;

/**
 * K03.02 ENGAGEMENT REPORT/BATTLE DAMAGE ASSESSMENT
 *
 * @author hansen
 */
public class K03_02 extends VmfMessage {

    public class EntityData {

        Position position;
        int urn;
        long sn;
        String targetNumber;
        Calendar time;
        int munitionsType, numberMunitions, percentDamaged, fireSupportEffectAchieved;

        public EntityData() {
            position = Position.ZERO;
            time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            munitionsType = numberMunitions = percentDamaged = fireSupportEffectAchieved = NO_INT;
        }

    }

    public class Report {

        int vmfIdentity, dimension, type, subtype;
        ArrayList<EntityData> entityData;

        public Report() {
            entityData = new ArrayList<>();
            subtype = NO_INT;
        }

    }

    int reportType;
    ArrayList<Report> reports = new ArrayList<>();

    public K03_02(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        parsedOk = parse(data);
    }

    private boolean parse(VmfDataBuffer data) {
        boolean gri1, gri2;
        int day, hour, min, sec;
        double latitude, longitude, altitude;
        try {
            if (getHeader().isValid() && !getHeader().isAck()) {
                reportType = data.getInt(1);
                do {
                    gri1 = data.getGri();
                    Report r = new Report();
                    r.vmfIdentity = data.getInt(4);
                    r.dimension = data.getInt(header.stdVersion < VMF_6017B ? 5 : 6);
                    r.type = data.getInt(6);
                    r.subtype = data.getFpiInt(6);
                    do {
                        gri2 = data.getGri();
                        EntityData d = new EntityData();
                        latitude = data.getInt(25) * LAT_CONVERSION_25BIT;
                        longitude = data.getInt(26) * LON_CONVERSION_26BIT;
                        longitude -= (longitude >= 180) ? 360.0 : 0;
                        altitude = 0;
                        if (data.getFpi()) {
                            altitude = data.getInt(17) * FEET_TO_METERS;
                        }
                        if (data.getFpi()) {
                            altitude = data.getInt(13) * 25.0 * FEET_TO_METERS;
                        }
                        d.position = Position.fromDegrees(latitude, longitude, altitude);
                        d.urn = data.getInt(24);
                        d.sn = data.getInt(32);
                        d.targetNumber = data.getFpiString(28);
                        day = data.getInt(5);
                        hour = data.getInt(5);
                        min = data.getInt(6);
                        sec = data.getFpi() ? data.getInt(6) : 0;
                        d.time.set(header.year, header.month, day, hour, min, sec);
                        // G1
                        if (data.getGpi()) {
                            d.munitionsType = data.getInt(9);
                            d.numberMunitions = data.getFpiInt(14);
                        }
                        // G2
                        if (data.getGpi()) {
                            d.percentDamaged = data.getFpiInt(7);
                            d.fireSupportEffectAchieved = data.getFpiInt(4);
                        }
                    } while (gri2);

                } while (gri1);
            }
            return true;
        } catch (Exception ex) {
        }
        return false;
    }

    @Override
    public String getSummary() {
        if (getHeader().isAck()) {
            return getHeader().getAckText();
        }
        switch (reportType) {
            case 1:
                return "BATTLE DAMAGE ASSESSMENT";
            case 0:
                return "ENGAGEMENT REPORT";
            default:
                return "ERROR";
        }

    }

    @Override
    public String getText() {
        if (getHeader().isAck()) {
            return "ACK to " + super.getSummary();
        }
        String s = super.getText();
        for (Report r : reports) {
            s += CR + VmfDictionary.getIdentity(r.vmfIdentity);
            s += CR + VmfDictionary.getType(r.dimension, r.type, r.subtype);
            for (EntityData e : r.entityData) {
                s += CR + "  URN: " + e.urn;
                s += CR + "  SN: " + e.sn;
                if (!e.targetNumber.isEmpty()) {
                    s += CR + "  TGT NUM: " + e.targetNumber;
                }

                s += CR + "  TIME: " + SDF_LONG.format(e.time.getTime());

                if (e.munitionsType != NO_INT) {
                    s += CR + "  Munitions: " + VmfDictionary.getMunitionsType(e.munitionsType);
                }
                if (e.numberMunitions != NO_INT) {
                    s += CR + "  Number: " + SDF_LONG.format(e.numberMunitions);
                }

                if (e.percentDamaged != NO_INT) {
                    s += CR + "  Percent Damaged: " + e.percentDamaged;
                }
                if (e.fireSupportEffectAchieved != NO_INT) {
                    s += CR + "  Effect Achieved: " + VmfDictionary.getEffectAchieved(e.fireSupportEffectAchieved);
                }

            }
        }

        return s;
    }

}
