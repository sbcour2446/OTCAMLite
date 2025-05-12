package gov.mil.otc._3dvis.ui.widgetpane;

import gov.mil.otc._3dvis.entity.base.EntityId;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WidgetPaneTester implements IWidgetPane {

    public static void show(String message) {
        WidgetPaneContainer.addWidgetPane(new WidgetPaneTester(message));
//        WidgetPaneContainer.addWidgetPane(new Pane(new Label(message)));
//        WidgetPaneTester widgetPaneTester = widgetPaneTesterMap.get(entity.getEntityId());
//        if (widgetPaneTester == null) {
//            widgetPaneTester = new WidgetPaneTester(entity);
//            widgetPaneTesterMap.put(entity.getEntityId(), widgetPaneTester);
//            WidgetPaneContainer.addWidgetPane(widgetPaneTester);
//        }
//        widgetPaneTester.requestFocus();
    }

    private static final Map<EntityId, WidgetPaneTester> widgetPaneTesterMap = new ConcurrentHashMap<>();
    //    private final IEntity entity;
    private VBox mainVBox = new VBox();

    private WidgetPaneTester(String message) {
        mainVBox.getChildren().add(new Label(message));
//        this.entity = entity;
//        this.getChildren().add(new Label(entity.getName()));
    }

    @Override
    public String getName() {
        return "WidgetPaneTester";
    }

    @Override
    public Pane getPane() {
        return mainVBox;
    }

    @Override
    public void dispose() {
        // not implemented
    }
}
