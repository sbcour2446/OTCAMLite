package gov.mil.otc._3dvis.ui.widgets.status;

public class Result {

    private final String text;
    private final boolean successful;

    public Result(String text, boolean successful) {
        this.text = text;
        this.successful = successful;
    }

    public String getText() {
        return text;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
