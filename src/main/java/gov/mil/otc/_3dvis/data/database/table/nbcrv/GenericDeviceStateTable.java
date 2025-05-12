package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.table.AbstractBaseTable;
import gov.mil.otc._3dvis.project.nbcrv.GenericDeviceState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericDeviceStateTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "nbcrv_device_state_generic";

    public List<GenericDeviceState> getDeviceGenericStates(Connection connection, int deviceId) {
        List<GenericDeviceState> genericDeviceStates = new ArrayList<>();
        String sql = String.format("SELECT DISTINCT(timestamp) FROM %s WHERE deviceId=%d", getTableName(), deviceId);
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                genericDeviceStates.add(new GenericDeviceState(timestamp, getValueMap(connection, deviceId, timestamp)));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return genericDeviceStates;
    }

    private Map<String, String> getValueMap(Connection connection, int deviceId, long timestamp) {
        Map<String, String> valueMap = new HashMap<>();
        String sql = String.format("SELECT * FROM %s WHERE deviceId=%d AND timestamp=%d", getTableName(), deviceId, timestamp);
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                String alertReason = resultSet.getString("fieldName");
                String state = resultSet.getString("fieldValue");
                valueMap.put(alertReason, state);
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return valueMap;
    }

    protected void insert(Connection connection, int deviceId, GenericDeviceState genericDeviceState) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            for (String fieldName : genericDeviceState.getFieldNames()) {
                preparedStatement.setInt(1, deviceId);
                preparedStatement.setLong(2, genericDeviceState.getTimestamp());
                preparedStatement.setString(3, fieldName);
                preparedStatement.setString(4, genericDeviceState.getValue(fieldName));
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(deviceId INTEGER" +
                ",timestamp INTEGER" +
                ",fieldName TEXT" +
                ",fieldValue TEXT" +
                ",PRIMARY KEY(deviceId,fieldName,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(deviceId" +
                ",timestamp" +
                ",fieldName" +
                ",fieldValue" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }
}
