package gov.mil.otc._3dvis.ui.tools.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.media.MediaSet;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.DateTimePicker2;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.validation.IntegerOnlyFilter;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import net.bramp.ffmpeg.FFmpeg;

import javax.swing.text.html.ImageView;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateMediaClipController extends TransparentWindow {

    private static final String SETTINGS_NAME = "createmediaclip";
    private final DateTimePicker2 startTimeDateTimePicker = new DateTimePicker2(System.currentTimeMillis());
    private final Spinner<Integer> durationSpinner = new Spinner<>();
    private final TextField outputTextField = new TextField();
    private final TextField clipNameTextField = new TextField();
    private final TableView<MediaSetView> tableView = new TableView<>();
    private final Button createButton = new Button("Create");
    private final Button cancelButton = new Button("Cancel");
    private final Button closeButton = new Button("Close");
    private final IEntity entity;
    private final List<MediaSet> mediaSetList;
    private boolean cancelRequested = false;
    private boolean isProcessing = false;
    private Thread processingThread = null;

    public static synchronized void show(IEntity entity, List<MediaSet> mediaSetList) {
        TimeManager.setPause(true);
        new CreateMediaClipController(entity, mediaSetList).createAndShow();
    }

    private CreateMediaClipController(IEntity entity, List<MediaSet> mediaSetList) {
        this.entity = entity;
        this.mediaSetList = mediaSetList;
    }

    @Override
    protected Pane createContentPane() {
        GridPane gridPane = new GridPane();
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        gridPane.add(new Label("Entity:"), 0, rowIndex);
        gridPane.add(new Label(entity.getName()), 1, rowIndex);

        rowIndex++;

        startTimeDateTimePicker.setTimestamp(TimeManager.getTime());

        gridPane.add(new Label("Start Time:"), 0, rowIndex);
        gridPane.add(startTimeDateTimePicker, 1, rowIndex);

        rowIndex++;

        SpinnerValueFactory<Integer> spinnerValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 30);
        TextFormatter<Integer> textFormatter = new TextFormatter<>(spinnerValueFactory.getConverter(),
                spinnerValueFactory.getValue(), new IntegerOnlyFilter(0, Integer.MAX_VALUE));
        spinnerValueFactory.valueProperty().bindBidirectional(textFormatter.valueProperty());

        durationSpinner.setValueFactory(spinnerValueFactory);
        durationSpinner.getEditor().setTextFormatter(textFormatter);
        durationSpinner.getEditor().setTextFormatter(textFormatter);
        durationSpinner.setEditable(true);

        gridPane.add(new Label("Duration (seconds):"), 0, rowIndex);
        gridPane.add(durationSpinner, 1, rowIndex);

        rowIndex++;

        outputTextField.setText(SettingsManager.getPreferences().getLastDirectory(SETTINGS_NAME).getAbsolutePath());
        outputTextField.setEditable(false);
        Button selectFolderButton = new Button("...");
        selectFolderButton.setOnAction(event -> selectOutputFolder());

        gridPane.add(new Label("Output Folder:"), 0, rowIndex);
        gridPane.add(outputTextField, 1, rowIndex);
        gridPane.add(selectFolderButton, 2, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Clip name:"), 0, rowIndex);
        gridPane.add(clipNameTextField, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Media Set:"), 0, rowIndex);

        initializeTableView();
        gridPane.add(tableView, 1, rowIndex, 2, 1);

        createButton.setOnAction(event -> startProcess());
        cancelButton.setOnAction(event -> cancel());
        closeButton.setOnAction(event -> close());
        cancelButton.setVisible(false);

        StackPane cancelCloseStackPane = new StackPane(cancelButton, closeButton);

        HBox buttonsHBox = new HBox(UiConstants.SPACING, createButton, cancelCloseStackPane);
        buttonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING, createTitleLabel("Create Media Clip"), new Separator(),
                gridPane, buttonsHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(800);
        mainVBox.setAlignment(Pos.CENTER);

        return mainVBox;
    }

    private void initializeTableView() {
        TableColumn<MediaSetView, ImageView> willCreateTableColumn = new TableColumn<>("Create");
        willCreateTableColumn.setCellValueFactory(new PropertyValueFactory<>("willProcess"));
        willCreateTableColumn.setStyle("-fx-alignment: CENTER");

        TableColumn<MediaSetView, String> fileTableColumn = new TableColumn<>("Media Set");
        fileTableColumn.setCellValueFactory(new PropertyValueFactory<>("mediaSet"));

        TableColumn<MediaSetView, String> statusTableColumn = new TableColumn<>("Status");
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        willCreateTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(5.0 / 37));
        fileTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(15.0 / 37));
        statusTableColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(16.0 / 37));

        tableView.getColumns().add(willCreateTableColumn);
        tableView.getColumns().add(fileTableColumn);
        tableView.getColumns().add(statusTableColumn);

        for (MediaSet mediaSet : mediaSetList) {
            tableView.getItems().add(new MediaSetView(mediaSet, true));
        }
    }

    private void selectOutputFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select output location.");
        directoryChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory(SETTINGS_NAME));
        File selectedFile = directoryChooser.showDialog(getStage());
        if (selectedFile != null) {
            outputTextField.setText(selectedFile.getAbsolutePath());
            outputTextField.setStyle(null);
            SettingsManager.getPreferences().setLastDirectory(SETTINGS_NAME, selectedFile.getAbsolutePath());
        }
    }

    private void startProcess() {
        if (processingThread == null) {
            if (clipNameTextField.getText().isBlank()) {
                DialogUtilities.showErrorDialog("Error", "Must enter a name for this clip set.", getStage());
                return;
            }

            final String outputDirectory = outputTextField.getText() + File.separator + clipNameTextField.getText();

            if (!createOutputDirectory(outputDirectory)) {
                String message = String.format("Could not create output directory %s.", outputDirectory);
                DialogUtilities.showErrorDialog("Error", message, getStage());
                return;
            }

            final long startTime = startTimeDateTimePicker.getTimestamp();
            final long duration = durationSpinner.getValue();

            createButton.setDisable(true);
            cancelButton.setVisible(true);
            closeButton.setVisible(false);
            cancelRequested = false;

            processingThread = new Thread(() -> processFiles(outputDirectory, startTime, duration),
                    "clip video thread");
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
            }, "canceling clip video thread").start();
        }
    }

    private void processFiles(String outputDirectory, long startTime, long duration) {
        isProcessing = true;

        for (MediaSetView mediaSetView : tableView.getItems()) {
            if (cancelRequested) {
                updateStatus(mediaSetView, "canceled");
                continue;
            }
            if (mediaSetView.getWillProcess().isSelected()) {
                updateStatus(mediaSetView, "processing");
                String status = createClip(mediaSetView.getMediaSet(), outputDirectory, startTime, duration);
                updateStatus(mediaSetView, status);
            } else {
                updateStatus(mediaSetView, "skipped");
            }
        }

        isProcessing = false;
        processComplete();
    }

    private boolean createOutputDirectory(String outputDirectory) {
        try {
            File file = new File(outputDirectory);
            if (!file.exists()) {
                return file.mkdir();
            }
        } catch (Exception e) {
            String message = String.format("Could not create output directory %s.", outputDirectory);
            Logger.getGlobal().log(Level.WARNING, message, e);
            return false;
        }
        return true;
    }

    private String createClip(MediaSet mediaSet, String outputDirectory, long startTime, long duration) {
        MediaFile mediaFile = mediaSet.getMediaFileAt(startTime);
        String ext = mediaFile.getName().substring(mediaFile.getName().lastIndexOf("."));
        String output = String.format("%s%s%s_%s.%s",
                outputDirectory, File.separator, mediaSet,
                Utility.formatTime(startTime, "yyyy_MM_dd_HH_mm_ss_SSS"), ext);
        try {
            FFmpeg fFmpeg = new FFmpeg("external\\ffmpeg\\bin\\ffmpeg");

            //ffmpeg -ss 00:00:30.0 -i input.wmv -c copy -t 00:00:10.0 output.wmv
            fFmpeg.run(List.of("-i",
                    mediaFile.getAbsolutePath(),
                    "-ss",
                    getStartTimeString(mediaFile, startTime),
                    "-c",
                    "copy",
                    "-t",
                    formatTime(duration * 1000),
                    output));
            return "processed";
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return "failed";
    }

    private String getStartTimeString(MediaFile mediaFile, long startTime) {
        long mediaOffset = startTime - mediaFile.getStartTime();
        return formatTime(mediaOffset);
    }

    private String formatTime(long milliseconds) {
        long hours = milliseconds / 3600000;
        long minutes = (milliseconds - hours * 3600000) / 60000;
        long seconds = (milliseconds - hours * 3600000 - minutes * 60000) / 1000;
        long millis = milliseconds - hours * 3600000 - minutes * 60000 - seconds * 1000;
        return String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, millis);
    }

    private void updateStatus(MediaSetView mediaSetView, String status) {
        Platform.runLater(() -> mediaSetView.setStatus(status));
    }

    private void processComplete() {
        Platform.runLater(() -> {
            createButton.setDisable(false);
            cancelButton.setVisible(false);
            closeButton.setVisible(true);
        });
    }
}
