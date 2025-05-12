package gov.mil.otc._3dvis.playback.dataset.tapets;

import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Map;

public class TapetsConfigurationFileView extends TableView<TapetsConfigurationImportObject> {

    protected TapetsConfigurationFileView(Map<Integer, TapetsImportObject> tapetsImportObjectMap) {

        TableColumn<TapetsConfigurationImportObject, String> tapetsIdTableColumn = new TableColumn<>("ID");
        tapetsIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("tapetsId"));

        TableColumn<TapetsConfigurationImportObject, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<TapetsConfigurationImportObject, String> descriptionTableColumn = new TableColumn<>("Description");
        descriptionTableColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<TapetsConfigurationImportObject, String> urnTableColumn = new TableColumn<>("URN");
        urnTableColumn.setCellValueFactory(new PropertyValueFactory<>("urn"));

        TableColumn<TapetsConfigurationImportObject, String> affiliationTableColumn = new TableColumn<>("Affiliation");
        affiliationTableColumn.setCellValueFactory(new PropertyValueFactory<>("affiliation"));

        TableColumn<TapetsConfigurationImportObject, String> entityTypeTableColumn = new TableColumn<>("Entity Type");
        entityTypeTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));

        TableColumn<TapetsConfigurationImportObject, String> startTimeTableColumn = new TableColumn<>("Start Time");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));

//        willCreateTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(5.0 / 37));
//        fileTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(15.0 / 37));
//        statusTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(16.0 / 37));

        getColumns().add(tapetsIdTableColumn);
        getColumns().add(nameTableColumn);
        getColumns().add(descriptionTableColumn);
        getColumns().add(urnTableColumn);
        getColumns().add(affiliationTableColumn);
        getColumns().add(entityTypeTableColumn);
        getColumns().add(startTimeTableColumn);

//        nameTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(20 / 37.0));
//        startTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));
//        stopTimeTableColumn.prefWidthProperty().bind(missionTableView.widthProperty().multiply(8 / 37.0));

        setPlaceholder(new Label("no configuration available"));
        setRowFactory(param -> new ConfigurationTableRow());
        setSelectionModel(null);

        URL url = ThemeHelper.class.getResource("/css/import_data_object_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            getStylesheets().add(css);
        }

        fill(tapetsImportObjectMap);
    }

    private void fill(Map<Integer, TapetsImportObject> tapetsImportObjectMap) {
        for (TapetsImportObject tapetsImportObject : tapetsImportObjectMap.values()) {
            TapetsConfigurationImportObject tapetsConfigurationImportObject = tapetsImportObject.getTapetsConfigurationImportObject();
            getItems().add(tapetsConfigurationImportObject);
        }
    }

    private static class ConfigurationTableRow extends TableRow<TapetsConfigurationImportObject> {

        @Override
        public void updateItem(TapetsConfigurationImportObject item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null) {
                setStyle("");
            } else {
                PseudoClass pseudoClass = PseudoClass.getPseudoClass("new");
                pseudoClassStateChanged(pseudoClass, item.isNew());

                pseudoClass = PseudoClass.getPseudoClass("missing");
                pseudoClassStateChanged(pseudoClass, item.isMissing());

                pseudoClass = PseudoClass.getPseudoClass("modified");
                pseudoClassStateChanged(pseudoClass, item.isModified());
            }
        }
    }
}
