package gov.mil.otc._3dvis.ui.data.dataimport.media;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.DateTimePicker2;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaFileChooser extends TransparentWindow {

    private static final String UNDERSCORE_OR_PERIOD = "[_|.]";
    private final Hyperlink entityHyperlink = new Hyperlink("select entity");
    private final ComboBox<MediaTimestampFormat> filenameFormatComboBox = new ComboBox<>();
    private final Label mediaSetLabel = new Label("Media Set:");
    private final TextField mediaSetTextField = new TextField();
    private final Label dateTimeLabel = new Label("Start Time:");
    private final DateTimePicker2 dateTimePicker2 = new DateTimePicker2(TimeManager.getTime());
    private final List<File> fileList;
    private IEntity entity = null;
    private List<MediaFileView> mediaFileViewList = null;

    public static List<MediaFileView> show(List<File> files, Stage parentStage) {
        MediaFileChooser mediaFileChooser = new MediaFileChooser(files, parentStage);
        parentStage.setOpacity(.75);
        mediaFileChooser.createAndShow(true);
        parentStage.setOpacity(1.0);
        return mediaFileChooser.mediaFileViewList;
    }

    private MediaFileChooser(List<File> files, Stage parentStage) {
        super(parentStage);
        fileList = files;
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Media File Details");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        Label fileLabel = new Label();
        if (fileList.size() == 1) {
            fileLabel.setText(fileList.get(0).getAbsolutePath());
        } else {
            fileLabel.setText("multiple files");
        }
        VBox titleVBox = new VBox(UiConstants.SPACING, titleLabel, fileLabel);
        titleVBox.setAlignment(Pos.CENTER);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(2).setHalignment(HPos.RIGHT);
        gridPane.setAlignment(Pos.CENTER);

        int rowIndex = 0;

        entityHyperlink.setOnAction(event -> onSelectEntityAction());
        gridPane.add(new Label("Entity:"), 0, rowIndex);
        gridPane.add(entityHyperlink, 1, rowIndex);

        rowIndex++;

        for (MediaTimestampFormat mediaTimestampFormat : MediaTimestampFormat.values()) {
            filenameFormatComboBox.getItems().add(mediaTimestampFormat);
        }
        filenameFormatComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> filenameFormatSelectionChange());
        filenameFormatComboBox.getSelectionModel().selectFirst();
        filenameFormatComboBox.setMaxWidth(Double.MAX_VALUE);
        filenameFormatComboBox.setStyle("-fx-font-family: consolas");
        gridPane.add(new Label("Filename Format:"), 0, rowIndex);
        gridPane.add(filenameFormatComboBox, 1, rowIndex, 2, 1);

        rowIndex++;

        mediaSetTextField.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(mediaSetLabel, 0, rowIndex);
        gridPane.add(mediaSetTextField, 1, rowIndex, 2, 1);

        rowIndex++;

        gridPane.add(dateTimeLabel, 0, rowIndex);
        gridPane.add(dateTimePicker2, 1, rowIndex, 2, 1);

        Button okButton = new Button("OK");
        okButton.setOnAction(event -> onOkButton());

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        HBox closeButtonHBox = new HBox(UiConstants.SPACING, okButton, cancelButton);
        closeButtonHBox.setAlignment(Pos.BASELINE_RIGHT);
        closeButtonHBox.setPadding(new Insets(0, 0, UiConstants.SPACING, 0));

        VBox mainVBox = new VBox(UiConstants.SPACING, titleVBox, new Separator(), gridPane, new Separator(), closeButtonHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));

        return mainVBox;
    }

    private void onSelectEntityAction() {
        entity = EntityPicker.show(getStage());
        if (entity != null) {
            String name = String.format("%s %s", entity.getEntityId().toString(),
                    entity.getLastEntityDetail() != null ? entity.getLastEntityDetail().getName() : "");
            entityHyperlink.setText(name);
        } else {
            entityHyperlink.setText("select entity");
        }
    }

    private void filenameFormatSelectionChange() {
        switch (filenameFormatComboBox.getSelectionModel().getSelectedItem()) {
            case STANDARD -> {
                mediaSetLabel.setDisable(true);
                mediaSetTextField.setDisable(true);
                dateTimeLabel.setDisable(true);
                dateTimePicker2.setDisable(true);
            }
            case SHADOW -> {
                mediaSetLabel.setDisable(true);
                mediaSetTextField.setText(getShadowMediaSet());
                mediaSetTextField.setDisable(true);
                dateTimeLabel.setDisable(false);
                dateTimePicker2.setDisable(false);
            }
            case APACHE -> {
                mediaSetLabel.setDisable(false);
                mediaSetTextField.setDisable(false);
                dateTimeLabel.setDisable(true);
                dateTimePicker2.setDisable(true);
            }
            case BLACK_HAWK -> {
                mediaSetLabel.setDisable(true);
                mediaSetTextField.setDisable(true);
                mediaSetTextField.setText(getBlackHawkMediaSet());
                dateTimeLabel.setDisable(true);
                dateTimePicker2.setDisable(true);
            }
            default -> {
                mediaSetLabel.setDisable(false);
                mediaSetTextField.setDisable(false);
                dateTimeLabel.setDisable(false);
                dateTimePicker2.setDisable(false);
            }
        }
    }

    private String getShadowMediaSet() {
        String[] videoName = fileList.get(0).getName().split(UNDERSCORE_OR_PERIOD);
        if (videoName.length >= 7) {
            return videoName[1];
        }
        return "";
    }

    private String getBlackHawkMediaSet() {
        String[] videoName = fileList.get(0).getName().split(UNDERSCORE_OR_PERIOD);
        if (videoName.length == 10) {
            return videoName[2];
        }
        return "";
    }

    private void onOkButton() {
        if (entity == null) {
            DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY, "Must select an Entity.", true, getStage());
            return;
        }
        mediaFileViewList = new ArrayList<>();
        for (File file : fileList) {
            MediaFile mediaFile = processFilename(file, filenameFormatComboBox.getSelectionModel().getSelectedItem());
            if (mediaFile != null) {
                mediaFileViewList.add(new MediaFileView(mediaFile, entity));
            }
        }
        close();
    }

    private MediaFile processFilename(File file, MediaTimestampFormat mediaTimestampFormat) {
        return switch (mediaTimestampFormat) {
            case STANDARD -> processStandardFilename();
            case SHADOW -> processShadowFilename(file);
            case APACHE -> processApacheFilename();
            default -> processNoFormatFilename(file);
        };
    }

    private MediaFile processNoFormatFilename(File file) {
        long startTime = dateTimePicker2.getTimestamp();
        return new MediaFile(file.getAbsolutePath(), startTime, "", mediaSetTextField.getText());
    }

    private MediaFile processStandardFilename() {
        return null;
    }

    private MediaFile processShadowFilename(File file) {
        Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        startTime.setTimeInMillis(0);
        String[] videoName = file.getName().split(UNDERSCORE_OR_PERIOD);
        if (videoName.length >= 7) {
            try {
                if (videoName[2].isEmpty()) {
                    int dayOfYear = Integer.parseInt(videoName[3]);
                    int hour = Integer.parseInt(videoName[4]);
                    int minute = Integer.parseInt(videoName[5]);
                    int second = Integer.parseInt(videoName[6]);

                    startTime.set(Calendar.YEAR, dateTimePicker2.getYear());
                    startTime.set(Calendar.DAY_OF_YEAR, dayOfYear);
                    startTime.set(Calendar.HOUR_OF_DAY, hour);
                    startTime.set(Calendar.MINUTE, minute);
                    startTime.set(Calendar.SECOND, second);
                    startTime.set(Calendar.MILLISECOND, 0);
                } else {
                    int dayOfYear = Integer.parseInt(videoName[2]);
                    int hour = Integer.parseInt(videoName[3]);
                    int minute = Integer.parseInt(videoName[4]);
                    int second = Integer.parseInt(videoName[5]);

                    startTime.set(Calendar.YEAR, dateTimePicker2.getYear());
                    startTime.set(Calendar.DAY_OF_YEAR, dayOfYear);
                    startTime.set(Calendar.HOUR_OF_DAY, hour);
                    startTime.set(Calendar.MINUTE, minute);
                    startTime.set(Calendar.SECOND, second);
                    startTime.set(Calendar.MILLISECOND, 0);
                }

                return new MediaFile(file.getAbsolutePath(), startTime.getTimeInMillis(), "", videoName[1]);
            } catch (NumberFormatException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
        return null;
    }

    private MediaFile processApacheFilename() {
        return null;
    }
}
