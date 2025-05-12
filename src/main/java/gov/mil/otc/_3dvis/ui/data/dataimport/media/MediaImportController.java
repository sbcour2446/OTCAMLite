package gov.mil.otc._3dvis.ui.data.dataimport.media;

import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class MediaImportController extends TransparentWindow {

    private final TableView<MediaFileView> tableView = new TableView<>();
    private final ObservableList<MediaFileView> observableList = FXCollections.observableArrayList();

    public static synchronized void show() {
        new MediaImportController().createAndShow();
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Import Media");
        titleLabel.setFont(Font.font(UiConstants.FONT_NAME, FontWeight.BOLD, 18));
        BorderPane titleBorderPane = new BorderPane(titleLabel);

        Button addFileButton = new Button("Add");
        addFileButton.setOnAction(event -> addFiles());
        Button removeFileButton = new Button("Remove");
        removeFileButton.setOnAction(event -> removeFiles());
        HBox buttonHBox = new HBox(UiConstants.SPACING, addFileButton, removeFileButton);

        initializeTable();

        Button importFileButton = new Button("Import");
        importFileButton.setOnAction(event -> importFiles());
        importFileButton.disableProperty().bind(Bindings.size(observableList).isEqualTo(0));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        HBox closeButtonHBox = new HBox(UiConstants.SPACING, importFileButton, closeButton);
        closeButtonHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING, titleBorderPane, new Separator(), buttonHBox, tableView, closeButtonHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));

        return mainVBox;
    }

    private void initializeTable() {
        TableColumn<MediaFileView, String> statusTableColumn = new TableColumn<>("Entity");
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityName"));
        TableColumn<MediaFileView, String> mediaSetTableColumn = new TableColumn<>("Media Set");
        mediaSetTableColumn.setCellValueFactory(new PropertyValueFactory<>("mediaSet"));
        TableColumn<MediaFileView, String> startTimeTableColumn = new TableColumn<>("Start Time");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        TableColumn<MediaFileView, String> stopTimeTableColumn = new TableColumn<>("Stop Time");
        stopTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("stopTime"));
        TableColumn<MediaFileView, String> filenameTableColumn = new TableColumn<>("Filename");
        filenameTableColumn.setCellValueFactory(new PropertyValueFactory<>("filename"));

        centerColumn(statusTableColumn);
        centerColumn(mediaSetTableColumn);
        centerColumn(startTimeTableColumn);

        tableView.getColumns().add(statusTableColumn);
        tableView.getColumns().add(mediaSetTableColumn);
        tableView.getColumns().add(startTimeTableColumn);
        tableView.getColumns().add(stopTimeTableColumn);
        tableView.getColumns().add(filenameTableColumn);

        statusTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(3.0 / 34.0));
        mediaSetTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(3.0 / 34.0));
        startTimeTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(4.0 / 34.0));
        stopTimeTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(4.0 / 34.0));
        filenameTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(20.0 / 34.0));

        tableView.setItems(observableList);
        tableView.setMinWidth(1200);
    }

    private void centerColumn(TableColumn<?, ?> column) {
        column.setStyle("-fx-alignment: CENTER");
    }

    private void addFiles() {
        getStage().setOpacity(.75);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select media files to add.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("mediaimport"));
        List<File> files = fileChooser.showOpenMultipleDialog(getStage());
        if (files != null && !files.isEmpty()) {
            List<MediaFileView> mediaFiles = MediaFileChooser.show(files, getStage());
            if (mediaFiles != null) {
                for (MediaFileView mediaFileView : mediaFiles) {
                    if (!observableList.contains(mediaFileView)) {
                        observableList.add(mediaFileView);
                    }
                }
            }
            SettingsManager.getPreferences().setLastDirectory("mediaimport", files.get(0).getParent());
        }
        getStage().setOpacity(1);
    }


    private void removeFiles() {
        tableView.getItems().removeAll(tableView.getSelectionModel().getSelectedItems());
    }

    private void importFiles() {
        for (MediaFileView mediaFileView : observableList) {
            MediaFile mediaFile = mediaFileView.getMediaFile();
            mediaFileView.getEntity().getMediaCollection().addMediaFile(mediaFile);
            DatabaseLogger.addMedia(mediaFile, mediaFileView.getEntity().getEntityId());
        }
        observableList.clear();
    }
}