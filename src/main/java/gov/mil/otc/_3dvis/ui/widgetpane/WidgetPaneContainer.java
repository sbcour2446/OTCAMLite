package gov.mil.otc._3dvis.ui.widgetpane;

import gov.mil.otc._3dvis.layer.Constant;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WidgetPaneContainer {

    public static Pane getPane() {
        return SINGLETON.containerPane;
    }

    public static void addWidgetPane(IWidgetPane widgetPane) {
        SINGLETON.doAddWidgetPane(widgetPane);
    }

    public static void showWidgetPane(IWidgetPane widgetPane) {
        SINGLETON.doShowWidgetPane(widgetPane);
    }

    public static void closeWidgetPane(IWidgetPane widgetPane) {
        SINGLETON.onClosePane(widgetPane);
    }

    public static void showWidgetRightPane(IWidgetPane widgetPane) {
        SINGLETON.doShowWidgetRightPane(widgetPane);
    }

    public static void hideWidgetRightPane(IWidgetPane widgetPane) {
        SINGLETON.doHideWidgetRightPane(widgetPane);
    }

    private static final WidgetPaneContainer SINGLETON = new WidgetPaneContainer();
    private final BorderPane containerPane = new BorderPane();
    private final Map<IWidgetPane, Tab> tabMap = new ConcurrentHashMap<>();
    private TabPane tabPane = null;

    private WidgetPaneContainer() {
        Pane pane = new Pane();
        pane.setMinHeight(Constant.TIMELINE_TOP);
        pane.setPickOnBounds(false);

        containerPane.setBottom(pane);
        containerPane.setPickOnBounds(false);
    }

    private void doAddWidgetPane(final IWidgetPane widgetPane) {
        if (tabPane == null) {
            createTabPane();
        }
        Tab tab = new Tab(widgetPane.getName(), widgetPane.getPane());
        tab.setOnCloseRequest(event -> onClosePane(widgetPane));
        tabMap.put(widgetPane, tab);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    private void doShowWidgetPane(IWidgetPane widgetPane) {
        Tab tab = tabMap.get(widgetPane);
        if (tab != null) {
            tabPane.getSelectionModel().select(tab);
        }
    }

    private void doShowWidgetRightPane(IWidgetPane widgetPane) {
        containerPane.setRight(widgetPane.getPane());
    }

    private void doHideWidgetRightPane(IWidgetPane widgetPane) {
        widgetPane.dispose();
        containerPane.setRight(null);
    }

    private void onClosePane(IWidgetPane widgetPane) {
        widgetPane.dispose();
        tabMap.remove(widgetPane);
        if (tabMap.isEmpty()) {
            containerPane.setLeft(null);
            tabPane = null;
        }
    }

    private void createTabPane() {
        tabPane = new TabPane();
        tabPane.getStyleClass().add("root");
        tabPane.getStyleClass().add("widget-pane");
        containerPane.setLeft(tabPane);
    }
}
