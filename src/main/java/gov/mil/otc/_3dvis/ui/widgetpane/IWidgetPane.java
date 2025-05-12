package gov.mil.otc._3dvis.ui.widgetpane;

import javafx.scene.layout.Pane;

public interface IWidgetPane {

    String getName();
    Pane getPane();
    void dispose();
}
