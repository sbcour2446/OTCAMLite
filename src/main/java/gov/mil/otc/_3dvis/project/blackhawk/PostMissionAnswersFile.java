package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

//tblPostMissionAnswers.csv
public class PostMissionAnswersFile extends CsvFile {

    private String missionNumHeader = "missionNum";
    private String tailNumHeader = "AircrafttailNum";
    private String tpPinHeader = "tpPin";
    private String tpSeatHeader = "tpSeat";
    private String tpRoleHeader = "tpRole";
    private String questionHeader = "Question";
    private String answerHeader = "Answer";
    private final DataSource dataSource;

    public PostMissionAnswersFile(File file, DataSource dataSource) {
        super(file);

        this.dataSource = dataSource;

        addColumn(missionNumHeader);
        addColumn(tailNumHeader);
        addColumn(tpPinHeader);
        addColumn(tpSeatHeader);
        addColumn(tpRoleHeader);
        addColumn(questionHeader);
        addColumn(answerHeader);
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String mission = fields[getColumnIndex(missionNumHeader)].trim();
            int tailNumber = Integer.parseInt(fields[getColumnIndex(tailNumHeader)].trim());
            String pin = fields[getColumnIndex(tpPinHeader)].trim();
            String seat = fields[getColumnIndex(tpSeatHeader)].trim();
            String role = fields[getColumnIndex(tpRoleHeader)].trim();
            String question = fields[getColumnIndex(questionHeader)].trim();
            String answer = fields[getColumnIndex(answerHeader)].trim();
            DatabaseLogger.addSurveyData(mission, tailNumber, pin, seat, role, question, answer, dataSource.getId());
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.INFO, message, e);
        }
    }
}
