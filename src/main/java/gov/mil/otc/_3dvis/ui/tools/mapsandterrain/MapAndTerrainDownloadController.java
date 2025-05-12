package gov.mil.otc._3dvis.ui.tools.mapsandterrain;

import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.retrieve.BulkRetrievable;
import gov.nasa.worldwind.util.Level;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MapAndTerrainDownloadController {

    private static MapAndTerrainDownloadController mapAndTerrainDownloadController = null;
    private final Stage stage = new Stage();
    private final ListView<Layer> layerListView = new ListView<>();

    public static synchronized void show() {
        if (mapAndTerrainDownloadController == null) {
            mapAndTerrainDownloadController = new MapAndTerrainDownloadController();
        }
//        mapAndTerrainDownloadController.doShow();
    }

    private MapAndTerrainDownloadController() {
        MenuItem newMenuItem = new MenuItem("New");
        MenuItem openMenuItem = new MenuItem("Open");
        MenuItem exitMenuItem = new MenuItem("Exit");
        Menu menu = new Menu("File");
        menu.getItems().addAll(newMenuItem, openMenuItem, exitMenuItem);
        MenuBar menuBar = new MenuBar(menu);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);
        borderPane.setCenter(layerListView);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle("Map And Terrain Download Utility");
        stage.initOwner(MainApplication.getInstance().getStage());

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("mapandterraindownloadcontroller");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());

        test();
    }

    private void test() {
        for (Layer layer : WWController.getWorldWindowPanel().getModel().getLayers()) {
            if (layer instanceof BulkRetrievable && layer instanceof TiledImageLayer) {
                layerListView.getItems().add(layer);
                System.out.println(layer.getName());
                for (Level level : ((TiledImageLayer) layer).getLevels().getLevels()) {
                    System.out.println(level.getLevelNumber() + ", " + level.getTexelSize() * 1000000);
                }
            }
        }
    }
}
