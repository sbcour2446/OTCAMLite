package gov.mil.otc._3dvis.ui.application.settings;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.utility.StageUtility;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.WorldWind;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class MapsAndTerrainController {

    private static MapsAndTerrainController mapsAndTerrainController = null;
    private final Stage stage = new Stage();
    private final ListView<File> listView = new ListView<>();

    public static synchronized void show() {
        if (mapsAndTerrainController == null) {
            mapsAndTerrainController = new MapsAndTerrainController();
        }
        mapsAndTerrainController.loadDatastore();
        mapsAndTerrainController.stage.show();
    }

    private MapsAndTerrainController() {
        stage.getIcons().add(ImageLoader.getLogo());
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        stage.setResizable(true);
        stage.setTitle("Maps and Terrain Settings");

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> onAddAction());
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(event -> onRemoveAction());

        VBox vBox = new VBox(10,
                new TitledPane("Datastore", listView),
                new HBox(10, addButton, removeButton));
        vBox.setPadding(new Insets(10, 10, 10, 10));

        Scene scene = new Scene(vBox);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageUtility.centerStage(stage, MainApplication.getInstance().getStage());
    }

    private void loadDatastore() {
        listView.getItems().clear();
        File writeLocation = WorldWind.getDataFileStore().getWriteLocation();
        for (File file : WorldWind.getDataFileStore().getLocations()) {
            if (!file.equals(writeLocation)) {
                listView.getItems().add(file);
            }
        }
    }

    private void onAddAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Add Datastore");
        File file = directoryChooser.showDialog(stage);
        if (file != null) {
            WorldWind.getDataFileStore().addLocation(file.getAbsolutePath(), false);
            SettingsManager.getSettings().addDatastore(file);
        }
        loadDatastore();
    }

    private void onRemoveAction() {
        File file = listView.getSelectionModel().getSelectedItem();
        if (file != null) {
            WorldWind.getDataFileStore().removeLocation(file.toString());
            SettingsManager.getSettings().removeDataStore(file);
            loadDatastore();
        }
    }
}
