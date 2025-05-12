package gov.mil.otc._3dvis.ui.data.dataimport.datafile;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DatabaseLogger;
import gov.mil.otc._3dvis.datamodel.TimedFile;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.tir.TirReader;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class TirImportController extends TransparentWindow {

    private static final int FIELD_MIN_WIDTH = 500;

    private final CheckBox assignToEntityCheckBox = new CheckBox("Assign to entity");
    private final Hyperlink selectEntityHyperlink = new Hyperlink("select entity");
    private final ListView<FileWrapper> fileListView = new ListView<>();
    private final Button importDataButton = new Button("Import");
    private IEntity assignedEntity = null;

    public static synchronized void show() {
        new TirImportController().createAndShow();
    }

    @Override
    protected Pane createContentPane() {
        assignToEntityCheckBox.setOnAction(event -> {
            if (assignToEntityCheckBox.isSelected()) {
                assignedEntity = null;
                selectEntityHyperlink.setVisible(true);
            } else {
                selectEntityHyperlink.setVisible(false);
            }
        });

        selectEntityHyperlink.setVisible(false);
        selectEntityHyperlink.setOnAction(event -> {
            assignedEntity = EntityPicker.show(getStage());
        });

        HBox entityHBox = new HBox(UiConstants.SPACING, assignToEntityCheckBox, selectEntityHyperlink);

        Button selectFileButton = new Button("Select Files");
        selectFileButton.setOnAction(event -> onSelectFileAction());

        fileListView.setCellFactory(fileViewListView -> new FileCell());

        importDataButton.setOnAction(event -> onImportAction());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, importDataButton, cancelButton);
        buttonsHBox.setAlignment(Pos.BASELINE_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Import TIRs"),
                new Separator(),
                entityHBox,
                new HBox(selectFileButton),
                fileListView,
                new Separator(),
                buttonsHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setMinWidth(FIELD_MIN_WIDTH);

        return mainVBox;
    }

    private void onSelectFileAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to import.");
        fileChooser.setInitialDirectory(SettingsManager.getPreferences().getLastDirectory("csvimport"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(getStage());
        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            String directory = selectedFiles.get(0).getParent();
            SettingsManager.getPreferences().setLastDirectory("csvimport", directory);
            loadFiles(selectedFiles);
            importDataButton.setDisable(false);
        }
    }

    private void loadFiles(List<File> files) {
        for (File file : files) {
            fileListView.getItems().add(new FileWrapper(file));
        }
    }

    private void onImportAction() {
        ProgressDialog progressDialog = new ProgressDialog(getStage());
        progressDialog.setCancelListener(progressDialog::close);
        progressDialog.addStatus("importing");
        progressDialog.createAndShow();

        importDataButton.setDisable(true);

        for (FileWrapper fileWrapper : fileListView.getItems()) {
            if (fileWrapper.isImported) {
                continue;
            }
            File file = fileWrapper.file;
            TimedFile timedFile = TirReader.processFile(file);
            if (assignedEntity == null) {
                DatabaseLogger.addTimedFile(timedFile, EntityId.ENTITY_ID_UNKNOWN);
            } else {
                DatabaseLogger.addTimedFile(timedFile, assignedEntity.getEntityId());
            }
        }

        fileListView.getItems().clear();
        progressDialog.setComplete(true);
    }

    private static class FileWrapper {
        private final File file;
        private final boolean isImported;

        private FileWrapper(File file) {
            this.file = file;
            boolean isImported = false;
            for (TimedFile timedFile : DataManager.getTimedFileList()) {
                if (timedFile.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                    isImported = true;
                    break;
                }
            }
            this.isImported = isImported;
        }

        public String toString() {
            if (isImported) {
                return "***" + file.getAbsolutePath() + "(already imported)";
            } else {
                return file.getAbsolutePath();
            }
        }
    }

    private static class FileCell extends ListCell<FileWrapper> {

        @Override
        public void updateItem(FileWrapper fileWrapper, boolean empty) {
            super.updateItem(fileWrapper, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (fileWrapper != null) {
                setText(fileWrapper.toString());
                if (fileWrapper.isImported) {
                    setStyle("-fx-text-fill: grey;");
                }
            } else {
                setText("null");
                setGraphic(null);
            }
        }
    }
}
