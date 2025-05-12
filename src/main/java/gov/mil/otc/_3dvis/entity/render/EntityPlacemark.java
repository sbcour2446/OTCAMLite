package gov.mil.otc._3dvis.entity.render;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.utility.SwingUtility;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.awt.image.BufferedImage;

public class EntityPlacemark extends PointPlacemark {

    private IEntity entity;
    private EntityTrack entityTrack;

    public EntityPlacemark(IEntity entity) {
        super(entity.getPosition());

        this.entity = entity;
        initialize();
    }

    public void reset() {
        initialize();
    }

    public IEntity getEntity() {
        return entity;
    }

    public void setEntity(IEntity entity) {
        this.entity = entity;
    }

    public EntityTrack getEntityTrack() {
        return entityTrack;
    }

    public void setEntityTrack(EntityTrack entityTrack) {
        this.entityTrack = entityTrack;
    }

    private void initialize() {
        float opacity = SettingsManager.getSettings().getIconDisplay().getIconOpacity() / 100f;
        int size = SettingsManager.getSettings().getIconDisplay().getIconSize(entity);
        if (entity.getEntityDisplay() != null) {
            opacity = entity.getEntityDisplay().getIconOpacity();
            size = entity.getEntityDisplay().getIconSize();
        }

        PointPlacemarkAttributes attributes = new PointPlacemarkAttributes();
        attributes.setLineMaterial(new Material(new Color(1f, 1f, 1f, .5f)));
        attributes.setLineWidth(1d);
        attributes.setScale(1.0);
        attributes.setImageColor(new Color(1, 1, 1, opacity));
        attributes.setLabelFont(Common.FONT_XLARGE);
        attributes.setLabelMaterial(Material.DARK_GRAY);
        attributes.setImageOffset(new Offset((size / 2d), 0d, AVKey.PIXELS, AVKey.PIXELS));
        attributes.setLabelScale(.75);
        setAttributes(attributes);

        PointPlacemarkAttributes highlightAttributes = new PointPlacemarkAttributes();
        highlightAttributes.copy(attributes);
        highlightAttributes.setImageColor(new Color(1, 1, 1, 1f));
        highlightAttributes.setLabelFont(Common.FONT_XLARGE);
        highlightAttributes.setLabelMaterial(Material.YELLOW);
        setHighlightAttributes(highlightAttributes);

        setAlwaysOnTop(true);

        setLineEnabled(SettingsManager.getSettings().getIconDisplay().isShowLineToTerrain());

        updateAltitudeMode();
//        updateHighlighted();
        updateLabel();
        updateRolloverText();
        updateTexture();
    }

    private void updateAltitudeMode() {
        if (entity.isMunition() || entity.isAircraft()) {
            setAltitudeMode(WorldWind.ABSOLUTE);
        } else {
            setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        }
    }

    public void updateLabel() {
        boolean showLabel = entity.getEntityDisplay().isShowLabel();
        String label = "";
        if (showLabel) {
            label = entity.getName();
            PointPlacemarkAttributes attributes = getAttributes();
            Color color = entity.getEntityDisplay().getLabelColor();
            attributes.setLabelMaterial(new Material(color));
            setAttributes(attributes);
        }
        setLabelText(label);
    }

    private void updateRolloverText() {
        String label = entity.getEntityId().toString() + System.lineSeparator();
        EntityDetail entityDetail = entity.getEntityDetail();
        if (entityDetail != null) {
            label += entityDetail.getName().isBlank() ? "" : (entityDetail.getName() + System.lineSeparator());
            if (!entityDetail.getEntityType().isUnknown()) {
                label += entityDetail.getEntityType().getDescription() + System.lineSeparator();
            }
//            label += entityDetail.getRtcaState() == null ? "" : entityDetail.getRtcaState().toString();
        }
        setValue(AVKey.ROLLOVER_TEXT, label);
    }

    private void updateTexture() {
        float opacity = SettingsManager.getSettings().getIconDisplay().getIconOpacity() / 100f;
        int size = SettingsManager.getSettings().getIconDisplay().getIconSize(entity);
        if (entity.getEntityDisplay() != null) {
            opacity = entity.getEntityDisplay().getIconOpacity();
            size = entity.getEntityDisplay().getIconSize();
        }

        if (entity.getEntityDetail() == null || entity.getEntityDetail().isOutOfComms() || entity.isTimedOut() ||
                !entity.isInScope()) {
            opacity *= .5f;
        }

        getAttributes().setImageColor(new Color(1, 1, 1, opacity));
        getAttributes().setImageOffset(new Offset((size / 2d), 0d, AVKey.PIXELS, AVKey.PIXELS));

        BufferedImage image = entity.createIcon();
        if (image != null) {
            BasicWWTexture texture = new BasicWWTexture(image, false);
            textures.put("image", texture);
        }
    }

    @Override
    protected WWTexture chooseTexture(PointPlacemarkAttributes attrs) {
        WWTexture texture = this.textures.get("image");
        if (texture != null) {
            return texture;
        }

        return super.chooseTexture(attrs);
    }
}
