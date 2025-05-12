package gov.mil.otc._3dvis.data;

import gov.mil.otc._3dvis.data.database.Database;
import gov.mil.otc._3dvis.data.oadms.WdlReading;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityScope;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.datamodel.aircraft.TspiExtendedData;
import gov.mil.otc._3dvis.datamodel.aircraft.UasPayloadData;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.event.MunitionDetonationEvent;
import gov.mil.otc._3dvis.event.MunitionFireEvent;
import gov.mil.otc._3dvis.event.otcam.OtcamEvent;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.project.blackhawk.CcmEvent;
import gov.mil.otc._3dvis.project.blackhawk.FlightData;
import gov.mil.otc._3dvis.project.dlm.message.GenericDlmMessage;
import gov.mil.otc._3dvis.project.nbcrv.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseLogger {

    private static final DatabaseLogger SINGLETON = new DatabaseLogger();
    private static final long UPDATE_RATE = 5000000000L; // 5 seconds
    private final Object shutdownMutex = new Object();
    private Thread workerThread = null;
    private Database database = null;
    private boolean loggingEnabled = false;

    private DatabaseLogger() {
    }

    /**
     * Creates an instance of the DatabaseLogger and starts the processing thread.
     *
     * @return The DatabaseLogger object.
     */
    protected static synchronized boolean createAndStart(Database database) {
        return SINGLETON.doCreateAndStart(database);
    }

    /**
     * Shutdown database logger and commits all data in queues.
     */
    protected static synchronized void shutdown() {
        SINGLETON.doShutdown();
    }

    public static void addEntity(IEntity entity) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(entity);
        }
    }

    public static void addEntityId(EntityId entityId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(entityId);
        }
    }

    public static void addEntityScope(EntityScope entityScope, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(entityScope, entityId, sourceId);
        }
    }

    public static void addEntityDetail(EntityDetail entityDetail, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(entityDetail, entityId, sourceId);
        }
    }

    public static void addTspiData(TspiData tspiData, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(tspiData, entityId, sourceId);
        }
    }

    public static void addTspiExtendedData(TspiExtendedData tspiExtendedData, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(tspiExtendedData, entityId, sourceId);
        }
    }

    public static void addUasPayloadData(UasPayloadData uasPayloadData, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(uasPayloadData, entityId, sourceId);
        }
    }

    public static void addFlightData(FlightData flightData, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(flightData, entityId, sourceId);
        }
    }

    public static void addEvent(Event event, int sourceId) {
        if (!SINGLETON.loggingEnabled) {
            return;
        }
        if (event instanceof OtcamEvent) {
            SINGLETON.database.add((OtcamEvent) event, sourceId);
        } else if (event instanceof MunitionFireEvent) {
            SINGLETON.database.add((MunitionFireEvent) event, sourceId);
        } else if (event instanceof MunitionDetonationEvent) {
            SINGLETON.database.add((MunitionDetonationEvent) event, sourceId);
        }
    }

    public static void addEvent(Event event, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            if (event instanceof CcmEvent) {
                SINGLETON.database.add(entityId, (CcmEvent) event, sourceId);
            }
        }
    }

    public static DataSource addMedia(MediaFile mediaFile, EntityId entityId) {
        if (SINGLETON.loggingEnabled) {
            return SINGLETON.database.add(mediaFile, entityId);
        }
        return null;
    }

    public static DataSource addTimedFile(TimedFile timedFile, EntityId entityId) {
        if (SINGLETON.loggingEnabled) {
            return SINGLETON.database.add(timedFile, entityId);
        }
        return null;
    }

    public static void addDlmMessage(GenericDlmMessage genericDlmMessage, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(genericDlmMessage, entityId, sourceId);
        }
    }

    public static void addC2Message(C2MessageEvent c2MessageEvent, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(c2MessageEvent, entityId, sourceId);
        }
    }

    public static void addCrewMissionData(String mission, int flightNumber, int tailNumber, String pin,
                                          String seat, String role, long startTime, long endTime, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.addCrewMissionData(mission, flightNumber, tailNumber, pin, seat, role,
                    startTime, endTime, sourceId);
        }
    }

    public static void addSurveyData(String mission, int tailNumber, String pin, String seat,
                                     String role, String question, String answer, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.addSurveyData(mission, tailNumber, pin, seat, role, question,
                    answer, sourceId);
        }
    }

    public static void addNbcrvStates(List<NbcrvState> nbcrvStates, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.addNbcrvStates(nbcrvStates, entityId, sourceId);
        }
    }

    public static void addRadNucStates(List<RadNucState> radNucStates, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.addRadNucStates(radNucStates, entityId, sourceId);
        }
    }

    public static void addNbcrvDevice(Device device, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(device, entityId, sourceId);
        }
    }

    public static void addNbcrvEvent(NbcrvDetection nbcrvDetection, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(nbcrvDetection, entityId, sourceId);
        }
    }

    public static void addNbcrvEvents(List<NbcrvDetection> nbcrvDetections, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.add(nbcrvDetections, entityId, sourceId);
        }
    }

    public static void addWdlReadings(List<WdlReading> wdlReadings, EntityId entityId, int sourceId) {
        if (SINGLETON.loggingEnabled) {
            SINGLETON.database.addWdlReadings(wdlReadings, entityId, sourceId);
        }
    }

    public static void saveEntity(NbcrvEntity entity) {
        if (SINGLETON.loggingEnabled) {
            if (SINGLETON.database.getEntityIds().contains(entity.getEntityId())) {
                SINGLETON.database.add(entity);
                SINGLETON.database.add(entity.getEntityId());
            }

        }
    }

    private boolean doCreateAndStart(Database database) {
        if (!loggingEnabled && database != null) {
            this.database = database;
            workerThread = new Thread(new ProcessingThread(), "DatabaseLogger");
            workerThread.start();
            loggingEnabled = true;
        }
        return loggingEnabled;
    }

    private void doShutdown() {
        if (loggingEnabled) {
            Logger.getGlobal().log(Level.INFO, "shutting down DatabaseLogger");
            loggingEnabled = false;
            synchronized (shutdownMutex) {
                shutdownMutex.notifyAll();
            }
            try {
                workerThread.join();
                Logger.getGlobal().log(Level.INFO, "DatabaseLogger shutdown");
            } catch (InterruptedException e) {
                Logger.getGlobal().log(Level.WARNING, "", e);
                Thread.currentThread().interrupt();
            }
            workerThread = null;
            database = null;
        }
    }

    private class ProcessingThread implements Runnable {

        @Override
        public void run() {
            if (database == null) {
                return;
            }

            long lastUpdateTime = System.nanoTime();
            while (loggingEnabled) {
                if (System.nanoTime() - lastUpdateTime > UPDATE_RATE) {
                    lastUpdateTime = System.nanoTime();

                    database.insertData();
                } else {
                    try {
                        synchronized (shutdownMutex) {
                            shutdownMutex.wait(100);
                        }
                    } catch (InterruptedException e) {
                        Logger.getGlobal().log(Level.WARNING, "", e);
                        Thread.currentThread().interrupt();
                    }
                }
            }

            database.insertData();
        }
    }
}
