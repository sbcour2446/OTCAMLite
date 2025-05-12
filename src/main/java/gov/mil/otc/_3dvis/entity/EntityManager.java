package gov.mil.otc._3dvis.entity;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.staticentity.StaticEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.time.TimeManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityManager {

    private static final EntityManager SINGLETON = new EntityManager();
    private final Map<EntityId, IEntity> entityMap = new HashMap<>();
    private final List<IEntityListener> entityListeners = Collections.synchronizedList(new ArrayList<>());
    private final EntityFilter entityFilter = new EntityFilter(SettingsManager.getSettings().getEntityFilter());
    private final Map<Integer, IEntity> urnEntityMap = new ConcurrentHashMap<>();
    private final Timer timer = new Timer("EntityManager::timer");
    private boolean isRunning = false;

    private EntityManager() {
    }

    public static void start() {
        SINGLETON.doStart();
    }

    public static void shutdown() {
        SINGLETON.doShutdown();
    }

    public static void addEntityListener(IEntityListener entityListener) {
        SINGLETON.entityListeners.add(entityListener);
    }

    public static void removeEntityListener(IEntityListener entityListener) {
        SINGLETON.entityListeners.remove(entityListener);
    }

    public static void addEntity(IEntity entity) {
        SINGLETON.doAddEntity(entity, false);
    }

    public static void addEntity(IEntity entity, boolean logEntry) {
        SINGLETON.doAddEntity(entity, logEntry);
    }

    public static IEntity getEntity(EntityId entityId) {
        if (entityId == null) {
            return null;
        }
        synchronized (SINGLETON.entityMap) {
            return SINGLETON.entityMap.get(entityId);
        }
    }

    public static List<IEntity> getEntities() {
        List<IEntity> copy;
        synchronized (SINGLETON.entityMap) {
            copy = new ArrayList<>(SINGLETON.entityMap.values());
        }
        return copy;
    }

    public static IEntity getEntityFromUrn(int urn) {
        return SINGLETON.doGetEntityFromUrn(urn);
    }

    private IEntity doGetEntityFromUrn(int urn) {
        if (urn == 0) {
            return null;
        }
        IEntity entity = urnEntityMap.get(urn);
        if (entity != null) {
            return entity;
        }
        for (IEntity entity1 : EntityManager.getEntities()) {
            EntityDetail entityDetail = entity1.getLastEntityDetail();
            if (entityDetail != null && entityDetail.getUrn() == urn) {
                urnEntityMap.put(urn, entity1);
                return entity1;
            }
        }
        return null;
    }

    public static EntityFilter getEntityFilter() {
        return SINGLETON.entityFilter;
    }

    public static void updateDisplay() {
        SINGLETON.doUpdateDisplay();
    }

    public static void refreshStatus() {
        SINGLETON.doRefreshStatus();
    }

    public static void removeEntity(IEntity entity) {
        SINGLETON.doRemoveEntity(entity);
    }

    public static void removeAllEntities() {
        SINGLETON.doRemoveAllEntities();
    }

    private void doUpdateDisplay() {
        for (IEntity entity : getEntities()) {
            entity.resetIcon();
        }
    }

    private void doRefreshStatus() {
        for (IEntity entity : getEntities()) {
            entity.refreshStatus();
        }
    }

    private void doStart() {
        if (!isRunning) {
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    update();
                }
            }, 50, 50);
            isRunning = true;
        }
    }

    private void doShutdown() {
        if (isRunning) {
            timer.cancel();
            isRunning = false;
        }
    }

    private void doAddEntity(IEntity entity, boolean logEntry) {
        entity.update(TimeManager.getTime(), new EntityFilter(entityFilter));
        synchronized (entityMap) {
            entityMap.put(entity.getEntityId(), entity);
        }
        synchronized (entityListeners) {
            for (IEntityListener entityListener : entityListeners) {
                entityListener.onEntityAdded(entity);
            }
        }
        if (logEntry) {
            DatabaseLogger.addEntity(entity);
            DatabaseLogger.addEntityId(entity.getEntityId());
            if (entity instanceof StaticEntity) {
                DataManager.addEntityImage(entity.getEntityId(), entity.getIcon());
            }
        }
    }

    private void doRemoveEntity(IEntity entity) {
        synchronized (entityMap) {
            entityMap.remove(entity.getEntityId());
        }
        entity.dispose();
        synchronized (entityListeners) {
            for (IEntityListener entityListener : entityListeners) {
                entityListener.onEntityDisposed(entity);
            }
        }
        DataManager.removeEntity(entity);
    }

    private void doRemoveAllEntities() {
        synchronized (entityMap) {
            for (IEntity entity : entityMap.values()) {
                entity.dispose();
                synchronized (entityListeners) {
                    for (IEntityListener entityListener : entityListeners) {
                        entityListener.onEntityDisposed(entity);
                    }
                }
            }
            entityMap.clear();
        }
    }

    private void update() {
        long time = TimeManager.getTime();

        EntityFilter filter = new EntityFilter(entityFilter);
        entityFilter.resetFilterModified();

        synchronized (entityMap) {
            for (IEntity entity : entityMap.values()) {
                try {
                    if (entity.update(time, filter)) {
                        synchronized (entityListeners) {
                            for (IEntityListener entityListener : entityListeners) {
                                entityListener.onEntityUpdated(entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "EntityManager::entity.update:" + entity.getEntityId() +
                            " : " + entity.getName(), e);
                }
            }
        }
    }
}
