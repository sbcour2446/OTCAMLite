package gov.mil.otc._3dvis.project.nbcrv.flir;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvDetection;
import gov.mil.otc._3dvis.project.nbcrv.RadNucState;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetectionsFile extends CsvFile {

    private final List<NbcrvDetection> detectionList = new ArrayList<>();
    private final List<RadNucState> radNucStateList = new ArrayList<>();

    public DetectionsFile(File file) {
        super(file);

        addColumn("Time");
        addColumn("System");
        addColumn("Device");
        addColumn("Mode", true);
        addColumn("Count", true);
        addColumn("Threat Type", true);
        addColumn("Material Name", true);
        addColumn("Material Class", true);
        addColumn("Measurement", true);
        addColumn("Units", true);
        addColumn("Criticality", true);
        addColumn("Threat Heading", true);
        addColumn("Threat Speed (m/s)", true);
        addColumn("Region Type", true);
        addColumn("Direction", true);
        addColumn("Arc Width", true);
        addColumn("Arc Radius (m)", true);
        addColumn("Lat", true);
        addColumn("Lon", true);
        addColumn("Quadrant", true);
        addColumn("Q1 Measurement", true);
        addColumn("Q1 Criticality", true);
        addColumn("Q2 Measurement", true);
        addColumn("Q2 Criticality", true);
        addColumn("Q3 Measurement", true);
        addColumn("Q3 Criticality", true);
        addColumn("Q4 Measurement", true);
        addColumn("Q4 Criticality", true);
    }

    @Override
    protected boolean doProcessFile() {
        int totalLines = countLines();
        int lineCount = 0;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
            if (!processHeader(bufferedReader)) {
                return false;
            }
            String line;
            while (!cancelRequested && (line = bufferedReader.readLine()) != null && !line.isEmpty()) {
                String[] fields = getFields(line);
                processLine(fields);
                lineCount++;
                status = (double) lineCount / totalLines;
            }
        } catch (Exception e) {
            String message = String.format("DetectionsFile::doProcessFile:Error processing file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }

        return !cancelRequested;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestring = fields[getColumnIndex("Time")];
            long timestamp = Utility.parseTime(timestring, "yyyy-MM-dd HH:mm:ss");

            String device = fields[getColumnIndex("Device")];
            String mode = fields[getColumnIndex("Mode")];
            String count = fields[getColumnIndex("Count")];
            String threadType = fields[getColumnIndex("Threat Type")];
            String materialName = fields[getColumnIndex("Material Name")];
            String materialClass = fields[getColumnIndex("Material Class")];
            double measurement = tryParseDouble(fields[getColumnIndex("Measurement")]);
            String units = fields[getColumnIndex("Units")];
            String criticality = fields[getColumnIndex("Criticality")];
            String threatHeading = fields[getColumnIndex("Threat Heading")];
            String threatSpeed = fields[getColumnIndex("Threat Speed (m/s)")];
            String regionType = fields[getColumnIndex("Region Type")];
            double direction = tryParseDouble(fields[getColumnIndex("Direction")]);
            double arcWidth = tryParseDouble(fields[getColumnIndex("Arc Width")]);
            double arcRadius = tryParseDouble(fields[getColumnIndex("Arc Radius (m)")]);

            List<Position> positions = new ArrayList<>();
            double latitude = Double.parseDouble(fields[getColumnIndex("Lat")]);
            double longitude = Double.parseDouble(fields[getColumnIndex("Lon")]);
            positions.add(Position.fromDegrees(latitude, longitude));
            for (int i = getColumnIndex("Lon") + 1; i < fields.length; i++) {
                latitude = Double.parseDouble(fields[i++]);
                if (i < fields.length) {
                    longitude = Double.parseDouble(fields[i]);
                    positions.add(Position.fromDegrees(latitude, longitude));
                }
            }

            NbcrvDetection nbcrvDetection = NbcrvDetection.create(timestamp, device, threadType, materialName,
                    materialClass, measurement, units, criticality, threatHeading, threatSpeed, regionType, direction,
                    arcWidth, arcRadius, positions);
            if (nbcrvDetection != null) {
                detectionList.add(nbcrvDetection);
            }

            if (getColumnIndex("Quadrant") >= 0 && !fields[getColumnIndex("Quadrant")].isBlank() &&
                    getColumnIndex("Q1 Measurement") >= 0 &&
                    getColumnIndex("Q1 Criticality") >= 0 &&
                    getColumnIndex("Q2 Measurement") >= 0 &&
                    getColumnIndex("Q2 Criticality") >= 0 &&
                    getColumnIndex("Q3 Measurement") >= 0 &&
                    getColumnIndex("Q3 Criticality") >= 0 &&
                    getColumnIndex("Q4 Measurement") >= 0 &&
                    getColumnIndex("Q4 Criticality") >= 0) {
                int quadrant = Integer.parseInt(fields[getColumnIndex("Quadrant")]);
                double q1Measurement = tryParseDouble(fields[getColumnIndex("Q1 Measurement")]);
                String q1Criticality = fields[getColumnIndex("Q1 Criticality")];
                double q2Measurement = tryParseDouble(fields[getColumnIndex("Q2 Measurement")]);
                String q2Criticality = fields[getColumnIndex("Q2 Criticality")];
                double q3Measurement = tryParseDouble(fields[getColumnIndex("Q3 Measurement")]);
                String q3Criticality = fields[getColumnIndex("Q3 Criticality")];
                double q4Measurement = tryParseDouble(fields[getColumnIndex("Q4 Measurement")]);
                String q4Criticality = fields[getColumnIndex("Q4 Criticality")];

                radNucStateList.add(new RadNucState(timestamp, quadrant, q1Measurement, q1Criticality,
                        q2Measurement, q2Criticality, q3Measurement, q3Criticality, q4Measurement, q4Criticality));
            }
        } catch (Exception e) {
            String message = String.format("DetectionsFile::processLine:Error processing file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    public List<NbcrvDetection> getDetectionList() {
        return detectionList;
    }

    public List<RadNucState> getRadNucStateList() {
        return radNucStateList;
    }
}
