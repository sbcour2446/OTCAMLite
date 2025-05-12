package gov.mil.otc._3dvis.playback;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

public class PlaybackManagerController extends TransparentWindow {

    public static synchronized void show() {
        new PlaybackManagerController().createAndShow();
    }

    public static synchronized void showAndLoad(String playbackName) {
        new PlaybackManagerController().createAndShow();
    }

    private final TableView<PlaybackView> tableView = new TableView<>();

    @Override
    protected Pane createContentPane() {
        initializeTable();

        HBox hBox = createLoadButtons();

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        AnchorPane buttonAnchorPane = new AnchorPane(hBox, closeButton);
        AnchorPane.setLeftAnchor(hBox, 0.0);
        AnchorPane.setRightAnchor(closeButton, 0.0);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Load Playback"),
                new Separator(),
                tableView,
                new Separator(),
                buttonAnchorPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setFillWidth(true);
        mainVBox.setPrefWidth(1000);

        return mainVBox;
    }

    private @NotNull HBox createLoadButtons() {
        Button loadButton = new Button("Load Selected");
        loadButton.setOnAction(event -> startLoad());

        Button unloadButton = new Button("Unload Selected");
        unloadButton.setOnAction(event -> unLoad());

        Button createButton = new Button("Create New");
        createButton.setOnAction(event -> create());

        CheckBox loadOnStartupCheckBox = new CheckBox("Load on startup");
        loadOnStartupCheckBox.setSelected(SettingsManager.getPreferences().isLoadPlaybackOnStartup());
        loadOnStartupCheckBox.setOnAction(actionEvent -> {
            SettingsManager.getPreferences().setLoadPlaybackOnStartup(loadOnStartupCheckBox.isSelected());
        });

        HBox hBox = new HBox(UiConstants.SPACING, loadButton, unloadButton, createButton, loadOnStartupCheckBox);
        hBox.setAlignment(Pos.BASELINE_LEFT);
        return hBox;
    }

    private void initializeTable() {
        TableColumn<PlaybackView, Boolean> selectTableColumn = new TableColumn<>("Select");
        selectTableColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectTableColumn.setCellFactory(personBooleanTableColumn -> new SelectPlaybackCell(getStage(), tableView));
        selectTableColumn.setSortable(false);
        selectTableColumn.setStyle("-fx-alignment: CENTER;");

        TableColumn<PlaybackView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setSortable(false);

        TableColumn<PlaybackView, String> statusTableColumn = new TableColumn<>("Status");
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusTableColumn.setSortable(false);

        TableColumn<PlaybackView, String> pathTableColumn = new TableColumn<>("Path");
        pathTableColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathTableColumn.setSortable(false);

        TableColumn<PlaybackView, String> creationTimeTableColumn = new TableColumn<>("Creation Time");
        creationTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("creationTime"));
        creationTimeTableColumn.setSortable(false);

        TableColumn<PlaybackView, Boolean> removeTableColumn = new TableColumn<>("Remove");
        removeTableColumn.setCellValueFactory(features -> new SimpleBooleanProperty(features.getValue() != null));
        removeTableColumn.setCellFactory(personBooleanTableColumn -> new RemovePlaybackCell(tableView));
        removeTableColumn.setSortable(false);
        removeTableColumn.setStyle("-fx-alignment: CENTER;");

        tableView.getColumns().add(selectTableColumn);
        tableView.getColumns().add(nameTableColumn);
        tableView.getColumns().add(statusTableColumn);
        tableView.getColumns().add(pathTableColumn);
        tableView.getColumns().add(creationTimeTableColumn);
        tableView.getColumns().add(removeTableColumn);

