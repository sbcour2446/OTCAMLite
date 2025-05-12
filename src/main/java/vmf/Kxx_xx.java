/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import java.util.Arrays;
import java.util.Calendar;

/**
 * This class is meant to be a message default in cases where a message parser
 * has not been completed.
 *
 * @author hansen
 */
public class Kxx_xx extends VmfMessage {

    String bodyText;

    Kxx_xx(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        parsedOk = parse(data);
    }

    private boolean parse(VmfDataBuffer data) {
        byte[] dataBuff = Arrays.copyOfRange(data.data.array(), data.bytePosition, data.bytePosition + data.numOfBytes);
        bodyText = CR + MsgBodyParser.parseVmf(defaultVmfVersion, header.fad, header.msgNumber, dataBuff, verboseDecode).trim();
        return true;
    }

    @Override
    public String getText() {
        if (header.isAck()) {
            return header.getAckText();
        } else if (header.msgType == MsgType.FILE) {
            return super.getText() + CR + "Filename: " + header.filename;
        } else {
            return super.getText() + bodyText;
        }

    }

    @Override
    public String getSummary() {
        return (header.msgType == MsgType.FILE) ? "File: " + header.filename : super.getSummary();
    }

}
