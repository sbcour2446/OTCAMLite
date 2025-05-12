package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.nasa.worldwind.geom.Position;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TspiDataTable extends DatabaseObjectTable<TspiData> {

    private static final String TABLE_NAME = "tspi_data";

    public List<TspiData> getTspi(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<TspiData> tspi = new ArrayList<>();
        String sql = getQuerySql(entityId, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                double altitude = resultSet.getDouble("altitude");
                double forwardVelocity = resultSet.getDouble("forwardVelocity");
                double verticalVelocity = resultSet.getDouble("verticalVelocity");
                double heading = resultSet.getDouble("heading");
                double pitch = resultSet.getDouble("pitch");
                double roll = resultSet.getDouble("roll");
                boolean isRecovered = resultSet.getBoolean("isRecovered");
                tspi.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude),
                        forwardVelocity, verticalVelocity, heading, pitch, roll, isRecovered));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return tspi;
    }

    public List<TspiData> getTspi(Connection connection, EntityId entityId) {
        List<TspiData> tspi = new ArrayList<>();

        String sql = String.format("SELECT * FROM %s WHERE site=%d AND app=%d AND id=%d ORDER BY timestamp",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                double altitude = resultSet.getDouble("altitude");
                double forwardVelocity = resultSet.getDouble("forwardVelocity");
                double verticalVelocity = resultSet.getDouble("verticalVelocity");
                double heading = resultSet.getDouble("heading");
                double pitch = resultSet.getDouble("pitch");
                double roll = resultSet.getDouble("roll");
                boolean isRecovered = resultSet.getBoolean("isRecovered");
                tspi.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude),
                        forwardVelocity, verticalVelocity, heading, pitch, roll, isRecovered));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return tspi;
    }

    /**
     * {@inheritDoc}
     */
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
                for (DatabaseObject<TspiData> object : objects) {
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
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<TspiData> object) {
        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setLong(4, object.getObject().getTimestamp());
            preparedStatement.setDouble(5, object.getObject().getPosition().getLatitude().degrees);
            preparedStatement.setDouble(6, object.getObject().getPosition().getLongitude().degrees);
            preparedStatement.setDouble(7, object.getObject().getPosition().getAltitude());
            preparedStatement.setDouble(8, object.getObject().getForwardVelocity());
            preparedStatement.setDouble(9, object.getObject().getVerticalVelocity());
            preparedStatement.setDouble(10, object.getObject().getHeading());
            preparedStatement.setDouble(11, object.getObject().getPitch());
            preparedStatement.setDouble(12, object.getObject().getRoll());
            preparedStatement.setBoolean(13, object.getObject().isRecovered());
            preparedStatement.setInt(14, object.getSourceId());
            preparedStatement.addBatch();
        } catch (SQLException e) {
            String message = String.format("Error inserting %d:%d:%d to %s.",
                    object.getEntityId().getSite(), object.getEntityId().getApplication(),
                    object.getEntityId().getId(), getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
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
                ",latitude REAL" +
                ",longitude REAL" +
                ",altitude REAL" +
                ",forwardVelocity REAL" +
                ",verticalVelocity REAL" +
                ",heading REAL" +
                ",pitch REAL" +
                ",roll REAL" +
                ",isRecovered INTEGER" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(sourceId,site,app,id,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site" +
                ",app" +
                ",id" +
                ",timestamp" +
                ",latitude" +
                ",longitude" +
                ",altitude" +
                ",forwardVelocity" +
                ",verticalVelocity" +
                ",heading" +
                ",pitch" +
                ",roll" +
                ",isRecovered" +
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
                ")";
    }

    /**
     * Creates a temporary table name by name.
     *
     * @param connection The database connection.
     * @return The temporary table name, post creation attempt. Returns empty if a creation didn't occur correctly.
     */
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

    /**
     * Insert the temp TSPI table to the primary TSPI database table.
     *
     * @param connection    The database connection.
     * @param tempTableName The temporary table name.
     */
    private void insertTempTable(Connection connection, String tempTableName) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO " + getTableName() +
                    " SELECT * FROM " + tempTableName + " t1" +
                    " WHERE NOT EXISTS" +
                    "  (SELECT * FROM " + getTableName() + " t2" +
                    "   WHERE t1.sourceId = t2.sourceId" +
                    "    AND t1.site = t2.site" +
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
