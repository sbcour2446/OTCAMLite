package gov.mil.otc._3dvis.ui.widgets.entity;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.IEntityListener;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.List;

public class EntityPickerOld extends TransparentWindow implements IEntityListener {

    private final ObservableList<IEntity> entities = FXCollections.observableArrayList();
    private final RadioButton byIdRadioButton = new RadioButton("By ID");
    private final RadioButton byNameRadioButton = new RadioButton("By Name");
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final ComboBox<IEntity> entityComboBox = new ComboBox<>();
    private final List<Class<?>> entityClassFilter;

    private IEntity selecteEntity = null;

    public static IEntity show(Stage parentStage) {
        return show(parentStage, null);
    }

    public static IEntity show(Stage parentStage, List<Class<?>> entityClassFilter) {
        EntityPickerOld entityPicker = new EntityPickerOld(parentStage, entityClassFilter);
        entityPicker.createAndShow(true);
        return entityPicker.selecteEntity;
    }

    private EntityPickerOld(Stage parentStage, List<Class<?>> entityClassFilter) {
        super(parentStage);
        this.entityClassFilter = entityClassFilter;
    }

    @Override
    public void onEntityAdded(IEntity entity) {
        Platform.runLater(() -> {
            if (!entities.contains(entity) && entity.isFiltered()) {
                entities.add(entity);
            }
        });
    }

    @Override
    public void onEntityUpdated(IEntity entity) {
        //do nothing
    }

    @Override
    public void onEntityDisposed(IEntity entity) {
        Platform.runLater(() -> entities.remove(entity));
    }

    @Override
    protected Pane createContentPane() {
        Label title = new Label("Entity Picker");

        CheckBox filterEntitiesInScope = new CheckBox("filter entities not in current scope");

        byNameRadioButton.setSelected(true);
        toggleGroup.getToggles().add(byIdRadioButton);
        toggleGroup.getToggles().add(byNameRadioButton);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> onToggleViewAction());

        Button okButton = new Button("OK");
        okButton.setOnAction(event -> onOkAction());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> onCancelAction());

        HBox viewButtonsHBox = new HBox(UiConstants.SPACING);
        viewButtonsHBox.getChildren().addAll(byNameRadioButton, byIdRadioButton, filterEntitiesInScope);

        entityComboBox.setMaxWidth(Double.MAX_VALUE);
        entityComboBox.setMinWidth(200);
        entityComboBox.setItems(entities);
        entityComboBox.setCellFactory(new ComboBoxCellFactory());
        entityComboBox.setButtonCell(entityComboBox.getCellFactory().call(null));
        entityComboBox.valueProperty().addListener((observable, oldValue, newValue) ->
                selecteEntity = entityComboBox.getSelectionModel().getSelectedItem());

        Hyperlink hyperlink = new Hyperlink("refresh");
        hyperlink.setOnAction(event -> loadEntities());

        HBox closeButtonsHBox = new HBox(UiConstants.SPACING);
        closeButtonsHBox.getChildren().addAll(okButton, cancelButton);

        VBox mainVBox = new VBox(UiConstants.SPACING);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.getChildren().addAll(
                title,
                new Separator(),
                viewButtonsHBox,
                entityComboBox,
                hyperlink,
                new Separator(),
                closeButtonsHBox);

        return mainVBox;
    }

    @Override
    protected boolean onShowing() {
        double x = getParentStage().getX() + getParentStage().getWidth() / 2 - 125;
        double y = getParentStage().getY() + getParentStage().getHeight() / 2 - 70;
        getStage().setX(x);
        getStage().setY(y);

        loadEntities();
        EntityManager.addEntityListener(this);

        return true;
    }

    @Override
    protected boolean closeRequested() {
        EntityManager.removeEntityListener(this);
        return true;
    }

    private void loadEntities() {
        IEntity currentSelection = selecteEntity;
        entities.clear();
        if (entityClassFilter == null || entityClassFilter.isEmpty()) {
            entities.addAll(EntityManager.getEntities());
        } else {
            for (IEntity entity : EntityManager.getEntities()) {
                if (!isFiltered(entity)) {
                    entities.add(entity);
                }
            }
        }
        if (currentSelection != null) {
            entityComboBox.getSelectionModel().select(currentSelection);
        }
    }

    private boolean isFiltered(IEntity entity) {
        for (Class<?> clazz : entityClassFilter) {
            if (entity.getClass() == clazz) {
                return false;
            }
        }
        return true;
    }

    private void onToggleViewAction() {
        loadEntities();
    }

    private void onOkAction() {
        close();
    }

    private void onCancelAction() {
        selecteEntity = null;
        close();
    }

    private final class ComboBoxCellFactory implements Callback<ListView<IEntity>, ListCell<IEntity>> {
        @Override
        public ListCell<IEntity> call(ListView<IEntity> param) {
            return new ListCell<>() {
                @Override
                public void updateItem(IEntity item,
                                       boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(getEntityDisplay(item));
                    } else {
                        setText("");
                    }
                }
            };
        }

        private String getEntityDisplay(IEntity entity) {
            if (byIdRadioButton.isSelected()) {
                return entity.getEntityId().toString();
            }
            EntityDetail entityDetail = entity.getEntityDetail();
            if (entityDetail == null) {
                return entity.getEntityId().toString();
            } else {
                return entityDetail.getName();
            }
        }
    }
}
