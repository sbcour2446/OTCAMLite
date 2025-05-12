package gov.mil.otc._3dvis.entity;

import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.MilitarySymbolUtility;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.base.EntityDisplay;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.IconType;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.utility.SwingUtility;
import gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.MilStd2525IconRetrieverEx;
import gov.nasa.worldwind.avlist.AVListImpl;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IconImageHelper {

    private static final String INVALID_CODE = "Invalid military symbol";
    private static final IconImageHelper SINGLETON = new IconImageHelper();
    private final HashMap<String, BufferedImage> imageMap = new HashMap<>();
    private final MilStd2525IconRetrieverEx milStd2525IconRetriever;

    private IconImageHelper() {
        milStd2525IconRetriever = new MilStd2525IconRetrieverEx("jar:file:milstd2525-symbols.jar!");
    }

    public static BufferedImage createResourseImage(String imageFileName) {
        BufferedImage bufferedImage = null;
        try (InputStream inputStream = IconImageHelper.class.getResourceAsStream(imageFileName)) {
            if (inputStream != null) {
                bufferedImage = ImageIO.read(inputStream);
            }
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Could not load image: %s", imageFileName), e);
        }
        return bufferedImage;
    }

    public static BufferedImage getImage(String militarySymbol) {
        try {
            return SINGLETON.milStd2525IconRetriever.createIcon(militarySymbol, new AVListImpl());
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Invalid military symbol %s", militarySymbol), e);
        }
        return null;
    }

    public static BufferedImage getImage(String militarySymbol, int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        try {
            BufferedImage militarySymbolImage = SINGLETON.milStd2525IconRetriever.createIcon(militarySymbol, new AVListImpl());
            graphics.drawImage(militarySymbolImage, 0, 0, width, height, null);
        } catch (Exception e) {
            String message = INVALID_CODE + " " + militarySymbol;
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
        graphics.dispose();
        return bufferedImage;
    }

    public static BufferedImage getIcon(IEntity entity) {
        if (entity.isMunition()) {
            return IconImageHelper.getMunitionImage(entity);
        } else {
            return IconImageHelper.getPlatformImage(entity);
        }
    }

    public static BufferedImage getIcon(Affiliation affiliation) {
        return getIcon(MilitarySymbolUtility.getDefaultMilitarySymbol(affiliation));
    }

    public static BufferedImage getIcon(String militarySymbol) {
        int size = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.PLATFORM);
        BufferedImage bufferedImage = new BufferedImage(size, size * 2, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setColor(Color.BLACK);
        int middle = size / 2;
        graphics.drawLine(middle, size, middle, size * 2);
        graphics.fillRect(3, size, size - 6, 3);
        graphics.fillRect(4, size + 1, size - 8, 1);

        try {
            BufferedImage militarySymbolImage = SINGLETON.milStd2525IconRetriever.createIcon(militarySymbol, new AVListImpl());
            graphics.drawImage(militarySymbolImage, 0, 0, size - 1, size - 1, null);
        } catch (Exception e) {
            String message = INVALID_CODE + " " + militarySymbol;
            Logger.getGlobal().log(Level.WARNING, message, e);
        }

        graphics.dispose();
        return bufferedImage;
    }

    public static BufferedImage getPinIcon(Color color, boolean noLine) {
        int size = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.PIN);
        int height = noLine ? size : size * 2;
        String key = String.format("PinImage,%d,%s", color.getRGB(), size);
        BufferedImage image = SINGLETON.imageMap.get(key);
        if (image == null) {
            Graphics2D graphics = null;
            try {
                image = new BufferedImage(size, height, BufferedImage.TYPE_INT_ARGB);
                graphics = image.createGraphics();
                int middle = size / 2;
                if (!noLine) {
                    graphics.setColor(Color.BLACK);
                    graphics.drawLine(middle, size, middle, size * 2);
                }
                graphics.setColor(color);
                graphics.fillOval(0, 0, size, size);
                SINGLETON.imageMap.put(key, image);
            } finally {
                if (graphics != null) {
                    graphics.dispose();
                }
            }
        }
        return image;
    }

    public static BufferedImage getPinIcon(Color color) {
        int size = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.PIN);
        String key = String.format("PinImage,%d,%s", color.getRGB(), size);
        BufferedImage image = SINGLETON.imageMap.get(key);
        if (image == null) {
            Graphics2D graphics = null;
            try {
                image = new BufferedImage(size, size * 2, BufferedImage.TYPE_INT_ARGB);
                graphics = image.createGraphics();
                int middle = size / 2;
                graphics.setColor(Color.BLACK);
                graphics.drawLine(middle, size, middle, size * 2);
                graphics.setColor(color);
                graphics.fillOval(0, 0, size, size);
                SINGLETON.imageMap.put(key, image);
            } finally {
                if (graphics != null) {
                    graphics.dispose();
                }
            }
        }
        return image;
    }

    public static BufferedImage getSquareIcon(Color color, boolean noLine) {
        int size = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.SQUARE);
        int height = noLine ? size : size * 2;
        String key = String.format("SquareImage,%d,%s", color.getRGB(), size);
        BufferedImage image = SINGLETON.imageMap.get(key);
        if (image == null) {
            Graphics2D graphics = null;
            try {
                image = new BufferedImage(size, height, BufferedImage.TYPE_INT_ARGB);
                graphics = image.createGraphics();

                int middle = size / 2;
                if (!noLine) {
                    graphics.setColor(Color.BLACK);
                    graphics.drawLine(middle, size, middle, size * 2);
                }
                graphics.setColor(color);
                graphics.fillRect(0, 0, size, size);

                SINGLETON.imageMap.put(key, image);
            } finally {
                if (graphics != null) {
                    graphics.dispose();
                }
            }
        }
        return image;
    }

    private static BufferedImage getMunitionImage(IEntity entity) {
        EntityDetail entityDetail = entity.getEntityDetail();
        if (entityDetail == null) {
            return null;
        }
        Color affiliationColor = SettingsManager.getSettings().getAffiliationColor(entityDetail.getAffiliation());
        int size = SettingsManager.getSettings().getIconDisplay().getIconSize(entity);
        String key = String.format("munition,%d,%s", affiliationColor.getRGB(), size);
        BufferedImage image = SINGLETON.imageMap.get(key);
        if (image == null) {
            Graphics2D graphics = null;
            try {
                image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                graphics = image.createGraphics();
                graphics.setColor(Color.BLACK);
                graphics.fillOval(0, 0, size, size);
                graphics.setColor(affiliationColor);
                graphics.fillOval(0, 0, size - 1, size - 1);
                SINGLETON.imageMap.put(key, image);
            } finally {
                if (graphics != null) {
                    graphics.dispose();
                }
            }
        }
        return image;
    }

    private static BufferedImage getPlatformImage(IEntity entity) {
        EntityDetail entityDetail = entity.getEntityDetail();
        if (entityDetail == null) {
            return null;
        }

        TspiData tspiData = entity.getCurrentTspi();
        if (tspiData == null) {
            return null;
        }

        EntityDisplay entityDisplay = entity.getEntityDisplay();
        if (entityDisplay == null) {
            return null;
        }

        Graphics2D graphics = null;
        String militarySymbol = entityDetail.getMilitarySymbol();
        Color rtcaColor = SettingsManager.getSettings().getRtcaColor(entityDetail.getRtcaState());
        int size = entityDisplay.getIconSize();
        boolean isAlternateColor = tspiData.isRecovered() || entityDetail.isOutOfComms() || entity.isTimedOut() || !entity.isInScope();
        Color markColor = entityDisplay.getMarkColor();
        String key = String.format("%s,%d,%d,%b,%b,%d",
                militarySymbol, rtcaColor.getRGB(), size, isAlternateColor,
                entityDisplay.isMarked(), markColor.getRGB());
        BufferedImage image = SINGLETON.imageMap.get(key);

        if (image != null) {
            return image;
        }

        int height = size * 2;
        if (entityDisplay.isMarked()) {
            height += 16 + 6; // add star height and addition spacing for events (todo: make constants)
        }

        try {
            int yOffset = 0;
            image = new BufferedImage(size, height, BufferedImage.TYPE_INT_ARGB);
            graphics = image.createGraphics();
            if (entityDisplay.isMarked()) {
                int x = (size - 16) / 2;
                BufferedImage highlightImage = createResourseImage("/images/star-16x16.png");
                if (highlightImage != null) {
                    if (markColor.equals(Color.WHITE)) {
                        graphics.drawImage(highlightImage, x, 0, 16, 16, null);
                    } else {
                        BufferedImage convertedImage = new BufferedImage(highlightImage.getWidth(), highlightImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        convertedImage.getGraphics().drawImage(highlightImage, 0, 0, null);
                        for (int x2 = 0; x2 < convertedImage.getWidth(); x2++) {
                            for (int y2 = 0; y2 < convertedImage.getHeight(); y2++) {
                                int currentRGB = convertedImage.getRGB(x2, y2);
                                if (currentRGB == Color.WHITE.getRGB()) {
                                    convertedImage.setRGB(x2, y2, markColor.getRGB());
                                }
                            }
                        }
                        graphics.drawImage(convertedImage, x, 0, 16, 16, null);
                    }
                    yOffset = 16 + 6;
                }
            }

            try {
                BufferedImage militarySymbolImage = SINGLETON.milStd2525IconRetriever.createIcon(militarySymbol, new AVListImpl());
                graphics.drawImage(militarySymbolImage, 0, yOffset, size - 1, size - 1, null);
            } catch (Exception e) {
                String message = INVALID_CODE + " " + militarySymbol;
                Logger.getGlobal().log(Level.WARNING, message, e);
                Color affiliationColor = SettingsManager.getSettings().getAffiliationColor(entity.getEntityDetail().getAffiliation());
                graphics = image.createGraphics();
                graphics.setColor(affiliationColor);
                graphics.fillRect(0, yOffset, size, size);
            }

            if (isAlternateColor) {
                graphics.setColor(Color.WHITE);
            } else {
                graphics.setColor(Color.BLACK);
            }

            int middle = size / 2;
            graphics.drawLine(middle, size + yOffset, middle, size * 2 + yOffset);
            graphics.fillRect(3, size + yOffset, size - 6, 3);
            graphics.setColor(rtcaColor);
            graphics.fillRect(4, size + yOffset + 1, size - 8, 1);
            SINGLETON.imageMap.put(key, image);
        } finally {
            if (graphics != null) {
                graphics.dispose();
            }
        }
        return image;
    }
}
