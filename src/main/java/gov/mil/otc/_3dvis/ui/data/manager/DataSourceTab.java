package gov.mil.otc._3dvis.ui.data.manager;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

public class DataSourceTab extends Tab {

    protected final TableView<DataSourceView> dataSourceTableView = new TableView<>();
    private final DataManagerController dataManagerController;

    protected DataSourceTab(String name, DataManagerController dataManagerController) {
        this.dataManagerController = dataManagerController;

        Button loadButton = new Button("Load Selected");
        loadButton.setOnAction(event -> loadSelectedDataSources(true));

        Button unloadButton = new Button("Unload Selected");
        unloadButton.setOnAction(event -> loadSelectedDataSources(false));

        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(event -> removeSelectedDataSources());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, loadButton, unloadButton, removeButton);
        buttonsHBox.setAlignment(Pos.CENTER_LEFT);
        buttonsHBox.setPadding(new Insets(UiConstants.SPACING));

        initializeTable();

        BorderPane borderPane = new BorderPane(dataSourceTableView, buttonsHBox, null, null, null);

        setText(name);
        setContent(borderPane);
    }

    public void load() {
        loadDataSourceTable();
    }

    private void initializeTable() {
        TableColumn<DataSourceView, ImageView> isUseTableColumn = new TableColumn<>("Use");
        isUseTableColumn.setCellValueFactory(new PropertyValueFactory<>("use"));
        isUseTableColumn.setSortable(false);
        isUseTableColumn.setEditable(false);
        isUseTableColumn.setStyle("-fx-alignment: CENTER");

        TableColumn<DataSourceView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setSortable(false);

        TableColumn<DataSourceView, String> startTimeTableColumn = new TableColumn<>("Start Time");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeTableColumn.setSortable(false);

        TableColumn<DataSourceView, String> stopTimeTableColumn = new TableColumn<>("Stop Time");
        stopTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("stopTime"));
        stopTimeTableColumn.setSortable(false);

        dataSourceTableView.getColumns().add(isUseTableColumn);
        dataSourceTableView.getColumns().add(nameTableColumn);
        dataSourceTableView.getColumns().add(startTimeTableColumn);
        dataSourceTableView.getColumns().add(stopTimeTableColumn);

        dataSourceTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dataSourceTableView.setPlaceholder(new Label("no data to display"));
    }

    protected void loadDataSourceTable() {
        dataSourceTableView.getItems().clear();
        for (DataSource dataSource : DataManager.getDataSources()) {
            dataSourceTableView.getItems().add(new DataSourceView(dataSource));
        }
    }

    private void loadSelectedDataSources(boolean load) {
        boolean reload = false;
        for (DataSourceView dataSourceView : dataSourceTableView.getSelectionModel().getSelectedItems()) {
            if (dataSourceView.getDataSource().isUse() != load) {
                dataSourceView.setUse(load);
                dataSourceView.getDataSource().setUse(load);
                DataManager.updateDataSource(dataSourceView.getDataSource());
                reload = true;
            }
        }
        if (reload) {
            ProgressDialog progressDialog = new ProgressDialog(dataManagerController.getStage(), null);
            progressDialog.addStatus("Reloading data, please wait...");
            progressDialog.createAndShow();
            new Thread(() -> {
                DataManager.reloadData();
                Platform.runLater(progressDialog::close);
            }, "DataManager - DataReload").start();
        }
    }

    protected void removeSelectedDataSources() {
        String message = String.format("Are you sure you want to remove the selected data sources?%n" +
                "All data will be lost.");
        if (!DialogUtilities.showYesNoDialog("Remove Mission", message, dataManagerController.getStage())) {
            return;
        }

        List<Integer> sourceIds = new ArrayList<>();
        for (DataSourceView dataSourceView : dataSourceTableView.getSelectionModel().getSelectedItems()) {
            sourceIds.add(dataSourceView.getDataSource().getId());
        }

        if (!sourceIds.isEmpty()) {
            removeDataSources(sourceIds);
        }
    }

    protected void removeDataSources(final List<Integer> sourceIds) {
        final ProgressDialog progressDialog = new ProgressDialog(dataManagerController.getStage(), null);
        progressDialog.addStatus("Removing data, please wait...");
        progressDialog.createAndShow();

        new Thread(() -> {
            if (!sourceIds.isEmpty()) {
                DataManager.removeDataSources(sourceIds);
            }
            DataManager.reloadData();
            Platform.runLater(() -> {
                dataManagerController.loadDataSources();
                progressDialog.close();
            });
        }, "DataManager - remove data sources thread").start();
    }
}
