/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package pcap;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.overlay.Overlay;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.MilStd2525IconRetrieverEx;
import gov.nasa.worldwind.symbology.milstd2525.SymbolCode;
import pcap.PcapReader.Packet;
import vmf.*;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author hansen
 */
public class ProtocolParser {

    public static final SimpleDateFormat SDF_LONG = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected static final int ETHERTYPE_POSITION = 12;
    protected static final int VLAN_TAG_SIZE = 4;
    protected static final int ETHERNET_MTU = 1500;
    protected static final int MIN_UDP_LEN = 40;
    protected static final int IP_PROTOCOL_ID = 0x0800;
    protected static final int VLAN_TPID = 0x8100;
    protected static final long SPANNING_TREE_ADDRESS = 0x00000180c2000000L;
    protected static final int PROTOCOL_UDP = 17;
    protected static final int PED_RX_PORT = 11422;
    protected static final int PED_TX_PORT = 11411;
    protected static final int IPV4_RX_PORT = 8930;
    protected static final int IPV4_TX_PORT = 8925;
    protected static final int VMF_PORT = 1581;

    protected static final int MDP_PORT_1 = 8750;
    protected static final int MDP_PORT_2 = 8751;
    protected static final int MDP_PORT_3 = 8752;
    protected static final int MDP_PORT_4 = 8753;
    protected static final int MDP_PORT_5 = 8754;
    protected static final int MDP_PORT_6 = 8755;

    VmfMessage msg;
    HashMap<Integer, IEntity> entities = new HashMap<>();
    HashMap<String, ImageIcon> imageMap = new HashMap<>();
    final MilStd2525IconRetrieverEx milStd2525IconRetriever;
    final String LAST_IMPORT_KEY = "VmfParser.LastImport";
    HashMap<Long, Ipv4Assembler> ipAssemblers = new HashMap<>();
    HashMap<String, VmfMessage> c2Messages = new HashMap<>();
    ArrayList<Overlay> overlays = new ArrayList<>();
    boolean useSa = true;
    ArrayList<Integer> urnFilterList = null;
    long startTime = -1;
    long stopTime = -1;
    boolean applyTimeFilter = false;

    public ProtocolParser() {
        milStd2525IconRetriever = new MilStd2525IconRetrieverEx("jar:file:milstd2525-symbols.jar!");
    }

    public void setUrnFilter(ArrayList<Integer> urnFilterList) {
        this.urnFilterList = urnFilterList;
    }

