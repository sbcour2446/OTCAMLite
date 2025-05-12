package gov.mil.otc._3dvis.vmf;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The VMF K02.04 Call for Fire Class
 */

public class K0204 extends VmfMessage {

    private int missiontype;
    private String targetNumber;
    private final ArrayList<TargetData> reports = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    protected K0204(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        if (header.getStdVersion() < VMF_6017C) {
            return true;
        } else {
            try {
                missiontype = data.getInt(4);
                targetNumber = data.getString(35);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public int getMissiontype() {
        return missiontype;
    }

    public String getTargetNumber() {
        return targetNumber;
    }

    public List<TargetData> getReports() {
        return reports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return super.getSummary() + ": " + missionTypeString();
    }

    /**
     * Return a Printable Mission Type String
     *
     * @return a Printable Mission Type String
     */
    public String missionTypeString() {
        return switch (missiontype) {
            case 0 -> "GEOGRAPHIC LOCATION";
            case 1 -> "PREVIOUS TARGET";
            case 2 -> "MOVING TARGET, ONE LOCATION";
            case 3 -> "MOVING TARGET, TWO LOCATIONS";
            case 4 -> "MOVING TARGET PREDICTED POINT";
            case 5 -> "FINAL PROTECTIVE FIRES";
            case 6 -> "PRIORITY COPPERHEAD MISSION";
            case 7 -> "ON-CALL TARGET";
            case 8 -> "QUICK SMOKE MISSION";
            case 9 -> "MULTIPLE PRECISION AIMING POINTS";
            default -> "UNDEFINED (" + missiontype + ")";
        };
    }

    /**
     * Return a Printable Mission Type String
     *
     * @return a Printable Mission Type String
     */
    public String getMissionType() {
        return System.lineSeparator() + "Mission Type: " + missionTypeString();
    }

    /**
     * Target Data Class
     */
    public static class TargetData {
    }
}
