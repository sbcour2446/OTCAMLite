package gov.mil.otc._3dvis.event;

import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.util.Calendar;

public class MunitionDetonationEvent extends RtcaEvent {

    private final Position impactPosition;
    private final EntityType munition;
    private int radius;
    private Ellipsoid detonationIndicator;

    public MunitionDetonationEvent(long eventTime, EntityId eventId, Position impactPosition, EntityType munition, int radius) {
        super(eventTime, eventId);
        this.impactPosition = impactPosition;
        this.munition = munition;
        this.radius = radius;
    }

    public Position getImpactPosition() {
        return impactPosition;
    }

    public EntityType getMunition() {
        return munition;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Munition Detonation");
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append(String.format("Munition: %s", munition.getDescription()));
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append(String.format("Impact: %.4f, %.4f, %.0fm", impactPosition.getLatitude().degrees,
                impactPosition.getLongitude().degrees, impactPosition.getElevation()));

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
        type = "Munition Detonation Event";
    }

    @Override
    public String getDescription() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimestamp());
        return String.format("Detonation: %s%nTime: %s",
                munition.getDescription(), simpleDateFormat.format(calendar.getTime()));
    }

    @Override
    public void update(long time, RenderableLayer layer) {
        boolean isActive = isActive(time);
        if (isActive && !isVisible) {
            if (impactPosition != null) {
                if (detonationIndicator == null) {
                    if (radius <= 0) {
                        radius = 5;
                    }
                    ShapeAttributes attrs = new BasicShapeAttributes();
                    attrs.setInteriorMaterial(Material.RED);
                    attrs.setInteriorOpacity(.35);
                    attrs.setDrawOutline(false);

                    detonationIndicator = new Ellipsoid(impactPosition, radius, radius * 2.0 / 3.0, radius);
                    detonationIndicator.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    detonationIndicator.setAttributes(attrs);
                    detonationIndicator.setValue(AVKey.ROLLOVER_TEXT, munition.getDescription());
                }
                layer.addRenderable(detonationIndicator);
                isVisible = true;
            }
        } else if (!isActive && isVisible) {
            if (detonationIndicator != null) {
                layer.removeRenderable(detonationIndicator);
            }
            isVisible = false;
        }
    }

    @Override
    public void dispose(RenderableLayer layer) {
        if (detonationIndicator != null) {
            layer.removeRenderable(detonationIndicator);
        }
    }

    @Override
    public Position getEventLocation() {
        return getImpactPosition();
    }
}
