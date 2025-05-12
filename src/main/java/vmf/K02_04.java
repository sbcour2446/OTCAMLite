/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author hansen
 */
public class K02_04 extends VmfMessage {

    int missiontype;
    String targetNumber;
    ArrayList<TargetData> reports = new ArrayList<>();

    public K02_04(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        parsedOk = parse(data);
    }

    @Override
    public String getSummary() {
        return super.getSummary() + ": " + missionType();
    }

    @Override
    public String getText() {
        return super.getText();
    }

    String missionType() {
        switch (missiontype) {
            case 0:
                return "GEOGRAPHIC LOCATION";
            case 1:
                return "PREVIOUS TARGET";
            case 2:
                return "MOVING TARGET, ONE LOCATION";
            case 3:
                return "MOVING TARGET, TWO LOCATIONS";
            case 4:
                return "MOVING TARGET PREDICTED POINT";
            case 5:
                return "FINAL PROTECTIVE FIRES";
            case 6:
                return "PRIORITY COPPERHEAD MISSION";
            case 7:
                return "ON-CALL TARGET";
            case 8:
                return "QUICK SMOKE MISSION";
            case 9:
                return "MULTIPLE PRECISION AIMING POINTS";
            default:
                return "UNDEFINED (" + missiontype + ")";
        }
    }

    String getMissionType() {
        return CR + "Mission Type: " + missionType();
    }

    private boolean parse(VmfDataBuffer data) {
        if (header.stdVersion < VMF_6017C) {

        } else {

            try {
                missiontype = data.getInt(4);
                targetNumber = data.getString(35);

            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    public class TargetData {

    }

}
