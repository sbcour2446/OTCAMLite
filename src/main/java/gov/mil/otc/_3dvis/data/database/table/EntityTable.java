package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityTable extends AbstractDataTable<IEntity> {

    private static final String TABLE_NAME = "entity";

    public List<EntityId> getEntityIds(Connection connection) {
        List<EntityId> entityIds = new ArrayList<>();

        String sql = String.format("SELECT site, app, id FROM %s ORDER BY site, app, id", getTableName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                int site = resultSet.getInt("site");
                int app = resultSet.getInt("app");
                int id = resultSet.getInt("id");
                entityIds.add(new EntityId(site, app, id));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return entityIds;
    }

    public Class<?> getClassType(Connection connection, EntityId entityId) {
        Class<?> classType = null;
        String sql = String.format("SELECT classType FROM %s WHERE site=%d AND app=%d AND id=%d",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                String classTypeString = resultSet.getString("classType");
                classType = Class.forName(classTypeString);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return classType;
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, IEntity object) {
        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setString(4, object.getClass().getName());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting %d:%d:%d to %s",
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
                "(site INTEGER,app INTEGER,id INTEGER,classType TEXT,PRIMARY KEY(site,app,id))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR REPLACE INTO " + tableName +
                "(site,app,id,classType) VALUES (?,?,?,?)";
    }
}
