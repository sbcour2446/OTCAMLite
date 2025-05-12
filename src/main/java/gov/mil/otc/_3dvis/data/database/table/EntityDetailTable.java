package gov.mil.otc._3dvis.data.database.table;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.datamodel.RtcaState;
import gov.mil.otc._3dvis.entity.base.EntityId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityDetailTable extends DatabaseObjectTable<EntityDetail> {

    private static final String TABLE_NAME = "entity_detail";

    public List<EntityDetail> getEntityDetails(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        List<EntityDetail> entityDetails = new ArrayList<>();
        String sql = getQuerySql(entityId, sourceIds);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                long timestamp = resultSet.getLong("timestamp");
                EntityType entityType = EntityType.fromString(resultSet.getString("entityType"));
                Affiliation affiliation = Affiliation.getEnum(resultSet.getInt("affiliation"));
                String name = resultSet.getString("name");
                String militarySymbol = resultSet.getString("militarySymbol");
                boolean isKillCatastrophic = resultSet.getBoolean("isKillCatastrophic");
                boolean isKillFirepower = resultSet.getBoolean("isKillFirepower");
                boolean isKillMobility = resultSet.getBoolean("isKillMobility");
                boolean isKillCommunication = resultSet.getBoolean("isKillCommunication");
                boolean isSuppressed = resultSet.getBoolean("isSuppressed");
                boolean isJammed = resultSet.getBoolean("isJammed");
                boolean isHit = resultSet.getBoolean("isHit");
                boolean isMissed = resultSet.getBoolean("isMissed");
                String rtcaOther = resultSet.getString("rtcaOther");
                int damagePercent = resultSet.getInt("damagePercent");
                RtcaState rtcaState = new RtcaState(isKillCatastrophic, isKillFirepower, isKillMobility,
                        isKillCommunication, isSuppressed, isJammed, isHit, isMissed, rtcaOther, damagePercent);
                String source = resultSet.getString("source");
                int urn = resultSet.getInt("urn");
                int milesPid = resultSet.getInt("milesPid");
                boolean outOfComms = resultSet.getBoolean("outOfComms");
                entityDetails.add(new EntityDetail.Builder()
                        .setTimestamp(timestamp)
                        .setEntityType(entityType)
                        .setAffiliation(affiliation)
                        .setName(name)
                        .setMilitarySymbol(militarySymbol)
                        .setUrn(urn)
                        .setRtcaState(rtcaState)
                        .setSource(source)
                        .setMilesPid(milesPid)
                        .setOutOfComms(outOfComms)
                        .build());
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return entityDetails;
    }

    @Override
    protected void insertRow(PreparedStatement preparedStatement, DatabaseObject<EntityDetail> object) {
        try {
            preparedStatement.setInt(1, object.getEntityId().getSite());
            preparedStatement.setInt(2, object.getEntityId().getApplication());
            preparedStatement.setInt(3, object.getEntityId().getId());
            preparedStatement.setLong(4, object.getObject().getTimestamp());
            preparedStatement.setString(5, object.getObject().getEntityType().toString());
            preparedStatement.setLong(6, object.getObject().getAffiliation().ordinal());
            preparedStatement.setString(7, object.getObject().getName());
            preparedStatement.setString(8, object.getObject().getMilitarySymbol());
            preparedStatement.setLong(9, object.getObject().getUrn());
            preparedStatement.setBoolean(10, object.getObject().getRtcaState().isKillCatastrophic());
            preparedStatement.setBoolean(11, object.getObject().getRtcaState().isKillFirepower());
            preparedStatement.setBoolean(12, object.getObject().getRtcaState().isKillMobility());
            preparedStatement.setBoolean(13, object.getObject().getRtcaState().isKillCommunication());
            preparedStatement.setBoolean(14, object.getObject().getRtcaState().isSuppression());
            preparedStatement.setBoolean(15, object.getObject().getRtcaState().isJammed());
            preparedStatement.setBoolean(16, object.getObject().getRtcaState().isHitNoKill());
            preparedStatement.setBoolean(17, object.getObject().getRtcaState().isMiss());
            preparedStatement.setString(18, object.getObject().getRtcaState().getRtcaOther());
            preparedStatement.setInt(19, object.getObject().getRtcaState().getDamagePercent());
            preparedStatement.setString(20, object.getObject().getSource());
            preparedStatement.setLong(21, object.getObject().getMilesPid());
            preparedStatement.setBoolean(22, object.getObject().isOutOfComms());
            preparedStatement.setInt(23, object.getSourceId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            String message = String.format("Error inserting %d:%d:%d to %s.",
                    object.getEntityId().getSite(), object.getEntityId().getApplication(),
                    object.getEntityId().getId(), getTableName());
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
                "(site INTEGER" +
                ",app INTEGER" +
                ",id INTEGER" +
                ",timestamp INTEGER" +
                ",entityType TEXT" +
                ",affiliation INTEGER" +
                ",name TEXT" +
                ",militarySymbol TEXT" +
                ",urn INTEGER" +
                ",isKillCatastrophic INTEGER" +
                ",isKillFirepower INTEGER" +
                ",isKillMobility INTEGER" +
                ",isKillCommunication INTEGER" +
                ",isSuppressed INTEGER" +
                ",isJammed INTEGER" +
                ",isHit INTEGER" +
                ",isMissed INTEGER" +
                ",rtcaOther TEXT" +
                ",damagePercent INTEGER" +
                ",source TEXT" +
                ",milesPid INTEGER" +
                ",outOfComms INTEGER" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(sourceId,site,app,id,timestamp))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(site" +
                ",app" +
                ",id" +
                ",timestamp" +
                ",entityType" +
                ",affiliation" +
                ",name" +
                ",militarySymbol" +
                ",urn" +
                ",isKillCatastrophic" +
                ",isKillFirepower" +
                ",isKillMobility" +
                ",isKillCommunication" +
                ",isSuppressed" +
                ",isJammed" +
                ",isHit" +
                ",isMissed" +
                ",rtcaOther" +
                ",damagePercent" +
                ",source" +
                ",milesPid" +
                ",outOfComms" +
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
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ",?" +
                ")";
    }
}
