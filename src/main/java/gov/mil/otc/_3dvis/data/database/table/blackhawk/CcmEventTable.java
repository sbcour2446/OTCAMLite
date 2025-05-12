package gov.mil.otc._3dvis.data.database.table.blackhawk;

import gov.mil.otc._3dvis.data.database.table.AbstractBaseTable;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.blackhawk.AcquisitionType;
import gov.mil.otc._3dvis.project.blackhawk.CcmEvent;
import gov.mil.otc._3dvis.project.blackhawk.EmitterType;
import gov.nasa.worldwind.geom.Position;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CcmEventTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "blackhawk_ccm_event_table";

    protected void add(Connection connection, EntityId entityId, CcmEvent ccmEvent, int sourceId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setInt(1, entityId.getSite());
            preparedStatement.setInt(2, entityId.getApplication());
            preparedStatement.setInt(3, entityId.getId());
            preparedStatement.setLong(4, ccmEvent.getTimestamp());
            preparedStatement.setLong(5, ccmEvent.getEndTime());
            preparedStatement.setDouble(6, ccmEvent.getStartPosition().getLatitude().degrees);
            preparedStatement.setDouble(7, ccmEvent.getStartPosition().getLongitude().degrees);
            preparedStatement.setDouble(8, ccmEvent.getStartPosition().getElevation());
            preparedStatement.setDouble(9, ccmEvent.getEndPosition().getLatitude().degrees);
            preparedStatement.setDouble(10, ccmEvent.getEndPosition().getLongitude().degrees);
            preparedStatement.setDouble(11, ccmEvent.getEndPosition().getElevation());
            preparedStatement.setDouble(12, ccmEvent.getAzimuth());
            preparedStatement.setDouble(13, ccmEvent.getElevation());
            preparedStatement.setDouble(14, ccmEvent.getRange());
            preparedStatement.setString(15, ccmEvent.getEmitterType().name());
            preparedStatement.setString(16, ccmEvent.getAcquisitionType().name());
            preparedStatement.setInt(17, sourceId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    protected List<CcmEvent> getCcmEvents(Connection connection) {
        List<CcmEvent> ccmEvents = new ArrayList<>();
        String sql = "SELECT * FROM " + getTableName();
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                int site = resultSet.getInt("site");
                int app = resultSet.getInt("app");
                int id = resultSet.getInt("id");
                long timeOn = resultSet.getLong("timeOn");
                long timeOff = resultSet.getLong("timeOff");
                double startLatitude = resultSet.getDouble("startLatitude");
                double startLongitude = resultSet.getDouble("startLongitude");
                double startAltitude = resultSet.getDouble("startAltitude");
                double stopLatitude = resultSet.getDouble("stopLatitude");
                double stopLongitude = resultSet.getDouble("stopLongitude");
                double stopAltitude = resultSet.getDouble("stopAltitude");
                double azimuth = resultSet.getDouble("azimuth");
                double elevation = resultSet.getDouble("elevation");
                double range = resultSet.getDouble("range");
                String emitterType = resultSet.getString("emitterType");
                String acquisitionType = resultSet.getString("acquisitionType");
                Position startPosition = Position.fromDegrees(startLatitude, startLongitude, startAltitude);
                Position stopPosition = Position.fromDegrees(stopLatitude, stopLongitude, stopAltitude);
                CcmEvent ccmEvent = new CcmEvent(timeOn, timeOff, startPosition,
                        stopPosition, azimuth, elevation, range, EmitterType.fromString(emitterType),
                        AcquisitionType.fromString(acquisitionType), new EntityId(site, app, id));
                ccmEvents.add(ccmEvent);
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return ccmEvents;
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
                ",timeOn INTEGER" +
                ",timeOff INTEGER" +
                ",startLatitude REAL" +
                ",startLongitude REAL" +
                ",startAltitude REAL" +
                ",stopLatitude REAL" +
                ",stopLongitude REAL" +
                ",stopAltitude REAL" +
                ",azimuth REAL" +
                ",elevation REAL" +
                ",range REAL" +
                ",emitterType TEXT" +
                ",acquisitionType TEXT" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(site,app,id,timeOn))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site" +
                ",app" +
                ",id" +
                ",timeOn" +
                ",timeOff" +
                ",startLatitude" +
                ",startLongitude" +
                ",startAltitude" +
                ",stopLatitude" +
                ",stopLongitude" +
                ",stopAltitude" +
                ",azimuth" +
                ",elevation" +
                ",range" +
                ",emitterType" +
                ",acquisitionType" +
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
