package gov.mil.otc._3dvis.vmf;

import gov.mil.otc._3dvis.overlay.Overlay;
import gov.mil.otc._3dvis.overlay.OverlayData;
import gov.mil.otc._3dvis.overlay.OverlayManagerX;
import gov.nasa.worldwind.geom.Position;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * The VMF K05.17 Overlay Class
 */
public class K0517 extends VmfMessage {

    private static String lastMsgKey = "";
    private final SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final List<Overlay> overlays = new ArrayList<>();
    private final List<OverlayInfo> infoList = new ArrayList<>();
    private boolean isDuplicate;
    private String body;
    private long reportTime;
    private int reportUrn;
    private long defaultDuration = 86400000L * 100L; // 100 days as a default
    private Calendar dtg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private Calendar revDtg = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private int newUpdateReplace;

    private static void checkForDuplicate(K0517 k0517) {
        String key = k0517.header.getKey();
        k0517.isDuplicate = key.equals(lastMsgKey);

        if (!k0517.header.isAck()) {
            lastMsgKey = key;
        }
    }

    /**
     * {@inheritDoc}
     */
    protected K0517(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        checkForDuplicate(this);

        if (isDuplicate() || header.isAck()) {
            return true;
        }

        OverlayType overlayType = OverlayType.getType(data.getInt(5));
        addBodyLine("overlayType", overlayType);
        reportUrn = data.getInt(24);
        addBodyLine("urn", reportUrn);

        //G1
        if (data.getGpi()) {
            int year = data.getInt(7) + 2000;
            int month = data.getInt(4);
            int dayOfMonth = data.getInt(5);
            int hour = data.getInt(5);
            int minute = data.getInt(6);
            int second = data.getFpiInt(6);

            dtg.set(year, month - 1, dayOfMonth, hour, minute, second == VmfDataBuffer.NO_INT ? 0 : second);
            dtg.set(Calendar.MILLISECOND, 0);
            reportTime = dtg.getTimeInMillis();
            addBodyLine("dtg", sdfLong.format(dtg.getTime()));
        }//G1

        String operationIdentification = data.getFpiString(14);
        addBodyLine("operationIdentification", operationIdentification);
        String opLanOpOrdName = data.getFpiString(224);
        addBodyLine("opLanOpOrdName", opLanOpOrdName);
        int fragoIdNo = data.getFpiInt(6);
        addBodyLine("fragoIdNo", fragoIdNo);
        newUpdateReplace = data.getInt(2);
        addBodyLine("newUpdateReplace", newUpdateReplace);

        String groupId = String.format("%7d-%d-%s", reportUrn, dtg.getTimeInMillis() / 1000, operationIdentification);

        /*
        OVERLAY UPDATE 0
        OVERLAY REPLACE 1
        OVERLAY NEW 2
         */

        //G2
        if (data.getGpi()) {
            // revision DTG is required for ovelay update and replace
            data.getFpiInt(6);
            data.getFpiInt(6);
            int year = data.getInt(7) + 2000;
            int month = data.getInt(4);
            int dayOfMonth = data.getInt(5);
            int hour = data.getInt(5);
            int minute = data.getInt(6);

            revDtg.set(year, month - 1, dayOfMonth, hour, minute, 0);
            revDtg.set(Calendar.MILLISECOND, 0);
            addBodyLine("revDtg", sdfLong.format(revDtg.getTime()));
            reportTime = revDtg.getTimeInMillis();
            if (newUpdateReplace == 0) {
                // this is a replace command so all prior overlays in this group end at this revision time
//                Overlay.endGroup(groupId, revDtg.getTimeInMillis());
            }
        }//G2

        //G3
        if (data.getGpi()) {
            data.getInt(4);
            data.getInt(5);
            data.getInt(5);
            data.getInt(6);
        }//G3

        //R1
        boolean griR1;
        do {
            OverlayInfo info = new OverlayInfo();
            griR1 = data.getGri();
            String groupLabelName = data.getFpiString(63);
            addBodyLine("groupLabelName", groupLabelName);
            int groupTypeIndicator = data.getFpiInt(3);
            addBodyLine("groupTypeIndicator", groupTypeIndicator);
            info.symbolDimension = data.getInt(5);
            addBodyLine("symbolDimension", info.symbolDimension);
            info.identity = data.getInt(4);
            addBodyLine("identity", info.identity);
            info.iconStatus = data.getInt(1);
            addBodyLine("iconStatus", (info.iconStatus == 0 ? "P" : "A"));

            //R1/R2
            boolean griR2;
            do {
                griR2 = data.getGri();
                info.addDeleteUpdateEntity = data.getInt(2);
                // ADD SYMBOL 0
                // DELETE SYMBOL 1
                // UPDATE SYMBOL 2
                addBodyLine("addDeleteUpdateEntity", info.addDeleteUpdateEntity);
                int entityIdSerialNumber = data.getFpiInt(32);
                addBodyLine("entityIdSerialNumber", entityIdSerialNumber);
                info.id = String.format("%s-%d", groupId, entityIdSerialNumber);

//                //G4
                if (data.getGpi()) {

                    info.entityType = data.getInt(6);
                    addBodyLine("entityType", info.entityType);
                    info.entitySubType = data.getInt(6);
                    addBodyLine("entitySubType", info.entitySubType);
                    info.iconSize = data.getFpiInt(8);

//                    overlayData.setSymbolCode(symbolDimension, entityType, entitySubType, identity, iconStatus);
//                    addBodyLine("FunctionCode", overlayData.symbolCode);
//                    addBodyLine("Symbol", overlayData.symbolCode);
                    addBodyLine("iconSize", info.iconSize);
                    int nationality = data.getFpiInt(9);
                    addBodyLine("nationality", nationality);
                    int iconOrder = data.getFpiInt(3);
                    addBodyLine("iconOrder", iconOrder);
                    info.iconFill = data.getFpiInt(4);
                    addBodyLine("iconFill", info.iconFill);

                    //G4/G5
                    if (data.getGpi()) {
                        int associatedSymbolDimension = data.getInt(5);
                        addBodyLine("associatedSymbolDimension", associatedSymbolDimension);
                        int associatedSymbolIdentity = data.getInt(4);
                        addBodyLine("associatedSymbolIdentity", associatedSymbolIdentity);
                        int associatedSymbolIconStatus = data.getInt(1);
                        addBodyLine("associatedSymbolIconStatus", associatedSymbolIconStatus);
                        int associatedSymbolEntityType = data.getInt(6);
                        addBodyLine("associatedSymbolEntityType", associatedSymbolEntityType);
                        int associatedSymbolEntitySubType = data.getFpiInt(6);
                        addBodyLine("associatedSymbolEntitySubType", associatedSymbolEntitySubType);
                        int associatedSymbolIconSize = data.getFpiInt(8);
                        addBodyLine("associatedSymbolIconSize", associatedSymbolIconSize);
                    }//G4/G5

                    if (data.getFpi()) {
                        //R1/R2/R3
                        boolean friR3;
                        do {
                            friR3 = data.getGri();
                            String additionalInfo = data.getString(140);
                            addBodyLine("additionalInfo", additionalInfo);
                            info.additionalInfo.add(additionalInfo);
                        } while (friR3);//R1/R2/R3
                    }

                    int symbolAxisOrientation = data.getFpiInt(9);
                    addBodyLine("symbolAxisOrientation", symbolAxisOrientation);
                    String staffComments = data.getFpiString(140);
                    addBodyLine("staffComments", staffComments);
                    info.staffComments = staffComments;

                    //R1/R2/R4
                    boolean griR4;
                    do {
                        griR4 = data.getGri();
                        int hHour = data.getFpiInt(5);
                        addBodyLine("hHour", hHour);

                        //R1/R2/R4/R5
                        boolean griR5;
                        do {
                            griR5 = data.getGri();

                            double latitude = data.getInt(25) * LAT_CONVERSION_25BIT;
                            if (latitude > 90) {
                                latitude -= 90.0;
                            }

                            double longitude = data.getInt(26) * LON_CONVERSION_26BIT;
                            if (longitude > 180.0) {
                                longitude -= 360.0;
                            }

                            // looks like JBC-P sends positions in reverse order
//                            if (symbolDimension != 12) {
//                                overlayData.positions.add(Position.fromDegrees(latitude, longitude));
//                            } else {
//                                overlayData.positions.add(0, Position.fromDegrees(latitude, longitude));
//                            }
                            info.positions.add(Position.fromDegrees(latitude, longitude));
                            addBodyLine("latitude", latitude);
                            addBodyLine("longitude", longitude);
                            int elevation = data.getFpiInt(17);
                            int height = data.getFpiInt(10);
                            int depth = data.getFpiInt(10);
                            int altitude = data.getFpiInt(13);
                            int upperAltitude = data.getFpiInt(10);
                            int lowerAltitude = data.getFpiInt(10);
                            int aglAltitude = data.getFpiInt(11);
                            int aglMaxAltitude = data.getFpiInt(11);
                            int aglMinAltitude = data.getFpiInt(11);
                            int lineWidth = data.getFpiInt(10);

                            //G4/G6
                            if (data.getGpi()) {
                                int squareSwitch = data.getInt(2);
                                int axisOrientation = data.getInt(8);
                                int areaMajorAxis = data.getInt(7);
                                int areaMinorAxis = data.getInt(7);
                            }//G4/G6

                            //G4/G7
                            if (data.getGpi()) {
                                int arcAzLeftLimit = data.getInt(13);
                                int arcAzRightLimit = data.getInt(13);
                                int arcLeftAzRange = data.getInt(16);
                                int arcRightAzRange = data.getInt(16);
                            }//G4/G7
                        } while (griR5);//R1/R2/R4/R5

                        //G4/G8
                        if (data.getGpi()) {
                            data.getFpiInt(24);
                            data.getFpiString(168);
                            data.getFpiInt(14);

                            //R1/R2/R6
                            if (data.getFpi()) {
                                boolean friR6;
                                do {
                                    friR6 = data.getGri();
                                    String designation = data.getString(245);
                                    addBodyLine("UNIQUE SYMBOL DESIGNATION", designation);
                                    info.text.add(designation);
                                } while (friR6);
                            }//R1/R2/R6

                            data.getFpiInt(2);
                            data.getFpiInt(3);
                            data.getFpiInt(14);
                            data.getFpiInt(3);
                            data.getFpiInt(1);
                            data.getFpiString(147);

                            //G4/G8/G9
                            if (data.getGpi()) {
                                data.getFpiInt(5);
                                data.getFpiInt(12);
                                data.getFpiInt(12);
                                data.getFpiInt(2);
                            }//G4/G8/G9

                            //G4/G8/G10
                            if (data.getGpi()) {
                                data.getInt(9);
                                data.getInt(11);
                            }//G4/G8/G10

                            data.getFpiInt(2);
                            data.getFpiInt(6);
                        }//G4/G8

                        //G4/G11
                        if (data.getGpi()) {
                            //R1/R2/R7
                            boolean griR7;
                            do {
                                griR7 = data.getGri();
                                data.getInt(3);
                                data.getInt(7);
                                data.getInt(4);
                                data.getInt(5);
                                data.getInt(5);
                                data.getInt(6);
                                data.getFpiInt(6);
                            } while (griR7);//R1/R2/R7
                        }//G4/G11
                    } while (griR4);//R1/R2/R4
                    infoList.add(info);
                }//G4

            } while (griR2);//R1/R2
        } while (griR1);//R1

        //G12
        if (data.getGpi()) {
            //G12/G13
            if (data.getGpi()) {
                //R8
                boolean griR8;
                do {
                    griR8 = data.getGri();
                    data.getInt(9);
                    data.getInt(20);
                    data.getInt(12);

                    //G12/G13/G14
                    if (data.getGpi()) {
                        data.getInt(7);
                        data.getFpiInt(12);
                        data.getFpiInt(12);
                    }//G12/G13/G14
                } while (griR8);//R8
            }//G12/G13

            //G12/G15
            if (data.getGpi()) {
                data.getInt(12);
                data.getInt(12);
                data.getInt(9);
            }//G12/G15

            //G12/G16
            if (data.getGpi()) {
                data.getInt(12);
                data.getInt(14);
                data.getInt(9);
            }//G12/G15

            //G12/G17
            if (data.getGpi()) {
                data.getInt(17);
                data.getInt(12);
                data.getInt(9);
            }//G12/G17

            //G12/G18
            if (data.getGpi()) {
                data.getInt(12);
                data.getInt(12);
                data.getInt(9);
            }//G12/G18
        }//G12

        String graphicText = data.getFpiString(70);
        addBodyLine("GraphicText", graphicText);
        String comments = data.getFpiString(1400);
        addBodyLine("Comments", comments);

        return true;
    }

