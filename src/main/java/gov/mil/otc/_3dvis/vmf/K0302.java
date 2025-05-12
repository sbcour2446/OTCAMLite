package gov.mil.otc._3dvis.vmf;

import gov.nasa.worldwind.geom.Position;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static gov.mil.otc._3dvis.vmf.VmfDataBuffer.NO_INT;

/**
 * The VMF K03.02 Engagement Report/Battle Damage Assessment Class
 */
public class K0302 extends VmfMessage {

    private final List<Report> reports = new ArrayList<>();
    private int reportType;

    /**
     * {@inheritDoc}
     */
    protected K0302(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        boolean gri1;
        boolean gri2;
        int day;
        int hour;
        int min;
        int sec;
        double latitude;
        double longitude;
        double altitude;
        try {
            if (getHeader().isValid() && !getHeader().isAck()) {
                reportType = data.getInt(1);
                do {
                    gri1 = data.getGri();
                    Report r = new Report();
                    r.vmfIdentity = data.getInt(4);
                    r.dimension = data.getInt(header.getStdVersion() < VMF_6017B ? 5 : 6);
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
                        d.time.set(header.getYear(), header.getMonth(), day, hour, min, sec);
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
        } catch (Exception e) {
            Logger.getGlobal().log(Level.INFO, null, e);
        }
        return false;
    }

    public List<Report> getReports() {
        return reports;
    }

    public int getReportType() {
        return reportType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        if (getHeader().isAck()) {
            return getHeader().getAckText();
        }
        return switch (reportType) {
            case 1 -> "BATTLE DAMAGE ASSESSMENT";
            case 0 -> "ENGAGEMENT REPORT";
            default -> "ERROR";
        };

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        if (getHeader().isAck()) {
            return "ACK to " + super.getSummary();
        }
        SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String s = super.getText();
        for (Report r : reports) {
            s += System.lineSeparator() + VmfDictionary.getIdentity(r.vmfIdentity);
            s += System.lineSeparator() + VmfDictionary.getEntityType(r.dimension, r.type, r.subtype);
            for (EntityData e : r.entityData) {
                s += System.lineSeparator() + "  URN: " + e.urn;
                s += System.lineSeparator() + "  SN: " + e.sn;
                if (!e.targetNumber.isEmpty()) {
                    s += System.lineSeparator() + "  TGT NUM: " + e.targetNumber;
                }

                s += System.lineSeparator() + "  TIME: " + sdfLong.format(e.time.getTime());

                if (e.munitionsType != NO_INT) {
                    s += System.lineSeparator() + "  Munitions: " + VmfDictionary.getMunitionsType(e.munitionsType);
                }
                if (e.numberMunitions != NO_INT) {
                    s += System.lineSeparator() + "  Number: " + sdfLong.format(e.numberMunitions);
                }

                if (e.percentDamaged != NO_INT) {
                    s += System.lineSeparator() + "  Percent Damaged: " + e.percentDamaged;
                }
                if (e.fireSupportEffectAchieved != NO_INT) {
                    s += System.lineSeparator() + "  Effect Achieved: " + VmfDictionary.getEffectAchieved(e.fireSupportEffectAchieved);
                }

            }
        }
        return s;
    }

    public class EntityData {

        private Position position;
        private int urn;
        private long sn;
        private String targetNumber;
        private final Calendar time;
        private int munitionsType;
        private int numberMunitions;
        private int percentDamaged;
        private int fireSupportEffectAchieved;

        public Position getPosition() {
            return position;
        }

        public int getUrn() {
            return urn;
        }

        public long getSn() {
            return sn;
        }

        public String getTargetNumber() {
            return targetNumber;
        }

        public Calendar getTime() {
            return time;
        }

        public int getMunitionsType() {
            return munitionsType;
        }

        public int getNumberMunitions() {
            return numberMunitions;
        }

        public int getPercentDamaged() {
            return percentDamaged;
        }

        public int getFireSupportEffectAchieved() {
            return fireSupportEffectAchieved;
        }

        public EntityData() {
            position = Position.ZERO;
            time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            munitionsType = numberMunitions = percentDamaged = fireSupportEffectAchieved = NO_INT;
        }

    }

    /**
     * The Report Class
     */
    public class Report {

        private int vmfIdentity;
        private int dimension;
        private int type;
        private int subtype;
        private final List<EntityData> entityData;

        public Report() {
            entityData = new ArrayList<>();
            subtype = NO_INT;
        }

        public int getVmfIdentity() {
            return vmfIdentity;
        }

        public int getDimension() {
            return dimension;
        }

        public int getType() {
            return type;
        }

        public int getSubtype() {
            return subtype;
        }

        public List<EntityData> getEntityData() {
            return entityData;
        }
    }
}
