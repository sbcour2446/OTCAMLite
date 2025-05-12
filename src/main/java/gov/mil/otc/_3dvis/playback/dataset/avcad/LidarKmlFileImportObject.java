package gov.mil.otc._3dvis.playback.dataset.avcad;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityScope;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.project.avcad.LidarEntity;
import gov.mil.otc._3dvis.project.avcad.LidarKmlFile;
import gov.mil.otc._3dvis.project.avcad.LidarPlacemark;
import gov.mil.otc._3dvis.project.avcad.LidarScan;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LidarKmlFileImportObject extends ImportObject<LidarKmlFile> {

    public static LidarKmlFileImportObject scanAndCreate(File file) {
        LidarKmlFileImportObject importObject = new LidarKmlFileImportObject(new LidarKmlFile(file), file.getName());
        if (importObject.determineIsNew()) {
            importObject.getObject().process();
        }
        return importObject;
    }

    public LidarKmlFileImportObject(LidarKmlFile object, String name) {
        super(object, name);
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        if ((!isNew() && !isModified()) || isMissing()) {
            return;
        }

        LidarEntity entity = getOrCreateEntity(getObject().getSystemName());
//        DataSource dataSource = DataManager.createDataSource(getObject().getAbsolutePath(),
//                getObject().getTimeSpan().getStartTime(), getObject().getTimeSpan().getStopTime());

        List<LidarKmlFile.ScanData> scanDatas = getObject().getScanDataList();
        for (int i = 0; i < scanDatas.size(); i++) {
            LidarKmlFile.ScanData scanData = scanDatas.get(i);
            LidarScan lidarScan = new LidarScan(i, scanData.getTimeSpan().getStartTime(), scanData.getTimeSpan().getStopTime());

            List<LidarKmlFile.Placemark> placemarks = scanData.getPlacemarkList();
            for (int j = 0; j < placemarks.size(); j++) {
                LidarKmlFile.Placemark placemark = placemarks.get(j);
                LidarPlacemark lidarPlacemark = new LidarPlacemark(j, placemark.getHeightOffset());
                lidarPlacemark.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                if (placemark.getOuterBoundaryList().size() > 2) {
                    lidarPlacemark.setOuterBoundary(placemark.getOuterBoundaryList());
                } else {
                    Logger.getGlobal().log(Level.WARNING, "LidarKmlFileImportObject::doImport: no outer boundary");
                }
                if (placemark.getInnerBoundaryList().size() > 2) {
                    lidarPlacemark.addInnerBoundary(placemark.getInnerBoundaryList());
                }

                Color color = Color.BLACK;
                try {
                    //#aabbggrr
                    String colorString = placemark.getStyle();
                    int alphaValue = 0;
                    int blueValue = 0;
                    int greenValue = 0;
                    int redValue = 0;
                    if (colorString.length() == 8) {
                        String alpha = colorString.substring(0, 2);
                        alphaValue = Integer.parseInt(alpha, 16);
                        String blue = colorString.substring(2, 4);
                        blueValue = Integer.parseInt(blue, 16);
                        String green = colorString.substring(4, 6);
                        greenValue = Integer.parseInt(green, 16);
                        String red = colorString.substring(6, 8);
                        redValue = Integer.parseInt(red, 16);
                    }
                    color = new Color(redValue, greenValue, blueValue, alphaValue);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "LidarKmlFileImportObject::doImport", e);
                }
                ShapeAttributes shapeAttributes = new BasicShapeAttributes();
                shapeAttributes.setInteriorMaterial(new Material(color));
                shapeAttributes.setOutlineMaterial(new Material(color));
                shapeAttributes.setOutlineWidth(2);
//                shapeAttributes.setInteriorOpacity(.5);
//                shapeAttributes.setOutlineOpacity(.5);
                lidarPlacemark.setAttributes(shapeAttributes);

                lidarScan.addPlacemark(lidarPlacemark);
            }

            entity.addLidarScan(lidarScan);
//            DataManager.addLidarScan(lidarScan, entity.getEntityId(), dataSource.getId());
        }

        addEntityDetails(entity, getObject().getSystemName(), getObject().getTimeSpan().getStartTime(),
                getObject().getTimeSpan().getStopTime());
        TspiData tspiData = new TspiData(getObject().getTimeSpan().getStartTime(), getObject().getLidarPosition());
        entity.addTspi(tspiData);
//        DatabaseLogger.addTspiData(tspiData, entity.getEntityId(), dataSource.getId());

        setImported(true);
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    private LidarEntity getOrCreateEntity(String entityName) {
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof LidarEntity && entityName.equals(entity.getLastEntityDetail().getName())) {
                return (LidarEntity) entity;
            }
        }

        EntityId entityId = DataManager.getNextGenericEntityId();
        LidarEntity entity = new LidarEntity(entityId);
        EntityManager.addEntity(entity, false);
        return entity;
    }

    private void addEntityDetails(IEntity entity, String name, long startTime, long stopTime) {
        EntityDetail entityDetail = new EntityDetail.Builder()
                .setTimestamp(startTime)
                .setName(getObject().getSystemName())
                .setSource(name)
                .setAffiliation(Affiliation.NONPARTICIPANT)
                .build();
        entity.addEntityDetail(entityDetail);
//        DatabaseLogger.addEntityDetail(entityDetail, entity.getEntityId(), dataSource.getId());

        EntityScope entityScope = new EntityScope(startTime, stopTime);
        entity.addEntityScope(entityScope);
//        DatabaseLogger.addEntityScope(entityScope, entity.getEntityId(), dataSource.getId());
    }
}
