package gov.mil.otc._3dvis.ui.data.manager;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import java.util.List;

public class ByMissionDataSourcePane extends Tab {

    private final TabPane tabPane = new TabPane();
    private final DataManagerController dataManagerController;

    protected ByMissionDataSourcePane(List<Mission> missions, DataManagerController dataManagerController) {
        this.dataManagerController = dataManagerController;


        for (Mission mission : missions) {
            MissionDataSourceTab missionDataSourceTab = new MissionDataSourceTab(mission, dataManagerController);
            missionDataSourceTab.load();
            missionDataSourceTab.setOnCloseRequest(event -> handleCloseRequest(event, missionDataSourceTab));
            tabPane.getTabs().add(missionDataSourceTab);
        }

        setText("by mission");
        setContent(new BorderPane(tabPane));
    }

    protected void load() {
        for (Tab tab : tabPane.getTabs()) {
            if (tab instanceof ByMissionDataSourcePane) {
                ((ByMissionDataSourcePane) tab).load();
            }
        }
    }

    private void handleCloseRequest(Event event, final MissionDataSourceTab missionDataSourceTab) {
        String message = String.format("Are you sure you want to remove mission %s?%n" +
                "All data will be lost.", missionDataSourceTab.getText());
        if (!DialogUtilities.showYesNoDialog("Remove Mission", message, dataManagerController.getStage())) {
            event.consume();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(dataManagerController.getStage(), null);
        progressDialog.addStatus("Removing data, please wait...");
        progressDialog.createAndShow();

        new Thread(() -> {
            DataManager.removeMission(missionDataSourceTab.getMission());
            DataManager.reloadData();
            Platform.runLater(() -> {
                dataManagerController.loadDataSources();
                progressDialog.close();
            });
        }, "DataManager - remove data sources thread").start();
    }
}
