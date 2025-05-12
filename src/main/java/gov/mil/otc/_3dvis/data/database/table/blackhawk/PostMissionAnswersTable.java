package gov.mil.otc._3dvis.data.database.table.blackhawk;

import gov.mil.otc._3dvis.data.database.table.AbstractBaseTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PostMissionAnswersTable extends AbstractBaseTable {

    private static final String TABLE_NAME = "blackhawk_post_mission_answers_table";

    protected void add(Connection connection, String mission, int tailNumber, String pin, String seat,
                       String role, String question, String answer, int sourceId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(getInsertSql(getTableName()))) {
            preparedStatement.setString(1, mission);
            preparedStatement.setInt(2, tailNumber);
            preparedStatement.setString(3, pin);
            preparedStatement.setString(4, seat);
            preparedStatement.setString(5, role);
            preparedStatement.setString(6, question);
            preparedStatement.setString(7, answer);
            preparedStatement.setInt(8, sourceId);
            preparedStatement.executeUpdate();
            connection.commit();
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
                "(mission TEXT" +
                ",tailNumber INTEGER" +
                ",pin TEXT" +
                ",seat TEXT" +
                ",role TEXT" +
                ",question INTEGER" +
                ",answer INTEGER" +
                ",sourceId INTEGER" +
                ",PRIMARY KEY(mission,pin,question))";
    }

    @Override
    protected String getInsertSql(String tableName) {
        return "INSERT OR IGNORE INTO " + tableName +
                "(mission" +
                ",tailNumber" +
                ",pin" +
                ",seat" +
                ",role" +
                ",question" +
                ",answer" +
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
                ")";
    }
}
