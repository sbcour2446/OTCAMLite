package gov.mil.otc._3dvis.data.database.table.nbcrv;

import gov.mil.otc._3dvis.data.database.DatabaseObject;
import gov.mil.otc._3dvis.data.database.DatabaseObjectPair;
import gov.mil.otc._3dvis.data.oadms.WdlReading;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.project.nbcrv.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvDataProxy {

    private NbcrvStateTable nbcrvStateTable = null;
    private NbcrvRadNucStateTable nbcrvRadNucStateTable = null;
    private DeviceTable deviceTable = null;
    private DeviceStateTable deviceStateTable = null;
    private NbcrvEventTable nbcrvEventTable = null;
    private NbcrvDetectionTable nbcrvDetectionTable = null;
    private NbcrvEventPositionsTable nbcrvEventPositionsTable = null;
    private GenericDeviceStateTable genericDeviceStateTable = null;
    private WdlReadingTable wdlReadingTable = null;

    public void addNbcrvStates(Connection connection, List<NbcrvState> nbcrvStates, EntityId entityId, int sourceId) {
        initialize(connection);
        for (NbcrvState nbcrvState : nbcrvStates) {
            nbcrvStateTable.add(new DatabaseObject<>(nbcrvState, entityId, sourceId));
        }
        nbcrvStateTable.insertToDatabase(connection);
    }

    public void addRadNucStates(Connection connection, List<RadNucState> radNucStates, EntityId entityId, int sourceId) {
        initialize(connection);
        for (RadNucState radNucState : radNucStates) {
            nbcrvRadNucStateTable.add(new DatabaseObject<>(radNucState, entityId, sourceId));
        }
        nbcrvRadNucStateTable.insertToDatabase(connection);
    }

    public void addDevice(Connection connection, Device device, EntityId entityId, int sourceId) {
        initialize(connection);
        deviceTable.add(new DatabaseObject<>(device, entityId, sourceId));
        for (DeviceState deviceState : device.getDeviceStates()) {
            deviceStateTable.add(new DatabaseObject<>(new DatabaseObjectPair<>(device, deviceState), entityId, sourceId));
        }
        deviceTable.insertToDatabase(connection);
        deviceStateTable.insertToDatabase(connection);
    }

    public void addEvent(Connection connection, NbcrvDetection nbcrvDetection, EntityId entityId, int sourceId) {
        initialize(connection);
        int eventId = nbcrvEventTable.insert(connection, nbcrvDetection, entityId, sourceId);
        nbcrvEventPositionsTable.insert(connection, nbcrvDetection, eventId);
        try {
            connection.commit();
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Error inserting to NBCRV Event", e);
        }
    }

    public void addEvents(Connection connection, List<NbcrvDetection> nbcrvDetections, EntityId entityId, int sourceId) {
        initialize(connection);
        for (NbcrvDetection nbcrvDetection : nbcrvDetections) {
            nbcrvDetectionTable.add(new DatabaseObject<>(nbcrvDetection, entityId, sourceId));
        }
        nbcrvDetectionTable.insertToDatabase(connection);

//        int lastid = 0;
//        for (NbcrvDetection nbcrvDetection : nbcrvDetections) {
//            int eventId = nbcrvEventTable.insert(connection, nbcrvDetection, entityId, sourceId);
//            if (eventId == -1) {
//                continue;
//            }
//            lastid = eventId;
//            nbcrvEventPositionsTable.insert(connection, nbcrvDetection, eventId);
//        }
//        try {
//            connection.commit();
//        } catch (SQLException e) {
//            Logger.getGlobal().log(Level.WARNING, "Error inserting to NBCRV Event", e);
//        }
    }

    public void addWdlReadings(Connection connection, List<WdlReading> wdlReadings, EntityId entityId, int sourceId) {
        initialize(connection);

        for (WdlReading wdlReading : wdlReadings) {
            wdlReadingTable.add(new DatabaseObject<>(wdlReading, entityId, sourceId));
        }
        wdlReadingTable.insertToDatabase(connection);
    }

    public List<NbcrvState> getNbcrvStates(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        initialize(connection);
        return nbcrvStateTable.getStates(connection, entityId, sourceIds);
    }

    public List<RadNucState> getRadNucStates(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        initialize(connection);
        return nbcrvRadNucStateTable.getStates(connection, entityId, sourceIds);
    }

    public List<Device> getDevices(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        initialize(connection);
        List<Device> devices = deviceTable.getDevices(connection, entityId, sourceIds);
        for (Device device : devices) {
            deviceStateTable.getDeviceStates(connection, entityId, device, sourceIds);
        }
        return devices;
    }

    public List<NbcrvDetection> getEvents(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        initialize(connection);
        return nbcrvDetectionTable.getNbcrvDetection(connection, entityId, sourceIds);
//        return nbcrvEventTable.getNbcrvEvents(connection, entityId, sourceIds, nbcrvEventPositionsTable);
    }

    public List<WdlReading> getWdlReadings(Connection connection, EntityId entityId, List<Integer> sourceIds) {
        initialize(connection);
        return wdlReadingTable.getWdlReadings(connection, entityId, sourceIds);
    }

    private void initialize(Connection connection) {
        if (nbcrvStateTable == null) {
            nbcrvStateTable = new NbcrvStateTable();
            nbcrvStateTable.createTable(connection);
        }
        if (nbcrvRadNucStateTable == null) {
            nbcrvRadNucStateTable = new NbcrvRadNucStateTable();
            nbcrvRadNucStateTable.createTable(connection);
        }
        if (deviceTable == null) {
            deviceTable = new DeviceTable();
            deviceTable.createTable(connection);
        }
        if (deviceStateTable == null) {
            deviceStateTable = new DeviceStateTable();
            deviceStateTable.createTable(connection);
        }
        if (nbcrvEventTable == null) {
            nbcrvEventTable = new NbcrvEventTable();
            nbcrvEventTable.createTable(connection);
        }
        if (nbcrvDetectionTable == null) {
            nbcrvDetectionTable = new NbcrvDetectionTable();
            nbcrvDetectionTable.createTable(connection);
        }
        if (nbcrvEventPositionsTable == null) {
            nbcrvEventPositionsTable = new NbcrvEventPositionsTable();
            nbcrvEventPositionsTable.createTable(connection);
        }
        if (genericDeviceStateTable == null) {
            genericDeviceStateTable = new GenericDeviceStateTable();
            genericDeviceStateTable.createTable(connection);
        }
        if (wdlReadingTable == null) {
            wdlReadingTable = new WdlReadingTable();
            wdlReadingTable.createTable(connection);
        }
    }
}
