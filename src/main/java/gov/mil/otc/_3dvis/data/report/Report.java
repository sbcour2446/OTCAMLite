package gov.mil.otc._3dvis.data.report;

import gov.mil.otc._3dvis.data.database.Database;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Report {

    protected static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private String outputDirectory;
    private Database database;
    private long startTime;
    private long stopTime;
    private BufferedWriter writer;

    protected Report() {
    }

    protected void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    protected void setDatabase(Database database) {
        this.database = database;
    }

    protected void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    protected void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    protected Database getDatabase() {
        return database;
    }

    public void createReport() {
        String filename = outputDirectory + File.separator + getFilename();
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            writeHeader();
            writeData();
            writer.close();
        } catch (Exception e) {
            String message = "Error writing report " + getReportName();
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    protected void writeHeader() throws IOException {
        String fieldSeparator = "";
        for (String column : getColumns()) {
            writer.write(fieldSeparator);
            fieldSeparator = ",";
            writer.write(column);
        }
        writer.newLine();
    }

    protected void writeTimestamp(long timestamp) throws IOException {
        String formatedTimestamp = Utility.formatTime(timestamp);
        writer.write("=\"" + formatedTimestamp + "\",");
    }

    protected void writeValue(EntityId entityId) throws IOException {
        String value = entityId != null ? entityId.toString() : "";
        writer.write("=\"" + value + "\",");
    }

    protected void writeValue(String value) throws IOException {
        writer.write("\"" + value + "\",");
    }

    protected void newLine() throws IOException {
        writer.newLine();
    }

    protected boolean inDateRange(long timestamp) {
        if (startTime > 0 && stopTime > 0) {
            return timestamp >= startTime && timestamp <= stopTime;
        }
        return true;
    }

    public abstract String getReportName();

    protected abstract String getFilename();

    protected abstract String[] getColumns();

    protected abstract void writeData() throws IOException;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Report report = (Report) o;
        return getReportName().equals(report.getReportName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReportName());
    }
}
