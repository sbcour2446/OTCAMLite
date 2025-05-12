package gov.mil.otc._3dvis.ui.tools.media;

import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import net.bramp.ffmpeg.FFmpeg;

import javax.swing.text.html.ImageView;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RemoveAudioUtility extends TransparentWindow {

    private final TableView<MediaFileView> tableView = new TableView<>();
    private final TextField inputTextField = new TextField();
    private final TextField outputTextField = new TextField();
    private final Button processButton = new Button("Process");
    private final Button cancelButton = new Button("Cancel");
    private final Button closeButton = new Button("Close");
    private boolean cancelRequested = false;
    private boolean isProcessing = false;
    private Thread processingThread = null;

    public static synchronized void show() {
        new RemoveAudioUtility().createAndShow();
    }

    private RemoveAudioUtility() {
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Remove Audio Utility");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        BorderPane titleBorderPane = new BorderPane(titleLabel);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        inputTextField.setMaxWidth(Double.MAX_VALUE);
        inputTextField.setEditable(false);
        Button inputButton = new Button("...");
        inputButton.setOnAction(event -> selectFiles());

        gridPane.add(new Label("Input Folder:"), 0, rowIndex);
        gridPane.add(inputTextField, 1, rowIndex);
        gridPane.add(inputButton, 2, rowIndex);

        rowIndex++;

        outputTextField.setMaxWidth(Double.MAX_VALUE);
        outputTextField.setEditable(false);

        gridPane.add(new Label("Output Folder:"), 0, rowIndex);
        gridPane.add(outputTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Filter:"), 0, rowIndex);
        gridPane.add(new TextField("CENTER"), 1, rowIndex);

        initializeTableView();

        processButton.setOnAction(event -> startProcess());
        processButton.setDisable(true);
        cancelButton.setOnAction(event -> cancel());
        cancelButton.setDisable(true);
        HBox processHBox = new HBox(UiConstants.SPACING, processButton, cancelButton);
        closeButton.setOnAction(event -> close());

        AnchorPane buttonAnchorPane = new AnchorPane(processHBox, closeButton);
        AnchorPane.setLeftAnchor(processHBox, 0.0);
        AnchorPane.setRightAnchor(closeButton, 0.0);

        VBox mainVBox = new VBox(UiConstants.SPACING, titleBorderPane, new Separator(), gridPane, tableView, buttonAnchorPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(800);

        return mainVBox;
    }

    private void initializeTableView() {
        TableColumn<MediaFileView, ImageView> willProcessTableColumn = new TableColumn<>("Process");
        willProcessTableColumn.setCellValueFactory(new PropertyValueFactory<>("willProcess"));
        willProcessTableColumn.setStyle("-fx-alignment: CENTER");

        TableColumn<MediaFileView, String> fileTableColumn = new TableColumn<>("File");
        fileTableColumn.setCellValueFactory(new PropertyValueFactory<>("file"));

        TableColumn<MediaFileView, String> statusTableColumn = new TableColumn<>("Status");
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        fileTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(20.0 / 37));
        statusTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(15.0 / 37));

        tableView.getColumns().add(willProcessTableColumn);
        tableView.getColumns().add(fileTableColumn);
        tableView.getColumns().add(statusTableColumn);
    }

    private void selectFiles() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("removeaudioutility"));
        directoryChooser.setTitle("Choose input folder");
        File file = directoryChooser.showDialog(getStage());
        if (file != null) {
            inputTextField.setText(file.getAbsolutePath());
            outputTextField.setText(file.getAbsolutePath() + "/processed");
            loadFiles(file);
            SettingsManager.getPreferences().setLastDirectory("removeaudioutility", file.getParent());
        }
    }

    private void loadFiles(File folder) {
        tableView.getItems().clear();

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            tableView.getItems().add(new MediaFileView(file, !file.getName().startsWith("CENTER")));
        }

        processButton.setDisable(false);
    }

    private void startProcess() {
        if (processingThread == null) {
            processButton.setDisable(true);
            cancelButton.setDisable(false);
            closeButton.setDisable(true);
            cancelRequested = false;
            processingThread = new Thread(this::processFiles, "remove audio thread");
            processingThread.start();
        }
    }

    private void cancel() {
        cancelButton.setDisable(true);
        if (isProcessing && processingThread != null) {
            cancelRequested = true;
            final ProgressDialog progressDialog = new ProgressDialog(getStage());
            progressDialog.addStatus("cancelling...");
            progressDialog.createAndShow();
            new Thread(() -> {
                try {
                    processingThread.join();
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                    Thread.currentThread().interrupt();
                }
                Platform.runLater(() -> {
                    progressDialog.close();
                    processComplete();
                });
            }, "remove audio thread - canceling thread").start();
        }
    }

    private void processFiles() {
        isProcessing = true;
        if (!createOutputDirectory()) {
            DialogUtilities.showErrorDialog("error", "error", getStage());
            return;
        }
        for (MediaFileView mediaFileView : tableView.getItems()) {
            if (cancelRequested) {
                updateStatus(mediaFileView, "canceled");
                continue;
            }
            if (mediaFileView.getWillProcess().isSelected()) {
                updateStatus(mediaFileView, "processing");
                String status = processFile(mediaFileView.getFile(), outputTextField.getText());
                updateStatus(mediaFileView, status);
            } else {
                moveFile(mediaFileView.getFile(), outputTextField.getText());
                updateStatus(mediaFileView, "skipped");
            }
        }
        isProcessing = false;
        processComplete();
    }

    private void processComplete() {
        Platform.runLater(() -> {
            closeButton.setDisable(false);
            cancelButton.setDisable(true);
        });
    }

    private boolean createOutputDirectory() {
        try {
            File file = new File(outputTextField.getText());
            if (!file.exists()) {
                return file.mkdir();
            }
        } catch (Exception e) {
            String message = String.format("Could not create output directory %s.", outputTextField.getText());
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return true;
    }

    private void moveFile(File file, String outputDirectory) {
        String output = outputDirectory + File.separator + file.getName();
        try {
            Files.move(file.toPath(), Paths.get(output));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    private void updateStatus(final MediaFileView mediaFileView, String status) {
        Platform.runLater(() -> mediaFileView.setStatus(status));
    }

    private String processFile(File file, String outputDirectory) {
        String ext = file.getName().substring(file.getName().lastIndexOf("."));
        String outputName = file.getName().substring(0, file.getName().lastIndexOf(".")) + "_noaudio" + ext;
        String output = outputDirectory + File.separator + outputName;
        try {
            FFmpeg fFmpeg = new FFmpeg("external\\ffmpeg\\bin\\ffmpeg");
            fFmpeg.run(List.of("-i",
                    file.getAbsolutePath(),
                    "-c",
                    "copy",
                    "-an",
                    output));
            return "processed";
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return "failed";
    }
}
