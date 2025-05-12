package gov.mil.otc._3dvis.project.javelin;

import gov.mil.otc._3dvis.data.file.delimited.SpaceSeparatedFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IvtsTspiFile extends SpaceSeparatedFile {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIMESTAMP_COLUMN = "Time(UTC)";
    private static final String LATITUDE_COLUMN = "Latitude(deg)";
    private static final String LONGITUDE_COLUMN = "Longitude(deg)";
    private static final String ALTITUDE_COLUMN = "Alt(M-HAE)";
    private static final String Q_COLUMN = "Q";
    private static final String STANDARD_DEVIATION_COLUMN = "StdDev";
    private static final String VELOCITY_EAST_COLUMN = "VE(M/sec)";
    private static final String VELOCITY_NORTH_COLUMN = "VN(M/Sec)";
    private static final String VELOCITY_UP_COLUMN = "VUp(M/Sec)";
    private final List<TspiData> tspiDataList = new ArrayList<>();
    private long epochMidnight;
    private double lastTime = 0;

    public IvtsTspiFile(File file, long epochMidnight) {
        super(file);

        addColumn(TIMESTAMP_COLUMN);
        addColumn(LATITUDE_COLUMN);
        addColumn(LONGITUDE_COLUMN);
        addColumn(ALTITUDE_COLUMN);
        addColumn(Q_COLUMN);
        addColumn(STANDARD_DEVIATION_COLUMN);
        addColumn(VELOCITY_EAST_COLUMN);
        addColumn(VELOCITY_NORTH_COLUMN);
        addColumn(VELOCITY_UP_COLUMN);

        this.epochMidnight = epochMidnight;
    }

    public List<TspiData> getTspiDataList() {
        return tspiDataList;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            double secondsFromMidnight = Double.parseDouble(fields[getColumnIndex(TIMESTAMP_COLUMN)]);
            if (secondsFromMidnight < lastTime) {
                epochMidnight += (60000 * 60 * 24);
            }
            lastTime = secondsFromMidnight;
            long timestamp = (long) (secondsFromMidnight * 1000) + epochMidnight;
            double latitude = Double.parseDouble(fields[getColumnIndex(LATITUDE_COLUMN)]);
            double longitude = Double.parseDouble(fields[getColumnIndex(LONGITUDE_COLUMN)]);
            double altitude = Double.parseDouble(fields[getColumnIndex(ALTITUDE_COLUMN)]);
            double q = Double.parseDouble(fields[getColumnIndex(Q_COLUMN)]);
            double standardDeviation = Double.parseDouble(fields[getColumnIndex(STANDARD_DEVIATION_COLUMN)]);
            double velocityEast = Double.parseDouble(fields[getColumnIndex(VELOCITY_EAST_COLUMN)]);
            double velocityNorth = Double.parseDouble(fields[getColumnIndex(VELOCITY_NORTH_COLUMN)]);
            double velocityUp = Double.parseDouble(fields[getColumnIndex(VELOCITY_UP_COLUMN)]);
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude)));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "IvtsTspiFile::processLine:", e);
        }
    }
}
