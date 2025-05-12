package gov.mil.otc._3dvis.project.avcad;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class ConnectionStatus extends TimedData {

    private final boolean isConnected;

    public ConnectionStatus(long timestamp, boolean isConnected) {
        super(timestamp);
        this.isConnected = isConnected;
    }

    public boolean isConnected() {
        return isConnected;
    }
}
