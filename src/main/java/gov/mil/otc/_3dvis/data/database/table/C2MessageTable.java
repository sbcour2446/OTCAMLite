package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class C2MessageTable extends DatabaseObjectTable<C2MessageEvent> {

    private static final String TABLE_NAME = "c2_message_table";

    public List<C2MessageEvent> getC2MessageEvent(Connection connection, IEntity entity, List<Integer> sourceIds) {
        List<C2MessageEvent> c2MessageEvents = new ArrayList<>();
        String sql = getQuerySql(entity.getEntityId(), sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                int senderUrn = resultSet.getInt("senderUrn");
                String destinationUrns = resultSet.getString("destinationUrns");
                String messageType = resultSet.getString("messageType");
                String summary = resultSet.getString("summary");
                String message = resultSet.getString("message");
                c2MessageEvents.add(new C2MessageEvent(timestamp, entity, senderUrn, destinationUrns,
                        messageType, summary, message));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return c2MessageEvents;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(sourceId INTEGER" +
                ",site INTEGER" +
                ",app INTEGER" +
                ",id INTEGER" +
                ",timestamp INTEGER" +
                ",senderUrn INTEGER" +
                ",destinationUrns TEXT" +
                ",messageType TEXT" +
                ",summary TEXT" +
                ",message TEXT" +
                ",PRIMARY KEY(sourceId,site,app,id,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(sourceId" +
                ",site" +
                ",app" +
                ",id" +
                ",timestamp" +
                ",senderUrn" +
                ",destinationUrns" +
                ",messageType" +
                ",summary" +
                ",message" +
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
                ")";
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<C2MessageEvent> object) {
        try {
            preparedStatement.setInt(1, object.getSourceId());
            preparedStatement.setInt(2, object.getEntityId().getSite());
            preparedStatement.setInt(3, object.getEntityId().getApplication());
            preparedStatement.setInt(4, object.getEntityId().getId());
            preparedStatement.setLong(5, object.getObject().getTimestamp());
            preparedStatement.setInt(6, object.getObject().getSenderUrn());
            preparedStatement.setString(7, object.getObject().getDestinationUrns());
            preparedStatement.setString(8, object.getObject().getMessageType());
            preparedStatement.setString(9, object.getObject().getSummary());
            preparedStatement.setString(10, object.getObject().getMessage());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting to %s.", getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
