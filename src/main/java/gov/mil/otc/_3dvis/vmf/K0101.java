package gov.mil.otc._3dvis.vmf;

import java.util.Calendar;

/**
 * The VMF K01.01 Free Text Message Class
 */
public class K0101 extends VmfMessage {

    private String subject;
    private String comments;

    /**
     * {@inheritDoc}
     */
    public K0101(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
        subject = comments = "";
    }

    /**
     * Message Parser
     *
     * @param data VmfDataBuffer data
     * @return true if successful parse otherwise, false
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {
        StringBuilder stringBuilder = new StringBuilder();
        if (getHeader().isValid() && !getHeader().isAck()) {
            subject = data.getString(140);
            boolean fri;

            do {
                fri = data.getGri();
                stringBuilder.append(data.getString(1400));
            } while (fri);
        }
        comments += stringBuilder.toString();
        return true;
    }

    public String getSubject() {
        return subject;
    }

    public String getComments() {
        return comments;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return super.getSummary() + ": " + comments;
    }

}
