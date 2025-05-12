package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.table.DatabaseObjectTable;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.nbcrv.Device;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DeviceTable extends DatabaseObjectTable<Device> {

    private static final String TABLE_NAME = "nbcrv_device";

    public List<Device> getDevices(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<Device> devices = new ArrayList<>();

        StringBuilder sourceIdString = new StringBuilder();
        String prefix = "";
        for (int sourceId : sourceIds) {
            sourceIdString.append(prefix);
            sourceIdString.append(sourceId);
            prefix = ",";
        }

        String sql = String.format("SELECT * FROM %s" +
                        " WHERE ROWID IN (" +
                        "  SELECT MIN(ROWID)" +
                        "  FROM %s" +
                        "  WHERE sourceId IN (%s)" +
                        "  AND site=%d AND app=%d AND id=%d" +
                        "  GROUP BY site, app, id, name" +
                        " ) ",
                getTableName(), getTableName(), sourceIdString,
                entityId.getSite(), entityId.getApplication(), entityId.getId());

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                devices.add(new Device(name));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return devices;
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
                ",name TEXT" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(site,app,id,name))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site" +
                ",app" +
                ",id" +
                ",name" +
                ",sourceId" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<Device> object) {
        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setString(4, object.getObject().getName());
            preparedStatement.setInt(5, object.getSourceId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting to %s.", getTableName());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
