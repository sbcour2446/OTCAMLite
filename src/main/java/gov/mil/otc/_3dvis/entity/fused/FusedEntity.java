package gov.mil.otc._3dvis.entity.fused;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FusedEntity {

    private final EntityId primaryEntityId;
    private final Map<EntityId, FusedLine> fusedLines = new HashMap<>();
    private final List<EntityId> entitiesToFuse = new ArrayList<>();

    public FusedEntity(IEntity primaryEntity) {
        this.primaryEntityId = primaryEntity.getEntityId();
    }

    public EntityId getPrimaryEntityId() {
        return primaryEntityId;
    }

    public void fuseEntity(IEntity entity) {
        if (!entity.getEntityId().equals(primaryEntityId)) {
            synchronized (entitiesToFuse) {
                if (!entitiesToFuse.contains(entity.getEntityId())) {
                    entitiesToFuse.add(entity.getEntityId());
                }
            }
        }
    }

    public void update(RenderableLayer layer) {
        synchronized (entitiesToFuse) {
            if (!entitiesToFuse.isEmpty()) {
                for (EntityId entityId : entitiesToFuse) {
                    if (!fusedLines.containsKey(entityId)) {
                        FusedLine fusedLine = new FusedLine(entityId);
                        fusedLines.put(entityId, fusedLine);
                        layer.addRenderable(fusedLine);
                    }
                }
                entitiesToFuse.clear();
            }
        }

        IEntity entity = EntityManager.getEntity(primaryEntityId);
        if (entity != null) {
            Position position = entity.getPosition();
            for (FusedLine fusedLine : fusedLines.values()) {
                fusedLine.update(position);
            }
        }
    }

    private class FusedLine extends Path {

        private final EntityId entityId;
        private boolean initialized = false;

        private FusedLine(EntityId entityId) {
            super();
            this.entityId = entityId;
        }

        @Override
        protected void initialize() {
            if (!initialized) {
                super.initialize();
                IEntity primaryEntity = EntityManager.getEntity(entityId);
                if (primaryEntity != null) {
                    setOffset(1);
                    setAltitudeMode(primaryEntity.isAircraft() || primaryEntity.isMunition() ? WorldWind.ABSOLUTE : WorldWind.CLAMP_TO_GROUND);
                    ShapeAttributes shapeAttributes = new BasicShapeAttributes();
                    shapeAttributes.setDrawInterior(false);
                    shapeAttributes.setDrawOutline(true);
                    shapeAttributes.setInteriorOpacity(0);
                    shapeAttributes.setOutlineMaterial(new Material(Color.CYAN));
                    shapeAttributes.setOutlineStippleFactor(1);
                    shapeAttributes.setOutlineWidth(1);
                    setAttributes(shapeAttributes);
                    setPositions(new ArrayList<>());
                    setValue(AVKey.ROLLOVER_TEXT, "Fused Entity");
                    initialized = true;
                }
            }
        }

        private void update(Position primaryPosition) {
            initialize();
            List<Position> positions = new ArrayList<>();
            IEntity entity = EntityManager.getEntity(primaryEntityId);
            if (entity != null && primaryPosition != null) {
                Position position = entity.getPosition();
                if (position != null) {
                    positions.add(primaryPosition);
                    positions.add(position);
                }
            }
            setPositions(positions);
        }
    }
}
