package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
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

public class EntityFileTable extends DatabaseObjectTable<String> {

    private static final String TABLE_NAME = "entity_file";

    public List<File> getFiles(Connection connection, EntityId entityId, String missionName) {
        List<File> files = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s WHERE site=%d AND app=%d AND id=%d AND missionName='%s'",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId(), missionName);
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                String fileName = resultSet.getString("fileName");
                files.add(new File(fileName));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return files;
    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(site INTEGER" +
                ",app INTEGER" +
                ",id INTEGER" +
                ",sourceId INTEGER" +
                ",missionName TEXT" +
                ",PRIMARY KEY(site,app,id,sourceId,missionName))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site" +
                ",app" +
                ",id" +
                ",sourceId" +
                ",missionName" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<String> object) {
        String missionName = object.getObject();
        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setInt(4, object.getSourceId());
            preparedStatement.setString(5, missionName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting to %s.", getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
