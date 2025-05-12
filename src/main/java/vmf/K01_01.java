package vmf;

import java.util.Calendar;

/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
/**
 *
 * @author hansen
 */
public class K01_01 extends VmfMessage {

    String subject, comments;

    K01_01(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        this.header = header;
        this.collectTime = collectTime;
        this.collector = collector;
        subject = comments = "";
        parsedOk = parse(data);
    }

    final boolean parse(VmfDataBuffer data) {

        if (getHeader().isValid() && !getHeader().isAck()) {
            subject = data.getString(140);
            boolean fri;

            do {
                fri = data.getGri();
                comments += data.getString(1400);
            } while (fri);
        }
        return true;
    }

    @Override
    public String getText() {

        if (getHeader().isAck()) {
            return getHeader().getAckText();
        } else {
            String text = header.getShortText();

            text += System.lineSeparator() + "Subject: " + subject;
            text += System.lineSeparator() + "Comments: " + comments;

            return text;
        }
    }

    @Override
    public String getSummary() {
        return super.getSummary() + ": " + comments;
    }

}
