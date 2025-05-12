package gov.mil.otc._3dvis.layer.menu;

import gov.mil.otc._3dvis.layer.control.ControlButton;
import gov.mil.otc._3dvis.utility.ImageLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MenuButton extends ControlButton {

    private static final String SHOW_MENU_IMAGE = "/images/show_menu.png";
    private static final String SHOW_MENU2_IMAGE = "/images/show_menu3.png";
    private static final String HIDE_MENU_IMAGE = "/images/hide_menu.png";
    private boolean selected = false;

    public MenuButton(String text, Dimension size, boolean isButton) {
        super(text, size, isButton);
        initialize();
    }

    private void initialize() {
        getAttributes().setImageOpacity(.75);
        getAttributes().setImageSource(ImageLoader.getBufferedImage(SHOW_MENU_IMAGE));
        getAttributes().setInsets(new Insets(2, 5, 2, 0));
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        BufferedImage image = ImageLoader.getBufferedImage(selected ? HIDE_MENU_IMAGE : SHOW_MENU2_IMAGE);
        getAttributes().setImageSource(image);
    }

    @Override
    public void highlight(boolean highlight) {
        BufferedImage image;
        if (selected) {
            image = ImageLoader.getBufferedImage(highlight ? HIDE_MENU_IMAGE : SHOW_MENU_IMAGE);
        } else {
            image = ImageLoader.getBufferedImage(highlight ? SHOW_MENU2_IMAGE : SHOW_MENU_IMAGE);
        }
        getAttributes().setImageSource(image);
        super.highlight(highlight);
    }
}
