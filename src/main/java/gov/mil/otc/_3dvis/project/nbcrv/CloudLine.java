package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.data.oadms.WdlReading;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.sin;

public class CloudLine {

    private final double azimuth;
    private final List<Renderable> cloudRenderables;

    public CloudLine(WdlReading wdlReading, Position position) {
        this.azimuth = wdlReading.getAzimuth();
        cloudRenderables = createCloudRenderables(wdlReading, position);
    }

    public double getAzimuth() {
        return azimuth;
    }

    public List<Renderable> getCloudRenderables() {
        return cloudRenderables;
    }

    private List<Renderable> createCloudRenderables(WdlReading wdlReading, Position position) {
        List<Renderable> renderableList = new ArrayList<>();
        for (WdlReading.WdlTile wdlTile : wdlReading.getWdlTileList()) {
            double height1 = 3;
            double height2 = 3;
            if (SettingsManager.getSettings().getNbcrvSettings().isUsePitch()) {
                height1 += sin(wdlReading.getElevation() * 0.0174533) * wdlTile.range1 * 6371000.0;
                height2 += sin(wdlReading.getElevation() * 0.0174533) * wdlTile.range2 * 6371000.0;
            }
            Position position1 = new Position(LatLon.greatCircleEndPosition(position,
                    Angle.fromDegrees(wdlReading.getAzimuth() + .25), Angle.fromRadians(wdlTile.range1)), height1);
            Position position2 = new Position(LatLon.greatCircleEndPosition(position,
                    Angle.fromDegrees(wdlReading.getAzimuth() + .25), Angle.fromRadians(wdlTile.range2)), height2);
            Position position3 = new Position(LatLon.greatCircleEndPosition(position,
                    Angle.fromDegrees(wdlReading.getAzimuth() - .25), Angle.fromRadians(wdlTile.range2)), height2);
            Position position4 = new Position(LatLon.greatCircleEndPosition(position,
                    Angle.fromDegrees(wdlReading.getAzimuth() - .25), Angle.fromRadians(wdlTile.range1)), height1);


            Polygon polygon = new Polygon();
            polygon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            ShapeAttributes shapeAttributes = new BasicShapeAttributes();
            shapeAttributes.setInteriorMaterial(new Material(Color.getHSBColor(wdlTile.hue, 1.0f, 1.0f)));
            shapeAttributes.setInteriorOpacity(.9);
            shapeAttributes.setDrawOutline(false);
            polygon.setAttributes(shapeAttributes);
            polygon.setOuterBoundary(List.of(position1, position2, position3, position4));
            renderableList.add(polygon);
        }
        return renderableList;
    }
//
//    private List<Renderable> createCloudRenderables(WdlReading wdlReading, Position position) {
//        List<Renderable> renderableList = new ArrayList<>();
//        for (int i = 0; i < wdlReading.getHueValueArray().length; i++) {
//            float hue = wdlReading.getHueValueArray()[i];
//            if (hue > 0) {
//                double range1 = wdlReading.getResolution() * i / 6371000.0;
//                double range2 = wdlReading.getResolution() * (i + 1) / 6371000.0;
//                double height1 = 3;
//                double height2 = 3;
//                if (SettingsManager.getSettings().getNbcrvSettings().isUsePitch()) {
//                    height1 = sin(wdlReading.getElevation() * 0.0174533) * wdlReading.getResolution() * i;
//                    height2 = sin(wdlReading.getElevation() * 0.0174533) * wdlReading.getResolution() * (i + 1);
//                }
//                Position position1 = new Position(LatLon.greatCircleEndPosition(position,
//                        Angle.fromDegrees(wdlReading.getAzimuth() + .15), Angle.fromRadians(range1)), height1);
//                Position position2 = new Position(LatLon.greatCircleEndPosition(position,
//                        Angle.fromDegrees(wdlReading.getAzimuth() + .15), Angle.fromRadians(range2)), height2);
//                Position position3 = new Position(LatLon.greatCircleEndPosition(position,
//                        Angle.fromDegrees(wdlReading.getAzimuth() - .15), Angle.fromRadians(range2)), height2);
//                Position position4 = new Position(LatLon.greatCircleEndPosition(position,
//                        Angle.fromDegrees(wdlReading.getAzimuth() - .15), Angle.fromRadians(range1)), height1);
//
//
//                Polygon polygon = new Polygon();
//                polygon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
//                ShapeAttributes shapeAttributes = new BasicShapeAttributes();
//                shapeAttributes.setInteriorMaterial(new Material(Color.getHSBColor(hue / 360, 1.0f, 1.0f)));
//                shapeAttributes.setInteriorOpacity(.9);
//                shapeAttributes.setDrawOutline(false);
//                polygon.setAttributes(shapeAttributes);
//                polygon.setOuterBoundary(List.of(position1, position2, position3, position4));
//                renderableList.add(polygon);
//
////                Path path = new Path(List.of(position1, position2));
////                if (SettingsManager.getSettings().getNbcrvSettings().isUsePitch()) {
////                    path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
////                } else {
////                    path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
////                    path.setOffset(3);
////                }
////                ShapeAttributes shapeAttributes = new BasicShapeAttributes();
////                shapeAttributes.setInteriorMaterial(new Material(Color.getHSBColor(hue / 360, 1.0f, 1.0f)));
////                shapeAttributes.setOutlineMaterial(new Material(Color.getHSBColor(hue / 360, 1.0f, 1.0f)));
////                shapeAttributes.setOutlineWidth(10);
////                path.setAttributes(shapeAttributes);
////                renderableList.add(path);
//            }
//        }
//        return renderableList;
//    }
}
