package gov.mil.otc._3dvis.data.report;

import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PositionReport extends Report {

    private static final String REPORT_NAME = "Position Report";
    private static final String FILE_NAME = "position_report.csv";
    private static final String[] COLUMNS = {"timestamp", "id", "name", "latitude", "longitude", "altitude", "mgrs", "status"};

    @Override
    public String getReportName() {
        return REPORT_NAME;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected String getFilename() {
        return FILE_NAME;
    }

    @Override
    protected String[] getColumns() {
        return COLUMNS;
    }

    @Override
    protected void writeData() throws IOException {
        List<Integer> sourceIds = getSourceIds();
        List<EntityId> entityIdList = getDatabase().getEntityIds();
        for (EntityId entityId : entityIdList) {
            IEntity entity = EntityManager.getEntity(entityId);
            List<TspiData> tspiDataList = getDatabase().getTspi(entityId, sourceIds);
            for (TspiData tspiData : tspiDataList) {
                if (!inDateRange(tspiData.getTimestamp())) {
                    continue;
                }
                writeTimestamp(tspiData.getTimestamp());
                writeValue(entityId);

                EntityDetail entityDetail = entity.getEntityDetailBefore(tspiData.getTimestamp());
                if (entityDetail != null) {
                    writeValue(entityDetail.getName());
                } else {
                    writeValue("");
                }

                Position position = tspiData.getPosition();
                writeValue(String.valueOf(position.getLatitude().degrees));
                writeValue(String.valueOf(position.getLongitude().degrees));
                writeValue(String.valueOf(position.getAltitude()));
                writeValue(MGRSCoord.fromLatLon(position.getLatitude(), position.getLongitude()).toString());
                newLine();
            }
        }
    }

    private List<Integer> getSourceIds() {
        List<Integer> sourceIds = new ArrayList<>();
        for (DataSource dataSource : getDatabase().getDataSources()) {
            if (dataSource.isUse()) {
                sourceIds.add(dataSource.getId());
            }
        }
        return sourceIds;
    }
}
