package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.datamodel.RtcaState;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.event.otcam.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for the OTCAM event table.
 */
public class OtcamEventTable extends DatabaseObjectTable<OtcamEvent> {

    private static final String TABLE_NAME = "otcam_event";

    public List<OtcamEvent> getEvents(Connection connection, List<Integer> sourceIds) {
        List<OtcamEvent> events = new ArrayList<>();
        String sql = getEventQuerySql(null, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                int eventSite = resultSet.getInt("eventSite");
                int eventApp = resultSet.getInt("eventApp");
                int eventId = resultSet.getInt("eventId");
                EntityId eventEntityId = new EntityId(eventSite, eventApp, eventId);
                long timestamp = resultSet.getLong("timestamp");
                EventType eventType = EventType.getEnum(resultSet.getInt("eventType"));
                EventOrigin eventOrigin = EventOrigin.getEnum(resultSet.getInt("eventOrigin"));
                EventFrom eventFrom = EventFrom.getEnum(resultSet.getInt("eventFrom"));

                AdminAction adminAction = AdminAction.getEnum(resultSet.getInt("adminAction"));
                if (resultSet.wasNull()) {
                    adminAction = null;
                }

                int targetSite = resultSet.getInt("targetSite");
                int targetApp = resultSet.getInt("targetApp");
                int targetId = resultSet.getInt("targetId");
                EntityId targetEntityId = new EntityId(targetSite, targetApp, targetId);
                if (resultSet.wasNull()) {
                    targetEntityId = null;
                }

                int shooterSite = resultSet.getInt("shooterSite");
                int shooterApp = resultSet.getInt("shooterApp");
                int shooterId = resultSet.getInt("shooterId");
                EntityId shooterEntityId = new EntityId(shooterSite, shooterApp, shooterId);
                if (resultSet.wasNull()) {
                    shooterEntityId = null;
                }

                EntityType munitionType = EntityType.fromString(resultSet.getString("munitionType"));
                if (resultSet.wasNull()) {
                    munitionType = null;
                }

                String munitionName = resultSet.getString("munitionName");

                boolean isKillCatastrophic = resultSet.getBoolean("isKillCatastrophic");
                boolean isKillFirepower = resultSet.getBoolean("isKillFirepower");
                boolean isKillMobility = resultSet.getBoolean("isKillMobility");
                boolean isKillCommunication = resultSet.getBoolean("isKillCommunication");
                boolean isSuppressed = resultSet.getBoolean("isSuppressed");
                boolean isJammed = resultSet.getBoolean("isJammed");
                boolean isHit = resultSet.getBoolean("isHit");
                boolean isMissed = resultSet.getBoolean("isMissed");
                String rtcaOther = resultSet.getString("rtcaOther");
                int damagePercent = resultSet.getInt("damagePercent");
                RtcaState rtcaState = new RtcaState(isKillCatastrophic, isKillFirepower, isKillMobility,
                        isKillCommunication, isSuppressed, isJammed, isHit, isMissed, rtcaOther, damagePercent);
                if (resultSet.wasNull()) {
                    rtcaState = null;
                }

                events.add(new OtcamEvent(timestamp, eventEntityId, eventType, eventOrigin, eventFrom, adminAction,
                        targetEntityId, shooterEntityId, munitionType, munitionName, rtcaState));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return events;
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<OtcamEvent> object) {
        try {
            preparedStatement.setInt(1, object.getObject().getEventId().getSite());
            preparedStatement.setInt(2, object.getObject().getEventId().getApplication());
            preparedStatement.setInt(3, object.getObject().getEventId().getId());
            preparedStatement.setLong(4, object.getObject().getTimestamp());
            preparedStatement.setInt(5, object.getObject().getEventType().ordinal());
            preparedStatement.setInt(6, object.getObject().getEventOrigin().ordinal());
            preparedStatement.setInt(7, object.getObject().getEventFrom().ordinal());

            if (object.getObject().getAdminAction() != null) {
                preparedStatement.setInt(8, object.getObject().getAdminAction().ordinal());
            } else {
                preparedStatement.setNull(8, Types.INTEGER);
            }

            if (object.getObject().getTargetId() != null) {
                preparedStatement.setInt(9, object.getObject().getTargetId().getSite());
                preparedStatement.setInt(10, object.getObject().getTargetId().getApplication());
                preparedStatement.setInt(11, object.getObject().getTargetId().getId());
            } else {
                preparedStatement.setNull(9, Types.INTEGER);
                preparedStatement.setNull(10, Types.INTEGER);
                preparedStatement.setNull(11, Types.INTEGER);
            }

            if (object.getObject().getShooterId() != null) {
                preparedStatement.setInt(12, object.getObject().getShooterId().getSite());
                preparedStatement.setInt(13, object.getObject().getShooterId().getApplication());
                preparedStatement.setInt(14, object.getObject().getShooterId().getId());
            } else {
                preparedStatement.setNull(12, Types.INTEGER);
                preparedStatement.setNull(13, Types.INTEGER);
                preparedStatement.setNull(14, Types.INTEGER);
            }

            if (object.getObject().getMunition() != null) {
                preparedStatement.setString(15, object.getObject().getMunition().toString());
            } else {
                preparedStatement.setNull(15, Types.VARCHAR);
            }

            preparedStatement.setString(16, object.getObject().getMunitionName());

            if (object.getObject().getRtcaState() != null) {
                preparedStatement.setBoolean(17, object.getObject().getRtcaState().isKillCatastrophic());
                preparedStatement.setBoolean(18, object.getObject().getRtcaState().isKillFirepower());
                preparedStatement.setBoolean(19, object.getObject().getRtcaState().isKillMobility());
                preparedStatement.setBoolean(20, object.getObject().getRtcaState().isKillCommunication());
                preparedStatement.setBoolean(21, object.getObject().getRtcaState().isSuppression());
                preparedStatement.setBoolean(22, object.getObject().getRtcaState().isJammed());
                preparedStatement.setBoolean(23, object.getObject().getRtcaState().isHitNoKill());
                preparedStatement.setBoolean(24, object.getObject().getRtcaState().isMiss());
                preparedStatement.setString(25, object.getObject().getRtcaState().getRtcaOther());
                preparedStatement.setInt(26, object.getObject().getRtcaState().getDamagePercent());
            } else {
                preparedStatement.setNull(17, Types.BOOLEAN);
                preparedStatement.setNull(18, Types.BOOLEAN);
                preparedStatement.setNull(19, Types.BOOLEAN);
                preparedStatement.setNull(20, Types.BOOLEAN);
                preparedStatement.setNull(21, Types.BOOLEAN);
                preparedStatement.setNull(22, Types.BOOLEAN);
                preparedStatement.setNull(23, Types.BOOLEAN);
                preparedStatement.setNull(24, Types.BOOLEAN);
                preparedStatement.setNull(25, Types.VARCHAR);
                preparedStatement.setNull(26, Types.INTEGER);
            }

            preparedStatement.setInt(27, object.getSourceId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting OTCAM event %s at %d from %d",
                    object.getObject().getEventId().toString(), object.getObject().getTimestamp(), object.getSourceId());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + getTableName() +
                "(eventSite INTEGER" +
                ",eventApp INTEGER" +
                ",eventId INTEGER" +
                ",timestamp INTEGER" +
                ",eventType INTEGER" +
                ",eventOrigin INTEGER" +
                ",eventFrom INTEGER" +
                ",adminAction INTEGER" +
                ",targetSite INTEGER" +
                ",targetApp INTEGER" +
                ",targetId INTEGER" +
                ",shooterSite INTEGER" +
                ",shooterApp INTEGER" +
                ",shooterId INTEGER" +
                ",munitionType TEXT" +
                ",munitionName TEXT" +
                ",isKillCatastrophic INTEGER" +
                ",isKillFirepower INTEGER" +
                ",isKillMobility INTEGER" +
                ",isKillCommunication INTEGER" +
                ",isSuppressed INTEGER" +
                ",isJammed INTEGER" +
                ",isHit INTEGER" +
                ",isMissed INTEGER" +
                ",rtcaOther TEXT" +
                ",damagePercent INTEGER" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(sourceId,eventSite,eventApp,eventId,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(eventSite" +
                ",eventApp" +
                ",eventId" +
                ",timestamp" +
                ",eventType" +
                ",eventOrigin" +
                ",eventFrom" +
                ",adminAction" +
                ",targetSite" +
                ",targetApp" +
                ",targetId" +
                ",shooterSite" +
                ",shooterApp" +
                ",shooterId" +
                ",munitionType" +
                ",munitionName" +
                ",isKillCatastrophic" +
                ",isKillFirepower" +
                ",isKillMobility" +
                ",isKillCommunication" +
                ",isSuppressed" +
                ",isJammed" +
                ",isHit" +
                ",isMissed" +
                ",rtcaOther" +
                ",damagePercent" +
                ",sourceId" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }
}
