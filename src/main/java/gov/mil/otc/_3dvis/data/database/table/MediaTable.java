package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.media.MediaFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaTable extends DatabaseObjectTable<MediaFile> {

    private static final String TABLE_NAME = "media";

    public List<MediaFile> getMedia(Connection connection, EntityId entityId) {
        List<MediaFile> mediaFiles = new ArrayList<>();

        String sql = String.format("SELECT * FROM %s WHERE site=%d AND app=%d AND id=%d ORDER BY timestamp",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                String filename = resultSet.getString("filename");
                long timestamp = resultSet.getLong("timestamp");
                long stopTime = resultSet.getLong("stopTime");
                String mediaGroup = resultSet.getString("mediaGroup");
                String mediaSet = resultSet.getString("mediaSet");
                mediaFiles.add(new MediaFile(filename, timestamp, stopTime, mediaGroup, mediaSet));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return mediaFiles;
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<MediaFile> object) {
        String path;
        if (object.getObject().isUseRelativePath()) {
            path = getRelativePath(object.getObject());
        } else {
            path = object.getObject().getAbsolutePath();
        }

        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setString(4, path);
            preparedStatement.setLong(5, object.getObject().getStartTime());
            preparedStatement.setLong(6, object.getObject().getStopTime());
            preparedStatement.setString(7, object.getObject().getMediaGroup());
            preparedStatement.setString(8, object.getObject().getMediaSet());
            preparedStatement.setInt(9, object.getSourceId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting %d:%d:%d to %s.",
                    object.getEntityId().getSite(), object.getEntityId().getApplication(),
                    object.getEntityId().getId(), getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(site INTEGER" +
                ",app INTEGER" +
                ",id INTEGER" +
                ",filename TEXT" +
                ",timestamp INTEGER" +
                ",stopTime INTEGER" +
                ",mediaGroup TEXT" +
                ",mediaSet TEXT" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(sourceId,site,app,id))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site" +
                ",app" +
                ",id" +
                ",filename" +
                ",timestamp" +
                ",stopTime" +
                ",mediaGroup" +
                ",mediaSet" +
                ",sourceId" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }

    private String getRelativePath(File file) {
        String relativePath;
        try {
            Path jarPath = Paths.get(new File(new File(".").getCanonicalPath()).toURI());
            Path selectedPath = Paths.get(file.toURI());
            relativePath = jarPath.relativize(selectedPath).toString();
        } catch (IOException e) {
            relativePath = file.getAbsolutePath();
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return relativePath;
    }
}
