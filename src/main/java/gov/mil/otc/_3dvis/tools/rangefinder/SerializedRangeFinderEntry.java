package gov.mil.otc._3dvis.tools.rangefinder;

import javafx.scene.paint.Color;

public class SerializedRangeFinderEntry {

    private final RangeFinderEntity source;
    private final RangeFinderEntity target;
    private final int minRange;
    private final int maxRange;
    private final Color color;
    private final boolean showLines;
    private final boolean followTerrain;
    private final boolean ignoreFilters;

    public SerializedRangeFinderEntry(RangeFinderEntry rangeFinderEntry) {
        source = rangeFinderEntry.getSource();
        target = rangeFinderEntry.getTarget();
        minRange = rangeFinderEntry.getMinRange();
        maxRange = rangeFinderEntry.getMaxRange();
        color = rangeFinderEntry.getColor();
        showLines = rangeFinderEntry.getShowLines();
        followTerrain = rangeFinderEntry.isFollowTerrain();
        ignoreFilters = rangeFinderEntry.isIgnoreFilters();
    }

    public RangeFinderEntry toRangeFinderEntry() {
        RangeFinderEntry rangeFinderEntry = new RangeFinderEntry(source, target);
        rangeFinderEntry.setMinRange(minRange);
        rangeFinderEntry.setMaxRange(maxRange);
        rangeFinderEntry.setColor(color);
        rangeFinderEntry.setShowLines(showLines);
        rangeFinderEntry.setFollowTerrain(followTerrain);
        rangeFinderEntry.setIgnoreFilters(ignoreFilters);
        return rangeFinderEntry;
    }
}
