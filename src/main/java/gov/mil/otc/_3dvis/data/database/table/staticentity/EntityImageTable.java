package gov.mil.otc._3dvis.data.database.table.staticentity;

import gov.mil.otc._3dvis.data.database.table.AbstractBaseTable;
import gov.mil.otc._3dvis.entity.base.EntityId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityImageTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "entity_image_table";

    protected void add(Connection connection, EntityId entityId, BufferedImage bufferedImage) {
        byte[] bytes = createByteArray(bufferedImage);
        if (bytes.length > 0) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
                preparedStatement.setInt(1, entityId.getSite());
                preparedStatement.setInt(2, entityId.getApplication());
                preparedStatement.setInt(3, entityId.getId());
                preparedStatement.setBytes(4, bytes);
                preparedStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                Logger.getGlobal().log(Level.WARNING, String.format("Error inserting to %s", getTableName()), e);
            }
        }
    }

    protected BufferedImage get(Connection connection, EntityId entityId) {
        String sql = String.format("SELECT image FROM %s WHERE site=%d AND app=%d AND id=%d",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId());
        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                byte[] bytes = resultSet.getBytes("image");
                return createBufferedImage(bytes);
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return null;
    }

    protected void remove(Connection connection, EntityId entityId) {
        String deleteSql = String.format("DELETE FROM %s WHERE site=%d AND app=%d AND id=%d",
                getTableName(), entityId.getSite(), entityId.getApplication(), entityId.getId());
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error deleting " + entityId + " from " + getTableName();
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    private byte[] createByteArray(BufferedImage bufferedImage) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "Error creating blob", e);
        }
        return new byte[0];
    }

    private BufferedImage createBufferedImage(byte[] bytes) {
        try {
            if (bytes != null && bytes.length > 0) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                return ImageIO.read(byteArrayInputStream);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "Error converting blob to image", e);
        }
        return null;
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
                ",image BLOB" +
                ",PRIMARY KEY(site,app,id))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site" +
                ",app" +
                ",id" +
                ",image" +
                ") VALUES " +
                "(?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }
}
