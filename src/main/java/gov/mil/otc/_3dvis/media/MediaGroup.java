package gov.mil.otc._3dvis.media;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MediaGroup {

    private final String name;
    private final Map<String, MediaSet> mediaSetMap = new TreeMap<>();

    public MediaGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addMediaSet(MediaSet mediaSet) {
        mediaSetMap.put(mediaSet.getName(), mediaSet);
    }

    public List<String> getMediaSetNames() {
        return new ArrayList<>(mediaSetMap.keySet());
    }

    public MediaSet getMediaSet(String name) {
        return mediaSetMap.get(name);
    }

    public List<MediaSet> getMediaSets() {
        return new ArrayList<>(mediaSetMap.values());
    }
}