    public void processOverlayInfo() {
        for (OverlayInfo info : infoList) {
            if (info.addDeleteUpdateEntity == 1) {
                Overlay overlay = OverlayManagerX.getOverlay(info.id);
                if (overlay != null) {
                    overlay.setEndTime(revDtg.getTimeInMillis());
                }
            }
            if (newUpdateReplace != 2) {
                System.out.println("  REV " + sdfLong.format(revDtg.getTime()));
            }
            Overlay overlay = OverlayManagerX.getOverlay(info.id);
            if (overlay == null) {
                overlay = Overlay.createOverlay(info.id, reportUrn);
            } else {
//                        System.out.println("revision");
            }
//              if overlay is modify or replace, delete the old overlay
            OverlayData overlayData = new OverlayData(overlay);
            overlayData.startTime = newUpdateReplace == 2 ? dtg.getTimeInMillis() : revDtg.getTimeInMillis();
            overlayData.endTime = overlayData.startTime + defaultDuration;

            if (info.iconFill != VmfDataBuffer.NO_INT) {
                overlayData.setColor(translateColor(info.iconFill));
            }
            overlayData.setSymbolCode(info.symbolDimension, info.entityType, info.entitySubType, 3, info.iconStatus);
            overlayData.staffComments = info.staffComments;
            overlayData.positions.addAll(info.positions);
            overlayData.textFields.addAll(info.text);
            overlay.addNewData(overlayData);
            overlays.add(overlay);
        }
    }

