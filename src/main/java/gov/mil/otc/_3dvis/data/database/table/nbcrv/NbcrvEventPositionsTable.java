package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.table.AbstractBaseTable;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import gov.nasa.worldwind.geom.Position;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvEventPositionsTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "nbcrv_event_positions";

    public List<Position> getPositions(Connection connection, int eventId) {
        List<Position> positions = new ArrayList<>();

        String sql = String.format("SELECT * FROM %s WHERE eventId=%d", getTableName(), eventId);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                double altitude = resultSet.getDouble("altitude");
                positions.add(Position.fromDegrees(latitude, longitude, altitude));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return positions;
    }

    public void insert(Connection connection, NbcrvDetection nbcrvDetection, int eventId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            for (Position position : nbcrvDetection.getPositionList()) {
                preparedStatement.setInt(1, eventId);
                preparedStatement.setDouble(2, position.getLatitude().degrees);
                preparedStatement.setDouble(3, position.getLongitude().degrees);
                preparedStatement.setDouble(4, position.getElevation());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
        }
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected String getCreateTableSql(String tableName) {
        return "CREATE TABLE IF NOT EXISTS " + tableName +
                "(eventId INTEGER" +
                ",latitude REAL" +
                ",longitude REAL" +
                ",altitude REAL" +
                ")";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(eventId" +
                ",latitude" +
                ",longitude" +
                ",altitude" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }
}
