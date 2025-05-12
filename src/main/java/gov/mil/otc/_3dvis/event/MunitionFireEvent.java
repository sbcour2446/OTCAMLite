package gov.mil.otc._3dvis.event;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;

public class MunitionFireEvent extends RtcaEvent {

    private final EntityId shooterId;
    private final EntityId targetId;
    private final EntityType munition;
    private final int quantity;
    private FireIndicator fireIndicator;
    private Path fireLine;

    public MunitionFireEvent(long eventTime, EntityId eventId, EntityId shooterId, EntityId targetId,
                             EntityType munition, int quantity) {
        super(eventTime, eventId);
        this.shooterId = shooterId;
        this.targetId = targetId;
        this.munition = munition;
        this.quantity = quantity;
    }

    public EntityId getShooterId() {
        return shooterId;
    }

    public EntityId getTargetId() {
        return targetId;
    }

    public EntityType getMunition() {
        return munition;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Munition Fire");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(String.format("Shooter: %s", shooterId.toString()));
        stringBuilder.append(System.lineSeparator());

        if (targetId != null) {
            stringBuilder.append(String.format("Target: %s", targetId));
            stringBuilder.append(System.lineSeparator());
        }

        stringBuilder.append(String.format("Munition: %s", munition.getDescription()));
        stringBuilder.append(System.lineSeparator());

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimestamp());
        stringBuilder.append(String.format("Time: %s", simpleDateFormat.format(calendar.getTime())));

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected void setType() {
        type = "Munition Fire Event";
    }

    @Override
    public String getDescription() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimestamp());
        return String.format("Fire: %s%nTime: %s",
                munition.getDescription(), simpleDateFormat.format(calendar.getTime()));
    }

    @Override
    public void update(long time, RenderableLayer layer) {
        boolean isActive = isActive(time);
        if (isActive) {
            if (!isVisible) {
                show(layer);
            } else {
                update();
            }
        } else if (isVisible) {
            hide(layer);
        }
    }

    @Override
    public void dispose(RenderableLayer layer) {
        hide(layer);
    }

    @Override
    public Position getEventLocation() {
        IEntity shooter = EntityManager.getEntity(shooterId);
        return shooter != null ? shooter.getPositionBefore(getTimestamp()) : null;
    }

    private void createRenderables() {
        if (fireIndicator == null) {
            IEntity shooter = EntityManager.getEntity(shooterId);
            if (shooter != null && shooter.getPosition() != null) {
                fireIndicator = new FireIndicator(shooter);
                fireIndicator.setValue(AVKey.ROLLOVER_TEXT, "Munition Fire: " + munition.getDescription());

                if (targetId != null) {
                    IEntity target = EntityManager.getEntity(targetId);
                    if (target != null && target.getPosition() != null) {
                        ArrayList<Position> positionList = new ArrayList<>();
                        positionList.add(shooter.getPosition());
                        positionList.add(target.getPosition());
                        fireLine = new Path(positionList);
                        ShapeAttributes attributes = new BasicShapeAttributes();
                        attributes.setOutlineWidth(3);
                        attributes.setOutlineMaterial(Material.RED);
                        attributes.setDrawOutline(true);
                        fireLine.setAttributes(attributes);
                        fireLine.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                        fireLine.setOffset(1);

                        String rolloverText = munition.getDescription() + "\n\r";
                        rolloverText += String.format("Range: %.1f",
                                Utility.calculateDistance1(shooter.getPosition(), target.getPosition()));
                        fireLine.setValue(AVKey.ROLLOVER_TEXT, rolloverText);
                    }
                }
            }
        }
    }

    private void show(RenderableLayer layer) {
        createRenderables();
        if (fireIndicator != null) {
            layer.addRenderable(fireIndicator);
        }
        if (fireLine != null) {
            layer.addRenderable(fireLine);
        }
        isVisible = true;
    }

    private void hide(RenderableLayer layer) {
        if (fireIndicator != null) {
            layer.removeRenderable(fireIndicator);
        }
        if (fireLine != null) {
            layer.removeRenderable(fireLine);
        }
        isVisible = false;
    }

    private void update() {
        if (fireIndicator != null) {
            IEntity shooter = EntityManager.getEntity(shooterId);
            if (shooter != null && shooter.getPosition() != null) {
                fireIndicator.setPosition(shooter.getPosition());
            }
        }
    }

    private static class FireIndicator extends PointPlacemark {

        public FireIndicator(IEntity entity) {
            super(entity.getPosition());
            initialize(entity);
        }

        private void initialize(IEntity entity) {
            BufferedImage fireIndicatorImage = IconImageHelper.createFireIndicator(entity);
            activeAttributes.setLabelFont(Common.FONT_SMALL);
            activeAttributes.setLabelMaterial(Material.RED);
            activeAttributes.setLabelOffset(new Offset(fireIndicatorImage.getWidth() / 2d,
                    (double) fireIndicatorImage.getHeight(), AVKey.PIXELS, AVKey.PIXELS));
            activeAttributes.setImageColor(new Color(1f, 1f, 1f, .5f));
            activeAttributes.setImageOffset(new Offset((fireIndicatorImage.getWidth() / 2d), 0d, AVKey.PIXELS, AVKey.PIXELS));
            activeAttributes.setImage(fireIndicatorImage);

            PointPlacemarkAttributes highlightAttributes = new PointPlacemarkAttributes();
            highlightAttributes.copy(activeAttributes);
            highlightAttributes.setImageColor(new Color(1f, 1f, 1f, 1f));

            setAttributes(activeAttributes);
            setHighlightAttributes(highlightAttributes);
        }
    }
}
