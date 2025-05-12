package gov.mil.otc._3dvis.data.database.table.blackhawk;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.table.DatabaseObjectTable;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.blackhawk.FlightData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlackHawkFlightDataTable extends DatabaseObjectTable<FlightData> {

    private static final String TABLE_NAME = "blackhawk_flight_data";

    public List<FlightData> getFlightData(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<FlightData> fightDataList = new ArrayList<>();
        String sql = getQuerySql(entityId, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                double pressureAltitude = resultSet.getDouble("pressureAltitude");
                double radarAltitude = resultSet.getDouble("radarAltitude");
                double heading = resultSet.getDouble("heading");
                double airspeed = resultSet.getDouble("airspeed");
                String wheelStatus = resultSet.getString("wheelStatus");
                fightDataList.add(new FlightData(timestamp, pressureAltitude, radarAltitude, heading, airspeed, wheelStatus));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return fightDataList;
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
                for (DatabaseObject<FlightData> object : objects) {
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
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<FlightData> object) {
        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setLong(4, object.getObject().getTimestamp());
            preparedStatement.setDouble(5, object.getObject().getPressureAltitude());
            preparedStatement.setDouble(6, object.getObject().getRadarAltitude());
            preparedStatement.setDouble(7, object.getObject().getHeading());
            preparedStatement.setDouble(8, object.getObject().getAirspeed());
            preparedStatement.setString(9, object.getObject().getWheelStatus());
            preparedStatement.setInt(10, object.getSourceId());
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
                ",pressureAltitude REAL" +
                ",radarAltitude REAL" +
                ",heading REAL" +
                ",airspeed REAL" +
                ",wheelStatus TEXT" +
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
                ",pressureAltitude" +
                ",radarAltitude" +
                ",heading" +
                ",airspeed" +
                ",wheelStatus" +
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
        String createTableSql = "CREATE TABLE " + tempTableName +
                "(site INTEGER" +
                ",app INTEGER" +
                ",id INTEGER" +
                ",timestamp INTEGER" +
                ",pressureAltitude REAL" +
                ",radarAltitude REAL" +
                ",heading REAL" +
                ",airspeed REAL" +
                ",wheelStatus TEXT" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(sourceId,site,app,id,timestamp))";
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
     * Insert the temp table to the primary database table.
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
