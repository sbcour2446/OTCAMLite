package gov.mil.otc._3dvis.project.dlm;

import gov.mil.otc._3dvis.data.report.Report;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;

import java.io.IOException;
import java.util.List;

public class DlmLaunchReport extends Report {

    private static final String REPORT_NAME = "DLM Launch Report";
    private static final String FILE_NAME = "dlm_launch_report.csv";
    private static final String[] COLUMNS = {"timestamp", "name", "count"};

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
        for (IEntity entity : EntityManager.getEntities()) {
            if (entity instanceof IDlmEntity) {
                EntityDetail entityDetail = entity.getLastEntityDetail();
                String name = entityDetail != null ? entityDetail.getName() : entity.getEntityId().toString();
                IDlmEntity dlmEntity = (IDlmEntity) entity;
                List<Launch> launches = dlmEntity.getDlmDisplayManager().getLaunchList();
                for (Launch launch : launches) {
                    writeTimestamp(launch.getStartTime());
                    writeValue(name);
                    writeValue(String.valueOf(launch.getLaunchNumber()));
                    newLine();
                }
            }
        }
    }
}
