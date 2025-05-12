package gov.mil.otc._3dvis.ui.data.manager;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.mission.Mission;

import java.util.List;

public class MissionDataSourceTab extends DataSourceTab {

    private final Mission mission;

    protected MissionDataSourceTab(Mission mission, DataManagerController dataManagerController) {
        super(mission.getName(), dataManagerController);
        this.mission = mission;
    }

    public Mission getMission() {
        return mission;
    }

    @Override
    protected void loadDataSourceTable() {
        List<Integer> sourceIds = DataManager.getMissionDataSourceIds(mission);
        for (DataSource dataSource : DataManager.getDataSources()) {
            if (sourceIds.contains(dataSource.getId())) {
                dataSourceTableView.getItems().add(new DataSourceView(dataSource));
            }
        }
    }
}
