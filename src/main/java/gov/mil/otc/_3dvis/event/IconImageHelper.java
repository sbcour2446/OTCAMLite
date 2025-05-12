package gov.mil.otc._3dvis.event;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.IconType;
import gov.mil.otc._3dvis.settings.SettingsManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class IconImageHelper {

    private static final HashMap<String, BufferedImage> IMAGE_MAP = new HashMap<>();

    private IconImageHelper() {
    }

    public static BufferedImage createFireIndicator(IEntity entity) {
        int size = getIconSize(entity);
        String key = String.format("%s,%d", "FireIndicator", size);
        BufferedImage image = IMAGE_MAP.get(key);
        if (image == null) {
            Graphics2D g = null;
            try {
                image = new BufferedImage(size, size * 2 + 10, BufferedImage.TYPE_INT_ARGB);
                g = image.createGraphics();
                g.setColor(Color.RED);
                int middle = size / 2;
                g.drawLine(4, 10, middle, 0);
                g.drawLine(middle, 0, size - 4, 10);
                g.drawLine(5, 10, middle, 1);
                g.drawLine(middle, 1, size - 5, 10);
                g.drawLine(6, 10, middle, 2);
                g.drawLine(middle, 2, size - 6, 10);
            } finally {
                if (g != null) {
                    g.dispose();
                }
            }
        }
        return image;
    }

    public static BufferedImage createEventIndicator(IEntity entity) {
        int size = getIconSize(entity);
        String key = String.format("%s,%d", "EventIndicator", size);
        BufferedImage image = IMAGE_MAP.get(key);
        if (image == null) {
            Graphics2D g = null;
            try {
                image = new BufferedImage(size, size * 2 + 3, BufferedImage.TYPE_INT_ARGB);
                g = image.createGraphics();
                g.setColor(Color.YELLOW);
                g.drawLine(4, 0, size - 4, 0);
                g.drawLine(4, 1, size - 4, 1);
                g.drawLine(4, 2, size - 4, 2);
            } finally {
                if (g != null) {
                    g.dispose();
                }
            }
        }
        return image;
    }

    private static int getIconSize(IEntity entity) {
        int size;
        if (entity.isPlatform()) {
            size = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.PLATFORM);
        } else if (entity.isLifeForm()) {
            size = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.LIFE_FORM);
        } else if (entity.isMunition()) {
            size = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.MUNITION);
        } else {
            size = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.OTHER);
        }
        return size;
    }
}
