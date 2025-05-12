package gov.mil.otc._3dvis.playback.dataset.avcad;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.project.avcad.AvcadConfiguration;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.TextWithStyleClass;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.File;

public class AvcadConfigurationImportObject extends ImportObject<AvcadConfiguration> {

    public static final String NAME = "avcadConfiguration.json";

    public static AvcadConfigurationImportObject scanAndCreate(File file) {
        AvcadConfiguration avcadConfiguration = AvcadConfiguration.load(file);
        return new AvcadConfigurationImportObject(avcadConfiguration);
    }

    public AvcadConfigurationImportObject(AvcadConfiguration object) {
        super(object, "AVCAD Configuration");
    }

    @Override
    public VBox getDisplayPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(UiConstants.SPACING));
        gridPane.setHgap(UiConstants.SPACING);
        gridPane.setVgap(UiConstants.SPACING);
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().add(new ColumnConstraints());
        gridPane.getColumnConstraints().get(0).setHalignment(HPos.RIGHT);
        gridPane.getColumnConstraints().get(1).setHgrow(Priority.ALWAYS);

        int rowIndex = 0;

        gridPane.add(new TextWithStyleClass("Time Zone Offset:"), 0, rowIndex);
        gridPane.add(new Label(String.valueOf(getObject().getTimeZonOffset())), 1, rowIndex);

        rowIndex++;

        ListView<String> alarmListView = new ListView<>();
        if (getObject().getAlarmList().isEmpty()) {
            alarmListView.getItems().add("---EMPTY---");
        } else {
            for (String alarm : getObject().getAlarmList()) {
                alarmListView.getItems().add(alarm);
            }
        }
        alarmListView.setMaxHeight(200);
        TextWithStyleClass alarmLabel = new TextWithStyleClass("Alarms:");
        gridPane.add(alarmLabel, 0, rowIndex);
        gridPane.add(alarmListView, 1, rowIndex);
        GridPane.setValignment(alarmLabel, VPos.TOP);

        rowIndex++;

        ListView<String> alertListView = new ListView<>();
        if (getObject().getAlertList().isEmpty()) {
            alertListView.getItems().add("---EMPTY---");
        } else {
            for (String alert : getObject().getAlertList()) {
                alertListView.getItems().add(alert);
            }
        }
        alertListView.setMaxHeight(200);
        TextWithStyleClass alertLabel = new TextWithStyleClass("Alerts:");
        gridPane.add(alertLabel, 0, rowIndex);
        gridPane.add(alertListView, 1, rowIndex);
        GridPane.setValignment(alertLabel, VPos.TOP);

        return new VBox(UiConstants.SPACING, gridPane);
    }

    @Override
    public void doImport() {
        // not implemented
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }
//
//    private boolean process() {
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(getObject().getAbsolutePath()))) {
//            String line;
//            if ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
//                offset = Integer.parseInt(line);
//            }
//        } catch (Exception e) {
//            String message = String.format("SensorTimeZoneImportObject::process:Error processing file %s",
//                    getObject().getAbsolutePath());
//            Logger.getGlobal().log(Level.WARNING, message, e);
//            return false;
//        }
//        return true;
//    }
}
