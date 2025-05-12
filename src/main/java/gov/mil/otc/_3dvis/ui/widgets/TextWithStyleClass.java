package gov.mil.otc._3dvis.ui.widgets;

    import javafx.scene.text.Text;

public class TextWithStyleClass extends Text {

    /**
     * {@inheritDoc}
     */
    public TextWithStyleClass() {
        super();
        getStyleClass().add("text-id");
    }

    /**
     * {@inheritDoc}
     */
    public TextWithStyleClass(String text) {
        this();
        setText(text);
    }

    /**
     * {@inheritDoc}
     */
    public TextWithStyleClass(double x, double y, String text) {
        this(text);
        setX(x);
        setY(y);
    }
}
