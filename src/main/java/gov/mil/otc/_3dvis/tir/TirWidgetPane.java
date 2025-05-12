package gov.mil.otc._3dvis.tir;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgetpane.IWidgetPane;
import gov.mil.otc._3dvis.ui.widgetpane.WidgetPaneContainer;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class TirWidgetPane implements IWidgetPane {

    private static TirWidgetPane instance = null;
    private final VBox mainVBox = new VBox();
    private final ListView<TirFileView> tirListView = new ListView<>();

    private TirWidgetPane() {
        initializeTirListView();
        mainVBox.getChildren().add(tirListView);
    }

    public static void show() {
        if (instance == null) {
            instance = new TirWidgetPane();
            WidgetPaneContainer.addWidgetPane(instance);
        }
    }

    private void initializeTirListView() {
        Label label = new Label("no files loaded");
        label.setPadding(new Insets(UiConstants.SPACING));
        tirListView.setPlaceholder(label);
        tirListView.setCellFactory(timestampFileListView -> new TirFileCell());
        tirListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tirListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            TirViewer.show(newValue.getFile());
        });
        for (TimedFile timedFile : DataManager.getTimedFileList()) {
            tirListView.getItems().add(new TirFileView(timedFile));
        }
        for (TimedFile timedFile : TirManager.getTirList()) {
            tirListView.getItems().add(new TirFileView(timedFile));
        }
    }

    @Override
    public String getName() {
        return "TIR List";
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
