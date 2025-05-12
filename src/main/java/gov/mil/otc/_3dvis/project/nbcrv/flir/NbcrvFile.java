package gov.mil.otc._3dvis.project.nbcrv.flir;

import gov.mil.otc._3dvis.data.file.delimited.csv.CsvFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.project.nbcrv.Device;
import gov.mil.otc._3dvis.project.nbcrv.DeviceState;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvState;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.geom.Position;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvFile extends CsvFile {

    private final List<NbcrvState> nbcrvStateList = new ArrayList<>();
    private final List<TspiData> tspiDataList = new ArrayList<>();
    private final List<DeviceColumn> deviceColumnList = new ArrayList<>();

    private final String[] sensorColumns = {
            "Alert",
            "State",
            "Major",
            "Minor",
            "Info",
            "Activity",
            "Operator",
    };

    public NbcrvFile(File file) {
        super(file, 2);
    }

    public List<NbcrvState> getNbcrvStates() {
        return nbcrvStateList;
    }

    public List<TspiData> getTspiDataList() {
        return tspiDataList;
    }

    public List<Device> getDeviceList() {
        List<Device> deviceList = new ArrayList<>();
        for (DeviceColumn deviceColumn : deviceColumnList) {
            deviceList.add(deviceColumn.device);
        }
        return deviceList;
    }

    @Override
    protected boolean processHeader(BufferedReader bufferedReader) {
        try {
            if (!parseDeviceColumns(bufferedReader)) {
                return false;
            }

            String line = bufferedReader.readLine();
            String[] values = line.split(",");
            int deviceColumnIndex = values.length;
            DeviceColumn firstDeviceColumn = !deviceColumnList.isEmpty() ? deviceColumnList.get(0) : null;
            if (firstDeviceColumn != null) {
                deviceColumnIndex = firstDeviceColumn.index;
            }
            for (int i = 0; i < deviceColumnIndex; i++) {
                String value = values[i].trim();
                addColumn(value, i);
            }
            for (int i = 0; i < deviceColumnList.size(); i++) {
                DeviceColumn deviceColumn = deviceColumnList.get(i);
                DeviceColumn nextDeviceColumn = deviceColumnList.size() > i + 1 ? deviceColumnList.get(i + 1) : null;
                int nextDeviceColumnIndex = nextDeviceColumn != null ? nextDeviceColumn.index : values.length;
                for (int j = deviceColumn.index; j < nextDeviceColumnIndex; j++) {
                    String value = values[j].trim();
                    String columnName = String.format("%s %s", deviceColumn.device.getName(), value);
                    addColumn(columnName, j, true);
                }
            }
        } catch (Exception e) {
            String message = String.format("Error reading header %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return true;
    }

    private boolean parseDeviceColumns(BufferedReader bufferedReader) {
        try {
            String line = bufferedReader.readLine();
            String[] values = line.split(",");
            for (int i = 0; i < values.length; i++) {
                if (values[i].isBlank()) {
                    continue;
                }
                String value = values[i].trim();
                if (!value.equals("Measurement") && !value.equals("Settings")) {
                    deviceColumnList.add(new DeviceColumn(value, i));
                }
            }
        } catch (Exception e) {
            String message = String.format("Error reading header %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return true;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            String timestring = fields[getColumnIndex("Time")];
            long timestamp = Utility.parseTime(timestring, "yyyy-MM-dd HH:mm:ss");

            double latitude = Double.parseDouble(fields[getColumnIndex("Lat")]);
            double longitude = Double.parseDouble(fields[getColumnIndex("Lon")]);
            double roll = Double.parseDouble(fields[getColumnIndex("Roll")]);
            double pitch = Double.parseDouble(fields[getColumnIndex("Pitch")]);
            double yaw = Double.parseDouble(fields[getColumnIndex("Yaw")]);
            double ptuYaw = Double.parseDouble(fields[getColumnIndex("PTU Yaw")]);
            double imcadTilt = Double.parseDouble(fields[getColumnIndex("iMCAD Tilt")]);
            double imcadAngle = Double.parseDouble(fields[getColumnIndex("iMCAD Angle")]);
            double imcadWidth = Double.parseDouble(fields[getColumnIndex("iMCAD Width")]);
            double csdsTilt = Double.parseDouble(fields[getColumnIndex("cSDS Tilt")]);
            double csdsAngle = Double.parseDouble(fields[getColumnIndex("cSDS Angle")]);
            double csdsWidth = Double.parseDouble(fields[getColumnIndex("cSDS Width")]);

            nbcrvStateList.add(new NbcrvState(timestamp, latitude, longitude, roll, pitch, yaw, ptuYaw, imcadTilt,
                    imcadAngle, imcadWidth, csdsTilt, csdsAngle, csdsWidth));
            tspiDataList.add(new TspiData(timestamp, Position.fromDegrees(latitude, longitude), null, null, yaw, pitch, roll));

            for (DeviceColumn deviceColumn : deviceColumnList) {
                String[] deviceValues = new String[sensorColumns.length];
                for (int i = 0; i < sensorColumns.length; i++) {
                    String columnName = String.format("%s %s", deviceColumn.device.getName(), sensorColumns[i]);
                    int columnIndex = getColumnIndex(columnName);
                    if (columnIndex >= 0) {
                        deviceValues[i] = fields[columnIndex];
                    } else {
                        deviceValues[i] = "";
                    }
                }
                deviceColumn.device.addDeviceState(createDeviceState(timestamp, deviceValues));
            }
        } catch (Exception e) {
            String message = String.format("Error parsing line in file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }

    private DeviceState createDeviceState(long timestamp, String[] values) {
        if (values.length < 7) {
            return null;
        }
        return new DeviceState(timestamp, values[0].equals("Y"), "", values[1], values[1],
                values[2], values[3], values[4], values[5], values[6], null, null, null);
    }

    private static class DeviceColumn {
        private final Device device;
        private final int index;

        private DeviceColumn(String name, int index) {
            this.device = new Device(name);
            this.index = index;
        }
    }
}
