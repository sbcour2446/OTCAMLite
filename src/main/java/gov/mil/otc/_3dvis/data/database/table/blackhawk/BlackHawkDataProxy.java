package gov.mil.otc._3dvis.data.database.table.blackhawk;

import gov.mil.otc._3dvis.data.database.table.AbstractBaseTable;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.blackhawk.CcmEvent;
import gov.mil.otc._3dvis.project.blackhawk.SurveyData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlackHawkDataProxy {

    private CcmEventTable ccmEventTable = null;
    private CrewMissionMatrixTable crewMissionMatrixTable = null;
    private PostMissionAnswersTable postMissionAnswersTable = null;

    public void remove(Connection connection, List<Integer> dataSourceIds) {
        if (ccmEventTable != null) {
            removeDataSource(connection, dataSourceIds, ccmEventTable);
        }
        if (crewMissionMatrixTable != null) {
            removeDataSource(connection, dataSourceIds, crewMissionMatrixTable);
        }
        if (postMissionAnswersTable != null) {
            removeDataSource(connection, dataSourceIds, postMissionAnswersTable);
        }
    }

    private void removeDataSource(Connection connection, List<Integer> dataSourceIds, AbstractBaseTable table) {
        StringBuilder sourceIdString = new StringBuilder();
        String prefix = "";
        for (int sourceId : dataSourceIds) {
            sourceIdString.append(prefix);
            sourceIdString.append(sourceId);
            prefix = ",";
        }

        String deleteSql = String.format("DELETE FROM %s WHERE sourceId IN (%s)", table.getTableName(), sourceIdString);
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteSql)) {
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            String message = "Error deleting DataSource " + sourceIdString + " from " + table.getTableName();
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    public void addCcmEvent(Connection connection, EntityId entityId, CcmEvent ccmEvent, int sourceId) {
        if (ccmEventTable == null) {
            ccmEventTable = new CcmEventTable();
            ccmEventTable.createTable(connection);
        }

        ccmEventTable.add(connection, entityId, ccmEvent, sourceId);
    }

    public List<CcmEvent> getCcmEvents(Connection connection) {
        if (ccmEventTable == null) {
            ccmEventTable = new CcmEventTable();
            ccmEventTable.createTable(connection);
        }

        return ccmEventTable.getCcmEvents(connection);
    }

    public void addCrewMissionData(Connection connection, String mission, int flightNumber, int tailNumber, String pin,
                                   String seat, String role, long startTime, long endTime, int sourceId) {
        createTables(connection);

        crewMissionMatrixTable.add(connection, mission, flightNumber, tailNumber, pin, seat, role, startTime, endTime, sourceId);
    }

    public void addSurveyData(Connection connection, String mission, int tailNumber, String pin, String seat,
                              String role, String question, String answer, int sourceId) {
        createTables(connection);

        postMissionAnswersTable.add(connection, mission, tailNumber, pin, seat, role, question, answer, sourceId);
    }

    public List<String> getRoles(Connection connection, Mission mission, int tailNumber) {
        createTables(connection);

        return crewMissionMatrixTable.getRoles(connection, mission, tailNumber);
    }

    public List<SurveyData> getSurveyData(Connection connection, Mission mission, int tailNumber, String role) {
        createTables(connection);

        List<SurveyData> surveyData = new ArrayList<>();
        String sql = String.format(
                "SELECT t2.mission, t2.pin, t2.seat, question, answer" +
                        " FROM %s t1" +
                        " LEFT OUTER JOIN %s t2" +
                        " ON t1.mission = t2.mission" +
                        "  AND t1.tailNumber = t2.tailNumber" +
                        "  AND t1.pin = t2.pin" +
                        " WHERE t2.mission='%s' AND t2.tailNumber=%d AND t2.role='%s'",
                crewMissionMatrixTable.getTableName(), postMissionAnswersTable.getTableName(),
                mission.getName(), tailNumber, role);

        try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                String pin = resultSet.getString("pin");
                String seat = resultSet.getString("seat");
                String question = resultSet.getString("question");
                String answer = resultSet.getString("answer");
                surveyData.add(new SurveyData(mission.getName(), tailNumber, pin, role, seat, question, answer));
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return surveyData;
    }

    private void createTables(Connection connection) {
        if (crewMissionMatrixTable == null) {
            crewMissionMatrixTable = new CrewMissionMatrixTable();
            crewMissionMatrixTable.createTable(connection);
        }
        if (postMissionAnswersTable == null) {
            postMissionAnswersTable = new PostMissionAnswersTable();
            postMissionAnswersTable.createTable(connection);
        }
    }
}
