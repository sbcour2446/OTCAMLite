package gov.mil.otc._3dvis.layer.control;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.ScreenAnnotation;

import java.awt.*;

import static gov.mil.otc._3dvis.layer.Constant.*;

public class ControlButton extends ScreenAnnotation {

    private final boolean isButton;

    public ControlButton(String text, Dimension size, boolean isButton) {
        super(text, new Point());
        this.isButton = isButton;
        initialize(size);
    }

    private void initialize(Dimension size) {
        getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED);
        getAttributes().setBackgroundColor(TRANSPARENT);
        getAttributes().setBorderWidth(0);
        getAttributes().setCornerRadius(2);
        getAttributes().setDrawOffset(new Point());
        getAttributes().setHighlightScale(1);
        getAttributes().setImageOpacity(OPACITY);
        getAttributes().setImageRepeat(AVKey.REPEAT_NONE);
        getAttributes().setImageScale(1);
        getAttributes().setInsets(new Insets(0, 0, 0, 0));
        getAttributes().setScale(1);
        getAttributes().setSize(size);
        getAttributes().setTextAlign(AVKey.LEFT);
        getAttributes().setTextColor(Color.WHITE);
    }

    public void highlight(boolean highlight) {
        getAttributes().setHighlighted(highlight);
        if (isButton) {
            getAttributes().setBackgroundColor(highlight ? BUTTON_COLOR : TRANSPARENT);
        }
    }
}
