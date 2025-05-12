package gov.mil.otc._3dvis.tena.observer;

import OTC.OTCAM.OtcamFusedEntity.Proxy;
import OTC.OTCAM.OtcamFusedEntity.PublicationState;
import OTC.OTCAM.OtcamFusedEntity.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.fused.FusedEntity;
import gov.mil.otc._3dvis.entity.fused.FusedEntityManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OtcamFusedEntityObserver extends OTC.OTCAM.OtcamFusedEntity.AbstractObserver {

    private static final OtcamFusedEntityObserver SINGLETON = new OtcamFusedEntityObserver();
    private final Subscription subscription = new Subscription();

    /**
     * The constructor.
     */
    private OtcamFusedEntityObserver() {
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
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to OTC.OTCAM.OtcamFusedEntity", e);
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
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to OTC.OTCAM.OtcamFusedEntity", e);
        }
    }

    @Override
    public void discoveryEvent(Proxy proxy, PublicationState publicationState) {
        IEntity primaryEntity = EntityManager.getEntity(EntityId.fromTena(publicationState.get_fusedEntityId()));
        if (primaryEntity != null) {
            FusedEntity fusedEntity = new FusedEntity(primaryEntity);
            for (TENA.LVC.EntityReference.ImmutableLocalClass entityReference : publicationState.get_entitySourceReference()) {
                IEntity entity = EntityManager.getEntity(EntityId.fromTena(entityReference.get_entityID()));
                fusedEntity.fuseEntity(entity);
            }
            FusedEntityManager.add(fusedEntity);
        }
        proxy.releaseReference();
    }

    @Override
    public void stateChangeEvent(Proxy proxy, PublicationState publicationState) {
        proxy.releaseReference();
    }

    @Override
    public void destructionEvent(Proxy proxy, PublicationState publicationState) {
        FusedEntityManager.remove(EntityId.fromTena(publicationState.get_fusedEntityId()));
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
