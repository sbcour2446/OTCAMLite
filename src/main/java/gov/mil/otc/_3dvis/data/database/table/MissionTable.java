package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.mission.Mission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "mission";

    public void addMission(Connection connection, Mission mission) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setString(1, mission.getName());
            preparedStatement.setLong(2, mission.getTimestamp());
            preparedStatement.setLong(3, mission.getStopTime());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    public void removeMission(Connection connection, Mission mission) {
        String deleteSql = String.format("DELETE FROM %s WHERE name='%s'", getTableName(), mission.getName());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteSql);
            connection.commit();
        } catch (SQLException e) {
            String message = String.format("Error deleting %s from %s", mission.getName(), getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    public List<Mission> getMissions(Connection connection) {
        List<Mission> missions = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s ORDER BY startTime", getTableName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                missions.add(new Mission(resultSet.getString(1),
                        resultSet.getLong(2),
                        resultSet.getLong(3)));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return missions;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(name TEXT" +
                ",startTime INTEGER" +
                ",stopTime INTEGER" +
                ",PRIMARY KEY(name))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(name, startTime, stopTime) VALUES (?,?,?)";
    }
}
