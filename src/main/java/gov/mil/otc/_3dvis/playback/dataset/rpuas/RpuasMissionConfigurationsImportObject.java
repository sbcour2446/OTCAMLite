package gov.mil.otc._3dvis.playback.dataset.rpuas;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.rpuas.DeviceConfiguration;
import gov.mil.otc._3dvis.project.rpuas.DeviceInformation;
import gov.mil.otc._3dvis.project.rpuas.MissionConfiguration;
import gov.mil.otc._3dvis.project.rpuas.MissionConfigurations;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
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

public class RpuasMissionConfigurationsImportObject extends ImportObject<MissionConfigurations> {

    public static RpuasMissionConfigurationsImportObject scanAndCreate(File file) {
        MissionConfigurations missionConfigurations = MissionConfigurations.load(file);
        return new RpuasMissionConfigurationsImportObject(missionConfigurations);
    }

    public static RpuasMissionConfigurationsImportObject createTemplate(String path, String filename) {
        MissionConfigurations missionConfigurations = new MissionConfigurations();
        DeviceInformation deviceInformation = new DeviceInformation("device 1", "skydio");
        missionConfigurations.addDeviceInformation(deviceInformation);
        deviceInformation = new DeviceInformation("device 2", "teal");
        missionConfigurations.addDeviceInformation(deviceInformation);
        MissionConfiguration missionConfiguration = new MissionConfiguration("mission 1",
                Utility.parseTime("2024-05-01 00:00", "yyyy-MM-dd HH:mm"),
                Utility.parseTime("2024-05-01 23:59", "yyyy-MM-dd HH:mm"));
        DeviceConfiguration deviceConfiguration = new DeviceConfiguration(missionConfiguration.getMissionName(),
                "device 1", Affiliation.FRIENDLY, 6000);
        missionConfiguration.addDeviceConfiguration(deviceConfiguration);
        deviceConfiguration = new DeviceConfiguration(missionConfiguration.getMissionName(),
                "device 2", Affiliation.HOSTILE, 6001);
        missionConfiguration.addDeviceConfiguration(deviceConfiguration);
        missionConfigurations.addMissionConfiguration(missionConfiguration);

        missionConfiguration = new MissionConfiguration("mission 2",
                Utility.parseTime("2024-05-02 00:00", "yyyy-MM-dd HH:mm"),
                Utility.parseTime("2024-05-02 23:59", "yyyy-MM-dd HH:mm"));
        deviceConfiguration = new DeviceConfiguration(missionConfiguration.getMissionName(),
                "device 1", Affiliation.HOSTILE, 6000);
        missionConfiguration.addDeviceConfiguration(deviceConfiguration);
        deviceConfiguration = new DeviceConfiguration(missionConfiguration.getMissionName(),
                "device 2", Affiliation.FRIENDLY, 6001);
        missionConfiguration.addDeviceConfiguration(deviceConfiguration);
        missionConfigurations.addMissionConfiguration(missionConfiguration);
        missionConfigurations.save(path, filename);
        return new RpuasMissionConfigurationsImportObject(missionConfigurations);
    }

    private final TableView<RpuasMissionConfigurationImportObject> missionConfigurationTableView = new TableView<>();

    public RpuasMissionConfigurationsImportObject(MissionConfigurations object) {
        super(object, "mission configuration");
        initialize();
    }

    private void initialize() {
        TableColumn<RpuasMissionConfigurationImportObject, String> missionTableColumn = new TableColumn<>("Mission");
        missionTableColumn.setCellValueFactory(new PropertyValueFactory<>("missionName"));

        TableColumn<RpuasMissionConfigurationImportObject, Long> startTimeTableColumn = new TableColumn<>("Start Time");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeTableColumn.setCellFactory(new TimestampTableColumnCellFactory<>());

        TableColumn<RpuasMissionConfigurationImportObject, Long> stopTimeTableColumn = new TableColumn<>("Stop Time");
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
                    RpuasMissionConfigurationImportObject rpuasMissionConfigurationImportObject
                            = new RpuasMissionConfigurationImportObject(newConfiguration, newConfiguration.getMissionName());
                    rpuasMissionConfigurationImportObject.setModified(true);
                    missionConfigurationTableView.getItems().add(rpuasMissionConfigurationImportObject);
                } else {
                    RpuasMissionConfigurationImportObject rpuasMissionConfigurationImportObject
                            = new RpuasMissionConfigurationImportObject(newConfiguration, newConfiguration.getMissionName());
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
                    RpuasMissionConfigurationImportObject rpuasMissionConfigurationImportObject
                            = new RpuasMissionConfigurationImportObject(newConfiguration, newConfiguration.getMissionName());
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
//        for (MissionConfigurationImportObject missionConfigurationImportObject : missionConfigurationTableView.getItems()) {
//            if (missionConfigurationImportObject.isNew() || missionConfigurationImportObject.isModified()) {
//                missionConfigurationImportObject.doImport();
//            }
//        }
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    public MissionConfiguration getMissionConfiguration(String missionName) {
        return getObject().getMissionConfiguration(missionName);
    }

    private static class MissionTableRow extends TableRow<RpuasMissionConfigurationImportObject> {

        @Override
        public void updateItem(RpuasMissionConfigurationImportObject item, boolean empty) {
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
