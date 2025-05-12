package gov.mil.otc._3dvis.overlay;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.nasa.worldwind.layers.RenderableLayer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class OverlayManager {

    private static final OverlayManager SINGLETON = new OverlayManager();

    public static void initialize() {
        SINGLETON.doInitialize();
    }

    public static void addOverlay(File file) {
        SINGLETON.doAddOverlay(file);
    }

    public static void removeOverlay(File file) {
        SINGLETON.doRemoveOverlay(file);
    }

    private final Map<String, RenderableLayer> layerMap = new HashMap<>();

    private OverlayManager() {
    }

    private void doInitialize() {
        for (File overlay : SettingsManager.getPreferences().getOverlayList()){
            doAddOverlay(overlay);
        }
    }

    private void doAddOverlay(File file) {
        if (layerMap.containsKey(file.getAbsolutePath())) {
            return;
        }

        RenderableLayer layer = null;
        if (file.getName().endsWith(".kml") || file.getName().endsWith(".kmz")) {
            layer = KmlLoader.getRenderable(file);
        } else if (file.getName().endsWith(".ovl")) {
            layer = OvlLoader.processOvl(file);
        }
        if (layer != null) {
            WWController.addLayer(layer);
            SettingsManager.getPreferences().addOverlay(file);
            layerMap.put(file.getAbsolutePath(), layer);
        }
    }

    private void doRemoveOverlay(File file) {
        RenderableLayer layer = layerMap.remove(file.getAbsolutePath());
        if (layer != null) {
            layer.removeAllRenderables();
            SettingsManager.getPreferences().removeOverlay(file);
            WWController.addLayer(layer);
        }
    }
}
