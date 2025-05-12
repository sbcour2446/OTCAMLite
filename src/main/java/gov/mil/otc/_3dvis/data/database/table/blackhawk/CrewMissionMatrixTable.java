package gov.mil.otc._3dvis.data.database.table.blackhawk;

import gov.mil.otc._3dvis.data.database.table.AbstractBaseTable;
import gov.mil.otc._3dvis.data.mission.Mission;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CrewMissionMatrixTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "blackhawk_crew_mission_matrix_table";

    protected void add(Connection connection, String mission, int flightNumber, int tailNumber, String pin,
                       String seat, String role, long startTime, long endTime, int sourceId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setString(1, mission);
            preparedStatement.setInt(2, flightNumber);
            preparedStatement.setInt(3, tailNumber);
            preparedStatement.setString(4, pin);
            preparedStatement.setString(5, seat);
            preparedStatement.setString(6, role);
            preparedStatement.setLong(7, startTime);
            preparedStatement.setLong(8, endTime);
            preparedStatement.setInt(9, sourceId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    protected List<String> getRoles(Connection connection, Mission mission, int tailNumber) {
        List<String> roles = new ArrayList<>();
        String sql = String.format("SELECT role FROM %s WHERE mission='%s' AND tailNumber=%d",
                getTableName(), mission.getName(), tailNumber);
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                roles.add(resultSet.getString("role"));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return roles;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(mission TEXT" +
                ",flightNumber INTEGER" +
                ",tailNumber INTEGER" +
                ",pin TEXT" +
                ",seat TEXT" +
                ",role TEXT" +
                ",startTime INTEGER" +
                ",endTime INTEGER" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(mission,flightNumber,tailNumber,pin))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(mission" +
                ",flightNumber" +
                ",tailNumber" +
                ",pin" +
                ",seat" +
                ",role" +
                ",startTime" +
                ",endTime" +
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
                ")";
    }
}
