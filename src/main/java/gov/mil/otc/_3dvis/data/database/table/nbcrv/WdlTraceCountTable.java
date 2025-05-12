package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.table.DatabaseObjectTable;
import gov.mil.otc._3dvis.data.oadms.WdlReading;
import gov.mil.otc._3dvis.entity.base.EntityId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WdlTraceCountTable extends DatabaseObjectTable<WdlReading> {

    private static final String TABLE_NAME = "nbcrv_wdl_trace_count";

    protected static List<Integer> getTraceCounts(Connection connection, EntityId entityId, long timestamp) {
        List<Integer> traceCounts = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE site=%d AND app=%d AND id=%d AND timestamp=%d ORDER BY traceIndex",
                TABLE_NAME, entityId.getSite(), entityId.getApplication(), entityId.getId(), timestamp);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                traceCounts.add(resultSet.getInt("traceCount"));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "WdlTraceCountTable:getTraceCounts", e);
        }
        return traceCounts;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(site INTEGER" +
                ",app INTEGER" +
                ",id INTEGER" +
                ",timestamp INTEGER" +
                ",traceIndex INTEGER" +
                ",traceCount INTEGER" +
                ",PRIMARY KEY(site,app,id,timestamp,traceIndex))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site" +
                ",app" +
                ",id" +
                ",timestamp" +
                ",traceIndex" +
                ",traceCount" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }

    @Override
    public void insertToDatabase(Connection connection) {
        synchronized (objects) {
            if (objects.isEmpty()) {
                return;
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            synchronized (objects) {
                for (DatabaseObject<WdlReading> object : objects) {
                    insertRow(preparedStatement, object);
                }
                objects.clear();
            }
            preparedStatement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<WdlReading> object) {
        WdlReading wdlReading = object.getObject();
        try {
            for (int i = 0; i < wdlReading.getTraceCounts().length; i++) {
                preparedStatement.setInt(1, object.getEntityId().getSite());
                preparedStatement.setInt(2, object.getEntityId().getApplication());
                preparedStatement.setInt(3, object.getEntityId().getId());
                preparedStatement.setLong(4, wdlReading.getTimestamp());
                preparedStatement.setInt(5, i);
                preparedStatement.setInt(6, wdlReading.getTraceCounts()[i]);
                preparedStatement.addBatch();
            }
        } catch (SQLException e) {
            String message = String.format("Error inserting to %s.", getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
