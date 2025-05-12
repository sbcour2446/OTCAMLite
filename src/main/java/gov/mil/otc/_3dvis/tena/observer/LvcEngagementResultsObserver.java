package gov.mil.otc._3dvis.tena.observer;

import TENA.LVC.Engagement.Results.Message;
import TENA.LVC.Engagement.Results.ReceivedMessage;
import TENA.LVC.Engagement.Results.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.tena.TenaUtility;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LVC Engagement Results Observer
 */
public class LvcEngagementResultsObserver extends TENA.LVC.Engagement.Results.AbstractObserver {

    private static final LvcEngagementResultsObserver SINGLETON = new LvcEngagementResultsObserver();
    private final Subscription subscription = new Subscription();

    /**
     * The constructor.
     */
    private LvcEngagementResultsObserver() {
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
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to TENA.LVC.Engagement.Results", e);
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
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to TENA.LVC.Engagement.Results", e);
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
        long eventTime = message.get_timeOfAssessment().get_nanosecondsSince1970() / TenaUtility.MILLI_NANO;

        EntityId shooterId = null;
        if (message.get_shooterReference().is_entityReference_set()) {
            shooterId = EntityId.fromTena(message.get_shooterReference().get_entityReference().get_entityID());
        }

        EntityId targetId = null;
        if (message.get_target().is_entityReference_set()) {
            targetId = EntityId.fromTena(message.get_target().get_entityReference().get_entityID());
        }

        String description = "";
        if (message.is_notes_set()) {
            description = message.get_notes();
        }

        //todo create event

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
