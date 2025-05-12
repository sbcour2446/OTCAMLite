package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.dlm.message.GenericDlmMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DlmMessageTable extends DatabaseObjectTable<GenericDlmMessage> {

    private static final String TABLE_NAME = "dlm_message";

    public List<GenericDlmMessage> getMessages(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<GenericDlmMessage> messages = new ArrayList<>();
        String sql = getQuerySql(entityId, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                int messageType = resultSet.getInt("messageType");
                int time = resultSet.getInt("time");
                long timestamp = resultSet.getLong("timestamp");
                boolean useTimestamp = resultSet.getBoolean("useTimestamp");
                byte[] data = resultSet.getBytes("data");
                messages.add(new GenericDlmMessage(messageType, time, data, timestamp, useTimestamp));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return messages;
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
                ",messageType INTEGER" +
                ",time INTEGER" +
                ",timestamp INTEGER" +
                ",useTimestamp INTEGER" +
                ",data BLOB" +
                ",PRIMARY KEY(sourceId,site,app,id,messageType,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(sourceId" +
                ",site" +
                ",app" +
                ",id" +
                ",messageType" +
                ",time" +
                ",timestamp" +
                ",useTimestamp" +
                ",data" +
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
                ")";
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<GenericDlmMessage> object) {
        try {
            preparedStatement.setInt(1, object.getSourceId());
            preparedStatement.setInt(2, object.getEntityId().getSite());
            preparedStatement.setInt(3, object.getEntityId().getApplication());
            preparedStatement.setInt(4, object.getEntityId().getId());
            preparedStatement.setInt(5, object.getObject().getMessageType());
            preparedStatement.setInt(6, object.getObject().getTime());
            preparedStatement.setLong(7, object.getObject().getTimestampOverride());
            preparedStatement.setBoolean(8, object.getObject().isUseTimestampOverride());
            preparedStatement.setBytes(9, object.getObject().getData());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting %d:%d:%d to %s.",
                    object.getEntityId().getSite(), object.getEntityId().getApplication(),
                    object.getEntityId().getId(), getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
