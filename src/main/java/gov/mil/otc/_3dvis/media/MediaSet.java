package gov.mil.otc._3dvis.media;

import gov.mil.otc._3dvis.entity.base.EntityId;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class MediaSet {

    private final String name;
    private final String mediaGroupName;
    private final Map<Long, MediaFile> mediaFiles = new TreeMap<>();

    public MediaSet(String name, String mediaGroupName) {
        this.name = name;
        this.mediaGroupName = mediaGroupName;
    }

    public String getName() {
        return name;
    }

    public String getMediaGroupName() {
        return mediaGroupName;
    }

    public void addMediaFile(MediaFile file) {
        mediaFiles.put(file.getStartTime(), file);
    }

    public Map<Long, MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public MediaFile getMediaFileAt(long timestamp) {
        for (Map.Entry<Long, MediaFile> entry : mediaFiles.entrySet()) {
            long startTime = entry.getKey();
            if (timestamp < startTime) {
                break;
            } else {
                MediaFile mediaFile = entry.getValue();
                if (timestamp < mediaFile.getStopTime()) {
                    return mediaFile;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaSet mediaSet = (MediaSet) o;
        return Objects.equals(name, mediaSet.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