    public void setTimeFilter(long startTime, long stopTime) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        applyTimeFilter = true;
    }

    public void process(File file) {
        try {
            PcapReader pr = new PcapReader(file);
            PcapReader.Packet packet;
            while ((packet = pr.getNextPacket()) != null) {
                if (packet.capLen >= MIN_UDP_LEN) {
                    // check reserved ethernet addresses (just the spanning tree multicast for now)
                    long ethAddress = packet.data.getLong() >> 16;
                    if (ethAddress != SPANNING_TREE_ADDRESS) {
                        int etherTypePosition = ETHERTYPE_POSITION;
                        // skip any vlan tags 
                        while (packet.data.getShort(etherTypePosition) == VLAN_TPID) {
                            etherTypePosition += VLAN_TAG_SIZE;
                        }
                        short etherType = packet.data.getShort(etherTypePosition);
                        // Process only IP data
                        if ((etherType > 0 && etherType <= ETHERNET_MTU) || etherType == IP_PROTOCOL_ID) {
                            packet.data.position(etherTypePosition + 2);
                            processIp(packet);
                        }
                    }
                }
            }
            pr.close();
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
    }

    public ArrayList<IEntity> getValidEntities() {
        return processNewEntities();
    }

    public HashMap<String, VmfMessage> getC2Messages() {
        return c2Messages;
    }

    public ArrayList<Overlay> getOverlays() {
        return overlays;
    }

    public static void parsePcapC2Messages(File file) {
        parsePcapC2Messages(file, null);
    }

    public static void parsePcapC2Messages(File file, ArrayList<Integer> urnFilterList) {
        ProtocolParser parser = new ProtocolParser();
        parser.urnFilterList = urnFilterList;
        try {
            PcapReader pr = new PcapReader(file);
            PcapReader.Packet packet;
            while ((packet = pr.getNextPacket()) != null) {
                if (packet.capLen >= MIN_UDP_LEN) {
                    // check reserved ethernet addresses (just the spanning tree multicast for now)
                    long ethAddress = packet.data.getLong() >> 16;
                    if (ethAddress != SPANNING_TREE_ADDRESS) {
                        int etherTypePosition = ETHERTYPE_POSITION;
                        // skip any vlan tags 
                        while (packet.data.getShort(etherTypePosition) == VLAN_TPID) {
                            etherTypePosition += VLAN_TAG_SIZE;
                        }
                        short etherType = packet.data.getShort(etherTypePosition);
                        // Process only IP data
                        if ((etherType > 0 && etherType <= ETHERNET_MTU) || etherType == IP_PROTOCOL_ID) {
                            packet.data.position(etherTypePosition + 2);
                            parser.processIp(packet);

                        }
                    }
                }
            }
            pr.close();
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }

        ArrayList<IEntity> validEntities = parser.processNewEntities();
        parser.processC2Messages(validEntities);
    }

    public static ArrayList<IEntity> parsePcap(File file) {
        ProtocolParser parser = new ProtocolParser();
        try {
            PcapReader pr = new PcapReader(file);
            PcapReader.Packet packet;
            while ((packet = pr.getNextPacket()) != null) {
                if (packet.capLen >= MIN_UDP_LEN) {
                    // check reserved ethernet addresses (just the spanning tree multicast for now)
                    long ethAddress = packet.data.getLong() >> 16;
                    if (ethAddress != SPANNING_TREE_ADDRESS) {
                        int etherTypePosition = ETHERTYPE_POSITION;
                        // skip any vlan tags 
                        while (packet.data.getShort(etherTypePosition) == VLAN_TPID) {
                            etherTypePosition += VLAN_TAG_SIZE;
                        }
                        short etherType = packet.data.getShort(etherTypePosition);
                        // Process only IP data
                        if ((etherType > 0 && etherType <= ETHERNET_MTU) || etherType == IP_PROTOCOL_ID) {
                            packet.data.position(etherTypePosition + 2);
                            parser.processIp(packet);

                        }
                    }
                }
            }
            pr.close();
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }

        return parser.processNewEntities();
    }

    protected static void printf(String fmt, Object... args) {
        System.out.printf(fmt, args);
    }

    public ArrayList<IEntity> processNewEntities() {
        ArrayList<IEntity> validEntities = new ArrayList<>();
//todo begin        entities.values().forEach((entity) -> {
//            // it there are any position reports for an entity, the first time will not be null.
//            if ( entity.getFirstTspi() != null) {
//                printf("%9s %40s %s\n", entity.getEntityId().toString(), entity.marking, entity.getMilitarySymbol());
//                entity.urn = Integer.toString(entity.entityId.id);
//                if (entity.getMilitarySymbol().length() != 15) {
//                    entity.setMilitarySymbol(entity.getDefault2525SymbolCode());//"SFGP--------USG";
//                } else {
//                    try {
//                        SymbolCode s = new SymbolCode(entity.getMilitarySymbol());
//                    } catch (Exception ex) {
//                        Logger.getGlobal().log(Level.INFO, null, ex);
//                        entity.setMilitarySymbol(entity.getDefault2525SymbolCode());
////                        if (entity.milStd2525Sidc.length() > 4 && entity.milStd2525Sidc.toUpperCase().charAt(2) == 'A') {
////                            entity.milStd2525Sidc = "SFAP--------USA";
////                        } else {
////                            entity.milStd2525Sidc = "SFGP--------USG";
////                        }
//                    }
//                }
//                entity.setSymbol(entity.getMilitarySymbol(), imageMap, EntityManager.ICON_SIZE, milStd2525IconRetriever);
//                if (entity.getMilitarySymbol().length() > 4 && entity.getMilitarySymbol().toUpperCase().charAt(2) == 'A') {
//                    entity.disEntityType.domain = 2;
//                }
//                if (entity.description.isEmpty()) {
//                    entity.setDescription(entity.urn);
//                }
//                if (entity.marking.isEmpty()) {
//                    entity.marking = entity.urn;
//                }
//
//                validEntities.add(entity);
//            } else {
//                // there are
//                entity.removeEntity(); // call in case any internal cleanup is required
//                AdHocEntity.removeEntity(entity);
//            }
//todo end        });

        return validEntities;
    }

    protected void processUDP(Packet packet) {
        ByteBuffer udp = packet.data;
        int srcPort = (int) udp.getShort() & 0xFFFF;
        int udpDest = (int) udp.getShort() & 0xFFFF;
        int length = (int) udp.getShort() & 0xFFFF;
        int checksum = (int) udp.getShort() & 0xFFFF;
        packet.udpDest.add(udpDest);

        switch (udpDest) {
            case VMF_PORT:
                processVMF(packet, length);
                break;

            case PED_TX_PORT:
            case PED_RX_PORT:
                processKgv72(packet);
                break;
            case IPV4_TX_PORT:
            case IPV4_RX_PORT:
                if (VmfMessage.supportNoPed) {
                    processIp(packet);
                }
                break;
            case MDP_PORT_1:
            case MDP_PORT_2:
            case MDP_PORT_3:
            case MDP_PORT_4:
            case MDP_PORT_5:
            case MDP_PORT_6:
                processMdp(packet, length);
                break;
        }
    }

    protected void queueC2Message(VmfMessage msg) {
        String key = msg.getHeader().getKey();
        if (c2Messages.get(key) == null) {
            c2Messages.put(key, msg); // we process them after all the entities have been created
        }
    }

    protected boolean packetIsBft(Packet p) {
        return p.udpDest.contains(IPV4_RX_PORT);
    }

    protected void processVMF(Packet packet, int payloadLength) {
        if (VmfMessage.bft2Only && !packetIsBft(packet)) {
            return;
        }

        Calendar collectTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        collectTime.setTimeInMillis(packet.timestamp / 1000000L);

        VmfMessage msg = VmfMessage.create(packet.data, payloadLength, collectTime, "DEMO");

        if (msg != null) {
//            if (msg.getHeader().isValid() && !msg.getHeader().isAck()) {
            if (msg.getHeader().isValid()) {
                switch (msg.getHeader().getMsgType()) {
                    case K01_01:
                        queueC2Message(msg);
                        break;
                    case K05_01:
                        if (useSa) {
                            processK05_01(msg);
                        }
                        break;
                    case K05_17:
                        processK05_17(msg);
                        break;
                    case SDSA:
                        if (useSa) {
                            processSdsa(msg);
                        }
                        break;
                    case FILE:
                        queueC2Message(msg);
                        break;
                    case K05_19:
                        break;
                    case OTHER:
                        queueC2Message(msg);
                        break;
                    default:
                        queueC2Message(msg);
                        break;

                }
            }
        }
    }

    protected void processKgv72(Packet packet) {
        ByteBuffer kgv = packet.data;
        int cmd = kgv.getInt();
        int header = kgv.getInt() & 0xFFFF;
        int said = kgv.getInt() & 0xFFFF;
        processIp(packet);
    }

    protected void processIp(Packet packet) {
        ByteBuffer ip = packet.data;
        int ipPosition = ip.position();
        int ihl = (int) ip.get();
        int ver = (ihl & 0xF0);
        ihl = (ihl & 0xF) * 4;
        if (ver != 0x40) {
            return;
        }
        int dscp = (int) ip.get();
        int len = (int) ip.getShort() & 0xFFFF;
        int id = (int) ip.getShort() & 0xFFFF;
        int flags = (int) ip.getShort() & 0xFFFF;
        int ttl = (int) ip.get();
        int protocol = (int) ip.get();
        int checksum = (int) ip.getShort() & 0xFFFF;
        int src = ip.getInt();
        int dst = ip.getInt();
        ip.position(ipPosition + ihl); // Point to the data field

        if ((flags & 0x3FFF) != 0) {
            int fragOffset = (flags & 0x1FFF) * 8;
            boolean moreFrags = (flags & 0x2000) != 0;
            long key = ((long) src & 0xFFFFFFFFL) | ((long) id << 32);
//            System.out.printf("(%5d) IP Fragment (offset = %4d) flags = %04X  Key=%08X\n", packet.id, fragOffset, flags, key);

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
                    processUDP(assembler.getPacket());
                }
            }

        } else if (protocol == PROTOCOL_UDP) {
            processUDP(packet);
        }
    }

    protected void processK05_01(VmfMessage msg) {
        if (msg instanceof K05_01) {
            K05_01 k05_01 = (K05_01) msg;

            for (K05_01.PositionReport report : k05_01.getReports()) {

                if (applyTimeFilter && (report.time.getTimeInMillis() < startTime || report.time.getTimeInMillis() > stopTime)) {
                    continue;
                }

                if (urnFilterList == null || urnFilterList.isEmpty() || urnFilterList.contains(report.urn)) {
                    IEntity entity = entities.get(report.urn);
                    if (entity == null) {
                        EntityId entityId = new EntityId(Defaults.SITE_APP_ID_3DVIS, Defaults.APP_ID_BFT, report.urn);
                        entity = new PlaybackEntity(entityId);
                        entities.put(report.urn, entity);
                    }

                    if (report.position.latitude.degrees != 0
                            && report.position.longitude.degrees != 0) {
                        entity.addTspi(new TspiData(report.time.getTimeInMillis(), report.position));
                    } else {
                        Logger.getGlobal().log(Level.INFO, String.format("filtering K05_01 from %d at ", report.urn, report.position));
                    }
                } else {
                    Logger.getGlobal().log(Level.INFO, String.format("filtering K05_01 from %d", report.urn));
                }
            }
        }

    }

    protected void processK05_17(VmfMessage msg) {
        if (msg instanceof K05_17) {
            K05_17 k05_17 = (K05_17) msg;
            k05_17.processOverlayInfo();
            for (Overlay overlay : k05_17.getOverlays()) {
                overlays.add(overlay);
            }
        }

    }

    public void processC2Messages(ArrayList<IEntity> entityList) {
        HashMap<Integer, IEntity> map = new HashMap<>();
        for (IEntity e : entityList) {
            map.put(e.getEntityId().getId(), e);
        }

        if (c2Messages.size() > 0) {
//todo begin            MessageForm dlg = MessageForm.open();
//            dlg.setAlwaysOnTop(true);
//            dlg.setVisible(true);
//
//            ArrayList<VmfMessage> messages = new ArrayList<>();
//
//            for (VmfMessage c2Msg : c2Messages.values()) {
//                if (applyTimeFilter && (c2Msg.getCollectionMs() < startTime || c2Msg.getCollectionMs() > stopTime)) {
//                    continue;
//                }
//
//                if (urnFilterList == null || urnFilterList.isEmpty() || urnFilterList.contains(c2Msg.getHeader().getSenderUrn())) {
//                    Entity entity = map.get(c2Msg.getHeader().getSenderUrn());
//                    if (entity != null) {
//                        VmfMessageEvent event = new VmfMessageEvent(c2Msg, entity);
//                        event.setEventTime(c2Msg.getHeader().getOrigninatorTime());
//                        event.setEndTime(c2Msg.getHeader().getOrigninatorTime() + VmfMessage.displayTimeMs);
//                        EventManager.addEvent(event);
//                        messages.add(c2Msg);
//                    }
//                } else {
//                    Logger.getGlobal().log(Level.INFO, String.format("filtering C2 message from %d", c2Msg.getHeader().getSenderUrn()));
//                }
//            }
//
//todo end            dlg.addMessages(messages);
        }
    }

    protected void processSdsa(VmfMessage msg) {
        if (msg instanceof Sdsa) {
            Sdsa sdsa = (Sdsa) msg;

            for (Sdsa.Record rec : sdsa.getRecords()) {
                if (urnFilterList == null || urnFilterList.isEmpty() || urnFilterList.contains(rec.urn)) {
                    IEntity entity = entities.get(rec.urn);
                    if (entity == null) {
                        EntityId entityId = new EntityId(Defaults.SITE_APP_ID_3DVIS, Defaults.APP_ID_BFT, rec.urn);
                        entity = new PlaybackEntity(entityId);
                        entities.put(rec.urn, entity);
                    }

                    String name = "";
                    // set name
                    if (!rec.fullName.isEmpty()) {
                        name = rec.fullName;
                    } else if (!rec.shortName.isEmpty()) {
                        name = rec.shortName;
                    } else if (!rec.alias.isEmpty()) {
                        name = rec.alias;
                    }
                    EntityDetail entityDetail = new EntityDetail.Builder()
                            .setTimestamp(rec.time.getTimeInMillis())
                            .setName(name)
                            .setAffiliation(Affiliation.FRIENDLY)
                            .setSource(rec.platformType)
                            .setMilitarySymbol(rec.symbol)
                            .setUrn(rec.urn)
                            .build();
                    entity.addEntityDetail(entityDetail);
                } else {
                    Logger.getGlobal().log(Level.INFO, String.format("filtering C2 message from %d", rec.urn));
                }
            }
        }
    }

    protected void processMdp(Packet packet, int length) {

        if (MdpAssembler.isSupported) {
            // create an MDP packet
            MdpAssembler mdpAssembler = MdpAssembler.process(packet, length);

            // if the stream is complete, send to the application layer decoder (MIL-STD 2045-47001)
            if ((mdpAssembler != null) && mdpAssembler.isComplete()) {
                Packet pkt = mdpAssembler.getData(packet.timestamp);
//            if (false) {
//                Packet pkt1 = pkt.deepCopy();
//                printBinaryFile(pkt1);
//            }
                if (pkt != null) {
                    processVMF(pkt, mdpAssembler.getLength());
                }
                MdpAssembler.remove(mdpAssembler);
            }
        }
    }

    private void printBinaryFile(Packet packet) {
        Calendar collectTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        collectTime.setTimeInMillis(packet.timestamp / 1000000L);
        VmfMessage msg = VmfMessage.create(packet.data, packet.udpLen, collectTime, "DEMO");

        if (msg instanceof BinaryFile) {
            ((BinaryFile) msg).save(System.getProperty("user.dir") + "/plugins/files");
        }
    }

}
