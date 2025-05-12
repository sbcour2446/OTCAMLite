package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.table.DatabaseObjectTable;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import gov.mil.otc._3dvis.project.nbcrv.RegionType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvDetectionTable extends DatabaseObjectTable<NbcrvDetection> {

    private static final String TABLE_NAME = "nbcrv_detection";

    public List<NbcrvDetection> getNbcrvDetection(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<NbcrvDetection> nbcrvDetections = new ArrayList<>();
        String sql = getQuerySql(entityId);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                String deviceName = resultSet.getString("deviceName");
                String regionTypeString = resultSet.getString("regionType");
                RegionType regionType = RegionType.valueOf(regionTypeString);
                String description = resultSet.getString("description");
                double arcAngle = resultSet.getDouble("arcAngle");
                double radius = resultSet.getDouble("radius");
                double direction = resultSet.getDouble("direction");
                double measurement = resultSet.getDouble("measurement");
                String positions = resultSet.getString("positions");
                nbcrvDetections.add(NbcrvDetection.create(timestamp, deviceName, regionType, description,
                        positions, arcAngle, radius, direction, measurement));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "NbcrvDetectionTable::getNbcrvDetection", e);
        }

        return nbcrvDetections;
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
                ",deviceName TEXT" +
                ",regionType TEXT" +
                ",description TEXT" +
                ",arcAngle REAL" +
                ",radius REAL" +
                ",direction REAL" +
                ",measurement REAL" +
                ",positions TEXT" +
                ",PRIMARY KEY(sourceId,site,app,id,timestamp,deviceName,regionType))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(sourceId" +
                ",site" +
                ",app" +
                ",id" +
                ",timestamp" +
                ",deviceName" +
                ",regionType" +
                ",description" +
                ",arcAngle" +
                ",radius" +
                ",direction" +
                ",measurement" +
                ",positions" +
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
                for (DatabaseObject<NbcrvDetection> object : objects) {
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
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<NbcrvDetection> object) {
        NbcrvDetection nbcrvDetection = object.getObject();
        try {
            preparedStatement.setInt(1, object.getSourceId());
            preparedStatement.setInt(2, object.getEntityId().getSite());
            preparedStatement.setInt(3, object.getEntityId().getApplication());
            preparedStatement.setInt(4, object.getEntityId().getId());
            preparedStatement.setLong(5, nbcrvDetection.getTimestamp());
            preparedStatement.setString(6, nbcrvDetection.getDeviceName());
            preparedStatement.setString(7, nbcrvDetection.getRegionType().toString());
            preparedStatement.setString(8, nbcrvDetection.getDescription());
            preparedStatement.setDouble(9, nbcrvDetection.getArcAngle());
            preparedStatement.setDouble(10, nbcrvDetection.getRadius());
            preparedStatement.setDouble(11, nbcrvDetection.getDirection());
            preparedStatement.setDouble(12, nbcrvDetection.getMeasurement());
            preparedStatement.setString(13, nbcrvDetection.getPositionListString());
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
                    "    AND t1.timestamp = t2.timestamp" +
                    "    AND t1.deviceName = t2.deviceName" +
                    "    AND t1.regionType = t2.regionType)");
            connection.commit();
        } catch (SQLException e) {
            String message = String.format("Error inserting %s table.", tempTableName);
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
