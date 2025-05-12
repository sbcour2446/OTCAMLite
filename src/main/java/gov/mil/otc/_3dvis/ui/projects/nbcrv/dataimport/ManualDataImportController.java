package gov.mil.otc._3dvis.ui.projects.nbcrv.dataimport;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.project.nbcrv.NbcrvEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.tir.TirReader;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class ManualDataImportController extends TransparentWindow {

    private final NbcrvEntity nbcrvEntity;
    private final ComboBox<Mission> missionComboBox = new ComboBox<>();
    private final ListView<File> fileListView = new ListView<>();
    private final Button importButton = new Button("Import");

    public static synchronized void show() {
        NbcrvEntity nbcrvEntity = (NbcrvEntity) EntityPicker.show(MainApplication.getInstance().getStage(), List.of(NbcrvEntity.class));
        if (nbcrvEntity == null) {
            return;
        }
        new ManualDataImportController(nbcrvEntity).createAndShow();
    }

    private ManualDataImportController(NbcrvEntity nbcrvEntity) {
        this.nbcrvEntity = nbcrvEntity;
    }

    @Override
    protected Pane createContentPane() {
        missionComboBox.getItems().addAll(DataManager.getMissions());
        missionComboBox.getSelectionModel().selectFirst();

        Button selectButton = new Button("Select Files");
        selectButton.setOnAction(event -> selectFiles());

        fileListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                Platform.runLater(() -> fileListView.getSelectionModel().select(-1)));

        importButton.setDisable(true);
        importButton.setOnAction(event -> importFiles());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, importButton, closeButton);
        buttonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        AnchorPane buttonAnchorPane = new AnchorPane(importButton, closeButton);
        AnchorPane.setLeftAnchor(importButton, 0.0);
        AnchorPane.setRightAnchor(closeButton, 0.0);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Import Manual Data - " + nbcrvEntity.getName()),
                new Separator(),
                selectButton,
                fileListView,
                new Separator(),
                buttonAnchorPane);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setFillWidth(true);
        mainVBox.setPrefWidth(800);

        return mainVBox;
    }

    private void selectFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select data files to import.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("import.manualData"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.csv", "*.pdf"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Comma-Separated Values (CSV)", "*.csv"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("TIR", "*.pdf"));
        List<File> files = fileChooser.showOpenMultipleDialog(getStage());
        if (files != null && !files.isEmpty()) {
            fileListView.getItems().clear();
            SettingsManager.getPreferences().setLastDirectory("import.manualData", files.get(0).getParent());

            for (File file : files) {
                fileListView.getItems().add(file);
            }
        }
        ListProperty<File> listProperty = new SimpleListProperty<>();
        listProperty.bind(fileListView.itemsProperty());
        importButton.disableProperty().bind(listProperty.emptyProperty());
    }

    private void importFiles() {
        for (File file : fileListView.getItems()) {
            TimedFile.FileType fileType = TimedFile.FileType.UNKNOWN;
            long timestamp = 0;
            String fileGroup = "";
            if (file.getName().toLowerCase().endsWith(".pdf")) {
                fileType = TimedFile.FileType.PDF;
                timestamp = TirReader.getTime(file);
                fileGroup = "TIR";
            } else if (file.getName().toLowerCase().endsWith(".csv")) {
                fileType = TimedFile.FileType.CSV;
                timestamp = missionComboBox.getSelectionModel().getSelectedItem().getTimestamp();
                fileGroup = "Manual Data";
            }
            TimedFile timedFile = new TimedFile(timestamp, file, fileType, fileGroup);
            nbcrvEntity.addTimedFile(timedFile);
            DatabaseLogger.addTimedFile(timedFile, nbcrvEntity.getEntityId());
        }
        fileListView.getItems().clear();
    }
}
