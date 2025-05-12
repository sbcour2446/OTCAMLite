package gov.mil.otc._3dvis.ui.tools.iterationtable;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.beans.binding.Bindings;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class IterationTableController {

    private static final IterationTableController iterationTableController = new IterationTableController();
    private final Stage stage = new Stage();
    private final TableView<IterationView> iterationTableView = new TableView<>();

    public static synchronized void show() {
        iterationTableController.doShow();
    }

    private IterationTableController() {
        initializeTable();

        VBox mainVBox = new VBox(iterationTableView);
        mainVBox.setPrefWidth(800);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle("Iteration Table");
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(mainVBox);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Iteration Table");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
    }

    private void doShow() {
        stage.show();
        loadMissionData();
    }

    private void initializeTable() {
        TableColumn<IterationView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setSortable(false);

        TableColumn<IterationView, String> startTimeTableColumn = new TableColumn<>("Start");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("start"));
        startTimeTableColumn.setSortable(false);

        TableColumn<IterationView, String> stopTimeTableColumn = new TableColumn<>("End");
        stopTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("end"));
        stopTimeTableColumn.setSortable(false);

        iterationTableView.getColumns().add(nameTableColumn);
        iterationTableView.getColumns().add(startTimeTableColumn);
        iterationTableView.getColumns().add(stopTimeTableColumn);

        nameTableColumn.prefWidthProperty().bind(iterationTableView.widthProperty().multiply(20 / 37.0));
        startTimeTableColumn.prefWidthProperty().bind(iterationTableView.widthProperty().multiply(8 / 37.0));
        stopTimeTableColumn.prefWidthProperty().bind(iterationTableView.widthProperty().multiply(8 / 37.0));

        iterationTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        iterationTableView.setPlaceholder(new Label("no iterations available"));

        iterationTableView.setRowFactory(param -> {
            ContextMenu rowMenu = new ContextMenu();

            MenuItem missionStartMenuItem = new MenuItem("go to iteration start");
            missionStartMenuItem.setOnAction(event -> {
                IterationView iterationView = iterationTableView.getSelectionModel().getSelectedItem();
                if (iterationView != null) {
                    goToIterationStart(iterationView);
                }
            });
            MenuItem missionStopMenuItem = new MenuItem("go to iteration end");
            missionStopMenuItem.setOnAction(event -> {
                IterationView iterationView = iterationTableView.getSelectionModel().getSelectedItem();
                if (iterationView != null) {
                    goToIterationStop(iterationView);
                }
            });

            rowMenu.getItems().addAll(missionStartMenuItem, missionStopMenuItem);

            TableRow<IterationView> row = new TableRow<>();
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() > 1) {
                    IterationView iterationView = iterationTableView.getSelectionModel().getSelectedItem();
                    if (iterationView != null) {
                        goToIterationStart(iterationView);
                    }
                }
            });
            return row;
        });
    }

    private void goToIterationStart(IterationView iterationView) {
        TimeManager.setTime(iterationView.getMission().getStartTime());
    }

    private void goToIterationStop(IterationView iterationView) {
        TimeManager.setTime(iterationView.getMission().getStopTime());
    }

    private void loadMissionData() {
        iterationTableView.getItems().clear();
        List<Iteration> iterations = DataManager.getIterationManager().getIterations();
        for (Iteration iteration : iterations) {
            iterationTableView.getItems().add(new IterationView(iteration));
        }
    }
}
