package gov.mil.otc._3dvis.data.database.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract class for a database table.
 */
public abstract class AbstractDataTable<T> extends AbstractBaseTable {

    protected final List<T> objects = new ArrayList<>();

    /**
     * Add an object to the queue for inserting into the database.
     *
     * @param object The object.
     */
    public void add(T object) {
        synchronized (objects) {
            objects.add(object);
        }
    }

    /**
     * Insert data to the database and clear queues.
     *
     * @param connection The database connection.
     */
    public void insertToDatabase(Connection connection) {
        synchronized (objects) {
            if (objects.isEmpty()) {
                return;
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            synchronized (objects) {
                for (T object : objects) {
                    insertRow(preparedStatement, object);
                }
                objects.clear();
            }
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    /**
     * Insert the row.
     */
    protected abstract void insertRow(PreparedStatement preparedStatement, T object);
}
