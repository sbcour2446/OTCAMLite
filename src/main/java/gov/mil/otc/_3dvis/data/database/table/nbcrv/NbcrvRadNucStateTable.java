package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.table.DatabaseObjectTable;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.nbcrv.RadNucState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvRadNucStateTable extends DatabaseObjectTable<RadNucState> {

    private static final String TABLE_NAME = "nbcrv_rad_nuc_state";

    public List<RadNucState> getStates(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<RadNucState> states = new ArrayList<>();
        String sql = getQuerySql(entityId, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                int quadrant = resultSet.getInt("quadrant");
                double q1Measurement = resultSet.getDouble("q1Measurement");
                String q1Criticality = resultSet.getString("q1Criticality");
                double q2Measurement = resultSet.getDouble("q2Measurement");
                String q2Criticality = resultSet.getString("q2Criticality");
                double q3Measurement = resultSet.getDouble("q3Measurement");
                String q3Criticality = resultSet.getString("q3Criticality");
                double q4Measurement = resultSet.getDouble("q4Measurement");
                String q4Criticality = resultSet.getString("q4Criticality");
                states.add(new RadNucState(timestamp, quadrant, q1Measurement, q1Criticality, q2Measurement,
                        q2Criticality, q3Measurement, q3Criticality, q4Measurement, q4Criticality));
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
                ",quadrant INTEGER" +
                ",q1Measurement REAL" +
                ",q1Criticality TEXT" +
                ",q2Measurement REAL" +
                ",q2Criticality TEXT" +
                ",q3Measurement REAL" +
                ",q3Criticality TEXT" +
                ",q4Measurement REAL" +
                ",q4Criticality TEXT" +
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
                ",quadrant" +
                ",q1Measurement" +
                ",q1Criticality" +
                ",q2Measurement" +
                ",q2Criticality" +
                ",q3Measurement" +
                ",q3Criticality" +
                ",q4Measurement" +
                ",q4Criticality" +
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

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<RadNucState> object) {
        RadNucState radNucState = object.getObject();
        try {
            preparedStatement.setInt(1, object.getSourceId());
            preparedStatement.setInt(2, object.getEntityId().getSite());
            preparedStatement.setInt(3, object.getEntityId().getApplication());
            preparedStatement.setInt(4, object.getEntityId().getId());
            preparedStatement.setLong(5, radNucState.getTimestamp());
            preparedStatement.setDouble(6, radNucState.getQuadrant());
            preparedStatement.setDouble(7, radNucState.getQ1Measurement());
            preparedStatement.setString(8, radNucState.getQ1Criticality());
            preparedStatement.setDouble(9, radNucState.getQ2Measurement());
            preparedStatement.setString(10, radNucState.getQ2Criticality());
            preparedStatement.setDouble(11, radNucState.getQ3Measurement());
            preparedStatement.setString(12, radNucState.getQ3Criticality());
            preparedStatement.setDouble(13, radNucState.getQ4Measurement());
            preparedStatement.setString(14, radNucState.getQ4Criticality());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting to %s.", getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
