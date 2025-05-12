package gov.mil.otc._3dvis.tena.observer;

import OTC.OTCAM.VmfMessage.Message;
import OTC.OTCAM.VmfMessage.ReceivedMessage;
import OTC.OTCAM.VmfMessage.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;

import java.util.logging.Level;
import java.util.logging.Logger;

public class VmfMessageObserver extends OTC.OTCAM.VmfMessage.AbstractObserver {

    private static final VmfMessageObserver SINGLETON = new VmfMessageObserver();
    private final Subscription subscription = new Subscription();

    /**
     * The constructor.
     */
    private VmfMessageObserver() {
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
            Subscription.subscribe(session, subscription, true);
        } catch (Timeout | NetworkError e) {
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to OTC.OTCAM.VmfMessage", e);
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
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to OTC.OTCAM.VmfMessage", e);
        }
    }

    @Override
    public void messageEvent(ReceivedMessage receivedMessage) {
        Message message = receivedMessage.getSentMessage();
        message.releaseReference();
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
