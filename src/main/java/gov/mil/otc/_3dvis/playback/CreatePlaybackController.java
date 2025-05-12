package gov.mil.otc._3dvis.playback;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.playback.dataset.ImportObjectTreeView;
import gov.mil.otc._3dvis.playback.dataset.PlaybackImportFolder;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreatePlaybackController extends TransparentWindow implements ProgressDialog.CancelListener {

    public static synchronized void show() {
        new CreatePlaybackController().createAndShow(true);
    }

    private final TextField playbackNameTextField = new TextField();
    private final TextField playbackFolderTextField = new TextField();
    private final CheckBox relativePathCheckBox = new CheckBox("use relative path");
    private final ContextMenu treeViewContextMenu = new ContextMenu();
    private final ImportObjectTreeView treeView = new ImportObjectTreeView();
    private final BorderPane dataViewPane = new BorderPane();
    private final Button saveButton = new Button("Save");
    private ProgressDialog progressDialog = null;
    private PlaybackImportFolder playbackImportFolder = null;

    @Override
    protected Pane createContentPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        playbackFolderTextField.setEditable(false);
        playbackFolderTextField.setMaxWidth(Double.MAX_VALUE);
        Button selectFolderButton = new Button("...");
        selectFolderButton.setOnAction(event -> selectPlaybackFolder());

        gridPane.add(new TextWithStyleClass("Playback Import Folder:"), 0, rowIndex);
        gridPane.add(playbackFolderTextField, 1, rowIndex);
        gridPane.add(selectFolderButton, 2, rowIndex);

        rowIndex++;

        playbackNameTextField.setMaxWidth(Double.MAX_VALUE);
        playbackNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            for (Playback playback : DataManager.getPlaybackList()) {
                if (playback.getName().equalsIgnoreCase(playbackNameTextField.getText())) {
                    playbackNameTextField.setStyle("-fx-text-fill:red;");
                    return;
                }
            }
            playbackNameTextField.setStyle(null);
        });
        gridPane.add(new TextWithStyleClass("Playback Name:"), 0, rowIndex);
        gridPane.add(playbackNameTextField, 1, rowIndex);

        rowIndex++;

        relativePathCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            File file = new File(playbackFolderTextField.getText());
            if (newValue) {
                playbackFolderTextField.setText(getRelativePath(file));
            } else {
                playbackFolderTextField.setText(getAbsolutePath(file));
            }
        });
