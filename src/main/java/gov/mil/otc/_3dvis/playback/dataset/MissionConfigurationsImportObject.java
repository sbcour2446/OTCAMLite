package gov.mil.otc._3dvis.playback.dataset;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.data.mission.MissionConfiguration;
import gov.mil.otc._3dvis.data.mission.MissionConfigurations;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgets.cellfactory.TimestampTableColumnCellFactory;
import gov.mil.otc._3dvis.utility.Utility;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.List;

public class MissionConfigurationsImportObject extends ImportObject<MissionConfigurations> {

    public static String FILE_NAME = "missionConfigurations.json";

    public static boolean isMissionConfigurationFile(File file) {
        return !file.isDirectory() && file.getName().equalsIgnoreCase(FILE_NAME);
    }

    public static MissionConfigurationsImportObject scanAndCreate(File file) {
        MissionConfigurations missionConfigurations = MissionConfigurations.load(file);
        return new MissionConfigurationsImportObject(missionConfigurations);
    }

    public static MissionConfigurationsImportObject createTemplate(String path, String filename) {
        MissionConfigurations missionConfigurations = new MissionConfigurations();
        MissionConfiguration missionConfiguration = new MissionConfiguration("mission 1",
                Utility.parseTime("2024-05-01 00:00", "yyyy-MM-dd HH:mm"),
                Utility.parseTime("2024-05-01 23:59", "yyyy-MM-dd HH:mm"));
        missionConfigurations.addMissionConfiguration(missionConfiguration);

        missionConfiguration = new MissionConfiguration("mission 2",
                Utility.parseTime("2024-05-02 00:00", "yyyy-MM-dd HH:mm"),
                Utility.parseTime("2024-05-02 23:59", "yyyy-MM-dd HH:mm"));
        missionConfigurations.addMissionConfiguration(missionConfiguration);
        missionConfigurations.save(path, filename);
        return new MissionConfigurationsImportObject(missionConfigurations);
    }

    private final TableView<MissionConfigurationImportObject> missionConfigurationTableView = new TableView<>();

    public MissionConfigurationsImportObject(MissionConfigurations object) {
        super(object, "mission configuration");
        initialize();
    }

    private void initialize() {
        TableColumn<MissionConfigurationImportObject, String> missionTableColumn = new TableColumn<>("Mission");
        missionTableColumn.setCellValueFactory(new PropertyValueFactory<>("missionName"));

        TableColumn<MissionConfigurationImportObject, Long> startTimeTableColumn = new TableColumn<>("Start Time");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeTableColumn.setCellFactory(new TimestampTableColumnCellFactory<>());

        TableColumn<MissionConfigurationImportObject, Long> stopTimeTableColumn = new TableColumn<>("Stop Time");
        stopTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("stopTime"));
        stopTimeTableColumn.setCellFactory(new TimestampTableColumnCellFactory<>());

        missionConfigurationTableView.getColumns().add(missionTableColumn);
        missionConfigurationTableView.getColumns().add(startTimeTableColumn);
        missionConfigurationTableView.getColumns().add(stopTimeTableColumn);

        missionConfigurationTableView.setPlaceholder(new Label("no configuration available"));
        missionConfigurationTableView.setRowFactory(param -> new MissionTableRow());
        missionConfigurationTableView.setSelectionModel(null);

        URL url = ThemeHelper.class.getResource("/css/import_data_object_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            missionConfigurationTableView.getStylesheets().add(css);
        }

        fillTable();
    }

    private void fillTable() {
        List<Mission> currentMissions = DataManager.getMissions();

        if (getObject() != null && getObject().getMissionConfigurationList() != null) {
            for (Mission mission : currentMissions) {
                MissionConfiguration newConfiguration = getObject().getMissionConfiguration(mission.getName());
                if (newConfiguration == null) {
                    continue;
                }
                if (!mission.getName().equals(newConfiguration.getMissionName())) {
                    MissionConfigurationImportObject rpuasMissionConfigurationImportObject
                            = new MissionConfigurationImportObject(newConfiguration, newConfiguration.getMissionName());
                    rpuasMissionConfigurationImportObject.setModified(true);
                    missionConfigurationTableView.getItems().add(rpuasMissionConfigurationImportObject);
                } else {
                    MissionConfigurationImportObject rpuasMissionConfigurationImportObject
                            = new MissionConfigurationImportObject(newConfiguration, newConfiguration.getMissionName());
                    rpuasMissionConfigurationImportObject.setImported(true);
                    missionConfigurationTableView.getItems().add(rpuasMissionConfigurationImportObject);
                }
            }
            for (MissionConfiguration newConfiguration : getObject().getMissionConfigurationList()) {
                boolean alreadyAdded = false;
                for (Mission mission : currentMissions) {
                    if (newConfiguration.getMissionName().equalsIgnoreCase(mission.getName())) {
                        alreadyAdded = true;
                        break;
                    }
                }
                if (!alreadyAdded) {
                    MissionConfigurationImportObject rpuasMissionConfigurationImportObject
                            = new MissionConfigurationImportObject(newConfiguration, newConfiguration.getMissionName());
                    rpuasMissionConfigurationImportObject.setNew(true);
                    setNew(true);
                    missionConfigurationTableView.getItems().add(rpuasMissionConfigurationImportObject);
                }
            }
        } else {
            setMissing(true);
        }
    }

    @Override
    public VBox getDisplayPane() {
        return new VBox(UiConstants.SPACING, missionConfigurationTableView);
    }

    @Override
    public void doImport() {
        for (MissionConfiguration missionConfiguration : getObject().getMissionConfigurationList()) {
            DataManager.addMission(new Mission(missionConfiguration.getMissionName(),
                    missionConfiguration.getStartTime(), missionConfiguration.getStopTime()));
        }
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    public MissionConfiguration getMissionConfiguration(String missionName) {
        return getObject().getMissionConfiguration(missionName);
    }

    private static class MissionTableRow extends TableRow<MissionConfigurationImportObject> {

        @Override
        public void updateItem(MissionConfigurationImportObject item, boolean empty) {
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
