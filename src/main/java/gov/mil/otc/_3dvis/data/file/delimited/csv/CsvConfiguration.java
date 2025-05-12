package gov.mil.otc._3dvis.data.file.delimited.csv;

import java.util.EnumMap;

public class CsvConfiguration {

    public static final String NAME = "CsvConfiguration.json";

    public enum Field {
        TIMESTAMP,
        LATITUDE,
        LONGITUDE,
        ALTITUDE,
        HEADING,
        PITCH,
        ROLL,
        SPEED,
        VELOCITY_X,
        VELOCITY_Y,
        VELOCITY_Z
    }

    public enum ValueType {
        TIMESTAMP,
        DEGREES,
        RADIANS,
        METERS,
        FEET,
        MSL_METERS,
        HAE_METERS,
        MSL_FEET
    }

    public static class FieldConfiguration {
        public String headerName;
        public ValueType valueType;

        public FieldConfiguration(String headerName, ValueType valueType) {
            this.headerName = headerName;
            this.valueType = valueType;
        }
    }

    private final int headerLineNumber;
    private final String headerStartField;
    private final int dataLineNumber;
    private final EnumMap<Field, FieldConfiguration> fieldConfigurationMap = new EnumMap<>(Field.class);
    private final String timestampFormat;
    private final boolean needsYear;
    private final int year;

    public CsvConfiguration(int headerLineNumber, String headerStartField, int dataLineNumber, String timestampFormat,
                            boolean needsYear, int year) {
        this.headerLineNumber = headerLineNumber;
        this.headerStartField = headerStartField;
        this.dataLineNumber = dataLineNumber;
        this.timestampFormat = timestampFormat;
        this.needsYear = needsYear;
        this.year = year;
    }

    public void addFieldConfiguration(Field field, FieldConfiguration fieldConfiguration) {
        fieldConfigurationMap.put(field, fieldConfiguration);
    }

    public int getHeaderLineNumber() {
        return headerLineNumber;
    }

    public String getHeaderStartField() {
        return headerStartField;
    }

    public int getDataLineNumber() {
        return dataLineNumber;
    }

    public EnumMap<Field, FieldConfiguration> getFieldConfigurationMap() {
        return fieldConfigurationMap;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public boolean isNeedsYear() {
        return needsYear;
    }

    public int getYear() {
        return year;
    }
}
