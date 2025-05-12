package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.datamodel.timed.TimedDataSet;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;

import java.util.List;

public class BlackHawkEntity extends AbstractEntity {

    private final TimedDataSet<FlightData> flightDataTimedDataSet = new TimedDataSet<>();

    public BlackHawkEntity(EntityId entityId) {
        super(entityId);
    }

    public void addFlightData(FlightData flightData) {
        flightDataTimedDataSet.add(flightData);
    }

    public void addFlightDataList(List<FlightData> flightDataList) {
        flightDataTimedDataSet.addAll(flightDataList);
    }

    public FlightData getFlightData() {
        return flightDataTimedDataSet.getCurrent();
    }

    @Override
    public boolean update(long time, EntityFilter entityFilter) {
        boolean hasChange = super.update(time, entityFilter);
        boolean hasFlightDataChange = flightDataTimedDataSet.updateTime(time);

        if (hasFlightDataChange) {
            updateStatusDisplay();
            checkWheelStatusChange();
        }

        return hasChange || hasFlightDataChange;
    }

    @Override
    protected StatusAnnotation createStatusAnnotation() {
        return new BlackHawkStatusAnnotation(this);
    }

    private void checkWheelStatusChange() {
        FlightData flightData = getFlightData();
        if (flightData != null) {
            setClampToGround(flightData.getWheelStatus().equalsIgnoreCase("WEIGHT ON"));
        }
    }
}
