package gov.mil.otc._3dvis.data.database.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for the database version table.
 */
public class DatabaseVersionTable {

    private DatabaseVersionTable() {
    }

    /**
     * Verify the database version.
     *
     * @param connection The database connection.
     * @param version    The database version.
     * @return True if version is verified.
     */
    public static boolean verifyDatabaseVersion(Connection connection, String version) {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='version';";
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (!resultSet.next()) {
                return false;
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Error reading version table.", e);
            return false;
        }

        sql = "SELECT version FROM version";
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                return version.equals(resultSet.getString("version"));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Error reading version table.", e);
        }

        return false;
    }

    /**
     * Creates and updates the version table if it does not exists.
     *
     * @param connection The database connection.
     * @param version    The database version.
     * @return True if table exists or create and update occurred successfully.
     */
    public static boolean createIfNotExists(Connection connection, String version) {
        String createSql = "CREATE TABLE IF NOT EXISTS version (version TEXT,created INTEGER)";
        try (Statement statement = connection.createStatement()) {
            statement.execute(createSql);
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Could not create version table.", e);
            return false;
        }

        String insertSql = String.format("INSERT INTO version (version,created) VALUES (%s, %d)",
                version, System.currentTimeMillis());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(insertSql);
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Could not update version table.", e);
            return false;
        }

        return true;
    }

    public static String getVersion(Connection connection) {
        String getVersionSql = "SELECT version FROM version";
        try (ResultSet resultSet = connection.createStatement().executeQuery(getVersionSql)) {
            if (resultSet.next()) {
                return resultSet.getString("version");
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return "";
    }
}
