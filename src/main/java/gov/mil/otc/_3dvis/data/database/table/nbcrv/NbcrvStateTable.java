package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.table.DatabaseObjectTable;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvStateTable extends DatabaseObjectTable<NbcrvState> {

    private static final String TABLE_NAME = "nbcrv_state";

    public List<NbcrvState> getStates(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<NbcrvState> states = new ArrayList<>();
        String sql = getQuerySql(entityId, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                double roll = resultSet.getDouble("roll");
                double pitch = resultSet.getDouble("pitch");
                double yaw = resultSet.getDouble("yaw");
                double ptuYaw = resultSet.getDouble("ptuYaw");
                double imcadTilt = resultSet.getDouble("imcadTilt");
                double imcadAngle = resultSet.getDouble("imcadAngle");
                double imcadWidth = resultSet.getDouble("imcadWidth");
                double csdsTilt = resultSet.getDouble("csdsTilt");
                double csdsAngle = resultSet.getDouble("csdsAngle");
                double csdsWidth = resultSet.getDouble("csdsWidth");
                states.add(new NbcrvState(timestamp, latitude, longitude, roll, pitch, yaw, ptuYaw, imcadTilt,
                        imcadAngle, imcadWidth, csdsTilt, csdsAngle, csdsWidth));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return states;
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
                ",latitude REAL" +
                ",longitude REAL" +
                ",roll REAL" +
                ",pitch REAL" +
                ",yaw REAL" +
                ",ptuYaw REAL" +
                ",imcadTilt REAL" +
                ",imcadAngle REAL" +
                ",imcadWidth REAL" +
                ",csdsTilt REAL" +
                ",csdsAngle REAL" +
                ",csdsWidth REAL" +
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
                ",latitude" +
                ",longitude" +
                ",roll" +
                ",pitch" +
                ",yaw" +
                ",ptuYaw" +
                ",imcadTilt" +
                ",imcadAngle" +
                ",imcadWidth" +
                ",csdsTilt" +
                ",csdsAngle" +
                ",csdsWidth" +
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
                ")";
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<NbcrvState> object) {
        NbcrvState nbcrvState = object.getObject();
        try {
            preparedStatement.setInt(1, object.getSourceId());
            preparedStatement.setInt(2, object.getEntityId().getSite());
            preparedStatement.setInt(3, object.getEntityId().getApplication());
            preparedStatement.setInt(4, object.getEntityId().getId());
            preparedStatement.setLong(5, nbcrvState.getTimestamp());
            preparedStatement.setDouble(6, nbcrvState.getLatitude());
            preparedStatement.setDouble(7, nbcrvState.getLongitude());
            preparedStatement.setDouble(8, nbcrvState.getRoll());
            preparedStatement.setDouble(9, nbcrvState.getPitch());
            preparedStatement.setDouble(10, nbcrvState.getYaw());
            preparedStatement.setDouble(11, nbcrvState.getPtuYaw());
            preparedStatement.setDouble(12, nbcrvState.getImcadTilt());
            preparedStatement.setDouble(13, nbcrvState.getImcadAngle());
            preparedStatement.setDouble(14, nbcrvState.getImcadWidth());
            preparedStatement.setDouble(15, nbcrvState.getCsdsTilt());
            preparedStatement.setDouble(16, nbcrvState.getCsdsAngle());
            preparedStatement.setDouble(17, nbcrvState.getCsdsWidth());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting to %s.", getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
