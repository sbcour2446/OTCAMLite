package gov.mil.otc._3dvis.ui.display;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.IMissionUpdateListener;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.*;

import java.util.List;

public class MissionWidgetPane implements IWidgetPane, IMissionUpdateListener {

    private static MissionWidgetPane instance = null;

    public static void show() {
        if (DataManager.getMissions().isEmpty()) {
            DialogUtilities.showInformationDialog("Mission List", "Mission List",
                    "No missions configured", MainApplication.getInstance().getStage());
            return;
        }
        if (instance == null) {
            instance = new MissionWidgetPane();
            WidgetPaneContainer.addWidgetPane(instance);
        }
    }

    private final VBox mainVBox = new VBox();
    private final ListView<Mission> missionListView = new ListView<>();

    private MissionWidgetPane() {
        initialize();
        DataManager.addMissionUpdateListener(this);
    }

    private void initialize() {
        Label label = new Label("no missions");
        label.setPadding(new Insets(UiConstants.SPACING));
        missionListView.setPlaceholder(label);
        missionListView.setCellFactory(timestampFileListView -> new MissionCell());
        missionListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        missionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TimeManager.setTime(newValue.getTimestamp());
            }
        });

        VBox.setVgrow(missionListView, Priority.ALWAYS);
        missionListView.setMaxHeight(Double.MAX_VALUE);
        mainVBox.getChildren().add(missionListView);

        loadMissions();
    }

    private void loadMissions() {
        missionListView.getItems().clear();
        for (Mission mission : DataManager.getMissions()) {
            missionListView.getItems().add(mission);
        }
    }

    @Override
    public String getName() {
        return "Missions";
    }

    @Override
    public Pane getPane() {
        return mainVBox;
    }

    @Override
    public void dispose() {
        instance = null;
    }

    @Override
    public void onMissionUpdate(List<Mission> missionList) {
        Platform.runLater(this::loadMissions);
    }

    private static class MissionCell extends ListCell<Mission> {

        public MissionCell() {
            getStyleClass().add("root");
            getStyleClass().add("widget-pane");
        }

        @Override
        public void updateItem(Mission mission, boolean empty) {
            super.updateItem(mission, empty);

            if (mission != null) {
                GridPane gridPane = new GridPane();
                gridPane.setHgap(UiConstants.SPACING);
                gridPane.getColumnConstraints().add(new ColumnConstraints());
                gridPane.getColumnConstraints().getFirst().setHalignment(HPos.RIGHT);

                int rowIndex = 0;

                gridPane.add(new Label("Mission:"), 0, rowIndex);
                gridPane.add(new Label(mission.getName()), 1, rowIndex);
                rowIndex++;
                gridPane.add(new Label("Start:"), 0, rowIndex);
                gridPane.add(new Label(Utility.formatTime(mission.getTimestamp(), Common.DATE_TIME_HHmm)), 1, rowIndex);
                rowIndex++;
                gridPane.add(new Label("Stop:"), 0, rowIndex);
                gridPane.add(new Label(Utility.formatTime(mission.getStopTime(), Common.DATE_TIME_HHmm)), 1, rowIndex);
                setGraphic(gridPane);
            }
        }
    }
}
