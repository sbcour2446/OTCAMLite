package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.DatabaseObjectPair;
import gov.mil.otc._3dvis.data.database.table.DatabaseObjectTable;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.DeviceState;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceStateTable extends DatabaseObjectTable<DatabaseObjectPair<Device, DeviceState>> {

    private static final String TABLE_NAME = "nbcrv_device_state";

    public void getDeviceStates(Connection connection, EntityId entityId, Device device, List<Integer> sourceIds) {
        StringBuilder sourceIdString = new StringBuilder();
        String prefix = "";
        for (int sourceId : sourceIds) {
            sourceIdString.append(prefix);
            sourceIdString.append(sourceId);
            prefix = ",";
        }

        String sql = String.format("SELECT * FROM %s" +
                        " WHERE ROWID IN (" +
                        "  SELECT MIN(ROWID)" +
                        "  FROM %s" +
                        "  WHERE sourceId IN (%s)" +
                        "  AND site=%d AND app=%d AND id=%d AND deviceName='%s'" +
                        "  GROUP BY site, app, id, deviceName, timestamp" +
                        " ) ",
                getTableName(), getTableName(), sourceIdString,
                entityId.getSite(), entityId.getApplication(), entityId.getId(), device.getName());

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                boolean alert = resultSet.getBoolean("alert");
                String alertReason = resultSet.getString("alertReason");
                String state = resultSet.getString("state");
                String stateDescription = resultSet.getString("stateDescription");
                String fault = resultSet.getString("fault");
                String major = resultSet.getString("major");
                String minor = resultSet.getString("minor");
                String info = resultSet.getString("info");
                String activity = resultSet.getString("activity");
                String operator = resultSet.getString("operator");
                Double pitch = resultSet.getDouble("pitch");
                if (resultSet.wasNull()) {
                    pitch = null;
                }
                Double roll = resultSet.getDouble("roll");
                if (resultSet.wasNull()) {
                    roll = null;
                }
                Double yaw = resultSet.getDouble("yaw");
                if (resultSet.wasNull()) {
                    yaw = null;
                }
                device.addDeviceState(new DeviceState(timestamp, alert, alertReason, state, stateDescription,
                        major, minor, info, activity, operator, pitch, roll, yaw));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
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
                ",deviceName TEXT" +
                ",timestamp INTEGER" +
                ",alert INTEGER" +
                ",alertReason TEXT" +
                ",state TEXT" +
                ",stateDescription TEXT" +
                ",fault TEXT" +
                ",major TEXT" +
                ",minor TEXT" +
                ",info TEXT" +
                ",activity TEXT" +
                ",operator TEXT" +
                ",pitch REAL" +
                ",roll REAL" +
                ",yaw REAL" +
                ",PRIMARY KEY(site,app,id,deviceName,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(sourceId" +
                ",site" +
                ",app" +
                ",id" +
                ",deviceName" +
                ",timestamp" +
                ",alert" +
                ",alertReason" +
                ",state" +
                ",stateDescription" +
                ",fault" +
                ",major" +
                ",minor" +
                ",info" +
                ",activity" +
                ",operator" +
                ",pitch" +
                ",roll" +
                ",yaw" +
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
                ")";
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement,
                             DatabaseObject<DatabaseObjectPair<Device, DeviceState>> object) {
        Device device = object.getObject().getObject1();
        DeviceState deviceState = object.getObject().getObject2();
        try {
            preparedStatement.setInt(1, object.getSourceId());
            preparedStatement.setInt(2, object.getEntityId().getSite());
            preparedStatement.setInt(3, object.getEntityId().getApplication());
            preparedStatement.setInt(4, object.getEntityId().getId());
            preparedStatement.setString(5, device.getName());
            preparedStatement.setLong(6, deviceState.getTimestamp());
            preparedStatement.setBoolean(7, deviceState.isAlert());
            preparedStatement.setString(8, deviceState.getAlertReason());
            preparedStatement.setString(9, deviceState.getState());
            preparedStatement.setString(10, deviceState.getStateDescription());
            preparedStatement.setString(11, deviceState.getFault());
            preparedStatement.setString(12, deviceState.getMajor());
            preparedStatement.setString(13, deviceState.getMinor());
            preparedStatement.setString(14, deviceState.getInfo());
            preparedStatement.setString(15, deviceState.getActivity());
            preparedStatement.setString(16, deviceState.getOperator());
            if (deviceState.getPitch() != null) {
                preparedStatement.setDouble(17, deviceState.getPitch());
            } else {
                preparedStatement.setNull(17, Types.DOUBLE);
            }
            if (deviceState.getRoll() != null) {
                preparedStatement.setDouble(18, deviceState.getRoll());
            } else {
                preparedStatement.setNull(18, Types.DOUBLE);
            }
            if (deviceState.getYaw() != null) {
                preparedStatement.setDouble(19, deviceState.getYaw());
            } else {
                preparedStatement.setNull(19, Types.DOUBLE);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting to %s.", getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
