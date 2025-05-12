package gov.mil.otc._3dvis.entity.base;

import gov.mil.otc._3dvis.settings.IconType;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.utility.SwingUtility;

import java.awt.*;
import java.util.Objects;

public class EntityDisplay {

    public static final EntityDisplay DEFAULT = new EntityDisplay(
            SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.PLATFORM),
            SettingsManager.getSettings().getIconDisplay().getIconOpacity() / 100f,
            false, Color.BLACK, false, Color.WHITE);

    private final int iconSize;
    private final float iconOpacity;
    private final boolean isMarked;
    private final boolean isShowLabel;
    private final SerializableColor labelColor;
    private final SerializableColor markColor;

    public EntityDisplay(IEntity entity) {
        if (entity.isPlatform()) {
            iconSize = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.PLATFORM);
        } else if (entity.isLifeForm()) {
            iconSize = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.LIFE_FORM);
        } else if (entity.isMunition()) {
            iconSize = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.MUNITION);
        } else {
            iconSize = SettingsManager.getSettings().getIconDisplay().getIconSize(IconType.OTHER);
        }

        iconOpacity = SettingsManager.getSettings().getIconDisplay().getIconOpacity() / 100f;
        isMarked = false;
        isShowLabel = false;
        labelColor = new SerializableColor(Color.BLACK);
        markColor = new SerializableColor(Color.WHITE);
    }

    public EntityDisplay(int iconSize, float iconOpacity, boolean isShowLabel, Color labelColor, boolean isMarked,
                         Color markColor) {
        this.iconSize = iconSize;
        this.iconOpacity = iconOpacity;
        this.isMarked = isMarked;
        this.isShowLabel = isShowLabel;
        this.labelColor = new SerializableColor(labelColor);
        this.markColor = new SerializableColor(markColor);
    }

    public int getIconSize() {
        return iconSize;
    }

    public float getIconOpacity() {
        return iconOpacity;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public boolean isShowLabel() {
        return isShowLabel;
    }

    public Color getLabelColor() {
        return labelColor.getColor();
    }

    public javafx.scene.paint.Color getLabelColorrFx() {
        return SwingUtility.toFxColor(labelColor.getColor());
    }

    public Color getMarkColor() {
        return markColor.getColor();
    }

    public javafx.scene.paint.Color getMarkColorFx() {
        return SwingUtility.toFxColor(markColor.getColor());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityDisplay entityDisplay = (EntityDisplay) o;
        return entityDisplay.iconSize == iconSize
                && entityDisplay.iconOpacity == iconOpacity
                && entityDisplay.isMarked == isMarked
                && entityDisplay.isShowLabel == isShowLabel
                && entityDisplay.labelColor == labelColor
                && entityDisplay.markColor == markColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iconSize, iconOpacity, isMarked, isShowLabel, labelColor, markColor);
    }

    private static final class SerializableColor {
        private transient Color color = null;
        private final int colorRGB;

        public SerializableColor(Color color) {
            colorRGB = color.getRGB();
        }

        public SerializableColor(int colorRGB) {
            this.colorRGB = colorRGB;
            color = new Color(colorRGB);
        }

        public java.awt.Color getColor() {
            if (color == null) {
                color = new Color(colorRGB);
            }
            return color;
        }
    }
}
