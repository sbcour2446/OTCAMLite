package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.data.file.delimited.DelimitedFile;
import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreatDataFile extends DelimitedFile {

    private static final String TIME_FORMAT = "yyyy DDD HH:mm:ss.SSS";
    private static final String TIMESTAMP_COLUMN = "Time (HH:MM:SS.SSS)";
    private final List<ValuePairTimedData> valuePairList = new ArrayList<>();
    private final List<TargetTimedData> azElDataList = new ArrayList<>();
    private int year = 2024;
    private int dayOfYear = 1;
    private boolean canIncrementDayOfYear = true;
    private Double azimuth = null;
    private Double elevation = null;
    private Integer range = null;

    public ThreatDataFile(File file) {
        super(file);
    }

    public List<ValuePairTimedData> getValuePairList() {
        return valuePairList;
    }

    public List<TargetTimedData> getAzElDataList() {
        return azElDataList;
    }

    @Override
    protected String[] getFields(String line) {
        return line.split("\t", -1);
    }

    @Override
    protected boolean processHeader(BufferedReader bufferedReader) {
        try {
            String line = bufferedReader.readLine();
            boolean foundYearDayInfo = false;
            while (!foundYearDayInfo && line != null) {
                if (!line.isBlank()) {
                    int yearIndex = line.indexOf("Year");
                    if (yearIndex >= 0) {
                        try {
                            year = Integer.parseInt(line.substring(yearIndex + 5, yearIndex + 9));
                            foundYearDayInfo = true;
                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, "", e);
                        }
                    }
                    int dayOfYearIndex = line.indexOf("Day");
                    if (dayOfYearIndex >= 0) {
                        try {
                            String dayOfYearString = line.substring(dayOfYearIndex + 4).trim();
                            dayOfYear = Integer.parseInt(dayOfYearString);
                        } catch (Exception e) {
                            Logger.getGlobal().log(Level.WARNING, "", e);
                        }
                    }
                }
                line = bufferedReader.readLine();
            }

            while (line != null && line.isBlank()) {
                line = bufferedReader.readLine();
            }

            if (line != null) {
                String[] header = getFields(line);
                for (int i = 0; i < header.length; i++) {
                    String headerValue = header[i].toLowerCase().trim();
                    if (!headerValue.isBlank()) {
                        addOtherColumn(headerValue, i);
                    }
                }
            }
        } catch (Exception e) {
            String message = String.format("ThreatDataFile::processHeader:Error reading header %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }

        return true;
    }

    @Override
    protected void processLine(String[] fields) {
        try {
            int timestampIndex = getOtherColumnIndex(TIMESTAMP_COLUMN);
            if (timestampIndex < 0) {
                return;
            }

            String timestampString = year + " " + String.format("%03d", dayOfYear) + " " + fields[timestampIndex];
            if (timestampString.length() != TIME_FORMAT.length()) {
                return;
            }
            if (timestampString.startsWith("24", 9)) {
                if (canIncrementDayOfYear) {
                    canIncrementDayOfYear = false;
                    dayOfYear++;
                }
                timestampString = timestampString.substring(0, 9) + "00" + timestampString.substring(11);
            } else {
                canIncrementDayOfYear = true;
            }
            long timestamp = Utility.parseTime(timestampString, DateTimeFormatter.ofPattern(TIME_FORMAT));
            if (timestamp <= 0) {
                return;
            }

            azimuth = null;
            elevation = null;
            range = null;
            Map<String, Integer> fieldMap = getOtherFieldsIndexMap();
            Map<String, String> values = new HashMap<>();
            for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
                if (field.getKey().equalsIgnoreCase(TIMESTAMP_COLUMN)) {
                    continue;
                }
                addValue(field.getKey(), fields[field.getValue()], values);
            }
            valuePairList.add(new ValuePairTimedData(timestamp, values));
            azElDataList.add(new TargetTimedData(timestamp, azimuth, elevation, range));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "ThreatDataFile::processLine:", e);
        }
    }

    private void addValue(String headerName, String value, Map<String, String> values) {
        String headerNameLower = headerName.toLowerCase();
        if (headerNameLower.equals("azimuth (degrees)")) {
            try {
                azimuth = Double.parseDouble(value);
                values.put("Azimuth", String.valueOf(azimuth));
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ThreatDataFile::addValue:", e);
                values.put("Azimuth", value);
            }
        } else if (headerNameLower.equals("elevation (degrees)")) {
            try {
                elevation = Double.parseDouble(value);
                values.put("Elevation", String.valueOf(elevation));
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ThreatDataFile::addValue:", e);
                values.put("Elevation", value);
            }
        } else if (headerNameLower.equals("range (nautical miles)")) {
            try {
                double miles = Double.parseDouble(value);
                range = (int) (miles * 1852);
                values.put("Range", String.valueOf(range));
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, "ThreatDataFile::addValue:", e);
                values.put("Range", value + " nm");
            }
        } else if (headerNameLower.startsWith("search scan selected")) {
            //0=Off, 1=Narrow 1, 2=Narrow 2, 3 = Wide 1, 4=Wide 2, 5=All
            value = switch (value) {
                case "0" -> "Off";
                case "1" -> "Narrow 1";
                case "2" -> "Narrow 2";
                case "3" -> "Wide 1";
                case "4" -> "Wide 2";
                case "5" -> "All";
                default -> value;
            };
            values.put("Search Scan Selected", value);
        } else if (headerNameLower.startsWith("jamming declared")) {
            //0=No, 1=Yes
            values.put("Jamming Declared", value.contains("1") ? "yes" : "");
        } else if (headerNameLower.startsWith("track mode")) {
            //0=None, 1=Disabled, 2=Acquisition, 3=Track, 4=Coast
            value = switch (value) {
                case "0" -> "None";
                case "1" -> "Disabled";
                case "2" -> "Acquisition";
                case "3" -> "Track";
                case "4" -> "Coast";
                default -> value;
            };
            values.put("Track Mode", value);
        } else if (headerNameLower.startsWith("missile 1 phase")) {
            //0=Not Launched, 1-5 = Missile Phase
            value = switch (value) {
                case "0" -> "Not Launched";
                case "1" -> "Missile Phase";
                default -> value;
            };
            values.put("Missile 1 Phase", value);
        } else if (headerNameLower.startsWith("missile 2 phase")) {
            //0=Not Launched, 1-5 = Missile Phase
            value = switch (value) {
                case "0" -> "Not Launched";
                case "1" -> "Missile Phase";
                default -> value;
            };
            values.put("Missile 2 Phase", value);
        } else if (headerNameLower.startsWith("system mode")) {
            //0=Combat, 1=Check
            value = switch (value) {
                case "0" -> "Combat";
                case "1" -> "Check";
                default -> value;
            };
            values.put("System Mode", value);
        } else if (headerNameLower.startsWith("target in zone")) {
            //0=No, 1=Yes
            values.put("Target in Zone", value.contains("1") ? "Yes" : "");
        } else if (headerNameLower.startsWith("ttr search mode")) {
            //0=No, 1=On
            values.put("TTR Search Mode", value.contains("1") ? "On" : "");
        } else if (headerNameLower.startsWith("transmitter(0=off,1=on)")) {
            values.put("transmitter", value.contains("1") ? "on" : "");
        } else if (headerNameLower.startsWith("transmitter on/off")) {
            //0=No, 1=On
            values.put("Transmitter", value.contains("1") ? "On" : "");
        } else if (headerNameLower.startsWith("range scale")) {
            //0=100KM, 1=150KM, 2=250KM, 3=0.5-50KM
            value = switch (value) {
                case "0" -> "100KM";
                case "1" -> "150KM";
                case "2" -> "250KM";
                case "3" -> "0.5-50KM";
                default -> value;
            };
            values.put("Range Scale", value);
        } else if (headerNameLower.startsWith("antenna mode")) {
            //0=Fixed 6 RPM, 1=Fixed 12 RPM, 2=Manual CW, 3=Manual CCW, 4-Stopped
            value = switch (value) {
                case "0" -> "Fixed 6 RPM";
                case "1" -> "Fixed 12 RPM";
                case "2" -> "Manual CW";
                case "3" -> "Manual CCW";
                case "4" -> "Stopped";
                default -> value;
            };
            values.put("Antenna Mode", value);
        } else if (headerNameLower.startsWith("target in zone missile")) {
            //0=No, 1=In Zone
            values.put("Target in Zone Missile", value.contains("1") ? "Yes" : "");
        } else if (headerNameLower.startsWith("target in zone gun")) {
            //0=No, 1=In Zone
            values.put("Target in Zone Gun", value.contains("1") ? "Yes" : "");
        } else if (headerNameLower.startsWith("ttr transmitter status")) {
            //0=Off, 1=HV On
            values.put("TTR Transmitter Status", value.contains("1") ? "On" : "");
        } else if (headerNameLower.startsWith("tir transmitter status")) {
            //0=Off, 1=HV On
            values.put("TIR Transmitter Status", value.contains("1") ? "On" : "");
        } else if (headerNameLower.startsWith("launch permitted")) {
            //0=Off, 1=Missile Ready
            values.put("Launch Permitted", value.contains("1") ? "Missile Ready" : "");
        } else if (headerNameLower.startsWith("jamming control")) {
            //0=Off, 1=On
            values.put("Jamming Control", value.contains("1") ? "On" : "");
        } else if (headerNameLower.startsWith("combat_standby")) {
            //0=Combat, 1=Standby
            value = switch (value) {
                case "0" -> "Combat";
                case "1" -> "Standby";
                default -> value;
            };
            values.put("Combat Standby", value);
        } else if (headerNameLower.startsWith("tir search mode")) {
            values.put("TIR Search Mode", value.contains("1") ? "on" : "");
        } else if (headerNameLower.startsWith("jamming declared")) {
            values.put("Jamming Declared", value.contains("1") ? "yes" : "");
        } else if (headerNameLower.startsWith("eccm")) {
            values.put("ECCM", value.contains("1") ? "TRUE" : "");
        } else if (headerNameLower.startsWith("fire button")) {
            values.put("Fire Button", value.contains("1") ? "TRUE" : "");
        }
    }
}
