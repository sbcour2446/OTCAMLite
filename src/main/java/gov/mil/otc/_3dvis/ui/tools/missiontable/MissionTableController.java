package gov.mil.otc._3dvis.ui.tools.missiontable;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class MissionTableController {

    private static final MissionTableController missionTableController = new MissionTableController();
    private final Stage stage = new Stage();
    private final TableView<MissionView> missionTableView = new TableView<>();

    public static synchronized void show() {
        missionTableController.doShow();
    }

    private MissionTableController() {
        initializeTable();

        BorderPane mainBorderPane = new BorderPane(missionTableView);
        mainBorderPane.setPrefWidth(800);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle("Mission Table");
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(mainBorderPane);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Mission Table");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
    }

    private void doShow() {
        stage.show();
        loadMissionData();
    }

    private void initializeTable() {
        TableColumn<MissionView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setSortable(false);

        TableColumn<MissionView, String> startTimeTableColumn = new TableColumn<>("Start");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        startTimeTableColumn.setSortable(false);

        TableColumn<MissionView, String> stopTimeTableColumn = new TableColumn<>("End");
        stopTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        stopTimeTableColumn.setSortable(false);

        missionTableView.getColumns().add(nameTableColumn);
        missionTableView.getColumns().add(startTimeTableColumn);
        missionTableView.getColumns().add(stopTimeTableColumn);

        nameTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(20 / 37.0));
        startTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));
        stopTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));

        missionTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        missionTableView.setPlaceholder(new Label("no missions available"));

        missionTableView.setRowFactory(param -> {
            ContextMenu rowMenu = new ContextMenu();

            MenuItem missionStartMenuItem = new MenuItem("go to mission start");
            missionStartMenuItem.setOnAction(event -> {
                MissionView missionView = missionTableView.getSelectionModel().getSelectedItem();
                if (missionView != null) {
                    goToMissionStart(missionView);
                }
            });
            MenuItem missionStopMenuItem = new MenuItem("go to mission end");
            missionStopMenuItem.setOnAction(event -> {
                MissionView missionView = missionTableView.getSelectionModel().getSelectedItem();
                if (missionView != null) {
                    goToMissionStop(missionView);
                }
            });

            rowMenu.getItems().addAll(missionStartMenuItem, missionStopMenuItem);

            TableRow<MissionView> row = new TableRow<>();
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() > 1) {
                    MissionView missionView = missionTableView.getSelectionModel().getSelectedItem();
                    if (missionView != null) {
                        goToMissionStart(missionView);
                    }
                }
            });
            return row;
        });
    }

    private void goToMissionStart(MissionView missionView) {
        TimeManager.setTime(missionView.getMission().getTimestamp());
    }

    private void goToMissionStop(MissionView missionView) {
        TimeManager.setTime(missionView.getMission().getStopTime());
    }

    private void loadMissionData() {
        missionTableView.getItems().clear();
        List<Mission> missions = DataManager.getMissions();
        for (Mission mission : missions) {
            missionTableView.getItems().add(new MissionView(mission));
        }
    }
}
