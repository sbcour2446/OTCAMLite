package gov.mil.otc._3dvis.data.file.delimited;

import gov.mil.otc._3dvis.data.file.ImportFile;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DelimitedFile extends ImportFile {

    public static final class ColumnInfo {
        private final String columnName;
        private final boolean containsAdditionalCharacters;
        private final boolean optional;
        private int index = -1;

        public ColumnInfo(String columnName, boolean containsAdditionalCharacters, boolean optional) {
            this.columnName = columnName.toLowerCase();
            this.containsAdditionalCharacters = containsAdditionalCharacters;
            this.optional = optional;
        }

        public String getColumnName() {
            return columnName;
        }

        public boolean isContainsAdditionalCharacters() {
            return containsAdditionalCharacters;
        }

        public boolean isOptional() {
            return optional;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    private final Map<String, ColumnInfo> columnMap = new HashMap<>();
    protected final Map<String, Integer> headerIndexMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    protected final Map<String, Integer> otherFieldsIndexMap = new LinkedHashMap<>();
    protected final List<String> optionalFields = new ArrayList<>();
    protected int headerLineNumber = 0;
    protected String headerStartField = "";
    protected boolean isValidFile = false;
    protected boolean hasHeader = true;

    protected DelimitedFile(File file) {
        this(file, 1);
    }

    protected DelimitedFile(File file, int headerLineNumber) {
        super(file);
        this.headerLineNumber = headerLineNumber;
    }

    protected DelimitedFile(File file, String headerStartField) {
        super(file);
        this.headerStartField = headerStartField;
    }

    protected abstract String[] getFields(String line);

    protected abstract void processLine(String[] fields);

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
                if (verifyFields(fields)) {
                    processLine(fields);
                }
                lineCount++;
                status = (double) lineCount / totalLines;
            }
        } catch (Exception e) {
            String message = String.format("DelimitedFile::doProcessFile:Error processing file %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }

        return !cancelRequested;
    }

    protected boolean processHeader(BufferedReader bufferedReader) {
        if (!hasHeader) {
            return true;
        }

        try {
            String line = bufferedReader.readLine();
            int lineNumber = 1;
            String[] header = null;
            if (headerLineNumber > 0) {
                while (lineNumber++ < headerLineNumber) {
                    line = bufferedReader.readLine();
                }
                header = getFields(line);
            } else if (!headerStartField.isBlank()) {
                while (line != null) {
                    header = getFields(line);
                    if (header != null && header.length > 0 && header[0].equalsIgnoreCase(headerStartField)) {
                        break;
                    }
                    line = bufferedReader.readLine();
                }
            } else {
                header = getFields(line);
            }

            if (header == null) {
                return false;
            }

            for (Map.Entry<String, ColumnInfo> entry : columnMap.entrySet()) {
                for (int i = 0; i < header.length; i++) {
                    String headerValue = header[i].toLowerCase().trim();
                    ColumnInfo columnInfo = entry.getValue();
                    if (columnInfo.containsAdditionalCharacters && headerValue.contains(columnInfo.columnName)) {
                        columnInfo.setIndex(i);
                    } else if (headerValue.equals(columnInfo.columnName)) {
                        columnInfo.setIndex(i);
                    }
                }
            }

            for (int i = 0; i < header.length; i++) {
                String headerValue = header[i].toLowerCase().trim();
                if (headerIndexMap.containsKey(headerValue)) {
                    headerIndexMap.put(headerValue, i);
                } else {
                    otherFieldsIndexMap.put(headerValue, i);
                }
            }
        } catch (Exception e) {
            String message = String.format("DelimitedFile::processHeader:Error reading header %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
        }

        for (Map.Entry<String, Integer> entry : headerIndexMap.entrySet()) {
            if (entry.getValue() == -1 && !optionalFields.contains(entry.getKey())) {
                String message = "processHeader::header not found:" + entry.getKey() + ", " + getFile().getAbsolutePath();
                Logger.getGlobal().log(Level.WARNING, message);
                return false;
            }
        }
        return true;
    }

    public int countLines() {
        int lines = 0;
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean endsWithoutNewLine = false;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                endsWithoutNewLine = (c[readChars - 1] != '\n');
            }
            if (endsWithoutNewLine) {
                ++count;
            }
            lines = count;
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, "DelimitedFile::countLines:", e);
        }
        return lines;
    }

    public boolean isValidFile() {
        if (isValidFile) {
            return true;
        } else {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
                isValidFile = processHeader(bufferedReader);
            } catch (Exception e) {
                String message = String.format("DelimitedFile::isValidFile:%s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
        return isValidFile;
    }

    public String[] getHeader() {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line = bufferedReader.readLine();
            return getFields(line);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return new String[0];
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    protected void addColumn(ColumnInfo columnInfo) {
        columnMap.put(columnInfo.columnName, columnInfo);
    }

    protected void addColumn(String columnName) {
        addColumn(columnName, false);
    }

    protected void addColumn(String columnName, boolean optional) {
        headerIndexMap.put(columnName.toLowerCase(), -1);
        if (optional) {
            optionalFields.add(columnName.toLowerCase());
        }
    }

    protected void addColumn(String columnName, int index) {
        addColumn(columnName, index, false);
    }

    protected void addColumn(String columnName, int index, boolean optional) {
        headerIndexMap.put(columnName.toLowerCase(), index);
        if (optional) {
            optionalFields.add(columnName.toLowerCase());
        }
    }

    protected ColumnInfo getColumn(String columnName) {
        return columnMap.get(columnName.toLowerCase());
    }

    protected int getColumnIndex(String columnName) {
        return headerIndexMap.getOrDefault(columnName.toLowerCase(), -1);
    }

    protected void addOtherColumn(String columnName, int index) {
        otherFieldsIndexMap.put(columnName.toLowerCase(), index);
    }

    protected int getOtherColumnIndex(String columnName) {
        return otherFieldsIndexMap.getOrDefault(columnName.toLowerCase(), -1);
    }

    protected Map<String, Integer> getOtherFieldsIndexMap() {
        return otherFieldsIndexMap;
    }

    protected boolean verifyFields(String[] fields) {
        if (fields.length < headerIndexMap.size()) {
            return false;
        }

        for (Map.Entry<String, Integer> entry : headerIndexMap.entrySet()) {
            if ((entry.getValue() < 0 || entry.getValue() >= fields.length || fields[entry.getValue()].isBlank()) &&
                    !optionalFields.contains(entry.getKey())) {
                return false;
            }
        }
        return true;
    }

    protected double tryParseDouble(String field) {
        if (field == null || field.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(field);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Removes leading and trailing single and double quotes from a string entry.
     *
     * @param entry The string to sanitize.
     * @return The sanitized string.
     */
    protected String sanitizeString(String entry) {

        // Trim any trailing or leading spaces
        String retVal = entry.trim();
        // Remove leading quotes
        if (retVal.startsWith("\"") || retVal.startsWith("'") || retVal.startsWith("`")) {
            retVal = retVal.substring(1);
        }

        // Remove trailing quotes
        if (retVal.endsWith("\"") || retVal.endsWith("'") || retVal.endsWith("`")) {
            retVal = retVal.substring(0, retVal.length() - 1);
        }
        return retVal;
    }
}
