package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.event.Event;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class CcmEvent extends Event {

    private final Path ccmLine = new Path();
    private final Position startPosition;
    private final Position endPosition;
    private final double azimuth;
    private final double elevation;
    private final double range;
    private final EmitterType emitterType;
    private final AcquisitionType acquisitionType;
    private EntityId entityId;

    public CcmEvent(long eventTime, long endTime, Position startPosition, Position endPosition,
                    double azimuth, double elevation, double range, EmitterType emitterType,
                    AcquisitionType acquisitionType) {
        this(eventTime, endTime, startPosition, endPosition, azimuth, elevation, range,
                emitterType, acquisitionType, null);
    }

    public CcmEvent(long eventTime, long endTime, Position startPosition, Position endPosition,
                    double azimuth, double elevation, double range, EmitterType emitterType,
                    AcquisitionType acquisitionType, EntityId entityId) {
        super(eventTime, endTime);

        this.entityId = entityId;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.range = range;
        this.emitterType = emitterType;
        this.acquisitionType = acquisitionType;

        ccmLine.setPositions(List.of(startPosition, endPosition));
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setOutlineMaterial(getMaterial(emitterType));
        shapeAttributes.setOutlineStippleFactor(getStippleFactor(emitterType, acquisitionType));
        String rollover = emitterType.toString() + " " + acquisitionType.getDescription();
        ccmLine.setAttributes(shapeAttributes);
        ccmLine.setValue(AVKey.ROLLOVER_TEXT, getDescription());
    }

    public void setEntityId(EntityId entityId) {
        this.entityId = entityId;
    }

    public EntityId getEntityId() {
        return entityId;
    }

    public Position getStartPosition() {
        return startPosition;
    }

    public Position getEndPosition() {
        return endPosition;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getElevation() {
        return elevation;
    }

    public double getRange() {
        return range;
    }

    public EmitterType getEmitterType() {
        return emitterType;
    }

    public AcquisitionType getAcquisitionType() {
        return acquisitionType;
    }

    private Material getMaterial(EmitterType emitterType) {
        return switch (emitterType) {
            case LAUNCH -> Material.YELLOW;
            case LASER -> Material.RED;
            case RADAR -> Material.CYAN;
        };
    }

    private int getStippleFactor(EmitterType emitterType, AcquisitionType acquisitionType) {
        if (emitterType == EmitterType.RADAR) {
            return switch (acquisitionType) {
                case SEARCH -> 5;
                case TRACK -> 2;
                case MG, NA -> 0;
            };
        } else {
            return 0;
        }
    }

    @Override
    protected void setType() {
        type = "CCM Event";
    }

    @Override
    public String getDescription() {
        String description = emitterType.getDescription();
        Calendar timeOn = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        timeOn.setTimeInMillis(getTimestamp());
        Calendar timeOff = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        timeOff.setTimeInMillis(endTime);

        if (emitterType != EmitterType.LAUNCH) {
            description = String.format("%s %s%nTime On: %s%nTime Off: %s", description, acquisitionType.getDescription(),
                    simpleDateFormat.format(timeOn.getTime()), simpleDateFormat.format(timeOff.getTime()));
        } else {
            description = String.format("%s%nTime: %s", description, simpleDateFormat.format(timeOn.getTime()));
        }
        return description;
    }

    @Override
    public void update(long time, RenderableLayer layer) {
        boolean isActive = isActive(time);
        if (isActive) {
            if (!isVisible) {
                layer.addRenderable(ccmLine);
                isVisible = true;
            }
        } else if (isVisible) {
            layer.removeRenderable(ccmLine);
            isVisible = false;
        }
    }

    @Override
    public void dispose(RenderableLayer layer) {
        layer.removeRenderable(ccmLine);
    }
}
