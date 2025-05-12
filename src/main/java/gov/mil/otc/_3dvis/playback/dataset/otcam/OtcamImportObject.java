package gov.mil.otc._3dvis.playback.dataset.otcam;

import gov.mil.otc._3dvis.data.otcam.OtcamLoader;
import gov.mil.otc._3dvis.data.otcam.OtcamUtility;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class OtcamImportObject extends ImportObject<File> {

    public static OtcamImportObject scanAndCreate(File file) {
        OtcamImportObject otcamImportObject = new OtcamImportObject(file, file.getName());
        if (otcamImportObject.verify()) {
            return otcamImportObject;
        }
        return null;
    }

    public OtcamImportObject(File object, String name) {
        super(object, name);
    }

    private boolean verify() {
        OtcamUtility otcamUtility = new OtcamUtility(getObject().getAbsolutePath());
        return otcamUtility.open();
    }

    public VBox getDisplayPane() {
        OtcamUtility otcamUtility = new OtcamUtility(getObject().getAbsolutePath());
        List<EntityId> entityIds = otcamUtility.getEntityIds();
        List<Event> events = otcamUtility.getEvents();
        List<Long> startStopTime = otcamUtility.getStartStopTime();
        String statusString = "";
        if (startStopTime.size() != 2) {
            statusString = "error loading database";
        }
        long startTime = startStopTime.get(0);
        long stopTime = startStopTime.get(1);


        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(0, UiConstants.SPACING, 0, UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().getFirst().setHalignment(HPos.RIGHT);

        int rowIndex = 0;

        gridPane.add(new Label("Status:"), 0, rowIndex);
        gridPane.add(new Label(statusString), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Start Time:"), 0, rowIndex);
        gridPane.add(new Label(Utility.formatTime(startTime)), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Stop Time:"), 0, rowIndex);
        gridPane.add(new Label(Utility.formatTime(stopTime)), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Entity Count:"), 0, rowIndex);
        gridPane.add(new Label(String.valueOf(entityIds.size())), 1, rowIndex);

        rowIndex++;

        gridPane.add(new Label("Event Count:"), 0, rowIndex);
        gridPane.add(new Label(String.valueOf(events.size())), 1, rowIndex);

        VBox vBox = new VBox(UiConstants.SPACING, gridPane);
        vBox.setPadding(new Insets(UiConstants.SPACING));
        return vBox;
    }

    @Override
    public void doImport() {
        if ((!isNew() && !isModified()) || isMissing()) {
            return;
        }

        OtcamLoader.loadAsync(getObject().getAbsolutePath());
//        OtcamUtility otcamUtility = new OtcamUtility(getObject().getAbsolutePath());
//        List<EntityId> entityIds = otcamUtility.getEntityIds();
//        List<Event> events = otcamUtility.getEvents();
//        List<Long> startStopTime = otcamUtility.getStartStopTime();
//        if (startStopTime.size() != 2) {
//            return;
//        }
//        long startTime = startStopTime.get(0);
//        long stopTime = startStopTime.get(1);
//
////        DataSource dataSource = DataManager.createDataSource(getObject().getAbsolutePath(), startTime, stopTime);
//        int counter = 0;
//        for (EntityId entityId : entityIds) {
//            IEntity entity = EntityManager.getEntity(entityId);
//            if (entity == null) {
//                entity = new PlaybackEntity(entityId);
//                EntityManager.addEntity(entity);
//            }
//            otcamUtility.getEntityScopes(entity, -1);//dataSource.getId());
//            otcamUtility.getEntityDetails(entity, -1);//dataSource.getId());
//            otcamUtility.getTspi(entity, -1);//dataSource.getId());
//            if (otcamUtility.isDlm(entityId)) {
//                if (!(entity instanceof IDlmEntity) &&
//                        entity instanceof AbstractEntity) {
//                    entity = new DlmPlaybackEntity((AbstractEntity) entity);
//                    EntityManager.addEntity(entity);
//                }
//                if (entity instanceof IDlmEntity) {
//                    otcamUtility.getDlmMessages((IDlmEntity) entity, -1);//dataSource.getId());
//                }
//            }
//        }
//
//        for (Event event : events) {
//            EventManager.addEvent(event);
//            if (event instanceof OtcamEvent otcamEvent) {
//                IEntity target = EntityManager.getEntity(otcamEvent.getTargetId());
//                if (target != null) {
//                    target.addEvent(otcamEvent);
//                }
//            } else if (event instanceof MunitionFireEvent munitionFireEvent) {
//                IEntity shooter = EntityManager.getEntity(munitionFireEvent.getShooterId());
//                if (shooter != null) {
//                    shooter.addEvent(munitionFireEvent);
//                }
//            }
//        }
//
//        otcamUtility.close();

        setImported(true);
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }
}
