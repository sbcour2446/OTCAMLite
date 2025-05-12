package gov.mil.otc._3dvis.ui.display.entitydisplay;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.settings.Defaults;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;
import gov.mil.otc._3dvis.ui.widgets.entity.EntityPicker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityDisplayController extends TransparentWindow {

    public static synchronized void show() {
        new EntityDisplayController().createAndShow();
    }

    private final Map<EntityId, EntityWrapper> allEntityMap = new HashMap<>();
    private final ListView<EntityGroup> groupListView = new ListView<>();
    private final ListView<EntityWrapper> entityListView = new ListView<>();
    private final CheckBox labelCheckBox = new CheckBox("Label");
    private final ColorPicker labelColorPicker = new ColorPicker(Color.BLACK);
    private final CheckBox markCheckBox = new CheckBox("Mark");
    private final ColorPicker markColorPicker = new ColorPicker(Color.WHITE);
    private final String opacityLabelFormat = "Opacity(%d)";
    private final Label opacityLabel = new Label(String.format(opacityLabelFormat, Defaults.ICON_OPACITY));
    private final Slider opacitySlider = new Slider(1, 100, Defaults.ICON_OPACITY);
    private final Slider iconSize = new Slider(1, 100, 25);

    @Override
    protected Pane createContentPane() {
        initialize();

        HBox mainHBox = new HBox(UiConstants.SPACING, createGroupTitledPane(), createEntityTitledPane(),
                createDisplayTitledPane());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> save());
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> close());
        HBox closeButtonHBox = new HBox(UiConstants.SPACING, saveButton, cancelButton);
        closeButtonHBox.setAlignment(Pos.CENTER_RIGHT);

        VBox mainVBox = new VBox(UiConstants.SPACING,
                createTitleLabel("Entity Display"),
                new Separator(),
                mainHBox,
                closeButtonHBox);
        mainVBox.setPadding(new Insets(UiConstants.SPACING));
        mainVBox.setFillWidth(true);
        mainVBox.setPrefWidth(1000);
        return mainVBox;
    }

    private @NotNull TitledPane createGroupTitledPane() {
        Button addGroupButton = new Button("Add");
        addGroupButton.setOnAction(event -> {
            String groupName = CreateEntityGroupController.show();
            if (!groupName.isBlank()) {
                groupListView.getItems().add(new EntityGroup(groupName));
                groupListView.getSelectionModel().selectLast();
            }
        });

        Button removeGroupButton = new Button("Remove");
        removeGroupButton.setOnAction(event -> {
            List<EntityGroup> selectedItems = groupListView.getSelectionModel().getSelectedItems();
            if (selectedItems != null) {
                removeEntityGroup(selectedItems);
            }
        });

        groupListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            loadEntityList(newValue);
            loadDisplayOptions(newValue);
        });

        HBox groupHBox = new HBox(UiConstants.SPACING, addGroupButton, removeGroupButton);
        VBox groupVBox = new VBox(UiConstants.SPACING, groupHBox, groupListView);
        TitledPane groupTitledPane = new TitledPane("Group", groupVBox);
        groupTitledPane.setCollapsible(false);
        return groupTitledPane;
    }

    private @NotNull TitledPane createEntityTitledPane() {
        Button addEntityButton = new Button("Add");
        addEntityButton.setOnAction(event -> {
            addEntities(groupListView.getSelectionModel().getSelectedItem());
        });

        Button removeEntityButton = new Button("Remove");
        removeEntityButton.setOnAction(event -> {
            removeSelectedEntities(groupListView.getSelectionModel().getSelectedItem());
        });

        entityListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        HBox entityHBox = new HBox(UiConstants.SPACING, addEntityButton, removeEntityButton);
        VBox entityVBox = new VBox(UiConstants.SPACING, entityHBox, entityListView);
        TitledPane entityTitledPane = new TitledPane("Entity", entityVBox);
        entityTitledPane.setCollapsible(false);
        return entityTitledPane;
    }

    private @NotNull TitledPane createDisplayTitledPane() {
        labelCheckBox.setOnAction(event -> {
            groupListView.getSelectionModel().getSelectedItem().setShowLabal(labelCheckBox.isSelected());
        });
        labelColorPicker.setOnAction(event -> {
            groupListView.getSelectionModel().getSelectedItem().setLabelColor(labelColorPicker.getValue());
        });
        HBox labelHBox = new HBox(UiConstants.SPACING, labelCheckBox, labelColorPicker);
        labelHBox.setAlignment(Pos.CENTER_LEFT);

        markCheckBox.setOnAction(event -> {
            groupListView.getSelectionModel().getSelectedItem().setMarked(markCheckBox.isSelected());
        });
        markColorPicker.setOnAction(event -> {
            groupListView.getSelectionModel().getSelectedItem().setMarkColor(markColorPicker.getValue());
        });
        HBox markHBox = new HBox(UiConstants.SPACING, markCheckBox, markColorPicker);
        markHBox.setAlignment(Pos.CENTER_LEFT);

        opacitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            float opacity = newValue.floatValue() / 100f;
            opacityLabel.setText(String.format(opacityLabelFormat, newValue.intValue()));
            groupListView.getSelectionModel().getSelectedItem().setIconOpacity(opacity);
        });
        HBox opacityHBox = new HBox(UiConstants.SPACING, opacityLabel, opacitySlider);
        opacityHBox.setAlignment(Pos.CENTER_LEFT);

        loadDisplayOptions(null);

        VBox displayVBox = new VBox(UiConstants.SPACING, labelHBox, markHBox, opacityHBox);
        displayVBox.setFillWidth(true);
        TitledPane displayTitledPane = new TitledPane("Display", displayVBox);
        displayTitledPane.setCollapsible(false);
        return displayTitledPane;
    }

    private void initialize() {
        for (IEntity entity : EntityManager.getEntities()) {
            EntityWrapper entityWrapper = new EntityWrapper(entity);
            boolean isNewGroup = true;
            for (EntityGroup entityGroup : groupListView.getItems()) {
                if (entityGroup.getName().equalsIgnoreCase(entity.getEntityGroupName())) {
                    entityWrapper.setCurrentGroup(entityGroup);
                    isNewGroup = false;
                    break;
                }
            }

            if (isNewGroup && !entity.getEntityGroupName().isBlank()) {
                EntityGroup entityGroup = new EntityGroup(entity.getEntityGroupName());
                entityGroup.setEntityDisplay(entity.getEntityDisplay());
                entityWrapper.setCurrentGroup(entityGroup);
                groupListView.getItems().add(entityGroup);
            }

            allEntityMap.put(entity.getEntityId(), entityWrapper);
        }
    }

    private void loadEntityList(EntityGroup entityGroup) {
        entityListView.getItems().clear();

        if (entityGroup == null) {
            return;
        }

        for (IEntity entity : entityGroup.getEntityList()) {
            EntityWrapper entityWrapper = allEntityMap.get(entity.getEntityId());
            if (entityWrapper == null) {
                System.out.println("debug error");
            }
            entityListView.getItems().add(entityWrapper);
        }
    }

    private void loadDisplayOptions(EntityGroup entityGroup) {
        if (entityGroup == null) {
            labelCheckBox.setSelected(false);
            labelCheckBox.setDisable(true);
            labelColorPicker.setValue(null);
            labelColorPicker.setDisable(true);
            markCheckBox.setSelected(false);
            markCheckBox.setDisable(true);
            markColorPicker.setValue(null);
            markColorPicker.setDisable(true);
            opacitySlider.setValue(Defaults.ICON_OPACITY);
            opacitySlider.setDisable(true);
        } else {
            labelCheckBox.setSelected(entityGroup.getEntityDisplay().isShowLabel());
            labelCheckBox.setDisable(false);
            labelColorPicker.setValue(entityGroup.getEntityDisplay().getLabelColorrFx());
            labelColorPicker.setDisable(false);
            markCheckBox.setSelected(entityGroup.getEntityDisplay().isMarked());
            markCheckBox.setDisable(false);
            markColorPicker.setValue(entityGroup.getEntityDisplay().getMarkColorFx());
            markColorPicker.setDisable(false);
            opacitySlider.setDisable(false);
        }
    }

    private void removeEntityGroup(List<EntityGroup> entityGroups) {
        for (EntityGroup entityGroup : entityGroups) {
            groupListView.getItems().removeAll(groupListView.getSelectionModel().getSelectedItems());
            for (IEntity entity : entityGroup.getEntityList()) {
                entity.removeEntityGroup();
            }
        }
    }

    private void addEntities(EntityGroup entityGroup) {
        if (entityGroup == null) {
            return;
        }
        List<IEntity> entities = EntityPicker.show(getStage(), true);
        List<EntityWrapper> alreadyInGroupList = new ArrayList<>();
        if (entities != null) {
            for (IEntity entity : entities) {
                EntityWrapper entityWrapper = allEntityMap.get(entity.getEntityId());
                if (entityWrapper == null) {
                    entityWrapper = new EntityWrapper(entity);
                    allEntityMap.put(entity.getEntityId(), entityWrapper);
                } else if (entityWrapper.getCurrentGroup() != null &&
                        !entityWrapper.getCurrentGroup().equals(entityGroup)) {
                    alreadyInGroupList.add(entityWrapper);
                    continue;
                }
                entityWrapper.setCurrentGroup(entityGroup);
                entityGroup.addEntity(entity);
            }
            if (!alreadyInGroupList.isEmpty()) {
                StringBuilder message = new StringBuilder();
                message.append("The following entity(s) are already in a group.  Do you wish to move them to the selected group?");
                for (EntityWrapper entityWrapper : alreadyInGroupList) {
                    message.append(System.lineSeparator());
                    message.append(entityWrapper.toString());
                }
                if (DialogUtilities.showYesNoDialog("Already in another group", message.toString(), getStage())) {
                    for (EntityWrapper entityWrapper : alreadyInGroupList) {
                        entityWrapper.removeCurrentGroup();
                        entityWrapper.setCurrentGroup(entityGroup);
                    }
                }
            }
            loadEntityList(entityGroup);
        }
    }

    private void removeSelectedEntities(EntityGroup entityGroup) {
        if (entityGroup == null) {
            return;
        }
        List<EntityWrapper> selectedItems = entityListView.getSelectionModel().getSelectedItems();
        if (selectedItems != null) {
            for (EntityWrapper entityWrapper : selectedItems) {
                entityWrapper.removeCurrentGroup();
            }
            loadEntityList(entityGroup);
        }
    }

    private void save() {
        for (IEntity entity : EntityManager.getEntities()) {
            entity.setEntityGroup("", null);
        }
        for (EntityGroup entityGroup : groupListView.getItems()) {
            for (IEntity entity : entityGroup.getEntityList()) {
                entity.setEntityGroup(entityGroup.getName(), entityGroup.getEntityDisplay());
            }
        }
        close();
    }
}
