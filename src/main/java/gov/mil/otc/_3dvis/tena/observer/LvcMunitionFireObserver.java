package gov.mil.otc._3dvis.tena.observer;

import TENA.LVC.Engagement.MunitionFire.Message;
import TENA.LVC.Engagement.MunitionFire.ReceivedMessage;
import TENA.LVC.Engagement.MunitionFire.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.event.MunitionFireEvent;
import gov.mil.otc._3dvis.tena.TenaUtility;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LVC Engagement MunitionFire Observer
 */
public class LvcMunitionFireObserver extends TENA.LVC.Engagement.MunitionFire.AbstractObserver {

    private static final LvcMunitionFireObserver SINGLETON = new LvcMunitionFireObserver();
    private final Subscription subscription = new Subscription();

    /**
     * The constructor.
     */
    private LvcMunitionFireObserver() {
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
            Subscription.subscribe(session, subscription);
        } catch (Timeout | NetworkError e) {
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to TENA.LVC.Engagement.MunitionFire", e);
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
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to TENA.LVC.Engagement.MunitionFire", e);
        }
    }

    /**
     * Callback for received messages.
     *
     * @param receivedMessage The received message.
     */
    @Override
    public void messageEvent(ReceivedMessage receivedMessage) {
        Message message = receivedMessage.getSentMessage();
        long eventTime = message.get_tspiAtFire().get_time().get_nanosecondsSince1970() / TenaUtility.MILLI_NANO;

        EntityId eventId = new EntityId(message.get_eventID().get_siteID().intValue(),
                message.get_eventID().get_applicationID().intValue(), message.get_eventID().get_eventID().intValue());

        EntityId shooterId = null;
        if (message.get_shooterReference().is_entityReference_set()) {
            shooterId = EntityId.fromTena(message.get_shooterReference().get_entityReference().get_entityID());
        }

        EntityId targetId = null;
        if (message.get_target().is_entityReference_set()) {
            targetId = EntityId.fromTena(message.get_target().get_entityReference().get_entityID());
        }

        EntityType munitionType = EntityType.fromTenaType(message.get_munitionType());

        int quantity = message.get_quantityFired().intValue();

        if (shooterId != null) {
            MunitionFireEvent munitionFireEvent = new MunitionFireEvent(eventTime, eventId, shooterId, targetId,
                    munitionType, quantity);
            EventManager.addEvent(munitionFireEvent);

            IEntity shooter = EntityManager.getEntity(shooterId);
            if (shooter != null) {
                shooter.addEvent(munitionFireEvent);
            }
        }

        receivedMessage.getSentMessage().releaseReference();
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
