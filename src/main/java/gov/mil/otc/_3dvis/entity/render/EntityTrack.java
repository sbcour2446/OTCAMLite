package gov.mil.otc._3dvis.entity.render;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.utility.SwingUtility;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

public class EntityTrack extends Path {

    private final EntityPlacemark entityPlacemark;

    private EntityTrack(EntityPlacemark entityPlacemark) {
        this.entityPlacemark = entityPlacemark;
        entityPlacemark.setEntityTrack(this);
    }

    public static EntityTrack createTrack(IEntity entity, EntityPlacemark entityPlacemark) {
        Material material = new Material(SwingUtility.toAwtColor(entity.getTrackingAttribute().getColor()));
        EntityTrack entityTrack = new EntityTrack(entityPlacemark);
        entityTrack.setPositions(entity.getTracks());
        entityTrack.setFollowTerrain(true);
        entityTrack.setDrawVerticals(true);
        entityTrack.setShowPositions(true);
        entityTrack.setExtrude(true);
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setOutlineMaterial(material);
        attrs.setInteriorMaterial(attrs.getOutlineMaterial());
        attrs.setInteriorOpacity(.2);
        attrs.setDrawOutline(true);
        attrs.setDrawInterior(entity.getTrackingAttribute().isDrawVerticals());
        attrs.setOutlineWidth(1);
        entityTrack.setAttributes(attrs);
        entityTrack.setShowPositionsScale(2.5);
        entityTrack.setDrawVerticals(entity.getTrackingAttribute().isDrawVerticals());
        entityTrack.setAltitudeMode(entity.isAircraft() || entity.isMunition() ? WorldWind.ABSOLUTE : WorldWind.CLAMP_TO_GROUND);
        return entityTrack;
    }

    public EntityPlacemark getEntityPlacemark() {
        return entityPlacemark;
    }
}
