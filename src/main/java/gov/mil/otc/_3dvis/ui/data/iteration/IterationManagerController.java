package gov.mil.otc._3dvis.ui.data.iteration;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.utility.ThemeHelper;
import gov.mil.otc._3dvis.ui.widgets.ProgressDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IterationManagerController extends TransparentWindow {

    private final TableView<IterationView> iterationTableView = new TableView<>();
    private final Button resetButton = new Button("Reset");
    private final Button saveButton = new Button("Save");

    public static synchronized void show() {
        new IterationManagerController().createAndShow();
    }

    private IterationManagerController() {
    }

    @Override
    protected Pane createContentPane() {
        Label titleLabel = new Label("Iteration Manager");
        titleLabel.setFont(Font.font("Consolas", FontWeight.BOLD, 18));
        VBox titleVBox = new VBox(UiConstants.SPACING, titleLabel);
        titleVBox.setAlignment(Pos.CENTER);

        Button addButton = new Button("Add");
        addButton.setOnAction(event -> addIteration());

        Button removeButton = new Button("Remove");
        removeButton.setOnAction(event -> removeIterations());

        Button editButton = new Button("Edit");
        editButton.setOnAction(event -> editRow());

        HBox buttonsHBox = new HBox(UiConstants.SPACING, addButton, removeButton, editButton);
        buttonsHBox.setAlignment(Pos.CENTER_LEFT);
        buttonsHBox.setPadding(new Insets(0, 0, 0, UiConstants.SPACING));

        VBox topVBox = new VBox(UiConstants.SPACING,
                titleVBox,
                new Separator(),
                buttonsHBox);
        topVBox.setPadding(new Insets(0, 0, UiConstants.SPACING, 0));

        initializeTable();
        loadIterationTable();

        resetButton.setOnAction(event -> reset());
        saveButton.setOnAction(event -> save());
        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());

        HBox closeHBox = new HBox(UiConstants.SPACING, resetButton, saveButton, closeButton);
        closeHBox.setPadding(new Insets(UiConstants.SPACING, 0, 0, 0));
        closeHBox.setAlignment(Pos.BASELINE_RIGHT);

        BorderPane borderPane = new BorderPane(iterationTableView, topVBox, null, closeHBox, null);
        borderPane.setPadding(new Insets(UiConstants.SPACING));
        borderPane.setMinWidth(500);

        return borderPane;
    }

    @Override
    protected boolean closeRequested() {
        return !hasChanges() || DialogUtilities.showYesNoDialog("Exit",
                "Do you want to exit without saving?  All changes will be lost.", getStage());
    }

    private void initializeTable() {
        TableColumn<IterationView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameTableColumn.setSortable(false);

        TableColumn<IterationView, String> startTimeTableColumn = new TableColumn<>("Start Time");
        startTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        startTimeTableColumn.setSortable(false);

        TableColumn<IterationView, String> stopTimeTableColumn = new TableColumn<>("Stop Time");
        stopTimeTableColumn.setCellValueFactory(new PropertyValueFactory<>("stopTime"));
        stopTimeTableColumn.setSortable(false);

        TableColumn<IterationView, String> statusTableColumn = new TableColumn<>("Status");
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusTableColumn.setSortable(false);

        iterationTableView.getColumns().add(nameTableColumn);
        iterationTableView.getColumns().add(startTimeTableColumn);
        iterationTableView.getColumns().add(stopTimeTableColumn);
        iterationTableView.getColumns().add(statusTableColumn);

        iterationTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        iterationTableView.setPlaceholder(new Label("no data to display"));

        URL url = ThemeHelper.class.getResource("/css/strikethrough_table.css");
        if (url != null) {
            String css = url.toExternalForm();
            iterationTableView.getStylesheets().add(css);
        }
        iterationTableView.setRowFactory(param -> {
            TableRow<IterationView> row = new TableRow<>() {
                @Override
                public void updateItem(IterationView item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null) {
                        setStyle("");
                        return;
                    }

                    PseudoClass delete = PseudoClass.getPseudoClass("delete-row");
                    pseudoClassStateChanged(delete, item.isRemoved());

                    if (item.isInvalid()) {
                        setStyle("-fx-text-background-color: red;");
                    } else if (item.isAdded()) {
                        setStyle("-fx-text-background-color: green;");
                    } else if (item.isUpdated()) {
                        setStyle("-fx-text-background-color: cyan;");
                    } else {
                        setStyle("");
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    IterationView iterationView = row.getItem();
                    editRow(iterationView);
                }
            });
            return row;
        });
    }

    private void loadIterationTable() {
        iterationTableView.getItems().clear();
        for (Iteration iteration : DataManager.getIterationManager().getIterations()) {
            iterationTableView.getItems().add(new IterationView(iteration));
        }
        validateIterations();
    }

    private void addIteration() {
        Iteration iteration = IterationFormController.show(getStage());
        if (iteration != null) {
            addIteration(iteration);
            validateIterations();
            iterationTableView.refresh();
        }
    }

    public void addIteration(Iteration iteration) {
        for (int i = 0; i < iterationTableView.getItems().size(); i++) {
            if (iterationTableView.getItems().get(i).getIteration().getStartTime() > iteration.getStartTime()) {
                iterationTableView.getItems().add(i, new IterationView(iteration, true));
                return;
            }
        }
        iterationTableView.getItems().add(new IterationView(iteration, true));
    }

    private void editRow() {
        if (iterationTableView.getSelectionModel().getSelectedItems().isEmpty()) {
            DialogUtilities.showWarningDialog("Edit Iteration",
                    "No iteration selected.  Please select a single iteration to edit.",
                    true, getStage());
            return;
        }

        if (iterationTableView.getSelectionModel().getSelectedItems().size() > 1) {
            DialogUtilities.showWarningDialog("Edit Iteration",
                    "Multiple iterations are selected.  Please select a single iteration to edit.",
                    true, getStage());
            return;
        }

        editRow(iterationTableView.getSelectionModel().getSelectedItem());
    }

    private void editRow(IterationView iterationView) {
        Iteration iteration = IterationFormController.show(iterationView.getIteration(), getStage());
        if (iteration != null) {
            iterationView.update(iteration);
            validateIterations();
            iterationTableView.refresh();
        }
    }

    private void removeIterations() {
        if (iterationTableView.getSelectionModel().getSelectedItems().isEmpty()) {
            return;
        }

        List<IterationView> removeList = new ArrayList<>();

        for (IterationView iterationView : iterationTableView.getSelectionModel().getSelectedItems()) {
            if (iterationView.isAdded()) {
                removeList.add(iterationView);
            } else {
                iterationView.remove();
            }
        }

        iterationTableView.getItems().removeAll(removeList);
        validateIterations();
        iterationTableView.refresh();
    }

    private void validateIterations() {
        sortIterations();

        for (int i = 0; i < iterationTableView.getItems().size(); i++) {
            iterationTableView.getItems().get(i).setInvalid(false);
        }

        for (int i = 0; i < iterationTableView.getItems().size() - 1; i++) {
            IterationView iterationView1 = iterationTableView.getItems().get(i);
            if (iterationView1.isRemoved()) {
                continue;
            }
            for (int j = i + 1; j < iterationTableView.getItems().size(); j++) {
                IterationView iterationView2 = iterationTableView.getItems().get(j);
                if (iterationView2.isRemoved()) {
                    continue;
                }
                if (iterationView1.getIteration().overlap(
                        iterationView2.getIteration())) {
                    iterationView1.setInvalid(true);
                    iterationView2.setInvalid(true);
                }
            }
        }

        boolean hasChanges = hasChanges();
        resetButton.setDisable(!hasChanges);
        saveButton.setDisable(!hasChanges);
    }

    private boolean hasChanges() {
        boolean hasChanges = false;
        for (int i = 0; i < iterationTableView.getItems().size(); i++) {
            if (iterationTableView.getItems().get(i).isAdded() ||
                    iterationTableView.getItems().get(i).isUpdated() ||
                    iterationTableView.getItems().get(i).isRemoved()) {
                hasChanges = true;
                break;
            }
        }
        return hasChanges;
    }

    private void sortIterations() {
        ObservableList<IterationView> observableList = FXCollections.observableArrayList();
        for (IterationView iterationView : iterationTableView.getItems()) {
            int i = 0;
            for (; i < observableList.size(); i++) {
                if (observableList.get(i).getIteration().getStartTime() > iterationView.getIteration().getStartTime()) {
                    break;
                }
            }
            observableList.add(i, iterationView);
        }
        iterationTableView.setItems(observableList);
    }

    private void reset() {
        if (!DialogUtilities.showYesNoDialog("Reset Modifications",
                "All changes will be lost, do you wish to continue?", getStage())) {
            return;
        }

        List<IterationView> removeList = new ArrayList<>();
        for (IterationView iterationView : iterationTableView.getItems()) {
            if (iterationView.isAdded()) {
                removeList.add(iterationView);
            } else {
                iterationView.reset();
            }
        }

        iterationTableView.getItems().removeAll(removeList);
        validateIterations();
        iterationTableView.refresh();
    }

    private void save() {
        for (IterationView iterationView : iterationTableView.getItems()) {
            if (iterationView.isInvalid()) {
                DialogUtilities.showErrorDialog(DialogUtilities.INVALID_ENTRY,
                        "Some entries are invalid.", getStage());
                return;
            }
        }

        final ProgressDialog progressDialog = new ProgressDialog(getStage(), null);
        progressDialog.addStatus("saving iteration data...");
        progressDialog.createAndShow();

        new Thread(() -> {
            for (IterationView iterationView : iterationTableView.getItems()) {
                if (iterationView.isAdded()) {
                    DataManager.getIterationManager().addIteration(iterationView.getIteration());
                } else if (iterationView.isUpdated()) {
                    DataManager.getIterationManager().updateIteration(iterationView.getOriginalIteration(),
                            iterationView.getIteration());
                }
            }
            Platform.runLater(() -> {
                progressDialog.close();
                loadIterationTable();
            });
        }, "Save Iteration Thread").start();
    }
}
