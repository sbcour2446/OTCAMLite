package gov.mil.otc._3dvis.vmf;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Binary File Class
 */
public class BinaryFile extends VmfMessage {

    private byte[] fileData;

    /**
     * {@inheritDoc}
     */
    protected BinaryFile(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        if (header.isValid() && !header.isAck()) {
            try {
                if (header.getMessageSize() <= 0) {
                    // if the size was not specified, we will just have to assume it is the entire packet
                    fileData = new byte[data.numOfBytes - data.bytePosition];
                } else {
                    fileData = new byte[header.getMessageSize()];
                }
            } catch (Exception e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
            data.data.position(data.bytePosition);
            data.data.get(fileData, 0, fileData.length);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return super.getSummary() + (header.getFilename() == null ? "" : ": " + header.getFilename());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return super.getText() + System.lineSeparator() + (header.getFilename() == null ? "" :
                "Filename: " + header.getFilename());
    }
}
