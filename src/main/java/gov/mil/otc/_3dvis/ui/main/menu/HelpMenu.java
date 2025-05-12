package gov.mil.otc._3dvis.ui.main.menu;

import gov.mil.otc._3dvis.ui.main.AboutController;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class HelpMenu extends Menu {

    public HelpMenu() {
        setText("Help");
        create();
    }

    private void create() {
        MenuItem aboutMenuItem = new MenuItem("About");
        aboutMenuItem.setOnAction(event -> AboutController.show());

        getItems().add(aboutMenuItem);
    }
}
