package gov.mil.otc._3dvis.ui.projects.nbcrv;

import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.project.nbcrv.SidecarEntity;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class SidecarStatusWidgetPane implements IWidgetPane {

    public static void show(IEntity entity) {
        SidecarStatusWidgetPane nbcrvStatusWidgetPane = sidecarStatusWidgetPaneMap.get(entity.getEntityId());
        if (nbcrvStatusWidgetPane == null) {
            nbcrvStatusWidgetPane = create(entity);
            if (nbcrvStatusWidgetPane == null) {
                return;
            }
            sidecarStatusWidgetPaneMap.put(entity.getEntityId(), nbcrvStatusWidgetPane);
            WidgetPaneContainer.addWidgetPane(nbcrvStatusWidgetPane);
        }
        WidgetPaneContainer.showWidgetPane(nbcrvStatusWidgetPane);
    }

    private static final Map<EntityId, SidecarStatusWidgetPane> sidecarStatusWidgetPaneMap = new ConcurrentHashMap<>();

    private final VBox mainVBox = new VBox();
    private ListView<String> deviceListView = new ListView<>();
    private final TextField operationalStatusTextField = new TextField();
    private final SidecarEntity entity;
    private final SidecarDevicePane sidecarDevicePane;
    private final Timer updateTimer = new Timer("SidecarStatusWidgetPane:updateTimer");

    private static SidecarStatusWidgetPane create(IEntity entity) {
        if (entity instanceof SidecarEntity) {
            return new SidecarStatusWidgetPane((SidecarEntity) entity);
        }
        return null;
    }

    public SidecarStatusWidgetPane(SidecarEntity entity) {
        this.entity = entity;
        sidecarDevicePane = new SidecarDevicePane(entity);
        mainVBox.getChildren().add(sidecarDevicePane.getPane());
    }

    @Override
    public String getName() {
        return entity.getName();
    }

    @Override
    public Pane getPane() {
        return mainVBox;
    }

    @Override
    public void dispose() {
        sidecarDevicePane.dispose();
        sidecarStatusWidgetPaneMap.remove(entity.getEntityId());
        updateTimer.cancel();
    }
}
