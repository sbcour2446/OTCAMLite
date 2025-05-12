package gov.mil.otc._3dvis.project.rpuas;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.EntityLayer;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.ShapeAttributes;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class RpuasEntity extends AbstractEntity {

    private final List<MissionConfiguration> missionConfigurationList = new ArrayList<>();
    private final TimedDataSet<ValuePairTimedData> otherData = new TimedDataSet<>();
    private MissionConfiguration currentConfiguration = null;
    private IEntity operator = null;
    private final Path operatorLine = new Path();
    private boolean showing = false;
    private Position operatorLastPosition = null;
    private ValuePairTimedData currentOtherData = null;

    public RpuasEntity(EntityId entityId) {
        super(entityId);

        ShapeAttributes shapeAttributes = new BasicShapeAttributes();
        shapeAttributes.setDrawInterior(false);
        shapeAttributes.setDrawOutline(true);
        shapeAttributes.setOutlineMaterial(Material.LIGHT_GRAY);
        shapeAttributes.setOutlineOpacity(.5);
        shapeAttributes.setOutlineStippleFactor(1);
        shapeAttributes.setOutlineWidth(5);
        operatorLine.setAttributes(shapeAttributes);
        operatorLine.setValue(AVKey.ROLLOVER_TEXT, getName());
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        boolean hasChange = super.update(time, entityFilter);

        boolean hasMissionChange = false;
        if (currentConfiguration == null || !currentConfiguration.inMission(time)) {
            hasMissionChange = updateCurrentConfiguration(time);
        }

        if (operator == null || hasMissionChange) {
            updateOperator();
        }

        Position operatorPosition = null;
        if (operator != null) {
            operatorPosition = operator.getPosition();
        }

        Position position = getPosition();
        boolean showOperatorLink = position != null && getEntityDetail() != null && !getEntityDetail().isOutOfComms()
                && isFiltered() && operatorPosition != null;

        if (showOperatorLink) {
            boolean updateOperatorLine = false;
            if (!operatorPosition.equals(operatorLastPosition)) {
                operatorLastPosition = operatorPosition;
                updateOperatorLine = true;
            }

            if (!showing) {
                showing = true;
                EntityLayer.add(operatorLine);
            }
            if (hasChange || hasMissionChange || updateOperatorLine) {
                updateOperatorLine(position, operatorPosition);
            }
        } else if (showing) {
            showing = false;
            EntityLayer.remove(operatorLine);
        }

        boolean hasStatusChange = false;
        if (otherData.updateTime(time)) {
            currentOtherData = otherData.getCurrent();
            hasStatusChange = true;
        }

        if (hasStatusChange) {
            notifyStatusListeners();
        }

        return hasChange;
    }

//    @Override
//    protected RpuasStatusAnnotation createStatusAnnotation() {
//        return new RpuasStatusAnnotation(this);
//    }


    public ValuePairTimedData getCurrentOtherData() {
        return currentOtherData;
    }

    private void updateOperatorLine(final Position position, Position operatorPosition) {
        double groundElevation = operatorPosition.elevation;
        if (WWController.getGlobe() != null) {
            groundElevation = WWController.getGlobe().getElevation(operatorPosition.latitude, operatorPosition.longitude);
        }
        final Position udpateOperatorPosition = new Position(operatorPosition, groundElevation);
        SwingUtilities.invokeLater(() -> {
            List<Position> positions = new ArrayList<>();
            positions.add(position);
            positions.add(udpateOperatorPosition);
            operatorLine.setPositions(positions);
        });
    }

    private boolean updateCurrentConfiguration(long timestamp) {
        for (MissionConfiguration missionConfiguration : missionConfigurationList) {
            if (missionConfiguration.inMission(timestamp)) {
                currentConfiguration = missionConfiguration;
                return true;
            }
        }
        currentConfiguration = null;
        return false;
    }

    private void updateOperator() {
        DeviceConfiguration deviceConfiguration = null;
        if (currentConfiguration != null) {
            deviceConfiguration = currentConfiguration.getDeviceConfiguration(getName());
        }
        if (deviceConfiguration == null) {
            operator = null;
        } else {
            for (IEntity entity : EntityManager.getEntities()) {
                if (entity.getEntityId().getApplication() == Defaults.APP_ID_TAPETS &&
                        entity.getEntityId().getId() == deviceConfiguration.getOperatorId()) {
                    operator = entity;
                    return;
                }
            }
        }
    }

    public void addMissionConfiguration(MissionConfiguration missionConfiguration) {
        missionConfigurationList.add(missionConfiguration);
    }

    public void addValuePairTimedData(List<ValuePairTimedData> data) {
        otherData.addAll(data);
    }
}