package gov.mil.otc._3dvis.ui.contextmenu;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.staticentity.StaticEntity;
import gov.mil.otc._3dvis.media.MediaCollection;
import gov.mil.otc._3dvis.media.MediaGroup;
import gov.mil.otc._3dvis.media.MediaPlayerManager;
import gov.mil.otc._3dvis.media.MediaSet;
import gov.mil.otc._3dvis.project.avcad.LidarEntity;
import gov.mil.otc._3dvis.project.avcad.LidarSettingsWidgetPane;
import gov.mil.otc._3dvis.project.avcad.SensorEntity;
import gov.mil.otc._3dvis.project.avcad.SensorStatusWidgetPane;
import gov.mil.otc._3dvis.project.blackhawk.BlackHawkEntity;
import gov.mil.otc._3dvis.project.blackhawk.SurveyDataTable;
import gov.mil.otc._3dvis.project.dlm.IDlmEntity;
import gov.mil.otc._3dvis.project.dlm.launchtable.LaunchTable;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvEntity;
import gov.mil.otc._3dvis.project.nbcrv.SidecarEntity;
import gov.mil.otc._3dvis.project.rpuas.RpuasEntity;
import gov.mil.otc._3dvis.project.rpuas.RpuasStatusWidgetPane;
import gov.mil.otc._3dvis.time.TimeManager;
import gov.mil.otc._3dvis.tools.FileViewer;
import gov.mil.otc._3dvis.ui.projects.nbcrv.NbcrvRadNucWidgetPane;
import gov.mil.otc._3dvis.ui.projects.nbcrv.NbcrvStatusWidgetPane;
import gov.mil.otc._3dvis.ui.projects.nbcrv.SidecarStatusWidgetPane;
import gov.mil.otc._3dvis.ui.utility.CsvViewer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.utility.staticentity.CreateStaticEntityController;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

public class ContextMenuController {

    private static final ContextMenuController SINGLETON = new ContextMenuController();
    private Stage mainStage;

    public static void initialize(Stage stage) {
        SINGLETON.mainStage = stage;
    }

    public static void show(final int x, final int y) {
        if (Platform.isFxApplicationThread()) {
            SINGLETON.doShow(x, y);
        } else {
            Platform.runLater(() -> SINGLETON.doShow(x, y));
        }
    }

    public static void show(final int x, final int y, final IEntity entity) {
        if (Platform.isFxApplicationThread()) {
            SINGLETON.doShow(x, y, entity);
        } else {
            Platform.runLater(() -> SINGLETON.doShow(x, y, entity));
        }
    }

    private void doShow(int x, int y) {
        final Position position = WWController.getWorldWindowPanel().getCurrentPosition();

        if (position == null) {
            return;
        }

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);
        ThemeHelper.applyTheme(contextMenu.getScene());

        Menu menu = createCopyLocationMenu(position);
        contextMenu.getItems().add(menu);

        Label staticLabel = new Label("Add static");
        ImageView staticImage = new ImageView(ImageLoader.getFxImage("/images/static/static_small.png"));
        staticLabel.setGraphic(staticImage);
        CustomMenuItem customMenuItem = new CustomMenuItem(staticLabel);
        customMenuItem.setOnAction(event -> CreateStaticEntityController.show(position));
        contextMenu.getItems().add(customMenuItem);

        contextMenu.show(mainStage, x, y);
    }

    private void doShow(int x, int y, IEntity entity) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setAnchorLocation(PopupWindow.AnchorLocation.CONTENT_BOTTOM_LEFT);
        ThemeHelper.applyTheme(contextMenu.getScene());

        MenuItem titleItem = new MenuItem();
        titleItem.setText(entity.getName());
        contextMenu.getItems().add(titleItem);

//        contextMenu.getItems().add(createDisplayMenu(entity));

        Position position = entity.getPosition();
        contextMenu.getItems().add(createCopyLocationMenu(position));

        entity.addContextMenuItems(contextMenu);

