package gov.mil.otc._3dvis.tir;

import gov.mil.otc._3dvis.datamodel.TimedFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TirManager {

    private static TirManager instance = null;

    public static void addTir(TimedFile timedFile) {
        if (instance == null) {
            instance = new TirManager();
        }
        instance.tirList.add(timedFile);
    }

    public static List<TimedFile> getTirList() {
        if (instance == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(instance.tirList);
    }

    public static void removeTir(TimedFile timedFile) {
        if (instance != null) {
            instance.tirList.remove(timedFile);
        }
    }

    public static void removeAllTirs() {
        if (instance != null) {
            instance.tirList.clear();
        }
    }

    private final List<TimedFile> tirList = new ArrayList<>();

    private TirManager() {
    }
}
