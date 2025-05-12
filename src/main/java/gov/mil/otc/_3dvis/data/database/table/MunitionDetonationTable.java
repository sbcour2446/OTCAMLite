package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.event.MunitionDetonationEvent;
import gov.nasa.worldwind.geom.Position;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for the MunitionDetonation event table.
 */
public class MunitionDetonationTable extends DatabaseObjectTable<MunitionDetonationEvent> {

    private static final String TABLE_NAME = "munition_detonation";

    public List<MunitionDetonationEvent> getEvents(Connection connection, List<Integer> sourceIds) {
        List<MunitionDetonationEvent> events = new ArrayList<>();
        String sql = getEventQuerySql(null, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");

                int eventSite = resultSet.getInt("eventSite");
                int eventApp = resultSet.getInt("eventApp");
                int eventId = resultSet.getInt("eventId");
                EntityId eventEntityId = new EntityId(eventSite, eventApp, eventId);

                double impactLatitude = resultSet.getDouble("impactLatitude");
                double impactLongitude = resultSet.getDouble("impactLongitude");
                double impactAltitude = resultSet.getDouble("impactAltitude");
                Position impactPosition = Position.fromDegrees(impactLatitude, impactLongitude, impactAltitude);

                EntityType munitionType = EntityType.fromString(resultSet.getString("munitionType"));
                int radius = resultSet.getInt("radius");

                events.add(new MunitionDetonationEvent(timestamp, eventEntityId, impactPosition, munitionType, radius));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return events;
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<MunitionDetonationEvent> object) {
        try {
            preparedStatement.setLong(1, object.getObject().getTimestamp());
            preparedStatement.setInt(2, object.getObject().getEventId().getSite());
            preparedStatement.setInt(3, object.getObject().getEventId().getApplication());
            preparedStatement.setInt(4, object.getObject().getEventId().getId());
            preparedStatement.setDouble(5, object.getObject().getImpactPosition().getLatitude().degrees);
            preparedStatement.setDouble(6, object.getObject().getImpactPosition().getLongitude().degrees);
            preparedStatement.setDouble(7, object.getObject().getImpactPosition().getAltitude());
            preparedStatement.setString(8, object.getObject().getMunition().toString());
            preparedStatement.setInt(9, object.getObject().getRadius());
            preparedStatement.setInt(10, object.getSourceId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting MunitionDetonation event %s at %d from %d",
                    object.getObject().getEventId().toString(), object.getObject().getTimestamp(), object.getSourceId());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + getTableName() +
                "(timestamp INTEGER" +
                ",eventSite INTEGER" +
                ",eventApp INTEGER" +
                ",eventId INTEGER" +
                ",impactLatitude REAL" +
                ",impactLongitude REAL" +
                ",impactAltitude REAL" +
                ",munitionType TEXT" +
                ",radius INTEGER" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(sourceId,eventSite,eventApp,eventId,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(timestamp" +
                ",eventSite" +
                ",eventApp" +
                ",eventId" +
                ",impactLatitude" +
                ",impactLongitude" +
                ",impactAltitude" +
                ",munitionType" +
                ",radius" +
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
                ",?" +
                ")";
    }
}