//        if (entity.supportsRtcaCommands()) {
//            Menu menu = new Menu("Send Command");
//            contextMenu.getItems().add(menu);
//            for (RtcaCommand.Type type : RtcaCommand.Type.values()) {
//                MenuItem menuItem = new MenuItem(type.toString());
//                menuItem.setOnAction(event -> entity.sendRtcaCommand(new RtcaCommand(entity, type)));
//                menu.getItems().add(menuItem);
//            }
//        }
//
//        Menu trackingMenu = new Menu("Tracking");
//        CheckMenuItem checkMenuItem = new CheckMenuItem("Enabled");
//        checkMenuItem.setSelected(entity.getTrackingAttribute().isEnabled());
//        checkMenuItem.setOnAction(event -> entity.getTrackingAttribute().setEnabled(checkMenuItem.isSelected()));
//        trackingMenu.getItems().add(checkMenuItem);
//        MenuItem settingsMenuItem = new MenuItem("Settings");
//        settingsMenuItem.setOnAction(event -> TrackingSettingsController.show());
//        trackingMenu.getItems().add(settingsMenuItem);
//        contextMenu.getItems().add(trackingMenu);

        if (entity.getMediaCollection().hasMedia()) {
            contextMenu.getItems().add(createMediaMenu(entity));
        }

        if (!entity.getAllTirs().isEmpty()) {
            contextMenu.getItems().add(createTirMenu(entity));
        }

        if (entity instanceof BlackHawkEntity) {
            Menu menu = createBlackHawkMenu((BlackHawkEntity) entity);
            if (menu != null) {
                contextMenu.getItems().add(menu);
            }
        }

        if (entity instanceof IDlmEntity) {
            contextMenu.getItems().add(createDlmEntityMenuItem((IDlmEntity) entity));
        }

        if (entity instanceof StaticEntity) {
            contextMenu.getItems().addAll(createStaticMenuItems(entity));
        }

        if (entity instanceof NbcrvEntity) {
            contextMenu.getItems().addAll(createNbcrvMenuItems((NbcrvEntity) entity));
        }

        if (entity instanceof SidecarEntity) {
//            contextMenu.getItems().addAll(createSidecarMenuItems(entity));
        }

        if (entity instanceof SensorEntity sensorEntity) {
            contextMenu.getItems().addAll(createSensorEntityMenuItems(sensorEntity));
        }

        if (entity instanceof LidarEntity lidarEntity) {
            contextMenu.getItems().addAll(createLidarSettingsMenuItems(lidarEntity));
        }

        if (entity instanceof RpuasEntity rpuasEntity) {
            contextMenu.getItems().addAll(createRpuasEntityMenuItems(rpuasEntity));
        }

        if (!(entity instanceof StaticEntity)) {
            MenuItem menuItem = new MenuItem("Go to first TSPI");
            menuItem.setOnAction(event -> {
                TspiData tspi = entity.getFirstTspi();
                if (tspi != null) {
                    TimeManager.setTime(tspi.getTimestamp());
                }
            });
            contextMenu.getItems().add(menuItem);
        }

        contextMenu.show(mainStage, x, y);
    }

    private Menu createDisplayMenu(IEntity entity) {
        Menu menu = new Menu("Display");

//        final CheckMenuItem highlightCheckMenuItem = new CheckMenuItem("Highlight");
//        highlightCheckMenuItem.setSelected(entity.isMarked());
//        highlightCheckMenuItem.setOnAction(event -> entity.setMarked(highlightCheckMenuItem.isSelected()));
//        menu.getItems().add(highlightCheckMenuItem);
//
//        final CheckMenuItem showLabelCheckMenuItem = new CheckMenuItem("Show Label");
//        showLabelCheckMenuItem.setSelected(entity.isShowLabel());
//        showLabelCheckMenuItem.setOnAction(event -> entity.showLabel(showLabelCheckMenuItem.isSelected()));
//        menu.getItems().add(showLabelCheckMenuItem);
//
//        final ColorPicker colorPicker = new ColorPicker(entity.getLabelColor());
//        CustomMenuItem customMenuItem = new CustomMenuItem(colorPicker);
//        customMenuItem.setHideOnClick(false);
//        colorPicker.setOnAction(event -> {
//            entity.setLabelColor(colorPicker.getValue());
//        });
//        menu.getItems().add(customMenuItem);

        return menu;
    }

    private Menu createMediaMenu(IEntity entity) {
        Menu menu = new Menu("Show Media");
        MediaCollection mediaCollection = entity.getMediaCollection();
        for (MediaGroup mediaGroup : mediaCollection.getMediaGroups()) {
            Menu groupMenu;
            if (mediaCollection.getMediaGroups().size() == 1) {
                groupMenu = menu;
            } else {
                groupMenu = new Menu(mediaGroup.getName());
                menu.getItems().add(groupMenu);
            }
            List<MediaSet> mediaSets = mediaGroup.getMediaSets();
            if (mediaSets.size() > 1) {
                MenuItem menuItem = new MenuItem("Show All");
                menuItem.setOnAction(event -> MediaPlayerManager.showAll(entity, mediaGroup.getName()));
                groupMenu.getItems().add(menuItem);
            }
            for (MediaSet mediaSet : mediaSets) {
                MenuItem menuItem = new MenuItem(mediaSet.getName());
                menuItem.setOnAction(event -> MediaPlayerManager.show(entity, mediaSet));
                groupMenu.getItems().add(menuItem);
            }
        }
        return menu;
    }

    private Menu createTirMenu(IEntity entity) {
        Menu menu = new Menu("Show TIR");

        for (TimedFile timedFile : entity.getAllTirs()) {
            MenuItem menuItem = new MenuItem(timedFile.getFile().getName());
            menuItem.setOnAction(event -> {
                if (timedFile.getFileType() == TimedFile.FileType.CSV) {
                    CsvViewer.show(timedFile.getFile());
                } else {
                    FileViewer.show(timedFile.getFile());
                }
            });
            menu.getItems().add(menuItem);
        }

        return menu;
    }

    private Menu createBlackHawkMenu(final BlackHawkEntity entity) {
        List<Mission> currentMissions = DataManager.getCurrentMissions();
        List<String> allRoles = new ArrayList<>();
        for (Mission mission : currentMissions) {
            List<String> roles = DataManager.getRoles(mission, entity.getEntityId().getId());
            for (String role : roles) {
                if (!allRoles.contains(role)) {
                    allRoles.add(role);
                }
            }
        }

        if (allRoles.isEmpty()) {
            return null;
        }

        Menu menu = new Menu("Surveys");
        for (String role : allRoles) {
            MenuItem menuItem = new MenuItem(role);
            menuItem.setOnAction(event -> SurveyDataTable.show(entity, role));
            menu.getItems().add(menuItem);
        }

        return menu;
    }

    private MenuItem createDlmEntityMenuItem(final IDlmEntity entity) {
        MenuItem menuItem = new MenuItem("Launch List");
        menuItem.setOnAction(event -> LaunchTable.show(entity));
        return menuItem;
    }

    private MenuItem[] createStaticMenuItems(final IEntity entity) {
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(actionEvent -> {
            EntityManager.removeEntity(entity);
        });
        MenuItem editMenuItem = new MenuItem("Edit");
        editMenuItem.setOnAction(actionEvent -> CreateStaticEntityController.show(entity));
        return new MenuItem[]{deleteMenuItem, editMenuItem};
    }

    private MenuItem[] createNbcrvMenuItems(NbcrvEntity entity) {
        List<MenuItem> menuItems = new ArrayList<>();

        MenuItem menuItem = new MenuItem("Show Status View");
        menuItem.setOnAction(event -> NbcrvStatusWidgetPane.show(entity));
        menuItems.add(menuItem);

        CheckMenuItem checkMenuItem = new CheckMenuItem("Show Rad/Nuc Compass");
        checkMenuItem.setSelected(NbcrvRadNucWidgetPane.isShowing(entity));
        checkMenuItem.setOnAction(event -> NbcrvRadNucWidgetPane.show(entity, checkMenuItem.isSelected()));
        menuItems.add(checkMenuItem);

        if (!entity.getTimedFileList().isEmpty()) {
            Menu menu = new Menu("Manual Data");
            menuItems.add(menu);
            for (TimedFile timedFile : entity.getTimedFileList()) {
                menuItem = new MenuItem(timedFile.getFile().getName());
                menuItem.setOnAction(event -> {
                    if (timedFile.getFileType() == TimedFile.FileType.CSV) {
                        CsvViewer.show(timedFile.getFile());
                    } else {
                        FileViewer.show(timedFile.getFile());
                    }
                });
                menu.getItems().add(menuItem);
            }
        }

        return menuItems.toArray(new MenuItem[0]);
    }

    private MenuItem[] createSidecarMenuItems(IEntity entity) {
        MenuItem menuItem = new MenuItem("Show Status View");
        menuItem.setOnAction(event -> SidecarStatusWidgetPane.show(entity));
        return new MenuItem[]{menuItem};
    }

    private MenuItem[] createSensorEntityMenuItems(SensorEntity sensorEntity) {
        MenuItem menuItem = new MenuItem("Show Status");
        menuItem.setOnAction(event -> SensorStatusWidgetPane.show());
        return new MenuItem[]{menuItem};
    }

    private MenuItem[] createLidarSettingsMenuItems(LidarEntity lidarEntity) {
        MenuItem menuItem = new MenuItem("Lidar Settings");
        menuItem.setOnAction(event -> LidarSettingsWidgetPane.show(lidarEntity));
        return new MenuItem[]{menuItem};
    }

    private MenuItem[] createRpuasEntityMenuItems(RpuasEntity rpuasEntity) {
        MenuItem menuItem = new MenuItem("Show Status");
        menuItem.setOnAction(actionEvent -> RpuasStatusWidgetPane.show(rpuasEntity));
        return new MenuItem[]{menuItem};
    }

    private Menu createCopyLocationMenu(Position position) {
        Menu menu = new Menu("Copy Location");
        ImageView copyImage = new ImageView(ImageLoader.getFxImage("/images/copy_small.png"));
        menu.setGraphic(copyImage);

        final String positionString = String.format("%3.6f\u00b0, %3.6f\u00b0, %,dm",
                position.getLatitude().degrees, position.getLongitude().degrees,
                (int) Math.round(position.getElevation()));
        Label positionLabel = new Label(positionString);

        CustomMenuItem customMenuItem = new CustomMenuItem(positionLabel);
        customMenuItem.setOnAction(event -> copyToClipboard(positionString));
        menu.getItems().add(customMenuItem);

        Tooltip tooltip = new Tooltip("Copy position");
        Tooltip.install(customMenuItem.getContent(), tooltip);

        final String mgrsString = MGRSCoord.fromLatLon(position.getLatitude(), position.getLongitude()).toString();
        Label mgrsLabel = new Label(mgrsString);

        customMenuItem = new CustomMenuItem(mgrsLabel);
        customMenuItem.setOnAction(event -> copyToClipboard(mgrsString));
        menu.getItems().add(customMenuItem);

        tooltip = new Tooltip("Copy position");
        Tooltip.install(customMenuItem.getContent(), tooltip);

        return menu;
    }

    private void copyToClipboard(String value) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(value), null);
    }
}
