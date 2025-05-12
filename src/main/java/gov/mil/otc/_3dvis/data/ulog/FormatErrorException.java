package gov.mil.otc._3dvis.data.ulog;

public class FormatErrorException extends Exception {
    public FormatErrorException(String s) {
        super(s);
    }

    public FormatErrorException(long position, String s) {
        super(position + ": " + s);
    }

    public FormatErrorException(long position, String s, Throwable cause) {
        super(position + ": " + s, cause);
    }
}
