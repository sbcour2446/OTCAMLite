package gov.mil.otc._3dvis.settings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTableSettings {

    private final Map<String, Boolean> columnVisibleMap = new ConcurrentHashMap<>();
    private final Map<String, Double> columnWidthMap = new ConcurrentHashMap<>();

    public Boolean isColumnVisible(String column) {
        return columnVisibleMap.computeIfAbsent(column, k -> true);
    }

    public void setColumnVisible(String column, boolean isVisible) {
        columnVisibleMap.put(column, isVisible);
    }

    public Double getColumnWidth(String column) {
        return columnWidthMap.get(column);
    }

    public void setColumnWidth(String column, double width) {
        columnWidthMap.put(column, width);
    }
}
