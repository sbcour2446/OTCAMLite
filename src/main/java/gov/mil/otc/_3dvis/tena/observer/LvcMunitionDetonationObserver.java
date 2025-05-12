package gov.mil.otc._3dvis.tena.observer;

import TENA.LVC.Engagement.MunitionDetonation.Message;
import TENA.LVC.Engagement.MunitionDetonation.ReceivedMessage;
import TENA.LVC.Engagement.MunitionDetonation.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.event.MunitionDetonationEvent;
import gov.mil.otc._3dvis.tena.TenaUtility;
import gov.nasa.worldwind.geom.Position;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LVC Engagement MunitionDetonation Observer
 */
public class LvcMunitionDetonationObserver extends TENA.LVC.Engagement.MunitionDetonation.AbstractObserver {

    private static final LvcMunitionDetonationObserver SINGLETON = new LvcMunitionDetonationObserver();
    private final Subscription subscription = new Subscription();

    /**
     * The constructor.
     */
    private LvcMunitionDetonationObserver() {
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
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to TENA.LVC.Engagement.MunitionDetonation", e);
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
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to TENA.LVC.Engagement.MunitionDetonation", e);
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
        long eventTime = message.get_tspiAtImpact().get_time().get_nanosecondsSince1970() / TenaUtility.MILLI_NANO;
        EntityId eventId = new EntityId(message.get_eventID().get_siteID().intValue(),
                message.get_eventID().get_applicationID().intValue(), message.get_eventID().get_eventID().intValue());
        Position impactPosition = TenaUtility.convertTenaPosition(message.get_tspiAtImpact().get_position());
        EntityType munitionType = EntityType.fromTenaType(message.get_munitionType());

        if (impactPosition != null) {
            MunitionDetonationEvent munitionDetonationEvent = new MunitionDetonationEvent(eventTime, eventId,
                    impactPosition, munitionType, 10);//todo get radius
            EventManager.addEvent(munitionDetonationEvent);
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
