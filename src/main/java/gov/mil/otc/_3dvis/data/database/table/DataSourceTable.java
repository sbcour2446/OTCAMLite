package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.DataSource;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSourceTable {

    private static final String TABLE_NAME = "datasource";

    /**
     * Create the table in the database if not exists.
     *
     * @param connection The database connection.
     * @return True if table was created successfully.
     */
    public boolean createTable(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(getCreateTableSql());
        } catch (SQLException e) {
            String message = "Could not create " + getTableName() + " table";
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return true;
    }

    public DataSource createDataSource(Connection connection, String name, long startTime, long stopTime, boolean use) {
        DataSource dataSource = new DataSource(getNextId(connection), name, startTime, stopTime, use);
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql())) {
            insertRow(preparedStatement, dataSource);
            return dataSource;
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
        return null;
    }

    public void removeDataSources(Connection connection, List<Integer> dataSourceIds) {
        StringBuilder sourceIdString = new StringBuilder();
        String prefix = "";
        for (int sourceId : dataSourceIds) {
            sourceIdString.append(prefix);
            sourceIdString.append(sourceId);
            prefix = ",";
        }

        String deleteSql = String.format("DELETE FROM %s WHERE id IN (%s)", getTableName(), sourceIdString);
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error deleting DataSource " + sourceIdString;
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    public void updateDataSource(Connection connection, DataSource dataSource) {
        String sql = String.format("UPDATE %s SET stopTime=?, use=? WHERE id=?", getTableName());
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, dataSource.getStopTime());
            preparedStatement.setBoolean(2, dataSource.isUse());
            preparedStatement.setInt(3, dataSource.getId());
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error updating DataSource " + dataSource.getId();
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    public List<DataSource> getDataSources(Connection connection) {
        List<DataSource> dataSources = new ArrayList<>();

        String sql = String.format("SELECT * FROM %s ORDER BY id", getTableName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                long startTime = resultSet.getLong("startTime");
                long stopTime = resultSet.getLong("stopTime");
                boolean use = resultSet.getBoolean("use");
                dataSources.add(new DataSource(id, name, startTime, stopTime, use));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return dataSources;
    }

    private int getNextId(Connection connection) {
        String sql = String.format("SELECT MAX(id) FROM %s", getTableName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return 0;
    }

    protected void insertRow(PreparedStatement preparedStatement, DataSource object) {
        try {
            preparedStatement.setInt(1, object.getId());
            preparedStatement.setString(2, object.getName());
            preparedStatement.setLong(3, object.getStartTime());
            preparedStatement.setLong(4, object.getStopTime());
            preparedStatement.setBoolean(5, object.isUse());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting %s(%d) to %s",
                    object.getName(), object.getId(), getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    protected String getTableName() {
        return TABLE_NAME;
    }

    protected String getCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + getTableName() +
                "(id INTEGER" +
                ",name TEXT" +
                ",startTime INTEGER" +
                ",stopTime INTEGER" +
                ",use INTEGER" +
                ",PRIMARY KEY(id))";
    }

    protected String getInsertSql() {
        return "INSERT OR IGNORE INTO " + getTableName() +
                "(id" +
                ",name" +
                ",startTime" +
                ",stopTime" +
                ",use)" +
                " VALUES (?,?,?,?,?)";
    }
}
