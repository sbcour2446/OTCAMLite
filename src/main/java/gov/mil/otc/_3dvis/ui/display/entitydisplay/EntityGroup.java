package gov.mil.otc._3dvis.ui.display.entitydisplay;

import gov.mil.otc._3dvis.entity.base.EntityDisplay;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.utility.SwingUtility;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EntityGroup {

    private String name = "";
    private final List<IEntity> entityList = new ArrayList<>();
    private EntityDisplay entityDisplay = EntityDisplay.DEFAULT;

    public EntityGroup(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addEntity(IEntity entity) {
        if (!entityList.contains(entity)) {
            entityList.add(entity);
        }
    }

    public void removeEntity(IEntity entity) {
        entityList.remove(entity);
    }

    public List<IEntity> getEntityList() {
        return new ArrayList<>(entityList);
    }

    public EntityDisplay getEntityDisplay() {
        return entityDisplay;
    }

    public void setEntityDisplay(EntityDisplay entityDisplay) {
        this.entityDisplay = entityDisplay;
    }

    public void setMarked(boolean marked) {
        if (entityDisplay.isMarked() != marked) {
            entityDisplay = new EntityDisplay(entityDisplay.getIconSize(), entityDisplay.getIconOpacity(),
                    entityDisplay.isShowLabel(), entityDisplay.getLabelColor(),
                    marked, entityDisplay.getMarkColor());
        }
    }

    public void setMarkColor(Color markColor) {
        java.awt.Color color = SwingUtility.toAwtColor(markColor);
        if (!Objects.equals(entityDisplay.getMarkColor(), color)) {
            entityDisplay = new EntityDisplay(entityDisplay.getIconSize(), entityDisplay.getIconOpacity(),
                    entityDisplay.isShowLabel(), entityDisplay.getLabelColor(),
                    entityDisplay.isMarked(), color);
        }
    }

    public void setShowLabal(boolean showLabal) {
        if (entityDisplay.isShowLabel() != showLabal) {
            entityDisplay = new EntityDisplay(entityDisplay.getIconSize(), entityDisplay.getIconOpacity(),
                    showLabal, entityDisplay.getLabelColor(),
                    entityDisplay.isMarked(), entityDisplay.getMarkColor());
        }
    }

    public void setLabelColor(Color labelColor) {
        java.awt.Color color = SwingUtility.toAwtColor(labelColor);
        if (!Objects.equals(entityDisplay.getLabelColor(), color)) {
            entityDisplay = new EntityDisplay(entityDisplay.getIconSize(), entityDisplay.getIconOpacity(),
                    entityDisplay.isShowLabel(), color,
                    entityDisplay.isMarked(), entityDisplay.getMarkColor());
        }
    }

    public void setIconOpacity(float opacity) {
        if (entityDisplay.getIconOpacity() != opacity) {
            entityDisplay = new EntityDisplay(entityDisplay.getIconSize(), opacity,
                    entityDisplay.isShowLabel(), entityDisplay.getLabelColor(),
                    entityDisplay.isMarked(), entityDisplay.getMarkColor());
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityGroup entityGroup = (EntityGroup) o;
        return entityGroup.getName().equalsIgnoreCase(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
