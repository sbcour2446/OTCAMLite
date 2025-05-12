package gov.mil.otc._3dvis.entity.fused;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.nasa.worldwind.layers.RenderableLayer;

import javax.swing.*;
import java.util.concurrent.ConcurrentHashMap;

public class FusedEntityManager {

    private static final FusedEntityManager SINGLETON = new FusedEntityManager();
    private final ConcurrentHashMap<EntityId, FusedEntity> entities = new ConcurrentHashMap<>();
    private final RenderableLayer layer = new RenderableLayer();
    private Timer timer;
    private boolean isRunning = false;

    private FusedEntityManager() {
    }

    public static void add(FusedEntity fusedEntity) {
        SINGLETON.doAdd(fusedEntity);
    }

    public static void remove(EntityId entityId) {
        SINGLETON.doRemove(entityId);
    }

    public static void remove(FusedEntity fusedEntity) {
        SINGLETON.doRemove(fusedEntity.getPrimaryEntityId());
    }

    private void doAdd(FusedEntity fusedEntity) {
        if (!isRunning) {
            start();
        }
        synchronized (entities) {
            entities.put(fusedEntity.getPrimaryEntityId(), fusedEntity);
        }
    }

    private void doRemove(EntityId entityId) {
        synchronized (entities) {
            entities.remove(entityId);
        }
        if (entities.isEmpty()) {
            shutdown();
        }
    }

    private void start() {
        if (!isRunning) {
            layer.setName("FusedEntityManager");
            layer.setPickEnabled(true);
            WWController.addLayer(layer);
            timer = new Timer(100, e -> update());
            timer.start();
            isRunning = true;
        }
    }

    private void shutdown() {
        if (isRunning) {
            WWController.removeLayer(layer);
            timer.stop();
            isRunning = false;
        }
    }

    private void update() {
        for (FusedEntity entity : entities.values()) {
            entity.update(layer);
        }
    }
}
