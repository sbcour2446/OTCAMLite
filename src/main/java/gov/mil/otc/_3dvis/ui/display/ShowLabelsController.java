package gov.mil.otc._3dvis.ui.display;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.ui.TransparentWindow;
import gov.mil.otc._3dvis.ui.UiConstants;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class ShowLabelsController extends TransparentWindow {

    public static void show() {
        new ShowLabelsController().createAndShow();
    }

    private final ListView<EntityCheckBox> entityListView = new ListView<>();

    @Override
    protected Pane createContentPane() {
        Button showAllButton = new Button("Show All");
        showAllButton.setOnAction(event -> {
            for (EntityCheckBox entityCheckBox : entityListView.getItems()) {
                entityCheckBox.setChecked(true);
            }
        });
        Button hideAllButton = new Button("Hide All");
        hideAllButton.setOnAction(event -> {
            for (EntityCheckBox entityCheckBox : entityListView.getItems()) {
                entityCheckBox.setChecked(false);
            }
        });

        HBox selectAllButtonHBox = new HBox(UiConstants.SPACING, showAllButton, hideAllButton);
        selectAllButtonHBox.setAlignment(Pos.CENTER_LEFT);

        for (IEntity entity : EntityManager.getEntities()) {
            EntityCheckBox item = new EntityCheckBox(entity, false);

            // observe item's on property and display message if it changes:
            item.checkedProperty().addListener((obs, wasChecked, isNowChecked) -> {
//                entity.setHighlighted(isNowChecked);
               // entity.showLabel(isNowChecked);
            });

            entityListView.getItems().add(item);
        }

        entityListView.setCellFactory(CheckBoxListCell.forListView(new Callback<EntityCheckBox, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(EntityCheckBox item) {
                return item.checkedProperty();
            }
        }));
//        entityListView.setCellFactory((ListView<IEntity> param) -> new ListCell<IEntity>() {
//            private CheckBox checkBox;
//
//            @Override
//            public void updateItem(IEntity item, boolean empty) {
//                super.updateItem(item, empty);
//                if (!(empty || item == null)) {
//                    getCheckBox().setSelected(false);
//                    setGraphic(getCheckBox());
//                    setText(item.getName() + " (" + item.getEntityId() + ")");
//                } else {
//                    setGraphic(null);
//                    setText(null);
//                }
//            }
//            private CheckBox getCheckBox(){
//                if(checkBox==null){
//                    checkBox = new CheckBox();
//                    checkBox.selectedProperty().addListener((obs, wasSelected, isNowSelected)->{
//                        if(getItem()!=null){
//                            getItem().setHighlighted(isNowSelected);
//                        }
//                    });
//                }
//                return checkBox;
//            }
//            private void setChecked(boolean checked) {
//                checkBox.setSelected(checked);
//            }
//        });
//
//        entityListView.setCellFactory(CheckBoxListCell.forListView(new Callback<IEntity, ObservableValue<Boolean>>() {
//
//            @Override
//            public ListCell<IEntity> call(ListView<IEntity> entity) {
//                return new ListCell<IEntity>() {
//
//                    @Override
//                    protected void updateItem(IEntity item, boolean empty) {
//                        super.updateItem(item, empty);
//                        if (item == null || empty) {
//                            setText(null);
//                            setGraphic(null);
//                        } else {
//                            String displayString = entity.toString();
//                            setText(displayString);
//                        }
//                    }
//                };
//            }
//
//            private CheckBox getCheckBox(){
//                if(checkBox==null){
//                    checkBox = new CheckBox();
//                    checkBox.selectedProperty().addListener((obs,old,val)->{
//                        if(getItem()!=null){
//                            getItem().isDoneProperty().setValue(val);
//                        }
//                    });
//                }
//                return checkBox;
//            }
//
//            @Override
//            public ObservableValue<Boolean> call(IEntity entity) {
//
//                BooleanProperty observable = new SimpleBooleanProperty();
//                observable.addListener((obs, wasSelected, isNowSelected) -> {
//                    entity.setHighlighted(isNowSelected);
//                });
//                return observable;
//            }
//        }));

//        for (IEntity entity : EntityManager.getEntities()) {
//            entityListView.getItems().add(entity);
//        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(event -> close());
        HBox buttonHBox = new HBox(UiConstants.SPACING, closeButton);
        buttonHBox.setAlignment(Pos.CENTER_RIGHT);

        return new VBox(UiConstants.SPACING,
                createTitleLabel("Show/Hide Entity Labels"),
                selectAllButtonHBox,
                entityListView,
                buttonHBox);
    }

    public static class EntityCheckBox {
        private final StringProperty name = new SimpleStringProperty();
        private final BooleanProperty checked = new SimpleBooleanProperty();

        public EntityCheckBox(IEntity entity, boolean checked) {
            setName(entity.getName() + " (" + entity.getEntityId() + ")");
            setChecked(checked);
        }

        public final StringProperty nameProperty() {
            return this.name;
        }

        public final String getName() {
            return this.nameProperty().get();
        }

        public final void setName(final String name) {
            this.nameProperty().set(name);
        }

        public final BooleanProperty checkedProperty() {
            return this.checked;
        }

        public final boolean isChecked() {
            return this.checkedProperty().get();
        }

        public final void setChecked(final boolean on) {
            this.checkedProperty().set(on);
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
