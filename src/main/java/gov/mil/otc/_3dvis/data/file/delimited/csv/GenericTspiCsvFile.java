package gov.mil.otc._3dvis.data.file.delimited.csv;

import gov.mil.otc._3dvis.data.tpsi.TspiCsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;

import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenericTspiCsvFile extends TspiCsvFile {

    private final CsvConfiguration csvConfiguration;

    public GenericTspiCsvFile(File file, CsvConfiguration csvConfiguration) {
        super(file, csvConfiguration.getHeaderLineNumber());
        this.csvConfiguration = csvConfiguration;
    }

    @Override
    public boolean processFile() {
        headerStartField = csvConfiguration.getHeaderStartField();

        if (!csvConfiguration.getFieldConfigurationMap().containsKey(CsvConfiguration.Field.TIMESTAMP) ||
                !csvConfiguration.getFieldConfigurationMap().containsKey(CsvConfiguration.Field.LATITUDE) ||
                !csvConfiguration.getFieldConfigurationMap().containsKey(CsvConfiguration.Field.LONGITUDE) ||
                !csvConfiguration.getFieldConfigurationMap().containsKey(CsvConfiguration.Field.ALTITUDE)) {
            return false;
        }

        for (Map.Entry<CsvConfiguration.Field, CsvConfiguration.FieldConfiguration> entry : csvConfiguration.getFieldConfigurationMap().entrySet()) {
            addColumn(entry.getValue().headerName);
        }

        return super.doProcessFile();
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            long timestamp = getTimestamp(fields);
            if (timestamp < 0) {
                return;
            }

            double latitude = Double.parseDouble(fields[getColumnIndex(
                    csvConfiguration.getFieldConfigurationMap().get(CsvConfiguration.Field.LATITUDE).headerName)]);
            double longitude = Double.parseDouble(fields[getColumnIndex(
                    csvConfiguration.getFieldConfigurationMap().get(CsvConfiguration.Field.LONGITUDE).headerName)]);

            double altitude = Double.parseDouble(fields[getColumnIndex(
                    csvConfiguration.getFieldConfigurationMap().get(CsvConfiguration.Field.ALTITUDE).headerName)]);
            if (csvConfiguration.getFieldConfigurationMap().get(CsvConfiguration.Field.ALTITUDE).valueType == CsvConfiguration.ValueType.MSL_FEET) {
                altitude *= .3048;
            }

            double heading = Double.parseDouble(fields[getColumnIndex(
                    csvConfiguration.getFieldConfigurationMap().get(CsvConfiguration.Field.HEADING).headerName)]);
            double pitch = Double.parseDouble(fields[getColumnIndex(
                    csvConfiguration.getFieldConfigurationMap().get(CsvConfiguration.Field.PITCH).headerName)]);
            double roll = Double.parseDouble(fields[getColumnIndex(
                    csvConfiguration.getFieldConfigurationMap().get(CsvConfiguration.Field.ROLL).headerName)]);

            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude, altitude),
                    null, null, heading, pitch, roll));
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    protected long getTimestamp(String[] fields) {
        try {
            String timestampFormat = csvConfiguration.getTimestampFormat();
            String timestampString = fields[getColumnIndex(
                    csvConfiguration.getFieldConfigurationMap().get(CsvConfiguration.Field.TIMESTAMP).headerName)];

            if (csvConfiguration.isNeedsYear()) {
                timestampFormat = "yyyy " + timestampFormat;
                timestampString = csvConfiguration.getYear() + " " + timestampString;
            }

            return Utility.parseTime(timestampString, timestampFormat);
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
        return -1;
    }
}
