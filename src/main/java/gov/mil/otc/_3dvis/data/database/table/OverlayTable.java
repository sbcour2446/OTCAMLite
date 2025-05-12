package gov.mil.otc._3dvis.data.database.table;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OverlayTable extends AbstractDataTable<File> {

    private static final String TABLE_NAME = "overlays";

    public List<File> getOverlays(Connection connection) {
        List<File> overlays = new ArrayList<>();

        String sql = String.format("SELECT filename FROM %s", getTableName());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                String filename = resultSet.getString("filename");
                overlays.add(new File(filename));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return overlays;
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, File object) {
        try {
            preparedStatement.setString(1, object.getAbsolutePath());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting %s to %s",
                    object.getAbsolutePath(), getTableName());
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
                "(filename TEXT,PRIMARY KEY(filename))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR REPLACE INTO " + tableName +
                "(filename) VALUES (?)";
    }
}
