package gov.mil.otc._3dvis.data.report;

import gov.mil.otc._3dvis.data.database.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReportManager {

    private final List<Report> reportList = Collections.synchronizedList(new ArrayList<>());
    private final Database database;

    public ReportManager(Database database) {
        this.database = database;
        addAvailableReport(new PositionReport());
        addAvailableReport(new EngagementReport());
    }

    public void addAvailableReport(Report report) {
        if (!reportList.contains(report)) {
            reportList.add(report);
        }
    }

    public List<Report> getAvailableReports() {
        return new ArrayList<>(reportList);
    }

    public void generateReport(Report report, String outputDirectory, long startTime, long stopTime) {
        report.setDatabase(database);
        report.setOutputDirectory(outputDirectory);
        report.setStartTime(startTime);
        report.setStopTime(stopTime);
        report.createReport();
    }
}
