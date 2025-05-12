package gov.mil.otc._3dvis.tena.observer;

import OTC.OTCAM.OtcamEntity.Proxy;
import OTC.OTCAM.OtcamEntity.PublicationState;
import OTC.OTCAM.OtcamEntity.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.tena.OtcamEntity;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OtcamEntityObserver extends OTC.OTCAM.OtcamEntity.AbstractObserver {

    private static final OtcamEntityObserver SINGLETON = new OtcamEntityObserver();
    private final Subscription subscription = new Subscription();

    /**
     * The constructor.
     */
    private OtcamEntityObserver() {
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
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to OTC.OTCAM.OtcamEntity", e);
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
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to OTC.OTCAM.OtcamEntity", e);
        }
    }

    @Override
    public void discoveryEvent(Proxy proxy, PublicationState publicationState) {
        OtcamEntity.processDiscoveryEvent(publicationState, proxy.getSDOpointer());
        proxy.releaseReference();
    }

    @Override
    public void stateChangeEvent(Proxy proxy, PublicationState publicationState) {
        EntityId entityId = EntityId.fromTena(publicationState.get_entityID());
        IEntity entity = EntityManager.getEntity(entityId);
        if (entity instanceof OtcamEntity) {
            OtcamEntity otcamEntity = (OtcamEntity) entity;
            otcamEntity.processOtcamPublicationState(publicationState);
        }
        proxy.releaseReference();
    }

    @Override
    public void destructionEvent(Proxy proxy, PublicationState publicationState) {
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
