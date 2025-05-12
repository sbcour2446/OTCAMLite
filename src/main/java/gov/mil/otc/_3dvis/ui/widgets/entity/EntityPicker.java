package gov.mil.otc._3dvis.ui.widgets.entity;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EntityPicker extends TransparentWindow {

    public static IEntity show(Stage parentStage) {
        return show(parentStage, null);
    }

    public static IEntity show(Stage parentStage, List<Class<?>> entityClassFilter) {
        EntityPicker entityPicker = new EntityPicker(parentStage, entityClassFilter, false);
        entityPicker.createAndShow(true);
        return entityPicker.selectedEntity;
    }

    public static List<IEntity> show(Stage parentStage, boolean selectMultiple) {
        return show(parentStage, null, selectMultiple);
    }

    public static List<IEntity> show(Stage parentStage, List<Class<?>> entityClassFilter, boolean selectMultiple) {
        EntityPicker entityPicker = new EntityPicker(parentStage, entityClassFilter, selectMultiple);
        entityPicker.createAndShow(true);
        return entityPicker.selectedEntityList;
    }

    private final TableView<EntityView> entityTableView = new TableView<>();
    private final List<Class<?>> entityClassFilter;
    private final CheckBox filterEntitiesInScope = new CheckBox("filter entities not in current scope");
    private IEntity selectedEntity = null;
    private final List<IEntity> selectedEntityList = new ArrayList<>();
    private final boolean selectMultiple;

    private EntityPicker(Stage parentStage, List<Class<?>> entityClassFilter, boolean selectMultiple) {
        super(parentStage);
        this.entityClassFilter = entityClassFilter;
        this.selectMultiple = selectMultiple;
    }

    @Override
    protected Pane createContentPane() {
        filterEntitiesInScope.setOnAction(event -> loadEntities());
        filterEntitiesInScope.setAlignment(Pos.CENTER_RIGHT);

        initializeTable();
        loadEntities();

        HBox filterHBox = new HBox(UiConstants.SPACING, filterEntitiesInScope);
        filterHBox.setAlignment(Pos.CENTER_LEFT);

        Button okButton = new Button("OK");
        okButton.setOnAction(event -> onOkAction());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> onCancelAction());

        Hyperlink hyperlink = new Hyperlink("refresh");
        hyperlink.setOnAction(event -> loadEntities());

        HBox refreshHBox = new HBox(UiConstants.SPACING, hyperlink);
        refreshHBox.setAlignment(Pos.CENTER_LEFT);

        HBox closeButtonsHBox = new HBox(UiConstants.SPACING);
        closeButtonsHBox.getChildren().addAll(okButton, cancelButton);
        closeButtonsHBox.setAlignment(Pos.CENTER_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.getChildren().addAll(
                createTitleLabel("Entity Picker"),
                new Separator(),
                filterHBox,
                entityTableView,
                refreshHBox,
                new Separator(),
                closeButtonsHBox);
        mainVBox.setMinWidth(600);

        return mainVBox;
    }

    private void initializeTable() {
        TableColumn<EntityView, EntityId> entityIdTableColumn = new TableColumn<>("Entity ID");
        entityIdTableColumn.setCellValueFactory(new PropertyValueFactory<>("entityId"));

        TableColumn<EntityView, String> nameTableColumn = new TableColumn<>("Name");
        nameTableColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        entityTableView.getColumns().add(entityIdTableColumn);
        entityTableView.getColumns().add(nameTableColumn);

        if (selectMultiple) {
            entityTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else {
            entityTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
        entityTableView.setPlaceholder(new Label("no entities"));

        entityTableView.setRowFactory(param -> {
            TableRow<EntityView> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    onOkAction();
                }
            });
            return row;
        });
    }

    private void loadEntities() {
        EntityView selectEntityView = entityTableView.getSelectionModel().getSelectedItem();
        IEntity currentSelection = null;
        if (selectEntityView != null) {
            currentSelection = entityTableView.getSelectionModel().getSelectedItem().entity;
        }
        selectEntityView = null;
        entityTableView.getItems().clear();
        for (IEntity entity : EntityManager.getEntities()) {
            if (!isFiltered(entity)) {
                EntityView entityView = new EntityView(entity);
                if (entity.equals(currentSelection)) {
                    selectEntityView = entityView;
                }
                entityTableView.getItems().add(entityView);
            }
        }
        if (selectEntityView != null) {
            entityTableView.getSelectionModel().select(selectEntityView);
        }
    }

    private boolean isFiltered(IEntity entity) {
        if (entityClassFilter != null && !entityClassFilter.isEmpty()) {
            for (Class<?> clazz : entityClassFilter) {
                if (entity.getClass() == clazz) {
                    return isInScopeFiltered(entity);
                }
            }
            return true;
        } else {
            return isInScopeFiltered(entity);
        }
    }

    private boolean isInScopeFiltered(IEntity entity) {
        if (filterEntitiesInScope.isSelected()) {
            return !entity.isInScope();
        }
        return false;
    }

    private void onOkAction() {
        if (selectMultiple) {
            List<EntityView> entityViews = entityTableView.getSelectionModel().getSelectedItems();
            if (entityViews != null) {
                for (EntityView entityView : entityViews) {
                    selectedEntityList.add(entityView.entity);
                }
            }
        } else {
            EntityView entityView = entityTableView.getSelectionModel().getSelectedItem();
            if (entityView != null) {
                selectedEntity = entityView.entity;
            }
        }
        close();
    }

    private void onCancelAction() {
        selectedEntity = null;
        close();
    }

    public static class EntityView {

        private final IEntity entity;

        public EntityView(IEntity entity) {
            this.entity = entity;
        }

        public String getEntityId() {
            return entity.getEntityId().toString();
        }

        public String getName() {
            return entity.getName();
        }
    }
}
