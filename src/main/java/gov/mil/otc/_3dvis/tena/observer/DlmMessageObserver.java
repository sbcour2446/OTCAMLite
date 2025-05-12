package gov.mil.otc._3dvis.tena.observer;

import OTC.OTCAM.DlmMessage.Message;
import OTC.OTCAM.DlmMessage.ReceivedMessage;
import OTC.OTCAM.DlmMessage.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;
import TENA.UnsignedByteVector;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.tena.LvcEntity;
import gov.mil.otc._3dvis.project.dlm.DlmEntity;
import gov.mil.otc._3dvis.project.dlm.message.DlmMessage;
import gov.mil.otc._3dvis.project.dlm.message.DlmMessageFactory;
import gov.mil.otc._3dvis.project.dlm.message.GenericDlmMessage;
import gov.mil.otc._3dvis.tena.TenaUtility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DlmMessageObserver extends OTC.OTCAM.DlmMessage.AbstractObserver {

    private static final DlmMessageObserver SINGLETON = new DlmMessageObserver();
    private final Subscription subscription = new Subscription();

    /**
     * The constructor.
     */
    private DlmMessageObserver() {
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
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to OTC.OTCAM.DlmMessage", e);
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
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to OTC.OTCAM.DlmMessage", e);
        }
    }

    @Override
    public void messageEvent(ReceivedMessage receivedMessage) {
        Message message = receivedMessage.getSentMessage();

        EntityId entityId = EntityId.fromTena(message.get_entityReference().get_entityID());
        IEntity entity = EntityManager.getEntity(entityId);
        DlmEntity dlmEntity = null;
        if (entity instanceof DlmEntity) {
            dlmEntity = (DlmEntity) entity;
        } else if (entity instanceof LvcEntity) {
            dlmEntity = new DlmEntity((LvcEntity) entity);
            EntityManager.addEntity(dlmEntity, true);
        }

        if (dlmEntity != null) {
            UnsignedByteVector unsignedByteVector = message.get_data();
            byte[] data = new byte[unsignedByteVector.size()];
            for (int i = 0; i < unsignedByteVector.size(); i++) {
                data[i] = unsignedByteVector.get(i).byteValue();
            }
            DlmMessage dlmMessage = DlmMessageFactory.createMessage(message.get_messageType(), data);
            if (dlmMessage != null) {
                long messageTimeOverride = message.is_timestamp_set() ?
                        message.get_timestamp().get_nanosecondsSince1970() / TenaUtility.MILLI_NANO : 0;
                if (dlmEntity.processMessage(dlmMessage, messageTimeOverride)) {
                    DatabaseLogger.addDlmMessage(new GenericDlmMessage(message.get_messageType(),
                                    message.get_time(), data, messageTimeOverride, message.is_timestamp_set()),
                            entity.getEntityId(), DataManager.getOrCreateRealTimeDataSource().getId());
                }
            }
        }

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
