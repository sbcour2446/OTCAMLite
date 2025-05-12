package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.mission.Mission;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MissionDataSourceTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "mission_datasource";

    public void addDataSource(Connection connection, Mission mission, DataSource dataSource) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setString(1, mission.getName());
            preparedStatement.setLong(2, dataSource.getId());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    public void removeDataSource(Connection connection, Mission mission, DataSource dataSource) {
        String deleteSql = String.format("DELETE FROM %s WHERE name='%s' AND sourceId=%d",
                getTableName(), mission.getName(), dataSource.getId());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteSql);
            connection.commit();
        } catch (SQLException e) {
            String message = String.format("Error deleting %s:%d from %s", mission.getName(), dataSource.getId(),
                    getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
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

    public List<Integer> getDataSourceIds(Connection connection, Mission mission) {
        List<Integer> dataSourceIds = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE name='%s' ORDER BY sourceId", getTableName(),
                mission.getName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                dataSourceIds.add(resultSet.getInt("sourceId"));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return dataSourceIds;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(name TEXT" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(name, sourceId))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(name, sourceId) VALUES (?,?)";
    }
}
