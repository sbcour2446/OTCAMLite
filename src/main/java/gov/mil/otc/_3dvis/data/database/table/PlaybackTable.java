package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.playback.Playback;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlaybackTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "playback";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(name TEXT" +
                ",path TEXT" +
                ",creationTime INTEGER" +
                ",PRIMARY KEY(name))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(name, path, creationTime) VALUES (?,?,?)";
    }

    public boolean addPlayback(Connection connection, Playback playback) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setString(1, playback.getName());
            preparedStatement.setString(2, playback.getFile().getAbsolutePath());
            preparedStatement.setLong(3, playback.getCreationTime());
            int count = preparedStatement.executeUpdate();
            connection.commit();
            return count != 0;
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
        return false;
    }

    public void removePlayback(Connection connection, Playback playback) {
        String deleteSql = String.format("DELETE FROM %s WHERE name='%s'", getTableName(), playback.getName());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteSql);
            connection.commit();
        } catch (SQLException e) {
            String message = String.format("Error deleting %s from %s", playback.getName(), getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    public List<Playback> getPlaybackList(Connection connection) {
        List<Playback> playbacks = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s ORDER BY name", getTableName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                playbacks.add(new Playback(resultSet.getString(1),
                        new File(resultSet.getString(2)),
                        resultSet.getLong(3)));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return playbacks;
    }
}
