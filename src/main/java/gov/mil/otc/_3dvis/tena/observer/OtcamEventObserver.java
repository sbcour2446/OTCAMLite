package gov.mil.otc._3dvis.tena.observer;

import OTC.OTCAM.OtcamEventMessage.Message;
import OTC.OTCAM.OtcamEventMessage.ReceivedMessage;
import OTC.OTCAM.OtcamEventMessage.Subscription;
import TENA.Middleware.NetworkError;
import TENA.Middleware.Session;
import TENA.Middleware.Timeout;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.datamodel.RtcaState;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.event.otcam.*;
import gov.mil.otc._3dvis.tena.TenaUtility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OtcamEventObserver extends OTC.OTCAM.OtcamEventMessage.AbstractObserver {

    private static final OtcamEventObserver SINGLETON = new OtcamEventObserver();
    private final Subscription subscription = new Subscription();

    /**
     * The constructor.
     */
    private OtcamEventObserver() {
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
            Logger.getGlobal().log(Level.SEVERE, "Could not subscribe to OTC.OTCAM.OtcamEventMessage", e);
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
            Logger.getGlobal().log(Level.SEVERE, "Could not unsubscribe to OTC.OTCAM.OtcamEventMessage", e);
        }
    }

    @Override
    public void messageEvent(ReceivedMessage receivedMessage) {
        Message message = receivedMessage.getSentMessage();

        EntityId eventId = new EntityId(message.get_eventId().get_siteID().intValue(),
                message.get_eventId().get_applicationID().intValue(),
                message.get_eventId().get_eventID().intValue());
        long timestamp = message.get_eventTime().get_nanosecondsSince1970() / TenaUtility.MILLI_NANO;
        EventType eventType = EventType.getEnum(message.get_typeEvent().swigValue());
        EventOrigin eventOrigin = EventOrigin.getEnum(message.get_eventOrigin().swigValue());
        EventFrom eventFrom = EventFrom.getEnum(message.get_eventFrom().swigValue());

        EntityId targetId = EntityId.fromTena(message.get_targetReference().get_entityID());

        AdminAction adminAction = null;
        if (message.is_adminResult_set()) {
            adminAction = AdminAction.getEnum(message.get_adminResult().get_adminAction().swigValue());
        }

        EntityId shooterId = null;
        EntityType munitionType = null;
        String munitionName = "";
        boolean isKillCatastrophic = false;
        boolean isKillFirepower = false;
        boolean isKillMobility = false;
        boolean isKillCommunication = false;
        boolean isSuppressed = false;
        boolean isHit = false;
        boolean isMissed = false;
        int damagePercent = 0;
        if (message.is_rtcaResult_set()) {
            OTC.OTCAM.RtcaResult.ImmutableLocalClass rtcaResult = message.get_rtcaResult();

            if (rtcaResult.is_shooterReference_set()) {
                shooterId = EntityId.fromTena(rtcaResult.get_shooterReference().get_entityID());
            }

            if (rtcaResult.is_munitionType_set()) {
                munitionType = EntityType.fromTenaType(rtcaResult.get_munitionType());
            } else if (rtcaResult.is_munitionName_set()) {
                munitionName = rtcaResult.get_munitionName();
            }

            if (rtcaResult.is_rtcaAssessment_set()) {
                isKillCatastrophic = rtcaResult.get_rtcaAssessment().get_rtcaKillCatastrophic();
                isKillFirepower = rtcaResult.get_rtcaAssessment().get_rtcaKillFirepower();
                isKillMobility = rtcaResult.get_rtcaAssessment().get_rtcaKillMobility();
                isKillCommunication = rtcaResult.get_rtcaAssessment().get_rtcaKillCommunication();
            }

            if (rtcaResult.is_transitoryRtcaState_set()) {
                isSuppressed = rtcaResult.get_transitoryRtcaState().get_rtcaSuppression();
                isHit = rtcaResult.get_transitoryRtcaState().get_rtcaHitNoKill();
                isMissed = rtcaResult.get_transitoryRtcaState().get_rtcaMiss();
            }

            if (rtcaResult.is_rtcaDamagePercent_set()) {
                damagePercent = rtcaResult.get_rtcaDamagePercent().intValue();
            }
        }

        RtcaState rtcaState = new RtcaState(isKillCatastrophic, isKillFirepower, isKillMobility,
                isKillCommunication, isSuppressed, false, isHit, isMissed, "", damagePercent);

        OtcamEvent otcamEvent = new OtcamEvent(timestamp, eventId, eventType, eventOrigin, eventFrom, adminAction,
                targetId, shooterId, munitionType, munitionName, rtcaState);

        EventManager.addEvent(otcamEvent);
        DatabaseLogger.addEvent(otcamEvent, DataManager.getOrCreateRealTimeDataSource().getId());

        IEntity target = EntityManager.getEntity(targetId);
        if (target != null) {
            target.addEvent(otcamEvent);
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
