package gov.mil.otc._3dvis.tena;

import TENA.Middleware.Runtime;
import TENA.Middleware.*;
import TENA.UnsignedShort;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.RtcaCommand;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.tena.LvcEntity;
import gov.mil.otc._3dvis.tena.observer.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TenaController {

    private static final TenaController SINGLETON = new TenaController();
    private static final int CALLBACK_TIMEOUT = 1000000;
    private final List<ITenaConnectionListener> connectionListenerList = Collections.synchronizedList(new ArrayList<>());
    private Thread workerThread;
    private boolean isRunning = false;
    private Runtime runtime = null;
    private Execution execution = null;
    private Session session = null;
    private Publisher publisher;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    private TenaController() {
        TenaNativeLibraryLoader.loadTenaLibraries();
    }

    private boolean isSupported() {
        if (!TenaNativeLibraryLoader.isInitialized()) {
            return TenaNativeLibraryLoader.loadTenaLibraries();
        }
        return true;
    }

    public static void addConnectionListener(ITenaConnectionListener connectionListener) {
        SINGLETON.doAddConnectionListener(connectionListener);
    }

    private void doAddConnectionListener(ITenaConnectionListener connectionListener) {
        if (!connectionListenerList.contains(connectionListener)) {
            connectionListenerList.add(connectionListener);
        }
    }

    public static ConnectionState getConnectionState() {
        return SINGLETON.connectionState;
    }

    public static void connect(String applicationName, String listenEndpointHostname, int listenEndpointPort,
                               String emEndpointHostname, int emEndpointPort) {
        if (SINGLETON.isSupported()) {
            SINGLETON.doConnect(applicationName, listenEndpointHostname,
                    listenEndpointPort, emEndpointHostname, emEndpointPort);
        }
    }

    public static void disconnect() {
        if (SINGLETON.isSupported()) {
            SINGLETON.doDisconnect();
        }
    }

    public static void sendRtcaCommand(RtcaCommand rtcaCommand) {
        if (SINGLETON.isSupported() && SINGLETON.publisher != null) {
            SINGLETON.publisher.addRtcaCommand(rtcaCommand);
        }
    }

    public static void sendMessage(TENA.LVC.Engagement.MunitionFire.Message message) {
        if (SINGLETON.isSupported() && SINGLETON.publisher != null) {
            SINGLETON.publisher.addMessage(message);
        }
    }

    public static void sendMessage(TENA.LVC.Engagement.MunitionDetonation.Message message) {
        if (SINGLETON.isSupported() && SINGLETON.publisher != null) {
            SINGLETON.publisher.addMessage(message);
        }
    }

    private void doConnect(String applicationName, String listenEndpointHostname,
                           int listenEndpointPort, String emEndpointHostname, int emEndpointPort) {
        Endpoint listenEndpoint = new Endpoint(listenEndpointHostname, UnsignedShort.valueOf(listenEndpointPort));
        Endpoint emEndpoint = new Endpoint(emEndpointHostname, UnsignedShort.valueOf(emEndpointPort));

        if (isRunning) {
            return;
        }

        setConnectionState(ConnectionState.CONNECTING);

        new Thread(() -> {
            Logger.getGlobal().log(Level.INFO, "connecting to TENA");
            try {
                EndpointVector endpointVector = new EndpointVector();
                endpointVector.add(listenEndpoint);

                Configuration configuration = new Configuration();
                configuration.setApplicationName(applicationName);
                configuration.setListenEndpoints(endpointVector);

                runtime = Runtime.init(configuration);
                execution = runtime.joinExecution(emEndpoint);
                session = execution.createSession(applicationName);

                DataManager.startRealTimeDataSource();
                subscribe();
                publisher = Publisher.createAndStart(session);

                isRunning = true;
                workerThread = new Thread(() -> {
                    while (isRunning) {
                        session.evokeMultipleCallbacks(CALLBACK_TIMEOUT);
                    }
                });
                workerThread.start();
                setConnectionState(ConnectionState.CONNECTED);
                Logger.getGlobal().log(Level.INFO, "TENA connected");
            } catch (Exception e) {
                doDisconnect();
                setConnectionState(ConnectionState.FAILED);
                Logger.getGlobal().log(Level.SEVERE, "TENA connection failed", e);
            }
        }, "TENA Callback Thread").start();
    }

    private void doDisconnect() {
        setConnectionState(ConnectionState.DISCONNECTING);
        new Thread(() -> {
            Logger.getGlobal().log(Level.INFO, "disconnecting from TENA");

            isRunning = false;

            unsubscribe();

            if (workerThread != null) {
                try {
                    workerThread.join();
                } catch (InterruptedException e) {
                    Logger.getGlobal().log(Level.SEVERE, null, e);
                    Thread.currentThread().interrupt();
                }
            }

            if (publisher != null) {
                publisher.shutdown();
            }

            if (session != null) {
                session.releaseReference();
                session = null;
            }
            if (execution != null) {
                execution.releaseReference();
                execution = null;
            }
            if (runtime != null) {
                runtime.releaseReference();
                runtime = null;
            }

            deScopeEntities();
            DataManager.stopRealTimeDataSource();
            setConnectionState(ConnectionState.DISCONNECTED);
            Logger.getGlobal().log(Level.INFO, "TENA disconnected");
        }, "TENA Disconnect Thread").start();
    }

    private void deScopeEntities() {
        long endTime = System.currentTimeMillis();
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof LvcEntity) {
                entity.setRealtimeStop(endTime);
            }
        }
    }

    private void subscribe() {
        if (session != null) {
            LvcEngagementResultsObserver.subscribe(session);
            LvcEntityObserver.subscribe(session);
            LvcMunitionDetonationObserver.subscribe(session);
            LvcMunitionFireObserver.subscribe(session);
            OtcamEntityObserver.subscribe(session);
            OtcamEventObserver.subscribe(session);
            OtcamFusedEntityObserver.subscribe(session);
            DlmMessageObserver.subscribe(session);
        }
    }

    private void unsubscribe() {
        if (session != null) {
            LvcEngagementResultsObserver.unsubscribe(session);
            LvcEntityObserver.unsubscribe(session);
            LvcMunitionDetonationObserver.unsubscribe(session);
            LvcMunitionFireObserver.unsubscribe(session);
            OtcamEntityObserver.unsubscribe(session);
            OtcamEventObserver.unsubscribe(session);
            OtcamFusedEntityObserver.unsubscribe(session);
            DlmMessageObserver.unsubscribe(session);
        }
    }

    private void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
        for (ITenaConnectionListener connectionListener : connectionListenerList) {
            connectionListener.onStatusChange(connectionState);
        }
    }
}
