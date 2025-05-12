package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.datamodel.aircraft.TspiExtendedData;
import gov.mil.otc._3dvis.entity.base.EntityId;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TspiExtendedDataTable extends DatabaseObjectTable<TspiExtendedData> {

    private static final String TABLE_NAME = "tspi_extended_data";

    public List<TspiExtendedData> getTspiExtendedData(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<TspiExtendedData> tspiExtendedDataList = new ArrayList<>();
        String sql = getQuerySql(entityId, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                double altitudeType = resultSet.getDouble("altitudeType");
                double uSpeed = resultSet.getDouble("uSpeed");
                double vSpeed = resultSet.getDouble("vSpeed");
                double wSpeed = resultSet.getDouble("wSpeed");
                double uAcceleration = resultSet.getDouble("uAcceleration");
                double vAcceleration = resultSet.getDouble("vAcceleration");
                double wAcceleration = resultSet.getDouble("wAcceleration");
                double phi = resultSet.getDouble("phi");
                double theta = resultSet.getDouble("theta");
                double psi = resultSet.getDouble("psi");
                double phiDot = resultSet.getDouble("phiDot");
                double thetaDot = resultSet.getDouble("thetaDot");
                double psiDot = resultSet.getDouble("psiDot");
                tspiExtendedDataList.add(new TspiExtendedData.Builder()
                        .setTimestamp(timestamp)
                        .setAltitudeType(altitudeType)
                        .setUSpeed(uSpeed)
                        .setVSpeed(vSpeed)
                        .setWSpeed(wSpeed)
                        .setUAcceleration(uAcceleration)
                        .setVAcceleration(vAcceleration)
                        .setWAcceleration(wAcceleration)
                        .setPhi(phi)
                        .setTheta(theta)
                        .setPsi(psi)
                        .setPhiDot(phiDot)
                        .setThetaDot(thetaDot)
                        .setPsiDot(psiDot)
                        .build());
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return tspiExtendedDataList;
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
                for (DatabaseObject<TspiExtendedData> object : objects) {
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
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<TspiExtendedData> object) {
        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setLong(4, object.getObject().getTimestamp());
            preparedStatement.setDouble(5, object.getObject().getAltitudeType());
            preparedStatement.setDouble(6, object.getObject().getUSpeed());
            preparedStatement.setDouble(7, object.getObject().getVSpeed());
            preparedStatement.setDouble(8, object.getObject().getWSpeed());
            preparedStatement.setDouble(9, object.getObject().getUAcceleration());
            preparedStatement.setDouble(10, object.getObject().getVAcceleration());
            preparedStatement.setDouble(11, object.getObject().getWAcceleration());
            preparedStatement.setDouble(12, object.getObject().getPhi());
            preparedStatement.setDouble(13, object.getObject().getTheta());
            preparedStatement.setDouble(14, object.getObject().getPsi());
            preparedStatement.setDouble(15, object.getObject().getPhiDot());
            preparedStatement.setDouble(16, object.getObject().getThetaDot());
            preparedStatement.setDouble(17, object.getObject().getPsiDot());
            preparedStatement.setInt(18, object.getSourceId());
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
                ",altitudeType REAL" +
                ",uSpeed REAL" + //north
                ",vSpeed REAL" + //east
                ",wSpeed REAL" + //vertical
                ",uAcceleration REAL" +
                ",vAcceleration REAL" +
                ",wAcceleration REAL" +
                ",phi REAL" +
                ",theta REAL" +
                ",psi REAL" +
                ",phiDot REAL" +
                ",thetaDot REAL" +
                ",psiDot REAL" +
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
                ",altitudeType" +
                ",uSpeed" +
                ",vSpeed" +
                ",wSpeed" +
                ",uAcceleration" +
                ",vAcceleration" +
                ",wAcceleration" +
                ",phi" +
                ",theta" +
                ",psi" +
                ",phiDot" +
                ",thetaDot" +
                ",psiDot" +
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
                ",altitudeType REAL" +
                ",uSpeed REAL" +
                ",vSpeed REAL" +
                ",wSpeed REAL" +
                ",uAcceleration REAL" +
                ",vAcceleration REAL" +
                ",wAcceleration REAL" +
                ",phi REAL" +
                ",theta REAL" +
                ",psi REAL" +
                ",phiDot REAL" +
                ",thetaDot REAL" +
                ",psiDot REAL" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(site,app,id,timestamp))";
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
            statement.executeUpdate("INSERT INTO " + getTableName()
                    + " SELECT * FROM " + tempTableName + " t1"
                    + " WHERE NOT EXISTS"
                    + "  (SELECT * FROM " + getTableName() + " t2"
                    + "   WHERE t1.site = t2.site"
                    + "    AND t1.app = t2.app"
                    + "    AND t1.id = t2.id"
                    + "    AND t1.timestamp = t2.timestamp)");
            connection.commit();
        } catch (SQLException e) {
            String message = String.format("Error inserting %s table.", tempTableName);
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
