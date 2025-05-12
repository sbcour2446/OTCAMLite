package gov.mil.otc._3dvis.tools.rangefinder;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.base.IPositionChangeListener;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

import javax.swing.*;
import java.util.Objects;

public class RangeLine implements IPositionChangeListener {

    private final RangeFinderEntry rangeFinderEntry;
    private final IEntity sourceEntity;
    private final IEntity targetEntity;
    private final BooleanProperty showLines = new SimpleBooleanProperty(false);
    private final BooleanProperty followTerrain = new SimpleBooleanProperty(false);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>();
    private final DoubleProperty slantRange = new SimpleDoubleProperty();
    private final DoubleProperty pathDistance = new SimpleDoubleProperty();
    private Path path = null;
    private Position sourcePosition;
    private Position targetPosition;

    public RangeLine(RangeFinderEntry rangeFinderEntry, IEntity sourceEntity, IEntity targetEntity) {
        this.rangeFinderEntry = rangeFinderEntry;
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        showLines.bind(rangeFinderEntry.showLinesProperty());
        followTerrain.bind(rangeFinderEntry.followTerrainProperty());
        color.bind(rangeFinderEntry.colorProperty());
    }

    public void activate() {
        updatePosition();
        sourceEntity.addPositionChangeListener(this);
        targetEntity.addPositionChangeListener(this);
        if (showLines.get()) {
            this.path = createPath();
            RangeLineLayer.addRangeLine(this);
        }
    }

    public void dispose() {
        RangeLineLayer.removeRangeLine(this);
        sourceEntity.removePositionChangeListener(this);
        targetEntity.removePositionChangeListener(this);
    }

    public void updateEntry() {
        if (showLines.get()) {
            RangeFinderLayer.updateRangePair(this);
        } else {
            RangeFinderLayer.removeRangePair(this);
        }
    }

    public void setHighlight(boolean highlight) {
        if (path != null) {
            path.setHighlighted(highlight);
        }
    }

    public void updateAttributes() {
        if (showLines.get()) {
            if (path == null) {
                this.path = createPath();
                RangeLineLayer.addRangeLine(this);
            } else {
                SwingUtilities.invokeLater(() -> {
                    path.setSurfacePath(isFollowTerrain() && !sourceEntity.isAircraft() && !sourceEntity.isMunition() &&
                            !targetEntity.isAircraft() && !targetEntity.isMunition());
                    path.getAttributes().setOutlineMaterial(new Material(new java.awt.Color(
                            (float) getColor().getRed(),
                            (float) getColor().getGreen(),
                            (float) getColor().getBlue(),
                            (float) getColor().getOpacity())));
                });
            }
        } else {
            RangeLineLayer.removeRangeLine(this);
        }
    }

    public RangeFinderEntry getRangeFinderEntry() {
        return rangeFinderEntry;
    }

    public IEntity getSourceEntity() {
        return sourceEntity;
    }

    public IEntity getTargetEntity() {
        return targetEntity;
    }

    public RangeFinderEntity getSource() {
        return new RangeFinderEntity(sourceEntity.getEntityId());
    }

    public RangeFinderEntity getTarget() {
        return new RangeFinderEntity(targetEntity.getEntityId());
    }

    public boolean getShowLines() {
        return showLines.get();
    }

    public BooleanProperty showLinesProperty() {
        return showLines;
    }

    public void setShowLines(boolean displayOnMap) {
        this.showLines.set(displayOnMap);
    }

    public boolean isFollowTerrain() {
        return followTerrain.get();
    }

    public BooleanProperty followTerrainProperty() {
        return followTerrain;
    }

    public void setFollowTerrain(boolean followTerrain) {
        this.followTerrain.set(followTerrain);
    }

    public Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public double getSlantRange() {
        return slantRange.get();
    }

    public DoubleProperty slantRangeProperty() {
        return slantRange;
    }

    public void setSlantRange(double d) {
        this.slantRange.set(d);
    }

    public double getPathDistance() {
        return pathDistance.get();
    }

    public DoubleProperty pathDistanceProperty() {
        return pathDistance;
    }

    public void setPathDistance(double d) {
        this.pathDistance.set(d);
    }

    public Path getPath() {
        return path;
    }

    public Position getSourcePosition() {
        return sourcePosition;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    private Path createPath() {
        Path thePath = new Path();
        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setInteriorOpacity(0);
        shapeAttributes.setOutlineMaterial(new Material(new java.awt.Color(
                (float) getColor().getRed(),
                (float) getColor().getGreen(),
                (float) getColor().getBlue(),
                (float) getColor().getOpacity())));
        shapeAttributes.setOutlineStippleFactor(1);
        shapeAttributes.setOutlineWidth(1);
        thePath.setAttributes(shapeAttributes);
        thePath.setSurfacePath(isFollowTerrain() && !sourceEntity.isAircraft() && !sourceEntity.isMunition() &&
                !targetEntity.isAircraft() && !targetEntity.isMunition());
        thePath.setValue(AVKey.ROLLOVER_TEXT, String.format("%.1f (m)", getSlantRange()));
        updatePosition();

        return thePath;
    }

    private void updatePosition() {
        Position sourcePositionTemp = sourceEntity.getPosition();
        Position targetPositionTemp = targetEntity.getPosition();

        if (sourcePositionTemp != null && targetPositionTemp != null) {
            if (!sourceEntity.isAircraft() && !sourceEntity.isMunition()) {
                double elevation = WWController.getWorldWindowPanel().getModel().getGlobe().getElevation(
                        sourcePositionTemp.getLatitude(), sourcePositionTemp.getLongitude());
                sourcePositionTemp = new Position(sourcePositionTemp.getLatitude(), sourcePositionTemp.getLongitude(),
                        elevation + 1);
            }
            if (!targetEntity.isAircraft() && !targetEntity.isMunition()) {
                double elevation = WWController.getWorldWindowPanel().getModel().getGlobe().getElevation(
                        targetPositionTemp.getLatitude(), targetPositionTemp.getLongitude());
                targetPositionTemp = new Position(targetPositionTemp.getLatitude(), targetPositionTemp.getLongitude(),
                        elevation + 1);
            }
            setSlantRange(Utility.calculateDistance1(sourcePositionTemp, targetPositionTemp));
            if (path != null) {
                setPathDistance(path.getLength());
            }
        }

        sourcePosition = sourcePositionTemp;
        targetPosition = targetPositionTemp;
    }

    @Override
    public void onPositionChange(Position position) {
        updatePosition();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RangeLine rangeLine = (RangeLine) o;
        return rangeFinderEntry.equals(rangeLine.rangeFinderEntry) &&
                sourceEntity.getEntityId().equals(rangeLine.sourceEntity.getEntityId()) &&
                targetEntity.getEntityId().equals(rangeLine.targetEntity.getEntityId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(rangeFinderEntry, sourceEntity, targetEntity);
    }
}
