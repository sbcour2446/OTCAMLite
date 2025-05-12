package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;

import javax.swing.*;
import java.awt.*;

public class Launch {

    private final Cone cone;
    private final long startTime;
    private final long stopTime;
    private int launchNumber;
    private Position centerPosition = Position.ZERO;
    private boolean showing = false;

    protected Launch(long startTime, int launchNumber) {
        double launchConeHeight = SettingsManager.getSettings().getDlmSetting().getLaunchConeHeight();
        double launchConeRadius = SettingsManager.getSettings().getDlmSetting().getLaunchConeRadius();
        cone = new Cone(centerPosition, launchConeHeight, launchConeRadius);
        this.startTime = startTime;
        stopTime = startTime + SettingsManager.getSettings().getDlmSetting().getTrackDisplayTime();
        this.launchNumber = launchNumber;

        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setInteriorMaterial(new Material(Color.ORANGE));
        shapeAttributes.setOutlineMaterial(new Material(Color.ORANGE));
        shapeAttributes.setInteriorOpacity(.5);
        shapeAttributes.setOutlineOpacity(.5);
        shapeAttributes.setDrawInterior(true);
        shapeAttributes.setDrawOutline(true);
        cone.setAttributes(shapeAttributes);
        cone.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        cone.setValue(AVKey.ROLLOVER_TEXT, String.format("Launch #%d", launchNumber));
        cone.setTilt(Angle.NEG180);
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public int getLaunchNumber() {
        return launchNumber;
    }

    public void setLaunchNumber(int launchNumber) {
        this.launchNumber = launchNumber;
        SwingUtilities.invokeLater(() -> cone.setValue(AVKey.ROLLOVER_TEXT, String.format("Launch #%d", launchNumber)));
    }

    protected void update(long time, Position position, boolean isFiltered) {
        if (position != null && !position.equals(centerPosition)) {
            centerPosition = position;
        }

        boolean show = isFiltered &&
                time >= startTime &&
                time < stopTime;
        if (show) {
            if (!showing) {
                showing = true;
                double launchHeight = SettingsManager.getSettings().getDlmSetting().getLaunchConeHeight();
                cone.setCenterPosition(new Position(centerPosition, launchHeight / 2));
                EntityLayer.add(cone);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(cone);
        }
    }

    protected void dispose() {
        if (showing) {
            showing = false;
            EntityLayer.remove(cone);
        }
    }
}
