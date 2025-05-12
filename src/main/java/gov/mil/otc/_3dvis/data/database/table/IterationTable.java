package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.iteration.Iteration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IterationTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "iteration";

    public void addIteration(Connection connection, Iteration iteration) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setString(1, iteration.getName());
            preparedStatement.setLong(2, iteration.getStartTime());
            preparedStatement.setLong(3, iteration.getStopTime());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    public void updateIteration(Connection connection, Iteration oldIteration, Iteration newIteration) {
        String updateSql = String.format("UPDATE %s SET name=?, startTime=?, stopTime=? WHERE name=?", getTableName());
        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSql)) {
            preparedStatement.setString(1, newIteration.getName());
            preparedStatement.setLong(2, newIteration.getStartTime());
            preparedStatement.setLong(3, newIteration.getStopTime());
            preparedStatement.setString(4, oldIteration.getName());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error updating %s", getTableName()), e);
        }
    }

    public void removeIteration(Connection connection, String name) {
        String deleteSql = String.format("DELETE FROM %s WHERE name=%s", getTableName(), name);
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteSql);
            connection.commit();
        } catch (SQLException e) {
            String message = String.format("Error deleting %s from %s", name, getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    public List<Iteration> getIterations(Connection connection) {
        List<Iteration> iterations = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s ORDER BY startTime", getTableName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                iterations.add(new Iteration(resultSet.getString(1),
                        resultSet.getLong(2),
                        resultSet.getLong(3)));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return iterations;
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
