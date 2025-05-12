package gov.mil.otc._3dvis;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.view.orbit.OrbitView;

import javax.swing.*;

public class WWController {

    private static final WWController SINGLETON = new WWController();
    private final WorldWindowGLJPanel worldWindowGLJPanel = new WorldWindowGLJPanel();
    private final Timer redrawTimer = new Timer(50, e -> worldWindowGLJPanel.redraw());
    private boolean initialized = false;

    private WWController() {
    }

    public static void initialize() {
        SINGLETON.doInitialize();
    }

    public static void shutdown() {
        SINGLETON.doShutdown();
    }

    public static WorldWindowGLJPanel getWorldWindowPanel() {
        return SINGLETON.worldWindowGLJPanel;
    }

    public static OrbitView getView() {
        return SINGLETON.worldWindowGLJPanel.getView() instanceof OrbitView ? (OrbitView) SINGLETON.worldWindowGLJPanel.getView() : null;
    }

    public static Globe getGlobe() {
        OrbitView orbitView = getView();
        return orbitView != null ? orbitView.getGlobe() : null;
    }

    public static void addLayer(Layer layer) {
        SINGLETON.worldWindowGLJPanel.getModel().getLayers().add(layer);
    }

    public static void removeLayer(Layer layer) {
        SINGLETON.worldWindowGLJPanel.getModel().getLayers().remove(layer);
    }

    public static boolean containsLayer(Layer layer) {
        return SINGLETON.worldWindowGLJPanel.getModel().getLayers().contains(layer);
    }

    private void doInitialize() {
        if (!initialized) {
            initialized = true;
            Model model = new BasicModel();
            Earth earth = new Earth();
            model.setGlobe(earth);
            worldWindowGLJPanel.setModel(model);
            redrawTimer.start();
        }
    }

    private void doShutdown() {
        redrawTimer.stop();
    }
}