        tableView.setPlaceholder(new Label("no configuration available"));
        tableView.setRowFactory(playbackTableView -> new TableRow<>() {
            @Override
            protected void updateItem(PlaybackView item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setStyle("");
                } else if (isCurrentPlayback(item.getPlayback())) {
                    PseudoClass pseudoClass = PseudoClass.getPseudoClass("loaded");
                    pseudoClassStateChanged(pseudoClass, true);
                    setStyle("-fx-background-color:blue");
                } else if (item.getStatus() != null && item.getStatus().equalsIgnoreCase("invalid path")) {
                    PseudoClass pseudoClass = PseudoClass.getPseudoClass("invalid");
                    pseudoClassStateChanged(pseudoClass, true);
                    setStyle("-fx-background-color:red");
                }
            }
        });
        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.setSelected(false);
            }
            if (newValue != null) {
                newValue.setSelected(true);
            }
        });

        URL url = ThemeHelper.class.getResource("/css/import_data_object_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            tableView.getStylesheets().add(css);
        }

        updateTable();
    }

    private void updateTable() {
        tableView.getItems().clear();
        for (Playback playback : DataManager.getPlaybackList()) {
            PlaybackView playbackView = new PlaybackView(playback);
            tableView.getItems().add(playbackView);
            if (!playback.verify()) {
                playbackView.setStatus("invalid path");
            } else if (isCurrentPlayback(playback)) {
                playbackView.setStatus("loaded");
            } else {
                playbackView.setStatus("");
            }
        }
        tableView.refresh();
    }

    private void startLoad() {
        PlaybackView selectedPlaybackView = tableView.getSelectionModel().getSelectedItem();
        if (selectedPlaybackView == null) {
            DialogUtilities.showWarningDialog("Load Playback", "Must select playback to load.", true, getStage());
            return;
        }

        Playback playback = selectedPlaybackView.getPlayback();
        if (playback == null || !playback.verify()) {
            DialogUtilities.showWarningDialog("Load Playback", "Invalid playback.", true, getStage());
            return;
        }

        if (isCurrentPlayback(playback)) {
            DialogUtilities.showInformationDialog("Load Playback", "Load Playback",
                    "Playback already loaded.", getStage());
            return;
        }

        selectedPlaybackView.setStatus("loading");

        PlaybackLoader.loadPlaybackAsync(playback, getStage(), successful -> Platform.runLater(() -> {
            if (successful) {
                selectedPlaybackView.setStatus("loaded");
            } else {
                selectedPlaybackView.setStatus("failed to load");
            }
            tableView.refresh();
        }));

    }

    private void unLoad() {
        PlaybackLoader.unloadCurrentPlayback();
        updateTable();
    }

    private void create() {
        CreatePlaybackController.show();
        updateTable();
    }

    private boolean isCurrentPlayback(Playback playback) {
        return DataManager.getCurrentPlayback() != null &&
                DataManager.getCurrentPlayback().getName().equalsIgnoreCase(playback.getName());
    }

    public static final class PlaybackView {

        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final Playback playback;
        private final StringProperty status = new SimpleStringProperty();

        public PlaybackView(Playback playback) {
            this.playback = playback;
            if (DataManager.getCurrentPlayback() != null &&
                    DataManager.getCurrentPlayback().getName().equalsIgnoreCase(playback.getName())) {
                setStatus("loaded");
            }
        }

        public boolean isSelected() {
            return selected.get();
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public Playback getPlayback() {
            return playback;
        }

        public String getName() {
            return playback.getName();
        }

        public String getPath() {
            return playback.getFile().getAbsolutePath();
        }

        public String getCreationTime() {
            return Utility.formatTime(playback.getCreationTime(), Common.DATE_TIME_HHmm);
        }

        public String getStatus() {
            return status.get();
        }

        public StringProperty statusProperty() {
            return status;
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }

    public static class SelectPlaybackCell extends TableCell<PlaybackView, Boolean> {

        private final RadioButton radioButton = new RadioButton();

        /**
         * SelectPlaybackCell constructor
         *
         * @param stage the stage in which the table is placed.
         * @param table the table to which a new person can be added.
         */
        private SelectPlaybackCell(final Stage stage, final TableView<?> table) {
            radioButton.setOnAction(actionEvent -> {
                table.getSelectionModel().select(getTableRow().getIndex());
            });
        }

        /**
         * places an add button in the row only if the row is not empty.
         */
        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                radioButton.setSelected(item);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(radioButton);
            } else {
                setGraphic(null);
            }
        }
    }

    public class RemovePlaybackCell extends TableCell<PlaybackView, Boolean> {

        private final Button button = new Button("Remove");

        /**
         * RemovePlaybackCell constructor
         *
         * @param table the table to which a new person can be added.
         */
        RemovePlaybackCell(final TableView<?> table) {
            button.setOnAction(actionEvent -> {
                PlaybackView playbackView = getTableRow().getItem();
                Playback playback = playbackView.getPlayback();
                if (DialogUtilities.showYesNoDialog("Delete Playback",
                        "Do you want to delete this playback?" + System.lineSeparator() + playback.getName(),
                        getStage())) {
                    DataManager.removePlayback(playback);
                    table.getItems().remove(playbackView);
                    table.refresh();
                }
            });
        }

        /**
         * places an add button in the row only if the row is not empty.
         */
        @Override
        protected void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);
            if (!empty) {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setGraphic(button);
            } else {
                setGraphic(null);
            }
        }
    }
}
