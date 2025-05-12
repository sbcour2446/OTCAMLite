package gov.mil.otc._3dvis.tools.rangefinder;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RangeFinderLayer extends RenderableLayer {

    private static final RangeFinderLayer SINGLETON = new RangeFinderLayer();
    private final ConcurrentHashMap<RangeLine, Path> pathMap = new ConcurrentHashMap<>();
    private boolean initialized = false;

    private RangeFinderLayer() {
    }

    public static void addRangePair(RangeLine rangeLine) {
        SINGLETON.doAddRangePair(rangeLine);
    }

    public static void updateRangePair(RangeLine rangeLine) {
        SINGLETON.doUpdateRangePair(rangeLine);
    }

    public static void removeRangePair(RangeLine rangeLine) {
        SINGLETON.doRemoveRangePair(rangeLine);
    }

    private void doAddRangePair(RangeLine rangeLine) {
        Path path = new Path();
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setInteriorOpacity(0);
        shapeAttributes.setOutlineMaterial(new Material(new Color(
                (float) rangeLine.getColor().getRed(),
                (float) rangeLine.getColor().getGreen(),
                (float) rangeLine.getColor().getBlue(),
                (float) rangeLine.getColor().getOpacity())));
        shapeAttributes.setOutlineStippleFactor(1);
        shapeAttributes.setOutlineWidth(1);
        path.setAttributes(shapeAttributes);
        path.setFollowTerrain(rangeLine.isFollowTerrain());
        path.setPositions(getPositions(rangeLine));
        path.setValue(AVKey.ROLLOVER_TEXT, String.format("%.1f (m)", rangeLine.getSlantRange()));

        pathMap.put(rangeLine, path);
        addRenderable(path);
        if (!initialized) {
            setName("RangeFinderLayer");
            setPickEnabled(true);
            WWController.addLayer(this);
            initialized = true;
        }
    }

    private void doUpdateRangePair(RangeLine rangeLine) {
        Path path = pathMap.get(rangeLine);
        if (path == null) {
            doAddRangePair(rangeLine);
        } else {
            path.setPathType(AVKey.GREAT_CIRCLE);
            path.setFollowTerrain(rangeLine.isFollowTerrain());
            path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            path.getAttributes().setOutlineMaterial(new Material(new Color(
                    (float) rangeLine.getColor().getRed(),
                    (float) rangeLine.getColor().getGreen(),
                    (float) rangeLine.getColor().getBlue(),
                    (float) rangeLine.getColor().getOpacity())));
        }
    }

    private void doRemoveRangePair(RangeLine rangeLine) {
        Path path = pathMap.remove(rangeLine);
        if (path != null) {
            removeRenderable(path);
        }
        if (renderables.isEmpty()) {
            WWController.removeLayer(this);
            initialized = false;
        }
    }

    private List<Position> getPositions(RangeLine rangeLine) {
        List<Position> positions = new ArrayList<>();
        IEntity sourceEntity = rangeLine.getSourceEntity();
        IEntity targetEntity = rangeLine.getTargetEntity();
        Position sourcePosition = sourceEntity.getPosition();
        Position targetPosition = targetEntity.getPosition();
        if (sourcePosition == null || targetPosition == null) {
            return positions;
        }
        if (sourceEntity.isAircraft() || sourceEntity.isMunition()) {
            positions.add(sourcePosition);
        } else {
            double elevation = WWController.getWorldWindowPanel().getModel().getGlobe().getElevation(sourcePosition.getLatitude(),
                    sourcePosition.getLongitude());
            positions.add(new Position(sourcePosition.getLatitude(), sourcePosition.getLongitude(), elevation + 2));
        }
        if (targetEntity.isAircraft() || targetEntity.isMunition()) {
            positions.add(targetPosition);
        } else {
            double elevation = WWController.getWorldWindowPanel().getModel().getGlobe().getElevation(targetPosition.getLatitude(),
                    targetPosition.getLongitude());
            positions.add(new Position(targetPosition.getLatitude(), targetPosition.getLongitude(), elevation + 2));
        }
        return positions;
    }

    @Override
    public void preRender(DrawContext dc) {
        synchronized (pathMap) {
            for (Map.Entry<RangeLine, Path> entry : pathMap.entrySet()) {
                List<Position> positions = new ArrayList<>();
                positions.add(entry.getKey().getSourcePosition());
                positions.add(entry.getKey().getTargetPosition());
                entry.getValue().setPositions(positions);
                entry.getValue().setValue(AVKey.ROLLOVER_TEXT, String.format("%.1f (m)", entry.getKey().getSlantRange()));
            }
        }
    }
}
