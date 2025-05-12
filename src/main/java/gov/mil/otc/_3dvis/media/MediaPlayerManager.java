package gov.mil.otc._3dvis.media;

import gov.mil.otc._3dvis.entity.base.IEntity;

import java.util.HashMap;
import java.util.Map;

public class MediaPlayerManager {

    private static final MediaPlayerManager SINGLETON = new MediaPlayerManager();
    //    private final Map<String, MediaPlayer> mediaPlayerMap = new HashMap<>();
    private final Map<String, MediaPlayerWindow> mediaPlayerWindowMap = new HashMap<>();
    private boolean isMuted = false;

    private MediaPlayerManager() {
    }

    public static void show(IEntity entity, MediaSet mediaSet) {
        String key = getKey(entity, mediaSet.getMediaGroupName());
        MediaPlayerWindow mediaPlayerWindow = SINGLETON.mediaPlayerWindowMap.get(key);
        if (mediaPlayerWindow == null) {
            mediaPlayerWindow = new MediaPlayerWindow(entity, mediaSet.getMediaGroupName());
            SINGLETON.mediaPlayerWindowMap.put(key, mediaPlayerWindow);
        }
        mediaPlayerWindow.addMedia(mediaSet);

//        String key = SINGLETON.getKey(entity, mediaSet);
//        MediaPlayer mediaPlayer = SINGLETON.mediaPlayerMap.get(key);
//        if (mediaPlayer == null) {
//            try {
//                mediaPlayer = new MediaPlayer(entity, mediaSet);
//            } catch (Exception e) {
//                Logger.getGlobal().log(Level.WARNING, null, e);
//            }
//            if (mediaPlayer != null) {
//                SINGLETON.mediaPlayerMap.put(key, mediaPlayer);
//            }
//        }
//        if (mediaPlayer != null) {
//            mediaPlayer.show();
//        }
    }

    public static void showAll(IEntity entity, String mediaGroupName) {
        MediaCollection mediaCollection = entity.getMediaCollection();
        for (MediaSet mediaSet : mediaCollection.getMediaGroup(mediaGroupName).getMediaSets()) {
            show(entity, mediaSet);
        }
    }

    public static void setMuteAll(boolean mute) {
        SINGLETON.isMuted = mute;
        for (MediaPlayerWindow mediaPlayerWindow : SINGLETON.mediaPlayerWindowMap.values()) {
            mediaPlayerWindow.setMute(mute);
        }
    }

    protected static void mediaPlayerWindowClosed(IEntity entity, String mediaGroup) {
        String key = getKey(entity, mediaGroup);
        SINGLETON.mediaPlayerWindowMap.remove(key);
    }

    protected static void mediaSetClosed(IEntity entity, MediaSet mediaSet) {
        String key = getKey(entity, mediaSet.getMediaGroupName());
        MediaPlayerWindow mediaPlayerWindow = SINGLETON.mediaPlayerWindowMap.get(key);
        if (mediaPlayerWindow != null) {
            mediaPlayerWindow.removeMedia(mediaSet);
        }
    }

    public static void removeMediaSet(IEntity entity, MediaSet mediaSet) {
        String key = getKey(entity, mediaSet.getMediaGroupName());
        MediaPlayerWindow mediaPlayerWindow = SINGLETON.mediaPlayerWindowMap.get(key);
        if (mediaPlayerWindow != null) {
            mediaPlayerWindow.removeMedia(mediaSet);
        }
//        SINGLETON.mediaPlayerMap.remove(SINGLETON.getKey(entity, mediaSet));
    }

    private static String getKey(IEntity entity, String mediaGroupName) {
        return entity.getEntityId().toString() + ":" + mediaGroupName;
    }
}
