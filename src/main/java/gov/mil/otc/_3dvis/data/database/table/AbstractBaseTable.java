package gov.mil.otc._3dvis.data.database.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base abstract table for the database.
 */
public abstract class AbstractBaseTable {

    /**
     * Create the table in the database if not exists.
     *
     * @param connection The database connection.
     * @return True if table was created successfully.
     */
    public boolean createTable(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(getCreateTableSql(getTableName()));
            connection.commit();
        } catch (SQLException e) {
            String message = "Could not create " + getTableName() + " table";
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return true;
    }

    /**
     * Get the table name.
     *
     * @return The table name.
     */
    public abstract String getTableName();

    /**
     * Get the SQL for creating the table.
     *
     * @return The 'create' table SQL string.
     */
    protected abstract String getCreateTableSql(String tableName);

    /**
     * Get the SQL for inserting a new row into the table.
     *
     * @return The insert SQL string.
     */
    protected abstract String getInsertSql(String tableName);
}