//        gridPane.add(relativePathCheckBox, 1, rowIndex);

        GridPane dataGridPane = new GridPane();
        dataGridPane.setPadding(new Insets(UiConstants.SPACING));
        dataGridPane.setHgap(UiConstants.SPACING);
        dataGridPane.setVgap(UiConstants.SPACING);
        dataGridPane.getColumnConstraints().add(new ColumnConstraints());
        dataGridPane.getColumnConstraints().add(new ColumnConstraints());
        dataGridPane.getColumnConstraints().get(0).setPercentWidth(35);
        dataGridPane.getColumnConstraints().get(1).setPercentWidth(65);

        dataGridPane.add(treeView, 0, 0);
        dataGridPane.add(dataViewPane, 1, 0);

        MenuItem menuItem = new MenuItem("Import");
        menuItem.setOnAction(event -> importItem());

        treeViewContextMenu.getItems().add(menuItem);

        treeView.setContextMenu(treeViewContextMenu);
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            dataViewPane.getChildren().clear();
            if (newValue != null && newValue.getValue() != null) {
                dataViewPane.setCenter(newValue.getValue().getDisplayPane());
            }
        });

        saveButton.setOnAction(actionEvent -> {
            for (Playback playback : DataManager.getPlaybackList()) {
                if (playback.getName().equalsIgnoreCase(playbackNameTextField.getText())) {
                    DialogUtilities.showErrorDialog("Save Playback",
                            "Playback already exist." + System.lineSeparator() +
                                    "Change name or select another import folder.",
                            true, getStage());
                    return;
                }
            }

            if (DataManager.addPlayback(new Playback(playbackNameTextField.getText(),
                    playbackImportFolder.getObject(),
                    System.currentTimeMillis()))) {
                close();
            } else {
                DialogUtilities.showErrorDialog("Save Playback",
                        "Failed to save playback set", true, getStage());
            }
        });

        HBox buttonHBox = new HBox(UiConstants.SPACING, saveButton);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(actionEvent -> close());

        AnchorPane buttonAnchorPane = new AnchorPane(buttonHBox, closeButton);
        buttonAnchorPane.setPadding(new Insets(UiConstants.SPACING, UiConstants.SPACING, 0, UiConstants.SPACING));
        AnchorPane.setLeftAnchor(buttonHBox, 0.0);
        AnchorPane.setRightAnchor(closeButton, 0.0);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Create Playback Set"),
                new Separator(),
                gridPane,
                dataGridPane,
                new Separator(),
                buttonAnchorPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setFillWidth(true);
        mainVBox.setPrefWidth(1000);

        return mainVBox;
    }

    private void selectPlaybackFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select playback data folder");
        directoryChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("playback"));
        File file = directoryChooser.showDialog(getStage());
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory("playback", file.getParent());
            if (relativePathCheckBox.isSelected()) {
                playbackFolderTextField.setText(getRelativePath(file));
            } else {
                playbackFolderTextField.setText(file.getAbsolutePath());
            }
            playbackNameTextField.setText(file.getName());
            playbackNameTextField.setStyle(null);

            for (Playback playback : DataManager.getPlaybackList()) {
                if (playback.getName().equalsIgnoreCase(file.getName())) {
                    playbackNameTextField.setStyle("-fx-text-fill:red;");
                    break;
                }
            }

            scanPlaybackFolder(file);
        }
    }

    private String getAbsolutePath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return file.getAbsolutePath();
        }
    }

    private String getRelativePath(File file) {
        String relativePath;
        try {
            Path jarPath = Paths.get(new File(new File(".").getCanonicalPath()).toURI());
            Path selectedPath = Paths.get(file.toURI());
            relativePath = jarPath.relativize(selectedPath).toString();
        } catch (IOException e) {
            relativePath = file.getAbsolutePath();
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return relativePath;
    }

    private void scanPlaybackFolder(final File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return;
        }

        playbackImportFolder = new PlaybackImportFolder(folder);
        Playback playback = new Playback(folder.getName(), playbackImportFolder, System.currentTimeMillis());
        PlaybackLoader.scanOnly(playback, getStage(), successful -> {
            Platform.runLater(() -> {
                if (successful) {
                    treeView.setPlaybackImportFolder(playbackImportFolder);
                }
            });
        });

//        StatusDialog statusDialog = new StatusDialog(getStage());
//        statusDialog.createAndShow();
////        progressDialog = new ProgressDialog(getStage(), this);
////        progressDialog.createAndShow();
//
//        playbackImportFolder = new PlaybackImportFolder(folder);
//        playbackImportFolder.startScan(statusDialog.createStatusItem(""));
//        playbackImportFolder.waitForScanComplete();
//        statusDialog.setComplete(true);
    }

    private void importItem() {
        final ImportObject<?> importObject = getSelectedItem();
        if (importObject != null) {
            new Thread(() -> {
                importObject.doImport();
                Platform.runLater(treeView::reload);
            }, importObject.getName() + " import").start();
        }
    }

    private ImportObject<?> getSelectedItem() {
        TreeItem<ImportObject<?>> selectedMunitionItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedMunitionItem != null) {
            return selectedMunitionItem.getValue();
        }
        return null;
    }

    @Override
    public void onCancel() {
        playbackImportFolder.requestCancel();
        Platform.runLater(() -> {
            progressDialog.close();
        });
    }
}

