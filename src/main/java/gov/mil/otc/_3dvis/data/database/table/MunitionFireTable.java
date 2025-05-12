package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.event.MunitionFireEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for the MunitionFire event table.
 */
public class MunitionFireTable extends DatabaseObjectTable<MunitionFireEvent> {

    private static final String TABLE_NAME = "munition_fire";

    public List<MunitionFireEvent> getEvents(Connection connection, List<Integer> sourceIds) {
        List<MunitionFireEvent> events = new ArrayList<>();
        String sql = getEventQuerySql(null, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");

                int eventSite = resultSet.getInt("eventSite");
                int eventApp = resultSet.getInt("eventApp");
                int eventId = resultSet.getInt("eventId");
                EntityId eventEntityId = new EntityId(eventSite, eventApp, eventId);

                int shooterSite = resultSet.getInt("shooterSite");
                int shooterApp = resultSet.getInt("shooterApp");
                int shooterId = resultSet.getInt("shooterId");
                EntityId shooterEntityId = new EntityId(shooterSite, shooterApp, shooterId);

                int targetSite = resultSet.getInt("targetSite");
                int targetApp = resultSet.getInt("targetApp");
                int targetId = resultSet.getInt("targetId");
                EntityId targetEntityId = new EntityId(targetSite, targetApp, targetId);

                EntityType munitionType = EntityType.fromString(resultSet.getString("munitionType"));
                int quantity = resultSet.getInt("quantity");

                events.add(new MunitionFireEvent(timestamp, eventEntityId, shooterEntityId, targetEntityId,
                        munitionType, quantity));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return events;
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<MunitionFireEvent> object) {
        try {
            preparedStatement.setLong(1, object.getObject().getTimestamp());
            preparedStatement.setInt(2, object.getObject().getEventId().getSite());
            preparedStatement.setInt(3, object.getObject().getEventId().getApplication());
            preparedStatement.setInt(4, object.getObject().getEventId().getId());
            preparedStatement.setInt(5, object.getObject().getShooterId().getSite());
            preparedStatement.setInt(6, object.getObject().getShooterId().getApplication());
            preparedStatement.setInt(7, object.getObject().getShooterId().getId());
            if (object.getObject().getTargetId() != null) {
                preparedStatement.setInt(8, object.getObject().getTargetId().getSite());
                preparedStatement.setInt(9, object.getObject().getTargetId().getApplication());
                preparedStatement.setInt(10, object.getObject().getTargetId().getId());
            } else {
                preparedStatement.setInt(8, 0);
                preparedStatement.setInt(9, 0);
                preparedStatement.setInt(10, 0);
            }
            preparedStatement.setString(11, object.getObject().getMunition().toString());
            preparedStatement.setInt(12, object.getObject().getQuantity());
            preparedStatement.setInt(13, object.getSourceId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting MunitionFire event %s at %d from %d",
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
                "(timestamp INTEGER" +
                ",eventSite INTEGER" +
                ",eventApp INTEGER" +
                ",eventId INTEGER" +
                ",shooterSite INTEGER" +
                ",shooterApp INTEGER" +
                ",shooterId INTEGER" +
                ",targetSite INTEGER" +
                ",targetApp INTEGER" +
                ",targetId INTEGER" +
                ",munitionType TEXT" +
                ",quantity INTEGER" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(sourceId,eventSite,eventApp,eventId,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(timestamp" +
                ",eventSite" +
                ",eventApp" +
                ",eventId" +
                ",shooterSite" +
                ",shooterApp" +
                ",shooterId" +
                ",targetSite" +
                ",targetApp" +
                ",targetId" +
                ",munitionType" +
                ",quantity" +
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
                ")";
    }
}
