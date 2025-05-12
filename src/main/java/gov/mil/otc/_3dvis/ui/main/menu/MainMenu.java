package gov.mil.otc._3dvis.ui.main.menu;

import gov.mil.otc._3dvis.Main;
import javafx.scene.control.MenuBar;

public class MainMenu {

    private static final MainMenu mainMenu = new MainMenu();
    private final MenuBar menuBar = new MenuBar();

    private MainMenu() {
        initialize();
    }

    public static MenuBar getMainMenu() {
        return mainMenu.menuBar;
    }

    private void initialize() {
        menuBar.getMenus().add(new ApplicationMenu());
        menuBar.getMenus().add(new DataMenu());
        menuBar.getMenus().add(new ToolsMenu());
        menuBar.getMenus().add(new DisplayMenu());

        if (!Main.isLiteMode()){
            menuBar.getMenus().add(new AdminMenu());
        }

        menuBar.getMenus().add(new HelpMenu());
    }
}
