package gov.mil.otc._3dvis.ui.data.dataimport;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.otcam.OtcamUtility;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.event.MunitionFireEvent;
import gov.mil.otc._3dvis.event.otcam.OtcamEvent;
import gov.mil.otc._3dvis.project.dlm.DlmPlaybackEntity;
import gov.mil.otc._3dvis.project.dlm.IDlmEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class OtcamImportController extends TransparentWindow {

    private final Hyperlink fileHyperlink = new Hyperlink();
    private final Label statusLabel = new Label();
    private final Label startTimeLabel = new Label();
    private final Label stopTimeLabel = new Label();
    private final Label entityCountLabel = new Label();
    private final Label eventCountLabel = new Label();
    private final Button importButton = new Button("Import");
    private final Button cancelButton = new Button("Cancel");
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Common.DATE_TIME_WITH_MILLIS);
    private boolean isClosed = false;
    private File selectedFile = null;
    private long startTime = -1;
    private long stopTime = -1;
    private List<EntityId> entityIds;
    private List<Event> events;

    public static void show() {
        new OtcamImportController().createAndShow();
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Import OTCAM Database");
        titleLabel.setFont(Font.font(UiConstants.FONT_NAME, FontWeight.BOLD, 18));
        fileHyperlink.setOnAction(event -> selectDatabaseFile());
        VBox titleVBox = new VBox(UiConstants.SPACING, titleLabel, fileHyperlink);
        titleVBox.setAlignment(Pos.CENTER);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);

        int rowIndex = 0;

        gridPane.add(new Label("Status:"), 0, rowIndex);
        gridPane.add(statusLabel, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Start Time:"), 0, rowIndex);
        gridPane.add(startTimeLabel, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Stop Time:"), 0, rowIndex);
        gridPane.add(stopTimeLabel, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Entity Count:"), 0, rowIndex);
        gridPane.add(entityCountLabel, 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Event Count:"), 0, rowIndex);
        gridPane.add(eventCountLabel, 1, rowIndex);

        importButton.setOnAction(event -> startImport());
        cancelButton.setOnAction(event -> close());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, importButton, cancelButton);
        buttonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING, titleVBox, new Separator(), gridPane, new Separator(), buttonsHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(500);

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        return selectDatabaseFile();
    }

    @Override
    protected boolean closeRequested() {
        isClosed = true;
        return true;
    }

    private boolean selectDatabaseFile() {
        String userDirectoryPath = System.getProperty("user.home");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("otcamimport"));
        selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile != null) {
            fileHyperlink.setText(selectedFile.getAbsolutePath());
            SettingsManager.getPreferences().setLastDirectory("otcamimport", selectedFile.getParent());
            loadOtcamDatabase();
            return true;
        }
        return false;
    }

    private void loadOtcamDatabase() {
        statusLabel.setText("loading...");
        importButton.setDisable(true);
        cancelButton.setText("Cancel");
        new Thread(() -> {
            OtcamUtility otcamUtility = new OtcamUtility(selectedFile.getAbsolutePath());
            entityIds = otcamUtility.getEntityIds();
            events = otcamUtility.getEvents();
            List<Long> startStopTime = otcamUtility.getStartStopTime();

            if (startStopTime.size() != 2) {
                Platform.runLater(() -> statusLabel.setText("error loading database"));
                return;
            }

            startTime = startStopTime.get(0);
            stopTime = startStopTime.get(1);

            Platform.runLater(() -> {
                if (!isClosed) {
                    statusLabel.setText("database loaded successfully");
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    calendar.setTimeInMillis(startTime);
                    startTimeLabel.setText(simpleDateFormat.format(calendar.getTime()));
                    calendar.setTimeInMillis(stopTime);
                    stopTimeLabel.setText(simpleDateFormat.format(calendar.getTime()));
                    entityCountLabel.setText(String.valueOf(entityIds.size()));
                    eventCountLabel.setText(String.valueOf(events.size()));
                    importButton.setDisable(false);
                }
            });
        }, "Load OTCAM Database").start();
    }

    private void startImport() {
        statusLabel.setText("importing...");
        importButton.setDisable(true);
        new Thread(this::doImport, "Load OTCAM Database").start();
    }

    private void doImport() {
        DataSource dataSource = DataManager.createDataSource(selectedFile.getAbsolutePath(), startTime, stopTime);
        for (EntityId entityId : entityIds) {
            IEntity entity = EntityManager.getEntity(entityId);
            if (entity == null) {
                entity = new PlaybackEntity(entityId);
                EntityManager.addEntity(entity, true);
            }
            OtcamUtility otcamUtility = new OtcamUtility(selectedFile.getAbsolutePath());
            otcamUtility.getEntityScopes(entity, dataSource.getId());
            otcamUtility.getEntityDetails(entity, dataSource.getId());
            otcamUtility.getTspi(entity, dataSource.getId());
            if (otcamUtility.isDlm(entityId)) {
                if (!(entity instanceof IDlmEntity) &&
                        entity instanceof AbstractEntity) {
                    entity = new DlmPlaybackEntity((AbstractEntity) entity);
                    EntityManager.addEntity(entity, true);
                }
                if (entity instanceof IDlmEntity) {
                    otcamUtility.getDlmMessages((IDlmEntity) entity, dataSource.getId());
                }
            }
        }
        importEvents(dataSource.getId());
        entityIds.clear();
        events.clear();
        Platform.runLater(() -> {
            if (!isClosed) {
                statusLabel.setText("database imported");
                cancelButton.setText("Close");
            }
        });
    }

    private void importEvents(int sourceId) {
        for (Event event : events) {
            EventManager.addEvent(event);
            DatabaseLogger.addEvent(event, sourceId);
            if (event instanceof OtcamEvent) {
                OtcamEvent otcamEvent = (OtcamEvent) event;
                IEntity target = EntityManager.getEntity(otcamEvent.getTargetId());
                if (target != null) {
                    target.addEvent(otcamEvent);
                }
            } else if (event instanceof MunitionFireEvent) {
                MunitionFireEvent munitionFireEvent = (MunitionFireEvent) event;
                IEntity shooter = EntityManager.getEntity(munitionFireEvent.getShooterId());
                if (shooter != null) {
                    shooter.addEvent(munitionFireEvent);
                }
            }
        }
    }
}
