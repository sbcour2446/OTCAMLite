package gov.mil.otc._3dvis.vmf.pcap;

import gov.mil.otc._3dvis.vmf.VmfMessage;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Protocol Parser Class to extract IP/UDP JVMF message data from the Payload
 */
public class ProtocolParser {

    private static final int ETHERTYPE_POSITION = 12;
    private static final int VLAN_TAG_SIZE = 4;
    private static final int ETHERNET_MTU = 1500;
    private static final int MIN_UDP_LEN = 40;
    private static final int IP_PROTOCOL_ID = 0x0800;
    private static final int VLAN_TPID = 0x8100;
    private static final long SPANNING_TREE_ADDRESS = 0x00000180c2000000L;
    private static final int PROTOCOL_UDP = 17;
    private static final int PED_RX_PORT = 11422;
    private static final int PED_TX_PORT = 11411;
    private static final int IPV4_RX_PORT = 8930;
    private static final int IPV4_TX_PORT = 8925;
    private static final int VMF_PORT = 1581;

    private Map<Long, Ipv4Assembler> ipAssemblers = new HashMap<>();
    private Map<String, VmfMessage> messages = new HashMap<>();

    public List<VmfMessage> processPcap(File file) {
        try {
            PcapReader pcapReader = PcapReader.createPcapReader(file);
            if (pcapReader == null) {
                return new ArrayList<>();
            }

            Packet packet;
            while ((packet = pcapReader.getNextPacket()) != null) {
                if (packet.getCapLen() >= MIN_UDP_LEN) {
                    // check reserved ethernet addresses (just the spanning tree multicast for now)
                    long ethAddress = packet.getData().getLong() >> 16;
                    if (ethAddress != SPANNING_TREE_ADDRESS) {
                        int etherTypePosition = ETHERTYPE_POSITION;
                        // skip any vlan tags
                        while (packet.getData().getShort(etherTypePosition) == VLAN_TPID) {
                            etherTypePosition += VLAN_TAG_SIZE;
                        }
                        short etherType = packet.getData().getShort(etherTypePosition);
                        // Process only IP data
                        if ((etherType > 0 && etherType <= ETHERNET_MTU) || etherType == IP_PROTOCOL_ID) {
                            packet.getData().position(etherTypePosition + 2);
                            VmfMessage vmfMessage = processIp(packet);
                            addVmfMessage(vmfMessage);
                        }
                    }
                }
            }
            pcapReader.close();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "Error process BFT file", e);
        }

        return new ArrayList<>(messages.values());
    }

    private void addVmfMessage(VmfMessage vmfMessage) {
        if (vmfMessage != null) {
            String key = vmfMessage.getHeader().getKey();
            if (messages.get(key) == null) {
                messages.put(key, vmfMessage);
            }
        }
    }

    /**
     * Process the IP layer for the received frame
     *
     * @param packet Packet Data
     * @return VMF Message Object of null if not successful
     */
    public VmfMessage processIp(Packet packet) {
        ByteBuffer ip = packet.getData();
        int ipPosition = ip.position();
        int ihl = ip.get();
        int ver = (ihl & 0xF0);
        ihl = (ihl & 0xF) * 4;
        if (ver != 0x40) {
            return null;
        }
        int dscp = ip.get();
        int len = ip.getShort() & 0xFFFF;
        int id = ip.getShort() & 0xFFFF;
        int flags = ip.getShort() & 0xFFFF;
        int ttl = ip.get();
        int protocol = ip.get();
        int checksum = ip.getShort() & 0xFFFF;
        int src = ip.getInt();
        int dst = ip.getInt();
        ip.position(ipPosition + ihl); // Point to the data field

        if ((flags & 0x3FFF) != 0) {
            int fragOffset = (flags & 0x1FFF) * 8;
            boolean moreFrags = (flags & 0x2000) != 0;
            long key = (src & 0xFFFFFFFFL) | ((long) id << 32);

            Ipv4Assembler assembler = ipAssemblers.get(key);
            if (assembler == null) {
                assembler = new Ipv4Assembler(packet, flags, len - ihl);
                ipAssemblers.put(key, assembler);
            } else {
                assembler.add(packet, flags, len - ihl);
            }
            if (assembler.isComplete()) {
                ipAssemblers.remove(key);
                if (protocol == PROTOCOL_UDP) {
                    return processUDP(assembler.getPacket());
                }
            }

        } else if (protocol == PROTOCOL_UDP) {
            return processUDP(packet);
        }

        return null;
    }

    /**
     * Process the IP/UDP packet from the protocol stack
     *
     * @param packet IP Payload packet containing UDP
     * @return VMF Message Object of null if not successful
     */
    public VmfMessage processUDP(Packet packet) {
        ByteBuffer udp = packet.getData();
        int srcPort = udp.getShort() & 0xFFFF;
        int udpDest = udp.getShort() & 0xFFFF;
        int length = udp.getShort() & 0xFFFF;
        int checksum = udp.getShort() & 0xFFFF;
        packet.getUdpDest().add(udpDest);
        VmfMessage msg = null;

        switch (udpDest) {
            case VMF_PORT:
                msg = processVMF(packet, length);
                break;
            case PED_TX_PORT, PED_RX_PORT:
                msg = processKgv72(packet);
                break;
            case IPV4_TX_PORT, IPV4_RX_PORT:
                if (VmfMessage.SUPPORT_NO_PED) {
                    msg = processIp(packet);
                }
                break;
            default:
                break;
        }
        return msg;
    }

    /**
     * Process the VMF Data from the UDP Payload
     *
     * @param packet        Packet Data
     * @param payloadLength Packet Data Length in bytes
     */
    public VmfMessage processVMF(Packet packet, int payloadLength) {
        //
        // todo: Need Launcher App BFT-Only flag
        //
        Calendar collectTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        collectTime.setTimeInMillis(System.currentTimeMillis());

        return VmfMessage.create(packet.getData(), payloadLength, collectTime, "OTCAM");
    }

    /**
     * Process PED-derived packet data
     *
     * @param packet Packet Data
     */
    public VmfMessage processKgv72(Packet packet) {
        ByteBuffer kgv = packet.getData();
        int cmd = kgv.getInt();
        int header = kgv.getInt() & 0xFFFF;
        int said = kgv.getInt() & 0xFFFF;
        return processIp(packet);
    }
}
