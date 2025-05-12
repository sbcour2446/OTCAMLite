package gov.mil.otc._3dvis.ui.data.kml;

import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.overlay.OverlayManager;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.StageSizer;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class OverlayManagerController {

    private static final OverlayManagerController SINGLETON = new OverlayManagerController();
    private final Stage stage = new Stage();
    private final TableView<OverlayFileView> overlayFileViewTableView = new TableView<>();

    public static synchronized void show() {
        SINGLETON.doShow();
    }

    private OverlayManagerController() {
        initializeTable();

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> onAdd());
        Button removeButton = new Button("Remove");
        removeButton.setOnAction(event -> onRemove());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, addButton, removeButton);
        buttonsHBox.setPadding(new Insets(0, UiConstants.SPACING, UiConstants.SPACING, UiConstants.SPACING));

        BorderPane mainBorderPane = new BorderPane(overlayFileViewTableView, buttonsHBox, null, null, null);
        mainBorderPane.setPadding(new Insets(UiConstants.SPACING));
        mainBorderPane.setPrefWidth(800);

        stage.getIcons().add(ImageLoader.getLogo());
        stage.setResizable(true);
        stage.setTitle("Overlay Manager");
        stage.initOwner(MainApplication.getInstance().getStage());
        stage.initStyle(StageStyle.UTILITY);
        Scene scene = new Scene(mainBorderPane);
        stage.setScene(scene);
        ThemeHelper.applyTheme(scene);
        StageSizer stageSizer = new StageSizer("Overlay Manager");
        stageSizer.setStage(stage, MainApplication.getInstance().getStage());
    }

    private void doShow() {
        stage.show();
    }

    private void initializeTable() {
        TableColumn<OverlayFileView, String> fileNameTableColumn = new TableColumn<>("File Name");
        fileNameTableColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));

        fileNameTableColumn.prefWidthProperty().bind(overlayFileViewTableView.widthProperty().multiply(25 / 37.0));

        overlayFileViewTableView.getColumns().add(fileNameTableColumn);

        for (File overlay : SettingsManager.getPreferences().getOverlayList()) {
            overlayFileViewTableView.getItems().add(new OverlayFileView(overlay));
        }
    }

    private void onAdd() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select overlay to import.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("overlaymanager"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Supported Types", "*.kml", "*.kmz", "*.ovl"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Keyhole Markup Language", "*.kml"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Compressed KML", "*.kmz"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CPOF Overlay XML", "*.ovl"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            SettingsManager.getPreferences().setLastDirectory("overlaymanager", file.getParent());
            OverlayManager.addOverlay(file);
            overlayFileViewTableView.getItems().add(new OverlayFileView(file));
        }
    }

    private void onRemove() {
        for (OverlayFileView overlayFileView : overlayFileViewTableView.getSelectionModel().getSelectedItems()) {
            OverlayManager.removeOverlay(overlayFileView.getFile());
        }
        overlayFileViewTableView.getItems().removeAll(overlayFileViewTableView.getSelectionModel().getSelectedItems());
    }
}
