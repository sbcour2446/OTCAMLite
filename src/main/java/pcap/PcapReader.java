/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package pcap;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *
 * @author hansen
 */
public class PcapReader {

    static final int SECTION_HEADER = 0x0A0D0D0A;
    static final int INTERFACE_DESCRIPTION = 1;
    static final int ENHANCED_PACKET = 6;
    boolean bytesReversed = false;
    Block block = new Block();
    long packetId = 0;
    File file;
    BufferedReader br = null;
    DataInputStream input = null;
    boolean moreData;
    Packet packet = new Packet();

    public PcapReader(File file) throws FileNotFoundException, IOException {
        this.file = file;
        packetId = 1;
        if (isZipFile(file)) {
            ZipFile zip = new ZipFile(file);
            for (Enumeration<? extends ZipEntry> e = (Enumeration<? extends ZipEntry>) zip.entries(); e.hasMoreElements();) {
                ZipEntry entry = e.nextElement();
                if (entry.getName().contains(".pcap")) {
                    input = new DataInputStream(
                            new BufferedInputStream(zip.getInputStream(entry)));
                }
            }
        } else {

            input = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(file)));
        }
        packet.data = ByteBuffer.wrap(packet.bytes);
        ReadHeader();
    }

    public void close() throws IOException {
        if (input != null) {
            input.close();
        }
    }

    int flip(int i) {
        return (bytesReversed) ? i << 24 & 0xff000000 | i << 8 & 0xff0000 | i >> 8 & 0xff00 | i >> 24 & 0xff : i;
    }

    Packet getNextPacket() {
        try {
            while (true) {
                block.type = flip(input.readInt());
                block.length = flip(input.readInt());
                if (block.type == ENHANCED_PACKET) {
                    return createPacket();
                } else if (block.type == INTERFACE_DESCRIPTION) {
                    // TODO need to get at least the timestamp resolution
                }
                long bytesToSkip = block.length - 8;
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
            if (block.length > packet.bytes.length) {
                packet.bytes = new byte[block.length];
            }
            packet.id = packetId++;
            packet.interfaceId = flip(input.readInt());
            packet.timestamp = flip(input.readInt());
            packet.timestamp = packet.timestamp << 32 | (flip(input.readInt()) & 0xFFFFFFFFL);
            // hack, assume that it the timestamp is greater than 1e16 then  it is already in nanoseconds else it is in microseconds
            // real solution is to read the options from the INTERFACE_DESCRIPTION block
            if (packet.timestamp < 10000000000000000L) {
                packet.timestamp *= 1000L; // change usec to nanoseconds
            }
            packet.capLen = flip(input.readInt());
            packet.origLen = flip(input.readInt());
            input.read(packet.bytes, 0, packet.capLen);
            packet.data.rewind();
            packet.udpDest.clear();
            packet.udpLen = 0;
            int bytesToSkip = block.length - 28 - packet.capLen; // type(1, len(1), interface(1), timestamp(2), cap(1), orig(1), len(1)
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

    private boolean ReadHeader() {
        try {
            Block header = new Block();
            header.type = input.readInt();
            while (header.type != SECTION_HEADER) {
                header.type = input.readInt();
            }
            int len = input.readInt();
            header.magic = input.readInt();
            bytesReversed = header.magic != 0x1a2b3c4d;
            header.length = flip(len) - 16;
            if (header.length > packet.bytes.length) {
                packet.bytes = new byte[header.length];
            }
            input.read(packet.bytes, 0, header.length);
            switch (header.length % 4) {
                case 1:
                    input.read();
                case 2:
                    input.read();
                case 3:
                    input.read();
            }
            int x = flip(input.readInt()) - 12;

        } catch (IOException ex) {
            Logger.getLogger(PcapReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    class Block {

        int type = 0;
        int length;
        int magic;
    }

    public static class Packet {

        long id;
        int udpLen;
        long timestamp;
        int interfaceId;
        int capLen, origLen;
        byte[] bytes;// = new byte[65536];
        ByteBuffer data;
        ArrayList<Integer> udpDest = new ArrayList<>();

        public Packet() {
            this.udpLen = 0;
            bytes = new byte[65536];
        }

        public Packet(int len) {
            this.udpLen = len;
            bytes = new byte[len];
            data = ByteBuffer.wrap(bytes);
        }

        Packet deepCopy() {
            Packet copy = new Packet(this.bytes.length);
            copy.id = this.id;
            copy.udpLen = this.udpLen;
            copy.timestamp = this.timestamp;
            copy.interfaceId = this.interfaceId;
            copy.capLen = this.capLen;
            copy.origLen = this.origLen;
            System.arraycopy(this.bytes, 0, copy.bytes, 0, copy.bytes.length);
            copy.data = ByteBuffer.wrap(copy.bytes);
            copy.data.position(this.data.position());
            return copy;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public ByteBuffer getData() {
            return data;
        }

    }

    private static boolean isZipFile(File file) throws IOException {
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
