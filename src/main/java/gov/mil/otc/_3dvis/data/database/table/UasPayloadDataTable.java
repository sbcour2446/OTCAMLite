package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.entity.base.EntityId;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UasPayloadDataTable extends DatabaseObjectTable<UasPayloadData> {

    private static final String TABLE_NAME = "uas_payload_data";

    public List<UasPayloadData> getUasPayloadData(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<UasPayloadData> uasPayloadDataList = new ArrayList<>();
        String sql = getQuerySql(entityId, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");

                int sysOpModeState = resultSet.getInt("sysOpModeState");
                int eoCameraStatus = resultSet.getInt("eoCameraStatus");
                int irPolarityStatus = resultSet.getInt("irPolarityStatus");
                int imageOutputState = resultSet.getInt("imageOutputState");
                double actCenterElAngle = resultSet.getDouble("actCenterElAngle");
                double actVertFieldOfView = resultSet.getDouble("actVertFieldOfView");
                double actCenterAzAngle = resultSet.getDouble("actCenterAzAngle");
                double actHorFieldOfView = resultSet.getDouble("actHorFieldOfView");
                double actSensorRotAngle = resultSet.getDouble("actSensorRotAngle");
                boolean imagePosition = resultSet.getBoolean("imagePosition");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                double altitude = resultSet.getDouble("altitude");
                double reportedRange = resultSet.getDouble("reportedRange");
                int preplanMode = resultSet.getInt("preplanMode");
                int fLaserPointerStatus = resultSet.getInt("fLaserPointerStatus");
                int selLaserRangeFirstLast = resultSet.getInt("selLaserRangeFirstLast");
                int laserDesignatorCode = resultSet.getInt("laserDesignatorCode");
                int laserDesignatorStatus = resultSet.getInt("laserDesignatorStatus");
                int pointingModeState = resultSet.getInt("pointingModeState");
                uasPayloadDataList.add(new UasPayloadData.Builder()
                        .setTimestamp(timestamp)
                        .setSysOpModeState(sysOpModeState)
                        .setEoCameraStatus(eoCameraStatus)
                        .setIrPolarityStatus(irPolarityStatus)
                        .setImageOutputState(imageOutputState)
                        .setActCenterElAngle(actCenterElAngle)
                        .setActVertFieldOfView(actVertFieldOfView)
                        .setActCenterAzAngle(actCenterAzAngle)
                        .setActHorFieldOfView(actHorFieldOfView)
                        .setActSensorRotAngle(actSensorRotAngle)
                        .setImagePosition(imagePosition)
                        .setLatitude(latitude)
                        .setLongitude(longitude)
                        .setAltitude(altitude)
                        .setReportedRange(reportedRange)
                        .setPreplanMode(preplanMode)
                        .setfLaserPointerStatus(fLaserPointerStatus)
                        .setSelLaserRangeFirstLast(selLaserRangeFirstLast)
                        .setLaserDesignatorCode(laserDesignatorCode)
                        .setLaserDesignatorStatus(laserDesignatorStatus)
                        .setPointingModeState(pointingModeState)
                        .build());
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return uasPayloadDataList;
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
                for (DatabaseObject<UasPayloadData> object : objects) {
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
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<UasPayloadData> object) {
        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setLong(4, object.getObject().getTimestamp());
            preparedStatement.setInt(5, object.getObject().getSysOpModeState());
            preparedStatement.setInt(6, object.getObject().getEoCameraStatus());
            preparedStatement.setInt(7, object.getObject().getIrPolarityStatus());
            preparedStatement.setInt(8, object.getObject().getImageOutputState());
            preparedStatement.setDouble(9, object.getObject().getActCenterElAngle());
            preparedStatement.setDouble(10, object.getObject().getActVertFieldOfView());
            preparedStatement.setDouble(11, object.getObject().getActCenterAzAngle());
            preparedStatement.setDouble(12, object.getObject().getActHorFieldOfView());
            preparedStatement.setDouble(13, object.getObject().getActSensorRotAngle());
            preparedStatement.setBoolean(14, object.getObject().isImagePosition());
            preparedStatement.setDouble(15, object.getObject().getLatitude());
            preparedStatement.setDouble(16, object.getObject().getLongitude());
            preparedStatement.setDouble(17, object.getObject().getAltitude());
            preparedStatement.setDouble(18, object.getObject().getReportedRange());
            preparedStatement.setInt(19, object.getObject().getPreplanMode());
            preparedStatement.setInt(20, object.getObject().getfLaserPointerStatus());
            preparedStatement.setInt(21, object.getObject().getSelLaserRangeFirstLast());
            preparedStatement.setInt(22, object.getObject().getLaserDesignatorCode());
            preparedStatement.setInt(23, object.getObject().getLaserDesignatorStatus());
            preparedStatement.setInt(24, object.getObject().getPointingModeState());
            preparedStatement.setInt(25, object.getSourceId());
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
                ",sysOpModeState INTEGER" +
                ",eoCameraStatus INTEGER" +
                ",irPolarityStatus INTEGER" +
                ",imageOutputState INTEGER" +
                ",actCenterElAngle REAL" +
                ",actVertFieldOfView REAL" +
                ",actCenterAzAngle REAL" +
                ",actHorFieldOfView REAL" +
                ",actSensorRotAngle REAL" +
                ",imagePosition INTEGER" +
                ",latitude REAL" +
                ",longitude REAL" +
                ",altitude REAL" +
                ",reportedRange REAL" +
                ",preplanMode INTEGER" +
                ",fLaserPointerStatus INTEGER" +
                ",selLaserRangeFirstLast INTEGER" +
                ",laserDesignatorCode INTEGER" +
                ",laserDesignatorStatus INTEGER" +
                ",pointingModeState INTEGER" +
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
                ",sysOpModeState" +
                ",eoCameraStatus" +
                ",irPolarityStatus" +
                ",imageOutputState" +
                ",actCenterElAngle" +
                ",actVertFieldOfView" +
                ",actCenterAzAngle" +
                ",actHorFieldOfView" +
                ",actSensorRotAngle" +
                ",imagePosition" +
                ",latitude" +
                ",longitude" +
                ",altitude " +
                ",reportedRange" +
                ",preplanMode" +
                ",fLaserPointerStatus" +
                ",selLaserRangeFirstLast" +
                ",laserDesignatorCode" +
                ",laserDesignatorStatus" +
                ",pointingModeState" +
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
                ",sysOpModeState INTEGER" +
                ",eoCameraStatus INTEGER" +
                ",irPolarityStatus INTEGER" +
                ",imageOutputState INTEGER" +
                ",actCenterElAngle REAL" +
                ",actVertFieldOfView REAL" +
                ",actCenterAzAngle REAL" +
                ",actHorFieldOfView REAL" +
                ",actSensorRotAngle REAL" +
                ",imagePosition INTEGER" +
                ",latitude REAL" +
                ",longitude REAL" +
                ",altitude REAL" +
                ",reportedRange REAL" +
                ",preplanMode INTEGER" +
                ",fLaserPointerStatus INTEGER" +
                ",selLaserRangeFirstLast INTEGER" +
                ",laserDesignatorCode INTEGER" +
                ",laserDesignatorStatus INTEGER" +
                ",pointingModeState INTEGER" +
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
