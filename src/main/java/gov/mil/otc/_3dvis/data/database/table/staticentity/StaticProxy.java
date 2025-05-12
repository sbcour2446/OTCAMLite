package gov.mil.otc._3dvis.data.database.table.staticentity;

import gov.mil.otc._3dvis.entity.base.EntityId;

import java.awt.image.BufferedImage;
import java.sql.Connection;

public class StaticProxy {

    private EntityImageTable entityImageTable = null;

    public void addEntityImage(Connection connection, EntityId entityId, BufferedImage bufferedImage) {
        if (entityImageTable == null) {
            entityImageTable = new EntityImageTable();
            entityImageTable.createTable(connection);
        }
        entityImageTable.add(connection, entityId, bufferedImage);
    }

    public BufferedImage getEntityImage(Connection connection, EntityId entityId) {
        if (entityImageTable == null) {
            entityImageTable = new EntityImageTable();
            entityImageTable.createTable(connection);
        }
        return entityImageTable.get(connection, entityId);
    }

    public void removeEntityImage(Connection connection, EntityId entityId) {
        if (entityImageTable == null) {
            entityImageTable = new EntityImageTable();
            entityImageTable.createTable(connection);
        }
        entityImageTable.remove(connection, entityId);
    }
}
