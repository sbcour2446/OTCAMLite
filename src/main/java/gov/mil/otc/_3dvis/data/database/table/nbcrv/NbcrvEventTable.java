package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.table.AbstractBaseTable;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import gov.mil.otc._3dvis.project.nbcrv.RegionType;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvEventTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "nbcrv_event";

    public List<NbcrvDetection> getNbcrvEvents(Connection connection, EntityId entityId, List<Integer> sourceIds, NbcrvEventPositionsTable nbcrvEventPositionsTable) {
        List<NbcrvDetection> events = new ArrayList<>();

        StringBuilder sourceIdString = new StringBuilder();
        String prefix = "";
        for (int sourceId : sourceIds) {
            sourceIdString.append(prefix);
            sourceIdString.append(sourceId);
            prefix = ",";
        }

        String sql = String.format("SELECT ROWID,* FROM %s" +
                        " WHERE ROWID IN (" +
                        "  SELECT MIN(ROWID)" +
                        "  FROM %s" +
                        "  WHERE sourceId IN (%s)" +
                        "  AND site=%d AND app=%d AND id=%d" +
                        "  GROUP BY site,app,id,timestamp,deviceName,eventType" +
                        " ) " +
                        " ORDER BY timestamp",
                getTableName(), getTableName(), sourceIdString,
                entityId.getSite(), entityId.getApplication(), entityId.getId());

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                int eventId = resultSet.getInt("ROWID");
                long timestamp = resultSet.getLong("timestamp");
                String deviceName = resultSet.getString("deviceName");
                String regionTypeString = resultSet.getString("regionType");
                RegionType regionType = RegionType.valueOf(regionTypeString);
                String description = resultSet.getString("description");
                double arcAngle = resultSet.getDouble("arcAngle");
                double radius = resultSet.getDouble("radius");
                double direction = resultSet.getDouble("direction");
                double measurement = resultSet.getDouble("measurement");
                List<Position> positions = nbcrvEventPositionsTable.getPositions(connection, eventId);
                NbcrvDetection nbcrvDetection = NbcrvDetection.create(timestamp, deviceName, regionType, description,
                        positions, arcAngle, radius, direction, measurement);
                if (nbcrvDetection != null) {
                    events.add(nbcrvDetection);
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return events;
    }

    public int insert(Connection connection, NbcrvDetection nbcrvDetection, EntityId entityId, int sourceId) {
        int result = -1;
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setInt(1, sourceId);
            preparedStatement.setInt(2, entityId.getSite());
            preparedStatement.setInt(3, entityId.getApplication());
            preparedStatement.setInt(4, entityId.getId());
            preparedStatement.setLong(5, nbcrvDetection.getTimestamp());
            preparedStatement.setString(6, nbcrvDetection.getDeviceName());
            preparedStatement.setString(7, "Detection");
            preparedStatement.setString(8, nbcrvDetection.getRegionType().toString());
            preparedStatement.setString(9, nbcrvDetection.getDescription());
            preparedStatement.setDouble(10, nbcrvDetection.getArcAngle());
            preparedStatement.setDouble(11, nbcrvDetection.getRadius());
            preparedStatement.setDouble(12, nbcrvDetection.getDirection());
            preparedStatement.setDouble(13, nbcrvDetection.getMeasurement());
            result = preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }

        if (result != 1) {
            System.out.println("NbcrvEventTable::insert:no row added: " + Utility.formatTime(nbcrvDetection.getTimestamp()) +
                    " : " + sourceId + " : " + nbcrvDetection.getDeviceName());
            return -1;
        }

        try (ResultSet resultSet = connection.prepareStatement("SELECT last_insert_rowid()").executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }

        return -1;
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
                ",timestamp INTEGER" +
                ",deviceName TEXT" +
                ",eventType TEXT" +
                ",regionType TEXT" +
                ",description TEXT" +
                ",arcAngle REAL" +
                ",radius REAL" +
                ",direction REAL" +
                ",measurement REAL" +
                ",PRIMARY KEY(sourceId,site,app,id,timestamp,deviceName,eventType,regionType))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(sourceId" +
                ",site" +
                ",app" +
                ",id" +
                ",timestamp" +
                ",deviceName" +
                ",eventType" +
                ",regionType" +
                ",description" +
                ",arcAngle" +
                ",radius" +
                ",direction" +
                ",measurement" +
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
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }
}
