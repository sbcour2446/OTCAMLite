package gov.mil.otc._3dvis.media;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MediaCollection {

    private final Map<String, MediaGroup> mediaGroupMap = new TreeMap<>();

    public void copy(MediaCollection mediaCollection) {
        for(MediaGroup mediaGroup : mediaCollection.getMediaGroups()) {
            mediaGroupMap.put(mediaGroup.getName(), mediaGroup);
        }
    }

    public void addMediaFile(MediaFile mediaFile) {
        MediaGroup mediaGroup = mediaGroupMap.get(mediaFile.getMediaGroup());
        if (mediaGroup == null) {
            mediaGroup = new MediaGroup(mediaFile.getMediaGroup());
            mediaGroupMap.put(mediaFile.getMediaGroup(), mediaGroup);
        }
        MediaSet mediaSet = mediaGroup.getMediaSet(mediaFile.getMediaSet());
        if (mediaSet == null) {
            mediaSet = new MediaSet(mediaFile.getMediaSet(), mediaGroup.getName());
            mediaGroup.addMediaSet(mediaSet);
        }
        mediaSet.addMediaFile(mediaFile);
    }

    public List<String> getMediaGroupNames() {
        return new ArrayList<>(mediaGroupMap.keySet());
    }


    public MediaGroup getMediaGroup(String name) {
        return mediaGroupMap.get(name);
    }

    public List<MediaGroup> getMediaGroups() {
        return new ArrayList<>(mediaGroupMap.values());
    }

    public boolean hasMedia() {
        return !mediaGroupMap.isEmpty();
    }
}
