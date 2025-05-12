package gov.mil.otc._3dvis.tena.observer;

import TENA.LVC.Entity.Proxy;
import TENA.LVC.Entity.PublicationState;
import TENA.LVC.Entity.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.tena.LvcEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LvcEntityObserver extends TENA.LVC.Entity.AbstractObserver {

    private static final LvcEntityObserver SINGLETON = new LvcEntityObserver();
    private final Subscription subscription = new Subscription();
    private final List<String> lvcOnlyList = Collections.synchronizedList(new ArrayList<>());

    /**
     * The constructor.
     */
    private LvcEntityObserver() {
    }

    /**
     * Subscribes to the message.
     *
     * @param session The TENA session.
     */
    public static void subscribe(Session session) {
        SINGLETON.doSubscribe(session);
    }

    /**
     * Subscribes to the message.
     *
     * @param session The TENA session.
     */
    private void doSubscribe(Session session) {
        try {
            subscription.addObserver(this);
            Subscription.subscribe(session, subscription, false);
        } catch (Timeout | NetworkError e) {
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to TENA.LVC.Entity", e);
        }
    }

    /**
     * Removes subscription for this message.
     *
     * @param session TENA session
     */
    public static void unsubscribe(Session session) {
        SINGLETON.doUnsubscribe(session);
    }

    /**
     * Removes subscription for this message.
     *
     * @param session TENA session
     */
    private void doUnsubscribe(Session session) {
        try {
            subscription.removeObserver(this);
            Subscription.unsubscribe(session);
        } catch (Timeout | NetworkError e) {
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to TENA.LVC.Entity", e);
        }
    }

    @Override
    public void discoveryEvent(Proxy proxy, PublicationState publicationState) {
        try {
            OTC.OTCAM.OtcamEntity.SDOpointer.downCast(proxy.getSDOpointer());
        } catch (Exception e) {
            if (!lvcOnlyList.contains(proxy.getSDOpointer().getObjectID().toHex())) {
                lvcOnlyList.add(proxy.getSDOpointer().getObjectID().toHex());
            }
            LvcEntity.processDiscoveryEvent(publicationState, proxy.getSDOpointer());
        }
        proxy.releaseReference();
    }

    @Override
    public void stateChangeEvent(Proxy proxy, PublicationState publicationState) {
        if (lvcOnlyList.contains(proxy.getSDOpointer().getObjectID().toHex())) {
            EntityId entityId = EntityId.fromTena(publicationState.get_entityID());
            IEntity entity = EntityManager.getEntity(entityId);
            if (entity instanceof LvcEntity) {
                LvcEntity lvcEntity = (LvcEntity) entity;
                lvcEntity.processPublicationState(publicationState);
            }
        }
        proxy.releaseReference();
    }

    @Override
    public void destructionEvent(Proxy proxy, PublicationState publicationState) {
        EntityId entityId = EntityId.fromTena(publicationState.get_entityID());
        IEntity entity = EntityManager.getEntity(entityId);
        if (entity != null) {
            entity.setRealtimeStop(System.currentTimeMillis());
        }
        proxy.releaseReference();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
