package gov.mil.otc._3dvis.vmf;

import java.util.Arrays;
import java.util.Calendar;

/**
 * This class is meant to be a message default in cases where a message parser has not been completed.
 */
public class Unknown extends VmfMessage {

    private String bodyText;
    private byte[] dataBuff;

    /**
     * {@inheritDoc}
     */
    protected Unknown(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean parse(VmfDataBuffer data) {
        dataBuff = Arrays.copyOfRange(data.data.array(), data.bytePosition, data.bytePosition + data.numOfBytes);
        return true;
    }

    public String getBodyText() {
        return bodyText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        if (header.isAck()) {
            return header.getAckText();
        } else if (header.getMessageType() == MessageType.FILE) {
            return super.getText() + System.lineSeparator() + "Filename: " + header.getFilename();
        } else {
            return super.getText() + bodyText;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return (header.getMessageType() == MessageType.FILE) ? "File: " + header.getFilename() : super.getSummary();
    }

}
