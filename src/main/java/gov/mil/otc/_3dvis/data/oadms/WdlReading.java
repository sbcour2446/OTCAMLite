package gov.mil.otc._3dvis.data.oadms;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.settings.SettingsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WdlReading extends TimedData {

    private final String operationalStatus;
    private final double azimuth;
    private final double elevation;
    private final double resolution;
    private final int traceId;
    private final int[] traceCountArray;
    private float[] hueValueArray = new float[0];
    private final List<WdlTile> wdlTileList = new ArrayList<>();
    private int numberOfRepeatTraces = 0;

    public WdlReading(long timestamp, String operationalStatus, double azimuth, double elevation, double resolution,
                      int traceId, String traceCountString, String hueValueString) {
        super(timestamp);
        this.operationalStatus = operationalStatus;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.resolution = resolution;
        this.traceId = traceId;
        this.traceCountArray = parseTraceCountString(traceCountString);
//        if (hueValueString == null) {
//            traceCountArray = parseTraceCountString(traceCountString);
//            hueValueArray = new float[traceCountArray.length];
//            calculateHueValues();
//            calculateWdlTiles();
//        } else {
//            hueValueArray = parseHueValueString(hueValueString);
//            populateWdlTileList(hueValueString);
//        }
    }

    public void averageRepeatTrace(String traceCountString) {
        numberOfRepeatTraces++;
        String[] values = traceCountString.split(",");
        try {
            for (int i = 0; i < values.length; i++) {
                if (i >= traceCountArray.length) {
                    break;
                }
                int traceCount = Integer.parseInt(values[i]);
                traceCountArray[i] = (traceCountArray[i] * numberOfRepeatTraces + traceCount) / (numberOfRepeatTraces + 1);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "WdlReading::parseTraceCountString", e);
        }
    }

    public boolean recalculate() {
//        if (traceCountArray == null) {
//            traceCountArray = parseTraceCountString(traceCountString);
//        }
//        hueValueArray = new float[traceCountArray.length];
//        calculateHueValues();
//        calculateWdlTiles();
        createWdlTiles();
        return true;
    }

    private int[] parseTraceCountString(String traceCountString) {
        String[] values = traceCountString.split(",");
        int[] tempArray = new int[values.length];
        try {
            for (int i = 0; i < values.length; i++) {
                tempArray[i] = Integer.parseInt(values[i]);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "WdlReading::parseTraceCountString", e);
        }
        return tempArray;
    }

    private float[] parseHueValueString(String hueValueString) {
        String[] values = hueValueString.split(",");
        float[] tempArray = new float[values.length];
        try {
            for (int i = 0; i < values.length; i++) {
                tempArray[i] = Float.parseFloat(values[i]);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "WdlReading::parseHueValueString", e);
        }
        return tempArray;
    }

    private void createWdlTiles() {
        wdlTileList.clear();

        int tilesPerWindow = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarMedianFilterWindowSize() / resolution);
        int halfTilesPerWindow = tilesPerWindow / 2;
        int hueIndex = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarClippedStartSize() / resolution);
        int maxRange = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarMaxRange() / resolution);
        int concentrationThreshold = SettingsManager.getSettings().getNbcrvSettings().getLidarConcentrationThreshold();
        float maxConcentration = SettingsManager.getSettings().getNbcrvSettings().getLidarMaxConcentration();
        float previousHue = 0;
        double range1 = 0.0;
        double range2 = 0.0;
        double distanceFactor = SettingsManager.getSettings().getNbcrvSettings().getLidarDistanceFactor();

        hueIndex = Math.max(hueIndex, halfTilesPerWindow);
        maxRange = Math.min(maxRange, traceCountArray.length);

        int startIndex = hueIndex - halfTilesPerWindow;
        int[] sortedArray = Arrays.copyOfRange(traceCountArray, startIndex, startIndex + tilesPerWindow);
        Arrays.sort(sortedArray);

        for (; hueIndex < maxRange; hueIndex++) {
            int median = sortedArray[halfTilesPerWindow];

            float concentration = (float) (Math.abs(traceCountArray[hueIndex] - median)
                    * Math.pow((float) (hueIndex + 1) / traceCountArray.length, distanceFactor));

            if (concentration < concentrationThreshold) {
                previousHue = -1;
                continue;
            }

            if (concentration > maxConcentration) {
                concentration = maxConcentration;
            }

            float concentrationRatio = concentration / maxConcentration;
            int tempHue = Math.round(500 * (1 - concentrationRatio) / 6);
            float hue = tempHue / 100f;

            if (hue != previousHue) {
                if (previousHue >= 0) {
                    wdlTileList.add(new WdlTile(range1, range2, previousHue));
                }
                range1 = getResolution() * hueIndex / 6371000.0;
            }

            range2 = getResolution() * (hueIndex + 1) / 6371000.0;
            previousHue = hue;
        }

        if (previousHue >= 0) {
            wdlTileList.add(new WdlTile(range1, range2, previousHue));
        }
    }

    private void populateWdlTileList(String hueValueString) {
        String[] values = hueValueString.split(",");
        try {
            int maxRange = SettingsManager.getSettings().getNbcrvSettings().getLidarMaxRange();
            float previousHue = 0;
            double range1 = 0.0;
            double range2 = 0.0;
            for (int i = 0; i < values.length; i++) {
                if (i * getResolution() > maxRange) {
                    if (previousHue > 0) {
                        wdlTileList.add(new WdlTile(range1, range2, previousHue));
                    }
                    break;
                }
                float hue = Float.parseFloat(values[i]);
                if (hue > 0) {
                    if (hue == previousHue) {
                        range2 = getResolution() * (i + 1) / 6371000.0;
                    } else {
                        if (previousHue > 0) {
                            wdlTileList.add(new WdlTile(range1, range2, previousHue));
                        }
                        range1 = getResolution() * i / 6371000.0;
                        range2 = getResolution() * (i + 1) / 6371000.0;
                    }
                } else if (previousHue > 0) {
                    wdlTileList.add(new WdlTile(range1, range2, previousHue));
                }
                previousHue = hue;
            }
            if (previousHue > 0) {
                wdlTileList.add(new WdlTile(range1, range2, previousHue));
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "WdlReading::populateWdlTileList", e);
        }
    }

    public List<WdlTile> getWdlTileList() {
        return wdlTileList;
    }

    private void calculateWdlTiles() {
        int tilesPerWindow = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarMedianFilterWindowSize() / resolution);
        int halfTilesPerWindow = tilesPerWindow / 2;
        int clipStartIndex = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarClippedStartSize() / resolution);
        int hueIndex = 0;
        float maxConcentration = SettingsManager.getSettings().getNbcrvSettings().getLidarMaxConcentration();
        int concentrationThreshold = SettingsManager.getSettings().getNbcrvSettings().getLidarConcentrationThreshold();
        int maxRange = SettingsManager.getSettings().getNbcrvSettings().getLidarMaxRange();
        float previousHue = 0;
        double range1 = 0.0;
        double range2 = 0.0;

        for (; hueIndex < clipStartIndex; hueIndex++) {
            if (hueIndex < hueValueArray.length) {
                hueValueArray[hueIndex] = 0;
            }
        }

        int startIndex = hueIndex - halfTilesPerWindow;
        int[] sortedArray = Arrays.copyOfRange(traceCountArray, startIndex, startIndex + tilesPerWindow);
        Arrays.sort(sortedArray);

        for (; hueIndex < traceCountArray.length - halfTilesPerWindow; hueIndex++) {
            int median = sortedArray[halfTilesPerWindow];

            float concentration = (float) (Math.abs(traceCountArray[hueIndex] - median)); //* Math.sqrt(hueIndex));
            //* Math.pow((float) (hueIndex + 1) / 1000, 2));

            if (concentration > maxConcentration) {
                concentration = maxConcentration;
            }

            float concentrationRatio = concentration / maxConcentration;
            int tempHue = Math.round(500 * (1 - concentrationRatio) / 6);
            float hue = -1;

            if (concentration > concentrationThreshold) {
                hue = tempHue / 100f;
            }

            hueValueArray[hueIndex] = hue;

            if (startIndex + tilesPerWindow < traceCountArray.length) {
                insertAndRemove(sortedArray, traceCountArray[startIndex + tilesPerWindow], traceCountArray[startIndex]);
                startIndex++;
            }

            if (hueIndex * getResolution() > maxRange) {
                break;
            }
            if (hue >= 0) {
                if (hue == previousHue) {
                    range2 = getResolution() * (hueIndex + 1) / 6371000.0;
                } else {
                    if (previousHue > 0) {
                        wdlTileList.add(new WdlTile(range1, range2, previousHue));
                    }
                    range1 = getResolution() * hueIndex / 6371000.0;
                    range2 = getResolution() * (hueIndex + 1) / 6371000.0;
                }
            } else if (previousHue >= 0) {
                wdlTileList.add(new WdlTile(range1, range2, previousHue));
            }
            previousHue = hue;
        }

        if (previousHue > 0) {
            wdlTileList.add(new WdlTile(range1, range2, previousHue));
        }

        for (; hueIndex < hueValueArray.length; hueIndex++) {
            hueValueArray[hueIndex] = 0;
        }
    }

    private void calculateHueValues() {
        int tilesPerWindow = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarMedianFilterWindowSize() / resolution);
        int halfTilesPerWindow = tilesPerWindow / 2;
        int clipStartIndex = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarClippedStartSize() / resolution);
        int hueIndex = 0;
        float maxConcentration = SettingsManager.getSettings().getNbcrvSettings().getLidarMaxConcentration();
        int concentrationThreshold = SettingsManager.getSettings().getNbcrvSettings().getLidarConcentrationThreshold();

        for (; hueIndex < clipStartIndex; hueIndex++) {
            if (hueIndex < hueValueArray.length) {
                hueValueArray[hueIndex] = 0;
            }
        }

        int startIndex = hueIndex - halfTilesPerWindow;
        int[] sortedArray = Arrays.copyOfRange(traceCountArray, startIndex, startIndex + tilesPerWindow);
        Arrays.sort(sortedArray);

        for (; hueIndex < traceCountArray.length - halfTilesPerWindow; hueIndex++) {
            int median = sortedArray[halfTilesPerWindow];

            float concentration = (float) (Math.abs(traceCountArray[hueIndex] - median) * Math.pow((hueIndex + 1) / 1000.0, 2));

            if (concentration > maxConcentration) {
                concentration = maxConcentration;
            }

            float concentrationRatio = 1;
            if (concentration > concentrationThreshold) {
                concentrationRatio = concentration / maxConcentration;
            }

            float hue = 300 * (1 - concentrationRatio);
            hueValueArray[hueIndex] = hue;

            if (startIndex + tilesPerWindow < traceCountArray.length) {
                insertAndRemove(sortedArray, traceCountArray[startIndex + tilesPerWindow], traceCountArray[startIndex]);
                startIndex++;
            }
        }

        for (; hueIndex < hueValueArray.length; hueIndex++) {
            hueValueArray[hueIndex] = 0;
        }
    }

    private void insertAndRemove(int[] array, int valueToAdd, int valueToRemove) {
        int insertIndex = binaryInsert(array, valueToAdd, 0, array.length - 1);
        int removeIndex = binarySearch(array, valueToRemove, 0, array.length - 1);

        if (insertIndex <= removeIndex) {
            for (int i = insertIndex; i <= removeIndex; i++) {
                int temp = array[i];
                array[i] = valueToAdd;
                valueToAdd = temp;
            }
        } else {
            for (int i = insertIndex - 1; i >= removeIndex; i--) {
                int temp = array[i];
                array[i] = valueToAdd;
                valueToAdd = temp;
            }
        }
    }

    private int binarySearch(int[] array, int value, int low, int high) {
        int index = Integer.MIN_VALUE;
        while (low <= high) {
            int mid = low + ((high - low) / 2);
            if (array[mid] < value) {
                low = mid + 1;
            } else if (array[mid] > value) {
                high = mid - 1;
            } else if (array[mid] == value) {
                index = mid;
                break;
            }
        }
        return index;
    }

    public int binaryInsert(int[] array, int value, int low, int high) {
        while (low <= high) {
            int mid = low + ((high - low) / 2);
            if (array[mid] < value) {
                low = mid + 1;
            } else if (array[mid] > value) {
                high = mid - 1;
            } else if (array[mid] == value) {
                return mid;
            }
        }
        return low;
    }

    private void calculateMeanFilter() {
        int tilesPerWindow = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarMedianFilterWindowSize() / resolution);
        int halfTilesPerWindow = tilesPerWindow / 2;
        int clipStartIndex = (int) (SettingsManager.getSettings().getNbcrvSettings().getLidarClippedStartSize() / resolution);
        int runningTotal = 0;
        int hueIndex = 0;
        float maxConcentration = 2000;

        for (; hueIndex < clipStartIndex; hueIndex++) {
            if (hueIndex < hueValueArray.length) {
                hueValueArray[hueIndex] = 0;
            }
        }

        for (int i = 0; i < traceCountArray.length; i++) {
            runningTotal += traceCountArray[i];
            if (i >= tilesPerWindow) {
                runningTotal -= traceCountArray[i - tilesPerWindow];
            }
            if (i >= clipStartIndex) {
                int mean = runningTotal / tilesPerWindow;
                float concentration = (float) (Math.abs(traceCountArray[hueIndex] - mean) * Math.pow((hueIndex + 1) / 1000.0, 2));

                if (concentration > maxConcentration) {
                    concentration = maxConcentration;
                }

                float concentrationRatio = 1;
                if (concentration > 50) {
                    concentrationRatio = concentration / maxConcentration;
                }

                float hue = 300 * (1 - concentrationRatio);
                hueValueArray[hueIndex++] = hue;
            }
        }

        for (; hueIndex < hueValueArray.length; hueIndex++) {
            hueValueArray[hueIndex] = 0;
        }
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getElevation() {
        return elevation;
    }

    public double getResolution() {
        return resolution;
    }

    public int getTraceId() {
        return traceId;
    }

    public String getTraceCountString() {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (int traceCount : traceCountArray) {
            stringBuilder.append(prefix);
            stringBuilder.append(traceCount);
            if (prefix.isEmpty()) {
                prefix = ",";
            }
        }
        return stringBuilder.toString();
    }

    public int[] getTraceCounts() {
        return traceCountArray;
    }

    public String getHueValueString() {
        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (float hue : hueValueArray) {
            stringBuilder.append(prefix);
            stringBuilder.append(hue);
            if (prefix.isEmpty()) {
                prefix = ",";
            }
        }
        return stringBuilder.toString();
    }

    public float[] getHueValueArray() {
        return hueValueArray;
    }

    public class WdlTile {
        public final double range1;
        public final double range2;
        public final float hue;

        public WdlTile(double range1, double range2, float hue) {
            this.range1 = range1;
            this.range2 = range2;
            this.hue = hue;
        }
    }
}
