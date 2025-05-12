package gov.mil.otc._3dvis.data.report;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.*;
import gov.mil.otc._3dvis.event.otcam.OtcamEvent;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;

import java.io.IOException;

public class EngagementReport extends Report {

    private static final String REPORT_NAME = "Engagement Report";
    private static final String FILE_NAME = "engagement_report.csv";
    private static final String[] COLUMNS = {
            //event info
            "timestamp",
            "event type",
            "event origin",
            "event from",

            //shooter info
            "shooter id",
            "shooter name",
            "shooter entity type",
            "shooter affiliation",
            "shooter latitude",
            "shooter longitude",
            "shooter altitude",
            "shooter mgrs",

            //target info
            "target id",
            "target name",
            "target entity type",
            "target affiliation",
            "target latitude",
            "target longitude",
            "target altitude",
            "target mgrs",

            //impact info
            "impact latitude",
            "impact longitude",
            "impact altitude",
            "impact mgrs",

            "munitions",
            "results",
            "range",
    };
//"timestamp",
// "event type",
// "event origin",
// "event from",
// "shooter id", "shooter name", "shooter entity type", "shooter affiliation",
// "shooter latitude", "shooter longitude", "shooter altitude", "shooter mgrs",
// "target id", "target name", "target entity type", "target affiliation",
// "target latitude", "target longitude", "target altitude", "target mgrs",
// "weapon", "results"

    @Override
    public String getReportName() {
        return REPORT_NAME;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected String getFilename() {
        return FILE_NAME;
    }

    @Override
    protected String[] getColumns() {
        return COLUMNS;
    }

    @Override
    protected void writeData() throws IOException {
        for (Event event : EventManager.getEvents()) {
            if (!inDateRange(event.getTimestamp())) {
                continue;
            }

            if (event instanceof RtcaEvent) {
                writeRtcaEvent((RtcaEvent) event);
            }
        }
    }

    private void writeRtcaEvent(RtcaEvent rtcaEvent) throws IOException {
        if (rtcaEvent instanceof MunitionFireEvent) {
            writeMunitionFireEvent((MunitionFireEvent) rtcaEvent);
        } else if (rtcaEvent instanceof MunitionDetonationEvent) {
            writeMunitionDetonationEvent((MunitionDetonationEvent) rtcaEvent);
        } else if (rtcaEvent instanceof OtcamEvent) {
            writeOtcamEvent((OtcamEvent) rtcaEvent);
        }
    }

    private void writeMunitionFireEvent(MunitionFireEvent munitionFireEvent) throws IOException {
        writeReportData(munitionFireEvent.getTimestamp(), "Munition Fire", "", "",
                munitionFireEvent.getShooterId(), munitionFireEvent.getTargetId(), null,
                munitionFireEvent.getMunition().getDescription(), "");
    }

    private void writeMunitionDetonationEvent(MunitionDetonationEvent munitionDetonationEvent) throws IOException {
        writeReportData(munitionDetonationEvent.getTimestamp(), "Munition Detonation", "", "",
                null, null, munitionDetonationEvent.getImpactPosition(),
                munitionDetonationEvent.getMunition().getDescription(), "");
    }

    private void writeOtcamEvent(OtcamEvent otcamEvent) throws IOException {
        writeReportData(otcamEvent.getTimestamp(), otcamEvent.getEventType().getDescription(),
                otcamEvent.getEventOrigin().getDescription(),
                otcamEvent.getEventFrom().getDescription(),
                otcamEvent.getShooterId(), otcamEvent.getTargetId(), null,
                otcamEvent.getMunitionDescription(), otcamEvent.getResults());
    }

    private void writeReportData(long timestamp, String eventType, String eventOrigin, String eventFrom,
                                 EntityId shooterId, EntityId targetId, Position impactPosition,
                                 String munitions, String results) throws IOException {
        writeTimestamp(timestamp);

        writeValue(eventType);
        writeValue(eventOrigin);
        writeValue(eventFrom);

        IEntity shooter = shooterId != null ? EntityManager.getEntity(shooterId) : null;
        EntityDetail shooterDetails = shooter != null ? shooter.getEntityDetailBefore(timestamp) : null;
        Position shooterPosition = shooter != null ? shooter.getPositionBefore(timestamp) : null;

        writeValue(shooterId);
        writeEntityDetail(shooterDetails);
        writePositionData(shooterPosition);

        IEntity target = targetId != null ? EntityManager.getEntity(targetId) : null;
        EntityDetail targetDetails = target != null ? target.getEntityDetailBefore(timestamp) : null;
        Position targetPosition = target != null ? target.getPositionBefore(timestamp) : null;

        writeValue(targetId);
        writeEntityDetail(targetDetails);
        writePositionData(targetPosition);

        writePositionData(impactPosition);

        writeValue(munitions);
        writeValue(results);
        writeRange(shooterPosition, targetPosition);

        newLine();
    }

    private void writeEntityDetail(EntityDetail entityDetail) throws IOException {
        if (entityDetail != null) {
            writeValue(entityDetail.getName());
            writeValue(entityDetail.getEntityType().getDescription());
            writeValue(entityDetail.getAffiliation().getName());
        } else {
            writeValue("");
            writeValue("");
            writeValue("");
        }
    }

    private void writePositionData(Position position) throws IOException {
        if (position != null) {
            writeValue(String.valueOf(position.getLatitude().degrees));
            writeValue(String.valueOf(position.getLongitude().degrees));
            writeValue(String.valueOf(position.getAltitude()));
            writeValue(MGRSCoord.fromLatLon(position.getLatitude(), position.getLongitude()).toString());
        } else {
            writeValue("");
            writeValue("");
            writeValue("");
            writeValue("");
        }
    }

    private void writeRange(Position position1, Position position2) throws IOException {
        if (position1 != null && position2 != null) {
            writeValue(String.valueOf(Utility.calculateDistance1(position1, position2)));
        }
    }
}
