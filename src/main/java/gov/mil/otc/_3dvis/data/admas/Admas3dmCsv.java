package gov.mil.otc._3dvis.data.admas;

import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Admas3dmCsv extends TspiCsvFile {
    //UTC Time,Time Offset (s),"Vertical Acceleration (3DM-GX3-25) (g)","Longitudinal Acceleration (3DM-GX3-25) (g)","Lateral Acceleration (3DM-GX3-25) (g)","Yaw Rate (3DM-GX3-25) (deg/s)","Roll Rate (3DM-GX3-25) (deg/s)","Pitch Rate (3DM-GX3-25) (deg/s)","Yaw Angle (3DM-GX3-25) (deg)","Pitch Angle (3DM-GX3-25) (deg)","Roll Angle (3DM-GX3-25) (deg)"
    //05/11/2023 20:15:30.854658000 UTC

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss.SSSSSSSSS";
    private static final String TIMESTAMP_COLUMN = "UTC Time";
    private static final String YAW_COLUMN = "\"Yaw Angle (3DM-GX3-25) (deg)\"";
    private static final String PITCH_COLUMN = "\"Pitch Angle (3DM-GX3-25) (deg)\"";
    private static final String ROLL_COLUMN = "\"Roll Angle (3DM-GX3-25) (deg)\"";
    private final List<TspiData> gpsFileTspi;
    private boolean filter1Hz = false;
    private long currentSecond = 0;
    private int currentTspiIndex = 0;

    public Admas3dmCsv(File file, List<TspiData> gpsFileTspi) {
        super(file);

        addColumn(TIMESTAMP_COLUMN);
        addColumn(YAW_COLUMN);
        addColumn(PITCH_COLUMN);
        addColumn(ROLL_COLUMN);
        this.gpsFileTspi = gpsFileTspi;
    }

    public void setFilter1Hz(boolean filter1Hz) {
        this.filter1Hz = filter1Hz;
    }

    @Override
    public List<TspiData> getTspiDataList() {
        while (currentTspiIndex < gpsFileTspi.size()) {
            tspiDataList.add(gpsFileTspi.get(currentTspiIndex++));
        }
        return tspiDataList;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestampString = fields[getColumnIndex(TIMESTAMP_COLUMN)];
            timestampString = timestampString.substring(0, timestampString.indexOf(" UTC"));
            long timestamp = Utility.parseTime(timestampString, DateTimeFormatter.ofPattern(DATE_FORMAT));

            if (filter1Hz) {
                if (currentSecond == timestamp / 1000) {
                    return;
                } else {
                    currentSecond = timestamp / 1000;
                }
            }

            double yaw = Double.parseDouble(fields[getColumnIndex(YAW_COLUMN)]);
            double pitch = Double.parseDouble(fields[getColumnIndex(PITCH_COLUMN)]);
            double roll = Double.parseDouble(fields[getColumnIndex(ROLL_COLUMN)]);

            timestamp = currentSecond * 1000;
            updateTspiData(timestamp, yaw, pitch, roll);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }


    private void updateTspiData(long timestamp, double yaw, double pitch, double roll) {
        while (currentTspiIndex < gpsFileTspi.size()) {
            TspiData tspiData = gpsFileTspi.get(currentTspiIndex);

            if (tspiData.getTimestamp() > timestamp) {
                return;
            }

            currentTspiIndex++;

            if (tspiData.getTimestamp() == timestamp) {
                tspiDataList.add(new TspiData(timestamp, tspiData.getPosition(), tspiData.getForwardVelocity(),
                        tspiData.getVerticalVelocity(), yaw, pitch, roll, tspiData.isRecovered()));
                return;
            }

            tspiDataList.add(tspiData);
        }
    }
}
