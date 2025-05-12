package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.table.DatabaseObjectTable;
import gov.mil.otc._3dvis.data.oadms.WdlReading;
import gov.mil.otc._3dvis.entity.base.EntityId;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WdlReadingTable extends DatabaseObjectTable<WdlReading> {

    private static final String TABLE_NAME = "nbcrv_wdl_reading";

    public List<WdlReading> getWdlReadings(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<WdlReading> wdlReadings = new ArrayList<>();
        String sql = getQuerySql(entityId);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                String operationalStatus = resultSet.getString("operationalStatus");
                double azimuth = resultSet.getDouble("azimuth");
                double elevation = resultSet.getDouble("elevation");
                double resolution = resultSet.getDouble("resolution");
                String traceCountString = resultSet.getString("traceCounts");
                String hueValueString = resultSet.getString("hueValues");

                wdlReadings.add(new WdlReading(timestamp, operationalStatus, azimuth, elevation, resolution,
                        0, traceCountString, hueValueString));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "WdlReadingTable::getWdlReadings", e);
        }

        return wdlReadings;
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
                ",operationalStatus TEXT" +
                ",azimuth REAL" +
                ",elevation REAL" +
                ",resolution REAL" +
                ",traceCounts TEXT" +
                ",hueValues TEXT" +
                ",PRIMARY KEY(site,app,id,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(sourceId" +
                ",site" +
                ",app" +
                ",id" +
                ",timestamp" +
                ",operationalStatus" +
                ",azimuth" +
                ",elevation" +
                ",resolution" +
                ",traceCounts" +
                ",hueValues" +
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
                ")";
    }

    @Override
    public void insertToDatabase(Connection connection) {
        synchronized (objects) {
            if (objects.isEmpty()) {
                return;
            }
        }

        String tempTableName = createTempTable(connection);
        if (tempTableName.isEmpty()) {
            return;
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(tempTableName))) {
            synchronized (objects) {
                for (DatabaseObject<WdlReading> object : objects) {
                    insertRow(preparedStatement, object);
                }
                objects.clear();
            }
            preparedStatement.executeBatch();
            connection.commit();
            insertTempTable(connection, tempTableName);
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<WdlReading> object) {
        WdlReading wdlReading = object.getObject();
        try {
            preparedStatement.setInt(1, object.getSourceId());
            preparedStatement.setInt(2, object.getEntityId().getSite());
            preparedStatement.setInt(3, object.getEntityId().getApplication());
            preparedStatement.setInt(4, object.getEntityId().getId());
            preparedStatement.setLong(5, wdlReading.getTimestamp());
            preparedStatement.setString(6, wdlReading.getOperationalStatus());
            preparedStatement.setDouble(7, wdlReading.getAzimuth());
            preparedStatement.setDouble(8, wdlReading.getElevation());
            preparedStatement.setDouble(9, wdlReading.getResolution());
            preparedStatement.setString(10, wdlReading.getTraceCountString());
            preparedStatement.setString(11, wdlReading.getHueValueString());
            preparedStatement.addBatch();
        } catch (SQLException e) {
            String message = String.format("Error inserting to %s.", getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    private String createTempTable(Connection connection) {
        String tempTableName = "temp_" + getTableName();
        String createTableSql = getCreateTableSql(tempTableName);
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS " + tempTableName);
            statement.execute(createTableSql);
        } catch (SQLException e) {
            String message = String.format("Error creating table %s.", tempTableName);
            Logger.getGlobal().log(Level.WARNING, message, e);
            tempTableName = "";
        }
        return tempTableName;
    }

    private void insertTempTable(Connection connection, String tempTableName) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO " + getTableName() +
                    " SELECT * FROM " + tempTableName + " t1" +
                    " WHERE NOT EXISTS" +
                    "  (SELECT * FROM " + getTableName() + " t2" +
                    "   WHERE t1.site = t2.site" +
                    "    AND t1.app = t2.app" +
                    "    AND t1.id = t2.id" +
                    "    AND t1.timestamp = t2.timestamp)");
            connection.commit();
        } catch (SQLException e) {
            String message = String.format("Error inserting %s table.", tempTableName);
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
