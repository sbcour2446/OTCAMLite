package gov.mil.otc._3dvis.project.dlm.launchtable;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.project.dlm.IDlmEntity;
import gov.mil.otc._3dvis.project.dlm.Launch;
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

public class LaunchTable {

    private final TableView<LaunchView> tableView = new TableView<>();

    public static void show(IDlmEntity entity) {
        new LaunchTable(entity);
    }

    private LaunchTable(IDlmEntity entity) {
        initializeTable();
        loadLaunches(entity);

        String name;
        EntityDetail entityDetail = entity.getLastEntityDetail();
        if (entityDetail != null) {
            name = entityDetail.getName();
        } else {
            name = entity.getEntityId().toString();
        }
        name += " Launch List";

        Iteration iteration = DataManager.getIterationManager().getCurrentIteration();
        if (iteration != null) {
            name += " " + iteration.getName();
        } else {
            name += " no mission";
        }

        VBox mainVBox = new VBox(tableView);
        mainVBox.setPrefWidth(800);

        Stage stage = new Stage();
        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle(name);
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(mainVBox);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer(name);
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
        stage.show();
    }

    private void initializeTable() {
        TableColumn<LaunchView, String> timeTableColumn = new TableColumn<>("Time");
        timeTableColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeTableColumn.setSortable(false);

        TableColumn<LaunchView, Integer> launchNumberTableColumn = new TableColumn<>("Launch Number");
        launchNumberTableColumn.setCellValueFactory(new PropertyValueFactory<>("launchNumber"));
        launchNumberTableColumn.setSortable(false);

        tableView.getColumns().add(timeTableColumn);
        tableView.getColumns().add(launchNumberTableColumn);

//        nameTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(20 / 37.0));
//        startTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));
//        stopTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.setPlaceholder(new Label("no data available"));

        tableView.setRowFactory(param -> {
            ContextMenu rowMenu = new ContextMenu();

            MenuItem menuItem = new MenuItem("Go to event");
            menuItem.setOnAction(event -> {
                LaunchView selectedLaunchView = tableView.getSelectionModel().getSelectedItem();
                if (selectedLaunchView != null) {
                    goToEvent(selectedLaunchView);
                }
            });
            rowMenu.getItems().add(menuItem);

            TableRow<LaunchView> row = new TableRow<>();
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(rowMenu));
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() > 1) {
                    LaunchView selectedLaunchView = tableView.getSelectionModel().getSelectedItem();
                    if (selectedLaunchView != null) {
                        goToEvent(selectedLaunchView);
                    }
                }
            });
            return row;
        });
    }

    private void goToEvent(LaunchView launchView) {
        TimeManager.setTime(launchView.getLaunch().getStartTime());
    }

    private void loadLaunches(IDlmEntity entity) {
        Iteration iteration = DataManager.getIterationManager().getCurrentIteration();
        for (Launch launch : entity.getDlmDisplayManager().getLaunchList(iteration)) {
            tableView.getItems().add(new LaunchView(launch));
        }
    }
}
