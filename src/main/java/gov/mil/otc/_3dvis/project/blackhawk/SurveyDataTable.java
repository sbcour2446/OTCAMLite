package gov.mil.otc._3dvis.project.blackhawk;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class SurveyDataTable {

    private final TableView<SurveyData> tableView = new TableView<>();

    public static void show(IEntity entity, String role) {
        new SurveyDataTable(entity, role);
    }

    private SurveyDataTable(IEntity entity, String role) {
        initializeTable();
        loadSurvey(entity, role);

        VBox mainVBox = new VBox(tableView);
        mainVBox.setPrefWidth(800);

        Stage stage = new Stage();
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle("Survey");
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(mainVBox);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Survey");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
        stage.show();
    }

    private void initializeTable() {
        TableColumn<SurveyData, String> missionTableColumn = new TableColumn<>("Mission");
        missionTableColumn.setCellValueFactory(new PropertyValueFactory<>("mission"));
        missionTableColumn.setSortable(false);

        TableColumn<SurveyData, Integer> tailNumberTableColumn = new TableColumn<>("Tail Number");
        tailNumberTableColumn.setCellValueFactory(new PropertyValueFactory<>("tailNumber"));
        tailNumberTableColumn.setSortable(false);

        TableColumn<SurveyData, String> pinTableColumn = new TableColumn<>("Pin");
        pinTableColumn.setCellValueFactory(new PropertyValueFactory<>("pin"));
        pinTableColumn.setSortable(false);

        TableColumn<SurveyData, String> roleTableColumn = new TableColumn<>("Role");
        roleTableColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleTableColumn.setSortable(false);

        TableColumn<SurveyData, String> seatTableColumn = new TableColumn<>("Seat");
        seatTableColumn.setCellValueFactory(new PropertyValueFactory<>("seat"));
        seatTableColumn.setSortable(false);

        TableColumn<SurveyData, String> questionTableColumn = new TableColumn<>("Question");
        questionTableColumn.setCellValueFactory(new PropertyValueFactory<>("question"));
        questionTableColumn.setSortable(false);

        TableColumn<SurveyData, String> answerTableColumn = new TableColumn<>("Answer");
        answerTableColumn.setCellValueFactory(new PropertyValueFactory<>("answer"));
        answerTableColumn.setSortable(false);

        tableView.getColumns().add(missionTableColumn);
        tableView.getColumns().add(tailNumberTableColumn);
        tableView.getColumns().add(pinTableColumn);
        tableView.getColumns().add(roleTableColumn);
        tableView.getColumns().add(seatTableColumn);
        tableView.getColumns().add(questionTableColumn);
        tableView.getColumns().add(answerTableColumn);

//        nameTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(20 / 37.0));
//        startTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));
//        stopTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.setPlaceholder(new Label("no survey available"));
    }

    private void loadSurvey(IEntity entity, String role) {
        List<Mission> currentMissions = DataManager.getCurrentMissions();
        for (Mission mission : currentMissions) {
            List<SurveyData> surveyDataList = DataManager.getSurveyData(mission, entity.getEntityId().getId(), role);
            for (SurveyData surveyData : surveyDataList) {
                tableView.getItems().add(surveyData);
            }
        }
    }
}
