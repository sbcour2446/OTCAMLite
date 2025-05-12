package gov.mil.otc._3dvis.project.avcad;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Polygon;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LidarPlacemark extends Polygon {

    private final int cloudId;
    private final double heightOffset;
    private final List<Position> originalOuterBoundary = new ArrayList<>();
    private final List<Position> originalInnerBoundary = new ArrayList<>();

    public LidarPlacemark(int cloudId, double heightOffset) {
        this.cloudId = cloudId;
        this.heightOffset = heightOffset;
    }

    public int getCloudId() {
        return cloudId;
    }

    public int getColorValue() {
        return getAttributes() != null ? getAttributes().getInteriorMaterial().getDiffuse().getRGB() : 0;
    }

    public void setOverrideHeight(boolean overrideHeight, double height) {
        final List<Position> newInnerBoundary;
        final List<Position> newOuterBoundary;
        if (overrideHeight) {
            newInnerBoundary = new ArrayList<>();
            newOuterBoundary = new ArrayList<>();
            for (Position position : originalInnerBoundary) {
                newInnerBoundary.add(new Position(position, height + heightOffset));
            }
            for (Position position : originalOuterBoundary) {
                newOuterBoundary.add(new Position(position, height + heightOffset));
            }
        } else {
            newInnerBoundary = originalInnerBoundary;
            newOuterBoundary = originalOuterBoundary;
        }
        SwingUtilities.invokeLater(() -> {
            super.setOuterBoundary(newOuterBoundary);
            if (!newInnerBoundary.isEmpty()) {
                //getBoundaries().set(1, newInnerBoundary);
            }
        });
    }

    @Override
    public void setOuterBoundary(Iterable<? extends Position> corners) {
        super.setOuterBoundary(corners);
        originalOuterBoundary.clear();
        for (Position position : corners) {
            originalOuterBoundary.add(position);
        }
    }

    @Override
    public void addInnerBoundary(Iterable<? extends Position> corners) {
        //super.addInnerBoundary(corners);
        originalInnerBoundary.clear();
        for (Position position : corners) {
            originalInnerBoundary.add(position);
        }
    }
}
