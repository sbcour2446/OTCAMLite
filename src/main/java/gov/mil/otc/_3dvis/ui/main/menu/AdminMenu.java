package gov.mil.otc._3dvis.ui.main.menu;

import gov.mil.otc._3dvis.ui.admin.CacheManager;
import gov.mil.otc._3dvis.ui.widgets.AdminLoginWindow;
import gov.nasa.worldwind.WorldWind;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class AdminMenu extends Menu {

    private final MenuItem loginMenuItem = new MenuItem("Login");
    private boolean isAdmin = false;

    public AdminMenu() {
        setText("Admin");
        create();
    }

    private void create() {
        loginMenuItem.setOnAction(event -> {
            if (loginMenuItem.getText().equalsIgnoreCase("login")) {
                isAdmin = AdminLoginWindow.show();
            } else {
                isAdmin = false;
            }
            createMenuItems();
        });
        getItems().add(loginMenuItem);
    }

    private void createMenuItems() {
        getItems().clear();
        getItems().add(loginMenuItem);
        if (isAdmin) {
            loginMenuItem.setText("Logoff");
            getItems().add(createOnlineMenuItem());
            getItems().add(createCacheManagerMenuItem());
        } else {
            loginMenuItem.setText("Login");
        }
    }

    private MenuItem createOnlineMenuItem() {
        CheckMenuItem checkMenuItem = new CheckMenuItem("Online");
        checkMenuItem.setSelected(!WorldWind.isOfflineMode());
        checkMenuItem.setOnAction(event -> {
            WorldWind.setOfflineMode(!checkMenuItem.isSelected());
        });
        return checkMenuItem;
    }

    private MenuItem createCacheManagerMenuItem() {
        CheckMenuItem checkMenuItem = new CheckMenuItem("Cache Manager");
        checkMenuItem.setOnAction(event -> {
           CacheManager.show();
        });
        return checkMenuItem;
    }
}
