package gov.mil.otc._3dvis.tena;

public enum ConnectionState {
    DISCONNECTING("Disconnecting"),
    DISCONNECTED("Disconnected"),
    CONNECTING("Connecting"),
    CONNECTED("Connected"),
    FAILED("Failed");
    final String description;

    ConnectionState(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
