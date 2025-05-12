package gov.mil.otc._3dvis.tena;

import OTC.OTCAM.AdminAction;
import OTC.OTCAM.EventFrom;
import OTC.OTCAM.EventOrigin;
import OTC.OTCAM.TypeOfEvent;
import TENA.Middleware.*;
import TENA.UnsignedShort;
import gov.mil.otc._3dvis.entity.RtcaCommand;
import gov.mil.otc._3dvis.entity.tena.LvcEntity;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Publisher {

    private final Thread workerThread = new Thread(new ProcessorThread(), "TENA - Publisher");
    private final Object mutex = new Object();
    private volatile boolean isRunning = true;
    private OTC.OTCAM.OtcamEventMessage.MessageSender otcamEventMessageSender = null;
    private TENA.LVC.Engagement.MunitionFire.MessageSender munitionFireMessageSender = null;
    private TENA.LVC.Engagement.MunitionDetonation.MessageSender munitionDetonationMessageSender = null;
    private final ConcurrentLinkedQueue<RtcaCommand> rtcaCommands = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<TENA.LVC.Engagement.MunitionFire.Message> munitionFireMessageQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<TENA.LVC.Engagement.MunitionDetonation.Message> munitionDetonationMessageQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger eventCounter = new AtomicInteger(1);

    private Publisher() {
    }

    public static Publisher createAndStart(Session session) {
        Publisher publisher = new Publisher();
        try {
            publisher.otcamEventMessageSender = OTC.OTCAM.OtcamEventMessage.MessageSender.create(session, CommunicationProperties.Reliable);
            publisher.munitionFireMessageSender = TENA.LVC.Engagement.MunitionFire.MessageSender.create(session, CommunicationProperties.Reliable);
            publisher.munitionDetonationMessageSender = TENA.LVC.Engagement.MunitionDetonation.MessageSender.create(session, CommunicationProperties.Reliable);
        } catch (CannotContactExecutionManager | ExecutionNotConfiguredForBestEffort | Timeout e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return null;
        }
        publisher.workerThread.start();
        return publisher;
    }

    public void shutdown() {
        isRunning = false;
        synchronized (mutex) {
            mutex.notifyAll();
        }
        try {
            workerThread.join();
        } catch (InterruptedException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            Thread.currentThread().interrupt();
        }
    }

    public void addRtcaCommand(RtcaCommand rtcaCommand) {
        if (rtcaCommand != null) {
            try {
                rtcaCommands.add(rtcaCommand);
                synchronized (mutex) {
                    mutex.notifyAll();
                }
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }

    public void addMessage(TENA.LVC.Engagement.MunitionFire.Message message) {
        munitionFireMessageQueue.add(message);
        synchronized (mutex) {
            mutex.notifyAll();
        }
    }

    public void addMessage(TENA.LVC.Engagement.MunitionDetonation.Message message) {
        munitionDetonationMessageQueue.add(message);
        synchronized (mutex) {
            mutex.notifyAll();
        }
    }

    private class ProcessorThread implements Runnable {

        @Override
        public void run() {
            while (isRunning) {
                try {
                    synchronized (mutex) {
                        mutex.wait();
                    }
                } catch (InterruptedException e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                    Thread.currentThread().interrupt();
                }

                processRtcaQueue();
                processMessageQueue();
            }
        }

        private void processRtcaQueue() {
            while (isRunning && !rtcaCommands.isEmpty()) {
                RtcaCommand rtcaCommand = rtcaCommands.remove();
                sendRtcaCommand(rtcaCommand);
            }
        }

        private void processMessageQueue() {
            while (isRunning && !munitionFireMessageQueue.isEmpty()) {
                try {
                    munitionFireMessageSender.send(munitionFireMessageQueue.remove());
                } catch (Timeout e) {
                    Logger.getGlobal().log(Level.SEVERE, "Error sending TENA munition fire message", e);
                }
            }

            while (isRunning && !munitionDetonationMessageQueue.isEmpty()) {
                try {
                    munitionDetonationMessageSender.send(munitionDetonationMessageQueue.remove());
                } catch (Timeout e) {
                    Logger.getGlobal().log(Level.SEVERE, "Error sending TENA munition detonation message", e);
                }
            }
        }

        private void sendRtcaCommand(RtcaCommand rtcaCommand) {
            if (otcamEventMessageSender == null || !(rtcaCommand.getEntity() instanceof LvcEntity)) {
                return;
            }

            try {
                OTC.OTCAM.OtcamEventMessage.Message message = OTC.OTCAM.OtcamEventMessage.Message.create();

                if (rtcaCommand.getType().equals(RtcaCommand.Type.RESURRECT)) {
                    message.set_typeEvent(TypeOfEvent.TypeOfEvent_Admin);
                    message.set_adminResult(OTC.OTCAM.AdminResult.LocalClass.create(AdminAction.AdminAction_Resurrect));
                } else {
                    OTC.OTCAM.RtcaAssessment.LocalClass rtcaAssessment = OTC.OTCAM.RtcaAssessment.LocalClass.create();
                    rtcaAssessment.set_rtcaKillCatastrophic(rtcaCommand.getType().equals(RtcaCommand.Type.KILL_CATASTROPHIC));
                    rtcaAssessment.set_rtcaKillMobility(rtcaCommand.getType().equals(RtcaCommand.Type.KILL_MOBILITY));
                    rtcaAssessment.set_rtcaKillFirepower(rtcaCommand.getType().equals(RtcaCommand.Type.KILL_FIREPOWER));
                    rtcaAssessment.set_rtcaKillCommunication(rtcaCommand.getType().equals(RtcaCommand.Type.KILL_COMMUNICATION));

                    OTC.OTCAM.TransitoryRtcaState.LocalClass transitoryRtcaState = OTC.OTCAM.TransitoryRtcaState.LocalClass.create();
                    transitoryRtcaState.set_rtcaHitNoKill(rtcaCommand.getType().equals(RtcaCommand.Type.HIT_NO_KILL));
                    transitoryRtcaState.set_rtcaMiss(rtcaCommand.getType().equals(RtcaCommand.Type.MISS));
                    transitoryRtcaState.set_rtcaSuppression(rtcaCommand.getType().equals(RtcaCommand.Type.SUPPRESSION));

                    OTC.OTCAM.RtcaResult.LocalClass rtcaResult = OTC.OTCAM.RtcaResult.LocalClass.create();
                    rtcaResult.set_rtcaAssessment(rtcaAssessment);
                    rtcaResult.set_transitoryRtcaState(transitoryRtcaState);
                    rtcaResult.set_rtcaDamagePercent(UnsignedShort.valueOf(rtcaCommand.getType().getDamagePercent()));

                    message.set_typeEvent(TypeOfEvent.TypeOfEvent_Engage);
                    message.set_rtcaResult(rtcaResult);
                }

                message.set_eventOrigin(EventOrigin.EventOrigin_Admin);
                message.set_eventFrom(EventFrom.EventFrom_Admin);
                message.set_eventId(TENA.LVC.EventID.LocalClass.create(
                        UnsignedShort.ONE,
                        UnsignedShort.ONE,
                        UnsignedShort.asUnsigned((short) eventCounter.getAndIncrement())));
                message.set_eventTime(TENA.Time.LocalClass.create());
                message.set_targetReference(TENA.LVC.EntityReference.LocalClass.create(
                        ((LvcEntity) rtcaCommand.getEntity()).getSdoPointer(),
                        rtcaCommand.getEntity().getEntityId().toTenaEntityId()));

                if (rtcaCommand.getEntity().getPosition() != null) {
                    message.set_targetTspi(rtcaCommand.getEntity().getCurrentTspi().toTenaTspi());
                } else {
                    message.set_targetTspi(TenaUtility.createUnknownTspi());
                }

                otcamEventMessageSender.send(message);
            } catch (NoLocalClassMethodsFactory | Timeout e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
    }
}
