package gov.mil.otc._3dvis.ui.widgets.entity.entitytype;

import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EntityTypePicker extends TransparentWindow {

    public static EntityType show(Stage parentStage) {
        return show(parentStage, null);
    }

    public static EntityType show(Stage parentStage, EntityType initialValue) {
        EntityTypePicker entityPicker = new EntityTypePicker(parentStage, initialValue);
        parentStage.setOpacity(.75);
        entityPicker.createAndShow(true);
        parentStage.setOpacity(1.0);
        return entityPicker.selectedEntityType;
    }

    private final TreeView<EntityTypeItem> treeView = new TreeView<>();
    private final BorderPane loadingPane = new BorderPane(new Label("loading..."));
    private final Button okButton = new Button("OK");
    private final EntityType initialValue;
    private TreeItem<EntityTypeItem> entityTypeItemTreeItem = new TreeItem<>();
    private EntityType selectedEntityType = null;
    private boolean isClosed = false;
    private double offsetX = 0;
    private double offsetY = 0;

    private EntityTypePicker(Stage parentStage, EntityType initialValue) {
        super(parentStage);

        this.initialValue = initialValue;
    }

    @Override
    protected Pane createContentPane() {
        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onSelectionChange());

        okButton.setOnAction(event -> onOkAction());
        okButton.setDisable(true);
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> onCancelAction());

        HBox closeButtonsHBox = new HBox(10);
        closeButtonsHBox.getChildren().addAll(okButton, cancelButton);

        VBox mainVBox = new VBox(10);
        mainVBox.setPadding(new Insets(10, 10, 10, 10));
        mainVBox.setFillWidth(true);
        mainVBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THIN)));
        mainVBox.setStyle("-fx-background-color: rgba(43, 43, 43, 0.90);");
        mainVBox.setMinWidth(500);

        mainVBox.getChildren().addAll(
                createTitleLabel("Entity Type Picker"),
                new Separator(),
                new StackPane(treeView, loadingPane),
                new Separator(),
                closeButtonsHBox);

        mainVBox.setOnMousePressed(event -> {
            offsetX = getStage().getX() - event.getScreenX();
            offsetY = getStage().getY() - event.getScreenY();
        });

        mainVBox.setOnMouseDragged(event -> {
            getStage().setX(event.getScreenX() + offsetX);
            getStage().setY(event.getScreenY() + offsetY);
        });

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        double x = getParentStage().getX() + getParentStage().getWidth() / 2 - 125;
        double y = getParentStage().getY() + getParentStage().getHeight() / 2 - 70;
        getStage().setX(x);
        getStage().setY(y);

        new Thread(() -> {
            EntityTypeTreeLoader.waitForComplete();
            Platform.runLater(() -> {
                if (!isClosed) {
                    entityTypeItemTreeItem = EntityTypeTreeLoader.getTreeItem();
                    treeView.setRoot(entityTypeItemTreeItem);
                    treeView.setShowRoot(false);
                    treeView.refresh();
                    loadingPane.setVisible(false);
                    if (initialValue != null) {
                        setSelectedEntityType(initialValue);
                    }
                }
            });
        }, "Munitions Tree Loading Thread").start();

        return true;
    }

    private void setSelectedEntityType(EntityType entityType) {
        TreeItem<EntityTypeItem> item = getTreeViewItem(entityTypeItemTreeItem, entityType);
        treeView.getSelectionModel().select(item);
    }

    private static TreeItem<EntityTypeItem> getTreeViewItem(TreeItem<EntityTypeItem> parent, EntityType entityType) {
        if (parent == null) {
            return null;
        }

        if (parent.getValue() != null && entityType.equals(parent.getValue().getEntityType())) {
            return parent;
        }

        for (TreeItem<EntityTypeItem> child : parent.getChildren()) {
            TreeItem<EntityTypeItem> item = getTreeViewItem(child, entityType);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /*
    todo add search capability
    private static TreeItem<EntityTypeItem> getTreeViewItem(TreeItem<EntityTypeItem> item, String value) {
        if (item != null && item.getValue().toString().cont.equals(value)) {
            return item;
        }

        for (TreeItem<EntityTypeItem> child : item.getChildren()) {
            TreeItem<String> s = getTreeViewItem(child, value);
            if (s != null) {
                return s;
            }

        }
        return null;
    }
     */

    private void onSelectionChange() {
        okButton.setDisable(treeView.getSelectionModel().getSelectedItem() == null ||
                treeView.getSelectionModel().getSelectedItem().getValue().getEntityType() == null);
    }

    private void onOkAction() {
        if (treeView.getSelectionModel().getSelectedItem() != null) {
            selectedEntityType = treeView.getSelectionModel().getSelectedItem().getValue().getEntityType();
        }
        close();
        isClosed = true;
    }

    private void onCancelAction() {
        selectedEntityType = null;
        close();
        isClosed = true;
    }
}
