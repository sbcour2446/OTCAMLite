package gov.mil.otc._3dvis.tools.rangefinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RangeFinderManager {

    private static final RangeFinderManager SINGLETON = new RangeFinderManager();
    private final List<RangeFinderEntry> rangeFinderEntryList = Collections.synchronizedList(new ArrayList<>());

    private RangeFinderManager() {
    }

    public static void addRangeFinderEntry(RangeFinderEntry rangeFinderEntry) {
        if (!SINGLETON.rangeFinderEntryList.contains(rangeFinderEntry)) {
            SINGLETON.rangeFinderEntryList.add(rangeFinderEntry);
        }
    }

    public static void removeRangeFinderEntry(RangeFinderEntry rangeFinderEntry) {
        SINGLETON.rangeFinderEntryList.remove(rangeFinderEntry);
    }

    public static List<RangeFinderEntry> getRangeFinderEntries() {
        return new ArrayList<>(SINGLETON.rangeFinderEntryList);
    }
}
