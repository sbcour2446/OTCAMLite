/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hansen
 */
public class BinaryFile extends VmfMessage {

    static final SimpleDateFormat SDF = new SimpleDateFormat("/yyyy-MM-dd-HH-mm-ss ");
    byte[] fileData;

    public BinaryFile(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        parsedOk = parse(data);
    }

    final boolean parse(VmfDataBuffer data) {

        if (header.isValid() && !header.isAck()) {
            try {
                if (header.messageSize <= 0) {
                    // if the size was not specified, we will just have to assume it is the entire packet
                    fileData = new byte[data.numOfBytes - data.bytePosition];
                } else {
                    fileData = new byte[header.messageSize];
                }
            } catch (Exception e) {
                System.out.println(e);
            }
            data.data.position(data.bytePosition);
            data.data.get(fileData, 0, fileData.length);
        }

        return false;
    }

    public File save(String path) {
        File file = null;
        if (!header.isAck()) {
            file = new File(path, SDF.format(header.origninatorTime.getTime()) + header.filename);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData, 0, fileData.length);
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(BinaryFile.class.getName()).log(Level.SEVERE, null, ex);
                file = null;
            }
        }
        return file;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + (header.filename == null ? "" : ": " + header.filename);
    }

    @Override
    public String getText() {
        return super.getText() + CR + (header.filename == null ? "" : "Filename: " + header.filename);
    }

}
