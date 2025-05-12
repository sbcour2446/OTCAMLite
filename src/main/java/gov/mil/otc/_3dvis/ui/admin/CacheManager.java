package gov.mil.otc._3dvis.ui.admin;

import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class CacheManager implements IWidgetPane {

    private static CacheManager instance = null;

    public static void show() {
        if (instance == null) {
            instance = new CacheManager();
            WidgetPaneContainer.addWidgetPane(instance);
        }
    }

    private final VBox mainVBox = new VBox();

    @Override
    public String getName() {
        return "Cache Manager";
    }

    @Override
    public Pane getPane() {
        return mainVBox;
    }

    @Override
    public void dispose() {
        instance = null;
    }
}
