package gov.mil.otc._3dvis.data.database.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppIdTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "app_id";

    public int getId(Connection connection, String name) {
        String sql = String.format("SELECT ROWID FROM %s WHERE name='%s'", getTableName(), name);
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return 0;
    }

    public int createId(Connection connection, String name) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
        return getId(connection, name);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName + "(name TEXT, PRIMARY KEY(name))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName + "(name) VALUES (?)";
    }
}
