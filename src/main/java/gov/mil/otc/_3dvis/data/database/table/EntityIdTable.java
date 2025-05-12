package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.entity.base.EntityId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityIdTable extends AbstractDataTable<EntityId> {

    private static final String TABLE_NAME = "entity_id";

    public EntityId getAndAddNextAvailableId(Connection connection, int site, int app) {
        int nextId = 1;
        String sql = String.format("SELECT MAX(id) FROM %s WHERE site=%d AND app=%d",
                getTableName(), site, app);
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                nextId = resultSet.getInt(1) + 1;
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        EntityId entityId = new EntityId(site, app, nextId);
        addId(connection, entityId);
        return entityId;
    }

    public void addId(Connection connection, EntityId entityId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            insertRow(preparedStatement, entityId);
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

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

    public void removeEntityId(Connection connection, EntityId entityId) {
        String sql = String.format("DELETE FROM %s WHERE site=%d AND app=%d AND id=%d",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId());
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error deleting " + entityId + " from " + getTableName();
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, EntityId object) {
        try {
            preparedStatement.setInt(1, object.getSite());
            preparedStatement.setInt(2, object.getApplication());
            preparedStatement.setInt(3, object.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting %d:%d:%d to %s",
                    object.getSite(), object.getApplication(), object.getId(), getTableName());
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
                "(site INTEGER,app INTEGER,id INTEGER,PRIMARY KEY(site,app,id))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site,app,id) VALUES (?,?,?)";
    }
}
