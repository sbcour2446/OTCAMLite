package gov.mil.otc._3dvis.data.ulog;

import java.nio.ByteBuffer;

public class MessageParameter extends MessageInfo {
    public MessageParameter(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    public String toString() {
        return String.format("PARAMETER: key=%s, value_type=%s, value=%s", format.name, format.getFullTypeString(), value);
    }
}
