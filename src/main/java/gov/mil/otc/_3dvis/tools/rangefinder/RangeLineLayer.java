package gov.mil.otc._3dvis.tools.rangefinder;

import gov.mil.otc._3dvis.WWController;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RangeLineLayer extends RenderableLayer {

    private static final RangeLineLayer SINGLETON = new RangeLineLayer();
    private final List<RangeLine> rangeLineList = Collections.synchronizedList(new ArrayList<>());
    private boolean initialized = false;

    private RangeLineLayer() {
    }

    public static void addRangeLine(RangeLine rangeLine) {
        SINGLETON.doAddRangeLine(rangeLine);
    }

    public static void removeRangeLine(RangeLine rangeLine) {
        SINGLETON.doRemoveRangeLine(rangeLine);
    }

    private void doAddRangeLine(RangeLine rangeLine) {
        if (!initialized) {
            setName("RangeFinderLayer");
            setPickEnabled(true);
            WWController.addLayer(this);
            initialized = true;
        }

        if (!rangeLineList.contains(rangeLine)) {
            rangeLineList.add(rangeLine);
            addRenderable(rangeLine.getPath());
        }
    }

    private void doRemoveRangeLine(RangeLine rangeLine) {
        if (rangeLineList.remove(rangeLine)) {
            removeRenderable(rangeLine.getPath());
        }
        if (renderables.isEmpty()) {
            WWController.removeLayer(this);
            initialized = false;
        }
    }

    @Override
    public void preRender(DrawContext dc) {
        synchronized (rangeLineList) {
            for (RangeLine rangeLine : rangeLineList) {
                Path path = rangeLine.getPath();
                List<Position> positions = new ArrayList<>();
                positions.add(rangeLine.getSourcePosition());
                positions.add(rangeLine.getTargetPosition());
                path.setPositions(positions);
                path.setValue(AVKey.ROLLOVER_TEXT, String.format("%.1f (m)", rangeLine.getSlantRange()));
            }
        }
    }
}
