package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.entity.base.EntityId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DatabaseObjectTable<T> extends AbstractDataTable<DatabaseObject<T>> {

    protected String getQuerySql(EntityId entityId) {
        return String.format("SELECT * FROM %s WHERE site=%d AND app=%d AND id=%d",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId());
    }

    protected String getQuerySql(EntityId entityId, List<Integer> dataSourceIds) {
        StringBuilder sourceIdString = new StringBuilder();
        String prefix = "";
        for (int sourceId : dataSourceIds) {
            sourceIdString.append(prefix);
            sourceIdString.append(sourceId);
            prefix = ",";
        }

        String entityIdString = "";
        if (entityId != null) {
            entityIdString = String.format("AND site=%d AND app=%d AND id=%d",
                    entityId.getSite(), entityId.getApplication(), entityId.getId());
        }

        return String.format("SELECT * FROM %s" +
                        " WHERE ROWID IN (" +
                        "  SELECT MIN(ROWID)" +
                        "  FROM %s" +
                        "  WHERE sourceId IN (%s)" +
                        "  %s" +
                        "  GROUP BY site, app, id, timestamp" +
                        " ) " +
                        " ORDER BY timestamp",
                getTableName(), getTableName(), sourceIdString, entityIdString);
    }

    protected String getEventQuerySql(EntityId entityId, List<Integer> dataSourceIds) {
        StringBuilder sourceIdString = new StringBuilder();
        String prefix = "";
        for (int sourceId : dataSourceIds) {
            sourceIdString.append(prefix);
            sourceIdString.append(sourceId);
            prefix = ",";
        }

        String entityIdString = "";
        if (entityId != null) {
            entityIdString = String.format("AND site=%d AND app=%d AND id=%d",
                    entityId.getSite(), entityId.getApplication(), entityId.getId());
        }

        return String.format("SELECT * FROM %s" +
                        " WHERE ROWID IN (" +
                        "  SELECT MIN(ROWID)" +
                        "  FROM %s" +
                        "  WHERE sourceId IN (%s)" +
                        "  %s" +
                        "  GROUP BY eventSite, eventApp, eventId, timestamp" +
                        " ) " +
                        " ORDER BY timestamp",
                getTableName(), getTableName(), sourceIdString, entityIdString);
    }

    public void remove(Connection connection, List<Integer> dataSourceIds) {
        StringBuilder sourceIdString = new StringBuilder();
        String prefix = "";
        for (int sourceId : dataSourceIds) {
            sourceIdString.append(prefix);
            sourceIdString.append(sourceId);
            prefix = ",";
        }

        String deleteSql = String.format("DELETE FROM %s WHERE sourceId IN (%s)", getTableName(), sourceIdString);
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error deleting DataSource " + sourceIdString + " from " + getTableName();
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    public void remove(Connection connection, EntityId entityId) {
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
}
