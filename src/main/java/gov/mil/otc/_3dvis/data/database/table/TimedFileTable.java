package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.entity.base.EntityId;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimedFileTable extends DatabaseObjectTable<TimedFile> {

    private static final String TABLE_NAME = "timed_image";

    public List<TimedFile> getFiles(Connection connection) {
        List<TimedFile> files = new ArrayList<>();

        String sql = String.format("SELECT * FROM %s ORDER BY timestamp", getTableName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                String filename = resultSet.getString("filename");
                long timestamp = resultSet.getLong("timestamp");
                TimedFile.FileType fileType = TimedFile.FileType.valueOf(resultSet.getString("fileType"));
                String fileGroup = resultSet.getString("fileGroup");
                files.add(new TimedFile(timestamp, new File(filename), fileType, fileGroup));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return files;
    }

    public List<TimedFile> getFiles(Connection connection, EntityId entityId) {
        List<TimedFile> files = new ArrayList<>();

        String sql = String.format("SELECT * FROM %s WHERE site=%d AND app=%d AND id=%d ORDER BY timestamp",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                String filename = resultSet.getString("filename");
                long timestamp = resultSet.getLong("timestamp");
                TimedFile.FileType fileType = TimedFile.FileType.valueOf(resultSet.getString("fileType"));
                String fileGroup = resultSet.getString("fileGroup");
                files.add(new TimedFile(timestamp, new File(filename), fileType, fileGroup));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return files;
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(sourceId INTEGER" +
                ",site INTEGER" +
                ",app INTEGER" +
                ",id INTEGER" +
                ",filename TEXT" +
                ",timestamp INTEGER" +
                ",fileType TEXT" +
                ",fileGroup TEXT" +
                ",PRIMARY KEY(sourceId,site,app,id,filename))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(sourceId" +
                ",site" +
                ",app" +
                ",id" +
                ",filename" +
                ",timestamp" +
                ",fileType" +
                ",fileGroup" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<TimedFile> object) {
        try {
            preparedStatement.setInt(1, object.getSourceId());
            preparedStatement.setInt(2, object.getEntityId().getSite());
            preparedStatement.setInt(3, object.getEntityId().getApplication());
            preparedStatement.setInt(4, object.getEntityId().getId());
            preparedStatement.setString(5, object.getObject().getFile().getAbsolutePath());
            preparedStatement.setLong(6, object.getObject().getTimestamp());
            preparedStatement.setString(7, object.getObject().getFileType().toString());
            preparedStatement.setString(8, object.getObject().getFileGroup());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting %s to %s.",
                    object.getObject().getFile().getAbsolutePath(), getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
