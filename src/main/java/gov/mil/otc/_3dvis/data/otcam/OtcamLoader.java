package gov.mil.otc._3dvis.data.otcam;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.event.MunitionFireEvent;
import gov.mil.otc._3dvis.event.otcam.OtcamEvent;
import gov.mil.otc._3dvis.project.dlm.DlmPlaybackEntity;
import gov.mil.otc._3dvis.project.dlm.IDlmEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OtcamLoader {

    private static final ConcurrentMap<String, OtcamUtility> loadingList = new ConcurrentHashMap<>();

    public static boolean loadAsync(String filename) {
        if (loadingList.containsKey(filename)) {
            return false;
        }

        OtcamUtility otcamUtility = new OtcamUtility(filename);
        otcamUtility.startLoadAsync();
        loadingList.put(filename, otcamUtility);

        return true;
    }

    public static void loadComplete(String filename) {
        loadingList.remove(filename);
    }

    public static void cancelLoad(String filename) {
        OtcamUtility otcamUtility = loadingList.remove(filename);
        if (otcamUtility != null) {
            otcamUtility.cancelLoad();
        }
    }

    public static void cancelAll() {
        for (OtcamUtility otcamUtility : loadingList.values()) {
            otcamUtility.cancelLoad();
        }
        loadingList.clear();
    }
}
