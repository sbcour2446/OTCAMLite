package gov.mil.otc._3dvis.layer.control;

import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.ScreenImage;

import java.awt.*;

import static gov.mil.otc._3dvis.layer.Constant.OPACITY;

public class ImageButton extends ControlButton {

    private final ScreenImage screenImage = new ScreenImage();
    private final Point imageOffset;

    public ImageButton(String image, Dimension size) {
        this(image, size, new Point(), false);
    }

    public ImageButton(String image, Dimension size, Point imageOffset) {
        this(image, size, imageOffset, false);
    }

    public ImageButton(String image, Dimension size, boolean isButton) {
        this(image, size, new Point(), isButton);
    }

    public ImageButton(String image, Dimension size, Point imageOffset, boolean isButton) {
        super("", size, isButton);
        initialize(image);
        this.imageOffset = imageOffset;
    }

    private void initialize(String image) {
        screenImage.setDelegateOwner(this);
        screenImage.setImageSource(ImageLoader.getBufferedImage(image));
        screenImage.setOpacity(OPACITY);
        screenImage.setRotationOffset(Offset.CENTER);
    }

    @Override
    public void highlight(boolean highlight) {
        super.highlight(highlight);
        screenImage.setOpacity(highlight ? 1.0 : OPACITY);
    }

    public void setHeading(double heading) {
        screenImage.setRotation(heading);
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        int y = dc.getView().getViewport().height - getScreenPoint().y - getAttributes().getSize().height / 2;
        screenImage.setScreenLocation(new Point(getScreenPoint().x + imageOffset.x, y + imageOffset.y));
        screenImage.render(dc);
        super.doRenderNow(dc);
    }
}
