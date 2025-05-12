package gov.mil.otc._3dvis.vmf.pcap;

import java.io.*;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PcapReader {

    public static PcapReader createPcapReader(File file) {
        PcapReader pcapReader = new PcapReader();
        if (pcapReader.open(file)) {
            return pcapReader;
        }
        return null;
    }

    private static final int SECTION_HEADER = 0x0A0D0D0A;
    private static final int INTERFACE_DESCRIPTION = 1;
    private static final int ENHANCED_PACKET = 6;
    private boolean bytesReversed = false;
    private final Block block = new Block();
    private long packetId = 0;
    private File file;
    private ZipFile zipFile = null;
    private BufferedReader br = null;
    private DataInputStream input = null;
    private boolean moreData;
    private Packet packet = new Packet();

    private PcapReader() {
    }

    private boolean open(File file) {
        try {
            if (isZipFile(file)) {
                zipFile = new ZipFile(file);
                for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
                    ZipEntry entry = e.nextElement();
                    if (entry.getName().contains(".pcap")) {
                        input = new DataInputStream(new BufferedInputStream(zipFile.getInputStream(entry)));
                        break;
                    }
                }
            } else {
                input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            }
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, "PcapReader", e);
        }

        if (input != null) {
            return ReadHeader();
        }

        return false;
    }

    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
        if (zipFile != null) {
            zipFile.close();
        }
    }

    private boolean ReadHeader() {
        try {
            Block header = new Block();
            header.setType(input.readInt());
            while (header.getType() != SECTION_HEADER) {
                header.setType(input.readInt());
            }
            int len = input.readInt();
            header.setMagic(input.readInt());
            bytesReversed = header.getMagic() != 0x1a2b3c4d;
            header.setLength(flip(len) - 16);
            if (header.getLength() > packet.getBytes().length) {
                packet = new Packet(header.getLength());
            }
            input.read(packet.getBytes(), 0, header.getLength());
            if (header.getLength() % 4 != 0) {
                input.read();
            }
            int x = flip(input.readInt()) - 12;
            Logger.getGlobal().log(Level.INFO, String.valueOf(x));
        } catch (IOException e) {
            String message = String.format("Error parsing %s", file.getAbsolutePath());
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return true;
    }

    protected Packet getNextPacket() {
        try {
            while (true) {
                block.setType(flip(input.readInt()));
                block.setLength(flip(input.readInt()));
                if (block.getType() == ENHANCED_PACKET) {
                    return createPacket();
                } else if (block.getType() == INTERFACE_DESCRIPTION) {
                    // TODO need to get at least the timestamp resolution
                }
                long bytesToSkip = block.getLength() - 8;
                long skipped = input.skip(bytesToSkip);
                while (skipped < bytesToSkip) {
                    skipped += input.skip(bytesToSkip - skipped);
                }
            }
        } catch (EOFException ex) {
            return null;
        } catch (IOException ex) {
            Logger.getLogger(PcapReader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Packet createPacket() {
        try {
//            packet = new Packet();
            if (block.getLength() > packet.getBytes().length) {
                packet = new Packet(block.getLength());
            }
            packet.setId(packetId++);
            packet.setInterfaceId(flip(input.readInt()));
            long timestamp = flip(input.readInt());
            timestamp = timestamp << 32 | (flip(input.readInt()) & 0xFFFFFFFFL);
            // hack, assume that it the timestamp is greater than 1e16 then  it is already in nanoseconds else it is in microseconds
            // real solution is to read the options from the INTERFACE_DESCRIPTION block
            if (timestamp < 10000000000000000L) {
                timestamp *= 1000L; // change usec to nanoseconds
            }
            packet.setTimestamp(timestamp);
            packet.setCapLen(flip(input.readInt()));
            packet.setOrigLen(flip(input.readInt()));
            input.read(packet.getBytes(), 0, packet.getCapLen());
            packet.getData().rewind();
            packet.getUdpDest().clear();
            packet.setUdpLen(0);
            int bytesToSkip = block.getLength() - 28 - packet.getCapLen(); // type(1, len(1), interface(1), timestamp(2), cap(1), orig(1), len(1)
            long skipped = input.skip(bytesToSkip);
            while (skipped < bytesToSkip) {
                skipped += input.skip(bytesToSkip - skipped);
            }
        } catch (IOException ex) {
            Logger.getLogger(PcapReader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return packet;
    }

    private int flip(int i) {
        return (bytesReversed) ? i << 24 & 0xff000000 | i << 8 & 0xff0000 | i >> 8 & 0xff00 | i >> 24 & 0xff : i;
    }

    private boolean isZipFile(File file) throws IOException {
        if (file.isDirectory()) {
            return false;
        }
        if (!file.canRead()) {
            throw new IOException("Cannot read file " + file.getAbsolutePath());
        }
        if (file.length() < 4) {
            return false;
        }
        int test;
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            test = in.readInt();
        }
        return test == 0x504b0304;
    }
}