    private Color translateColor(int iconFill) {
        return switch (iconFill) {
            case 0 -> Color.WHITE;
            case 1 -> Color.BLACK;
            case 2, 3 -> Color.RED;
            case 4, 5 -> Color.GREEN;
            case 6 -> Color.BLUE;
            case 7 -> Color.CYAN;
            case 8, 9 -> Color.YELLOW;
            case 10 -> Color.PINK;
            case 11 -> Color.MAGENTA;
//                case 12: return Color.SAFARI;
//                case 13: return Color.KHAKI;
            case 14 -> Color.LIGHT_GRAY;
            default -> Color.DARK_GRAY;
        };
    }

    public List<Overlay> getOverlays() {
        return overlays;
    }

    /**
     * Determine Duplicate Report
     *
     * @return true if duplicate otherwise, false
     */
    public final boolean isDuplicate() {
        return isDuplicate;
    }

    /**
     * Add a Body Line
     *
     * @param s   String to add
     * @param val Body Object
     */
    void addBodyLine(String s, Object val) {
        body += s + ": " + val + System.lineSeparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        String text = header.toString();

        text += System.lineSeparator() + "Overlay";

        return text;
    }

    private enum OverlayType {
        CSS_OVERLAY,
        OPERATIONS_OVERLAY,
        ENEMY_OVERLAY,
        FIRE_SUPPORT_OVERLAY,
        HIGHER_ECHELON_OPERATIONS_OVERLAY1,
        HIGHER_ECHELON_OPERATIONS_OVERLAY2,
        CURRENT_OPERATIONS_OVERLAY,
        FUTURE_OPERATIONS_OVERLAY,
        AIR_SPACE_COORDINATION_OVERLAY,
        ROUTE_OVERLAY,
        RANGE_CARD_OVERLAY,
        OBSTACLE_OVERLAY,
        MODIFIED_OBSTACLE_OVERLAY,
        COMBINED_OBSTACLE_OVERLAY,
        SECTOR_IDENTIFICATION_OVERLAY,
        PLANNED_OPERATION_OVERLAY,
        TRAFFIC_CIRCULATION_AND_CONTROL_OVERLAY,
        FIRE_PLAN_OVERLAY,
        TARGET_OVERLAY,
        WEATHER_OVERLAY,
        UNDEFINED;

        public static OverlayType getType(int i) {
            for (OverlayType o : OverlayType.values()) {
                if (o.ordinal() == i) {
                    return o;
                }
            }

            return OverlayType.UNDEFINED;
        }
    }

    /**
     * The Overlay Info Class
     */
    public static class OverlayInfo {

        private String id;
        private String staffComments;
        private int iconFill;
        private int iconSize;
        private int symbolDimension;
        private int entityType;
        private int entitySubType;
        private int iconStatus;
        private int addDeleteUpdateEntity;
        private int identity;
        private final List<Position> positions = new ArrayList<>();
        private final List<String> text = new ArrayList<>();
        private final List<String> additionalInfo = new ArrayList<>();
    }
}
