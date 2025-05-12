package gov.mil.otc._3dvis.data.ulog;

import java.nio.ByteBuffer;

public class MessageLog {
    public final String message;
    public final long timestamp;
    public final char logLevel;

    public MessageLog(ByteBuffer buffer, int msgSize) {
        logLevel = (char) (buffer.get() & 0xFF);
        timestamp = buffer.getLong();
        message = MessageFormat.getString(buffer, msgSize - 9);
    }

    public String getLevelStr() {
        return switch (logLevel) {
            case '0' -> "EMERG";
            case '1' -> "ALERT";
            case '2' -> "CRIT";
            case '3' -> "ERROR";
            case '4' -> "WARNING";
            case '5' -> "NOTICE";
            case '6' -> "INFO";
            case '7' -> "DEBUG";
            default -> "(unknown)";
        };
    }

    @Override
    public String toString() {
        return String.format("LOG: time=%s, level=%s, message=%s", timestamp, getLevelStr(), message);
    }
}
