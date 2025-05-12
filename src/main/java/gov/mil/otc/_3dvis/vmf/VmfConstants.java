package gov.mil.otc._3dvis.vmf;

/**
 * Global BFT BftConstants
 */
public class VmfConstants {
    /**
     * Private Constructor to hide public one
     */
    private VmfConstants() {
    }

    /**
     * Constants
     */
    public static final int PROTOCOL_UDP = 17; // IP Header UDP Protocol ID
    public static final int PED_RX_PORT = 11422; // BFT PED Receive UDP Port
    public static final int PED_TX_PORT = 11411; // BFT PED Transmit UDP Port
    public static final int IPV4_RX_PORT = 8930; // BFT IPV4 Receive UDP Port
    public static final int IPV4_TX_PORT = 8925; // BFT IPV4 Transmit UDP Port
    public static final int VMF_PORT = 1581; // VMF UDP Destination Port

    public static final int MAX_PACKET_LEN = 65536; // Maximum Packet Length
}
