package gov.mil.otc._3dvis.settings;

import com.google.gson.annotations.SerializedName;
import gov.mil.otc._3dvis.entity.base.IEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconDisplay {

    @SerializedName("icon opacity")
    private Integer iconOpacity;

    @SerializedName("icon size")
    private final Map<IconType, Integer> iconSizeMap = new ConcurrentHashMap<>();

    private boolean showLineToTerrain;

    public int getIconOpacity() {
        if (iconOpacity == null) {
            iconOpacity = Defaults.ICON_OPACITY;
        }
        return iconOpacity;
    }

    public void setIconOpacity(int iconOpacity) {
        this.iconOpacity = iconOpacity;
    }

    public int getIconSize(IconType iconType) {
        return iconSizeMap.computeIfAbsent(iconType, k -> iconType.getDefaultSize());
    }

    public int getIconSize(IEntity entity) {
        return getIconSize(entity.getIconType());
    }

    public void setIconSize(IconType iconType, int size) {
        if (size >= iconType.minimumSize && size <= iconType.maximumSize) {
            iconSizeMap.put(iconType, size);
        }
    }

    public boolean isShowLineToTerrain() {
        return showLineToTerrain;
    }

    public void setShowLineToTerrain(boolean showLineToTerrain) {
        this.showLineToTerrain = showLineToTerrain;
    }
}
