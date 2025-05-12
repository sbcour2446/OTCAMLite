package gov.mil.otc._3dvis.ui.main.menu;

import gov.mil.otc._3dvis.Main;
import gov.mil.otc._3dvis.layer.timeline.TimelineLayer;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.application.settings.IconDisplayController;
import gov.mil.otc._3dvis.ui.application.settings.MapsAndTerrainController;
import gov.mil.otc._3dvis.ui.application.settings.UnitPreferenceController;
import gov.mil.otc._3dvis.ui.application.tena.TenaConnectionController;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.TimeZonePicker;
import gov.mil.otc._3dvis.utility.KeepAlive;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.TimeZone;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationMenu extends Menu {

    public ApplicationMenu() {
        setText("Application");
        create();
    }

    private void create() {
        getItems().add(createSettingsMenu());
        getItems().add(createLogLevelMenu());

        if (!Main.isLiteMode()) {
            getItems().add(createTenaMenuItem());
        }

        getItems().add(createKeepAliveMenuItem());

        MenuItem closeMenuItem = new MenuItem("Close");
        closeMenuItem.setOnAction(event -> onCloseAction());
        getItems().add(closeMenuItem);
    }

    private Menu createSettingsMenu() {
        MenuItem mapsAndTerrainMenuItem = new MenuItem("Maps And Terrain");
        mapsAndTerrainMenuItem.setOnAction(event -> MapsAndTerrainController.show());

        MenuItem iconDisplayMenuItem = new MenuItem("Icon Display");
        iconDisplayMenuItem.setOnAction(event -> IconDisplayController.show());

        CheckMenuItem showLocalTimeCheckMenuItem = new CheckMenuItem("Show Local Time");
        showLocalTimeCheckMenuItem.setOnAction(this::selectTimeZone);

        MenuItem unitPreferencesMenuItem = new MenuItem("Unit Preferences");
        unitPreferencesMenuItem.setOnAction(event -> UnitPreferenceController.show());

        Menu menu = new Menu("Settings");
        menu.getItems().add(mapsAndTerrainMenuItem);
        menu.getItems().add(iconDisplayMenuItem);
        menu.getItems().add(showLocalTimeCheckMenuItem);
        menu.getItems().add(unitPreferencesMenuItem);

        return menu;
    }

    private Menu createLogLevelMenu() {
        Menu menu = new Menu("Log Level");
        ToggleGroup toggleGroup = new ToggleGroup();
        RadioMenuItem radioMenuItem;

        radioMenuItem = new RadioMenuItem(Level.OFF.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.OFF));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel() == Level.OFF) {
            radioMenuItem.setSelected(true);
        }

        radioMenuItem = new RadioMenuItem(Level.SEVERE.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.SEVERE));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel().equals(Level.SEVERE)) {
            radioMenuItem.setSelected(true);
        }

        radioMenuItem = new RadioMenuItem(Level.WARNING.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.WARNING));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel().equals(Level.WARNING)) {
            radioMenuItem.setSelected(true);
        }

        radioMenuItem = new RadioMenuItem(Level.INFO.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.INFO));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel().equals(Level.INFO)) {
            radioMenuItem.setSelected(true);
        }

        radioMenuItem = new RadioMenuItem(Level.CONFIG.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.CONFIG));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel().equals(Level.CONFIG)) {
            radioMenuItem.setSelected(true);
        }

        radioMenuItem = new RadioMenuItem(Level.FINE.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.FINE));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel().equals(Level.FINE)) {
            radioMenuItem.setSelected(true);
        }

        radioMenuItem = new RadioMenuItem(Level.FINER.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.FINER));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel().equals(Level.FINER)) {
            radioMenuItem.setSelected(true);
        }

        radioMenuItem = new RadioMenuItem(Level.FINEST.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.FINEST));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel().equals(Level.FINEST)) {
            radioMenuItem.setSelected(true);
        }

        radioMenuItem = new RadioMenuItem(Level.ALL.getName());
        radioMenuItem.setOnAction(event -> setLogLevel(Level.ALL));
        toggleGroup.getToggles().add(radioMenuItem);
        menu.getItems().add(radioMenuItem);
        if (Logger.getGlobal().getLevel().equals(Level.ALL)) {
            radioMenuItem.setSelected(true);
        }

        return menu;
    }

    private void setLogLevel(Level level) {
        Logger.getGlobal().setLevel(level);
        for (Handler handler : Logger.getGlobal().getHandlers()) {
            handler.setLevel(level);
        }
        SettingsManager.getSettings().setLogLevel(level);
    }

    private MenuItem createTenaMenuItem() {
        MenuItem menuItem = new MenuItem("TENA Connection");
        menuItem.setOnAction(event -> TenaConnectionController.show());
        return menuItem;
    }

    private MenuItem createKeepAliveMenuItem() {
        CheckMenuItem checkMenuItem = new CheckMenuItem("Keep Alive");
        checkMenuItem.setSelected(SettingsManager.getPreferences().getKeepAlive());
        checkMenuItem.setOnAction(event -> {
            if (checkMenuItem.isSelected()) {
                KeepAlive.start();
            } else {
                KeepAlive.shutdown();
            }
            SettingsManager.getPreferences().setKeepAlive(checkMenuItem.isSelected());
        });

        return checkMenuItem;
    }

    private void selectTimeZone(ActionEvent e) {
        if (((CheckMenuItem) e.getSource()).isSelected()) {
            TimeZone timeZone = TimeZonePicker.show(MainApplication.getInstance().getStage());
            if (timeZone != null) {
                TimelineLayer.setLocalTimeZone(timeZone);
                TimelineLayer.setShowLocalTimeZone(true);
            } else {
                ((CheckMenuItem) e.getSource()).setSelected(false);
            }
        } else {
            TimelineLayer.setShowLocalTimeZone(false);
        }
    }

    private void onCloseAction() {
        Window window = MainApplication.getInstance().getScene().getWindow();
        window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
    }
}
